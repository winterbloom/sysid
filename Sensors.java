import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.gpio.GpioPinDigitalOutput;

public class Sensors implements Runnable {

    private volatile float senseVal = 0;
    private volatile float[] senseFilter = new float[6];
    private int loopNo = 0;
    
    public void run() {
        System.out.println("Starting sonar...");
        final GpioController gpio = GpioFactory.getInstance();
        final GpioPinDigitalOutput trig = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, "trig", PinState.LOW);
        final GpioPinDigitalInput echo = gpio.provisionDigitalInputPin(RaspiPin.GPIO_05);
	int loopCycle = 0;
        while(!Thread.currentThread().isInterrupted()) {
            try {
		wait(50);
            trig.high();
            wait(1);
            trig.low();
            long start = System.nanoTime();
            long end = 0;
            while(echo.isLow()) {
                start = System.nanoTime();
            }
            while(echo.isHigh()) {
                end = System.nanoTime();
            }

	    senseVal = (end - start) / 29154.5f;
	    senseFilter[loopNo] = this.senseVal;
	    UltraMain.setSense(senseVal, getFilterDist());

	    System.out.println(Thread.currentThread().isInterrupted() + " " + loopCycle++ + ": " + senseVal);
	    
	    loopNo = ++loopNo % senseFilter.length;
	    } catch (Exception e) {
		e.printStackTrace();
	    }
        }
    }
    private void wait(int millis) {
        try {
            Thread.sleep(millis);
        } catch(InterruptedException ie) {}
    }
    /*public float getDist() {
        return senseVal;
	}*/
    private float getFilterDist() {
	float sum = 0;
	for(float x : senseFilter) {
	    sum += x;
	}
	return sum / senseFilter.length;
    }
}
