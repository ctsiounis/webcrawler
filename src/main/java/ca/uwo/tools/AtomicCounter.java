package ca.uwo.tools;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicCounter implements Counter{
	private AtomicInteger counter = new AtomicInteger(0);
	private int target;
	private AtomicInteger responses100 = new AtomicInteger(0);
	private AtomicInteger responses200 = new AtomicInteger(0);
	private AtomicInteger responses300 = new AtomicInteger(0);
	private AtomicInteger responses400 = new AtomicInteger(0);
	private AtomicInteger responses500 = new AtomicInteger(0);
	
	public AtomicCounter(int target) {
		this.target = target;
	}
	
	public boolean reachedTarget() {
		return counter.addAndGet(1) > target;
	}

	@Override
	public void reduce() {
		counter.decrementAndGet();
	}

	@Override
	public int get100() {
		return responses100.addAndGet(0);
	}

	@Override
	public void increase100() {
		responses100.addAndGet(1);
	}
	
	@Override
	public int get200() {
		return responses200.addAndGet(0);
	}

	@Override
	public void increase200() {
		responses200.addAndGet(1);
	}

	@Override
	public int get300() {
		return responses300.addAndGet(0);
	}

	@Override
	public void increase300() {
		responses300.addAndGet(1);
	}

	@Override
	public int get400() {
		return responses400.addAndGet(0);
	}

	@Override
	public void increase400() {
		responses400.addAndGet(1);
	}

	@Override
	public int get500() {
		return responses500.addAndGet(0);
	}

	@Override
	public void increase500() {
		responses500.addAndGet(1);
	}

}
