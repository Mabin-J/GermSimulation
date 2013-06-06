import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class GermBrainNeuron extends Thread{
	List<GermBrainNeuronJob> jobQueue;
	Map<String, String> dbConfig;
	Map<String, Integer> neuronConfig;
	Lock threadLock;
	static Lock globalTimeLock;
	GermBrainNeuronJob thisJob;
	GermBrain brain;
	
	int signalDecreasePowerPerTurn;
	
	Connection conn;
	Statement stmt = null;
	Statement stmt2 = null;
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
	}
	
	public void run(){
		while(true){
			threadLock.lock();
			if(!jobQueue.isEmpty()){
				thisJob = jobQueue.get(0);
				jobQueue.remove(0);
			}
			threadLock.unlock();
			
			if(thisJob != null){
				// for Debug
				//System.out.println(globalTime.getTime3() + ") Neuron: StartJob (" + thisJob.targetNeuronNum + ")");
				
				Nature.time.replaceOldTime(thisJob.time);
				
				try{
					int signalPowerSum = 0;
					List<Integer> connectionIdxs = new ArrayList<Integer>();
					List<Integer> scores = new ArrayList<Integer>();
					
					rs = stmt.executeQuery("SELECT * FROM connection WHERE target_idx = " + thisJob.targetNeuronNum + ";");
					
					while(rs.next()){
						int time1 = rs.getInt("time1");
						int time2 = rs.getInt("time2");
						int time3 = rs.getInt("time3");
						int signalPower = rs.getInt("signalPower");
						int score = rs.getInt("score");
						int connectionIdx = rs.getInt("idx");
						
						int diffTime = thisJob.time.diffTime(time1, time2, time3); 
						
						rs2 = stmt2.executeQuery("SELECT * FROM neurons WHERE idx = " + rs.getInt("neuron_idx"));
						rs2.next();
						if(rs2.getInt("type") == 0){
							if(diffTime > (signalPower / signalDecreasePowerPerTurn)){
								try{
									stmt2.executeUpdate("UPDATE connection SET score = " + (score - 1) 
											+ " WHERE idx = " + connectionIdx + ";");
								} catch (SQLException e){
									//TODO:
									e.printStackTrace();
								}
								
								if(score <= 0){
									stmt2.executeUpdate("DELETE FROM connection WHERE idx = " + connectionIdx + ";");
								}
								
								continue;
							}
						}

						signalPowerSum += signalPower - (diffTime * signalDecreasePowerPerTurn);
						connectionIdxs.add(connectionIdx);
						scores.add(rs.getInt("score"));
					}
					
					rs = stmt.executeQuery("SELECT * FROM neurons WHERE idx = " + thisJob.targetNeuronNum + ";");
					rs.next();
					int threshold = rs.getInt("threshold");
					int type = rs.getInt("type");
					
					if(signalPowerSum >= threshold){
						for(int i = 0; i < connectionIdxs.size(); i++){
							stmt.executeUpdate("UPDATE connection SET signalPower = 0, score = " + (scores.get(i) + 1) 
									+ ", time1 = " + thisJob.time.getTime1() 
									+ ", time2 = " + thisJob.time.getTime2() 
									+ ", time3 = " + thisJob.time.getTime3() 
									+ " WHERE idx = " + connectionIdxs.get(i) + ";");
							
							if(scores.get(i) > (neuronConfig.get("defaultScore") * 2)){
								brain.makeNewConnection(connectionIdxs.get(i));
							}
						}
						
						
						
						if(type == 2){
							brain.stimulateActor(thisJob.targetNeuronNum);
						} else {
							rs = stmt.executeQuery("SELECT * FROM connection WHERE neuron_idx = " + thisJob.targetNeuronNum + ";");
							while(rs.next()){
								signalPowerSum /= connectionIdxs.size();
								thisJob.time.increaseTime();
								stmt2.executeUpdate("UPDATE connection SET time1 = " + thisJob.time.getTime1() 
										+ ", time2 = " + thisJob.time.getTime2() 
										+ ", time3 = " + thisJob.time.getTime3() 
										+ ", signalPower = " + (rs.getInt("signalPower") + signalPowerSum)
										+ " WHERE neuron_idx = " + thisJob.targetNeuronNum + ";");
								
								GermBrainNeuronJob newJob = new GermBrainNeuronJob();
								newJob.time = new Time(thisJob.time);
								
								newJob.requestNeuronNum = thisJob.targetNeuronNum;
								newJob.targetNeuronNum = rs.getInt("target_idx");
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
