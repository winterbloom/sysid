import com.pi4j.component.adafruithat.AdafruitDcMotor;
import com.pi4j.component.adafruithat.AdafruitMotorHat;

public class MotorPower {
    public static void main (String[] args) {
	AdafruitMotorHat motorHat = new AdafruitMotorHat(0X60);
	AdafruitDcMotor motor = motorHat.getDcMotor("M3");
	float motorMax = 100.0f;
	motor.setPowerRange(motorMax);

	//Check for argument
	if (args.length == 0) {
	    System.out.println("Usage: MotorPower targetPower");
	    System.exit(1);
	}

	//Check for type of argument
	float targetPower = 0f;
	try {
	    targetPower = (float) Double.parseDouble(args[0]);
	} catch (Exception ex) {
	    System.out.println("targetPower must be a float");
	    System.exit(1);
	}

	//Check for range of argument	
	if (targetPower > motorMax || targetPower < -motorMax) {
	    System.out.println("targetPower must be within " + motorMax + " and -" + motorMax);
	    System.exit(1);
	}

	//Run motor
	for (int i=0;i<500;i++) {
	    motor.speed(targetPower);
	}

	motor.stop();
	System.exit(0);
    }
}
