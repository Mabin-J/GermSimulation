import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Nature {
	private Map<Integer, Map<Integer, Objects>> area;
	private Map<Integer, Map<Integer, Integer>> smells;
	
	private int sizeX, sizeY;
	private Map<String, Integer> foodConfig;
	
	public static Time time;
	
	public Nature(Map<String, Integer> natureConfig, Map<String, Integer> foodConfig){
		sizeX = natureConfig.get("sizeX");
		sizeY = natureConfig.get("sizeY");
		
		this.foodConfig = foodConfig;
		
		area = new HashMap<Integer, Map<Integer, Objects>>(sizeX * 2);
		smells = new HashMap<Integer, Map<Integer, Integer>>(sizeX * 2);

		buildSquareWall();
		
		time = new Time();
	}
	
	private void buildSquareWall(){
		Map<Integer, Objects> areaY = new HashMap<Integer, Objects>();
		Wall wall = new Wall(this);
		for(int i = 0; i < sizeY; i++){
			areaY.put(i, wall);
		}
		
		area.put(0, areaY);
		area.put(sizeX - 1, areaY);
		
		for(int i = 1; i < sizeX - 1; i++){
			Map<Integer, Objects> wallYpiece1 = new HashMap<Integer, Objects>();
			wallYpiece1.put(0, wall);
			wallYpiece1.put(sizeY - 1, wall);
			area.put(i, wallYpiece1);
		}
	}
	
	public void addObject(Objects obj){
		int tmpX, tmpY;
		while(true){
			tmpX = (int) (Math.random() * sizeX);
			tmpY = (int) (Math.random() * sizeX);
			
			if(!area.containsKey(tmpX))
				break;
			else {
				Map<Integer, Objects> areaYHandler = area.get(tmpX);
				if(!areaYHandler.containsKey(tmpY))
					break;
			}
		}
		
		Map<Integer, Objects> areaY;
		
		if(!area.containsKey(tmpX)){
			areaY = new HashMap<Integer, Objects>();
			area.put(tmpX, areaY);
		} else {
			areaY = area.get(tmpX);
		}
		areaY.put(tmpY, obj);
		
		obj.setLocation(tmpX, tmpY);
	}
	
	public void refreshTime(Time src){
		if(time.getTime1() < src.getTime1()
				|| time.getTime2() < src.getTime2()
				|| time.getTime3() < src.getTime3()){
			time.copyFrom(src);
		}
	}
	
	public void moveObject(Objects obj, int x, int y){
		int currentX = obj.getLocationX();
		int currentY = obj.getLocationY();
		
		int targetX = currentX + x;
		int targetY = currentY + y;
		if(!area.get(targetX).containsKey(targetY)){
			area.get(currentX).remove(currentY);
			area.get(targetX).put(targetY, obj);
			obj.setLocation(targetX, targetY);
		}
	}
	
	public boolean isExistObjects(int x, int y){
		if(area.containsKey(x))
			if(area.get(x).containsKey(y))
				return true;
		
		return false;
	}
	public boolean isThrowable(int x, int y){
		if(area.get(x).containsKey(y)){
			Objects tmpObject = area.get(x).get(y);
			if(tmpObject.isThroughable()){
				tmpObject.getType();
				return true;
			}else
				return false;
		}
		return true;
	}
	public Objects getObjects(int x, int y){
		if(area.containsKey(x))
			if(area.get(x).containsKey(y))
				return area.get(x).get(y);
		
		return null;
	}
	
	public int getSmell(int x, int y){
		if(smells.containsKey(x)){
			if(smells.get(x).containsKey(y)){
				return smells.get(x).get(y);
			}
		}
		return 0;
	}
	
	public void resetFood(Objects obj){
		area.get(obj.getLocationX()).remove(obj.getLocationY());
		smells = new HashMap<Integer, Map<Integer, Integer>>();
		
		setNewFood();
	}
	
	public void setNewFood(){
		Food newFood = new Food(this, foodConfig);
		addObject(newFood);
		
		int smell = newFood.getSmell();
		int smellLange = sizeX / 5;
		
		System.out.println("Food Located (X: " + newFood.getLocationX()
				+ ", Y:" + newFood.getLocationY() + ")");
		
		int startPointX = newFood.getLocationX();
		int startPointY = newFood.getLocationY();
		
		int tmpSmell = smell;
		for(int i = 1; i <= smellLange; i++){
			tmpSmell -= (smell / smellLange);
			startPointX--;
			startPointY--;
			
			int tmpPointX = startPointX;
			int tmpPointY = startPointY;
			for(int j = 0; j < (2 * i + 1); j++){
				if(tmpPointX >= 0 && tmpPointY >= 0){
					putSmell(tmpPointX, tmpPointY, tmpSmell);
				}
				tmpPointX++;
			}
			
			for(int j = 1; j < (2 * i + 1); j++){
				if(tmpPointX >= 0 && tmpPointY >= 0){
					putSmell(tmpPointX, tmpPointY, tmpSmell);
				}
				tmpPointY++;
			}
			
			for(int j = 1; j < (2 * i + 1); j++){
				if(tmpPointX >= 0 && tmpPointY >= 0){
					putSmell(tmpPointX, tmpPointY, tmpSmell);
				}
				tmpPointX--;
			}
			for(int j = 1; j < (2 * i + 1); j++){
				if(tmpPointX >= 0 && tmpPointY >= 0){
					putSmell(tmpPointX, tmpPointY, tmpSmell);
				}
				tmpPointY--;
			}
		}
		
//		print smell map
/*		
		for(int i = 0; i < sizeY; i++){
			for(int j = 0; j < sizeX; j++){
				if(smells.containsKey(j)){
					if(smells.get(j).containsKey(i)){
						System.out.printf("%3d ", smells.get(j).get(i));
					} else {
						System.out.printf("%3d ", 0);
					}
				} else {
					System.out.printf("%3d ", 0);
				}
			}
			System.out.println(" ");
		}
*/
	}
	
	public void putSmell(int x, int y, int smellStrength){
		if(smells.containsKey(x)){
			smells.get(x).put(y, smellStrength);
		} else {
			Map<Integer, Integer> smellY = new HashMap<Integer, Integer>();
			smellY.put(y, smellStrength);
			smells.put(x, smellY);
		}
	}
}
