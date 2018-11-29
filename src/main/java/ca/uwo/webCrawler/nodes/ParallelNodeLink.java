package ca.uwo.webCrawler.nodes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.uwo.tools.Counter;

public class ParallelNodeLink implements INodeLink{
	List<String> children = new ArrayList<>();
	List<INodeLink> needToBeExplored = new ArrayList<>();
	String stringUrl;
	URL url;
	Counter counter;
	Map<String, INodeLink> existing;
	HttpURLConnection urlConnection;

	public ParallelNodeLink(String url, Counter counter, Map<String, INodeLink> existing) {
		try {
			//TODO Check how it can be done for every node before initializing it
			if (!url.startsWith("http")) {
				if (!url.startsWith("www")) {
					url = "www." + url;
				}
				url = "http://" + url;
			}
			stringUrl = url;
			this.url = new URL(url);
			urlConnection = (HttpURLConnection) this.url.openConnection();
			this.counter = counter;
			this.existing = existing;
		} catch (Exception e) {
			// System.out.println("Failed to access URL link" + e.getMessage());
			e.printStackTrace();
		}
	}

	public List<String> getChildren() {
		return children;
	}

	public List<INodeLink> getNeedToBeExplored() {
		return needToBeExplored;
	}

	public String getStringUrl() {
		return stringUrl;
	}

	public URL getUrl() {
		return url;
	}
	
	public CompletableFuture<Void> get() {
		List<INodeLink> links = findLinks();
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		
		for (INodeLink link : links) {
			futures.add(link.get());
		}
		return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
	}

	public List<INodeLink> findLinks() {
		BufferedReader bis;
		try {
			// Connect and get the response code to check
			urlConnection.setRequestMethod("GET");
			urlConnection.connect();
			int response = urlConnection.getResponseCode();
			//System.out.println(response);

			// Create stream to get data from website
			bis = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

			String s, childLink;
			Pattern linkOnly;
			Matcher linkMatcher;
			ParallelNodeLink child;

			// TODO Check for 400 response codes

			// Check to see if we have a redirection response
			// If we do find the redirection from headers
			if ((response / 3) == 100) {
				//System.out.println("Redirection needed!!!");
				Map<String, List<String>> headers = urlConnection.getHeaderFields();
				childLink = makeAbsolute(headers.get("Location").get(0));

				// If we haven't reached the target number of nodes, add the redirection node
				if (!counter.reachedTarget()) {
					child = new ParallelNodeLink(childLink, counter, existing);
					children.add(childLink);
					needToBeExplored.add(child);
					existing.put(childLink, child);
					//System.out.println(childLink);
				}
				return needToBeExplored;
			} else { // If not create regular expression to find links
				linkOnly = Pattern.compile("(?<=<a\\b[^>]{0,30}href=\")([^>\\s]*?)(?=\".+>)");
			}

			// Start reading from the website
			while ((s = bis.readLine()) != null) {
				// System.out.println(s);

				// Find references to other websites
				linkMatcher = linkOnly.matcher(s);
				if (linkMatcher.find()) {
					childLink = linkMatcher.group();

					// If it actually is a website
					if (isValid(childLink)) {
						childLink = makeAbsolute(childLink);
						// Check to see if there's already a node for the link
						INodeLink node = findNodeLink(childLink);
						if (node != null) {
							if (!seenBeforeInThisSite(childLink)) {
								children.add(childLink);
								//System.out.println(childLink);
								continue;
							}
						}
						// If we haven't seen this link in the site before
						if (!seenBeforeInThisSite(childLink)) {
							// If we have reached our target amount of nodes, stop
							if (counter.reachedTarget()) {
								break;
							}
							// Otherwise, add new node for link
							child = new ParallelNodeLink(childLink, counter, existing);
							children.add(childLink);
							needToBeExplored.add(child);
							existing.put(childLink, child);
							//System.out.println(childLink);
						}

					}
				}
			}

			bis.close();
		} catch (IOException e) {
			// System.out.println("Error when reading from link " + link.toString() + ":" +
			// e.getMessage());
			e.printStackTrace();
		}
		return needToBeExplored;
	}

	private INodeLink findNodeLink(String childLink) {
		return existing.get(childLink);
	}

	private boolean isValid(String link) {
		// Check to see if its a website
		if (link.matches("javascript:.*|mailto:.*")) {
			return false;
		}
		return true;
	}

	private String makeAbsolute(String link) {
		// Remove position links within the same page
		int hashTagIndex = link.indexOf("#");
		if (hashTagIndex != -1)
			link = link.substring(0, hashTagIndex);

		String absoluteLink;
		if (link.matches("http://.*") || link.matches("https://.*")) {
			absoluteLink = link;
		} else if (link.matches("/.*") && stringUrl.matches(".*$[^/]")) {
			absoluteLink = url + "/" + link;
		} else if (link.matches("[^/].*") && stringUrl.matches(".*[^/]")) {
			absoluteLink = url + "/" + link;
		} else if (link.matches("/.*") && stringUrl.matches(".*[/]")) {
			absoluteLink = url + link;
		} else if (link.matches("/.*") && stringUrl.matches(".*[^/]")) {
			absoluteLink = url + link;
		} else if ("".equals(link)) {
			absoluteLink = stringUrl;
		} else {

			throw new RuntimeException("Cannot make the link absolute. Url: " + stringUrl + " Link " + link);
		}

		// Remove final "/" for uniformity
		if (absoluteLink.matches(".+/"))
			absoluteLink = absoluteLink.substring(0, absoluteLink.length() - 1);

		return absoluteLink;
	}

	private boolean seenBeforeInThisSite(String urlToCheck) {
		// Check to see if it's the same as current website
		if (urlToCheck.equals(stringUrl))
			return true;

		// Check to see if it's been explored before
		for (String child : children) {
			if (urlToCheck.equals(child))
				return true;
		}
		return false;
	}
}
