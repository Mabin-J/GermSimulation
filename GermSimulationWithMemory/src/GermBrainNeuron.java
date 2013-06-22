import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import DataContainer.DataContainer;


public class GermBrainNeuron extends Thread{
	List<GermBrainNeuronJob> jobQueue;
	Map<String, String> dbConfig;
	Map<String, Integer> neuronConfig;
	Lock threadLock;
	static Lock globalTimeLock;
	GermBrainNeuronJob thisJob;
	GermBrain brain;
	
	int signalDecreasePowerPerTurn;
	
	DataContainer myDb;
/*	
	Connection conn;
	Statement stmt = null;
	Statement stmt2 = null;
*/
	ResultSet rs = null;
	ResultSet rs2 = null;
	
	public GermBrainNeuron(GermBrain brain, Time globalTime, List<GermBrainNeuronJob> jobQueue, Map<String, String> dbConfig, Map<String, Integer> neuronConfig, Lock threadLock){
		super();
		
		this.brain = brain;
		this.jobQueue = jobQueue;
		this.dbConfig = dbConfig;
		this.neuronConfig = neuronConfig;
		this.threadLock = threadLock;
		
		if(globalTimeLock == null)
			globalTimeLock = new ReentrantLock();
		
		signalDecreasePowerPerTurn = neuronConfig.get("signalDecreasePowerPerTurn");
	
		myDb = new DataContainer();
		
/* TODO: Remove
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch(Exception e){
			System.out.println("Cannot Found ConnectorJ Class");
		}
		
		conn = null;
		
		try{
			conn = DriverManager.getConnection("jdbc:mysql://" + dbConfig.get("host") + "/" + dbConfig.get("dbname")
					, dbConfig.get("username")
					, dbConfig.get("password"));
			stmt = conn.createStatement();	
			stmt2 = conn.createStatement();
		} catch(SQLException e){
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
		}
*/
	}
	
