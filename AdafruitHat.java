package com.pi4j.component.adafruithat; 
/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: Java Library (Core)
 * FILENAME      :  AdafruitHat.java
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
import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;

import com.pi4j.*;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

/**
 * 
 * This supper class developed to interface with both the "Adafruit DC and Stepper Motor HAT"
 * and the "Adafruit Servo HAT" developed for the Raspberry Pi. Look below for technical 
 * details on the Adafruit Motor and Servo HATs. 
 * 
 *<p>
 * <a href="https://www.adafruit.com/products/2348">See MotorHAT </a>
 * 
 *<p>
 * <a href="https://www.adafruit.com/products/2327">See ServoHAT</a>
 * 
 *<p>
 * These two HATs use the same PCA9685 PWM chip for operating servos or motors. 
 * The "AdafuitMotorHat" and "AdafruitServoHat" classes extend this class to handle 
 * the particulars of motor and servo commanding.
 * 
 *<p>
 * The PCA9685 chip was designed to handle LEDs through PWM control. However, the chip
 * is used on the Adafruit HATs to control motors and servos. To remain consistent, the 
 * chip's address naming scheme used here is identical to the scheme found in the PCA9685  
 * Product Data Sheet, Rev 4 - 16 April 2015.
 * 
 * @author Eric Eliason
 * @see com.pi4j.component.adafruithat.AdafruitMotorHat
 * @see com.pi4j.component.adafruithat.AdafruitServoHat
 */
public  class AdafruitHat {
	
	protected final int DEVICE_ADDR; // I2C device address for the Adafruit HAT
	
	/**
	 * Pulse width frequency of PWM. On first power up of a Adafruit HAT the default frequency for
	 * the PCA9685 chip is 200 Hz. 
	 */
	public double frequency = 200.0; 
	
