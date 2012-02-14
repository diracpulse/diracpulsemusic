
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
	public static int startNoteIndex = 0; // = 
	
	public static final int segmentWidth = 6;
	public static final int segmentHeight = 9;
	public static final int leftFreqSegments = 8; // used by drawFreqScale
	public static final int upperTimeSegments = 6; // used by drawTimeScale
	public static final int xDataStart = segmentWidth * leftFreqSegments; // start of first data cell
	public static final int yDataStart = segmentHeight * upperTimeSegments; // start of first data cell
	public static final int timeStepInMillis = FDData.timeStepInMillis; // timeInMillis = time * timeStepInMillis
	public static final int noteBase = FDData.noteBase; // frequencyInHz = pow(2.0, (note / noteBase))
	
	public static final boolean test = true;
	
	//Data Entry Swing Classes
	public static JTextField startTimeTextField;
	public static JTextField startOctaveTextField;
	public static JTextField startNoteOffsetTextField;
	public static JTextField startLogAmplitudeTextField;
	public static JTextField endTimeTextField;
	public static JTextField endOctaveTextField;
	public static JTextField endNoteOffsetTextField;
	public static JTextField endLogAmplitudeTextField;
		
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
		dataCreationBar.add(new JLabel("S TIME:"));
		startTimeTextField = new JTextField(3 + 1 + 3); // SSS.SSSS
		dataCreationBar.add(startTimeTextField);
		dataCreationBar.add(new JLabel("S(TART) OCTAVE:")); 
		startOctaveTextField = new JTextField(2); // OO
		dataCreationBar.add(startOctaveTextField);
		dataCreationBar.add(new JLabel("S NOTE OFFSET:")); 
		startNoteOffsetTextField = new JTextField(2 + 1 + 2); // NN.FF
		dataCreationBar.add(startNoteOffsetTextField);
		dataCreationBar.add(new JLabel("S LOG AMPLITUDE:"));
		startLogAmplitudeTextField = new JTextField(2 + 1 + 2); // AA.AA
		dataCreationBar.add(startLogAmplitudeTextField);
		dataCreationBar.add(new JLabel("E(ND) TIME:"));
		endTimeTextField = new JTextField(3 + 1 + 3); // SSS.SSSS
		dataCreationBar.add(endTimeTextField);
		dataCreationBar.add(new JLabel("E OCTAVE:")); 
		endOctaveTextField = new JTextField(2); // OO
		dataCreationBar.add(endOctaveTextField);
		dataCreationBar.add(new JLabel("E NOTE OFFSET:")); 
		endNoteOffsetTextField = new JTextField(2 + 1 + 2); // NN.FF
		dataCreationBar.add(endNoteOffsetTextField);
		dataCreationBar.add(new JLabel("E LOG AMPLITUDE"));
		endLogAmplitudeTextField = new JTextField(2 + 1 + 2); // AA.AA
		dataCreationBar.add(endLogAmplitudeTextField);
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
        initTestData();
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
	
	// returns notes in reverse order
	public static ArrayList<Integer> getNotes() {
		TreeSet<Integer> notes = new TreeSet<Integer>();
		for(Integer time: timeToNoteToData.keySet()) {
			for(Integer note: timeToNoteToData.get(time).keySet()) {
				if(!notes.contains(note)) notes.add(note);
			}
		}
		ArrayList<Integer> returnVal = new ArrayList<Integer>();
		for(Integer note: notes) {
			returnVal.add(0, note);
		}
		return returnVal;
	}
	
	public static double getMaxAmplitude() {
		return 16.0;
	}
	
	public static double getMinAmplitude() {
		return 0.0;
	}
	
	public void initTimeToNoteToData() {
		timeToNoteToData = new TreeMap<Integer, TreeMap<Integer, FDData>>();
	}
	
	public void initTestData() {
		if(!test) return;
		try {
			addDataInterpolate(new FDData(0, 31.0 * 6.0, 12.0), new FDData(200, 31.0 * 6.0, 12.0), true);
			addDataInterpolate(new FDData(50, 31.0 * 9.0, 11.0), new FDData(150, 31.0 * 10.0, 0.0), true);
			addDataInterpolate(new FDData(100, 31.0 * 8.0, 10.0), new FDData(1000, 31.0 * 9.0, 0.0), true);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Data out of bounds", 
					"FDEditor.initTestData()", 
					JOptionPane.ERROR_MESSAGE);
		}
		TreeMap<Integer, TreeMap<Integer, FDData>>  timeToNoteToDataClone = SynthTools.copyTreeMap(timeToNoteToData);
		timeToNoteToData = timeToNoteToDataClone;
		
	}

	/* Input ArrayList<String> = 
	 * time (in seconds),
	 * octave = floor(note / noteBase),
	 * note offset = note - octave * noteBase;
	 * logAmplitude = log2(amplitude)
	 */
	public static boolean addDataInterpolate(ArrayList<String> start, ArrayList<String> end, boolean overwrite) {
		FDData startDataPoint;
		FDData endDataPoint;
		int startTime;
		int startNote;
		double startNoteFraction;
		double startLogAmplitude;
		int endTime;
		int endNote;
		double endNoteFraction;
		double endLogAmplitude;
		try {
			double startTimeSeconds = new Double(start.get(0));
			startTime = (int) Math.round(1000.0 * startTimeSeconds / timeStepInMillis);
			int startOctave = new Integer(start.get(1));
			double dStartNoteOffset = new Double(start.get(2));
			int startNoteOffset = (int) Math.round(dStartNoteOffset);
			startNoteFraction = dStartNoteOffset - startNoteOffset;
			startLogAmplitude = new Double(start.get(3));
			startNote = startOctave * noteBase + startNoteOffset;
			double endTimeSeconds = new Double(end.get(0));
			endTime = (int) Math.round(1000.0 * endTimeSeconds / timeStepInMillis);
			int endOctave = new Integer(end.get(1));
			double dEndNoteOffset = new Double(end.get(2));
			int endNoteOffset = (int) Math.round(dEndNoteOffset);
			endNoteFraction = dEndNoteOffset - endNoteOffset;
			endLogAmplitude = new Double(end.get(3));
			endNote = endOctave * noteBase + endNoteOffset;
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, "Error Parsing String To Number", 
					                            "FDEditor.addDataInterpolate", JOptionPane.ERROR_MESSAGE);
			return false;
		}	
		try {
			startDataPoint = new FDData(startTime, startNote, startNoteFraction, startLogAmplitude);
			endDataPoint = new FDData(endTime, endNote, endNoteFraction, endLogAmplitude);
		} catch (Exception e){
			JOptionPane.showMessageDialog(null, "Data out of bounds", 
												"FDEditor.addDataInterpolate (String Args)", 
												JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return addDataInterpolate(startDataPoint, endDataPoint, overwrite);
	}
	
	// returns true if data already exists in interpolated region
	// does not perform bounds checking
	public static boolean addDataInterpolate(FDData start, FDData end, boolean overwrite) {
		FDData dataPoint;
		boolean returnVal = false;
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
				return false;
			}
			interpolatedData.add(dataPoint);		
		}
		returnVal = containsData(interpolatedData);
		if(returnVal && !overwrite) return returnVal;
		addData(interpolatedData);
		return returnVal;
	}
	
	public static void addData(ArrayList<FDData> dataArray) {
		for(FDData dataPoint: dataArray) addData(dataPoint);
	}
	
	public static boolean containsData(ArrayList<FDData> dataArray) {
		for(FDData dataPoint: dataArray) {
			if(containsData(dataPoint)) return true;
		}
		return false;
	}
	
	public static void addData(FDData data) {
		if(!timeToNoteToData.containsKey(data.getTime())) {
			timeToNoteToData.put(data.getTime(), new TreeMap<Integer, FDData>());
		}
		TreeMap<Integer, FDData> noteToData = timeToNoteToData.get(data.getTime());
		noteToData.put(data.getNote(), data);
	}
	
	// returns true if data already exists
	public static boolean containsData(FDData data) {
		if(!timeToNoteToData.containsKey(data.getTime())) return false;
		TreeMap<Integer, FDData> noteToData = timeToNoteToData.get(data.getTime());
		if(!noteToData.containsKey(data.getNote())) return false;
		return true;
	}
	
}
