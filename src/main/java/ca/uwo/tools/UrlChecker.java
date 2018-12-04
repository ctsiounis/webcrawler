package ca.uwo.tools;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ca.uwo.webCrawler.nodes.INodeLink;

public class UrlChecker {
	Map<String, INodeLink> existing;
	
	public UrlChecker () {
		existing = new ConcurrentHashMap<String, INodeLink>();
	}
	
	public INodeLink addNodeLink(String childLink, INodeLink child) {
		return existing.putIfAbsent(childLink, child);
	}
	
	public INodeLink find(String link) {
		return existing.get(link);
	}

	public boolean isValid(String link) {
		// Check to see if its a website
		if (link.matches("javascript:.*|mailto:.*")) {
			return false;
		}
		return true;
	}

	public String makeAbsolute(String link, String url) {
		// Remove position links within the same page
		int hashTagIndex = link.indexOf("#");
		if (hashTagIndex != -1)
			link = link.substring(0, hashTagIndex);

		String absoluteLink;
		if (link.matches("http://.*") || link.matches("https://.*")) {
			absoluteLink = link;
		} else if (link.matches("/.*") && url.matches(".*$[^/]")) {
			absoluteLink = url + "/" + link;
		} else if (link.matches("[^/].*") && url.matches(".*[^/]")) {
			absoluteLink = url + "/" + link;
		} else if (link.matches("/.*") && url.matches(".*[/]")) {
			absoluteLink = url + link;
		} else if (link.matches("/.*") && url.matches(".*[^/]")) {
			absoluteLink = url + link;
		} else if ("".equals(link)) {
			absoluteLink = url;
		} else {

			throw new RuntimeException("Cannot make the link absolute. Url: " + url + ", Link: " + link);
		}

		// Remove final "/" for uniformity
		if (absoluteLink.matches(".+/"))
			absoluteLink = absoluteLink.substring(0, absoluteLink.length() - 1);

		return absoluteLink;
	}

	public Map<String, INodeLink> getExisting() {
		return existing;
	}
	
}
