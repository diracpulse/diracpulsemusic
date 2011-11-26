
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.io.*;

public class FDEditor extends JFrame {
	
	/**
	 * 
	 */

	public static MultiWindow parent;
	public static FDView view;
	public static FDController controller;
	public static FDActionHandler actionHandler;
	public static JToolBar navigationBar;
	public static JToolBar dataCreationBar;
	public static TreeMap<Integer, TreeMap<Integer, FDData>>  timeToNoteToData;
	
	public static int startTimeIndex = 0; // = (actual Time)/ timeStepInMillis
	public static int startFreqIndex = 0; // = 
	
	public static final int segmentWidth = 6;
	public static final int segmentHeight = 9;
	public static final int leftFreqSegments = 8; // used by drawFreqScale
	public static final int upperTimeSegments = 6; // used by drawTimeScale
	public static final int xDataStart = segmentWidth * leftFreqSegments; // start of first data cell
	public static final int yDataStart = segmentHeight * upperTimeSegments; // start of first data cell
	public static final int timeStepInMillis = 5; // timeInMillis = time * timeStepInMillis
	public static final int noteBase = 31; // frequencyInHz = pow(2.0, (note / noteBase))
	
	//Data Entry Swing Classes
	public static JTextField startTimeTextField;
	public static JTextField startNoteTextField;
	public static JTextField startAmplitudeTextField;
	public static JTextField endTimeTextField;
	public static JTextField endNoteTextField;
	public static JTextField endAmplitudeTextField;
	
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
	
	public JToolBar createDataCreationBar() {
		dataCreationBar = new JToolBar("Data Creation Bar");
		dataCreationBar.add(new JLabel("START TIME: (S)"));
		startTimeTextField = new JTextField(3 + 1 + 3); // SSS.SSSS
		dataCreationBar.add(startTimeTextField);
		dataCreationBar.add(new JLabel("START NOTE: (Octave,Note.Fraction)")); 
		startNoteTextField = new JTextField(2 + 1 + 2 + 1 + 2); // OO,NN.FF
		dataCreationBar.add(startNoteTextField);
		dataCreationBar.add(new JLabel("START AMPLITUDE(2.0 ^ VALUE)"));
		startAmplitudeTextField = new JTextField(2 + 1 + 2); // AA.AA
		dataCreationBar.add(startAmplitudeTextField);
		dataCreationBar.add(new JLabel("end TIME: (S)"));
		endTimeTextField = new JTextField(3 + 1 + 3); // SSS.SSSS
		dataCreationBar.add(endTimeTextField);
		dataCreationBar.add(new JLabel("end NOTE: (Octave,Note.Fraction)")); 
		endNoteTextField = new JTextField(2 + 1 + 2 + 1 + 2); // OO,NN.FF
		dataCreationBar.add(endNoteTextField);
		dataCreationBar.add(new JLabel("end AMPLITUDE(2.0 ^ VALUE)"));
		endAmplitudeTextField = new JTextField(2 + 1 + 2); // AA.AA
		dataCreationBar.add(endAmplitudeTextField);
		JButton button = new JButton("Add Data");
		button.addActionListener(controller);
		dataCreationBar.add(button);
		return dataCreationBar;
	}
	
	public void openFileInFDEditor() {
        //String fileName = FileTools.PromptForFileOpen(view);
        //ReadFDFileData(fileName, "mono5ms");
        //String fileNameTrimmed = fileName.substring(0, fileName.length() - 4);
        //view.repaint();
	}
	
    public FDEditor() {
        view = new FDView();
        view.setBackground(Color.black);
        controller = new FDController(this);
        setJMenuBar(createMenuBar());
        //this.setLayout(new GridLayout(3,0));
        add(createNavigationBar(), BorderLayout.NORTH);
        add(createDataCreationBar(), BorderLayout.SOUTH);
        view.addMouseListener(controller);
        controller.setView(view);
        add(view);
        setSize(1500, 800);
        initTimeToNoteToData();
        //openFileInFDEditor();
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
	
	public static TreeSet<Integer> getNotes() {
		TreeSet<Integer> notes = new TreeSet<Integer>();
		for(Integer time: timeToNoteToData.keySet()) {
			for(Integer note: timeToNoteToData.get(time).keySet()) {
				if(!notes.contains(note)) notes.add(note);
			}
		}
		return notes;
	}
	
	public void initTimeToNoteToData() {
		timeToNoteToData = new TreeMap<Integer, TreeMap<Integer, FDData>>();
	}
	
	// returns true if data already exists in interpolated region
	public boolean addDataInterpolate(FDData start, FDData end, boolean overwrite) {
		boolean returnVal = false;
		ArrayList<FDData> interpolatedData = new ArrayList<FDData>();
		if(start.time < end.time) {
			FDData temp = start;
			start = end;
			end = temp;
			System.out.println("FDEditor.addDataInterpolate start,end exchanged");
		}
		double deltaTime = end.time - start.time;
		double deltaLogAmplitude = end.logAmplitude - start.logAmplitude;
		double deltaNote = end.note - start.note;
		for(int time = start.time; time < end.time; time++) {
			double elapsedTime = time - start.time;
			double logAmplitude = start.logAmplitude + deltaLogAmplitude * elapsedTime / deltaTime;
			double dNote = start.noteFraction + deltaNote * elapsedTime / deltaTime;
			int note = (int) Math.round(dNote);
			double noteFraction = dNote - note;
			FDData dataPoint = new FDData(time, note, logAmplitude);
			dataPoint.setNoteFraction(noteFraction);
			interpolatedData.add(dataPoint);
		}
		returnVal = containsData(interpolatedData);
		if(returnVal && !overwrite) return returnVal;
		addData(interpolatedData);
		return returnVal;
	}
	
	public void addData(ArrayList<FDData> dataArray) {
		for(FDData dataPoint: dataArray) addData(dataPoint);
	}
	
	public boolean containsData(ArrayList<FDData> dataArray) {
		for(FDData dataPoint: dataArray) {
			if(containsData(dataPoint)) return true;
		}
		return false;
	}
	
	// returns true if data already exists
	public void addData(FDData data) {
		if(!timeToNoteToData.containsKey(data.getTime())) {
			timeToNoteToData.put(data.getTime(), new TreeMap<Integer, FDData>());
		}
		TreeMap<Integer, FDData> noteToData = timeToNoteToData.get(data.getTime());
		noteToData.put(data.getNote(), data);
	}
	
	public boolean containsData(FDData data) {
		if(!timeToNoteToData.containsKey(data.getTime())) return false;
		TreeMap<Integer, FDData> noteToData = timeToNoteToData.get(data.getTime());
		if(!noteToData.containsKey(data.getNote())) return false;
		return true;
	}
	
}
