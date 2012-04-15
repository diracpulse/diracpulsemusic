
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
	public static int minLength = 20;
	public static int maxNote = 1;
	public static int minNote = 0;
	public static double maxLogAmplitude = 1.0;
	public static double minLogAmplitude = 0.0;
	public static boolean minTimeAlwaysZero = true;
	public static int maxViewTime = 0;
	public static int minViewTime = 1;
	public static int maxViewNote = 0;
	public static int minViewNote = 1;
	public static boolean clipZero = false;
	

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
		minViewTime = minTime;
		maxViewTime = 200;
		minViewNote = minNote;
		maxViewNote = maxNote;
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
	
	public static void toggleClipZero() {
		clipZero = !clipZero;
		view.repaint();
	}
	
	public static int frequencyInHzToNote(double freqInHz) {
		return (int) Math.round(Math.log(freqInHz)/Math.log(2.0) * (double) FDData.noteBase);
	}
	
	public static void promptForOctaveView(GraphEditor parent) {
		Object[] octaves = {0, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192};
		Integer octave =  (Integer) JOptionPane.showInputDialog(parent, "Frequency Range Select", 
										"Select Minimum Freq", JOptionPane.PLAIN_MESSAGE, null, octaves, 0);
		if(octave == null) return;
		if(octave == 0 ) {
			minViewNote = minNote;
			maxViewNote = maxNote;
		}
		minViewNote = frequencyInHzToNote(octave);
		maxViewNote = minViewNote + FDData.noteBase * 2;
		view.repaint();
	}
	
	public static void promptForColorView(GraphEditor parent) {
		Object[] colorData = {"Frequency", "Harmonics"};
		String choice =  (String) JOptionPane.showInputDialog(parent, "Color Display Select", 
										"Select Color View", JOptionPane.PLAIN_MESSAGE, null, colorData, 0);
		if(choice == null) return;
		if(choice.equals("Frequency")) GraphView.dataView = GraphView.DataView.FREQUENCY;
		if(choice.equals("Harmonics")) GraphView.dataView = GraphView.DataView.HARMONICS;
		view.repaint();
	}

}
