package ca.uwo.tools;

public class SimpleCounter implements Counter {
	private int target;
	private int counter = 0;
	private int responses100 = 0;
	private int responses200 = 0;
	private int responses300 = 0;
	private int responses400 = 0;
	private int responses500 = 0;
	
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

	@Override
	public int get100() {
		return responses100;
	}


	@Override
	public void increase100() {
		responses100++;
	}

	@Override
	public int get200() {
		return responses200;
	}


	@Override
	public void increase200() {
		responses200++;
	}


	@Override
	public int get300() {
		return responses300;
	}


	@Override
	public void increase300() {
		responses300++;
	}


	@Override
	public int get400() {
		return responses400;
	}


	@Override
	public void increase400() {
		responses400++;
	}


	@Override
	public int get500() {
		return responses500;
	}


	@Override
	public void increase500() {
		responses500++;
	}

}
