
public class Time {
	private int time1;
	private int time2;
	private int time3;
	private int term = 1;
	private int checkCnt = 1;
	private boolean check = false;
	
	public Time(){
		time1 = 0;
		time2 = 0;
		time3 = 0;
	}
	
	public Time(Time src){
		time1 = src.getTime1();
		time2 = src.getTime2();
		time3 = src.getTime3();
	}
	
	private void calcTime(){
		if(time3 >= 1000000000){
			time2 += time3 - 999999999;
			time3 -= 1000000000;
		}
		
		if(time2 >= 1000000000){
			time1 += time2 - 999999999;
			time2 -= 1000000000;
		}
	}
	
	public void increaseTime(){
		time3++;
		term++;
		if(term / (100 * checkCnt) >= 1){
			check = true;
			checkCnt++;
		}
		calcTime();
	}
	
	public boolean isCheckTime(){
		boolean tmpCheck = check;
		check = false;
		return tmpCheck;
	}
	
	public int getTime1(){
		return time1;
	}
	
	public int getTime2(){
		return time2;
	}
	
	public int getTime3(){
		return time3;
	}
	
	public void copyFrom(Time src){
		time1 = src.getTime1();
		time2 = src.getTime2();
		time3 = src.getTime3();
	}
	
	public int diffTime(Time src){
		if(time1 != src.getTime1())
			return 1000000000;
		
		if(time2 - src.getTime2() > 1
				|| time2 - src.getTime2() < -1)
			return 1000000000;
		
		if(time2 - src.getTime2() == 1){
			return 1000000000 + time3 - src.getTime3();
		} else if(time2 - src.getTime2() == -1){
			return time3 - (1000000000 + src.getTime3());
		} else {
			return time3 - src.getTime3();
		}
	}
	
	public int diffTime(int srcTime1, int srcTime2, int srcTime3){
		if(time1 != srcTime1)
			return 1000000000;
		
		if(time2 - srcTime2 > 1
				|| time2 - srcTime2 < -1)
			return 1000000000;
		
		if(time2 - srcTime2 == 1){
			return 1000000000 + time3 - srcTime3;
		} else if(time2 - srcTime2 == -1){
			return time3 - (1000000000 + srcTime3);
		} else {
			return time3 - srcTime3;
		}
	}
	
	public void replaceOldTime(Time src){
		if(time1 < src.time1){
			time1 = src.time1;
			time2 = src.time2;
			time3 = src.time3;
		} else if(time1 == src.time1
				&& time2 < src.time2){
			time1 = src.time1;
			time2 = src.time2;
			time3 = src.time3;
		} else if(time1 == src.time1
				&& time2 == src.time2
				&& time3 < src.time3){
			time1 = src.time1;
			time2 = src.time2;
			time3 = src.time3;
		}
	}
}