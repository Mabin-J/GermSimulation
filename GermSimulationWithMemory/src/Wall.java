
public class Wall extends Objects{
	public Wall(Nature myNature){
		super(myNature);
		eatable = false;
		smell = 0;
		throughable = false;
		thisType = "Wall";
	}
}
