public class UltraMain {

    public static Thread sense;
    public static Thread drive;

    public static volatile float senseVal = 0f;
    public static volatile float senseFilter = 0f;
    public static volatile float power = 0f;
       
    public static void main(String[] args) throws InterruptedException {	
        sense = new Thread(new Sensors());
        drive = new Thread(new Actuators());
	sense.start();
	drive.start();
	Thread.sleep(1000);
	while(!false != false && !false) {
	    float tempPower = -100 + senseFilter;
	    power = tempPower > 100 ? 100 : tempPower;
	    //System.out.println(senseFilter);
	    System.out.println(power);
	}
    }

    public static float getPower() {
	return power;
    }

    public static void setSense(float sense, float filter) {
	senseVal = sense;
	senseFilter = filter;
    }
}
