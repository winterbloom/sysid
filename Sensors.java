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
  public void run() {
    System.out.println("Starting sonar...");
    final GpioController gpio = GpioFactory.getInstance();
    final GpioPinDigitalOutput trig = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, "trig", PinState.LOW);
    final GpioPinDigitalInput echo = gpio.provisionDigitalInputPin(RaspiPin.GPIO_05);
    for(int i = 0; i < 250; i++) {
      wait(100);
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
      System.out.println((end - start) / 29154.5);
    }
  }
  private void wait(int millis) {
    try {
      Thread.sleep(millis);
    } catch(InterruptedException ie) {}
  }
}
