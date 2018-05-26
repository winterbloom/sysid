package com.pi4j.component.adafruithat;
/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: Java Library (Core)
 * FILENAME      :  AdafruitServo.java
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
import com.pi4j.component.servo.Servo;
import com.pi4j.component.servo.ServoDriver;

/**
 * This java class commands a servo wired to the "Adafruit Servo HAT"
 * developed for the Raspberry Pi. 
 * <p>
 * <a href="https://www.adafruit.com/products/2327">See ServoHAT</a>
 * <p>
 * The AdafruitServo class controls the servo position and movement.
 * A class needs to be instantiated for each servo. Servo names range from 
 * "S01" for pin set 0 (labeled on the HAT) through "S16" for pin set 15.
 * <p>
 * Up to 16 servos can be commanded on a single HAT. Each has its own 
 * independent control but all must have the same PWM operating frequency (50Hz default) 
 * <p>
 * The fully-dedicated PCA9685 PWM driver chip  
 * controls servo position. The chip is commanded through the I2C interface. Only two 
 * pins (SDA and SCL) are required to drive the multiple servos, and since it's I2C you can 
 * also connect any other I2C devices or HAT to the same pins.
 * <p>
 * This means up to 32 Adafruit Servo HATs can be stacked to control up to 512 servos.
 * The default I2C address for a HAT is 0x40 but can be modified on the HAT with
 * solder connections to alter its I2C device address.
 * 
 * @author Eric Eliason
 * 
 */
public class AdafruitServo implements Servo {
	
	//Adafruit Servo Hat used to command servo functions
	private AdafruitServoHat servoHat;	
	//Motor name "S01" through "S16"
	private String servo;
	
	//PCA9685 Register addresses for PWM that control motor speed
	private int[] PWM_ADDR;
	//Corresponding speed values
	private byte[] PWM_VALUES;
	
	//PCA9685 to stop the servo
	private final byte[] PWM_STOP    = new byte[] {0X00, 0X00, 0X00, 0X00};

	private Map<String, int[]> servoAddr;
	
	private float period; //period or duty-cycle in milliseconds  
	
	/*
	 * operating limits of servo are based on pulse width (milliseconds).
	 * These initial values are fairly conservative and should work for
	 * many servos. The operating limits can be updated by the application
	 * with the setOperatingLimits() method. Consult the servo's data sheet
	 * to obtain the manufacture's recommended values.
	 */
	private float minimumPulseWidth = 0.6f;
	private float neutralPulseWidth = 1.5f;
	private float maximumPulseWidth = 2.4f;
	
	/*
	 * The relative minimum and maximum values to command the servo
	 * through the setPosition() method. The minimum value corresponds to
	 * minimumPulseWidth position of the servo. The maximum value corresponds
	 * to the maximum pulse width position.
	 */
	private float minimumX = 0.0f;
	private float maximumX = 1.0f;
	
	//current servo position
	private float servoPosition;
	
	/**
	 * Constructor 
	 * @param servoHat AdafruitMotorHat
	 * @param servo Motor value "S01" through "S16"
	 */
	public AdafruitServo(AdafruitServoHat servoHat, String servo) {
		this.servoHat = servoHat;
		this.servo    = servo;
		setup();
	}
	
