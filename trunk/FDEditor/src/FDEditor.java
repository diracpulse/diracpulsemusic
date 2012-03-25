
import javax.swing.*;

import java.awt.*;
import java.util.*;
import java.io.*;

public class FDEditor extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6252327634736973395L;
	/**
	 * 
	 */

	public static MultiWindow parent;
	public static FDView view;
	public static FDController controller;
	public static FDActionHandler actionHandler;
	public static JToolBar navigationBar;
	
	public static TreeMap<Long, Harmonic> harmonicIDToHarmonic;
	public static TreeMap<Integer, ArrayList<Long>> averageNoteToHarmonicID;
	public static TreeMap<Integer, TreeMap<Integer, FDData>>  timeToNoteToData;
	public static TreeSet<Long> selectedHarmonicIDs;
	public static TreeSet<Integer> selectedNotes;
	public static ArrayList<Harmonic>  harmonics;
	public static double minLogAmplitudeThreshold = 0.0;
	public static String fileName;
	public static MathTools mathTools;
	
	public static int leftX = 0; // = (actual Time)/ timeStepInMillis
	public static int upperY = 0; // =
	public static int maxTime = 0;
	public static int maxScreenFreq = 0;
	public static int maxNote = 0;
	public static int minNote = Integer.MAX_VALUE;
	
	public static final int xStep = 6;
	public static final int yStep = 9;
	public static final int leftFreqSegments = 8; // used by drawFreqScale
	public static final int upperTimeSegments = 6; // used by drawTimeScale
	public static final int leftOffset = xStep * leftFreqSegments; // start of first data cell
	public static final int upperOffset = yStep * upperTimeSegments; // start of first data cell
	public static final int timeStepInMillis = FDData.timeStepInMillis; // timeInMillis = time * timeStepInMillis
	public static final int noteBase = FDData.noteBase; // frequencyInHz = pow(2.0, (note / noteBase))
	public static Random randomIDGenerator = new Random();

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
        fileName = FileTools.PromptForFileOpen(view);
        FDFileInput.ReadBinaryFileData(fileName);
        initAverageNoteToHarmonicID();
        this.setTitle(fileName);
        view.repaint();
	}
	
    public FDEditor() {
        view = new FDView();
        view.setBackground(Color.black);
        controller = new FDController(this);
        setJMenuBar(createMenuBar());
        //this.setLayout(new GridLayout(3,0));
        add(createNavigationBar(), BorderLayout.NORTH);
        view.addMouseListener(controller);
        controller.setView(view);
        add(view);
        setSize(1500, 800);
        openFileInFDEditor();
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
	
	public static double getMaxAmplitude() {
		return 16.0;
	}
	
	public static double getMinAmplitude() {
		return 0.0;
	}
	
	public static void clearCurrentData() {
		timeToNoteToData = new TreeMap<Integer, TreeMap<Integer, FDData>>();
		harmonicIDToHarmonic = new TreeMap<Long, Harmonic>();
		selectedHarmonicIDs = new TreeSet<Long>();
		selectedNotes = new TreeSet<Integer>();
	}
	
	public static void addData(FDData data) {
		int time = data.getTime();
		int note = data.getNote();
		if(time > maxTime) maxTime = time;
		if(note > maxNote) maxNote = note;
		if(note < minNote) minNote = note;
		long harmonicID = data.getHarmonicID();
		if(!timeToNoteToData.containsKey(time)) {
			timeToNoteToData.put(time, new TreeMap<Integer, FDData>());
		}
		if(!harmonicIDToHarmonic.containsKey(harmonicID)) {
			harmonicIDToHarmonic.put(harmonicID, new Harmonic(harmonicID));
		}
		timeToNoteToData.get(time).put(note, data);
		harmonicIDToHarmonic.get(harmonicID).addData(data);
	}
	
	public static FDData getData(int time, int note) {
		if(timeToNoteToData == null) return null;
		if(!timeToNoteToData.containsKey(time)) return null;
		if(!timeToNoteToData.get(time).containsKey(note)) return null;
		return timeToNoteToData.get(time).get(note);
	}

	public static void playDataInCurrentWindow(FDEditor parent) {
		int endTime = leftX + view.getTimeAxisWidthInMillis() / timeStepInMillis;
		new PlayDataInWindow(parent, leftX, endTime, 50, view.getTimeAxisWidthInMillis(), false);
	}
	
	public static void playSelectedDataInCurrentWindow(FDEditor parent) {
		int endTime = leftX + view.getTimeAxisWidthInMillis() / timeStepInMillis;
		new PlayDataInWindow(parent, leftX, endTime, 50, view.getTimeAxisWidthInMillis(), true);
	}

	public static void drawPlayTime(int offsetInMillis, int refreshRateInMillis) {
		view.drawPlayTime(offsetInMillis, refreshRateInMillis);
		refreshView();
	}
	
	public static void refreshView() {
		view.repaint();
	}

	public static long getRandomID() {
		return randomIDGenerator.nextLong();
	}
	
	public static int freqToNote(int freq) {
		return maxNote - freq;
	}
	
	public static void flattenHarmonics() {
		timeToNoteToData = new TreeMap<Integer, TreeMap<Integer, FDData>>();
		for(Harmonic harmonic: harmonicIDToHarmonic.values()) {
			harmonic.flattenHarmonic();
			for(FDData data: harmonic.getAllData()) {
				int time = data.getTime();
				int note = data.getNote();
				if(!timeToNoteToData.containsKey(time)) {
					timeToNoteToData.put(time, new TreeMap<Integer, FDData>());
				}
				if(timeToNoteToData.get(time).containsKey(note)) {
					FDData currentData = timeToNoteToData.get(time).get(note);
					if(currentData.getLogAmplitude() >= data.getLogAmplitude()) {
						continue;
					}
				}
				timeToNoteToData.get(time).put(note, data);
			} 
		}
		SynthTools.createHarmonics(timeToNoteToData);
		refreshView();
	}

	public static void initAverageNoteToHarmonicID() {
		averageNoteToHarmonicID = new TreeMap<Integer, ArrayList<Long>>();
		for(Harmonic harmonic: harmonicIDToHarmonic.values()) {
			int averageNote = harmonic.getAverageNote();
			if(!averageNoteToHarmonicID.containsKey(averageNote)) {
				averageNoteToHarmonicID.put(averageNote, new ArrayList<Long>());
			}
			averageNoteToHarmonicID.get(averageNote).add(harmonic.getHarmonicID());
		}
	}
	
	public void saveSelectedHarmonicsToFile() {
		String fileName = this.getTitle();
		String fileNameTrimmed = fileName.substring(0, fileName.length() - 9); // ".selected"
        FDFileOutput.OutputHarmonicsToFile(fileNameTrimmed);
        JOptionPane.showMessageDialog(this, "Finished saving: " + fileNameTrimmed + ".harmonics");
	}
}
