import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.crypto.Data;

import DataContainer.DataContainer;
import DataContainer.MyResultSet;


public class GermBrain{
	Germ master;
	Map<String, String> dbConfig;
	Map<String, Integer> neuronConfig;
/*
	Connection conn;
	Statement stmt = null;
	Statement stmt2 = null;
*/
	DataContainer myDb;
	
	private List<GermBrainNeuronJob> jobQueue;
	Map<Integer, Integer> actorNeuronNums;
	Random randNum;
	
	GermBrainNeuron[] neuronsThread;
	Lock threadLock;
	
	public GermBrain(Germ master, Map<String, String> dbConfig, Map<String, Integer> neuronConfig){
		this.master = master;
		
		this.dbConfig = dbConfig;
		this.neuronConfig = neuronConfig;
		
		myDb = new DataContainer();
		
		neuronsThread = new GermBrainNeuron[neuronConfig.get("threadAmount")];
		
		randNum = new Random();
		
		actorNeuronNums = new HashMap<Integer, Integer>();
		
/*
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch(Exception e){
			System.out.println("GermBrain: Cannot Found ConnectorJ Class");
		}
		
		conn = null;

		try{
			conn = DriverManager.getConnection("jdbc:mysql://" + dbConfig.get("host") + "/" + dbConfig.get("dbname")
					, dbConfig.get("username")
					, dbConfig.get("password"));
			stmt = conn.createStatement();	
			stmt2 = conn.createStatement();
			rs = stmt.executeQuery("SELECT * FROM connection;");
			
		} catch(SQLException e){
			if(e.getSQLState().equals("28000")){
				System.out.println("GermBrain: DB Access denied");
			} else if(e.getSQLState().equals("42S02")){
				System.out.println("GermBrain: Didn't Exist Table, Initializing Start\n");
				createTables();
				initNeuron();
			}else {
//				e.printStackTrace(); TODO: Remove This Line
				System.out.println("SQLException: " + e.getMessage());
				System.out.println("SQLState: " + e.getSQLState());
				System.out.println("VendorError: " + e.getErrorCode());
			}
			
			return;
		}
		
*/

		threadLock = new ReentrantLock();
		jobQueue = new ArrayList<GermBrainNeuronJob>(100);
		
// for Initializing Test
		clearNeuron();
		initNeuron();
	}
	
	public void start(){
		for(int i = 0; i < neuronsThread.length; i++){
			neuronsThread[i] = new GermBrainNeuron(this, Nature.time, jobQueue, dbConfig, neuronConfig, threadLock);
			neuronsThread[i].start();
//			System.out.println("Brain: Thread" + i + " is started");
		}
	}
	
