package com.pi4j.component.adafruithat;
/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: Java Library (Core)
 * FILENAME      :  AdafruitMotorHat.java
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
import java.util.HashMap;
import java.util.Map;
/**
 * This class extends the AdafruitHat superclass and handles the specific operating
 * commanding for DC and Stepper motors. 
 *
 * @author Eric Eliason
 * @see com.pi4j.component.adafruithat.AdafruitHat
 */
public class AdafruitMotorHat extends AdafruitHat {
	
	private double maxFreq = 1526;

	//Tracks if a DC motor has already been allocated.
	private Map<String,Boolean> dcMotorAllocated = new HashMap<String,Boolean>();
	{
		dcMotorAllocated.put("M1",false); //designation for DC Motor
		dcMotorAllocated.put("M2",false);
		dcMotorAllocated.put("M3",false);
		dcMotorAllocated.put("M4",false);
	}
	
	//Tracks if a Stepper motor has already been allocated.
	private Map<String,Boolean> stepperMotorAllocated = new HashMap<String,Boolean>();
	{
		stepperMotorAllocated.put("SM1",false);
		stepperMotorAllocated.put("SM2",false);
	}	
		
	public AdafruitMotorHat(int deviceAddr) {
		super(deviceAddr);
		setup();
	}
	
	public AdafruitMotorHat(int deviceAddr, int i2cBus) {
		super(deviceAddr,i2cBus);
		setup();
	}
	
	/**
	 * Set the maximum PWM frequency of the PC9885 chip for minimal motor vibration.
	 */
	private void setup() {
		setPwmFreq(maxFreq);		
	}

	/**
	 * Create a AdafruitDcMotor instance for this motor
	 * @param motor valid values "M1", "M2", "M3", "M4"
	 * @return AdafruitDcMotor instance for the specified motor
	 */
    public AdafruitDcMotor getDcMotor(String motor) {
    	
    	//Is motor string valid?
    	boolean hit = false;
    	for (String  motorTest: dcMotorAllocated.keySet()) {
    		if (motor == motorTest) {
    			hit = true;
    			break;
    		}
    	}
    	if (!hit) {
    		System.out.println("*** Error *** Motor name not valid. Valid values:");
    		for (String  motorTest: dcMotorAllocated.keySet()) System.out.format("\"%s\"\n", motorTest);
    		throw new IllegalArgumentException(motor);
    	}
    	
    	//Has motor already been allocated?
    	if (dcMotorAllocated.get(motor)) {
    		System.out.println("*** Error *** Motor already allocated");
			throw new IllegalArgumentException(motor);
    	}
    	
    	//Set flag to allocate motor.
    	dcMotorAllocated.put(motor, true);
    	
    	//Can't allocate a DC motor if corresponding Stepper Motor already allocated  
    	hit = false;
    	if ((motor=="M1"||motor=="M2") && stepperMotorAllocated.get("SM1")) hit = true;
    	if ((motor=="M3"||motor=="M4") && stepperMotorAllocated.get("SM2")) hit = true;
    	if (hit) {
    		System.out.println("*** Error *** Conflict between allocating DC motor and Stepper Motor");
			throw new IllegalArgumentException(motor);    		
    	}
    	
       	//Create an instance for this motor.
    	return new AdafruitDcMotor(AdafruitMotorHat.this, motor);
    }
    
	/**
	 * Create a AdafruitStepperMotor instance for this motor
	 * @param motor valid values "SM1", "SM2"
	 * @return AdafruitStepperMotor instance for the specified motor.
	 */
    public AdafruitStepperMotor getStepperMotor(String motor) {
    	
    	//check motor is valid
    	boolean hit = false;
    	for (String  motorTest: stepperMotorAllocated.keySet()) {
    		if (motor == motorTest) {
    			hit = true;
    			break;
    		}
    	}
    	if (!hit) {
    		System.out.println("*** Error *** Motor name not valid, Valid values:");
    		for (String  motorTest: stepperMotorAllocated.keySet()) System.out.format("\"%s\"\n", motorTest);
    		throw new IllegalArgumentException(motor);
    	}
    	
    	//Has stepper motor already been allocated?
    	if (stepperMotorAllocated.get(motor)) {
    		System.out.println("*** Error *** Motor already allocated");
			throw new IllegalArgumentException(motor);
    	}
    	
    	//Set flag to allocate motor.
    	stepperMotorAllocated.put(motor, true);
    	
    	//Can't allocate a stepper motor if corresponding DC motor already allocated  
    	hit = false;
    	if (motor=="SM1" && (dcMotorAllocated.get("M1")||dcMotorAllocated.get("M2"))) hit = true;
    	if (motor=="SM2" && (dcMotorAllocated.get("M3")||dcMotorAllocated.get("M4"))) hit = true;
    	if (hit) {
    		System.out.println("*** Error *** Conflict between allocating DC motor and Stepper Motor");
			throw new IllegalArgumentException(motor);    		
    	}
    	
       	//Create an instance for this stepper motor.
    	return new AdafruitStepperMotor(AdafruitMotorHat.this, motor);
    }
    
}
