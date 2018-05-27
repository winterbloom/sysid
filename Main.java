import java.util.HashMap;
import java.util.List;
import java.util.*;

public class Main {

    public static Thread sense;
    public static Thread drive;

    public static volatile float senseVal = 0f;
    public static volatile float senseFilter = 0f;
    public static volatile float power = 0f;

    private static final long nanosPerSecond = 1000000000;
    private static final long step = (long)(nanosPerSecond * .05);

    private static Float[] coefs = {50f, 0.1f, 0f};
       
    public static void main(String[] args) throws InterruptedException {
	/*Float[][] arry = {{2f, 1f, 1f}, {4f, -6f, 0f}, {-2f, 7f, 2f}};
	Float[] vec = {5f, -2f, 9f};
	Float[] uruur = eliminate(arry, vec);
	System.out.println("uruur: " + Arrays.toString(uruur));
	*/
	//Thread.sleep(10000);
        sense = new Thread(new Sensors());
        drive = new Thread(new Actuators());
	sense.start();
	drive.start();
	Thread.sleep(1000);

	//Create position profile
	HashMap<Long, Float> posPf = new HashMap<>();

	/*	posPf.put((long)0, 0f);
	posPf.put(step, 1f);
	posPf.put(2*step, 4f);
	posPf.put(3*step, 9f);
	posPf.put(4*step, 16f);
	posPf.put(5*step, 25f);
	posPf.put(6*step, 36f);
	posPf.put(7*step, 49f);
	posPf.put(8*step, 62f);
	posPf.put(9*step, 73f);
	posPf.put(10*step, 82f);
	posPf.put(11*step, 89f);
	posPf.put(12*step, 94f);
	posPf.put(13*step, 97f);
	posPf.put(14*step, 98f);*/

	for(int i = 0; i < 15; i++) {
	    if(i < 15) {
		posPf.put(step * i, (float) (i / 2f) * (i / 2f) * (i / 20f));
	    } else {
		posPf.put(step * i, 98f - ((28f - i) / 2f) * ((28f - i) / 2f) + i * 0.1f);
	    }
	}

	System.out.println("posPf: " + posPf + "\n");

	HashMap<Long, Float> accels = getAccel(posPf);

	HashMap<Long, Float> predict = new HashMap<Long, Float>();
        HashMap<Long, Float> predictDeriv = new HashMap<Long, Float>();
        predict = (getDeriv(posPf, step / 2));

        for(int i = 1; i < predict.size() - 1; i++) {
	    predictDeriv.put(step * i, (predict.get(step * i - step / 2) + predict.get(step * i + step / 2)) / 2);
        }

	extrapolate(predictDeriv);

	for (Long name : accels.keySet()) {
	    System.out.println("accels: " + name.toString() + ": " + accels.get(name).toString());
	}
	
	do {
	    float zero = getZero();

	    //Create power profile
	    HashMap<Long, Float> powerPf = new HashMap<>();
	    for (int i = 0; i < accels.size(); i++) {
		
		powerPf.put(i*step, coefs[0] + coefs[1] * accels.get(step * i) + coefs[2] * predictDeriv.get(step * i));
	    }

	    System.out.println("posPf: " + posPf + "\n");
	    System.out.println("posPf deriv: " + getDeriv(posPf, step / 2) + "\n");

	    HashMap<Long, Float> actualPos = new HashMap<>();

	    //Run power profile
	    long start = System.nanoTime();
	    long lastTime = step - 1;
	    while (System.nanoTime() - start < (powerPf.size() - 1) * step) {
		//Interpolator
		long time = System.nanoTime() - start;
		float low = powerPf.get(time - time % step);
		float high = powerPf.get(time - (time % step) + step);
		float interVal = low + (high - low) * (time % step)/(float)step;
		//System.out.println("low: " + low);
		//System.out.println("high: " + high);
		//System.out.println("inter: " + interVal);
		power = interVal;
		try { Thread.sleep(1); } catch (InterruptedException e) {}
	        if(lastTime % step > time % step) {
		    actualPos.put(time - time % step, senseVal - zero);
		}
		lastTime = time;
	    }
	    System.out.println("actualPos: " + actualPos + "\n");
	    HashMap<Long, Float> actual = new HashMap<Long, Float>();
	    HashMap<Long, Float> actualDeriv = new HashMap<Long, Float>();
	    actual = (getDeriv(actualPos, step / 2));

	    HashMap<Long, Float> actAccel = getAccel(actualPos);

	    for(int i = 1; i < actual.size() - 1; i++) {
		actualDeriv.put(step * i, (actual.get(step * i - step / 2) + actual.get(step * i + step / 2)) / 2);
	    }
	    System.out.println("predictDeriv: " + predictDeriv + "\n");
	    System.out.println("actualDeriv: " + actualDeriv + "\n");

	    //Performs regression on data
	    ArrayList<Float[]> vectors = new ArrayList<Float[]>();
	    for(int i = 1; i < actualDeriv.size(); i++) {
		for(int j = i + 1; j < actualDeriv.size(); j++) {
		    for(int k = j + 1; k < actualDeriv.size(); k++) {
			Float[][] matrix = {{1f, powerPf.get(step * i) * -1, actualDeriv.get(step * i)},
					    {1f, powerPf.get(step * j) * -1, actualDeriv.get(step * j)},
					    {1f, powerPf.get(step * k) * -1, actualDeriv.get(step * k)},
			};
			Float[] out = {actAccel.get(i * step), actAccel.get(j * step), actAccel.get(k * step)};
			Float[] estimate = new Float[3];
			//System.out.println("out: " + Arrays.toString(out));
			//System.out.println("matrix: " + Arrays.toString(matrix[0]) + " " + Arrays.toString(matrix[1]) + " " + Arrays.toString(matrix[2]));
			//Thread.sleep(1000);
			estimate = eliminate(matrix, out);
			if (!(Double.isNaN(estimate[0]) || Double.isNaN(estimate[2]) || Double.isNaN(estimate[1]) ||
			      Double.isInfinite(estimate[0]) || Double.isInfinite(estimate[1]) || Double.isInfinite(estimate[2]))) {
			    System.out.println("estimate: " + Arrays.toString(estimate));
			    vectors.add(estimate);
			}
		    }
		}
	    }

	    double[] avgs = new double[vectors.get(0).length];

	    for (int i = 0; i < vectors.get(0).length; i++) {
		for (Float[] x : vectors) {
		    avgs[i] += x[i];
		}
		avgs[i] /= vectors.size() * vectors.size();
	    }

	    System.out.println("avgs: " + Arrays.toString(avgs));
	    
	} while(false);
    }