	/**
	 * Setup servo commanding, used in the constructor to get things going.
	 */
	private void setup() {
		
		//Map associates the servo with the corresponding LED PWM addresses for commanding that servo.
	    servoAddr = new HashMap<String, int[]>();
	    {
	    	servoAddr.put("S01", new int[] {servoHat.LED0_ON_L,  servoHat.LED0_ON_H,  servoHat.LED0_OFF_L,  servoHat.LED0_OFF_H});
	    	servoAddr.put("S02", new int[] {servoHat.LED1_ON_L,  servoHat.LED1_ON_H,  servoHat.LED1_OFF_L,  servoHat.LED1_OFF_H});
	    	servoAddr.put("S03", new int[] {servoHat.LED2_ON_L,  servoHat.LED2_ON_H,  servoHat.LED2_OFF_L,  servoHat.LED2_OFF_H});
	    	servoAddr.put("S04", new int[] {servoHat.LED3_ON_L,  servoHat.LED3_ON_H,  servoHat.LED3_OFF_L,  servoHat.LED3_OFF_H});
	    	servoAddr.put("S05", new int[] {servoHat.LED4_ON_L,  servoHat.LED4_ON_H,  servoHat.LED4_OFF_L,  servoHat.LED4_OFF_H});
	    	servoAddr.put("S06", new int[] {servoHat.LED5_ON_L,  servoHat.LED5_ON_H,  servoHat.LED5_OFF_L,  servoHat.LED5_OFF_H});
	    	servoAddr.put("S07", new int[] {servoHat.LED6_ON_L,  servoHat.LED6_ON_H,  servoHat.LED6_OFF_L,  servoHat.LED6_OFF_H});
	    	servoAddr.put("S08", new int[] {servoHat.LED7_ON_L,  servoHat.LED7_ON_H,  servoHat.LED7_OFF_L,  servoHat.LED7_OFF_H});
	    	servoAddr.put("S09", new int[] {servoHat.LED8_ON_L,  servoHat.LED8_ON_H,  servoHat.LED8_OFF_L,  servoHat.LED8_OFF_H});
	    	servoAddr.put("S10", new int[] {servoHat.LED9_ON_L,  servoHat.LED9_ON_H,  servoHat.LED9_OFF_L,  servoHat.LED9_OFF_H});
	    	servoAddr.put("S11", new int[] {servoHat.LED10_ON_L, servoHat.LED10_ON_H, servoHat.LED10_OFF_L, servoHat.LED10_OFF_H});
	    	servoAddr.put("S12", new int[] {servoHat.LED11_ON_L, servoHat.LED11_ON_H, servoHat.LED11_OFF_L, servoHat.LED11_OFF_H});
	    	servoAddr.put("S13", new int[] {servoHat.LED12_ON_L, servoHat.LED12_ON_H, servoHat.LED12_OFF_L, servoHat.LED12_OFF_H});
	    	servoAddr.put("S14", new int[] {servoHat.LED13_ON_L, servoHat.LED13_ON_H, servoHat.LED13_OFF_L, servoHat.LED13_OFF_H});
	    	servoAddr.put("S15", new int[] {servoHat.LED14_ON_L, servoHat.LED14_ON_H, servoHat.LED14_OFF_L, servoHat.LED14_OFF_H});
	    	servoAddr.put("S16", new int[] {servoHat.LED15_ON_L, servoHat.LED15_ON_H, servoHat.LED15_OFF_L, servoHat.LED15_OFF_H});
	    };
		
		//Check for valid servo value
		boolean hit = false;
		for (String  servoTest: servoHat.servoAllocated.keySet()) {
			if (servo == servoTest) {
			hit = true;
			break;
			}
		}
		if (!hit) {
	 		System.out.println("*** Error *** servo name not valid. Valid values: \"S01\" through \"S15\"");
	 		servoHat.stopAll();
    		throw new IllegalArgumentException(servo);			
		}
		//get the corresponding LED PWM addresses for this servo.
		PWM_ADDR = servoAddr.get(servo);
		
		//move the servo to the neutral position.
		setPulseWidth(neutralPulseWidth);
	}
	
	/**
	 * Set the pulse width (milliseconds) to drive servo position.
	 * 
	 * @param pulseWidth in milliseconds
	 */
	public void setPulseWidth(float pulseWidth) {
		
		period = (1.0f/(float) servoHat.frequency)*1000.0f; //pulse period in milliseconds/cycle
		
		if (pulseWidth < minimumPulseWidth || pulseWidth > maximumPulseWidth  || pulseWidth > period) {			
			System.out.println("*** Error *** pulseWidth valued invalid");
			System.out.format("Must be in range: %8.2f to %8.2f\n",minimumPulseWidth,maximumPulseWidth);
			System.out.format("and must be less than period: %8.2\n", period);
			servoHat.stopAll();
			throw new IllegalArgumentException(Float.toString(pulseWidth));
		}
		
		//raw servo value for setting the pulseWidth
		int rawServo = (int) (Math.round((pulseWidth/period)*4095.0));
		if (rawServo < 0) rawServo = 0;
		
		//System.out.format("Period: %8.2f Pulse width: %8.4f, raw servo position: %d\n", period, pulseWidth, rawServo);
		PWM_VALUES = new byte[] {(byte) 0, (byte) 0, (byte) (rawServo & 0XFF), (byte) (rawServo >>8)};
		sendCommands();
		
	}
	
