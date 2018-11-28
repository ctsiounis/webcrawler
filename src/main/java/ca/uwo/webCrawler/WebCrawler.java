package ca.uwo.webCrawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.uwo.tools.Counter;
import ca.uwo.tools.SimpleCounter;
import ca.uwo.webCrawler.nodes.NodeLink;

public class WebCrawler {
	Map<String, NodeLink> existing = new HashMap<String, NodeLink>();
	NodeLink root;

	public static void main(String[] args) {
		String initialURL = "http://www.lib.uwo.ca";
		WebCrawler crawler = new WebCrawler();
		crawler.run(initialURL);

	}

	public void run(String initialURL) {
		List<NodeLink> nodesToVisit = new ArrayList<NodeLink>();
		Counter counter = new SimpleCounter(20);

		// Add root to hashmap of existing nodes
		// and list of nodes that need visiting
		root = new NodeLink(initialURL, counter, existing);
		existing.put(root.getStringUrl(), root);
		nodesToVisit.add(root);

		while (!nodesToVisit.isEmpty()) {
			NodeLink temp = nodesToVisit.remove(0);

			System.out.println("Running: " + temp.getStringUrl());
			temp.findLinks();
			nodesToVisit.addAll(temp.getNeedToBeExplored());
		}
	}

	public Map<String, NodeLink> getExisting() {
		return existing;
	}

	public NodeLink getRoot() {
		return root;
	}
	

}
