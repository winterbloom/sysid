package com.pi4j.component.adafruithat;
/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: Java Library (Core)
 * FILENAME      :  AdafruitServoHat.java
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
 * commanding for servos. 
 * 
 * @author Eric Eliason
 * @see com.pi4j.component.adafruithat.AdafruitHat
 */
public class AdafruitServoHat extends AdafruitHat {

	protected double defaultFrequency = 50; //default frequency for AdafruitServoHat
	
	//Tracks if a servo has already been allocated.
	public final  Map<String,Boolean> servoAllocated = new HashMap<String,Boolean>();
	{
		servoAllocated.put("S01",false); //Servo name designations
		servoAllocated.put("S02",false);
		servoAllocated.put("S03",false);
		servoAllocated.put("S04",false);
		servoAllocated.put("S05",false);
		servoAllocated.put("S06",false);
		servoAllocated.put("S07",false);
		servoAllocated.put("S08",false);
		servoAllocated.put("S09",false);
		servoAllocated.put("S10",false);
		servoAllocated.put("S11",false);
		servoAllocated.put("S12",false);
		servoAllocated.put("S13",false);
		servoAllocated.put("S14",false);
		servoAllocated.put("S15",false);
		servoAllocated.put("S16",false);		
	}
	
	public AdafruitServoHat(int deviceAddr) {
		super(deviceAddr);
		setup();
	}
	
	public AdafruitServoHat(int deviceAddr, int i2cBus) {
		super(deviceAddr, i2cBus);
		setup();
	}
	
	/**
	 * For servos set the operating pulse frequency (duty-cycle) to 50HZ on 
	 * the PCA9685 chip. Applications can  override this initial default by 
	 * calling the setPwmFreq() method. However, most servos will satisfactorily 
	 * operate at 50Hz frequency.
	 */
	private void setup() {		
		this.setPwmFreq(defaultFrequency);	
	}
	
	/**
	 * Create an AdafruitServo instance for this servo. Each of the 16 servo pin sets
	 * is assigned a unique name "S01" through "S16".  The pin sets are labeled on
	 * the Adafruit Servo HAT with numbers 0 through 15. "S01" corresponds to pin set 0,
	 * "S02" to pin set 1, etc.
	 * @param servo valid values "S01" through "S16"
	 * @return AdafruitServo instance for the specified servo
	 */	
	public AdafruitServo getServo(String servo) {
		boolean hit = false;
		for (String  servoTest: servoAllocated.keySet()) {
			if (servo == servoTest) {
			hit = true;
			break;
			}
		}
		if (!hit) {
	 		System.out.println("*** Error *** servo name not valid. Valid values: \"S01\" through \"S16\"");    		
    		throw new IllegalArgumentException(servo);			
		}
		
	 	//Has servo already been allocated?
    	if (servoAllocated.get(servo)) {
    		System.out.println("*** Error *** Servo already allocated");
			throw new IllegalArgumentException(servo);
    	}
		
    	//Set flag that servo is allocated
		servoAllocated.put(servo, true);
		
		//Create the instance for this servo
		return new AdafruitServo(AdafruitServoHat.this, servo);
	}
}
