public class MotorSignal {

    private float[] motorPower;

    public MotorSignal(int n) {
	motorPower = new float[n];
	for(int i = 0; i < n; i++) {
	    motorPower[i] = 0;
	}
    }

    public synchronized void setPower(float[] power) {
	for(int i = 0; i < power.length && i < motorPower.length; i++) {
	    float temp = power[i];
	    temp = temp > 99.9f ? 99.9f : (temp < -99.9f ? -99.9f : temp);
	    motorPower[i] = temp;
	}
    }

    public synchronized float[] getPower() {
	return motorPower;
    }

}
	    
