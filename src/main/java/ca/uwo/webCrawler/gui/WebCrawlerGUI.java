package ca.uwo.webCrawler.gui;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import ca.uwo.webCrawler.WebCrawler;
import ca.uwo.webCrawler.nodes.NodeLink;

public class WebCrawlerGUI extends JFrame implements ActionListener {
	JTextField website;
	JPanel graphPanel;
	

	public static void main(String[] args) {
		//WebCrawler crawler = new WebCrawler();
		//crawler.run("http://www.csd.uwo.ca/faculty/solis");
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
		
		JLabel websiteLabel = new JLabel("Insert website to start crawling: ");
		website = new JTextField(25);
		JButton runCrawl = new JButton("Run");
		runCrawl.addActionListener(this);
		
		controlPanel.add(websiteLabel);
		controlPanel.add(website);
		controlPanel.add(runCrawl);
		
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		getContentPane().add(controlPanel, BorderLayout.PAGE_START);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		WebCrawler crawler = new WebCrawler();
		crawler.run(website.getText());
		WebCrawlerGraphCreator graphCreator = new WebCrawlerGraphCreator(crawler.getRoot(), crawler.getExisting());
		graphPanel.add(graphCreator.getGraphComponent());
		revalidate();
		repaint();
	}

}
