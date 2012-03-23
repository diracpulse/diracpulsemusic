
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
	public static Random randomIDGenerator = new Random();

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
        view.repaint();
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
	
	public static void addBeat(int octave, int note, TreeSet<Integer> overtones, int duration) {
		int absoluteNote = frequencyInHzToNote(octave) + note;
		int maxNote = frequencyInHzToNote(FDData.maxFrequencyInHz);
		double secondsPerBeat = 60.0 / (double) bpm;
		double maxDuration = (double) 1000.0 / FDData.timeStepInMillis * secondsPerBeat;
		maxDuration *= 4.0 / duration;
		try {
			long harmonicID = randomIDGenerator.nextLong();
			System.out.println(0 + " " + absoluteNote + " " + maxAmplitude + " " + harmonicID);
			System.out.println(maxDuration + " " + absoluteNote + " " + 0.0 + " " + harmonicID);
			FDData start = new FDData(0, absoluteNote, (float) maxAmplitude, harmonicID);
			FDData end = new FDData((int) maxDuration, absoluteNote, 0.0f, harmonicID);
			System.out.println(start + " : " + end);
			addData(start);
			addData(end);
			for(int overtone: overtones) {
				harmonicID = randomIDGenerator.nextLong();
				int currentNote = absoluteNote + frequencyInHzToNote(overtone);
				while(currentNote < maxNote) {
					harmonicID = randomIDGenerator.nextLong();
					double taper = (currentNote - absoluteNote) / (double) FDData.noteBase;
					double logAmplitude = maxAmplitude - taper;
					double currentDuration = maxDuration / taper;
					start = new FDData(0, currentNote, (float) logAmplitude, harmonicID);
					end = new FDData((int) currentDuration, currentNote, 0.0f, harmonicID);
					System.out.println(start + " : " + end);
					addData(start);
					addData(end);
					currentNote += 31.0;
				}
			}
		} catch (Exception e) {
			System.out.println("HarmonicsEditor.addBeat() error creating data:" + e.getMessage());
		}
		refreshView();
	}
	
	public static int frequencyInHzToNote(double freqInHz) {
		return (int) Math.round(Math.log(freqInHz)/Math.log(2.0) * (double) FDData.noteBase);
	}

	// returns true if data already exists in interpolated region
	// does not perform bounds checking
	public static void addHarmonicInterpolate(FDData start, FDData end, boolean overwrite) {
		FDData dataPoint;
		ArrayList<FDData> interpolatedData = new ArrayList<FDData>();
		if(start.getTime() > end.getTime()) {
			FDData temp = start;
			start = end;
			end = temp;
			System.out.println("FDEditor.addDataInterpolate start,end exchanged");
		}
		double deltaTime = end.getTime() - start.getTime();
		double deltaLogAmplitude = end.getLogAmplitude() - start.getLogAmplitude();
		double deltaNote = end.getNoteComplete() - start.getNoteComplete();
		for(int time = start.getTime(); time <= end.getTime(); time++) {
			double elapsedTime = time - start.getTime();
			double logAmplitude = start.getLogAmplitude() + deltaLogAmplitude * elapsedTime / deltaTime;
			double dNote = start.getNoteComplete() + deltaNote * elapsedTime / deltaTime;
			int note = (int) Math.round(dNote);
			double noteFraction = dNote - note;
			try {
				dataPoint = new FDData(time, note, noteFraction, logAmplitude);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Data out of bounds", 
													"FDEditor.addDataInterpolate(Numerical Args)", 
													JOptionPane.ERROR_MESSAGE);
				return;
			}
			interpolatedData.add(dataPoint);		
		}
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
		new PlayDataInWindow(parent, leftX, endTime, 50, view.getTimeAxisWidthInMillis());
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
	
	public static void flattenAllHarmonics() {
		for(long harmonicID: harmonicIDToHarmonic.keySet()) {
			harmonicIDToHarmonic.get(harmonicID).flattenHarmonic();
		}
	}

}
