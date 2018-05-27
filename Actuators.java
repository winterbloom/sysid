import com.pi4j.component.adafruithat.AdafruitDcMotor;
import com.pi4j.component.adafruithat.AdafruitMotorHat;

public class Actuators implements Runnable {
    public void run() {
	AdafruitMotorHat motorHat = new AdafruitMotorHat(0X60);

	Runtime.getRuntime().addShutdownHook(new Thread() {
		public void run() {
		    System.out.println("Turning off all motors");
		    motorHat.stopAll();
		}
	    });

	AdafruitDcMotor motor = motorHat.getDcMotor("M3");
	motor.setPowerRange(100.0f);

	int loopCount = 0;	
	while(!Thread.currentThread().isInterrupted()) {
	    float temp = UltraMain.getPower();
	    motor.speed(temp);
	    //motor.speed(40);
	    System.out.println(loopCount++ + ": " + temp);
	}
	    
	
	motor.stop();
    }
}
