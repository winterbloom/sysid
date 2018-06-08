import com.pi4j.component.adafruithat.AdafruitServo;
import com.pi4j.component.adafruithat.AdafruitServoHat;

public class BigMotor implements Runnable {

    private final int Address = 0X40;
    private String[] pwms;
    private String[] dirs;
    private final float minPulse = 0.001f;
    private final float nPulse = 1.00f;
    private final float maxPulse = 1.999f;
    private float[] powers;
    private MotorSignal signal;

    public BigMotor(String[] pwmPorts, String[] dirPorts, MotorSignal signal) {
	pwms = new String[pwmPorts.length];
	dirs = new String[dirPorts.length];
	for(int i = 0; i < pwmPorts.length; i++) {
	    pwms[i] = pwmPorts[i];
	    dirs[i] = dirPorts[i];
	}
	powers = new float[pwmPorts.length];
	this.signal = signal;
    }
    
    public void run() {
	
	AdafruitServoHat hat = new AdafruitServoHat(Address);
	AdafruitServo[] servos = new AdafruitServo[pwms.length];
        AdafruitServo[] dirport = new AdafruitServo[dirs.length];
	for(int i = 0; i < pwms.length; i++) {
	    servos[i] = hat.getServo(pwms[i]);
	    dirport[i] = hat.getServo(dirs[i]);
	    servos[i].setOperatingLimits(minPulse, nPulse, maxPulse);
	    dirport[i].setOperatingLimits(minPulse, nPulse, maxPulse);
	    servos[i].setPositionRange(0.0f, 100.0f);
	}

	Runtime.getRuntime().addShutdownHook(new Thread() {
	    public void run() { 
		 System.out.println("Turn off all motors");
		 hat.stopAll();		    	
	    }
	});

	System.out.println("Motors ready!");

	while(!Thread.currentThread().isInterrupted()) {
	    powers = signal.getPower();
	    for(int i = 0; i < servos.length; i++) {
		servos[i].setPosition(Math.abs(powers[i]));
		dirport[i].setPosition(powers[i] < 0 ? 0 : 1);
	    }
	}
    }
    
}
	    
	    
