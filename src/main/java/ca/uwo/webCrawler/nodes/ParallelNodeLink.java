package ca.uwo.webCrawler.nodes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import javax.net.ssl.SSLHandshakeException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
	ExecutorService executor;
	

	public ParallelNodeLink(String url, Counter counter, UrlChecker checker, ExecutorService executor) {
		this.executor = executor;
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
		}, executor
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
			//System.out.println(response);

			// Create stream to get data from website
			bis = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

			String s, childLink;
			ParallelNodeLink child;

			// Check for response code and log
			if ((response / 100) == 1) {
				counter.increase100();
			} else if ((response / 100) == 2) {
				counter.increase200();
			} else if ((response / 100) == 3) {
				counter.increase300();
			} else if ((response / 100) == 4) {
				counter.increase400();
				return needToBeExplored;
			} else {
				counter.increase500();
				return needToBeExplored;
			}

			//System.out.println("Running: " + stringUrl);

			// Check to see if we have a redirection response
			// If we do find the redirection from headers
			if ((response / 100) == 3) {
				//System.out.println("Redirection needed!!!");
				Map<String, List<String>> headers = urlConnection.getHeaderFields();
				childLink = checker.makeAbsolute(headers.get("Location").get(0), stringUrl);
				//childLink = response2.parse().absUrl("Location");
				// If we haven't reached the target number of nodes, add the redirection node
				if (!counter.reachedTarget()) {
					children.add(childLink);
					child = new ParallelNodeLink(childLink, counter, checker, executor);
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
			}
			
			StringBuilder page = new StringBuilder();
			// Read the website
			while ((s = bis.readLine()) != null) {
				// System.out.println(s);
				page.append(s+"\n");
			}
			
			Document doc = Jsoup.parse(page.toString(), stringUrl);
			// Search for links to other websites
			Elements links = doc.select("a");
			for (Element link : links) {
			    childLink = link.absUrl("href");
			    
			    // If it actually is a website
				if (checker.isValid(childLink)) {
					//childLink = checker.makeAbsolute(childLink, stringUrl);

					if (!seenBeforeInThisSite(childLink)) {
						if (!counter.reachedTarget()) {
							children.add(childLink);
							child = new ParallelNodeLink(childLink, counter, checker, executor);
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

			bis.close();
		} catch (FileNotFoundException e) {
			// System.out.println("Error when reading from link " + link.toString() + ":" +
			// e.getMessage());
			e.printStackTrace();
			counter.increase400();
		} catch (SSLHandshakeException e) {
			// System.out.println("Error when reading from link " + link.toString() + ":" +
			// e.getMessage());
			e.printStackTrace();
			counter.increase400();
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
