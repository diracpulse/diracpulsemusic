package main;

import javax.swing.*;

import java.awt.*;
import java.util.*;
import java.io.*;

public class GraphEditor extends JFrame implements AbstractEditor {

	public enum Channel {
		LEFT,
		RIGHT,
		STEREO,
	}
	
	private static final long serialVersionUID = 6252327634736973395L;

	public static MultiWindow parent;
	public static GraphView view;
	public static GraphController controller;
	public static GraphActionHandler actionHandler;
	public static TreeMap<Long, Harmonic> harmonicIDToHarmonic;
	//public static TreeMap<Long, Harmonic> harmonicIDToControlPointHarmonic;
	public static HashMap<Integer, HashMap<Integer, HashSet<Long>>> timeToScaledLogAmplitudeToHarmonicIDs;
	public static HashMap<Integer, HashMap<Integer, HashSet<Long>>> noteToScaledLogAmplitudeToHarmonicIDs;
	public static double logAmplitudeScale = 50.0;
	public static HashMap<Integer, HashMap<Integer, HashSet<Long>>> timeToNoteToHarmonicIDs;
	//public static HashSet<Long> selectedHarmonicIDs;
	public static long activeControlPointHarmonicID = 0;
	public static int minHarmonicLength = 1;
	public static int maxTime = 1;
	public static int minTime = 0;
	public static int maxNote = 1;
	public static int minNote = 0;
	public static double maxLogAmplitude = 1.0;
	public static double minLogAmplitude = 0.0;
	public static boolean minTimeAlwaysZero = true;
	public static int minViewTime = 0;
	public static int maxViewTime = 1;
	public static int maxViewNote = 0;
	public static int minViewNote = 1;
	public static double maxViewLogAmplitude = 0.0;
	public static double minViewLogAmplitude = 1.0;
	public static double minClipThreshold = 0.0;
	public static double maxClipThreshold = 24.0;
	public static boolean displaySelectedHarmonics = true;
	public static boolean displayUnselectedHarmonics = true;
	public static Random randomIDGenerator;
	public static Channel currentChannel = Channel.STEREO;
	
	public JMenuBar createMenuBar() {
        GraphActionHandler actionHandler = new GraphActionHandler(this);
        return actionHandler.createMenuBar();
    }

	public void addHarmonicsToGraphEditor(TreeMap<Long, Harmonic> harmonicIDToHarmonicIn) {
		initVariables();
		for(Harmonic harmonic: harmonicIDToHarmonicIn.values()) {
			for(FDData data: harmonic.getAllData()) {
				addData(data);
			}
		}
        endReadData();
	}
	
