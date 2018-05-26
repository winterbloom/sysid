package com.pi4j.component.adafruithat; 
/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: Java Library (Core)
 * FILENAME      :  AdafruitDcMotor.java
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
import java.util.Map;

import com.pi4j.component.motor.Motor;
import com.pi4j.component.motor.MotorState;

/**
 * This java class commands DC motors wired to the "Adafruit DC and Stepper Motor HAT"
 * developed for the Raspberry Pi. 
 * <p>
 * <a href="https://www.adafruit.com/products/2348">See MotorHAT</a>
 * <p>
 * Up to 4 DC motors can be commanded with the Motor HAT named "M1" through "M4".
 * <p>
 * Use the AdafruitStepperMotor class for commanding stepper motors.
 * <p>
 * An Adafruit Motor HAT can drive drive up to 4 DC or 2 Stepper motors with 
 * full PWM speed and direction control. The fully-dedicated PCA9685 PWM driver chip  
 * controls motor direction and speed. This chip handles all the motor and 
 * speed controls over I2C. Only two pins (SDA and SCL) are required to drive the 
 * multiple motors, and since it's I2C you can also connect any other I2C devices or 
 * other HATs to the same pins.
 * <p>
 * This means up to 32 Adafruit Motor HATs can be stacked to control up to 128 DC motors or
 * 64 stepper motors. The default I2C address for a HAT is 0x60 but can be modified with
 * solder connections to alter the I2C Address. Motors are controlled with the TB6612 MOSFET driver 
 * with 1.2A per channel current capability (20ms long bursts of 3A peak).
 * 
 * @author Eric Eliason
 * @see com.pi4j.component.adafruithat.AdafruitStepperMotor
 */
public class AdafruitDcMotor implements Motor {
	//Adafruit Motor Hat used to command motor functions
	private AdafruitMotorHat motorHat;	
	//Motor name "M1" through "M4"
	private String motor;
	
	//PCA9685 Register addresses for PWM that control motor speed
	private int[] PWM_ADDR;
	//Corresponding speed values
	private byte[] PWM_VALUES;
	
	//PCA9685 Register addresses for 1st PWM controlling motor direction
	private int[] IN1_ADDR;
	//Corresponding motor direction values
	private byte[] IN1_VALUES;
	
	//PCA9685 Register addresses for 2nd PWM controlling motor direction
	private int[] IN2_ADDR;
	//Corresponding motor direction values
	private byte[] IN2_VALUES;
	
	//Relative maximum power value to command motor. This is a somewhat arbitrary value
	//that can be set by the setPowerRange() method. Some applications may want to use
	//a different range other than 0.0 (no power) to 1.0 (full throttle).
	private float maximumPower = 1.0f;
	
	//Speed setting for motor (-maximumPower to maximumPower, - for reverse, + for forward)
	private float speed = 0.0f;
	//Power setting (0.0 to maximumPower) used in conjunction with forward() and reverse() methods.
	private float power = 0.0f;
	
	//Current motor state: stop, forward, reverse
	private MotorState motorState;
	
	/*
	 * The brakeMode is used in the stop method to quickly stop motor movement.
	 * If true, the motor is temporary reversed for a set number of
	 * milliseconds. This mode eliminates the coasting time for a motor.
	 * If false then the motor coasts to a stop. 
	 */
	private boolean brakeMode = false;
	
	/*
	 * The brakeModeValue specifies the number of milliseconds to reverse
	 * the motor direction to minimize motor coasting. This value can
	 * be set by the user with the method setBreakModeValue(milliseconds)
	 * This value is dependent on the properties of the motor in use
	 * and ideally should be set based on the motor used.
	 */
	private long brakeModeValue = 35;
	
	//PWM values for setting a motor for power, stop, forward, and reverse directions
	private final byte[] PWM_STOP    = new byte[] {0X00, 0X00, 0X00, 0X00};
	private final byte[] IN_FORWARD  = new byte[] {0X00, 0X10, 0X00, 0X00};	
	private final byte[] IN_REVERSE  = new byte[] {0X00, 0X00, 0X00, 0X10};	
		
	/**
	 * Constructor 
	 * @param motorHat AdafruitMotorHat
	 * @param motor Motor value "M1", "M2", "M3", or "M4"
	 */
	public AdafruitDcMotor(AdafruitMotorHat motorHat, String motor) {
		this.motorHat = motorHat;
		this.motor  = motor;		
		this.setup();
	}

