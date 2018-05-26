package com.pi4j.component.adafruithat;
/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: Java Library (Core)
 * FILENAME      :  AdafruitStepperMotor.java
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
import com.pi4j.component.motor.MotorState;
import com.pi4j.component.motor.StepperMotorBase;

/**
 * This java class has been developed to command stepper motors wired to a 
 * "Adafruit DC and Stepper Motor HAT" developed for the Raspberry Pi
 *  <p>
 *  <a href="https://www.adafruit.com/products/2348">See MotorHAT </a>
 *  <p>
 * Up to two stepper motors can be controlled with the motor HAT. These motors
 * are named "SM1" (corresponding to M1 and M2 pins) and "SM2" (M3 and M4 pins). Both
 * DC and stepper motors can be wired to the same motor HAT.
 * <p>
 * Use the AdafruitDCMotor class for commanding DC motors.
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
 * @see com.pi4j.component.adafruithat.AdafruitDcMotor
 */
public class AdafruitStepperMotor extends StepperMotorBase  {
	
	//Adafruit Motor Hat used to command motor functions
	private AdafruitMotorHat motorHat;	
	
	//Motor name "SM1" or "SM2"
	private String motor;
	
	//Stepper Mode starts in SINGLE_PHASE mode as default mode
	private StepperMode stepperMode = StepperMode.SINGLE_PHASE;
	
	//Motor State starts in FORWARD direction as default direction
	private MotorState motorState = MotorState.FORWARD;
	
	//There are 8 micro-steps per full step.
	private final int microSteps = 8;
	
	//Time interval between each step in the step() method. Default starts
	//as 0 interpreted as step motor at the fastest possible speed.
	private long milliSeconds = 0;
	
	//Terminate if the step's time interval can not be achieved.
	private boolean killFlag = false;

	//Number of motor steps incurred. 
	private long currentStep = 0;
	
	//Number of motor steps per 360 degrees rotation. User must set this value
	//in the setStepsPerRevolution() method.
	private int stepsPerRevolution = 0;
	
	private int coilIndex;
	private int powerIndex;
	private int pwmA;
	private int pwmB;
	
	private int[][] singleStepCoils = new int[][] {{1,0,0,0}, {0,1,0,0}, {0,0,1,0}, {0,0,0,1}};
	private int[][] halfStepCoils   = new int[][] {{1,0,0,0}, {1,1,0,0}, {0,1,0,0}, {0,1,1,0}, {0,0,1,0}, {0,0,1,1}, {0,0,0,1}, {1,0,0,1}};
	private int[][] microStepCoils = new int[][] {{1,1,0,0},{0,1,1,0},{0,0,1,1},{1,0,0,1}};
	private int[] microStepCurve = new int[] {0, 800, 1568, 2272, 2880, 3392, 3776, 4000, 4095};
	private int[] coils = new int[4];
	private byte[] ZERO = new byte[] {0,0,0,0};

	/*
	 * PCA9685-chip addresses to control coil A on the stepper motor
	 */
	private int[] A_PWM_ADDR;
	private int[] A_IN2_ADDR;
	private int[] A_IN1_ADDR;
	/*
	 * Corresponding PWM values for coil A
	 */
	private byte[] A_PWM_VALUES;
	private byte[] A_IN2_VALUES;
	private byte[] A_IN1_VALUES;
	/*
	 * PCA9685-chip addresses to control coil B on the stepper motor
	 */
	private int[] B_PWM_ADDR;
	private int[] B_IN2_ADDR;
	private int[] B_IN1_ADDR;
	/*
	 * Corresponding PWM values for coil B
	 */
	private byte[] B_PWM_VALUES;
	private byte[] B_IN2_VALUES;
	private byte[] B_IN1_VALUES;	
	/**
	 * Stepper Motor Constructor
	 * @param motorHat - must be created by caller
	 * @param motor - motor name "SM1" or "SM2"
	 */
	public AdafruitStepperMotor(AdafruitMotorHat motorHat, String motor) {
		this.motorHat = motorHat;
		this.motor = motor;
		this.setup();
	}
	