	/*
	 * The PCA9685 chip has 16 LED PWM register sets used to command motor and servo
	 * controllers. 
	 * 
	 * From PCA9685 Product Data Sheet, Rev. 4 - 16 April 2015
	 * This is the register address layout as defined in table 4, section 7.3 
	 * on pages 10-13.
	 */
	/**
	 * Register 1 for setting PWM modes
	 */
	public final int MODE1 			=0X00;
	/**
	 * Register 2 for setting PWM modes
	 */
	public final int MODE2 			=0X01;
	public final int SUBADR1 		=0X02; //I2C-bus subaddress 1
	public final int SUBADR2 		=0X03; //I2C-bus subaddress 2
	public final int SUBADR3 		=0X04; //I2C-bus subaddress 3
	/**
	 * Simultaneously command all PWMs
	 */
	public final int ALLCALLADR		=0X05; //LED all Call I2C-bus address
	/**
	 * 12-bit low-byte for turn on time of LED0 PWM
	 */
	public final int LED0_ON_L		=0X06;
	/**
	 * 12-bit high-byte for turn on time of LED0 PWM
	 */	
	public final int LED0_ON_H		=0X07; //LED0 ON high byte
	/**
	 * 12-bit low-byte for turn off time of LED0 PWM
	 */
	public final int LED0_OFF_L		=0X08; //LED0 OFF low byte
	/**
	 * 12-bit high-byte for turn off time of LED0 PWM
	 */	
	public final int LED0_OFF_H		=0X09; //LED0 OFF high byte
	public final int LED1_ON_L		=0X0A; //etc.....
	public final int LED1_ON_H		=0X0B;
	public final int LED1_OFF_L		=0X0C;
	public final int LED1_OFF_H		=0X0D;		
	public final int LED2_ON_L		=0X0E;
	public final int LED2_ON_H		=0X0F;
	public final int LED2_OFF_L		=0X10;
	public final int LED2_OFF_H		=0X11;
	public final int LED3_ON_L		=0X12;
	public final int LED3_ON_H		=0X13;
	public final int LED3_OFF_L		=0X14;
	public final int LED3_OFF_H		=0X15;
	public final int LED4_ON_L		=0X16;
	public final int LED4_ON_H		=0X17;
	public final int LED4_OFF_L		=0X18;
	public final int LED4_OFF_H		=0X19;
	public final int LED5_ON_L		=0X1A;
	public final int LED5_ON_H		=0X1B;
	public final int LED5_OFF_L		=0X1C;
	public final int LED5_OFF_H		=0X1D;
	public final int LED6_ON_L		=0X1E;
	public final int LED6_ON_H		=0X1F;
	public final int LED6_OFF_L		=0X20;
	public final int LED6_OFF_H		=0X21;	
	public final int LED7_ON_L		=0X22;
	public final int LED7_ON_H		=0X23;
	public final int LED7_OFF_L		=0X24;
	public final int LED7_OFF_H		=0X25;
	public final int LED8_ON_L		=0X26;
	public final int LED8_ON_H		=0X27;
	public final int LED8_OFF_L		=0X28;
	public final int LED8_OFF_H		=0X29;
	public final int LED9_ON_L		=0X2A;
	public final int LED9_ON_H		=0X2B;
	public final int LED9_OFF_L		=0X2C;
	public final int LED9_OFF_H		=0X2D;
	public final int LED10_ON_L		=0X2E;
	public final int LED10_ON_H		=0X2F;
	public final int LED10_OFF_L	=0X30;
	public final int LED10_OFF_H	=0X31;
	public final int LED11_ON_L		=0X32;
	public final int LED11_ON_H		=0X33;
	public final int LED11_OFF_L	=0X34;
	public final int LED11_OFF_H	=0X35;	
	public final int LED12_ON_L		=0X36;
	public final int LED12_ON_H		=0X37;
	public final int LED12_OFF_L	=0X38;
	public final int LED12_OFF_H	=0X39;
	public final int LED13_ON_L		=0X3A;
	public final int LED13_ON_H		=0X3B;
	public final int LED13_OFF_L	=0X3C;
	public final int LED13_OFF_H	=0X3D;
	public final int LED14_ON_L		=0X3E;
	public final int LED14_ON_H		=0X3F;
	public final int LED14_OFF_L	=0X40;
	public final int LED14_OFF_H	=0X41;
	public final int LED15_ON_L		=0X42;
	public final int LED15_ON_H		=0X43;
	public final int LED15_OFF_L	=0X44;
	public final int LED15_OFF_H	=0X45;
	// hex 46-F9 reserved
	/**
	 * 12-bit low-byte for turn on time for all PWMs
	 */
	public final int ALL_LED_ON_L	=0XFA; //Load all LEDn_ON_L
	/**
	 * 12-bit high-byte for turn on time for all PWMs
	 */
	public final int ALL_LED_ON_H	=0XFB; //Load all LEDn_ON_H
	/**
	 * 12-bit low-byte for turn off time for all PWMs
	 */
	public final int ALL_LED_OFF_L	=0XFC; //Load all LEDn_OFF_L
	/**
	 * 12-bit high-byte for turn off time for all PWMs
	 */
	public final int ALL_LED_OFF_H	=0XFD; //Load all LEDn_OFF_H
	/**
	 * Prescale value for setting PWM output frequency
	 */
	public final int PRE_SCALE		=0XFE; //prescale for PWM output frequency
	/**
	 * Define test mode
	 */
	public final int TEST_MODE		=0XFF; //Defines test mode to be entered 	
	
	/* 
	 * Command values for PCA9685 chip 
	 * From PCA9685 Product Data Sheet, Rev. 4 - 16 April 2015
	 * See tables 5 & 6, pages 14-16
	 */

	protected final int COMMAND_SLEEP   = 0X10; //MODE1 command, enable sleep, Oscillator off
	protected final int COMMAND_ALLCALL = 0X01; //MODE1 command, enable LED ALLCALL 
	protected final int COMMAND_OUTDRV  = 0x04; //MODE2 command, 16 LED outputs are configured with totem pole structure
	protected final int COMMAND_RESTART = 0X80; //MODE1 command, enable restart
	//protected final int COMMAND_INVRT   = 0X10; //MODE2 command, output logic  state is inverted
	
	protected I2CBus HatI2C;
	protected I2CDevice hatDevice;
		
