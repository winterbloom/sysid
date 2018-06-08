import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.GpioPinDigitalInput;

public class BigMain {

    private static final String[] ports = {"S01", "S02", "S03"};
    private static final String[] dport = {"S05", "S06", "S07"};
    private static final GpioController gpio = GpioFactory.getInstance();
    private static final GpioPinDigitalInput button = gpio.provisionDigitalInputPin(RaspiPin.GPIO_23);
    private static long startTime = 0;
    private static final long dataTimeStep = 100000000;
    private static final int expMultiplier = 5;
    
    public static void main(String[] args) throws InterruptedException {
	
	MotorSignal power = new MotorSignal(ports.length);
	Thread motors = new Thread(new BigMotor(ports, dport, power));
	DataCollector manager = new DataCollector();
	float[][] currExperiment = new float[3][3];
	boolean doneWithExperiment = false;
	Double[] powers = new Double[3];
	
	Thread.sleep(100);
	motors.start();
	Thread.sleep(1000);

	currExperiment = genExperiment();
	long lastTime;
	long thisTime;
	
	for(int i = 0; i < 20; i += 2) {
	    
            int section = 0;
	    startTime = System.nanoTime();
	    lastTime = getMuTime();
	    
	    while(!doneWithExperiment) {
		
		for(int q = 0; q < expMultiplier; q++) {
		    
		    power.setPower(currExperiment[section]);
		    thisTime = getMuTime();
                    System.out.println(getMuTime());
		    powers = floatsToDoubles(currExperiment[section]);
		    
		    while((lastTime % dataTimeStep) < (thisTime % dataTimeStep)) {
			lastTime = thisTime;
			thisTime = getMuTime();
		    }

		    System.out.println(lastTime % dataTimeStep + ", " + thisTime % dataTimeStep);
		    
		    lastTime = getMuTime();
 
		    manager.collectData(powers);
		    
		}

		section++;
                if(section >= currExperiment.length) {
		    doneWithExperiment = true;
		}
		
	    }

            System.out.println("Waiting...");
	    
	    while(button.isLow()) {
	        Thread.sleep(10);
	    }

	    doneWithExperiment = false;
	    currExperiment = genExperiment();
        
	    Thread.sleep(1000);
	    
	}

	System.out.println("DONE!!!");
	    
    }

    public static long getMuTime() {
        return (System.nanoTime() - startTime);
    }

    public static float[][] genExperiment() {
	float[][] experiments = new float[4][3];
	for(int i = 0; i < experiments.length - 1; i++) {
	    for(int j = 0; j < experiments[i].length; j++) {
		experiments[i][j] = (float) (Math.random() * 200.0) - 100.0f;
	    }
	}
	
	for(int i = 0; i < experiments[experiments.length - 1].length; i++) {
	    experiments[experiments.length - 1][i] = 0.0f;
	}
	
	return experiments;
    }

    public static Double[] floatsToDoubles(float[] input) {
        if (input == null) {
            return null;
        }
	
        Double[] output = new Double[input.length];
        for (int i = 0; i < input.length; i++) {
            output[i] = new Double(input[i]);
        }
	
        return output;
    }
    
}