	/**
	 * Associate the stepper motor name with the corresponding PWM addresses that drive the stepper motor.
	 */
	private void setup() {
		if (motor == "SM1") {
			A_PWM_ADDR = new int[] {motorHat.LED8_ON_L,  motorHat.LED8_ON_H,  motorHat.LED8_OFF_L,  motorHat.LED8_OFF_H};
			A_IN2_ADDR = new int[] {motorHat.LED9_ON_L,  motorHat.LED9_ON_H,  motorHat.LED9_OFF_L,  motorHat.LED9_OFF_H};
			A_IN1_ADDR = new int[] {motorHat.LED10_ON_L, motorHat.LED10_ON_H, motorHat.LED10_OFF_L, motorHat.LED10_OFF_H};
			B_PWM_ADDR = new int[] {motorHat.LED13_ON_L, motorHat.LED13_ON_H, motorHat.LED13_OFF_L, motorHat.LED13_OFF_H};
			B_IN2_ADDR = new int[] {motorHat.LED12_ON_L, motorHat.LED12_ON_H, motorHat.LED12_OFF_L, motorHat.LED12_OFF_H};
			B_IN1_ADDR = new int[] {motorHat.LED11_ON_L, motorHat.LED11_ON_H, motorHat.LED11_OFF_L, motorHat.LED11_OFF_H};			
		}
		else if (motor == "SM2") {
			A_PWM_ADDR = new int[] {motorHat.LED2_ON_L,  motorHat.LED2_ON_H,  motorHat.LED2_OFF_L,  motorHat.LED2_OFF_H};
			A_IN2_ADDR = new int[] {motorHat.LED3_ON_L,  motorHat.LED3_ON_H,  motorHat.LED3_OFF_L,  motorHat.LED3_OFF_H};
			A_IN1_ADDR = new int[] {motorHat.LED4_ON_L,  motorHat.LED4_ON_H,  motorHat.LED4_OFF_L,  motorHat.LED4_OFF_H};
			B_PWM_ADDR = new int[] {motorHat.LED7_ON_L,  motorHat.LED7_ON_H,  motorHat.LED7_OFF_L,  motorHat.LED7_OFF_H};
			B_IN2_ADDR = new int[] {motorHat.LED6_ON_L,  motorHat.LED6_ON_H,  motorHat.LED6_OFF_L,  motorHat.LED6_OFF_H};
			B_IN1_ADDR = new int[] {motorHat.LED5_ON_L,  motorHat.LED5_ON_H,  motorHat.LED5_OFF_L,  motorHat.LED5_OFF_H};				
		}
		else {
			//Invalid motor, get out of here.
			System.out.println("*** Error *** Illegal motor name. Must be \"SM1\" or \"SM2\"");
			motorHat.stopAll();
			throw new IllegalArgumentException(motor);			
		}
	}
	
	/**
	 *  Move the stepper motor one step and specify motor direction
	 *  
	 * @param motorState stepper movement direction 
	 * (MotorState.FORWARD or MotorState.REVERSE)
	 */
	public void oneStep(MotorState motorState) {
		if (motorState != MotorState.FORWARD && motorState != MotorState.REVERSE) {
			//Invalid motorState, get out of here.
			System.out.println("*** Error *** Illegal motorState. Must be MotorSate.FORWARD or MotorState.REVERSE");
			motorHat.stopAll();
			throw new IllegalArgumentException(motorState.name());	
		}
		this.motorState = motorState;
		this.oneStep();
	}
	
	/**
	 * Move the stepper motor one step use previously set motor direction.
	 */
	public void oneStep() {
		if (stepperMode == StepperMode.SINGLE_PHASE) stepSingleStep();
		else if (stepperMode == StepperMode.DOUBLE_PHASE) stepSingleStep();
		else if (stepperMode == StepperMode.HALF_STEP) stepHalfStep();
		else if (stepperMode == StepperMode.MULTI_STEP) stepMultiStep();
		else {
			System.out.println("*** Error *** Invalid stepperMode.");
			motorHat.stopAll();
			throw new IllegalArgumentException(stepperMode.name());	
		}
	}
	
