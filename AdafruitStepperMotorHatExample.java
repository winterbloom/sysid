/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: Java Library (Core)
 * FILENAME      :  AdafruitStepperMotorHatExample.java
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
import com.pi4j.component.adafruithat.AdafruitMotorHat;
import com.pi4j.component.adafruithat.AdafruitStepperMotor;
import com.pi4j.component.adafruithat.StepperMode;
/**
 * Demonstrate commanding a single stepper motor wired to an "Adafruit DC and Stepper Motor HAT"
 * for the Raspberry Pi computer. 
 * <p>
 * <a href="https://www.adafruit.com/products/2348">See MotorHAT</a>
 * <p>
 * 
 * @author Eric Eliason
 * @see com.pi4j.component.adafruithat.AdafruitStepperMotor
 * @see com.pi4j.component.adafruithat.AdafruitMotorHat
 */
public class AdafruitStepperMotorHatExample {
	
	public static void main(String[] args) {		
		final int motorHATAddress = 0X60;
		
		//create instance of a motor HAT
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
			
		
		
		//Create an instance for this stepper motor. A motorHAT can command
		//two stepper motors ("SM1" and "SM2")
		AdafruitStepperMotor stepper = motorHat.getStepperMotor("SM1");
			
		//Set Stepper Mode to SINGLE_PHASE
		stepper.setMode(StepperMode.SINGLE_PHASE);
		
		//Set the number of motor steps per 360 degree
		//revolution for this stepper mode.
		stepper.setStepsPerRevolution(200);
		
		//Time between each step in milliseconds. 
		//In this example, "true" indicates to terminate if 
		//stepper motor can not achieve 100ms per step.
		stepper.setStepInterval(100, true);

		//forward
		stepper.step(100);
		System.out.format("currentStep: %d\n",stepper.getCurrentStep());
		
		//reverse
		stepper.step(-100);
		System.out.format("currentStep: %d\n",stepper.getCurrentStep());
		
		//Move 2.5 revolutions at the fastest possible speed in forward direction
		stepper.setStepInterval(0,false);
		stepper.rotate(2.5);
		System.out.format("currentStep: %d\n",stepper.getCurrentStep());
		
		//Move 2.5 revolutions at the fastest possible speed in reverse direction
		stepper.setStepInterval(0,false);
		stepper.rotate(-2.5);
		System.out.format("currentStep: %d\n",stepper.getCurrentStep());
		
		//Stop all motors attached to this HAT
		motorHat.stopAll();
	
	}
}
    