package com.pi4j.component.adafruithat;
/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: Java Library (Core)
 * FILENAME      :  StepperMode.java
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
/**
 * There are four stepper modes available for commanding a stepper motor. 
 * The default stepper mode is SINGLE_PHASE.
 * @author eric
 *
 */
public enum StepperMode {
	/**
	 * Single phase is the simplest type of stepping and uses the least power. 
	 * It uses a single coil to hold the motor in place alternating power between
	 * the coils to make the step movement.
	 */
	SINGLE_PHASE,
	/**
	 * Double Phase uses two coils on at once using twice the power but
	 * offering approximately 25% more holding power at the step. 
	 */
	DOUBLE_PHASE,
	/**
	 * Mix of single and double phase alternately using approximately
	 * twice the power but has twice as many steps per rotation.
	 */
	HALF_STEP,
	/**
	 * This mode uses a mix of single and double stepping with PWM to slowly
	 * transition between steps. Its slower than single stepping but has much
	 * higher precision and has 8 times the number of steps per rotation.  
	 */	
	MULTI_STEP

}
