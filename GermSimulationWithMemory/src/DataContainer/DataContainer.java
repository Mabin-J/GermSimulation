package DataContainer;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class DataContainer {
	public static final int NAME_NEURONS = 0;
	public static final int NAME_CONNECTION = 1;
	private static Table[] database;
	
	private static Lock dataLock;
	
	public DataContainer(){
		if(database == null){
			database = new Table[2];
			this.dataLock = new ReentrantLock();
			init();
		}
	}
	
	public DataContainer(String filename){
		//TODO: Read Data from File
	}
	
	public void init(){
		database[NAME_NEURONS] = new Table(new String[]{"idx", "threshold", "type"});
		database[NAME_NEURONS].setAutoIncreaseOpt("idx");
		database[NAME_NEURONS].setUnique("idx");
		
		database[NAME_CONNECTION] = new Table(new String[]{"idx", "neuron_idx", "target_idx", "time1", "time2", "time3", "score", "signalPower"});
		database[NAME_CONNECTION].setAutoIncreaseOpt("idx");
		database[NAME_CONNECTION].setUnique("idx");
		database[NAME_CONNECTION].setIndex("neuron_idx");
		database[NAME_CONNECTION].setDefault("time1", 0);
		database[NAME_CONNECTION].setDefault("time2", 0);
		database[NAME_CONNECTION].setDefault("time3", 0);
		database[NAME_CONNECTION].setDefault("signalPower", 0);
	}
	
	public int countItems(Map<String, Object> where, int tableIdx){
		dataLock.lock();
		int result = database[tableIdx].countItems(where); 
		dataLock.unlock();
		return result;
	}
	
	public List<Object[]> getList(Map<String, Object> where, int tableIdx){
		dataLock.lock();
		List<Object[]> result = database[tableIdx].getList(where);
		dataLock.unlock();
		return result;
	}

	public ResultSet getResultSet(int tableIdx){
		dataLock.lock();
		ResultSet result = database[tableIdx].getResultSet();
		dataLock.unlock();
		return result;
	}
	
	public ResultSet getResultSet(Map<String, Object> where, int tableIdx){
		dataLock.lock();
		ResultSet result = database[tableIdx].getResultSet(where);
		dataLock.unlock();
		return result;
	}
	
	public int putData(Map<String, Object> args, int tableIdx){
		dataLock.lock();
		int result = database[tableIdx].putData(args);
		dataLock.unlock();
		return result;
	}
	
	public int updateData(Map<String, Object> update, Map<String, Object> where, int tableIdx){
		dataLock.lock();
		int result = database[tableIdx].updateData(update, where);
		dataLock.unlock();
		return result;
	}
	
	public int removeData(Map<String, Object> where, int tableIdx){
		dataLock.lock();
		int result = database[tableIdx].removeData(where);
		dataLock.unlock();
		return result;
	}

	
/* Backup	
	private void createTables(){
		String query = "";
		String querys[];

		System.out.print("GermBrain: Create Tables... ");
		
		try{
			String tmpLine;
			FileReader initSQLfr = new FileReader("./SQL/init.sql");
			BufferedReader initSQLbr = new BufferedReader(initSQLfr);
			while((tmpLine = initSQLbr.readLine()) != null)
				query += tmpLine + "\n";
			initSQLbr.close();
			initSQLfr.close();
		} catch(Exception e){
			e.printStackTrace();
			return;
		}
		
		querys = query.split(";");
		
		try{
			for(int i = 0; i < querys.length - 1; i++){
				stmt.executeUpdate(querys[i]);	
			}
		} catch(SQLException e){
			System.out.println("SQLException: " + e.getMessage());
 			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
		}
		System.out.println("GermBrain: Complete\n");
	}
*/
}