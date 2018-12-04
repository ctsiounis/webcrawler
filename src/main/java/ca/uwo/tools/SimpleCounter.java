package ca.uwo.tools;

public class SimpleCounter implements Counter {
	int target;
	int counter = 0;
	
	public SimpleCounter(int target) {
		this.target = target;
	}


	@Override
	public boolean reachedTarget() {
		counter++;
		return counter > target;
	}


	@Override
	public void reduce() {
		counter--;
	}

}
