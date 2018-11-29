package ca.uwo.webCrawler.gui;

import java.awt.HeadlessException;
import java.util.HashMap;
import java.util.Map;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import ca.uwo.webCrawler.nodes.INodeLink;
import ca.uwo.webCrawler.nodes.NodeLink;

public class WebCrawlerGraphCreator {
	private static final long serialVersionUID = 1L;
	final mxGraph graph = new mxGraph();
	Object parent = graph.getDefaultParent();
	Map<String, Object> graphNodes = new HashMap<String, Object>();
	final mxGraphComponent graphComponent;

	public WebCrawlerGraphCreator(Map<String, INodeLink> existing) throws HeadlessException {
		graph.setCellsEditable(false);
		graph.setAllowDanglingEdges(false);

		createVertices(existing);
		createEdges(existing);

		mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
		layout.setInterHierarchySpacing(500);
		layout.setInterRankCellSpacing(200);
		layout.execute(parent);
		graphComponent = new mxGraphComponent(graph);
	}
	
	public mxGraphComponent getGraphComponent() {
		return graphComponent;
	}

	private void createVertices(Map<String, INodeLink> existing) {
		graph.getModel().beginUpdate();
		try {
			// Create vertices for every link in the graph
			for (String url : existing.keySet()) {
				Object node = graph.insertVertex(parent, null, url, 20, 20, 130, 45, "whiteSpace=wrap;");
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
				for (String child : entry.getValue().getChildren()) {
					Object parentNode = graphNodes.get(entry.getKey());
					Object childNode = graphNodes.get(child);
					graph.insertEdge(parent, null, null, parentNode, childNode);
				}
			}
		} finally {
			graph.getModel().endUpdate();
		}
	}
}
