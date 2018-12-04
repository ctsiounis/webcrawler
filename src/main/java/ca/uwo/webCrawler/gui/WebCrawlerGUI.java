package ca.uwo.webCrawler.gui;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import com.sun.xml.internal.ws.util.NoCloseOutputStream;

import ca.uwo.parallelWebCrawler.ParallelWebCrawler;
import ca.uwo.tools.UrlChecker;
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
		NumberFormat format = NumberFormat.getInstance();
	    NumberFormatter formatter = new NumberFormatter(format);
	    formatter.setValueClass(Integer.class);
	    formatter.setMinimum(0);
	    formatter.setMaximum(Integer.MAX_VALUE);
	    formatter.setAllowsInvalid(false);
	    formatter.setCommitsOnValidEdit(true);
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
		
		stats = new JTextArea(100, 25);
		stats.setEditable(false);
		statsPanel.add(stats);
		
		getContentPane().add(graphScrollPane, BorderLayout.CENTER);
		getContentPane().add(controlPanel, BorderLayout.PAGE_START);
		getContentPane().add(statsScrollPanel, BorderLayout.EAST);
	}

	private void pressed(){
		status.setText("Running...");
	}
	
	private void released() {
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
		
		int avgIncoming = graphCreator.getAverageIncoming();
		int avgOutgoing = graphCreator.getAverageOutgoing();
		double avgDistance = graphCreator.getAvgDistance();
		double diameter = graphCreator.getDiameter();
		stats.setText("");
		stats.append("Average number of incoming edges: " + avgIncoming + "\n");
		stats.append("Average number of outgoing edges: " + avgOutgoing + "\n");
		stats.append("Average distance between vertices: " + String.format("%.2f", avgDistance) + "\n");
		stats.append("Graph's diameter: " + String.format("%.2f", diameter) + "\n");

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
