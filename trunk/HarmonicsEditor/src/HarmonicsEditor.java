
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

	public enum Channel {
		LEFT,
		RIGHT,
		MONO,
		STEREO,
	}
	
	public static Channel currentChannel = Channel.STEREO;

	public static MultiWindow parent;
	public static HarmonicsView view;
	public static HarmonicsController controller;
	public static HarmonicsActionHandler actionHandler;
	public static JToolBar navigationBar;
	public static ControlPanel controlPanel;	
	public static TreeMap<Long, Harmonic> harmonicIDToHarmonicMono;
	public static TreeMap<Long, Harmonic> harmonicIDToHarmonicLeft;
	public static TreeMap<Long, Harmonic> harmonicIDToHarmonicRight;
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
	public static RandomLoop randomLoop = null;

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

	public static TreeMap<Long, Harmonic> getHarmonicIDToHarmonic() {
		if(currentChannel == Channel.STEREO || currentChannel == Channel.MONO) return harmonicIDToHarmonicMono;
		if(currentChannel == Channel.LEFT) return harmonicIDToHarmonicLeft;
		if(currentChannel == Channel.RIGHT) return harmonicIDToHarmonicRight;
		return null;
	}
	
	public void openFileInHarmonicsEditor(String extension) {
        fileName = FileTools.PromptForFileOpen(view, extension);
        HarmonicsFileInput.ReadBinaryFileData(fileName);
        //removeNullHarmonics();
        if(harmonicIDToHarmonicMono.isEmpty()) {
        	harmonicIDToHarmonicMono = null;
        	return;
        }
        if(harmonicIDToHarmonicLeft.isEmpty()) {
        	harmonicIDToHarmonicLeft = null;
        	return;
        }
        if(harmonicIDToHarmonicRight.isEmpty()) {
        	harmonicIDToHarmonicRight = null;
        	return;
        }     
        this.setTitle(fileName);
        //System.out.println(fileName);
        //view.repaint();
	}
	
	public void removeNullHarmonics() {
		for(long harmonicID: harmonicIDToHarmonicMono.keySet()) {
			if(harmonicIDToHarmonicMono.get(harmonicID) == null) {
				System.out.println("null found");
				harmonicIDToHarmonicMono.remove(harmonicID);
				continue;
			}
			if(!harmonicIDToHarmonicMono.get(harmonicID).containsData()) {
				System.out.println("emptyHarmonicFound");
				harmonicIDToHarmonicMono.remove(harmonicID);
				continue;
			}		
		}
		for(long harmonicID: harmonicIDToHarmonicLeft.keySet()) {
			if(harmonicIDToHarmonicLeft.get(harmonicID) == null) {
				System.out.println("null found");
				harmonicIDToHarmonicLeft.remove(harmonicID);
				continue;
			}
			if(!harmonicIDToHarmonicLeft.get(harmonicID).containsData()) {
				System.out.println("emptyHarmonicFound");
				harmonicIDToHarmonicLeft.remove(harmonicID);
				continue;
			}		
		}
		for(long harmonicID: harmonicIDToHarmonicRight.keySet()) {
			if(harmonicIDToHarmonicRight.get(harmonicID) == null) {
				System.out.println("null found");
				harmonicIDToHarmonicRight.remove(harmonicID);
				continue;
			}
			if(!harmonicIDToHarmonicRight.get(harmonicID).containsData()) {
				System.out.println("emptyHarmonicFound");
				harmonicIDToHarmonicRight.remove(harmonicID);
				continue;
			}		
		}
	}
	
	public void loadInstrument() {
		openFileInHarmonicsEditor(".harmonics");
		SoftSynth.harmonicIDToInstrumentHarmonicMono = harmonicIDToHarmonicMono;
		SoftSynth.harmonicIDToInstrumentHarmonicLeft = harmonicIDToHarmonicLeft;
		SoftSynth.harmonicIDToInstrumentHarmonicRight = harmonicIDToHarmonicRight;
		refreshView();
	}
	
	public void loadKickDrum() {
		openFileInHarmonicsEditor(".harmonics");
		SoftSynth.harmonicIDToKickDrumHarmonicMono = harmonicIDToHarmonicMono;
		SoftSynth.harmonicIDToKickDrumHarmonicLeft = harmonicIDToHarmonicLeft;
		SoftSynth.harmonicIDToKickDrumHarmonicRight = harmonicIDToHarmonicRight;
		refreshView();
	}
	
	public void loadHighFreq() {
		openFileInHarmonicsEditor(".harmonics");
		SoftSynth.harmonicIDToHighFreqHarmonicMono = harmonicIDToHarmonicMono;
		SoftSynth.harmonicIDToHighFreqHarmonicLeft = harmonicIDToHarmonicLeft;
		SoftSynth.harmonicIDToHighFreqHarmonicRight = harmonicIDToHarmonicRight;
		refreshView();
	}
	
	public void loadBassSynth() {
		openFileInHarmonicsEditor(".harmonics");
		SoftSynth.harmonicIDToBassSynthHarmonicMono = harmonicIDToHarmonicMono;
		SoftSynth.harmonicIDToBassSynthHarmonicLeft = harmonicIDToHarmonicLeft;
		SoftSynth.harmonicIDToBassSynthHarmonicRight = harmonicIDToHarmonicRight;
		refreshView();
	}
	
	public void loadSnare() {
		openFileInHarmonicsEditor(".harmonics");
		SoftSynth.harmonicIDToSnareHarmonicMono = harmonicIDToHarmonicMono;
		SoftSynth.harmonicIDToSnareHarmonicLeft = harmonicIDToHarmonicLeft;
		SoftSynth.harmonicIDToSnareHarmonicRight = harmonicIDToHarmonicRight;
		refreshView();
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
		
	public static void handleBeatSelected(int beat) {
		System.out.println("Beat Selected = " + beat);
		randomLoop.handleBeatSelected(beat);
	}
	
	public static void newRandomLoop(HarmonicsEditor parent) {
		randomLoop = new RandomLoop(parent);
		randomLoop.playRandomLoop();
	}
	
	public static void playRandomLoop(HarmonicsEditor parent) {
		if(randomLoop == null) randomLoop = new RandomLoop(parent);
		randomLoop.playRandomLoop();
	}
	
	public static void playScale(HarmonicsEditor parent) {
		if(randomLoop == null) randomLoop = new RandomLoop(parent);
		randomLoop.playScale();
	}
	
	public static int frequencyInHzToNote(double freqInHz) {
		return (int) Math.round(Math.log(freqInHz)/Math.log(2.0) * (double) FDData.noteBase);
	}
	
	public static double noteToFrequencyInHz(int note) {
		return (int) Math.pow(2.0, (double) note / (double) FDData.noteBase);
	}

	public static void clearCurrentData() {
		maxTime = 0;
		harmonicIDToHarmonicMono = new TreeMap<Long, Harmonic>();
		harmonicIDToHarmonicLeft = new TreeMap<Long, Harmonic>();
		harmonicIDToHarmonicRight = new TreeMap<Long, Harmonic>();
	}
	
	public static void addData(FDData data, int channel) {
		TreeMap<Long, Harmonic> harmonicIDToHarmonic = null;
		if(channel == 0) harmonicIDToHarmonic = harmonicIDToHarmonicMono;
		if(channel == 1) harmonicIDToHarmonic = harmonicIDToHarmonicLeft;
		if(channel == 2) harmonicIDToHarmonic = harmonicIDToHarmonicRight;
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
		for(long harmonicID: harmonicIDToHarmonicMono.keySet()) {
			harmonicIDToHarmonicMono.get(harmonicID).flattenHarmonic();
		}
		for(long harmonicID: harmonicIDToHarmonicLeft.keySet()) {
			harmonicIDToHarmonicLeft.get(harmonicID).flattenHarmonic();
		}
		for(long harmonicID: harmonicIDToHarmonicRight.keySet()) {
			harmonicIDToHarmonicRight.get(harmonicID).flattenHarmonic();
		}
	}

}