	/**
	 * Move the stepper motor one step in SINGLE_PHASE or DOUBLE_PHASE mode.
	 */
	private void stepSingleStep() {
		if (motorState == MotorState.FORWARD) currentStep += 1;
		else currentStep -= 1;
		
		powerIndex = Math.abs((int) ((currentStep-1) % 2));
		
		/*
		 * For SINGLE_PHASE alternate power on/off for coils A & B. We use
		 * 4095(12-bit) to indicate the PWM is on for the full pulse width.
		 */
		if (stepperMode == StepperMode.SINGLE_PHASE) {
			if (powerIndex == 0) {
				pwmA = 4095;   //100% power for coil A
				pwmB = 0;      //power turned off on coil B			
			}
			else {
				pwmA = 0;		//power turned off on coil A 
				pwmB = 4095;	//100% power for coil B
			}
		}
		/*
		 * For DOUBLE_PHASE stepping the power on both coils is always 100%
		 */
		else {
			pwmA = 4095;
			pwmB = 4095;
		}
		
		//Set the power levels for coils A & B.
		A_PWM_VALUES = this.setStepperPWM(0,pwmA); 
		B_PWM_VALUES = this.setStepperPWM(0,pwmB);
			
		coilIndex =  Math.abs((int) ((currentStep-1) % 4));	
		//Set the direction and movement PWMs for coils A & B
		A_IN2_VALUES = this.setPin(singleStepCoils[coilIndex][0]);
		B_IN1_VALUES = this.setPin(singleStepCoils[coilIndex][1]);
		A_IN1_VALUES = this.setPin(singleStepCoils[coilIndex][2]);
		B_IN2_VALUES = this.setPin(singleStepCoils[coilIndex][3]);
		//Command the PCA9685 chip to step the motor.
		this.sendCommands();
	}
	
	/**
	 * Move the stepper motor one step in HALF_STEP mode.
	 */
	private void stepHalfStep() {
		if (motorState == MotorState.FORWARD) currentStep += 1;
		else currentStep -= 1;
		
		//Set the power levels for coils A & B. 
		//Always maximum power for both coils in half-step
		A_PWM_VALUES = this.setStepperPWM(0,4095); 
		B_PWM_VALUES = this.setStepperPWM(0,4095);
		
		coilIndex =  Math.abs((int) ((currentStep-1) % 8));	
		//Set the direction and movement PWMs for coils A & B
		A_IN2_VALUES = this.setPin(halfStepCoils[coilIndex][0]);
		B_IN1_VALUES = this.setPin(halfStepCoils[coilIndex][1]);
		A_IN1_VALUES = this.setPin(halfStepCoils[coilIndex][2]);
		B_IN2_VALUES = this.setPin(halfStepCoils[coilIndex][3]);
		//Command the PCA9685 chip to step the motor.
		this.sendCommands();
		
	}
	
	/**
	 * Move the stepper motor one step in MULTI_STEP mode.
	 */
	private void stepMultiStep() {
		
		if (motorState == MotorState.FORWARD) currentStep += 1;
		else currentStep -= 1;
		
		powerIndex = Math.abs((int) ((currentStep - 1) % (microSteps*4)));
		
		if (powerIndex >=0 && powerIndex < microSteps) {
			pwmA = microStepCurve[microSteps-powerIndex];
			pwmB = microStepCurve[powerIndex];
			coils = microStepCoils[0];
		}
		else if (powerIndex >= microSteps && powerIndex < microSteps*2) {
			pwmA = microStepCurve[powerIndex - microSteps];
			pwmB = microStepCurve[microSteps*2 - powerIndex];
			coils = microStepCoils[1];
		}
		else if (powerIndex >= microSteps*2 && powerIndex < microSteps*3) {
			pwmA = microStepCurve[microSteps*3 - powerIndex];
			pwmB = microStepCurve[powerIndex-microSteps*2];
			coils = microStepCoils[2];
		}
		else if (powerIndex >= microSteps*3 && powerIndex < microSteps*4) {
			pwmA = microStepCurve[powerIndex - microSteps*3];
			pwmB = microStepCurve[microSteps*4 - powerIndex];
			coils = microStepCoils[3];
		}
		
		//Set the power levels for coils A & B.
		A_PWM_VALUES = this.setStepperPWM(0,pwmA); 
		B_PWM_VALUES = this.setStepperPWM(0,pwmB);
		
		//Set the direction and movement PWMs for coils A & B
		A_IN2_VALUES = this.setPin(coils[0]);
		B_IN1_VALUES = this.setPin(coils[1]);
		A_IN1_VALUES = this.setPin(coils[2]);
		B_IN2_VALUES = this.setPin(coils[3]);
		
		this.sendCommands();

	}

