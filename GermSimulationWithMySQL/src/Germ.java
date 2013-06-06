import java.util.Map;


public class Germ extends Objects{
	final static int DIRECTION_UP = 0;
	final static int DIRECTION_UP_RIGHT = 1;
	final static int DIRECTION_RIGHT = 2;
	final static int DIRECTION_DOWN_RIGHT = 3;
	final static int DIRECTION_DOWN = 4;
	final static int DIRECTION_DOWN_LEFT = 5;
	final static int DIRECTION_LEFT = 6;
	final static int DIRECTION_UP_LEFT = 7;
	
	Map<String, Integer> germConfig, neuronConfig;
	Map<String, String> dbConfig;
	
	
	boolean[] sensor_status;
	int sensorAmount;
	
	int signalPower;	
	
	boolean[] actor_status;
	int actorAmount;
	
	int[] direction2sensor;
	
	final int[] direction2diffX = {0, +1, +1, +1, 0, -1, -1, -1};
	final int[] direction2diffY = {-1, -1, 0, +1, +1, +1, 0, -1};
	
	final String[] direction2string = {"Up", "UpRight", "Right", "DownRight", "Down", "DownLeft", "Left", "UpLeft"};
	
//	private int speed;

	private int energy;
	private int energyDefault;
	
	private int direction;
	private int emergencyPower;
	
	GermBrain brain;
	
	public Germ(Nature myNature, 
			Map<String, Integer> germConfig, 
			Map<String, Integer> neuronConfig,
			Map<String, String> dbConfig){
		super(myNature);
		
		eatable = false;
		smell = 0;
		throughable = false;
		thisType = "Germ";
		
		this.germConfig = germConfig;
		this.neuronConfig = neuronConfig;
		this.dbConfig = dbConfig;
		
		sensorAmount = neuronConfig.get("neuronSensorAmount");
		actorAmount = neuronConfig.get("neuronActorAmount");
		signalPower = neuronConfig.get("signalDefaultPower");
		this.energy = germConfig.get("defaultEnergy");
		
//		speed = 0;
		energyDefault = energy = germConfig.get("defaultEnergy");
		direction = 0;
		direction2sensor = new int[8];
		for(int i = 0; i < 8; i++){
			direction2sensor[i] = i;
		}
		
		sensor_status = new boolean[sensorAmount];
		actor_status = new boolean[actorAmount];
		
		brain = new GermBrain(this, dbConfig, neuronConfig);
	}
	
	public void born(){
		
		for(int i = 0; i < sensorAmount; i++){
			sensor_status[i] = true;
			brain.putSignal(Nature.time, i, signalPower);
		}
		
		for(int i = 0; i < actorAmount; i++){
			actor_status[i] = false;
		}
		
		System.out.println(Nature.time.getTime3() + ") Germ: Borned!");
		brain.start();
	}
	
	public void touch(int dictation, boolean status){
		sensor_status[dictation] = status;
	}
	
	public void go(){
		energy--;
		if(myNature.isThrowable(locationX + direction2diffX[direction], locationY + direction2diffY[direction]))
			myNature.moveObject(this, direction2diffX[direction], direction2diffY[direction]);
		
		
		System.out.println(Nature.time.getTime3() + ") Germ: Go (Direction: " + direction2string[direction] 
				+ ", x: " + locationX 
				+ ", y: " + locationY+ ")");
		
		senseTouch();
		snuff();
	}
	
	public void senseTouch(){
		Objects tmpObject;
		
		for(int i = 0; i < 8; i++){
			tmpObject = myNature.getObjects(locationX + direction2diffX[i], locationY + direction2diffY[i]);
			if(tmpObject != null){
				if(tmpObject.isEatable())
					eat(tmpObject);
			}
			
			if(!myNature.isThrowable(locationX + direction2diffX[i], locationY + direction2diffY[i])){
				System.out.println(Nature.time.getTime3() + ") Germ: Touched (Sensed Direction: " + direction2string[direction2sensor[i]] + ")");
				this.touch(direction2sensor[i], true);
				brain.putSignal(Nature.time, direction2sensor[i], signalPower);
			} else {
				this.touch(direction2sensor[i], false);
			}				
		}
	}
	
