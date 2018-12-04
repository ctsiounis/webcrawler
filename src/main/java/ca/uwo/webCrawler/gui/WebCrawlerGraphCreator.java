package ca.uwo.webCrawler.gui;

import java.awt.HeadlessException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.mxgraph.analysis.StructuralException;
import com.mxgraph.analysis.mxAnalysisGraph;
import com.mxgraph.analysis.mxGraphGenerator;
import com.mxgraph.analysis.mxTraversal;
import com.mxgraph.costfunction.mxConstCostFunction;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import ca.uwo.webCrawler.nodes.INodeLink;

public class WebCrawlerGraphCreator {
	private static final long serialVersionUID = 1L;
	final mxGraph graph = new mxGraph();
	Object defaultParent = graph.getDefaultParent();
	Map<String, Object> graphNodes = new HashMap<String, Object>();
	final mxGraphComponent graphComponent;
	Map<String, Integer> incoming = new HashMap<String, Integer>();
	Map<String, Integer> outgoing = new HashMap<String, Integer>();
	double avgDistance, diameter;

	public WebCrawlerGraphCreator(Map<String, INodeLink> existing) throws HeadlessException {
		graph.setCellsEditable(false);
		graph.setAllowDanglingEdges(false);

		createVertices(existing);
		createEdges(existing);

		mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
		layout.setInterHierarchySpacing(500);
		layout.setInterRankCellSpacing(200);
		layout.execute(defaultParent);
		graphComponent = new mxGraphComponent(graph);
		
		findAverageDistanceAndDiameter();
	}

	public mxGraphComponent getGraphComponent() {
		return graphComponent;
	}

	private void createVertices(Map<String, INodeLink> existing) {
		graph.getModel().beginUpdate();
		try {
			// Create vertices for every link in the graph
			for (String url : existing.keySet()) {
				Object node = graph.insertVertex(defaultParent, null, url, 20, 20, 130, 45, "whiteSpace=wrap;");
				graphNodes.put(url, node);
			}
		} finally {
			graph.getModel().endUpdate();
		}
	}

	private void createEdges(Map<String, INodeLink> existing) {
		graph.getModel().beginUpdate();
		try {
			for (Map.Entry<String, INodeLink> entry : existing.entrySet()) {
				// Find parent url
				String parent = entry.getKey();

				for (String child : entry.getValue().getChildren()) {
					// Create graph edges
					Object parentNode = graphNodes.get(parent);
					Object childNode = graphNodes.get(child);
					graph.insertEdge(defaultParent, null, null, parentNode, childNode);

					// Count outgoing
					outgoing.put(parent, outgoing.getOrDefault(parent, 0) + 1);

					// Count incoming
					incoming.put(child, incoming.getOrDefault(child, 0) + 1);
				}
			}
		} finally {
			graph.getModel().endUpdate();
		}
	}

	public int getAverageIncoming() {
		int sum = 0;
		int count = 0;

		for (Map.Entry<String, Integer> entry : incoming.entrySet()) {
			sum += entry.getValue();
			count++;
		}

		return sum / count;
	}

	public int getAverageOutgoing() {
		int sum = 0;
		int count = 0;

		for (Map.Entry<String, Integer> entry : outgoing.entrySet()) {
			sum += entry.getValue();
			count++;
		}

		return sum / count;
	}

	public double getAvgDistance() {
		return avgDistance;
	}

	public double getDiameter() {
		return diameter;
	}

	private void findAverageDistanceAndDiameter() {
		mxAnalysisGraph aGraph = new mxAnalysisGraph();
		aGraph.setGraph(graph);
		mxGraphGenerator generator = new mxGraphGenerator(
				mxGraphGenerator.getGeneratorFunction(graph, false, 0, 10),
				new mxConstCostFunction(1)
		);
		aGraph.setGenerator(generator);
		
		double max = 0.0;
		double sum = 0.0;
		int count = 0;
		
		try {
			ArrayList<Object[][]> frw = mxTraversal.floydRoyWarshall(aGraph);
			Object[][] distanceMap = frw.get(0);
			for (int i = 0; i < distanceMap.length; i++) {
				for (int j = 0; j < distanceMap[i].length; j++) {
					double value = Double.parseDouble(distanceMap[i][j].toString());
					if(value != 0.0) {
						sum+=value;
						count++;
						if (value > max)
							max = value;
					}
				}
			}
			
			avgDistance = sum/count;
			diameter = max;
		} catch (StructuralException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
