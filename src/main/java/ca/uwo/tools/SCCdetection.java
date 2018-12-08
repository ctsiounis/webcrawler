package ca.uwo.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.List;

import ca.uwo.parallelWebCrawler.ParallelWebCrawler;
import ca.uwo.webCrawler.nodes.INodeLink;

public class SCCdetection { 
    private Map<String,List<String>> adj; //Adjacency List 
    private Map<String,List<String>> transpose; //Transpose Adjacency List 
    Map<String, INodeLink> nodes;
    int numPerComp;
    List<Integer> stronglyConnComp = new ArrayList<>();
  
    //Constructor 
    public SCCdetection (Map<String, INodeLink> nodes) 
    { 
        this.nodes = nodes; 
        adj = new HashMap<String, List<String>>(); 
        transpose = new HashMap<String, List<String>>(); 
        for (Map.Entry<String, INodeLink> node : nodes.entrySet()) {
        	adj.put(node.getKey(), node.getValue().getChildren());
        }
    } 
  
    // A recursive function to print DFS starting from v 
    void DFSUtil(String v, Map<String, Boolean> visited, Map<String,List<String>> matrix) 
    { 
        // Mark the current node as visited and print it 
        visited.put(v, true); 
        numPerComp++;
        //System.out.print(v + " "); 
        
        // Recur for all the vertices adjacent to this vertex
        for (String adjNode : matrix.get(v)) {
        	if (!visited.get(adjNode))
        		DFSUtil(adjNode, visited, matrix);
        }
    } 
  
    // Function that returns reverse (or transpose) of this graph 
    void createTranspose() {
    	for (String node : adj.keySet()) {
    		for (String adjNode : adj.get(node)) {
    			transpose.putIfAbsent(adjNode, new ArrayList<>());
    			transpose.get(adjNode).add(node);
    		}
    	} 
    } 
  
    void fillOrder(String v, Map<String, Boolean> visited, Stack<String> stack) 
    { 
        // Mark the current node as visited and print it 
    	visited.put(v, true);
  
        // Recur for all the vertices adjacent to this vertex 
    	for (String adjNode : adj.get(v)) {
        	if (!visited.get(adjNode))
        		fillOrder(adjNode, visited, stack);
        }
  
        // All vertices reachable from v are processed by now, 
        // push v to Stack 
        stack.push(v); 
    } 
  
    // The main function that finds and prints all strongly 
    public void findSCCs() 
    { 
        Stack<String> stack = new Stack<>(); 
  
        // Mark all the vertices as not visited (For first DFS) 
        Map<String, Boolean> visited = new HashMap<>(); 
        for (Entry<String, List<String>> node : adj.entrySet()) {
            visited.put(node.getKey(), false); 
        }
  
        // Fill vertices in stack according to their finishing 
        // times 
        for (Map.Entry<String, Boolean> node : visited.entrySet()) {
        	if (!node.getValue())
        		fillOrder(node.getKey(), visited, stack);
        }
  
        // Create a reversed graph 
        createTranspose(); 
  
        // Mark all the vertices as not visited (For second DFS) 
        for (Entry<String, List<String>> node : adj.entrySet()) {
            visited.put(node.getKey(), false); 
        } 
  
        // Now process all vertices in order defined by Stack 
        while (!stack.empty()) 
        { 
            // Pop a vertex from stack 
            String v = stack.pop(); 
  
            // Print Strongly connected component of the popped vertex 
            if (!visited.get(v)) {
            	numPerComp = 0;
                DFSUtil(v, visited, transpose); 
                if (numPerComp > 1)
                	stronglyConnComp.add(numPerComp);
            } 
        }
    }
    
    public List<Integer> getSCC() {
    	return stronglyConnComp;
    }
    
    public static void main(String[] args) {
		String initialURL = "http://www.uwo.ca";
		ParallelWebCrawler crawler = new ParallelWebCrawler();
		crawler.crawl(initialURL, 100);
		
		SCCdetection detection = new SCCdetection(crawler.getExisting());
		detection.findSCCs();
		for (Integer i: detection.getSCC()) {
			System.out.println(i);
		}

	}
}