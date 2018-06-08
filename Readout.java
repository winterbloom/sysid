public class Readout {

    private double val;
    private double[] valList;
    private double[] timeList;
    private int loopInd;
    private long start;
    private final double muPS = 1000000;
    private double zero;
    
    public Readout(int history) {
	val = 0;
	loopInd = 0;
	start = System.nanoTime() / 1000;
	valList = new double[history];
	timeList = new double[history];
	for(int i = 0; i < history; i++) {
	    timeList[i] = 0;
	    valList[i] = 0;
	}
    }

    public synchronized void reset() {
	zero = val;
	start = System.nanoTime() / 1000;
	for(int i = 0; i < valList.length; i++) {
	    timeList[i] = 0;
	    valList[i] = 0;
	}
	val = 0;
    }

    public synchronized void setVal(double inVal) {
	inVal = inVal - zero;
	val = inVal;
	loopInd = loopInd == valList.length - 1 ? 0 : loopInd + 1;
	valList[loopInd] = inVal;
	timeList[loopInd] = getSecTime();
    }

    public synchronized double getVal() {
	return val;
    }

    public synchronized double getDeriv(double tStep){
	boolean found = false;
        double t = getSecTime();
	double prevTime = 0;
	double prevVal = 0;
        for(int i = loopInd - 1; i != loopInd && !found; i--) {
	    if(i == -1) i = valList.length - 1;
	    if(timeList[i] < t - tStep) {
		found = true;
		prevTime = timeList[i];
		prevVal = valList[i];
	    }
	}
	if(found) {
	    return (val - prevVal) / (getSecTime() - prevTime);
	} else {
	    int quet = loopInd == valList.length - 1 ? 0 : loopInd + 1;
	    return (val - valList[quet]) / (getSecTime() - timeList[quet]);
	}
    }            

    private double getSecTime() {
	long currTime = (System.nanoTime() / 1000) - start;
	return (double) currTime / muPS;
    }
}
