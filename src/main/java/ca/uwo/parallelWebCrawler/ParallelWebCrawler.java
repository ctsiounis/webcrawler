package ca.uwo.parallelWebCrawler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import ca.uwo.tools.AtomicCounter;
import ca.uwo.tools.Counter;
import ca.uwo.tools.UrlChecker;
import ca.uwo.webCrawler.nodes.INodeLink;
import ca.uwo.webCrawler.nodes.ParallelNodeLink;

public class ParallelWebCrawler {

	UrlChecker checker = new UrlChecker();
	Counter counter;
	ParallelNodeLink root;
	List<CompletableFuture<List<ParallelNodeLink>>> futures = new ArrayList<>();

	public static void main(String[] args) {
		String initialURL = "http://www.uwo.ca";
		ParallelWebCrawler crawler = new ParallelWebCrawler();
		crawler.crawl(initialURL, 10);

	}

	public void crawl(String initialURL, int numberOfNodes) {
		//List<ParallelNodeLink> nodesToVisit = new ArrayList<ParallelNodeLink>();
		counter = new AtomicCounter(numberOfNodes);
		counter.reachedTarget();

		// Add root to hashmap of existing nodes
		// and list of nodes that need visiting
		root = new ParallelNodeLink(initialURL, counter, checker);
		checker.addNodeLink(root.getStringUrl(), root);
		try {
			root.get().get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Finished!!!");
	}

	public Map<String, INodeLink> getExisting() {
		return checker.getExisting();
	}

	public ParallelNodeLink getRoot() {
		return root;
	}

	public Counter getCounter() {
		return counter;
	}
	
	
}
