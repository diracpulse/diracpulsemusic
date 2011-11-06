
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.io.*;

public class FDEditor extends JFrame {
	
	/**
	 * 
	 */

	public static MultiWindow parent;
	public static FDView view;
	public static FDController controller;
	public static FDActionHandler actionHandler;
	public static JToolBar navigationBar;
	public static TreeMap  timeToFreqToData;
	
	public static int segmentWidth = 6;
	public static int segmentHeight = 9;
	public static int leftFreqSegments = 8; // used by drawFreqScale
	public static int upperTimeSegments = 7; // used by drawTimeScale
	public static int xDataStart = segmentWidth * leftFreqSegments; // start of first data cell
	public static int yDataStart = segmentHeight * upperTimeSegments; // start of first data cell
	public static int leftX = 0; // index of freq in leftmost data cell
	public static int upperY = 0; // index of time in uppermost data cell
	public static int timeStepInMillis = 5; // time in ms = time * timeStepInMillis
	
	public JMenuBar createMenuBar() {
        FDActionHandler actionHandler = new FDActionHandler(this);
        return actionHandler.createMenuBar();
    }

	public JToolBar createNavigationBar() {
		navigationBar = new JToolBar("Navigation Bar");
        // Create Navigation Buttons
        addNavigationButton("F+31");
        addNavigationButton("F-31");
        addNavigationButton("F+6");
        addNavigationButton("F-6");
        addNavigationButton("+250ms");
        addNavigationButton("-250ms");
        addNavigationButton("+500ms");
        addNavigationButton("-500ms");
        addNavigationButton("+1s");
        addNavigationButton("-1s");       
        addNavigationButton("+2s");
        addNavigationButton("-2s");     
        addNavigationButton("+5s");
        addNavigationButton("-5s");
        addNavigationButton("+10s");
        addNavigationButton("-10s");      
        addNavigationButton("+30s");
        addNavigationButton("-30s");        
    	addNavigationButton("+1min");
    	addNavigationButton("-1min");
    	return navigationBar;
	}
	
	public void addNavigationButton(String buttonText) {
		JButton button = new JButton(buttonText);
		button.addActionListener(controller);
		navigationBar.add(button);
	}
	
	public void openFileInFDEditor() {
        //String fileName = FileTools.PromptForFileOpen(view);
        //ReadFDFileData(fileName, "mono5ms");
        //String fileNameTrimmed = fileName.substring(0, fileName.length() - 4);
        //view.repaint();
	}
	
    public FDEditor() {
        view = new FDView();
        view.setBackground(Color.black);
        controller = new FDController(this);
        setJMenuBar(createMenuBar());
        add(createNavigationBar(), BorderLayout.PAGE_START);
        view.addMouseListener(controller);
        controller.setView(view);
        add(view);
        setSize(1500, 800);
        //openFileInFDEditor();
    }
    
	private static void createAndShowGUI() {
		// Create and set up the window.
		parent = new MultiWindow();
		parent.fdEditorFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		parent.fdEditorFrame.pack();
		parent.fdEditorFrame.setVisible(true);
		//parent.fdEditorFrame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//parent.fdEditorFrame2.pack();
		//parent.fdEditorFrame2.setVisible(true);
	}

	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

}
