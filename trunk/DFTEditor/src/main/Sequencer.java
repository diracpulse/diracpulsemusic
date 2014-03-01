package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

public class Sequencer extends JPanel {

	private static final long serialVersionUID = 3138005743637187863L;
	
	MultiWindow parent;
	SequencerView view;
	SequencerController controller;
	ArrayList<ModuleEditor> moduleEditors = null;
	public JScrollPane scrollPane;
	private JToolBar navigationBar = null;
	public static int noteBase = 53;
	public static double minFreq = 256.0;
	public static int noteHeight = 11;
	public static int numPercussion = 3;
	public static double bpm = 120;
	public static int maxBeats = 20;
	public static int pixelsPerBeat = 128;
	public static int totalPixels = maxBeats * pixelsPerBeat;
	public static int divisionsPerBeat = 8;
	public static int pixelsPerDivision = pixelsPerBeat / divisionsPerBeat;
	public static double secondsPerBeat = 60.0 / bpm;
	public static double maxTimeInSeconds = maxBeats * secondsPerBeat;
	public static int leftDigits = 9;
	public static int scrollableWidth = totalPixels + SequencerUtils.digitWidth * leftDigits;
	public static double secondsPerPixel = secondsPerBeat / pixelsPerBeat;
	public static int scrollableHeight = (noteBase + 3) * noteHeight;
	public ArrayList<double[]> freqRatiosAtTimeInPixels;
	public ArrayList<MultiWindow.ModuleEditorInfo> moduleInfo;
	public int currentModuleIndex = 0;
	double[] leftSamples = null;
	double[] rightSamples = null;
	
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
        addNavigationButton("Get Module");
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
        scrollPane = new JScrollPane(view);
        scrollPane.setSize(800, 600);
        add(scrollPane, BorderLayout.CENTER);
        freqRatiosAtTimeInPixels = new ArrayList<double[]>();
        moduleInfo = new ArrayList<MultiWindow.ModuleEditorInfo>();
        for(MultiWindow.ModuleEditorInfo info: parent.moduleEditorInfo) {
        	freqRatiosAtTimeInPixels.add(new double[maxBeats * pixelsPerBeat]);
        	double[] freqRatioAtTime = freqRatiosAtTimeInPixels.get(freqRatiosAtTimeInPixels.size() - 1);
        	for(int time = 0; time < freqRatioAtTime.length; time++) {
        		freqRatioAtTime[time] = -1.0;
        	}
        	moduleInfo.add(info);
        }
    }
    
    
    public void initLeftRight() {
    	double samplesPerPixel = (secondsPerBeat / pixelsPerBeat) * SynthTools.sampleRate;
    	int numSamples = (int) Math.round(samplesPerPixel * pixelsPerBeat * maxBeats);
    	leftSamples = new double[numSamples];
    	rightSamples = new double[numSamples];
    	for(int sample = 0; sample < numSamples; sample++) {
    		leftSamples[sample] = 0.0;
    		rightSamples[sample] = 0.0;
    	}
    	for(int moduleIndex = 0; moduleIndex < parent.moduleEditorInfo.size(); moduleIndex++) {
    		double[] controlSamples = new double[numSamples];
    		double[] controlPixels = freqRatiosAtTimeInPixels.get(moduleIndex);
	    	for(int pixel = 0; pixel < pixelsPerBeat * maxBeats - 1; pixel++) {
	    		int startSample = (int) Math.round(pixel * samplesPerPixel);
	    		int endSample = (int) Math.round((pixel + 1) * samplesPerPixel);
	    		double controlVal = controlPixels[pixel];
	    		if(controlVal > 0.0 && controlVal < 1.0) controlVal = 1.0; // percussion
	    		for(int sample = startSample; sample < endSample; sample++) {
	    			if(sample >= numSamples) break;
	    			controlSamples[sample] = controlVal;
	    		}
	    	}
	    	double[] leftOut = parent.moduleEditorInfo.get(moduleIndex).getModuleEditor().getSamples(controlSamples).get(0);
	    	double[] rightOut = parent.moduleEditorInfo.get(moduleIndex).getModuleEditor().getSamples(controlSamples).get(1);
	    	if(leftOut == null || rightOut == null) continue; 
	    	for(int sample = 0; sample < numSamples; sample++) {
	    		if(sample < leftOut.length) leftSamples[sample] += leftOut[sample];
	    		if(sample < rightOut.length) rightSamples[sample] += rightOut[sample];
	    	}
    	}
    }
    
    public void play() {
    	initLeftRight();
		AudioPlayer.playAudio(leftSamples, rightSamples);
    }
    
	public void dft() {
		initLeftRight();
		parent.dftEditorFrame.ModuleDFT(leftSamples, rightSamples);
	}
    
}