	//I2C Bus Address
    private final int DEFAULT_I2C_BUS = I2CBus.BUS_1;
    private int I2C_BUS;
    
    //Register addresses for simultaneously commanding all LED PWMs
    private final int[]  PWM_ALL_ADDR = new int[] {ALL_LED_ON_L,ALL_LED_ON_H,ALL_LED_OFF_L,ALL_LED_OFF_H};
    
    //Values to stop all LED PWMs
    private final byte[] PWM_ALL_STOP = new byte[] {0X00, 0X00, 0X00, 0X00};
   
	
	/**
	 * Pass AdafruitHat device address to constructor.
	 * Note up to 32 unique HATs can be stacked on the Raspberry Pi.
	 * Each requires a unique device address that can be set by solder points
	 * on the HAT.
	 * @param deviceAddr Valid addresses range 0X60 to 0X7F
	 */
	public AdafruitHat(int deviceAddr) {
		checkDeviceAddr(deviceAddr);
		DEVICE_ADDR = deviceAddr;
		I2C_BUS = DEFAULT_I2C_BUS;
		setup();
	}
	
	/**
	 * Pass I2C Bus number and Adafruit Hat device address to constructor.
	 * Note up to 32 unique HATs can be stacked on the Raspberry Pi.
	 * Each requires a unique device address that can be set by solder points
	 * on the hat. 
	 * @param deviceAddr Valid addresses range 0X60 to 0X7F
	 * @param i2cBus Valid bus numbers are I2CBus.BUS_1 or I2CBus.BUS_2
	 */
	public AdafruitHat(int deviceAddr, int i2cBus) {
		checkDeviceAddr(deviceAddr);
		DEVICE_ADDR = deviceAddr;
		checkBus(i2cBus);
		I2C_BUS = i2cBus;
		setup();
	}
	
	/**
	 * Check for a valid Adafruit HAT device address
	 * @param deviceAddr Valid values range 0X40 to 0X7F
	 */
	private void checkDeviceAddr(int deviceAddr) {
		if (deviceAddr < 0X40 || deviceAddr > 0X7F) {
			System.out.println("*** Error *** Illegal AdafruitHat device address must be in rage 0X40 to 0X7F");
			throw new IllegalArgumentException(String.format("0X%02X", deviceAddr));
		}
	}
	
	/**
	 * Check for valid I2C Bus address
	 * @param i2cBus Is this I2C Bus value valid?
	 */
	private void checkBus(int i2cBus) {
		if (i2cBus != I2CBus.BUS_1 && i2cBus != I2CBus.BUS_1) {
			System.out.println("*** Error *** - Illega I2C Bus address must be I2CBus.BUS_1 or I2CBus.BUS_2");
			throw new IllegalArgumentException(Integer.toString(i2cBus));
		}
	}
	
	/**
	 * Setup HAt commanding.
	 */
	private void setup() {	
		try {
			//instantiate I2C and I2C Device interface
			HatI2C = I2CFactory.getInstance(I2C_BUS);		
			hatDevice = HatI2C.getDevice(DEVICE_ADDR);
			
			//Enable the All Call mode to simultaneously command all LED PWMs
			hatDevice.write(MODE1, (byte) COMMAND_ALLCALL);
			
			//16 LED outputs are configured with totem pole structure
			hatDevice.write(MODE2, (byte) COMMAND_OUTDRV);
			//wait for oscillator
			sleep(10); 
			
			//read MODE1 Register to get existing state
			int mode1 = hatDevice.read(MODE1);
			if (mode1 < 0) {
				System.out.println("*** Error *** IC2 read returns negative value.");
				throw new IOException(Integer.toString(mode1));
			}
			//No sleeping allowed
			mode1 = mode1 & ~COMMAND_SLEEP; 
			//Write back the MODE1 register with no sleep
			hatDevice.write(MODE1, (byte) mode1);
			//wait for oscillator
			sleep(10);
		}  catch (IOException e) {
			System.out.println("*** Error *** failed to commnicate with AdafruitHat device");
			stopAll();
			e.printStackTrace();
		} catch (Exception e) {
		    System.out.println("*** Error *** unsupported bus - broked");
		    stopAll();
		    e.printStackTrace();
		}
   	
	}

