/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: Java Library (Core)
 * FILENAME      :  AdafruitServoHatExample.java
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
import com.pi4j.component.adafruithat.AdafruitServo;
import com.pi4j.component.adafruithat.AdafruitServoHat;
/**
 * Example program demonstrating use of the AdafruitServoHat and AdafruitServo classes 
 * for information on the "Adafruit Servo HAT" 
 * <p>
 * <a href="https://www.adafruit.com/products/2327">See ServoHAT</a>
 * <p>
 * 
 * In this simple example a single servo is attached to the Adafruit Servo HAT. It
 * is commanded to move to various positions.
 * 
 * @author Eric Eliason
 * @see com.pi4j.component.adafruithat.AdafruitServo
 * @see com.pi4j.component.adafruithat.AdafruitServoHat
 */
public class AdafruitServoHatExample {

	public static void main(String[] args) {
		final int servoHATAddress = 0X40;
		AdafruitServoHat servoHat = new AdafruitServoHat(servoHATAddress);
		AdafruitServo servo  = servoHat.getServo("S01");
		
		/*
		 * Because the Adafruit servo HAT uses PWMs that pulse independently of
		 * the Raspberry Pi the servos will continue to drain power
		 * if the program abnormally terminates. 
		 * A shutdown hook like the one in this example is useful to stop the 
		 * servos when the program is abnormally interrupted.
		 */		
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() { 
		    	System.out.println("Turn off all servos");
		    	servoHat.stopAll();		    	
		    }
		 });
				
		//Set pulse width operating limits of servo, consult servo data sheet
		//for manufaturer's recommended operating limits. Values
		//are in milliseconds;
		float minimumPulseWidth = 0.6f;
		float neutralPulseWidth = 1.5f;
		float maximumPulseWidth = 2.4f;
		servo.setOperatingLimits(minimumPulseWidth, neutralPulseWidth, maximumPulseWidth);
		
		//Set relative range of servo for setPosition() commanding
		//(The default is 0.0 to 1.0)
		servo.setPositionRange(0.0F, 100.0f);

		//Option 1: Command servo position by using pulse width
		for (float pulseWidth: new float[] {minimumPulseWidth, neutralPulseWidth, maximumPulseWidth}) {
			servo.setPulseWidth(pulseWidth);
			servoHat.sleep(1000);
		}		

		//Move servo to neutral position.
		servo.setPulseWidth(neutralPulseWidth);
		servoHat.sleep(1000);
		
		//Option 2: Command servo position by relative values			
		for (float position: new float[] {0.0f, 10.0f, 20.0f, 30.0f, 040.0f, 50.0f, 60.0f, 70.0f, 80.0f, 90.0f, 100.00f, 0.0f, 100.0f, 0.0f, 100.0f}) {
			System.out.format("Move to position: %8.1f\n", position);
			servo.setPosition(position);
			servoHat.sleep(1000);
		}
		
		//stop all servos
		servoHat.stopAll();
	}	
}
