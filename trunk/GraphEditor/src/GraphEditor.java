
import javax.swing.*;

import java.awt.*;
import java.util.*;
import java.io.*;

public class GraphEditor extends JFrame {

	private static final long serialVersionUID = 6252327634736973395L;

	public static MultiWindow parent;
	public static GraphView view;
	public static GraphController controller;
	public static GraphActionHandler actionHandler;
	public static HashMap<Long, Harmonic> harmonicIDToHarmonic;
	public static int maxTime = 1;
	public static int minTime = 0;
	public static int maxNote = 1;
	public static int minNote = 0;
	public static double maxLogAmplitude = 1.0;
	public static double minLogAmplitude = 0.0;
	public static boolean minTimeAlwaysZero = true;

	public JMenuBar createMenuBar() {
        GraphActionHandler actionHandler = new GraphActionHandler(this);
        return actionHandler.createMenuBar();
    }

	public void openFileInGraphEditor(String extension) {
		startReadData();
        String fileName = FileTools.PromptForFileOpen(view, extension);
        GraphFileInput.ReadBinaryFileData(fileName);
        endReadData();
        this.setTitle(fileName);
        view.repaint();
	}
	
	static void startReadData() {
		harmonicIDToHarmonic = new HashMap<Long, Harmonic>();
		maxTime = FDData.minTime;
		minTime = FDData.maxTime;
		maxNote = FDData.getMinNote();
		minNote = FDData.getMaxNote();
		maxLogAmplitude = FDData.minLogAmplitude;
	}
	
	public static void addData(FDData data) {
		if(!harmonicIDToHarmonic.containsKey(data.getHarmonicID())) {
			harmonicIDToHarmonic.put(data.getHarmonicID(), new Harmonic(data.getHarmonicID()));
		}
		harmonicIDToHarmonic.get(data.getHarmonicID()).addData(data);
	}
	
	static void endReadData() {
		for(Harmonic harmonic: harmonicIDToHarmonic.values()) {
			if(harmonic.getStartTime() < minTime) minTime = harmonic.getStartTime();
			if(harmonic.getEndTime() > maxTime) maxTime = harmonic.getEndTime();
			if(harmonic.getAverageNote() < minNote) minNote = harmonic.getAverageNote();
			if(harmonic.getAverageNote() > maxNote) maxNote = harmonic.getAverageNote();
			if(harmonic.getMaxLogAmplitude() > maxLogAmplitude) maxLogAmplitude = harmonic.getMaxLogAmplitude();
		}
		if(minTimeAlwaysZero) minTime = 0;
	}	

    public GraphEditor() {
        view = new GraphView();
        view.setBackground(Color.black);
        controller = new GraphController(this);
        setJMenuBar(createMenuBar());
        view.addMouseListener(controller);
        controller.setView(view);
        add(view);
        setSize(1500, 800);
        harmonicIDToHarmonic = new HashMap<Long, Harmonic>();
    }
    
	private static void createAndShowGUI() {
		// Create and set up the window.
		parent = new MultiWindow();
		parent.graphEditorFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		parent.graphEditorFrame.pack();
		parent.graphEditorFrame.setVisible(true);
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