    private static HashMap<Long, Float> getAccel(HashMap<Long, Float> pos) {
	HashMap<Long, Float> accels = getDeriv(getDeriv(pos, step/2), step);
	//Extrapolate the points at 0 and the end
	extrapolate(accels);
	return accels;
    }

    private static HashMap<Long, Float> extrapolate(HashMap<Long, Float> hash) {
	hash.put((long)0, 2*hash.get(step) - (hash.get(2*step)));
	hash.put(hash.size()*step, 2*hash.get((hash.size()-1)*step) - hash.get((hash.size()-2)*step));
	return hash;
    }

    private static HashMap<Long, Float> getDeriv(HashMap<Long, Float> points, long offset) {
	HashMap<Long, Float> derivs = new HashMap(points.size() - 1);
	for (int i = 0; i < points.size() - 2; i++) {
	    //Calculate the numerical derivative between i and i+1
	    derivs.put(offset + step*i, (float)((points.get(i*step + offset - step / 2)-points.get((i+1)*step + offset - step / 2))/((double)step/(double)nanosPerSecond)));
	}
	return derivs;
    }

    private static float getZero() {
	float sum = 0;
	int readings = 10;
	for(int i = 0; i < readings; i++) {
	    sum += senseVal;
	    try {
		Thread.sleep(150);
	    } catch(InterruptedException e) {}
	}
        return sum/readings;
    }

    public static float getPower() {
	return power;
    }

    public static void setSense(float sense, float filter) {
	senseVal = sense;
	senseFilter = filter;
    }

    public float interpolate(HashMap<Long, Float> hash, long start, long time) {
       	float low = hash.get(time - time % step);
       	float high = hash.get(time - (time % step) + step);
       	float interVal = low + (high - low) * (time % step)/(float)step;
       	return interVal;
    }

    private static Float[] eliminate(Float[][] matrix, Float[] solution) {
        //Transforms matrix into upper triangualar form
	for(int i = 0; i < matrix.length; i++) {
	    for(int j = i + 1; j < matrix.length; j++) {
		Float mulFact = matrix[j][i]/matrix[i][i];
		//System.out.println(Arrays.toString(matrix[j]));
	        matrix[j][i] = 0f;
		solution[j] -= solution[i] * mulFact;
		for(int q = i + 1; q < matrix.length; q++) {
		    matrix[j][q] -= matrix[i][q] * mulFact;
		    //System.out.println(matrix[i][q] * mulFact);
		}
		//System.out.println(Arrays.toString(matrix[j]));
	    }
	}

	Float[] result = new Float[matrix.length];
	Arrays.fill(result, 0f);

	System.out.println("solution: " + Arrays.toString(solution));
	System.out.println("matrix: " + Arrays.toString(matrix[0]) + " " + Arrays.toString(matrix[1]) + " " + Arrays.toString(matrix[2]));
	for(int i = matrix.length - 1; i >= 0; i--) {
	    //System.out.println("i: " + i);
	    //System.out.println("solution[i]: " + solution[i]);
	    //System.out.println("matrix[i][i]: " + matrix[i][i]);
	    result[i] = solution[i]/matrix[i][i];
	    //System.out.println("result: " + Arrays.toString(result));
	    matrix[i][i] = 0f;
	    solution[i] = 0f;
	    for (int j = i - 1; j >= 0; j--) {
		solution[j] -= matrix[j][i] * result[i];
		System.out.println("solution: " + Arrays.toString(solution));
	    }
	}

	return result;
    }
}
