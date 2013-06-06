
public abstract class Objects {
	protected boolean eatable;
	protected int smell;
	protected boolean throughable;
	protected int locationX, locationY;
	protected Nature myNature;
	protected String thisType;
	
	public Objects(Nature myNature){
		this.myNature = myNature;
		eatable = false;
		smell = 0;
		throughable = true;
	}
	
	public boolean isEatable(){
		return eatable;
	}

	public void setSmell(int strength){
		smell = strength;
	}
	
	public int getSmell(){
		return smell;
	}
	
	public boolean isThroughable(){
		return throughable;
	}
	
	public void setLoctionX(int x){
		this.locationX = x;
	}
	
	public void setLocationY(int y){
		this.locationY = y;
	}
	
	public void setLocation(int x, int y){
		this.locationX = x;
		this.locationY = y;
	}
	
	public int getLocationX(){
		return locationX;
	}
	
	public int getLocationY(){
		return locationY;
	}
	
	public String getType(){
		return thisType;
	}
}
