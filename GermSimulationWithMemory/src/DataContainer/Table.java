package DataContainer;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Table {
	private int fieldAmount = 0;
	private String[] fields;
	private boolean[] autoIncreaseOpt;
	private boolean[] indexOpt;
	private boolean[] uniqueOpt;
	private boolean[] defaultOpt;
	private boolean[] nullOpt;
	private int[] autoIncreaseVal;
	private Object[] defaultVal;
	private List<Object[]> data;
	private Map<Object, List<Object[]>>[] indexes;
	
//	static Lock dataLock;
	
	public Table(String[] fields){
		this.fields = fields;
		this.fieldAmount = fields.length;
		this.autoIncreaseOpt = new boolean[this.fieldAmount];
		this.indexOpt = new boolean[this.fieldAmount];
		this.uniqueOpt = new boolean[this.fieldAmount];
		this.defaultOpt = new boolean[this.fieldAmount];
		this.nullOpt = new boolean[this.fieldAmount];
		this.autoIncreaseVal = new int[this.fieldAmount];
		this.defaultVal = new Object[this.fieldAmount];
		this.data = new ArrayList<Object[]>();
		this.indexes = new Map[this.fieldAmount];
		
//		this.dataLock = new ReentrantLock();
		
		for(int i = 0; i < fieldAmount; i++){
			autoIncreaseOpt[i] = false;
			indexOpt[i] = false;
			uniqueOpt[i] = false;
			nullOpt[i] = false;
			autoIncreaseVal[i] = 1;
		}
	}
	
	private int getFieldIdx(String fieldName){
		for(int i = 0; i < fieldAmount; i++){
			if(fields[i].equals(fieldName)){
				return i;
			}
		}
		return -1;
	}
	
	public void setAutoIncreaseOpt(String fieldName){
		this.setAutoIncreaseOpt(getFieldIdx(fieldName));
	}
	
	public void setAutoIncreaseOpt(int fieldNum){
		autoIncreaseOpt[fieldNum] = true;
		autoIncreaseVal[fieldNum] = 1;
	}
	
	public void setIndex(String fieldName){
		this.setIndex(getFieldIdx(fieldName));
	}
	
	public void setIndex(int fieldNum){
		indexOpt[fieldNum] = true;
		indexes[fieldNum] = new HashMap<Object, List<Object[]>>();
	}
	
	public void setUnique(int fieldNum){
		uniqueOpt[fieldNum] = true;
		setIndex(fieldNum);
	}
	
	public void setUnique(String fieldName){
		this.setUnique(getFieldIdx(fieldName));
	}

	public void setDefault(String fieldName, Object value){
		this.setDefault(getFieldIdx(fieldName), value);
	}
	
	public void setDefault(int fieldNum, Object value){
		defaultOpt[fieldNum] = true;
		defaultVal[fieldNum] = value;
	}
	
	public int putData(Map<String, Object> args){
		Object[] item = new Object[fieldAmount];
		int tmpIdx = 0;
		
		
//		dataLock.lock();
		for(String key: args.keySet()){
			if((tmpIdx = getFieldIdx(key)) < 0)
				return -1;
			
			if(uniqueOpt[tmpIdx]){
				Map<Object, List<Object[]>> indexHandler = indexes[tmpIdx];
				if(indexHandler.containsKey(args.get(key))){
//					dataLock.unlock();
					return -1;
				}
			}
			item[tmpIdx] = args.get(key);
		}
		
		for(int i = 0; i < fieldAmount; i++){
			if(autoIncreaseOpt[i]){
				if(args.containsKey(fields[i])){
					if((int)item[i] > autoIncreaseVal[i]){
						autoIncreaseVal[i] = (int)item[i] + 1;
					}
				} else {
					item[i] = autoIncreaseVal[i]++;
				}
			}
			
			if(defaultOpt[i]){
				if(!args.containsKey(fields[i])){
					item[i] = defaultVal[i];
				}
			}
			
			if(!nullOpt[i]){
				if(item[i] == null)
					return -1;
			}
			
			if(indexOpt[i]){
				Map<Object, List<Object[]>> indexHandler = indexes[i];
				List<Object[]> listHandler;
				if(indexHandler.containsKey(item[i])){
					listHandler = indexHandler.get(item[i]);
				}else {
					listHandler = new ArrayList<Object[]>();
					indexHandler.put(item[i], listHandler);
				}
				
				listHandler.add(item);
			}
		}
		data.add(item);
		
//		dataLock.unlock();
		return 0;
	}
	
	private List<Object[]> getList(){
		return (List<Object[]>) ((ArrayList)data).clone();
	}
	
	private List<Object[]> getList(int fieldNum, Object whereValue){
		List<Object[]> result = null;
		
//		dataLock.lock();
		if(indexOpt[fieldNum]){
			result = (List<Object[]>) ((ArrayList)indexes[fieldNum].get(whereValue)).clone();
		} else {
			result = new ArrayList<Object[]>();
			for(Object[] item: data){
				if((int)item[fieldNum] == (int)whereValue){
					result.add(item);
				}
			}
		}
//		dataLock.unlock();
		return result;
	}
	
	public ResultSet getResultSet(){
		return new MyResultSet(getList(), fields);
	}
	
	public ResultSet getResultSet(Map<String, Object> where){
		return new MyResultSet(getList(where), fields);
	}
	
	public List<Object[]> getList(Map<String, Object> where){
		if(!chkFieldName(where))
			return null;
		
		
		int tmpIdx = 0;
		List<Object[]> correctObject1 = null;
		List<Object[]> correctObject2 = null;
		List<Object[]> correctObject3 = null;
		
		for(String key: where.keySet()){
			int fieldNum = getFieldIdx(key);
			if(tmpIdx == 0){
				correctObject1 = getList(fieldNum, where.get(key));
				tmpIdx++;
/*				
				if(indexOpt[fieldNum]){
					correctObject1 = indexes[fieldNum].get(where.get(key));
				} else {
					correctObject1 = new ArrayList<Object[]>();
					for(Object[] item: data){
						if(item[fieldNum] == where.get(key)){
							correctObject1.add(item);
						}
					}
				}
*/
			} else {
				correctObject2 = getList(fieldNum, where.get(key));
				correctObject3 = new ArrayList<Object[]>();
				for(Object[] item: correctObject1){
					if(correctObject2.contains(item)){
						correctObject3.add(item);
					}
				}
				
				correctObject1 = correctObject3;
				tmpIdx++;
			}
		}
//		return (List<Object[]>) ((ArrayList)correctObject1).clone();
		
		if(correctObject1 != null)
			return (List<Object[]>) ((ArrayList)correctObject1).clone();
		
		return correctObject1;
	}
	
	public int countItems(Map<String, Object> where){
		List<Object[]> target = getList(where); 
		
		if(target == null)
			return -1;
		
		return getList(where).size();
	}
	
	private boolean chkFieldName(Map<String, Object> data){
		for(String key: data.keySet()){
			if(getFieldIdx(key) == -1){
				return false;
			}
		}
		return true;
	}
	
	public int updateData(Map<String, Object> update, Map<String, Object> where){
		if(!chkFieldName(update))
			return -1;
		
		List<Object[]> targetList = getList(where);
		if(targetList == null)
			return -1;
		
		for(Object[] item: targetList){
			for(String key: update.keySet()){
				item[getFieldIdx(key)] = update.get(key);
			}
		}
		
		return 0;
	}
	
	public int removeData(Map<String, Object> where){
		if(!chkFieldName(where))
			return -1;
		
		List<Object[]> targetList = getList(where);
		
		for(Object[] item: targetList){
			for(int i = 0; i < fieldAmount; i++){
				if(indexOpt[i]){
					List<Object[]> listHandler = indexes[i].get(item[i]);
					listHandler.remove(item);
					if(listHandler.size() == 0)
						indexes[i].remove(item[i]);
				}
			}
			data.remove(item);
		}
		
		return 0;
	}
}