	/**
	 * set the PWM values for the coil specification
	 * @param coil
	 * @return
	 */
	private byte[] setPin(int coil) {
		byte values[] = new byte[4];		
		if (coil == 0) values = setStepperPWM(0, 4095); 
		else if (coil == 1) values = setStepperPWM(4095, 0);
		else {
			System.out.println("*** Error *** coil value must be 0 or 1.");
			motorHat.stopAll();
			throw new IllegalArgumentException(Integer.toString(coil));			
		}		
		return values;		
	}
	
	/**
	 * Create the byte array values for the PWM ON and OFF registers
	 * @param on -  ON  PWM (when to start pulse)
	 * @param off - OFF PWM (when to stop pulse)
	 * @return PWM array of ON and OFF byte values to be written to the PCA9685 device.
	 */
	private byte[] setStepperPWM(int on, int off) {
		byte[] pwm = new byte[4];
		pwm[0] = (byte) (on & 0XFF);	//low-order byte of ON PWM value
		pwm[1] = (byte) (on >> 8);		//high-order byte of ON PWM value
		pwm[2] = (byte) (off & 0XFF);	//low-order byte on OFF PWM value
		pwm[3] = (byte) (off >>8);		//high-order byte of OFF PWM value
		return pwm;
	}

	/**
	 * Command the LED PWMs to set the motor power and direction.
	 * A_PWM - PWM for coil A
	 * A_IN2 - 2nd direction PWM, coil A
	 * A_IN1 - 1st direction PWM, coil A
	 * 
	 * B_PWM - PWM for coil B
	 * B_IN2 - 2nd direction PWM, coil B
	 * B_IN1 - 1st direction PWM, coil B
	 */
 	private void sendCommands() {
 		//Command A coil
		for (int i=0; i<4; i++) motorHat.write(A_PWM_ADDR[i],A_PWM_VALUES[i]);
		for (int i=0; i<4; i++) motorHat.write(A_IN2_ADDR[i],A_IN2_VALUES[i]);
		for (int i=0; i<4; i++) motorHat.write(A_IN1_ADDR[i],A_IN1_VALUES[i]);
		//Command B coil
		for (int i=0; i<4; i++) motorHat.write(B_PWM_ADDR[i],B_PWM_VALUES[i]);
		for (int i=0; i<4; i++) motorHat.write(B_IN2_ADDR[i],B_IN2_VALUES[i]);
		for (int i=0; i<4; i++) motorHat.write(B_IN1_ADDR[i],B_IN1_VALUES[i]);		
	}
	
	 /**
	  * Move the number of steps specified.
	  * Positive values will move stepper motor in forward direction, negative
	  * values move motor in reverse direction.
	  */
	@Override
	public void step(long steps) {
		long mySteps;
		
		if (steps == 0) {
			setState(MotorState.STOP);
			this.stop();
			return;
		}
		else if (steps < 0) {
			setState(MotorState.REVERSE);
			mySteps = -steps;
		}
		else {
			setState(MotorState.FORWARD);
			mySteps = steps;
			
		}
		if (milliSeconds == 0) for (long iStep=0;  iStep<mySteps; iStep++) this.oneStep();
		else {
			for (long iStep=0;  iStep<mySteps; iStep++) {
				long tStart = System.currentTimeMillis();
				this.oneStep();
				long deltaT = System.currentTimeMillis() - tStart;
				//wait the additional time for the desired time interval
				if (deltaT < milliSeconds) {
					long wait = milliSeconds - deltaT;
					motorHat.sleep(wait);
				}
				else if (killFlag) {
					//If the time interval per step can not be achieved 
					//and the killFlag is true then get out of here.
					System.out.println("*** Error *** Time interval per step can not be achieved");
					motorHat.stopAll();
					throw new IllegalArgumentException();	
				}				
			}
		}
	}
	
