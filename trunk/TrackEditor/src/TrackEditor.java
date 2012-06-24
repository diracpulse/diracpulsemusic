
import javax.swing.*;

import java.awt.*;
import java.util.*;
import java.io.*;

public class TrackEditor extends JFrame {

	private static final long serialVersionUID = 6252327634736973395L;

	public static MultiWindow parent;
	public static TrackView view;
	public static TrackController controller;
	public static TrackActionHandler actionHandler;
	public static HashMap<Integer, ArrayList<Harmonic>> fileIndexToHarmonics = new HashMap<Integer, ArrayList<Harmonic>>();
	public static HashMap<Long, Harmonic> loopHarmonicIDToHarmonic = new HashMap<Long, Harmonic>();
	public static HashMap<Long, Harmonic> trackHarmonicIDToHarmonic = new HashMap<Long, Harmonic>();
	public static HashMap<Integer, HashSet<Integer>> fileIndexToTaggedBeats = new HashMap<Integer, HashSet<Integer>>();
	public static double maxLoopLogAmplitude = 16.0;
	public static double minLoopLogAmplitude = 0.0;
	public static Random randomIDGenerator = new Random();
	public static int beatLengthInMillis = 500;
	public static int numBeats = 0;
	public static File[] loopFiles = null;
	public static int currentLoopFileIndex = 0;
	public static int xStep = 6; // one digit
	public static int yStep = 9; // one digit;
	
	public JMenuBar createMenuBar() {
        TrackActionHandler actionHandler = new TrackActionHandler(this);
        return actionHandler.createMenuBar();
    }
	
	public void openDirectoryInTrackEditor() {
		loopFiles = FileTools.PromptForDirectoryOpen(view);
		if(loopFiles == null) return;
		loopHarmonicIDToHarmonic = new HashMap<Long, Harmonic>();
		trackHarmonicIDToHarmonic = new HashMap<Long, Harmonic>();
		fileIndexToHarmonics = new HashMap<Integer, ArrayList<Harmonic>>();
		int index = 0;
		for(File loopFile: TrackEditor.loopFiles) {
			TrackEditor.loopHarmonicIDToHarmonic = new HashMap<Long, Harmonic>();
			TrackFileInput.ReadBinaryFileData(loopFile.getAbsolutePath());
			fileIndexToHarmonics.put(index, new ArrayList<Harmonic>());
			for(Harmonic harmonic: TrackEditor.loopHarmonicIDToHarmonic.values()) {
				harmonic.flattenHarmonic();
				fileIndexToHarmonics.get(index).add(harmonic);
			}
			fileIndexToTaggedBeats.put(index, new HashSet<Integer>());
			index++;
		}
		currentLoopFileIndex = index - 1;
		TrackView.initLeftPanel();
		view.repaint();
	}

	public static void selectNewLoop(int index) {
		loopHarmonicIDToHarmonic = new HashMap<Long, Harmonic>();
		for(Harmonic harmonic: TrackEditor.fileIndexToHarmonics.get(index)) {
			loopHarmonicIDToHarmonic.put(harmonic.getHarmonicID(), harmonic);
		}
		currentLoopFileIndex = index;
		view.initLeftPanel();
		view.repaint();
	}
		
	public static void addData(FDData data) {
		if(!loopHarmonicIDToHarmonic.containsKey(data.getHarmonicID())) {
			loopHarmonicIDToHarmonic.put(data.getHarmonicID(), new Harmonic(data.getHarmonicID()));
		}
		loopHarmonicIDToHarmonic.get(data.getHarmonicID()).addData(data);
	}
	
	public static void addLoop() {
		addBeat(0);
		addBeat(1);
		addBeat(2);
		addBeat(3);		
	}
	
	public static void addBeat(int beat) {
		int beatTimeStep = beatLengthInMillis / FDData.timeStepInMillis;
		int loopStartTime = beatTimeStep * beat;
		int loopEndTime = loopStartTime + beatTimeStep;
		int trackStartTime = numBeats * beatTimeStep;
		HashMap<Long, Harmonic> tempHarmonicIDToHarmonic = new HashMap<Long, Harmonic>();
		for(Harmonic harmonic: loopHarmonicIDToHarmonic.values()) {
			long id = randomIDGenerator.nextLong();
			tempHarmonicIDToHarmonic.put(id, new Harmonic(id));
			for(FDData data: harmonic.getAllData().values()) {
				if(data.getTime() >= loopStartTime && data.getTime() < loopEndTime) {
					int newTime = data.getTime() - loopStartTime + trackStartTime;
					try {
						FDData newData = new FDData(newTime, data.getNote(), data.getLogAmplitude(), id);
						tempHarmonicIDToHarmonic.get(id).addData(newData);
						//System.out.println(newData);
					} catch (Exception e) {
						System.out.println("Error: TrackEditor.addCurrentData()");
					}
				}
			}
		}
		for(Harmonic harmonic: tempHarmonicIDToHarmonic.values()) {
			if(!harmonic.containsData()) continue;
			trackHarmonicIDToHarmonic.put(harmonic.getHarmonicID(), harmonic);
		}
		numBeats++;
		view.repaint();
	}

	public static void clearTrackData() {
		trackHarmonicIDToHarmonic = new HashMap<Long, Harmonic>();
		numBeats = 0;
		view.repaint();
	}
	
	public static void toggleTaggedBeat(int beat) {
		if(fileIndexToTaggedBeats.get(currentLoopFileIndex).contains(beat)) {
			fileIndexToTaggedBeats.get(currentLoopFileIndex).remove(beat);
		} else {
			fileIndexToTaggedBeats.get(currentLoopFileIndex).add(beat);
		}
		view.initLeftPanel();
		view.repaint();
	}
	
    public TrackEditor() {
        view = new TrackView();
        view.setBackground(Color.black);
        controller = new TrackController(this);
        setJMenuBar(createMenuBar());
        view.addMouseListener(controller);
        controller.setView(view);
        add(view);
        setSize(1500, 800);
        loopHarmonicIDToHarmonic = new HashMap<Long, Harmonic>();
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
	
	public static int frequencyInHzToNote(double freqInHz) {
		return (int) Math.round(Math.log(freqInHz)/Math.log(2.0) * (double) FDData.noteBase);
	}
	
	public static double noteToFrequencyInHz(int note) {
		return (int) Math.pow(2.0, (double) note / (double) FDData.noteBase);
	}
	
	public static void playLoopDataInCurrentWindow(TrackEditor parent) {
		new PlayData(parent, 60, view.getTimeAxisWidthInMillis(), PlayData.DataType.LOOP);
	}
	
	public static void playTrackDataInCurrentWindow(TrackEditor parent) {
		new PlayData(parent, 60, view.getTimeAxisWidthInMillis(), PlayData.DataType.TRACK);
	}

	public static void drawPlayTime(int offsetInMillis) {
		view.drawPlayTime(offsetInMillis);
		view.repaint();
	}

}
