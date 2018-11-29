package ca.uwo.parallelWebCrawler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import ca.uwo.tools.AtomicCounter;
import ca.uwo.tools.Counter;
import ca.uwo.webCrawler.nodes.INodeLink;
import ca.uwo.webCrawler.nodes.ParallelNodeLink;

public class ParallelWebCrawler {

	Map<String, INodeLink> existing = new ConcurrentHashMap<String, INodeLink>();
	ParallelNodeLink root;
	List<CompletableFuture<List<ParallelNodeLink>>> futures = new ArrayList<>();

	public static void main(String[] args) {
		String initialURL = "https://www.uwo.ca";
		ParallelWebCrawler crawler = new ParallelWebCrawler();
		crawler.crawl(initialURL, 50);

	}

	public void crawl(String initialURL, int numberOfNodes) {
		//List<ParallelNodeLink> nodesToVisit = new ArrayList<ParallelNodeLink>();
		Counter counter = new AtomicCounter(numberOfNodes);

		// Add root to hashmap of existing nodes
		// and list of nodes that need visiting
		root = new ParallelNodeLink(initialURL, counter, existing);
		existing.put(root.getStringUrl(), root);
		try {
			root.get().get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//nodesToVisit.add(root);

		//run(nodesToVisit);
		/*CompletableFuture<Void> completed = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
		try {
			completed.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		System.out.println("Finished!!!");
	}
	
	/*private void run(List<NodeLink> nodesToVisit) {
		NodeLink temp = nodesToVisit.remove(0);

		System.out.println("Running: " + temp.getStringUrl());
		CompletableFuture<List<NodeLink>> link = CompletableFuture.supplyAsync(
				() -> temp.findLinks()
		);
		link.thenAccept(links -> nodesToVisit.addAll(links));
		//futures.add(link);
		//nodesToVisit.addAll(temp.findLinks());
		try {
			link.get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		while (!nodesToVisit.isEmpty()) {
			NodeLink temp2 = nodesToVisit.remove(0);

			System.out.println("Running: " + temp2.getStringUrl());
			link = CompletableFuture.supplyAsync(
					() -> temp2.findLinks()
			);
			link.thenAccept(links -> nodesToVisit.addAll(links));
			//futures.add(link);
			//nodesToVisit.addAll(temp.findLinks());
			//TODO Find out what's going on with timing
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}*/

	public Map<String, INodeLink> getExisting() {
		return existing;
	}

	public ParallelNodeLink getRoot() {
		return root;
	}
}