	public void run(){
		while(true){
			if(Main.pauseSignal)
				while(Main.pauseSignal)
					try {
						Thread.sleep(1000);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
			
			try{
//				Thread.sleep(100);
			} catch (Exception e){
				e.printStackTrace();
			}
			
			threadLock.lock();
			if(!jobQueue.isEmpty()){
				thisJob = jobQueue.get(0);
				jobQueue.remove(0);
			}
			threadLock.unlock();
			
			if(thisJob != null){
				// for Debug
//				System.out.println(Nature.time.getTime3() + ") Neuron: StartJob (" + thisJob.targetNeuronNum + ")");
				
				Nature.time.replaceOldTime(thisJob.time);
				
				try{
					int signalPowerSum = 0;
					List<Integer> connectionIdxs = new ArrayList<Integer>();
					List<Integer> scores = new ArrayList<Integer>();
					
					Map<String, Object> where = new HashMap<String, Object>();
					where.put("target_idx", thisJob.targetNeuronNum);
					rs = myDb.getResultSet(where, DataContainer.NAME_CONNECTION);
/* TODO: Remove					
					rs = stmt.executeQuery("SELECT * FROM connection WHERE target_idx = " + thisJob.targetNeuronNum + ";");
*/
					
					while(rs.next()){
						int time1 = rs.getInt("time1");
						int time2 = rs.getInt("time2");
						int time3 = rs.getInt("time3");
						int signalPower = rs.getInt("signalPower");
						int score = rs.getInt("score");
						int connectionIdx = rs.getInt("idx");
						
						int diffTime = thisJob.time.diffTime(time1, time2, time3); 
						
						where.clear();
						where.put("idx", rs.getInt("neuron_idx"));
						rs2 = myDb.getResultSet(where, DataContainer.NAME_NEURONS);
/* TODO: Remove
						rs2 = stmt2.executeQuery("SELECT * FROM neurons WHERE idx = " + rs.getInt("neuron_idx"));
*/
						rs2.next();
//						System.out.println("type=" + rs2.getInt("type"));
						if(rs2.getInt("type") == 0){
							if(diffTime > (signalPower / signalDecreasePowerPerTurn)){
//								try{
									Map<String, Object> update = new HashMap<String, Object>();
									where.clear();
									update.put("score", (score - 1));
									where.put("idx", connectionIdx);
/* TODO: Remove									
									stmt2.executeUpdate("UPDATE connection SET score = " + (score - 1) 
											+ " WHERE idx = " + connectionIdx + ";");
*/
//								} catch (SQLException e){
									//TODO:
//									e.printStackTrace();
//								}
								
								if(score <= 0){
									where.clear();
									where.put("idx", connectionIdx);
									
									myDb.removeData(where, DataContainer.NAME_CONNECTION);
/*
									stmt2.executeUpdate("DELETE FROM connection WHERE idx = " + connectionIdx + ";");
*/
								}
								
								continue;
							}
						}

						signalPowerSum += signalPower - (diffTime * signalDecreasePowerPerTurn);
						connectionIdxs.add(connectionIdx);
						scores.add(rs.getInt("score"));
					}
					
					where.clear();
					where.put("idx", thisJob.targetNeuronNum);
					
					rs = myDb.getResultSet(where, DataContainer.NAME_NEURONS);

/* TODO: Remove
					rs = stmt.executeQuery("SELECT * FROM neurons WHERE idx = " + thisJob.targetNeuronNum + ";");
*/
					rs.next();
					int threshold = rs.getInt("threshold");
					int type = rs.getInt("type");
					
					if(signalPowerSum >= threshold){
						for(int i = 0; i < connectionIdxs.size(); i++){
							Map<String, Object> update = new HashMap<String, Object>();
							update.put("signalPower", 0);
							update.put("score", (scores.get(i) + 1));
							update.put("time1", thisJob.time.getTime1());
							update.put("time2", thisJob.time.getTime2());
							update.put("time3", thisJob.time.getTime3());
							
							where.clear();
							where.put("idx", connectionIdxs.get(i));
							
							myDb.updateData(update, where, DataContainer.NAME_CONNECTION);
							
/* TODO: Remove
							stmt.executeUpdate("UPDATE connection SET signalPower = 0, score = " + (scores.get(i) + 1) 
									+ ", time1 = " + thisJob.time.getTime1() 
									+ ", time2 = " + thisJob.time.getTime2() 
									+ ", time3 = " + thisJob.time.getTime3() 
									+ " WHERE idx = " + connectionIdxs.get(i) + ";");
*/
							
							if(scores.get(i) > (neuronConfig.get("defaultScore") * 2)){
								brain.makeNewConnection(connectionIdxs.get(i));
							}
						}
						
						
						
						if(type == 2){
							brain.stimulateActor(thisJob.targetNeuronNum);
						} else {
							where.clear();
							where.put("neuron_idx", thisJob.targetNeuronNum);
	
							ResultSet rs2 = myDb.getResultSet(where, DataContainer.NAME_CONNECTION);
/* TODO: Remove
							rs = stmt.executeQuery("SELECT * FROM connection WHERE neuron_idx = " + thisJob.targetNeuronNum + ";");
*/
							
							int connectionAmount = myDb.countItems(where, DataContainer.NAME_CONNECTION);
							signalPowerSum /= connectionAmount;
							
							while(rs2.next()){
								thisJob.time.increaseTime();
								
								Map<String, Object> update = new HashMap<String, Object>();
								where.clear();
								where.put("idx", rs2.getInt("idx"));
								update.put("time1", thisJob.time.getTime1());
								update.put("time2", thisJob.time.getTime2());
								update.put("time3", thisJob.time.getTime3());
								update.put("signalPower", (rs2.getInt("signalPower") + signalPowerSum));
								myDb.updateData(update, where, DataContainer.NAME_CONNECTION);
								
/* TODO: Remove
								stmt2.executeUpdate("UPDATE connection SET time1 = " + thisJob.time.getTime1() 
										+ ", time2 = " + thisJob.time.getTime2() 
										+ ", time3 = " + thisJob.time.getTime3() 
										+ ", signalPower = " + (rs.getInt("signalPower") + signalPowerSum)
										+ " WHERE neuron_idx = " + thisJob.targetNeuronNum + ";");
*/
								
								GermBrainNeuronJob newJob = new GermBrainNeuronJob();
								newJob.time = new Time(thisJob.time);
								
								newJob.requestNeuronNum = thisJob.targetNeuronNum;
								newJob.targetNeuronNum = rs2.getInt("target_idx");
								threadLock.lock();
								jobQueue.add(newJob);
								threadLock.unlock();
							}
						}
					}
				}catch (SQLException e){
					//TODO: Fill this
					e.printStackTrace();
				}
			} else {
				globalTimeLock.lock();
				Nature.time.increaseTime();
				globalTimeLock.unlock();
			}
			globalTimeLock.lock();
			boolean check = Nature.time.isCheckTime();
			globalTimeLock.unlock();
			
			if(check){
				brain.checkBody();
			}
			
			thisJob = null;
		}
	}
}
