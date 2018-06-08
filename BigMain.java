public class BigMain {
    public static void main(String[] args) throws InterruptedException {
	float[] powerVals = {0f, 0f, 0f};
	String[] ports = {"S01", "S02", "S03"};
	String[] dport = {"S05", "S06", "S07"};
	MotorSignal power = new MotorSignal(ports.length);
	Thread motors = new Thread(new BigMotor(ports, dport, power));
	Thread.sleep(100);
	motors.start();
	Thread.sleep(1000);
	for(float i = 0f; i < 40f; i += 0.2) {
	    powerVals[0] = (float) Math.sin(i) * 95f;
	    powerVals[1] = (float) Math.cos(i) * 95f;
	    powerVals[2] = powerVals[0];
	    power.setPower(powerVals);
	    Thread.sleep(200);
	}
    }
}