	static void initVariables() {
		harmonicIDToHarmonic = new TreeMap<Long, Harmonic>();
		timeToScaledLogAmplitudeToHarmonicIDs = new HashMap<Integer, HashMap<Integer, HashSet<Long>>>();
		noteToScaledLogAmplitudeToHarmonicIDs = new HashMap<Integer, HashMap<Integer, HashSet<Long>>>();
		timeToNoteToHarmonicIDs = new HashMap<Integer, HashMap<Integer, HashSet<Long>>>();
		//harmonicIDToControlPointHarmonic = new TreeMap<Long, Harmonic>();
		activeControlPointHarmonicID = randomIDGenerator.nextLong();
		//harmonicIDToControlPointHarmonic.put(activeControlPointHarmonicID, new Harmonic(activeControlPointHarmonicID));
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
			for(FDData data: harmonic.getAllDataInterpolated().values()) {
				int scaledLogAmplitude = (int) Math.round(data.getLogAmplitude() * logAmplitudeScale);
				if(!timeToScaledLogAmplitudeToHarmonicIDs.containsKey(data.getTime())) {
					timeToScaledLogAmplitudeToHarmonicIDs.put(data.getTime(), new HashMap<Integer, HashSet<Long>>());
				}
				if(!timeToScaledLogAmplitudeToHarmonicIDs.get(data.getTime()).containsKey(scaledLogAmplitude)) {
					timeToScaledLogAmplitudeToHarmonicIDs.get(data.getTime()).put(scaledLogAmplitude, new HashSet<Long>());
				}
				timeToScaledLogAmplitudeToHarmonicIDs.get(data.getTime()).get(scaledLogAmplitude).add(data.getHarmonicID());
				if(!noteToScaledLogAmplitudeToHarmonicIDs.containsKey(data.getNote())) {
					noteToScaledLogAmplitudeToHarmonicIDs.put(data.getNote(), new HashMap<Integer, HashSet<Long>>());
				}
				if(!noteToScaledLogAmplitudeToHarmonicIDs.get(data.getNote()).containsKey(scaledLogAmplitude)) {
					noteToScaledLogAmplitudeToHarmonicIDs.get(data.getNote()).put(scaledLogAmplitude, new HashSet<Long>());
				}
				noteToScaledLogAmplitudeToHarmonicIDs.get(data.getNote()).get(scaledLogAmplitude).add(data.getHarmonicID());
				if(!timeToNoteToHarmonicIDs.containsKey(data.getTime())) {
					timeToNoteToHarmonicIDs.put(data.getTime(), new HashMap<Integer, HashSet<Long>>());
				}
				if(!timeToNoteToHarmonicIDs.get(data.getTime()).containsKey(data.getNote())) {
					timeToNoteToHarmonicIDs.get(data.getTime()).put(data.getNote(), new HashSet<Long>());
				}
				timeToNoteToHarmonicIDs.get(data.getTime()).get(data.getNote()).add(data.getHarmonicID());
			}
		}
		resetView();
	}
	
	static void resetView() {
		if(minTimeAlwaysZero) minTime = 0;
		minViewTime = minTime;
		maxViewTime = maxTime;
		minViewNote = minNote;
		maxViewNote = maxNote;
		minViewLogAmplitude = 0.0;
		maxViewLogAmplitude = maxLogAmplitude;
		refreshView();
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
        randomIDGenerator = new Random();
        initVariables();
        this.setTitle("GraphEditor: [no file]");
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

	public static void toggleDisplaySelected() {
		displaySelectedHarmonics = !displaySelectedHarmonics;
		refreshView();
	}
	
	public static void toggleDisplayUnselected() {
		displayUnselectedHarmonics = !displayUnselectedHarmonics;
		refreshView();
	}
	
	public static int frequencyInHzToNote(double freqInHz) {
		return (int) Math.round(Math.log(freqInHz)/Math.log(2.0) * (double) FDData.noteBase);
	}
	
	public static double noteToFrequencyInHz(int note) {
		return (int) Math.pow(2.0, (double) note / (double) FDData.noteBase);
	}
	
	public static void promptForOctaveView(GraphEditor parent) {
		Object[] octaves = {0, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192};
		Integer octave =  (Integer) JOptionPane.showInputDialog(parent, "Frequency Range Select", 
										"Select Minimum Freq", JOptionPane.PLAIN_MESSAGE, null, octaves, 0);
		if(octave == null) return;
		if(octave == 0 ) {
			minViewNote = minNote;
			maxViewNote = maxNote;
			view.refresh();
			return;
		}
		minViewNote = frequencyInHzToNote(octave);
		maxViewNote = minViewNote + FDData.noteBase * 2;
		refreshView();
	}

	public static void leftClickMenu(GraphEditor parent, int x, int y) {
		if(GraphView.yView == GraphView.YView.AMPLITUDE) {
			clipAmplitude(y);
			return;
		}
		clipFrequency(parent, y);
	}
	
	// zoom > 1 -> Zoom In
	// zoom < 1 -> Zoom Out
	public static void zoomInTime(int x, double zoom) {
		double divisor = 2 * zoom;
		System.out.println(minViewTime + " " + maxViewTime);
		minViewTime = (int) Math.round(GraphUtils.screenXToValue(x - view.getWidth() / divisor));
		maxViewTime = (int) Math.round(GraphUtils.screenXToValue(x + view.getWidth() / divisor));
		if(minViewTime < 0) {
			maxViewTime -= minViewTime;
			minViewTime = 0;
		}
		if(maxViewTime > maxTime) {
			minViewTime += (maxViewTime - maxTime);
			maxViewTime = maxTime;
		}
		System.out.println(minViewTime + " " + maxViewTime);
	}
	
	public static void zoomOutTime(int x, double zoom) {
		double divisor = 2 / zoom;
		System.out.println(minViewTime + " " + maxViewTime);
		minViewTime = (int) Math.round(GraphUtils.screenXToValue(x - view.getWidth() / divisor));
		maxViewTime = (int) Math.round(GraphUtils.screenXToValue(x + view.getWidth() / divisor));
		if(minViewTime < 0) minViewTime = 0;
		if(maxViewTime > maxTime) maxViewTime = maxTime;
		System.out.println(minViewTime + " " + maxViewTime);
	}
	
	// zoom > 1 -> Zoom In
	// zoom < 1 -> Zoom Out
	public static void zoomInFrequencyX(int x, double zoom) {
		double divisor = 2 * zoom;
		System.out.println(minViewNote + " " + maxViewNote);
		minViewNote = (int) Math.round(GraphUtils.screenXToValue(x + view.getWidth() / divisor));
		maxViewNote = (int) Math.round(GraphUtils.screenXToValue(x - view.getWidth() / divisor));
		if(minViewNote < 0) {
			maxViewNote -= minViewNote;
			minViewNote = 0;
		}
		if(maxViewNote > maxNote) {
			minViewNote += (maxViewNote - maxNote);
			maxViewNote = maxNote;
		}
		System.out.println(minViewNote + " " + maxViewNote);
	}
	
	public static void zoomOutFrequencyX(int x, double zoom) {
		double divisor = 2 / zoom;
		System.out.println(minViewNote + " " + maxViewNote);
		minViewNote = (int) Math.round(GraphUtils.screenXToValue(x + view.getWidth() / divisor));
		maxViewNote = (int) Math.round(GraphUtils.screenXToValue(x - view.getWidth() / divisor));
		if(minViewNote < 0) minViewNote = 0;
		if(maxViewNote > maxNote) maxViewNote = maxNote;
		System.out.println(minViewNote + " " + maxViewNote);
	}
	
	// zoom > 1 -> Zoom In
	// zoom < 1 -> Zoom Out
	public static void zoomInFrequencyY(int y, double zoom) {
		double divisor = 2 * zoom;
		System.out.println(minViewNote + " " + maxViewNote);
		minViewNote = (int) Math.round(GraphUtils.screenYToValue(y + view.getHeight() / divisor));
		maxViewNote = (int) Math.round(GraphUtils.screenYToValue(y - view.getHeight() / divisor));
		System.out.println(minViewNote + " " + maxViewNote);
		if(minViewNote < minNote) {
			maxViewNote -= (minViewNote - minNote);
			minViewNote = minNote;
		}
		if(maxViewNote > maxNote) {
			minViewNote -= (maxViewNote - maxNote);
			maxViewNote = maxNote;
		}
		System.out.println(minViewNote + " " + maxViewNote);
	}
	
	public static void zoomOutFrequencyY(int y, double zoom) {
		double divisor = 2 / zoom;
		System.out.println(minViewNote + " " + maxViewNote);
		minViewNote = (int) Math.round(GraphUtils.screenYToValue(y + view.getHeight() / divisor));
		maxViewNote = (int) Math.round(GraphUtils.screenYToValue(y - view.getHeight() / divisor));
		System.out.println(minViewNote + " " + maxViewNote);
		if(minViewNote < minNote) minViewNote = minNote;
		if(maxViewNote > maxNote) maxViewNote = maxNote;
		System.out.println(minViewNote + " " + maxViewNote);
	}
	
	// zoom > 1 -> Zoom In
	// zoom < 1 -> Zoom Out
	public static void zoomInAmplitude(int y, double zoom) {
		double divisor = 2 * zoom;
		System.out.println(minViewLogAmplitude + " " + maxViewLogAmplitude);
		minViewLogAmplitude = GraphUtils.screenYToValue(y + view.getHeight() / divisor);
		maxViewLogAmplitude = GraphUtils.screenYToValue(y - view.getHeight() / divisor);
		if(minViewLogAmplitude < 0) {
			maxViewLogAmplitude -= minViewLogAmplitude;
			minViewLogAmplitude = 0;
		}
		if(maxViewLogAmplitude > maxLogAmplitude) {
			minViewLogAmplitude -= (maxViewLogAmplitude - maxLogAmplitude);
			maxViewLogAmplitude = maxLogAmplitude;
		}
		System.out.println(minViewLogAmplitude + " " + maxViewLogAmplitude);
	}
	
	public static void zoomOutAmplitude(int y, double zoom) {
		double divisor = 2 / zoom;
		System.out.println(minViewLogAmplitude + " " + maxViewLogAmplitude);
		minViewLogAmplitude = GraphUtils.screenYToValue(y + view.getHeight() / divisor);
		maxViewLogAmplitude = GraphUtils.screenYToValue(y - view.getHeight() / divisor);
		System.out.println(minViewLogAmplitude + " " + maxViewLogAmplitude);
		if(minViewLogAmplitude < 0) minViewLogAmplitude = 0;
		if(maxViewLogAmplitude > maxLogAmplitude) maxViewLogAmplitude = maxLogAmplitude;
		System.out.println(minViewLogAmplitude + " " + maxViewLogAmplitude);
	}
	
	public static void clipAmplitude(int y) {
		minViewLogAmplitude = GraphUtils.screenYToValue(y);
		refreshView();
	}
	
	public static void clipFrequency(GraphEditor parent, int y) {
		Object[] colorData = {"Clip Treble", "Clip Bass"};
		String choice =  (String) JOptionPane.showInputDialog(parent, "Color Display Select", 
										"Select Clip Region", JOptionPane.PLAIN_MESSAGE, null, colorData, 0);
		if(choice == null) return;
		if(choice.equals("Clip Treble")) {
			maxViewNote = (int) Math.round(GraphUtils.screenYToValue(y));
		}
		if(choice.equals("Clip Bass")) {
			minViewNote = (int) Math.round(GraphUtils.screenYToValue(y));
		}
		refreshView();
	}

	public static void newControlPointHarmonic() {
		//activeControlPointHarmonicID = randomIDGenerator.nextLong();
		//harmonicIDToControlPointHarmonic.put(activeControlPointHarmonicID, new Harmonic(activeControlPointHarmonicID));
	}
	
	public void createPCMDataLinear() {
		GraphSynthTools.createPCMDataLinear();
	}
	
	public void createPCMDataLinearCubicSpline() {
		GraphSynthTools.createPCMDataLinearCubicSpline();
	}
	
	public void createPCMDataLinearNoise() {
		GraphSynthTools.createPCMDataLinearNoise();
	}
	
	public void playPCMData() {
		GraphSynthTools.playPCMData();
	}

	public int getMaxViewTime() {
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
	
	public static void playDataInControlPoints(GraphEditor parent) {
		//GraphSynthTools.createPCMControlPointData();
		//GraphSynthTools.playWindow();
	}

	public static void refreshView() {
		view.refresh();
	}
	
}