	/**
	 * Rotate the stepper motor for the number of revolutions specified.
	 * Positive values move in forward direction, negative in reverse direction,
	 * 0 value will stop the stepper motor.
	 */
	public void rotate(double revolutions) {
		if (stepsPerRevolution == 0) {
			System.out.println("*** Error *** stepsPerRevolution was not initialzied by stepsPerRevoution method.");
			motorHat.stopAll();
			throw new IllegalArgumentException();			
		}
		step(Math.round(revolutions * (double) stepsPerRevolution));
	}
	/**
	 * This method is used to specify precise timing interval between between each motor step. 
	 * The time interval applies only to step() and rotate() methods. A millisecond value of 0 indicates 
	 * the stepping is to occur at the fastest possible rate.   
	 *  
	 * @param milliSeconds Time interval in milliseconds between each step. If 0 (default value) then
	 * stepping occurs at the fastest rate possible. 
	 * @param killFlag The killFlag instructs the step() and rotate() method to terminate if the milliSeconds between
	 * each step can not be achieved. true=terminate if time interval can not be achieved, false=keep stepping
	 * even if time interval can not be achieved.
	 */
	public void setStepInterval(long milliSeconds, boolean killFlag) {
		this.killFlag = killFlag;
		this.setStepInterval(milliSeconds);
	}
	
	/**
	 * This method is used to specify precise timing interval between between each motor step. 
	 * The time interval applies only to step() and rotate() methods. A millisecond value of 0 indicates
	 * the stepping is to occur at the fastest possible rate. If you want to specify that the
	 * Use the alternate overload method to specify that step() method is to terminate if the
	 * time interval can not be achieved.   
	 *  
	 * @param milliSeconds Time interval in milliseconds between each step. If 0 (default value) then
	 * stepping occurs at the fastest rate possible. 
	 */
	@Override
	public void setStepInterval(long milliSeconds) {
		if (milliSeconds < 0) {
			//Invalid option, get out of here.
			System.out.println("*** Error ***  milliSeconds specified must be greater than or equal to 0");
			motorHat.stopAll();
			throw new IllegalArgumentException();		
		}
		
		this.milliSeconds = milliSeconds;
	}	
	
	/**
	 * Return the Motor State
	 * 
	 * @return MotorState .FORWARD, .REVERSE, .STOP
	 */
	@Override
 	public MotorState getState() {
		return motorState;
	}

	/**
	 * Set Motor state
	 * @param motorState (MotorState.FORWARD, MotorState.REVERSE, MotorState.STOP)
	 */
	@Override
	public void setState(MotorState motorState) {
		if (motorState != MotorState.FORWARD &&
			motorState != MotorState.REVERSE &&
			motorState != MotorState.STOP) {
			System.out.println("*** Error *** Invalid motorState. Must be MotorSate.FORWARD, MotorState.REVERSE, MotoState.STOP");
			motorHat.stopAll();
			throw new IllegalArgumentException(motorState.name());	
		}
		
		//Stop all actions on this motor
		if (motorState == MotorState.STOP) {
			this.stop();
		}
		this.motorState = motorState;
	}
	