	/**
	 * For the given motor (M1-M4) set up the LED PWM register addresses for that motor.
	 */
	private void setup() {
		/*
		 * Each of the four DC motor controllers have different PCA9685 LED PWM addresses to control power 
		 * and direction. Information on the PWM wiring can be found on the Adafruit motor 
		 * hat schematics here: 
		 * https://learn.adafruit.com/adafruit-dc-and-stepper-motor-hat-for-raspberry-pi/downloads
		 */
		if (motor == "M1") {
			PWM_ADDR = new int[] {motorHat.LED8_ON_L,  motorHat.LED8_ON_H,  motorHat.LED8_OFF_L,  motorHat.LED8_OFF_H};
			IN2_ADDR = new int[] {motorHat.LED9_ON_L,  motorHat.LED9_ON_H,  motorHat.LED9_OFF_L,  motorHat.LED9_OFF_H};
			IN1_ADDR = new int[] {motorHat.LED10_ON_L, motorHat.LED10_ON_H, motorHat.LED10_OFF_L, motorHat.LED10_OFF_H};						
		}
		else if (motor == "M2") {
			PWM_ADDR = new int[] {motorHat.LED13_ON_L, motorHat.LED13_ON_H, motorHat.LED13_OFF_L, motorHat.LED13_OFF_H};
			IN2_ADDR = new int[] {motorHat.LED12_ON_L, motorHat.LED12_ON_H, motorHat.LED12_OFF_L, motorHat.LED12_OFF_H};
			IN1_ADDR = new int[] {motorHat.LED11_ON_L, motorHat.LED11_ON_H, motorHat.LED11_OFF_L, motorHat.LED11_OFF_H};								
		}
		else if (motor == "M3") {
			PWM_ADDR = new int[] {motorHat.LED2_ON_L,  motorHat.LED2_ON_H,  motorHat.LED2_OFF_L,  motorHat.LED2_OFF_H};
			IN2_ADDR = new int[] {motorHat.LED3_ON_L,  motorHat.LED3_ON_H,  motorHat.LED3_OFF_L,  motorHat.LED3_OFF_H};
			IN1_ADDR = new int[] {motorHat.LED4_ON_L,  motorHat.LED4_ON_H,  motorHat.LED4_OFF_L,  motorHat.LED4_OFF_H};								
		} 
		else if (motor == "M4") {
			PWM_ADDR = new int[] {motorHat.LED7_ON_L,  motorHat.LED7_ON_H,  motorHat.LED7_OFF_L,  motorHat.LED7_OFF_H};
			IN2_ADDR = new int[] {motorHat.LED6_ON_L,  motorHat.LED6_ON_H,  motorHat.LED6_OFF_L,  motorHat.LED6_OFF_H};
			IN1_ADDR = new int[] {motorHat.LED5_ON_L,  motorHat.LED5_ON_H,  motorHat.LED5_OFF_L,  motorHat.LED5_OFF_H};								
		}
		else {
			//Invalid motor, get out of here.
			System.out.println("*** Error *** Illegal motor Name. Must be \"M1\",\"M2\",\"M3\", or \"M4\"");
			motorHat.stopAll();
			throw new IllegalArgumentException(motor);			
		}
		
		//Clear the LED PWM registers and stop motor.
		PWM_VALUES = PWM_STOP;
		this.stop();
		motorState = MotorState.STOP;		
	}
	
	/**
	 * Command the LED PWMs to set the motor speed and motor direction.
	 * PWM_VALUES = speed control
	 * IN1_VALUES = 1st PWM for controlling direction
	 * IN2_VALUES = 2nd PWM for controlling direction
	 */
	private void sendCommands() {
		for (int i=0; i<4; i++) motorHat.write(PWM_ADDR[i],PWM_VALUES[i]);
		for (int i=0; i<4; i++) motorHat.write(IN2_ADDR[i],IN2_VALUES[i]);
		for (int i=0; i<4; i++) motorHat.write(IN1_ADDR[i],IN1_VALUES[i]);		
	}
	
