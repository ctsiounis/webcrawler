package ca.uwo.webCrawler.nodes;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface INodeLink {
	public List<String> getChildren();
	public String getStringUrl();
	public CompletableFuture<Void> get();
	public List<INodeLink> findLinks();
}
