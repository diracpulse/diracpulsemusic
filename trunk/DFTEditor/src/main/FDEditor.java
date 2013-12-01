
import javax.swing.*;

import java.awt.*;
import java.util.*;
import java.io.*;

public class FDEditor extends JFrame implements AbstractEditor {

	private static final long serialVersionUID = 6252327634736973395L;

	public enum Channel {
		LEFT,
		RIGHT,
		STEREO,
	}
	
	public static MultiWindow parent;
	public static FDView view;
	public static FDController controller;
	public static FDActionHandler actionHandler;
	public static JToolBar navigationBar;
	
	//public static TreeMap<Long, Harmonic> harmonicIDToHarmonic;
	public static TreeMap<Integer, TreeMap<Integer, ArrayList<FDData>>>  timeToNoteToData;
	//public static TreeSet<Long> selectedHarmonicIDs;
	//public static ArrayList<Harmonic>  harmonics;
	public static double minLogAmplitudeThreshold = 0.0;
	
	public static int minViewTime = 0;
	public static int minViewFreq = 0;
	public static int maxTime = 0;
	//public static int maxScreenFreq = 0;
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
	public static Channel currentChannel = Channel.STEREO;

	public JMenuBar createMenuBar() {
        FDActionHandler actionHandler = new FDActionHandler(this);
        return actionHandler.createMenuBar();
    }
	
	public void addHarmonicsToFDEditor(TreeMap<Long, Harmonic> harmonicIDToHarmonicIn) {
		clearCurrentData();
		for(Harmonic harmonic: harmonicIDToHarmonicIn.values()) {
			for(FDData data: harmonic.getAllData()) {
				addData(data);
			}
		}
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
        this.setTitle("FDEditor: [no file]");
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
		timeToNoteToData = new TreeMap<Integer, TreeMap<Integer, ArrayList<FDData>>>();
		//harmonicIDToHarmonic = new TreeMap<Long, Harmonic>();
	}
	
	public static void addData(FDData data) {
		int time = data.getTime();
		int note = data.getNote();
		if(time > maxTime) maxTime = time;
		if(note > maxNote) maxNote = note;
		if(note < minNote) minNote = note;
		long harmonicID = data.getHarmonicID();
		if(!timeToNoteToData.containsKey(time)) {
			timeToNoteToData.put(time, new TreeMap<Integer, ArrayList<FDData>>());
		}
		if(!timeToNoteToData.get(time).containsKey(note)) {
			timeToNoteToData.get(time).put(note, new ArrayList<FDData>());
		}		
		//if(!harmonicIDToHarmonic.containsKey(harmonicID)) {
		//	harmonicIDToHarmonic.put(harmonicID, new Harmonic(harmonicID));
		//}
		timeToNoteToData.get(time).get(note).add(data);
		//harmonicIDToHarmonic.get(harmonicID).addData(data);
	}
	
	public static ArrayList<FDData> getData(int time, int note) {
		if(timeToNoteToData == null) return null;
		if(!timeToNoteToData.containsKey(time)) return null;
		if(!timeToNoteToData.get(time).containsKey(note)) return null;
		return timeToNoteToData.get(time).get(note);
	}

	public static int getMaxViewTime() {
		int maxViewTime = minViewTime + view.getTimeAxisWidth();
		if(maxViewTime < maxTime) return maxViewTime;
		return maxTime;
	}
	
	public int getMaxViewTimeInMillis() {
		return getMaxViewTime() * FDData.timeStepInMillis;
	}
	
	public void playAllDataInCurrentWindow() {
		new PlayDataInWindow(this, 50, getMaxViewTimeInMillis());
	}
	
	public void playSelectedDataInCurrentWindow() {
		new PlayDataInWindow(this, 50, getMaxViewTimeInMillis());
	}
	
	public void drawPlayTime(int offsetInMillis) {
		view.drawPlayTime(offsetInMillis);
		refreshView();
	}
	
	public void createPCMDataLinear() {
		FDSynthTools.createPCMDataLinear();
	}
	
	public void createPCMDataLinearCubicSpline() {
		FDSynthTools.createPCMDataLinearCubicSpline();
	}
	
	public void createPCMDataLinearNoise() {
		FDSynthTools.createPCMDataLinearNoise();
	}
	
	public void playPCMData() {
		FDSynthTools.playPCMData();
	}
	
	public static void refreshView() {
		view.refresh();
	}

	public static long getRandomID() {
		return randomIDGenerator.nextLong();
	}
	
	public static int freqToNote(int freq) {
		return maxNote - freq;
	}
		
	public static int noteToFreq(int note) {
		return maxNote - note;
	}
	
	public void saveHarmonicsToFile(ArrayList<Harmonic> harmonics) {
		String fileName = DFTEditor.dftFileName;
		String fileNameTrimmed = fileName.substring(0, fileName.length() - 4); // ".wav"
        FileOutput.OutputHarmonicsToFile(fileNameTrimmed + ".selected", harmonics);
        JOptionPane.showMessageDialog(this, "Finished saving: " + fileNameTrimmed + ".selected");
	}

}