	/**
	 * Command the motor speed.
	 * Positive speed moves in the forward direction.
	 * Negative speed moves in the reverse direction.
	 * @param speed -maximumPower to maximumPower
	 * 
	 */
	public void speed(float speed) {
		if (speed < -maximumPower || speed > maximumPower) {
			System.out.format("*** Error *** Speed value must be in range %8.1f to %8.1f\n",-maximumPower,maximumPower);
			motorHat.stopAll();
			throw new IllegalArgumentException(Float.toString(speed));
		}
		this.speed = speed;
		this.power = Math.abs(this.speed);
		PWM_VALUES = this.setPwm(this.speed);
		
		//sets up the commanding values for the LED PWMs 
		if (this.speed == 0.0) {
			//turn off PWMs
			this.stop();
			motorState = MotorState.STOP;
		}
		else if (this.speed > 0.0) {
			//set PWMs for forward direction
			IN2_VALUES = IN_FORWARD;
			IN1_VALUES = IN_REVERSE;
			motorState = MotorState.FORWARD;
		}
		else {
			//set PWMs for reverse direction
			IN2_VALUES = IN_REVERSE;	
			IN1_VALUES = IN_FORWARD;
			motorState = MotorState.REVERSE;			
		}
		//Command the PCA9685 for setting speed and direction of the DC motor
		this.sendCommands();		
	}
	
	
	/**
	 * Return current speed value for the motor
	 * @return speed
	 */
	public float getSpeed() {
		return speed;
	}

	/**
	 * Set the power value (speed) for the DC Motor. This method is used in combination
	 * with the forward() and reverse() methods. The power value will be used to the
	 * motor controller on the next forward() or reverse() command.
	 * @param power Valid value range: 0.0 (no power) to maximumPower (full throttle)
	 */
	public void setPower(float power) {
		if (power < 0.0 || power > maximumPower) {
			System.out.format("*** Error *** Power value must be in range 0.0 to %8.1f\n",maximumPower);
			motorHat.stopAll();
			throw new IllegalArgumentException(Float.toString(power));
		}
		this.power = power;
		if (motorState == MotorState.REVERSE) this.speed = -power;
		else this.speed = power;
		
		PWM_VALUES = this.setPwm(power);
	}
	
	/**
	 * Return current power value for the motor
	 * @return Valid range: 0.0 (no power) to maximumPower (full throttle)
	 */
	public float getPower() {
		return power;
	}

	/**
	 * Optionally set the maximum power value that corresponds the
	 * motor's full throttle.
	 * @param maximumPower full throttle value for DC motor. The default
	 * is 1.0 unless set by the calling application.
	 */
	public void setPowerRange(float maximumPower) {
		if (maximumPower <= 0.0) {
			System.out.println("*** Error *** Power range value must be > 0.0");
			motorHat.stopAll();
			throw new IllegalArgumentException(Float.toString(maximumPower));
		}
		this.maximumPower = maximumPower;		
	}
	
	/**
	 * Return the maximum power value that corresponds to the maximum
	 * throttle of the DC motor.
	 * @return maximumPower
	 */
	public float getPowerRange() {
		return maximumPower;
	}
	
	/**
	 * Convert the motor speed to the PWM values
	 * @param speed ranges from -maximumPower to maximumPower, positive numbers are forward direction, negative reverse
	 */
	protected byte[] setPwm(float speed) {
		byte[] pwm = new byte[] {0,0,0,0};
		int rawSpeed = (int) Math.round(Math.abs(speed/maximumPower)*4095); //PWM commanding is 12-bit (4095)
		pwm[2] = (byte) (rawSpeed & 0XFF);  //Extract low-order byte
		pwm[3] = (byte) (rawSpeed >> 8);    //Extract high-order byte
		return pwm;
	}
	
	/**
	 * Set break mode for stop() method;
	 * true  = brake the motor when stopping (quickly stop);
	 * false = let the motor coast to a stop 
	 * @param brakeMode true=brake, false= coast to a stop
	 */
	public void setBrakeMode(boolean brakeMode) {
		this.brakeMode = brakeMode;		
	}
	/**
	 * Set Brake mode value.
	 * Number of milliseconds to apply power in opposite
	 * motor direction to brake (quickly stop) motor.
	 * 
	 * Dependent on motor characteristics. Set this 
	 * value when precise braking is required.
	 * 
	 * @param brakeModeValue milliseconds
	 */
	public void setBrakeModeValue(long brakeModeValue) {
		if (brakeModeValue < 1 || brakeModeValue > 100) {
			System.out.println("*** Error *** brakeModeValue must be in range 1 - 100 milliseconds");
			motorHat.stopAll();
			throw new IllegalArgumentException(Long.toString(brakeModeValue));
		}
		this.brakeModeValue = brakeModeValue;
	}
	
	/**
	 * Command the DC motor to go in the forward direction.
	 */
	@Override
	public void forward() {
		IN2_VALUES = IN_FORWARD;
		IN1_VALUES = IN_REVERSE;
		//Command the PCA9685 for forward direction
		this.sendCommands();
		motorState = MotorState.FORWARD;
		this.speed = this.power;
	}

