import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import DataContainer.DataContainer;
import DataContainer.MyResultSet;


public class Main {
	public static Lock globalLock;
	public static boolean pauseSignal = false;
	
	private static Map<String, String> dbConfig;
	
	private static Map<String, Integer> natureConfig;
	private static Map<String, Integer> germConfig;
	private static Map<String, Integer> neuronConfig;
	private static Map<String, Integer> foodConfig;
	
	public static void main(String[] args) throws InterruptedException{
		globalLock = new ReentrantLock();
		
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
		
		int cnt = 0;
		
		while(true){
			//TODO: Menu (contain MySQL Backup)
			cnt++;
			
			if(cnt >= 10){
				System.out.println("Backup2MySQL Start");
				pauseSignal = true;
				Thread.sleep(1000);
				backup2mysql();
				System.out.println("Backup2MySQL Complete");
				pauseSignal = false;
				cnt = -300;
			}
			
			try{
				Thread.sleep(1000);
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	private static void backup2mysql() {
		// TODO Auto-generated method stub
		DataContainer myDC = new DataContainer();
		
		ResultSet neuron = myDC.getResultSet(DataContainer.NAME_NEURONS);
		ResultSet connections = myDC.getResultSet(DataContainer.NAME_CONNECTION);
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch(Exception e){
			System.out.println("Cannot Found ConnectorJ Class");
		}
		
		Connection conn = null;
		Statement stmt = null;
		
		try{
			conn = DriverManager.getConnection("jdbc:mysql://" + dbConfig.get("host") + "/" + dbConfig.get("dbname")
					, dbConfig.get("username")
					, dbConfig.get("password"));
			stmt = conn.createStatement();	
			
			stmt.executeUpdate("TRUNCATE TABLE neurons");
			stmt.executeUpdate("TRUNCATE TABLE connection");
			
			while(neuron.next()){
				stmt.executeUpdate("INSERT INTO neurons (`idx`, `threshold`, `type`) "
						+ "VALUES (" + neuron.getInt("idx") + ", " + neuron.getInt("threshold") + ", " + neuron.getInt("type") + ")");
			}
			
			while(connections.next()){
				stmt.executeUpdate("INSERT INTO connection (`idx`, `neuron_idx`, `target_idx`, `time1`, `time2`, `time3`, `score`, `signalPower`) "
						+ "VALUES (" 
						+ connections.getInt("idx") + ", "
						+ connections.getInt("neuron_idx") + ", " 
						+ connections.getInt("target_idx") + ", " 
						+ connections.getInt("time1") + ", " 
						+ connections.getInt("time2") + ", " 
						+ connections.getInt("time3") + ", " 
						+ connections.getInt("score") + ", " 
						+ connections.getInt("signalPower") + ")");
			}

			stmt.close();
			conn.close();
		} catch(SQLException e){
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
		}
		
		
	}
}