	/**
	 * Set the stepper mode. For information on the types of stepper modes see: 
	 * https://learn.adafruit.com/adafruit-dc-and-stepper-motor-hat-for-raspberry-pi/overview
	 * Please note: the currentStep is reset to zero whenever there is a change in the stepperMode.
	 * 
	 * @param stepperMode 
	 * StepperMode.SINGLE_PHASE - Single phase is the simplest type of stepping and uses the least power. 
	 * It uses a single coil to hold the motor in place alternating between
	 * the coils to make the step movement. 
	 * StepperMode.DOUBLE_PHASE - Double Phase uses two coils on at once using twice the power but
	 * offering approximately 25% more holding power at the step. 
	 * StepperMode.HALF_STEP - Mix of single and double phase alternately. This mode uses approximately
	 * twice the power but has twice as many steps per rotation. 
	 * SteeperMode.MULTISTEP - This mode uses a mix of single and double stepping with PWM to slowly
	 * transition between steps. It is much slower than single step mode but has much
	 * higher precision and has 8 times the number of steps per rotation.  
	 */
	public void setMode(StepperMode stepperMode) {
		if (stepperMode != StepperMode.SINGLE_PHASE && 
			stepperMode != StepperMode.DOUBLE_PHASE &&
			stepperMode != StepperMode.HALF_STEP &&
			stepperMode != StepperMode.MULTI_STEP) {
			//Invalid option, get out of here.
			System.out.println("*** Error *** Invalid StepperMode value");
			motorHat.stopAll();
			throw new IllegalArgumentException();			
		}
		
		this.stepperMode = stepperMode;
		
		//currentStep is reset to zero whenever there is a mode change
		//because of complications different steps per motor 360 deg. rotation.
		currentStep = 0;
	}
	
	/**
	 * Returns the currentStep number (number of steps the motor has incurred starting with 1
	 * as the first step. Note that the currentStep is reset to zero whenever there is a change
	 * in the StepperMode. The currentStep can be negative if the motor is moving in the negative
	 * direction.  
	 * @return currentStep 
	 */
	public long getCurrentStep () {
		return currentStep;
	}

	/**
	 * Stop all power and commanding to the stepper motor
	 */
	@Override
	public void stop() {
		A_PWM_VALUES = ZERO;
		A_IN2_VALUES = ZERO;
		A_IN1_VALUES = ZERO;
		B_PWM_VALUES = ZERO;
		B_IN2_VALUES = ZERO;
		B_IN1_VALUES = ZERO;
		motorState = MotorState.STOP;
		sendCommands();
	}
	
	/**
	 * Specify the steps per revolution for this stepper motor.
	 * 
	 * Many stepper motors have 200 steps per revolution. Please
	 * note this value must take into account the Stepper Mode
	 * employed. For SINGLE_PHASE or DOUBLE_PHASE 
	 * mode use the physical motor steps.  For HALF_STEP this value
	 * will be twice the number of physical motor steps. For MULTI_STEP
	 * this value will be 8 times the number of physical motor steps.
	 */
	@Override
	public void setStepsPerRevolution(int stepsPerRevolution) {
		if (stepsPerRevolution < 1) {
			System.out.println("*** Error *** stepsPerRevolution must be > 0");
			motorHat.stopAll();
			throw new IllegalArgumentException(Integer.toString(stepsPerRevolution));	
		}
		this.stepsPerRevolution = stepsPerRevolution;
	}
	
	/**
	 * Return the number of steps per revolution.
	 */
	@Override
	public float getStepsPerRevolution() {
		return (float) this.stepsPerRevolution;
	}	
	
	/**
	 * Is the MotorState the provided value?
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
	 * Get the String name of the motor
	 * @return MotorName
	 */
	@Override
	public String getName() {
		//Here's our generated device
		return String.format("Adafuit StepperMotor Device: 0X%04X Motor: %s", motorHat.DEVICE_ADDR, motor);
	}
	
	/**
	 * This method not supported for AdaFruitStepperMotor class
	 */
	@Override 
	public void forward() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * This method not supported for AdaFruitStepperMotor class
	 */
	@Override 
	public void reverse() {
		throw new UnsupportedOperationException();
	}	
	
	/**
	 * This method not supported for AdaFruitStepperMotor class
	 */
	@Override 
	public void setStepSequence(byte[] junk) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * This method not supported for AdaFruitStepperMotor class
	 */
	@Override 
	public byte[] getStepSequence() {
		throw new UnsupportedOperationException();
	}	
}
