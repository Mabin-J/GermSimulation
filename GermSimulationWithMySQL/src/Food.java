import java.util.Map;


public class Food extends Objects{
	private int calories;
	
	public Food(Nature myNature, Map<String, Integer> foodConfig) {
		super(myNature);
		
		smell = 255;
		eatable = true;
		throughable = false;
		thisType = "Food";
		
		calories = foodConfig.get("calories");
	}
	
	public int getCalories(){
		return calories;
	}
}
