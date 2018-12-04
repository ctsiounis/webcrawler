package ca.uwo.tools;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicCounter implements Counter{
	private AtomicInteger counter = new AtomicInteger(0);
	int target;
	
	public AtomicCounter(int target) {
		this.target = target;
	}
	
	public boolean reachedTarget() {
		return counter.addAndGet(1)>target;
	}

	@Override
	public void reduce() {
		counter.decrementAndGet();
	}

}
