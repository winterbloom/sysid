import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class Encoder implements Runnable{

    private final GpioController gpio = GpioFactory.getInstance();
    private Readout output;
    private final GpioPinDigitalInput phaseA;
    private final GpioPinDigitalInput phaseB;

    public Encoder(Readout out, Pin A, Pin B) {
	output = out;
	phaseA = gpio.provisionDigitalInputPin(A);
	phaseB = gpio.provisionDigitalInputPin(B);
    }

    public void run() {

        int state = 0;
	int pos = 0;
	boolean a;
	boolean b;
        int last;
	long lastTime = System.nanoTime();	
	
	while(true) {
	    last = pos;
	    a = phaseA.isHigh();
	    b = phaseB.isHigh();
	    if(state == 0) {
		if(a && !b) {
		    pos++;
		    state = 1;
		} else if(!a && b) {
		    pos--;
		    state = 3;
		}
	    } else if(state == 1) {
		if(a && b) {
		    pos++;
		    state = 2;
		} else if(!a && !b) {
		    pos--;
		    state = 0;
		}
	    } else if(state == 2) {
		if(!a && b) {
		    pos++;
		    state = 3;
		} else if(a && !b) {
		    pos--;
		    state = 1;
		}
	    } else if(state == 3) {
		if(!a && !b) {
		    pos++;
		    state = 0;
		} else if(a && b) {
		    pos--;
		    state = 2;
		}
	    }
	    if(last != pos || System.nanoTime() - lastTime > 100000000) {
	      //System.out.println(pos);
	      output.setVal((double) pos / 1462.0);
	      lastTime = System.nanoTime();
	    }
	}
    }
}
