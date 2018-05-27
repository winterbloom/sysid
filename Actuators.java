import com.pi4j.component.adafruithat.AdafruitDcMotor;
import com.pi4j.component.adafruithat.AdafruitMotorHat;

public class Actuators implements Runnable {

    private AdafruitDcMotor motor;
    
    public void run() {
	AdafruitMotorHat motorHat = new AdafruitMotorHat(0X60);

	Runtime.getRuntime().addShutdownHook(new Thread() {
		public void run() {
		    System.out.println("Turning off all motors");
		    motor.stop();
		    motorHat.stopAll();
		}
	    });

	motor = motorHat.getDcMotor("M3");
	motor.setPowerRange(100.0f);

	int loopCount = 0;	
	while(!Thread.currentThread().isInterrupted()) {
	    float temp = Main.getPower();
	    motor.speed(temp);
	    //motor.speed(40);
	}
	    
	
	motor.stop();
    }
}
