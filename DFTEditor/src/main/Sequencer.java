package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

public class Sequencer extends JPanel {

	private static final long serialVersionUID = 6252327634736973395L;

	MultiWindow parent;
	SequencerView view;
	SequencerController controller;
	ArrayList<ModuleEditor> moduleEditors = null;
	private JToolBar navigationBar = null;
	public static final int columnWidth = 150;
	public static final int scrollableWidth = columnWidth * 16;
	public static final int scrollableHeight = columnWidth * 8;
	

	public void addNavigationButton(String buttonText) {
		JButton button = new JButton(buttonText);
		button.addActionListener((ActionListener) controller);
		navigationBar.add(button);
	}
	
	public JToolBar createNavigationBar() {
		navigationBar = new JToolBar("Navigation Bar");
        // Create Navigation Buttons
        addNavigationButton("Play");
        addNavigationButton("DFT");
        addNavigationButton("Load");
        addNavigationButton("Save");
    	return navigationBar;
	}

	
    public Sequencer(MultiWindow parent) {
		super(new BorderLayout());
		this.parent = parent;
        view = new SequencerView(this);
        view.setBackground(Color.black);
        controller = new SequencerController(this);
        add(createNavigationBar(), BorderLayout.PAGE_START);
        view.addMouseListener(controller);
        view.addMouseMotionListener(controller);
        view.setPreferredSize(new Dimension(scrollableWidth, scrollableHeight));
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setSize(800, 600);
        add(scrollPane, BorderLayout.CENTER);
    }

}
