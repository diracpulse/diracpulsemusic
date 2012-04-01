
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.io.*;

public class HarmonicsEditor extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6252327634736973395L;
	/**
	 * 
	 */

	public static MultiWindow parent;
	public static HarmonicsView view;
	public static HarmonicsController controller;
	public static HarmonicsActionHandler actionHandler;
	public static JToolBar navigationBar;
	public static ControlPanel controlPanel;
	
	public static TreeMap<Long, Harmonic> harmonicIDToHarmonic;
	public static TreeSet<Integer> averageNotes;
	//public static TreeMap<Integer, TreeMap<Integer, FDData>>  timeToNoteToData;
	public static ArrayList<Harmonic>  harmonics;
	public static double minLogAmplitudeThreshold = 0.0;
	public static int bpm = 120;
	public static double maxAmplitude = 14.0;
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
	public static final int controlPanelSegments = 14;
	public static final int leftFreqSegments = 8; // used by drawFreqScale
	public static final int upperTimeSegments = 6; // used by drawTimeScale
	public static final int leftOffset = xStep * (leftFreqSegments + controlPanelSegments); // start of first data cell
	public static final int upperOffset = yStep * upperTimeSegments; // start of first data cell
	public static final int timeStepInMillis = FDData.timeStepInMillis; // timeInMillis = time * timeStepInMillis
	public static final int noteBase = FDData.noteBase; // frequencyInHz = pow(2.0, (note / noteBase))
	public static Random randomGenerator = new Random();

	public JMenuBar createMenuBar() {
        HarmonicsActionHandler actionHandler = new HarmonicsActionHandler(this);
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

	public void openFileInHarmonicsEditor() {
        fileName = FileTools.PromptForFileOpen(view);
        HarmonicsFileInput.ReadBinaryFileData(fileName);
        this.setTitle(fileName);
        System.out.println("Hello");
        //view.repaint();
	}
	
	public void loadInstrument() {
		openFileInHarmonicsEditor();
		SoftSynth.harmonicIDToInstrumentHarmonic = harmonicIDToHarmonic;
	}
	
    public HarmonicsEditor() {
        view = new HarmonicsView();
        view.setBackground(Color.black);
        controller = new HarmonicsController(this);
        setJMenuBar(createMenuBar());
        //this.setLayout(new GridLayout(3,0));
        add(createNavigationBar(), BorderLayout.NORTH);
        view.addMouseListener(controller);
        controller.setView(view);
        add(view);
        setSize(1500, 800);
        controlPanel = new ControlPanel(xStep, yStep);
        //openFileInHarmonicsEditor();
        HarmonicsEditor.clearCurrentData();
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
	
	public static int[] getRandomChord() {
		int randIndex = randomGenerator.nextInt(10);
		switch(randIndex) {
			case 0:
				return new int[] {6, 7, 8};
			case 1:
				return new int[] {7, 6, 8};
			case 2:
				return new int[] {6, 7, 10};
			case 3:
				return new int[] {7, 6, 10};
			case 4:
				return new int[] {8, 10, 6};
			case 5:
				return new int[] {8, 10, 7};
			case 6:
				return new int[] {10, 8, 6};
			case 7:
				return new int[] {10, 8, 7};
			case 8:
				return new int[] {13, 10};
			case 9:
				return new int[] {10, 13};
		}
		return null;
	}
	
	public static int[] getRandomConsonantChord() {
		int randIndex = randomGenerator.nextInt(6);
		switch(randIndex) {
			case 0:
				return new int[] {13, 8};
			case 1:
				return new int[] {8, 13};
			case 2:
				return new int[] {8, 10};
			case 3:
				return new int[] {10, 8};
			case 4:
				return new int[] {13, 10};
			case 5:
				return new int[] {10, 13};
		}
		return null;
	}
	
	public static void randomLoop(HarmonicsEditor parent) {
		clearCurrentData();
		SoftSynth.initLoop();
		int centerNote = frequencyInHzToNote(350.0);
		int duration = 100;
		int numBeats = 3;
		for(int time = 0; time < duration * numBeats; time += duration) {
			int note = centerNote + randomGenerator.nextInt(13);
			int[] chords = getRandomChord();
			SoftSynth.addBeat(time, note, chords, duration);
		}
		SoftSynth.addDataToHarmonicsEditor();
		playSelectedDataInCurrentWindow(parent);
	}

	public static int frequencyInHzToNote(double freqInHz) {
		return (int) Math.round(Math.log(freqInHz)/Math.log(2.0) * (double) FDData.noteBase);
	}
	
	public static double noteToFrequencyInHz(int note) {
		return (int) Math.pow(2.0, (double) note / (double) FDData.noteBase);
	}

	public static void clearCurrentData() {
		harmonicIDToHarmonic = new TreeMap<Long, Harmonic>();
	}
	
	public static void addData(FDData data) {
		int time = data.getTime();
		int note = data.getNote();
		if(time > maxTime) maxTime = time;
		if(note > maxNote) maxNote = note;
		if(note < minNote) minNote = note;
		long harmonicID = data.getHarmonicID();
		if(!harmonicIDToHarmonic.containsKey(harmonicID)) {
			harmonicIDToHarmonic.put(harmonicID, new Harmonic(harmonicID));
		}
		harmonicIDToHarmonic.get(harmonicID).addData(data);
	}
	
	public static void playSelectedDataInCurrentWindow(HarmonicsEditor parent) {
		int endTime = leftX + view.getTimeAxisWidthInMillis() / timeStepInMillis;
		// HACK: view.getTimeAxisWidthInMillis() * 2; TIMES TWO IS HACK TO PLAY ALL
		new PlayDataInWindow(parent, leftX, endTime, 50, view.getTimeAxisWidthInMillis() * 2);
	}

	public static void drawPlayTime(int offsetInMillis, int refreshRateInMillis) {
		view.drawPlayTime(offsetInMillis, refreshRateInMillis);
		refreshView();
	}
	
	public static void refreshView() {
		view.repaint();
	}

	public static long getRandomID() {
		return randomGenerator.nextLong();
	}
	
	public static int getRandomInt(int range) {
		return randomGenerator.nextInt(range);
	}
	
	public static double getRandomDouble(double range) {
		return range * 2.0 * (randomGenerator.nextDouble() - 0.5);
	}
	
	public static void flattenAllHarmonics() {
		for(long harmonicID: harmonicIDToHarmonic.keySet()) {
			harmonicIDToHarmonic.get(harmonicID).flattenHarmonic();
		}
	}

}
