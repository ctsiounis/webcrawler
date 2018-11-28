package ca.uwo.parallelWebCrawler;

import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

public class ParallelWebCrawler {

	public static void main(String[] args) {
		URL u;
		BufferedReader bis;
		String s;

		try {

			/*
			 * URL that the web crawler will download. You might change this URL to download
			 * other pages
			 */
			u = new URL("http://www.csd.uwo.ca/faculty/solis");

			bis = new BufferedReader(new InputStreamReader(u.openStream()));

			while ((s = bis.readLine()) != null) {
				//Pattern htmltag = Pattern.compile("<a\\b[^>]*href=\"h([^>]*?)>");
				Pattern linkOnly = Pattern.compile("(?<=<a\\b[^>]{0,30}href=\")http([^>]*?)(?=\">)");
				//Matcher matcher = htmltag.matcher(s);
				Matcher linkMatcher = linkOnly.matcher(s);
				//if (matcher.find())
				//	System.out.println(matcher.group());
				if (linkMatcher.find())
					System.out.println(linkMatcher.group());
			}

			bis.close();

		} catch (MalformedURLException mue) {
			System.out.println("Malformed URL");

		} catch (IOException ioe) {
			System.out.println("IOException:" + ioe.getMessage());
		}
	}

}
