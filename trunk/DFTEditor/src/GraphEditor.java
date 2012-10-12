
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
	public static TreeMap<Long, Harmonic> harmonicIDToHarmonic;
	public static TreeMap<Long, Harmonic> harmonicIDToControlPointHarmonic;
	public static HashMap<Integer, HashMap<Integer, Long>> timeToLogAmplitudeTimes10ToHarmonicID;
	public static HashMap<Integer, HashMap<Integer, Long>> timeToNoteToHarmonicID;
	public static HashSet<Long> selectedHarmonicIDs;
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
	public static boolean clipZero = false;
	public static boolean displaySelectedHarmonics = true;
	public static boolean displayUnselectedHarmonics = true;
	public static double zoomFactor = 2.0;
	public static Random randomIDGenerator;
	
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
		timeToLogAmplitudeTimes10ToHarmonicID = new HashMap<Integer, HashMap<Integer, Long>>();
		timeToNoteToHarmonicID = new HashMap<Integer, HashMap<Integer, Long>>();
		selectedHarmonicIDs = new HashSet<Long>();
		harmonicIDToControlPointHarmonic = new TreeMap<Long, Harmonic>();
		activeControlPointHarmonicID = randomIDGenerator.nextLong();
		harmonicIDToControlPointHarmonic.put(activeControlPointHarmonicID, new Harmonic(activeControlPointHarmonicID));
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
		int logAmplitudeTimes10 = (int) Math.round(data.getLogAmplitude() * 10.0);
		if(!timeToLogAmplitudeTimes10ToHarmonicID.containsKey(data.getTime())) {
			timeToLogAmplitudeTimes10ToHarmonicID.put(data.getTime(), new HashMap<Integer, Long>());
		}
		timeToLogAmplitudeTimes10ToHarmonicID.get(data.getTime()).put(logAmplitudeTimes10, data.getHarmonicID());
		if(!timeToNoteToHarmonicID.containsKey(data.getTime())) {
			timeToNoteToHarmonicID.put(data.getTime(), new HashMap<Integer, Long>());
		}
		timeToNoteToHarmonicID.get(data.getTime()).put(data.getNote(), data.getHarmonicID());
	}
	
	static void endReadData() {
		for(Harmonic harmonic: harmonicIDToHarmonic.values()) {
			if(harmonic.getStartTime() < minTime) minTime = harmonic.getStartTime();
			if(harmonic.getEndTime() > maxTime) maxTime = harmonic.getEndTime();
			if(harmonic.getAverageNote() < minNote) minNote = harmonic.getAverageNote();
			if(harmonic.getAverageNote() > maxNote) maxNote = harmonic.getAverageNote();
			if(harmonic.getMaxLogAmplitude() > maxLogAmplitude) maxLogAmplitude = harmonic.getMaxLogAmplitude();
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
		view.repaint();
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
	
	public static void toggleDisplaySelected() {
		displaySelectedHarmonics = !displaySelectedHarmonics;
		view.repaint();
	}
	
	public static void toggleDisplayUnselected() {
		displayUnselectedHarmonics = !displayUnselectedHarmonics;
		view.repaint();
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
			view.repaint();
			return;
		}
		minViewNote = frequencyInHzToNote(octave);
		maxViewNote = minViewNote + FDData.noteBase * 2;
		view.repaint();
	}
	
	public static void promptForColorView(GraphEditor parent) {
		Object[] colorData = {"Amplitude", "Frequency", "Harmonics"};
		String choice =  (String) JOptionPane.showInputDialog(parent, "Color Display Select", 
										"Select Color View", JOptionPane.PLAIN_MESSAGE, null, colorData, 2);
		if(choice == null) return;
		if(choice.equals("Amplitude")) GraphView.colorView = GraphView.ColorView.AMPLITUDE;
		if(choice.equals("Frequency")) GraphView.colorView = GraphView.ColorView.FREQUENCY;
		if(choice.equals("Harmonics")) GraphView.colorView = GraphView.ColorView.HARMONICS;
		view.repaint();
	}
	
	public static void promptForYView(GraphEditor parent) {
		Object[] colorData = {"Amplitude", "Frequency"};
		String choice =  (String) JOptionPane.showInputDialog(parent, "Y View Select", 
										"Select Color View", JOptionPane.PLAIN_MESSAGE, null, colorData, 0);
		if(choice == null) return;
		if(choice.equals("Amplitude")) {
			GraphView.yView = GraphView.YView.AMPLITUDE;
			GraphView.colorView = GraphView.ColorView.FREQUENCY;
		}
		if(choice.equals("Frequency")) {
			GraphView.yView = GraphView.YView.FREQUENCY;
			GraphView.colorView = GraphView.ColorView.AMPLITUDE;
		}
		view.repaint();
	}
	
	public static void leftClickMenu(GraphEditor parent, int x, int y) {
		if(GraphView.yView == GraphView.YView.AMPLITUDE) {
			clipAmplitude(y);
			return;
		}
		clipFrequency(parent, y);
	}
	
	public static void zoomInX(int x) {
		double divisor = 2 * zoomFactor;
		minViewTime = GraphUtils.screenXToTime(x - view.getWidth() / divisor);
		maxViewTime = GraphUtils.screenXToTime(x + view.getWidth() / divisor);
		if(minViewTime < 0) {
			maxViewTime -= minViewTime;
			minViewTime = 0;
		}
		if(maxViewTime > maxTime) {
			minViewTime += (maxViewTime - maxTime);
			maxViewTime = maxTime;
		}
		view.repaint();
	}
	
	public static void zoomInAmplitude(int y) {
		double divisor = 2 * zoomFactor;
		System.out.println(y);
		minViewLogAmplitude = GraphUtils.screenYToValue(y + view.getHeight() / divisor);
		maxViewLogAmplitude = GraphUtils.screenYToValue(y - view.getHeight() / divisor);
		System.out.println(minViewLogAmplitude + " " + maxViewLogAmplitude);
		if(minViewLogAmplitude < 0) {
			maxViewLogAmplitude -= minViewLogAmplitude;
			minViewLogAmplitude = 0;
		}
		if(maxViewLogAmplitude > maxLogAmplitude) {
			minViewLogAmplitude -= (maxViewLogAmplitude - maxLogAmplitude);
			maxViewLogAmplitude = maxLogAmplitude;
		}
		System.out.println(minViewLogAmplitude + " " + maxViewLogAmplitude);
		view.repaint();
	}
	
	public static void clipAmplitude(int y) {
		minViewLogAmplitude = GraphUtils.screenYToValue(y);
		view.repaint();
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
		view.repaint();
	}
	
	public static void zoomInFrequency(int y) {
		double divisor = 2 * zoomFactor;
		System.out.println(y);
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
		view.repaint();
	}
	
	public static void newControlPointHarmonic() {
		activeControlPointHarmonicID = randomIDGenerator.nextLong();
		harmonicIDToControlPointHarmonic.put(activeControlPointHarmonicID, new Harmonic(activeControlPointHarmonicID));
	}
	
	public static void playDataInCurrentWindow(GraphEditor parent) {
		GraphSynthTools.createPCMWindowData();
		GraphSynthTools.playWindow();
	}
	
	public static void playDataInSequencer(GraphEditor parent) {
		GraphSynthTools.createPCMSequencerData();
		GraphSynthTools.playWindow();
	}
	
	public static void playDataInControlPoints(GraphEditor parent) {
		GraphSynthTools.createPCMControlPointData();
		GraphSynthTools.playWindow();
	}

	public static void drawPlayTime(int offsetInMillis) {
		view.drawPlayTime(offsetInMillis);
		view.repaint();
	}

}
