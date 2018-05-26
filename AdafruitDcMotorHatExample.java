/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: Java Library (Core)
 * FILENAME      :  AdafruitDcMotorExample.java
 *
 * This file is part of the Pi4J project. More information about
 * this project can be found here:  http://www.pi4j.com/
 * **********************************************************************
 * %%
 * Copyright (C) 2012 - 2016 Pi4J
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
import com.pi4j.component.adafruithat.AdafruitDcMotor;
import com.pi4j.component.adafruithat.AdafruitMotorHat;

/**
 * Example program commanding a DC Motor wired to a AdafruitMotorHat.
 * <p>
 * <a href="https://www.adafruit.com/products/2348">See MotorHAT</a>
 * <p>
 * In this example four DC motors are wired to the Adafruit motor HAT. They are commanded at
 * different speeds and motor direction.
 * 
 * @author Eric Eliason
 * @see com.pi4j.component.adafruithat.AdafruitDcMotor
 * @see com.pi4j.component.adafruithat.AdafruitMotorHat
 *
 */
public class AdafruitDcMotorHatExample {
	 
	 
	public static void main(String[] args)  {
		final int motorHATAddress = 0X60;
		AdafruitMotorHat motorHat = new AdafruitMotorHat(motorHATAddress);
		
		/*
		 * Because the Adafruit motor HAT uses PWMs that pulse independently of
		 * the Raspberry Pi the motors will keep running at its current direction
		 * and power levels if the program abnormally terminates. 
		 * A shutdown hook like the one in this example is useful to stop the 
		 * motors when the program is abnormally interrupted.
		 */		
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() { 
		    	System.out.println("Turn off all motors");
		    	motorHat.stopAll();		    	
		    }
		 });

		AdafruitDcMotor motorLeft  = motorHat.getDcMotor("M1");
		AdafruitDcMotor motorRight = motorHat.getDcMotor("M2");
		AdafruitDcMotor motorWrist = motorHat.getDcMotor("M3");
		AdafruitDcMotor motorElbow = motorHat.getDcMotor("M4");			

		//A speed value of 100 sets the DC motor to maximum throttle.
		//The default power range is 1.0.
		motorLeft.setPowerRange(100.0f);
		motorRight.setPowerRange(100.0f);
		motorWrist.setPowerRange(100.0f);
		motorElbow.setPowerRange(100.0f);
		
		/*
		 * Pass through several speeds in forward and reverse direction for all motors.
		 * Negative values for reverse direction, positive is forward direction. 
		 */			
		for (float speed: new float[] {-100.0f, -75.0f, -50.0f, -35.0f, 0.0f, 25.0f, 50.0f, 75.0f, 100.0f}) {
			System.out.format("Set speed to: %6.1f\n", speed);
			motorLeft.speed(speed);			
			motorRight.speed(speed);
			motorWrist.speed(speed);
			motorElbow.speed(speed);
			
			//motorHat.sleep() will stop all motors if interrupted.
			motorHat.sleep(5000);
		}
		
		//Stop motors in brake mode.
		motorLeft.setBrakeMode(true);
		motorLeft.stop();
		
		motorRight.setBrakeMode(true);
		motorRight.stop();
		
		motorWrist.setBrakeMode(true);
		motorWrist.stop();
		
		motorElbow.setBrakeMode(true);
		motorElbow.stop();
		
		motorHat.sleep(2000);

		//Now command just the left motor.
		//For future events stop by coasting.
		motorLeft.setBrakeMode(false);
		
		//Set power but do not set or change the motor state (stop, forward, reverse)
		//The power value will be used with the next forward() or reverse() command and 
		//does not otherwise change the current motor power level.
		motorLeft.setPower(100.0f);
		
		//move forward at power level specified above
		System.out.println("Move foward");
		motorLeft.forward();
		motorHat.sleep(2000);
		
		//coast to a stop
		motorLeft.stop();
		motorHat.sleep(2000);
		
		//move reverse at current power level
		System.out.println("Move reverse");
		motorLeft.reverse();
		motorHat.sleep(2000);
		
		//coast to a stop 
		motorLeft.stop();
		motorHat.sleep(2000);
		
		//move forward then stop after 5 seconds.
		System.out.println("Move forward 5 seconds.");
		motorLeft.forward(5000);
		
	}
}