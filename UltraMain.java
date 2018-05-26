public class UltraMain {

    public static Thread sense;
    public static Thread drive;
    
    public static void main(String[] args) throws InterruptedException {	
        sense = new Thread(new Sensors());
	//drive = new Thread(new Actuators());
	sense.start();
	//drive.start();
    }
}