	/**
	 * Set the operating pulse width range for the servo. Consult the servo's data sheet
	 * for the manufacturer's recommended pulse width designation.  
	 * @param minimumPulseWidth minimum-position pulse width in milliseconds 
	 * @param neutralPulseWidth neutral-position pulse width in milliseconds
	 * @param maximumPulseWidth maximum-position pulse width in milliseconds
	 */
	public void setOperatingLimits(float minimumPulseWidth, float neutralPulseWidth, float maximumPulseWidth) {
		boolean hit = false;
		if (minimumPulseWidth <= 0.0 || neutralPulseWidth <= 0.0 || maximumPulseWidth <= 0.0) {
			hit = true;
			System.out.println("*** Error *** pulse width values can not be 0.0");
		}
		if (minimumPulseWidth > period || neutralPulseWidth > period || maximumPulseWidth > period) {
			
		}
		if (minimumPulseWidth >= neutralPulseWidth) {
			hit = true;
			System.out.println("*** Error *** minimumPulseWidth > neutralPulseWidth");
		}
		if (minimumPulseWidth >= maximumPulseWidth) {
			hit = true;
			System.out.println("*** Error *** minimumPulseWidth >= maximumPulseWidth");			 
		}
		if (neutralPulseWidth >=  maximumPulseWidth) {
			hit = true;
			System.out.println("*** Error *** neutralPulseWidth >= maximumPulseWidth");
		}
		if (hit) {
			servoHat.stopAll();
			throw new IllegalArgumentException();			
		}
		
		this.minimumPulseWidth = minimumPulseWidth;
		this.neutralPulseWidth = neutralPulseWidth;
		this.maximumPulseWidth = maximumPulseWidth;
	}

	
	/**
	 * Specify the relative minimum and maximum value range of the servo motor.  
	 * The value passed to the setPostion() method must be in this range.
	 * The minimumX value corresponds to the servo position of minimum pulse width and
	 * maximumX to the servo position of the maximum pulse width.
	 * @param minimumX Defaults to 0.0 
	 * @param maximumX Defaults to 1.0
	 */
	public void setPositionRange(float minimumX, float maximumX) {
		if ( minimumX >= maximumX) {
			System.out.println("*** Error *** xMax must be greater than xMin");
			servoHat.stopAll();
			throw new IllegalArgumentException();
		}
		this.minimumX = minimumX;
		this.maximumX = maximumX;
	}
	
	/**
	 * Move servo to relative position.  
	 * @param servoPosition The range of the servoPosition corresponds to the minimum
	 * and maximum values set in the setPositionRange() method. The default range 
	 * is 0.0 to 1.0 if the setPositionRange() method is not called.
	 */
	@Override
	public void setPosition(float servoPosition) {
		if (servoPosition < minimumX || servoPosition > maximumX) {
			System.out.println("*** Error *** servo value must be in range 0.0 to 1.0");
			servoHat.stopAll();
			throw new IllegalArgumentException(Float.toString(servoPosition));
		}
		float slope = (maximumPulseWidth - minimumPulseWidth) / (maximumX-minimumX);
		float b =  maximumPulseWidth - slope*maximumX;
		float pulseWidth = slope * servoPosition + b;
		if (pulseWidth > maximumPulseWidth) pulseWidth = maximumPulseWidth;
		if (pulseWidth < minimumPulseWidth) pulseWidth = minimumPulseWidth;
		setPulseWidth(pulseWidth);
		this.servoPosition = servoPosition;
	}

	/**
	 * Return the current servo position
	 */
	@Override 
	public float getPosition() {	
		return (float) servoPosition;
	}
	
	/**
	 * Return the operating PWM frequency in cycles per second.
	 * The operating frequency can be changed with the 
	 * AdafruitServoHat.setPwmFreq() method.
	 * @return PWM frequency in cycles/second 
	 */
	public float getPwmFreq() {
		return (float) servoHat.frequency;		
	}
	
	/**
	 * Return minimum operating pulse width of servo.
	 * @return minimumPulseWidth in milliseconds
	 */
	public float getMinimumPulseWidth() {
		return minimumPulseWidth;
	}
	/**
	 * Return neutral operating pulse width of servo.
	 * @return neutralPulseWidth in milliseconds
	 */	
	public float getNeutralPulseWidth() {
		return neutralPulseWidth;
	}	
	/**
	 * Return maximum operating pulse width of servo.
	 * @return maximumPulseWidth in milliseconds
	 */	
	public float getMaximumPulseWidth() {
		return maximumPulseWidth;
	}		
	
	/**
	 * Stop servo
	 */
	public void stop() {
		PWM_VALUES = PWM_STOP;
		sendCommands();
	}

	/**
	 *  Send commands to the I2C device.
	 */
	private void sendCommands() {
		for (int i=0; i<4; i++) servoHat.write(PWM_ADDR[i],PWM_VALUES[i]);
	}

	
	/**
	 * Does nothing.
	 */
	@Override
	public void setName(String name) {
		
	}

	/**
	 * return name of servo, name constructed in method()
	 */
	@Override
	public String getName() {
		//Here's our generated device
		return String.format("Adafuit Servo Device: 0X%04X Motor: %s", servoHat.DEVICE_ADDR, servo);
	}

	/*
	 * Not applicable methods to this implementation of the servo class
	 */
	@Override
	public void setTag(Object tag) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getTag() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setProperty(String key, String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasProperty(String key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getProperty(String key, String defaultValue) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getProperty(String key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, String> getProperties() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeProperty(String key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clearProperties() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ServoDriver getServoDriver() {
		throw new UnsupportedOperationException();
	}
	
}