	private void initNeuron() {
		System.out.println("GermBrain: Initializing Neurons...");
		int neuronAmount = neuronConfig.get("neuronAmount");
		int neuronSensorAmount = neuronConfig.get("neuronSensorAmount");
		int neuronActorAmount = neuronConfig.get("neuronActorAmount");
		int connectionMinAmount = neuronConfig.get("connectionMinAmount");
		int connectionMaxAmount = neuronConfig.get("connectionMaxAmount");
		int defaultScore = neuronConfig.get("defaultScore");
		
		System.out.println("GermBrain:   Create Neurons...");
		int tenPercentsAmountOfNeuron = neuronAmount / 10;

//		try{
			for(int neuronNumber = 0; neuronNumber < neuronSensorAmount; neuronNumber++){
				Map<String, Object> item = new HashMap<String, Object>();
				item.put("idx", neuronNumber);
				item.put("threshold", neuronConfig.get("signalThreshold"));
				item.put("type", 1);
				myDb.putData(item, DataContainer.NAME_NEURONS);
/*	TODO: Remove
				stmt.executeUpdate("INSERT INTO neurons (`idx`, `threshold`, `type`) "
						+ "VALUES (" + neuronNumber + ", " + neuronConfig.get("signalThreshold") + ", 1)");
*/	
			}
			
			
			for(int neuronNumber = neuronSensorAmount; neuronNumber < neuronAmount; neuronNumber++){
				Map<String, Object> item = new HashMap<String, Object>();
				item.put("idx", neuronNumber);
				item.put("threshold", neuronConfig.get("signalThreshold"));
				item.put("type", 0);
				myDb.putData(item, DataContainer.NAME_NEURONS);
/*	TODO: Remove
				stmt.executeUpdate("INSERT INTO neurons (`idx`, `threshold`, `type`) "
						+ "VALUES (" + neuronNumber + ", " + neuronConfig.get("signalThreshold") + ", 0)");
*/				
				if(neuronNumber % tenPercentsAmountOfNeuron == 0){
					System.out.println("GermBrain:      " + neuronNumber / tenPercentsAmountOfNeuron + "0%");
				}
			}
			
			System.out.println("GermBrain:     100% Complete.\n");
			
			System.out.println("GermBrain:   Connecting Neurons Randomly...");
			
			for(int neuronNumber = 0; neuronNumber < neuronAmount; neuronNumber++){
				int tmpPoint = ((Math.abs(randNum.nextInt())) % (neuronAmount - neuronSensorAmount - (connectionMaxAmount * 2))) + connectionMaxAmount + neuronSensorAmount;
				int targetAmount = (int) ((Math.abs(randNum.nextInt())) % (connectionMaxAmount - connectionMinAmount + 1)) + connectionMinAmount;
				List<Integer> tmpTargets = new ArrayList<Integer>(targetAmount * 2);	
				
				for(int targetedCompleteAmount = 0; targetedCompleteAmount != targetAmount;){
					int tmpTarget = tmpPoint + (int) ((Math.abs(randNum.nextInt())) % (connectionMaxAmount * 2)) - connectionMaxAmount;
					if(!tmpTargets.contains(tmpTargets)){
						tmpTargets.add(tmpTarget);
						targetedCompleteAmount++;
					}
				}
				
				for(int j = 0; j < targetAmount; j++){
					Map<String, Object> item = new HashMap<String, Object>();
					item.put("neuron_idx", neuronNumber);
					item.put("target_idx", tmpTargets.get(j));
					item.put("score", defaultScore);
					myDb.putData(item, DataContainer.NAME_CONNECTION);
/* TODO: Remove
					stmt.executeUpdate("INSERT INTO connection (`neuron_idx`, `target_idx`, `score`) "
							+ "VALUES (" + neuronNumber + ", " + tmpTargets.get(j) + ", " + defaultScore + ");");
*/
				}
				if(neuronNumber % tenPercentsAmountOfNeuron == 0){
					System.out.println("GermBrain:      " + neuronNumber / tenPercentsAmountOfNeuron + "0%");
				}
			}
			System.out.println("GermBrain:     100% Complete.\n");
			
			System.out.println("GermBrain:   Selecting Actor Neurons...");
			int tmpNeuronNumber = neuronAmount / 2;
			
			for(int selectedNeuronAmount = 0; selectedNeuronAmount < neuronActorAmount;){
				int connectionAmount = 0;
				
				Map<String, Object> where = new HashMap<String, Object>();
				where.put("target_idx", tmpNeuronNumber);
				connectionAmount = myDb.countItems(where, DataContainer.NAME_CONNECTION);
				
/* TODO: Remove				
				rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM connection WHERE target_idx = " + tmpNeuronNumber + " GROUP BY target_idx;");
				
				if(rs.next()){
					connectionAmount = rs.getInt("count");
				}
*/
				if(connectionAmount != 0){
					Map<String, Object> update = new HashMap<String, Object>();
					where.clear();
					
					update.put("type", 2);
					where.put("idx", tmpNeuronNumber);
					myDb.updateData(update, where, DataContainer.NAME_NEURONS);
					
					where.clear();
					where.put("neuron_idx", tmpNeuronNumber);
					myDb.removeData(where, DataContainer.NAME_CONNECTION);
					
/* TODO: Remove
					Statement stmt2 = conn.createStatement();
					stmt2.executeUpdate("UPDATE neurons SET type = 2 WHERE idx = " + tmpNeuronNumber + ";");
					stmt2.executeUpdate("DELETE FROM connection WHERE neuron_idx = " + tmpNeuronNumber + ";");
*/
					actorNeuronNums.put(tmpNeuronNumber, selectedNeuronAmount);
					System.out.println("GermBrain:     Neuron" + tmpNeuronNumber + " is selected.");
					selectedNeuronAmount++;
				}
				
				tmpNeuronNumber++;
			}
			
//		} catch(SQLException e){
//			e.printStackTrace();
//		}
			
		System.out.println("\nGermBrain: Initializing Neurons Complete.\n");
		try{
			Thread.sleep(1000);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private void clearNeuron(){
		System.out.print("GermBrain: Clear Neurons... ");
		myDb.init();
		
/* TODO: Remove
		try{
			stmt.executeUpdate("TRUNCATE TABLE neurons");
			stmt.executeUpdate("TRUNCATE TABLE connection");
			
		}catch(SQLException e){
			e.printStackTrace();
		}
*/
		System.out.println("GermBrain: Compete.");
	}
	
	public void putSignal(Time time, int sensorNeuronNum, int signalPower){
		try {
			Map<String, Object> where = new HashMap<String, Object>();
			where.put("neuron_idx", sensorNeuronNum);
			ResultSet rs2 = myDb.getResultSet(where, DataContainer.NAME_CONNECTION);
/* TODO: Remove			
			rs2 = stmt.executeQuery("SELECT * FROM connection WHERE neuron_idx = " + sensorNeuronNum + ";");
*/			
			
			while(rs2.next()){
//				System.out.println("idx: " + rs.getInt("idx"));
				
				Map<String, Object> update = new HashMap<String, Object>();
				where.clear();
				
				update.put("time1", time.getTime1());
				update.put("time2", time.getTime2());
				update.put("time3", time.getTime3());
				update.put("signalPower", (signalPower + rs2.getInt("signalPower")));
				where.put("idx", rs2.getInt("idx"));
				myDb.updateData(update, where, DataContainer.NAME_CONNECTION);
				
/* TODO: Remove
				stmt2.execute("UPDATE connection SET time1 = " + time.getTime1() 
						+ ", time2 = " + time.getTime2() 
						+ ", time3 = " + time.getTime3() 
						+ ", signalPower = " + (signalPower + rs.getInt("signalPower"))
						+ " WHERE idx = " + rs.getInt("idx") + ";");
*/
				
				GermBrainNeuronJob newJob = new GermBrainNeuronJob();
				newJob.time = new Time(time);
				newJob.time.increaseTime();
				
				newJob.requestNeuronNum = sensorNeuronNum;
				newJob.targetNeuronNum = rs2.getInt("target_idx");
				threadLock.lock();
				jobQueue.add(newJob);
				threadLock.unlock();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void stimulateActor(int actorNeuronNum){
		if(actorNeuronNums.get(actorNeuronNum) >= 0
				&& actorNeuronNums.get(actorNeuronNum) <= 2){
			master.go();
		} else if(actorNeuronNums.get(actorNeuronNum) == 3){
			master.turnLeft();
		} else if(actorNeuronNums.get(actorNeuronNum) == 4){
			master.turnRight();
		}
	}
	
	public void checkBody(){
		master.checkBody();
	}

	public void makeNewConnection(int connectionIdx) {
		int connectionMaxAmount = neuronConfig.get("connectionMaxAmount");
		int defaultScore = neuronConfig.get("defaultScore");
		
		int neuron_idx = 0;
		int tmpTarget = 0;
		
		ResultSet rs;
		
		try {
			Map<String, Object> where = new HashMap<String, Object>();
			where.put("idx", connectionIdx);
			rs = myDb.getResultSet(where, DataContainer.NAME_CONNECTION);
			
/* TODO: Remove			
			rs = stmt.executeQuery("SELECT * FROM connection WHERE idx = " + connectionIdx + ";");
*/
			rs.next();
			neuron_idx = rs.getInt("neuron_idx");
			int tmpPoint = rs.getInt("target_idx");
			
			while(true){
				tmpTarget = tmpPoint + (int) ((Math.abs(randNum.nextInt())) % (connectionMaxAmount * 2)) - connectionMaxAmount;
				
				where.clear();
				where.put("neuron_idx", neuron_idx);
				where.put("target_idx", tmpTarget);
	
				int count = myDb.countItems(where, DataContainer.NAME_CONNECTION);
				if(count == 0)
					break;
				else
					if(connectionMaxAmount <= count)
						connectionMaxAmount = count * 2;
				
/* TODO: Remove				
				rs = stmt.executeQuery("SELECT COUNT(*) as count FROM connection WHERE neuron_idx = " + neuron_idx + " AND target_idx = " + tmpTarget + " GROUP BY neuron_idx;");
				if(!rs.next()){
					break;
				} else {
					if(connectionMaxAmount <= rs.getInt("count")){
						connectionMaxAmount = rs.getInt("count") * 2;
					}
				}
*/
			}
			
			Map<String, Object> insert = new HashMap<String, Object>();
			
			insert.put("neuron_idx", neuron_idx);
			insert.put("target_idx", tmpTarget);
			insert.put("score", defaultScore);
			myDb.putData(insert, DataContainer.NAME_CONNECTION);
			
			Map<String, Object> update = new HashMap<String, Object>();
			where.clear();
			update.put("score", defaultScore);
			where.put("idx", connectionIdx);
			myDb.updateData(update, where, DataContainer.NAME_CONNECTION);
			
/*TODO Remove
			stmt.executeUpdate("INSERT INTO connection (`neuron_idx`, `target_idx`, `score`) "
					+ "VALUES (" + neuron_idx + ", " + tmpTarget + ", " + defaultScore + ");");
			stmt.executeUpdate("UPDATE connection SET score = " + defaultScore + " WHERE idx = " + connectionIdx + ";");
*/
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(Nature.time.getTime3() + ") GermBrain: Make New Neuron Connection (" + neuron_idx + " - " + tmpTarget + ")");
	}

}

