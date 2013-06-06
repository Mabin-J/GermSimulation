import java.util.HashMap;
import java.util.Map;


public class Main {
	public static void main(String[] args){
		Map<String, String> dbConfig;
		
		Map<String, Integer> natureConfig;
		Map<String, Integer> germConfig;
		Map<String, Integer> neuronConfig;
		Map<String, Integer> foodConfig;
		
		dbConfig = new HashMap<String, String>(8);
		dbConfig.put("host", "localhost");
		dbConfig.put("dbname", "germ_simulation");
		dbConfig.put("username", "germ");
		dbConfig.put("password", "germ");
		
		natureConfig = new HashMap<String, Integer>(4);
		natureConfig.put("sizeX", 100);
		natureConfig.put("sizeY", 100);
		
		germConfig = new HashMap<String, Integer>(4);
		germConfig.put("checkInterval", 1000);
		germConfig.put("defaultEnergy", 10000);
		
		neuronConfig = new HashMap<String, Integer>(6);
		neuronConfig.put("threadAmount", 4);
		neuronConfig.put("defaultScore", 1000);
		neuronConfig.put("connectionMinAmount", 1);
		neuronConfig.put("connectionMaxAmount", 5);
		neuronConfig.put("signalThreshold", 1000);
		neuronConfig.put("signalDefaultPower", 1000);
		neuronConfig.put("signalDecreasePowerPerTurn", 1);
		neuronConfig.put("neuronAmount", 10000);
		neuronConfig.put("neuronSensorAmount", 24);
		// 00 ~ 07: touch_sensor (each direction);
		// 08 ~ 15: smell_sensor (real model / digital model)
		// 16 ~ 23: Emergency sensor
		neuronConfig.put("neuronActorAmount", 5);
		// 00 ~ 02: go_strate;
		// 03 ~ 03: turn half left;
		// 04 ~ 04: turn half right;
		
		foodConfig = new HashMap<String, Integer>(2);
		foodConfig.put("calories", 10000);
		
		Nature nature = new Nature(natureConfig, foodConfig);
		Germ germ = new Germ(nature, germConfig, neuronConfig, dbConfig);
		nature.addObject(germ);
		nature.setNewFood();
		germ.born();
		
		while(true){
			try{
				Thread.sleep(1000);
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}
}