	public void snuff(){
		
		
		int smellStrength = myNature.getSmell(locationX + direction2diffX[direction], locationY + direction2diffY[direction]);
		if(smellStrength !=0){
			System.out.println(Nature.time.getTime3() + ") Germ: Feel Smell (Strength: " + smellStrength + ")");
		}
		
		for(int i = 8; i < 16; i++){
			sensor_status[i] = false;
		}
		
		int tmpStrength = smellStrength;
		if(tmpStrength / 128 >= 1){
			sensor_status[8] = true;
			brain.putSignal(Nature.time, 8, signalPower);
			tmpStrength %= 128;
		}
		if(tmpStrength / 64 >= 1){
			sensor_status[9] = true;
			brain.putSignal(Nature.time, 9, signalPower);
			tmpStrength %= 64;
		}
		if(tmpStrength / 32 >= 1){
			sensor_status[10] = true;
			brain.putSignal(Nature.time, 10, signalPower);
			tmpStrength %= 32;
		}
		if(tmpStrength / 16 >= 1){
			sensor_status[11] = true;
			brain.putSignal(Nature.time, 11, signalPower);
			tmpStrength %= 16;
		}
		if(tmpStrength / 8 >= 1){
			sensor_status[12] = true;
			brain.putSignal(Nature.time, 12, signalPower);
			tmpStrength %= 8;
		}
		if(tmpStrength / 4 >= 1){
			sensor_status[13] = true;
			brain.putSignal(Nature.time, 13, signalPower);
			tmpStrength %= 4;
		}
		if(tmpStrength / 2 >= 1){
			sensor_status[14] = true;
			brain.putSignal(Nature.time, 14, signalPower);
			tmpStrength %= 2;
		}
		if(tmpStrength >= 1){
			sensor_status[15] = true;
			brain.putSignal(Nature.time, 15, signalPower);
		}
	}

	public void turnLeft(){
		energy--;
		direction--;
		if(direction < 0)
			direction += 8;
		
		System.out.println(Nature.time.getTime3() + ") Germ: Turn Left (Direction: " + direction2string[direction] + ")");
		
		int tmpDirection = direction;
		for(int i = 0; i < 8; i++){
			direction2sensor[i] = (8 - direction + i) % 8;
		}
		
		senseTouch();
		snuff();
	}
	public void turnRight(){
		energy--;
		direction++;
		direction %= 8;

		System.out.println(Nature.time.getTime3() + ") Germ: Turn Right (Direction: " + direction2string[direction] + ")");
		
		int tmpDirection = direction;
		for(int i = 0; i < 8; i++){
			direction2sensor[i] = (8 - direction + i) % 8;
		}
		snuff();
		
		senseTouch();
	}
	
	public void checkBody(){
		energy--;
		System.out.println(Nature.time.getTime3() + ") Germ: CheckBody (Energy: " + energy + ")");
		if(energy < energyDefault / 10){
			emergencyPower = energy / (energyDefault / 10) * 255;			
		}
		senseEmergency();
		
		for(int i = 0; i < sensor_status.length; i++){
			if(sensor_status[i])
				brain.putSignal(Nature.time, i, signalPower);
		}
	}
	
	public void senseEmergency(){
		for(int i = 16; i < 24; i++){
			sensor_status[i] = false;
		}
		
		int tmpStrength = emergencyPower;
		if(tmpStrength / 128 >= 1){
			sensor_status[16] = true;
			brain.putSignal(Nature.time, 16, signalPower);
			tmpStrength %= 128;
		}
		if(tmpStrength / 64 >= 1){
			sensor_status[17] = true;
			brain.putSignal(Nature.time, 17, signalPower);
			tmpStrength %= 64;
		}
		if(tmpStrength / 32 >= 1){
			sensor_status[18] = true;
			brain.putSignal(Nature.time, 18, signalPower);
			tmpStrength %= 32;
		}
		if(tmpStrength / 16 >= 1){
			sensor_status[19] = true;
			brain.putSignal(Nature.time, 19, signalPower);
			tmpStrength %= 16;
		}
		if(tmpStrength / 8 >= 1){
			sensor_status[20] = true;
			brain.putSignal(Nature.time, 20, signalPower);
			tmpStrength %= 8;
		}
		if(tmpStrength / 4 >= 1){
			sensor_status[21] = true;
			brain.putSignal(Nature.time, 21, signalPower);
			tmpStrength %= 4;
		}
		if(tmpStrength / 2 >= 1){
			sensor_status[22] = true;
			brain.putSignal(Nature.time, 22, signalPower);
			tmpStrength %= 2;
		}
		if(tmpStrength >= 1){
			sensor_status[23] = true;
			brain.putSignal(Nature.time, 23, signalPower);
		}
	}
	
	public void eat(Objects obj){
		System.out.println(Nature.time.getTime3() + ") Germ: EatFood");
		Food food = (Food) obj;
		energy = energy + food.getCalories();
		myNature.resetFood(obj);
		
		snuff();
	}
}
