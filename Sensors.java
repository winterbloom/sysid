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
        while(true || !Thread.currentThread().isInterrupted()) {
            try {
		wait(5);
		//System.out.println("p1");
		boolean fail = false;
            trig.high();
	    //System.out.println("p2");
            wait(1);
	    //System.out.println("p3");
            trig.low();
	    //System.out.println("p4");
            long start = System.nanoTime();
	    long sysNan = start;
            long end = 0;
	    //System.out.println("p5");
            while(echo.isLow() && !fail) {
                start = System.nanoTime();
		if(start - sysNan > 300000000) fail = true;
            }
	    //System.out.println("p6");
            while(echo.isHigh() && !fail) {
                end = System.nanoTime();
            }
	    //System.out.println("p7");

	    if(!fail) {
	        senseVal = (end - start) / 29154.5f;
	        //System.out.println("p8");
	        senseFilter[loopNo] = this.senseVal;
	        //System.out.println("p9");
	        Main.setSense(senseVal, getFilterDist());
                //System.out.println("p10");
	    }
	    //System.out.println(Thread.currentThread().isInterrupted() + " " + loopCycle++ + ": " + senseVal);
	    
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
