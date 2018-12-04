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
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.uwo.tools.Counter;
import ca.uwo.tools.UrlChecker;

public class ParallelNodeLink implements INodeLink {
	List<String> children = new ArrayList<>();
	List<INodeLink> needToBeExplored = new ArrayList<>();
	String stringUrl;
	URL url;
	Counter counter;
	UrlChecker checker;
	HttpURLConnection urlConnection;

	public ParallelNodeLink(String url, Counter counter, UrlChecker checker) {
		try {
			// TODO Check how it can be done for every node before initializing it
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
			this.checker = checker;
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
		CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
			List<INodeLink> links = findLinks();
			List<CompletableFuture<Void>> futures = new ArrayList<>();
			for (INodeLink link : links) {
				futures.add(link.get());
			}
			CompletableFuture<Void> completed = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
			try {
				completed.get();
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		);
		
		return future;
	}

	public List<INodeLink> findLinks() {
		BufferedReader bis;
		try {
			// Connect and get the response code to check
			urlConnection.setRequestMethod("GET");
			urlConnection.connect();
			int response = urlConnection.getResponseCode();
			// System.out.println(response);

			// Create stream to get data from website
			bis = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

			String s, childLink;
			Pattern linkOnly;
			Matcher linkMatcher;
			ParallelNodeLink child;
			boolean added;

			// TODO Check for 400 response codes

			//System.out.println("Running: " + stringUrl);

			// Check to see if we have a redirection response
			// If we do find the redirection from headers
			if ((response / 3) == 100) {
				//System.out.println("Redirection needed!!!");
				Map<String, List<String>> headers = urlConnection.getHeaderFields();
				childLink = checker.makeAbsolute(headers.get("Location").get(0), stringUrl);

				// If we haven't reached the target number of nodes, add the redirection node
				if (!counter.reachedTarget()) {
					children.add(childLink);
					child = new ParallelNodeLink(childLink, counter, checker);
					if (checker.addNodeLink(childLink, child) == null) {
						//System.out.println("Added node!!");
						needToBeExplored.add(child);
					} else {
						counter.reduce();
					}
					//System.out.println(childLink);
				} else {
					if (checker.find(childLink) != null)
						children.add(childLink);
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
					if (checker.isValid(childLink)) {
						childLink = checker.makeAbsolute(childLink, stringUrl);

						if (!seenBeforeInThisSite(childLink)) {
							if (!counter.reachedTarget()) {
								children.add(childLink);
								child = new ParallelNodeLink(childLink, counter, checker);
								if (checker.addNodeLink(childLink, child) == null) {
									//System.out.println("Added node!!");
									needToBeExplored.add(child);
								} else {
									counter.reduce();
								}
								//System.out.println(childLink);
							} else {
								if (checker.find(childLink) != null)
									children.add(childLink);
							}
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
