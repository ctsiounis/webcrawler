package ca.uwo.webCrawler;

import java.util.Map;

import ca.uwo.tools.Counter;
import ca.uwo.webCrawler.nodes.INodeLink;

public interface IWebCrawler {
	public void crawl(String initialURL, int numberOfNodes);
	public Map<String, INodeLink> getExisting();
	public Counter getCounter();
}
