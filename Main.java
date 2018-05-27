import java.util.HashMap;

public class Main {

    public static Thread sense;
    public static Thread drive;

    public static volatile float senseVal = 0f;
    public static volatile float senseFilter = 0f;
    public static volatile float power = 0f;

    private static final long nanosPerSecond = 1000000000;
    private static final long step = (long)(nanosPerSecond * .1);
       
    public static void main(String[] args) throws InterruptedException {	
        sense = new Thread(new Sensors());
        drive = new Thread(new Actuators());
	sense.start();
	drive.start();
	Thread.sleep(1000);

	//Create position profile
	HashMap<Long, Float> posPf = new HashMap<>();
	posPf.put((long)0, 0f);
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
	posPf.put(14*step, 98f);

	HashMap<Long, Float> accels = getAccel(posPf);

	for (Long name : accels.keySet()) {
	    System.out.println(name.toString() + ": " + accels.get(name).toString());
	}
	
	float gravity = 350;
	float mass = .13f;
	
	while(!false) {
	    float zero = getZero();

	    //Create power profile
	    HashMap<Long, Float> powerPf = new HashMap<>();
	    for (int i = 0; i < accels.size(); i++) {
		
		powerPf.put(i*step, mass * (accels.get(i*step) - gravity));
	    }

	    System.out.println(powerPf);

	    //Run power profile
	    long start = System.nanoTime();
	    while (System.nanoTime() - start < (powerPf.size() - 1) * step) {
		//Interpolator
		long time = System.nanoTime() - start;
		float low = powerPf.get(time - time % step);
		float high = powerPf.get(time - (time % step) + step);
		float interVal = low + (high - low) * (time % step)/(float)step;
		System.out.println("low: " + low);
		System.out.println("high: " + high);
		System.out.println("inter: " + interVal);
		power = interVal;
		try { Thread.sleep(10); } catch (InterruptedException e) {}
	    }
	}
    }

    private static HashMap<Long, Float> getAccel(HashMap<Long, Float> pos) {
	HashMap<Long, Float> accels = getDeriv(getDeriv(pos, step/2), step);
	//Extrapolate the points at 0 and the end
	accels.put((long)0, 2*accels.get(step) - (accels.get(2*step)));
	accels.put(accels.size()*step, 2*accels.get((accels.size()-1)*step) - accels.get((accels.size()-2)*step));
	return accels;
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
}