	/**
	 * Command the DC motor to go in the forward direction for the time
	 * specified then stop.
	 */
	@Override
	public void forward(long milliseconds) {
		IN2_VALUES = IN_FORWARD;
		IN1_VALUES = IN_REVERSE;
		//Command the PCA9685 for forward direction
		this.sendCommands();
		motorState = MotorState.FORWARD;
		motorHat.sleep(milliseconds);
		this.stop();
		motorState = MotorState.STOP;
		this.speed = this.power;
	}

	/** 
	 * Command the DC motor to go in the reverse direction.
	 */
	@Override
	public void reverse() {
		IN2_VALUES = IN_REVERSE;
		IN1_VALUES = IN_FORWARD;
		//Command the PCA9685 for reverse direction
		this.sendCommands();
		motorState = MotorState.REVERSE;
		this.speed = -this.power;
	}

	/**
	 * Command the DC motor to go in the reverse direction for the time
	 * specified then stop.
	 */
	@Override
	public void reverse(long milliseconds) {
		IN2_VALUES = IN_REVERSE;
		IN1_VALUES = IN_FORWARD;
		//Command the PCA9685 for reverse direction
		this.sendCommands();
		motorState = MotorState.REVERSE;
		motorHat.sleep(milliseconds);		
		this.stop();
		motorState = MotorState.STOP;
		this.speed = -this.power;
	}

	/**
	 * Stop the motor.
	 */
	@Override
	public void stop() {
		//if brakeMode then temporarily switch direction to quickly stop motor.
		if (brakeMode) {
			byte[] inSwitch = IN2_VALUES;
			IN2_VALUES = IN1_VALUES;
			IN1_VALUES = inSwitch;
			this.sendCommands();
			motorHat.sleep(brakeModeValue);			
		}
		IN2_VALUES = PWM_STOP;
		IN1_VALUES = PWM_STOP;
		this.sendCommands();
		motorState = MotorState.STOP;
	}

	/**
	 * Returns motor name
	 */
	@Override
	public String getName() {
		//Here's our generated device
		return String.format("Adafuit DcMotor Device: 0X%04X Motor: %s", motorHat.DEVICE_ADDR, motor);
	}
	
	/**
	 * Is the motor state the value passed to this method?
	 * returns true or false
	 */
	@Override
	public boolean isState(MotorState state) {
		return (motorState == state);
	}

	/**
	 * Is the motor stopped?
	 * returns true of false
	 */
	@Override
	public boolean isStopped() {
		return (motorState == MotorState.STOP);
	}
	
	/**
	 * Return the motor state. Possible values returned:
	 * MotorState.STOP
	 * MotorState.FORWARD
	 * MotorState.BACKWARD
	 */
	@Override
	public MotorState getState() {
		//We're tracking the motor state with this variable
		return motorState;
	}
	

	/****************************************************
	 * Methods below are place holders 
	 ***************************************************/
	
	/**
	 * Place holder, does nothing
	 */
	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException();	
	}
	
	/**
	 * Place holder, does nothing
	 */	
	@Override
	public void setTag(Object tag) {
		throw new UnsupportedOperationException();
	}


	/**
	 * Place holder, does nothing
	 */
	@Override
	public Object getTag() {
		throw new UnsupportedOperationException();
	}


	/**
	 * Place holder, does nothing
	 */
	@Override
	public void setProperty(String key, String value) {
		throw new UnsupportedOperationException();
	}


	/**
	 * Place holder, does nothing
	 */
	@Override
	public boolean hasProperty(String key) {
		throw new UnsupportedOperationException();
	}


	/**
	 * Place holder, does nothing
	 */
	@Override
	public String getProperty(String key, String defaultValue) {
		throw new UnsupportedOperationException();
	}


	/**
	 * Place holder, does nothing
	 */
	@Override
	public String getProperty(String key) {
		throw new UnsupportedOperationException();
	}


	/**
	 * Place holder, does nothing
	 */
	@Override
	public Map<String, String> getProperties() {
		throw new UnsupportedOperationException();
	}


	/**
	 * Place holder, does nothing
	 */
	@Override
	public void removeProperty(String key) {
		throw new UnsupportedOperationException();	
	}


	/**
	 * Place holder, does nothing
	 */
	@Override
	public void clearProperties() {
		throw new UnsupportedOperationException();	
	}
	

	/**
	 * This method disabled
	 */
	@Override
	public void setState(MotorState state) {
		throw new UnsupportedOperationException();
	}
}