	/**
	 * Set the PCA9685 pulse frequency
	 * @param frequency valid range 24HZ to 1526HZ
	 */
	public void setPwmFreq(double frequency) {
		final double  oscillatorHz = 25000000; //Oscillator runs at 25Mz
		final double  levels = 4096; //12-bit levels		
		int mode1; //store value of PCA9685 MODE1 register
		
		/*
		 * From PCA9685 Data Sheet, page 25:
		 * The PRE_SCALE register allows the pulse frequency to be set for
		 * all LED PWMs.  The general equation:
		 * prescale = (25MHz)/(4096*frequency) - 1
		 *
		 * where: 
		 * frequency = desired frequency of LED PWMs 
		 * 25MHz = clock rate of the internal oscillator
		 * 4096 = 12-bit control
		 * subtract 1, counting starts at zero.
		 *
		 * Frequency must be in range 24 to 1526HZ (see page 25) 
		 */		
		if (frequency < 24.0 || frequency > 1526.0) {
    		System.out.println("*** Error *** Frequency must be in range 24 HZ to 1526 HZ");
			throw new IllegalArgumentException(String.format("%8.2f", frequency));
		}
		
		this.frequency = frequency;
				
		double preScaleLevel = (oscillatorHz/(levels*this.frequency));		
		int preScale = (int) Math.round(preScaleLevel) - 1;
		//preScale must be between 3 and 255 (see page 25)
		if (preScale < 3 || preScale > 255) {
    		System.out.println("*** Error *** PCA9685 prescale value must be in range 3 - 255");
			throw new IllegalArgumentException(String.format("%d", preScale));
		}
		
		//System.out.format("Set PWM frequency to: %10.2f\n", frequency);
		//System.out.format("preScaleLevel: %10.4f\n", preScaleLevel);
		//System.out.format("Final preScale: %d\n", preScale);
		
		/*
		 * The oscillator needs to be turned off while setting the
		 * prescale value (see page 25) 
		 */
		try {
			mode1 = hatDevice.read(MODE1);
			if (mode1 < 0) {
				System.out.println("*** Error *** IC2 returns negative value.");
				throw new IOException(Integer.toString(mode1));
			}
			
			//PRE_SCALE register can be set only in sleep mode.
			hatDevice.write(MODE1, (byte) ((mode1 & 0X7F) | COMMAND_SLEEP));
			sleep(10); //wait for oscillators
			
			hatDevice.write(PRE_SCALE, (byte) preScale);
			sleep(10); //wait for oscillators
			
			//Restart the PCA9865 with original mode
			hatDevice.write(MODE1, (byte) ((mode1 & 0X7f) | COMMAND_RESTART)); 
			sleep(10); //wait for oscillators
			
		} catch (IOException e) {
			System.out.println("*** Error *** Can not I2C read from hatDevice");
			stopAll();
			e.printStackTrace();
		}
		
	}	
	/**
	 * Write the 8-bit value to the indicated address
	 * @param addr Register address I2C device
	 * @param value Value to write at register address
	 */
	public void write(int addr, byte value) {		
		try {
			//System.out.format("address: 0X%02X value: 0x%02X\n",addr,value);
			hatDevice.write(addr,value);
		} catch (IOException e) {
			System.out.println("*** ERROR *** Can not perform I2C write to AdafruitHat Device");
			e.printStackTrace();
		}
	}
	/**
	 * Sleep and force all motors or servos to stop if interrupted.
	 * @param milliseconds Sleep time
	 */
	public void sleep(long milliseconds) {		
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			System.out.println("*** ERROR *** Interrupted sleep");
			stopAll();
			e.printStackTrace();
		}
	}
	/**
	 * Stop all motors and servos for this Adafruit HAT. 
	 */
	public void stopAll() {
		for (int i=0; i<4; i++) write(PWM_ALL_ADDR[i], PWM_ALL_STOP[i]);
	}
}
