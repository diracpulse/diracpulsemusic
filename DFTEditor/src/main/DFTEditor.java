package main;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class DFTEditor extends JFrame implements AbstractEditor {
	
	public enum Channel {
		LEFT,
		RIGHT,
		STEREO,
	}

	public enum ChannelMixer {
		LEFT_RIGHT,
		WAV,
		LEFT_WAV,
		WAV_RIGHT;
		
	}
	
	public static Channel currentChannel = Channel.STEREO;
	public static ChannelMixer currentChannelMixer = ChannelMixer.LEFT_RIGHT;
	public static String dftFileName = null;
	private static final long serialVersionUID = -2291799595853726615L;
	public static MultiWindow parent;
	public static DFTView view;
	public static DFTController controller;
	// Swing components
	//public static JMenuBar menuBar = null;
	public static JToolBar navigationBar = null;
	
	//public static TreeMap<Integer, TreeMap<Integer, Float>> timeToFreqToAmp;
	public static double[][] amplitudesLeft; // amplitude = amplitudes[time][freq]
	public static double[][] amplitudesRight; // amplitude = amplitudes[time][freq]
	public static double[][] amplitudesStereo; // amplitude = amplitudes[time][freq]
	public static TreeMap<Integer, TreeSet<Integer>> timeToFreqsAtMaximaLeft;
	public static TreeMap<Integer, TreeSet<Integer>> timeToFreqsAtMaximaRight;
	public static TreeMap<Integer, TreeSet<Integer>> timeToNoiseFreqsAtMaximaLeft;
	public static TreeMap<Integer, TreeSet<Integer>> timeToNoiseFreqsAtMaximaRight;
	public static TreeMap<Long, Harmonic> harmonicIDToHarmonic = null;
	public static HashSet<Long> selectedHarmonicIDs;
	public static HashSet<Integer> selectedNotes;
	//public static int minHarmonicLength = 1;
	public static double minLogAmplitudeThreshold = 1.0; // used by autoSelect
	public static int minLengthThreshold = 1;
	public static boolean deleteSelected = false;
	public static TreeMap<Integer, Integer> floorAmpToCount;
	public static int digitWidth = 6; // one digit
	public static int digitHeight = 9; // one digit;
	public static int leftOffset = digitWidth * 6; // start of first data cell
	public static int upperOffset = digitHeight * 6; // start of first data cell
	public static int minViewTime = 0; // index of freq in leftmost data cell
	public static int minViewFreq = 0; // index of time in uppermost data cell
	public static int timeStepInMillis = FDData.timeStepInMillis; // time in ms = time * timeStepInMillis
	// these are the max/min valued amplitude for the whole file
	// they are used to calculate the color of amplitude values
	public static float maxAmplitude = 17.0f;
	public static float minAmplitude = 0.0f;
	// the maximum summed value at any given time
	public static float maxAmplitudeSum;
	public static double minFreqHz = DFT.minFreqHz;
	public static double maxFreqHz = DFT.maxFreqHz;
	public static int freqsPerOctave = FDData.noteBase;
	public static int minScreenNote;
	public static int maxScreenNote;
	public static int maxScreenFreq;
	// maxTime = length of file in ms / timeStepInMillis
	public static int maxTime = 0;
	// harmonic display variables
	public static int drawHarmonicsBaseFreq = -1; // -1 means don't display
	public static Random randomIDGenerator;
	
	
	// IMPORTANT: In order to display data with upper frequencies above lower frequencies:
	// amplitudes[time][0] references the HIGHEST note (maxRealFreq)
	// amplitudes[time][maxScreenFreq] references the LOWEST note (minRealFreq)
	// in general "freq" refers to the index to the data in amplitudes[][]
	// in general "note" refers to freqInHz = 2^(note / noteBase) and is the value output by FDData
	// they are related as calculated in noteToFreq(int note) and freqToNote(int freq)
	
	public static void newFileData() {
		selectedHarmonicIDs = new HashSet<Long>();
		selectedNotes = new HashSet<Integer>();
	    harmonicIDToHarmonic = new TreeMap<Long, Harmonic>();
		timeToFreqsAtMaximaLeft = new TreeMap<Integer, TreeSet<Integer>>();
		timeToFreqsAtMaximaRight = new TreeMap<Integer, TreeSet<Integer>>();
		timeToNoiseFreqsAtMaximaLeft = new TreeMap<Integer, TreeSet<Integer>>();
		timeToNoiseFreqsAtMaximaRight = new TreeMap<Integer, TreeSet<Integer>>();
	}
	/*
	public static float[][] getAmplitudes() {
		if(currentChannel == Channel.LEFT) return amplitudesLeft;
		if(currentChannel == Channel.RIGHT) return amplitudesRight;
		return null;
	}
	*/
	/*
	public static TreeMap<Integer, TreeSet<Integer>> getMaximas() {
		if(currentChannel == Channel.LEFT) return timeToFreqsAtMaximaLeft;
		if(currentChannel == Channel.RIGHT) return timeToFreqsAtMaximaRight;
		return null;
	}
	*/
	public static double getAmplitude(int time, int freq) {
		if(amplitudesLeft == null || amplitudesRight == null) return 0.0f;
		double leftVal = 0.0f;
		double rightVal = 0.0f;
		if(time < amplitudesLeft.length && freq < amplitudesLeft[0].length) leftVal = amplitudesLeft[time][freq];
		if(time < amplitudesRight.length && freq < amplitudesRight[0].length) rightVal = amplitudesRight[time][freq]; 
		if(currentChannel == Channel.LEFT) return leftVal;
		if(currentChannel == Channel.RIGHT) return rightVal;
		if(currentChannel == Channel.STEREO) {
			if(leftVal > rightVal) return leftVal;
			return rightVal;
		}
		System.out.println("DFTEditor.getAmplitude: unknown channel");
		return -1.0f;
	}
	
	public static boolean isMaxima(FDData.Channel channel, int time, int freq) {
		TreeMap<Integer, TreeSet<Integer>> timeToFreqsAtMaxima = null;
		if(channel == FDData.Channel.LEFT) timeToFreqsAtMaxima = timeToFreqsAtMaximaLeft;
		if(channel == FDData.Channel.RIGHT) timeToFreqsAtMaxima = timeToFreqsAtMaximaRight;
		if(timeToFreqsAtMaxima == null) return false;
		if(time >= maxTime || freq >= maxScreenFreq) return false;
		if(!timeToFreqsAtMaxima.containsKey(time)) return false;
		return timeToFreqsAtMaxima.get(time).contains(freq);
	}
	
	public static boolean isMaxima(int time, int freq) {
		if(timeToFreqsAtMaximaLeft == null || timeToFreqsAtMaximaRight == null) return false;
		if(time >= maxTime && freq >= maxScreenFreq) return false;
		boolean left = false;
		boolean right = false;
		if(timeToFreqsAtMaximaLeft.containsKey(time)) {
			left = timeToFreqsAtMaximaLeft.get(time).contains(freq);
		}
		if(timeToFreqsAtMaximaRight.containsKey(time)) {
			right = timeToFreqsAtMaximaRight.get(time).contains(freq);
		}
		if(currentChannel == Channel.LEFT) return left;
		if(currentChannel == Channel.RIGHT) return right;
		if(currentChannel == Channel.STEREO) {
			return left || right;
		}
		System.out.println("DFTEditor.isMaxima: unknown channel");
		return false;
	}
	
	public static int noteToFreq(int note) {
		return maxScreenNote - note;
	}
	
	public static int freqToNote(int freq) {
		return maxScreenNote - freq;
	}
	
	public static int getTimeAxisWidthInMillis() {
		return view.getTimeAxisWidthInMillis();
	}
	
	public static int getMinViewTimeInMillis() {
		return minViewTime * FDData.timeStepInMillis;
	}
	
	public static int getMaxViewTime() {
		int maxViewTime = minViewTime + view.getTimeAxisWidth();
		if(maxViewTime < maxTime) return maxViewTime;
		return maxTime;
	}
	
	public int getMaxViewTimeInMillis() {
		return getMaxViewTime() * FDData.timeStepInMillis;
	}
	
	public void playDataInCurrentWindow() {
		new PlayDataInWindow(this, 50, getMaxViewTimeInMillis());
	}
	
	public void drawPlayTime(int offsetInMillis) {
		view.drawPlayTime(offsetInMillis);
		refreshView();
	}
	
	public void createPCMDataLinear() {
		SynthTools.createPCMDataLinear();
	}
	
	public void createPCMDataLinearCubicSpline() {
		SynthTools.createPCMDataLinearCubicSpline();
	}

	public void playPCMData() {
		SynthTools.playPCMData();
	}
	
	public static void refreshView() {
		view.refresh();
	}

	public static long getRandomID() {
		return randomIDGenerator.nextLong();
	}

	public void printFloorAmpCount() {
		int total = 0;
		for(int i: floorAmpToCount.keySet()) {
			int count = floorAmpToCount.get(i);
			System.out.println("floor(amp) =  " + i + ": count = " + count);
			total += count;
		}
		System.out.println("total count: " + total);
	}
	
	public JMenuBar createMenuBar() {
        ActionHandler actionHandler = new ActionHandler(this);
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

	public void FileDFT(boolean prompt) {
		newFileData();
        if(prompt || dftFileName == null) dftFileName = FileTools.PromptForFileOpenWAV(view);
        DFT.FileDFTMatrix(dftFileName);
    	SynthTools.refresh = true;
    	SynthTools.createHarmonics();
    	ActionHandler.refreshAll();
        this.setTitle("DFTEditor: " + dftFileName);
        refreshAllViews();
	}

	public void ModuleDFT(double[] left, double[] right) {
		newFileData();
        DFT.SynthDFTMatrix(left, right);
    	SynthTools.refresh = true;
    	SynthTools.createHarmonics();
    	ActionHandler.refreshAll();
        this.setTitle("DFTEditor: [Modules Output]");
        refreshAllViews();
	}
	
	public void cutoff() {
		SynthTools.createHarmonics();
        //parent.graphEditorFrame.addHarmonicsToGraphEditor(harmonicIDToHarmonic);
        //parent.fdEditorFrame.addHarmonicsToFDEditor(harmonicIDToHarmonic);
        refreshAllViews();
	}
	
	public static void selectHarmonics(HashSet<Long> harmonicIDs) {
		if(harmonicIDs == null) return;
		for(long harmonicID: harmonicIDs) {
			if(selectedHarmonicIDs.contains(harmonicID)) continue;
			selectedHarmonicIDs.add(harmonicID);
			Harmonic harmonic = harmonicIDToHarmonic.get(harmonicID);
			for(FDData innerData: harmonic.getAllDataInterpolated().values()) {
				if(!DFTEditor.selectedNotes.contains(innerData.getNote())) DFTEditor.selectedNotes.add(innerData.getNote());
			}
		}
		refreshAllViews();
	}
	
	public static void unselectHarmonics(HashSet<Long> harmonicIDs) {
		if(harmonicIDs == null) return;
		for(long harmonicID: harmonicIDs) {	
			if(selectedHarmonicIDs.contains(harmonicID)) continue;
			selectedHarmonicIDs.remove(harmonicID);
			refreshAllViews();
		}
	}
	
	public static HashSet<Long> getSelectedHarmonicIDs() {
		// this is done to avoid a null error in FDSynthTools at: "if(!harmonic.isSynthesized()) continue;"
		if(selectedHarmonicIDs == null) return new HashSet<Long>();
		return selectedHarmonicIDs;
	}
	
	public static void refreshAllViews() {
		view.refresh();
		//FDEditor.view.refresh();
		//GraphEditor.view.refresh();
	}
	
	static void sliceAtTime(int time) {
		int numFreqs = DFTEditor.amplitudesLeft[0].length;
		for(int freq = 0; freq < numFreqs; freq++) {
			DFTEditor.amplitudesLeft[time][freq] = 0.0f;
		}
		numFreqs = DFTEditor.amplitudesRight[0].length;
		for(int freq = 0; freq < numFreqs; freq++) {
			DFTEditor.amplitudesRight[time][freq] = 0.0f;
		}
		SynthTools.createHarmonics();
	}

    public DFTEditor() {
    	//FileConvert.wavImportAll();
        view = new DFTView();
        view.setBackground(Color.black);
        controller = new DFTController();
        setJMenuBar(createMenuBar());
        add(createNavigationBar(), BorderLayout.PAGE_START);
        view.addMouseListener(controller);
        add(view);
        setSize(1500, 800);
        randomIDGenerator = new Random();
        this.setTitle("DFTEditor: [no file]");
        newFileData();
    }
    
	private static void createAndShowGUI() {
		// Create and set up the window.
		//parent = new MultiWindow();
		//parent.dftEditorFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//parent.dftEditorFrame.pack();
		//parent.dftEditorFrame.setVisible(true);
	}

	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				//createAndShowGUI();
				parent = new MultiWindow();
			}
		});
	}

}
