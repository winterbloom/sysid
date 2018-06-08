import com.pi4j.io.gpio.RaspiPin;
import java.util.*;

public class DataCollector {
    
    public volatile Readout inEnc1 = new Readout(30);
    public volatile Readout inEnc2 = new Readout(30);
    private Thread encoder1 = new Thread(new Encoder(inEnc1, RaspiPin.GPIO_28, RaspiPin.GPIO_29));
    private Thread encoder2 = new Thread(new Encoder(inEnc2, RaspiPin.GPIO_24, RaspiPin.GPIO_25));
    //{resultant state} {old state} {power inputs}
    private ArrayList<Double[][]> stateData = new ArrayList<Double[][]>();
    private Double[] derivs = new Double[3];
    private Double[][] transformation = new Double[3][3];

    public DataCollector() {
	encoder1.start();
	encoder2.start();
	Double[][] initialState = {{0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}};
	stateData.add(initialState);
    }

    public void collectData(Double[] currentPowers) {
	derivs[0] = inEnc1.getDeriv(0.05);
	derivs[1] = inEnc2.getDeriv(0.05);
	derivs[2] = 0.0;
        transformation[0] = derivs;
        transformation[1] = stateData.get(stateData.size() - 1)[0];
	transformation[2] = currentPowers;
	stateData.add(transformation);
    }    
    
}
