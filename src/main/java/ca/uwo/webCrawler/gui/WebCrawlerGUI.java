package ca.uwo.webCrawler.gui;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.NumberFormat;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.NumberFormatter;

import ca.uwo.parallelWebCrawler.ParallelWebCrawler;
import ca.uwo.tools.Counter;
import ca.uwo.webCrawler.WebCrawler;
import ca.uwo.webCrawler.nodes.INodeLink;

public class WebCrawlerGUI extends JFrame implements MouseListener, KeyListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JTextField website;
	JFormattedTextField numberOfNodes;
	JRadioButton simpleCrawler, parallelCrawler;
	JPanel graphPanel;
	JLabel status;
	JTextArea stats;
	JCheckBox graphVisual;
	

	public static void main(String[] args) {
		WebCrawlerGUI frame = new WebCrawlerGUI("Web Crawler");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setExtendedState(MAXIMIZED_BOTH);
		frame.setVisible(true);
	}

	public WebCrawlerGUI(String title) throws HeadlessException {
		super(title);
		
		getContentPane().setLayout(new BorderLayout());
		
		// Graph Panel
		graphPanel = new JPanel();
		JScrollPane graphScrollPane = new JScrollPane(graphPanel);
		
		// Control Panel
		JPanel controlPanel = new JPanel();
		
		JLabel nodesLabel = new JLabel("Insert number of nodes to check: ");
		NumberFormat format = NumberFormat.getIntegerInstance();
		format.setGroupingUsed(false);
	    NumberFormatter formatter = new NumberFormatter(format);
	    formatter.setValueClass(Integer.class);
	    formatter.setMinimum(0);
	    formatter.setMaximum(Integer.MAX_VALUE);
	    formatter.setAllowsInvalid(false);
	    formatter.setCommitsOnValidEdit(false);
	    numberOfNodes = new JFormattedTextField(formatter);
	    numberOfNodes.setColumns(5);
	    
	    JLabel chooseCrawlerLabel = new JLabel("Choose type of crawler: ");
	    simpleCrawler = new JRadioButton("Simple Crawler", true);
	    parallelCrawler = new JRadioButton("ParallelCrawler", false);
	    ButtonGroup crawlerGroup = new ButtonGroup();
	    crawlerGroup.add(simpleCrawler);
	    crawlerGroup.add(parallelCrawler);
	    
		JLabel websiteLabel = new JLabel("Insert website to start crawling: ");
		website = new JTextField(20);
		website.addKeyListener(this);
		JButton runCrawl = new JButton("Run");
		runCrawl.addMouseListener(this);
		runCrawl.addKeyListener(this);
		
		status = new JLabel("Waiting");
		
		controlPanel.setLayout(new BoxLayout(controlPanel,
                BoxLayout.LINE_AXIS));
		controlPanel.add(nodesLabel);
		controlPanel.add(numberOfNodes);
		controlPanel.add(Box.createHorizontalStrut(5));
		controlPanel.add(new JSeparator(SwingConstants.VERTICAL));
		controlPanel.add(Box.createHorizontalStrut(5));
		controlPanel.add(chooseCrawlerLabel);
		controlPanel.add(simpleCrawler);
		controlPanel.add(parallelCrawler);
		controlPanel.add(Box.createHorizontalStrut(5));
		controlPanel.add(new JSeparator(SwingConstants.VERTICAL));
		controlPanel.add(Box.createHorizontalStrut(5));
		controlPanel.add(websiteLabel);
		controlPanel.add(website);
		controlPanel.add(runCrawl);
		controlPanel.add(status);
		
		//Stats Panel
		JPanel statsPanel = new JPanel();
		JScrollPane statsScrollPanel= new JScrollPane(statsPanel);
		
		stats = new JTextArea(100, 28);
		stats.setEditable(false);
		statsPanel.add(stats);
		
		JPanel optionsPanel = new JPanel();
		graphVisual = new JCheckBox("Visualize web graph");
		optionsPanel.add(graphVisual);
		
		getContentPane().add(graphScrollPane, BorderLayout.CENTER);
		getContentPane().add(controlPanel, BorderLayout.PAGE_START);
		getContentPane().add(statsScrollPanel, BorderLayout.EAST);
		getContentPane().add(optionsPanel, BorderLayout.SOUTH);
	}

	private void pressed(){
		status.setText("Running...");
	}
	
	private void released() {
		String websiteURL = website.getText();
		int nodesToExplore = Integer.parseInt(numberOfNodes.getText());
		stats.append("***********************************************************\n");
		stats.append("Website: " + websiteURL + "(" + nodesToExplore + " nodes)");
		//INodeLink root = null;
		Map<String, INodeLink> nodes = null;
		Counter counter = null;
		
		if (simpleCrawler.isSelected()) {
			stats.append(" - Simple Web Crawler\n");
			stats.append("***********************************************************\n");
			long startTime = System.nanoTime();
			
			WebCrawler crawler = new WebCrawler();
			crawler.crawl(websiteURL, nodesToExplore);
			//root = crawler.getRoot();
			nodes = crawler.getExisting();
			counter = crawler.getCounter();
			
			long endTime = System.nanoTime();
			long duration = (endTime - startTime);
			stats.append("Running time: " + duration + "\n");
			//System.out.println("Simple Crawler time: " + duration);
		} else if (parallelCrawler.isSelected()) {
			stats.append(" - Parallel Web Crawler\n");
			stats.append("***********************************************************\n");
			long startTime = System.nanoTime();
			
			ParallelWebCrawler crawler = new ParallelWebCrawler();
			crawler.crawl(websiteURL, nodesToExplore);
			//root = crawler.getRoot();
			nodes = crawler.getExisting();
			counter = crawler.getCounter();
			
			long endTime = System.nanoTime();
			long duration = (endTime - startTime);
			stats.append("Running time: " + duration + "\n");
			//System.out.println("Parallel Crawler time: " + duration);
		}
		
		boolean visualize = graphVisual.isSelected();
		WebCrawlerGraphCreator graphCreator = new WebCrawlerGraphCreator(nodes, visualize);
		
		graphPanel.removeAll();
		if (visualize)
			graphPanel.add(graphCreator.getGraphComponent());
		
		int avgIncoming = graphCreator.getAverageIncoming();
		int avgOutgoing = graphCreator.getAverageOutgoing();
		double avgDistance = graphCreator.getAvgDistance();
		double diameter = graphCreator.getDiameter();
		//stats.setText("");
		stats.append("Average number of incoming edges: " + avgIncoming + "\n");
		stats.append("Average number of outgoing edges: " + avgOutgoing + "\n");
		stats.append("Average distance between vertices: " + String.format("%.2f", avgDistance) + "\n");
		stats.append("Graph's diameter: " + String.format("%.2f", diameter) + "\n");
		stats.append("Distribution of responses:\n");
		stats.append("  100(Informational):\t" + counter.get100() + "\n");
		stats.append("  200(Successful):\t" + counter.get200() + "\n");
		stats.append("  300(Redirection):\t" + counter.get300() + "\n");
		stats.append("  400(Client Error):\t" + counter.get400() + "\n");
		stats.append("  500(Server Error):\t" + counter.get500() + "\n");
		stats.append("--------------------------------------------------------------------------\n");
		revalidate();
		repaint();
		
		status.setText("Done!");
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		pressed();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		released();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ENTER){
            pressed();
        }
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ENTER){
            released();
        }
	}

}
