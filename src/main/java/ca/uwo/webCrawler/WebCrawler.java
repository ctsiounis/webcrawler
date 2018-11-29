package ca.uwo.webCrawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.uwo.tools.Counter;
import ca.uwo.tools.SimpleCounter;
import ca.uwo.webCrawler.nodes.INodeLink;
import ca.uwo.webCrawler.nodes.NodeLink;

public class WebCrawler {
	Map<String, INodeLink> existing = new HashMap<String, INodeLink>();
	NodeLink root;

	public static void main(String[] args) {
		String initialURL = "https://www.uwo.ca";
		WebCrawler crawler = new WebCrawler();
		crawler.crawl(initialURL, 50);

	}

	public void crawl(String initialURL, int numberOfNodes) {
		List<INodeLink> nodesToVisit = new ArrayList<INodeLink>();
		Counter counter = new SimpleCounter(numberOfNodes);

		// Add root to hashmap of existing nodes
		// and list of nodes that need visiting
		root = new NodeLink(initialURL, counter, existing);
		existing.put(root.getStringUrl(), root);
		nodesToVisit.add(root);

		while (!nodesToVisit.isEmpty()) {
			INodeLink temp = nodesToVisit.remove(0);

			//System.out.println("Running: " + temp.getStringUrl());	
			nodesToVisit.addAll(temp.findLinks());
		}
	}

	public Map<String, INodeLink> getExisting() {
		return existing;
	}

	public NodeLink getRoot() {
		return root;
	}
	

}
