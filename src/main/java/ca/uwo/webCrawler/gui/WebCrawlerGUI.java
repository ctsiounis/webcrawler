package ca.uwo.webCrawler.gui;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.NumberFormatter;

import ca.uwo.parallelWebCrawler.ParallelWebCrawler;
import ca.uwo.webCrawler.WebCrawler;
import ca.uwo.webCrawler.nodes.INodeLink;

public class WebCrawlerGUI extends JFrame implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JTextField website;
	JFormattedTextField numberOfNodes;
	JRadioButton simpleCrawler, parallelCrawler;
	JPanel graphPanel;
	

	public static void main(String[] args) {
		WebCrawlerGUI frame = new WebCrawlerGUI("Web Crawler");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setExtendedState(MAXIMIZED_BOTH);
		frame.setVisible(true);
	}

	public WebCrawlerGUI(String title) throws HeadlessException {
		super(title);
		
		getContentPane().setLayout(new BorderLayout());
		
		graphPanel = new JPanel();
		JScrollPane scrollPane = new JScrollPane(graphPanel);
		JPanel controlPanel = new JPanel();
		
		JLabel nodesLabel = new JLabel("Insert number of nodes to check: ");
		NumberFormat format = NumberFormat.getInstance();
	    NumberFormatter formatter = new NumberFormatter(format);
	    formatter.setValueClass(Integer.class);
	    formatter.setMinimum(0);
	    formatter.setMaximum(Integer.MAX_VALUE);
	    formatter.setAllowsInvalid(false);
	    formatter.setCommitsOnValidEdit(true);
	    numberOfNodes = new JFormattedTextField(formatter);
	    numberOfNodes.setColumns(10);
	    
	    JLabel chooseCrawlerLabel = new JLabel("Choose type of crawler");
	    simpleCrawler = new JRadioButton("Simple Crawler", true);
	    parallelCrawler = new JRadioButton("ParallelCrawler", false);
	    ButtonGroup crawlerGroup = new ButtonGroup();
	    crawlerGroup.add(simpleCrawler);
	    crawlerGroup.add(parallelCrawler);
	    
		JLabel websiteLabel = new JLabel("Insert website to start crawling: ");
		website = new JTextField(25);
		JButton runCrawl = new JButton("Run");
		runCrawl.addActionListener(this);
		
		controlPanel.add(nodesLabel);
		controlPanel.add(numberOfNodes);
		controlPanel.add(chooseCrawlerLabel);
		controlPanel.add(simpleCrawler);
		controlPanel.add(parallelCrawler);
		controlPanel.add(websiteLabel);
		controlPanel.add(website);
		controlPanel.add(runCrawl);
		
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		getContentPane().add(controlPanel, BorderLayout.PAGE_START);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		String websiteURL = website.getText();
		int nodesToExplore = Integer.parseInt(numberOfNodes.getText());
		
		//INodeLink root = null;
		Map<String, INodeLink> nodes = null;
		if (simpleCrawler.isSelected()) {
			long startTime = System.nanoTime();
			
			WebCrawler crawler = new WebCrawler();
			crawler.crawl(websiteURL, nodesToExplore);
			//root = crawler.getRoot();
			nodes = crawler.getExisting();
			
			long endTime = System.nanoTime();
			long duration = (endTime - startTime);
			System.out.println("Simple Crawler time: " + duration);
		} else if (parallelCrawler.isSelected()) {
			long startTime = System.nanoTime();
			
			ParallelWebCrawler crawler = new ParallelWebCrawler();
			crawler.crawl(websiteURL, nodesToExplore);
			//root = crawler.getRoot();
			nodes = crawler.getExisting();
			
			long endTime = System.nanoTime();
			long duration = (endTime - startTime);
			System.out.println("Parallel Crawler time: " + duration);
		}
		WebCrawlerGraphCreator graphCreator = new WebCrawlerGraphCreator(nodes);
		graphPanel.removeAll();
		graphPanel.add(graphCreator.getGraphComponent());
		revalidate();
		repaint();
	}

}
