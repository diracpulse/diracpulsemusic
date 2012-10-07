
import javax.swing.*;
import java.awt.*;
import java.util.*;

public class DFTEditor extends JFrame {
	
	public enum Channel {
		LEFT,
		RIGHT,
		MONO,
		STEREO,
	}
	
	public static Channel currentChannel = Channel.MONO;
	private static final long serialVersionUID = -2291799595853726615L;
	public static MultiWindow parent;
	public static DFTView view;
	public static DFTController controller;
	// Swing components
	//public static JMenuBar menuBar = null;
	public static JToolBar navigationBar = null;
	
	//public static TreeMap<Integer, TreeMap<Integer, Float>> timeToFreqToAmp;
	public static float[][] amplitudesMono; // amplitude = amplitudes[time][freq]
	public static float[][] amplitudesLeft; // amplitude = amplitudes[time][freq]
	public static float[][] amplitudesRight; // amplitude = amplitudes[time][freq]
	public static TreeMap<Integer, TreeSet<Integer>> timeToFreqsAtMaximaMono;
	public static TreeMap<Integer, TreeSet<Integer>> timeToFreqsAtMaximaLeft;
	public static TreeMap<Integer, TreeSet<Integer>> timeToFreqsAtMaximaRight;
	public static TreeMap<Integer, TreeMap<Integer, FDData>>  timeToFreqToSelectedDataMono;
	public static TreeMap<Integer, TreeMap<Integer, FDData>>  timeToFreqToSelectedDataLeft;
	public static TreeMap<Integer, TreeMap<Integer, FDData>>  timeToFreqToSelectedDataRight;
	public static TreeMap<Long, Harmonic> harmonicIDToHarmonicMono = null;
	public static TreeMap<Long, Harmonic> harmonicIDToHarmonicLeft = null;
	public static TreeMap<Long, Harmonic> harmonicIDToHarmonicRight = null;
	//public static int minHarmonicLength = 1;
	public static double minLogAmplitudeThreshold = 1.0; // used by autoSelect
	public static int minLengthThreshold = 1;
	public static ArrayList<Selection> selections;
	public static Selection.Area selectionArea = Selection.Area.RECTANGLE;
	public static boolean deleteSelected = false;
	public static TreeMap<Integer, Integer> floorAmpToCount;
	public static int xStep = 6; // one digit
	public static int yStep = 9; // one digit;
	public static int leftOffset = xStep * 6; // start of first data cell
	public static int upperOffset = yStep * 6; // start of first data cell
	public static int leftX = 0; // index of freq in leftmost data cell
	public static int upperY = 0; // index of time in uppermost data cell
	public static int timeStepInMillis = FDData.timeStepInMillis; // time in ms = time * timeStepInMillis
	// these are the max/min valued amplitude for the whole file
	// they are used to calculate the color of amplitude values
	public static float maxAmplitude;
	public static float minAmplitude;
	// the maximum summed value at any given time
	public static float maxAmplitudeSum;
	//Freq ranges from minRealFreq to maxRealFreq, it does not start at 0 (Direct Current)
	//minRealFreq = log(minRealFreqInHz) * freqsPerOctave
	//maxRealFreq = log(maxRealFreqInHz) * freqsPerOctave
	//maxScreenFreq = maxRealFreq - minRealFreq
	//(where log is base 2, of course)
	public static int freqsPerOctave = FDData.noteBase;
	public static int minScreenNote;
	public static int maxScreenNote;
	public static int maxScreenFreq;
	// maxTime = length of file in ms / timeStepInMillis
	public static int maxTime;
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
	    harmonicIDToHarmonicMono = new TreeMap<Long, Harmonic>();
	    harmonicIDToHarmonicLeft = new TreeMap<Long, Harmonic>();
	    harmonicIDToHarmonicRight = new TreeMap<Long, Harmonic>();
		timeToFreqsAtMaximaMono = new TreeMap<Integer, TreeSet<Integer>>();
		timeToFreqsAtMaximaLeft = new TreeMap<Integer, TreeSet<Integer>>();
		timeToFreqsAtMaximaRight = new TreeMap<Integer, TreeSet<Integer>>();
		timeToFreqToSelectedDataMono = new TreeMap<Integer, TreeMap<Integer, FDData>>();
		timeToFreqToSelectedDataLeft = new TreeMap<Integer, TreeMap<Integer, FDData>>();
		timeToFreqToSelectedDataRight = new TreeMap<Integer, TreeMap<Integer, FDData>>();
	}
	
	public static float[][] getAmplitudes() {
		if(currentChannel == Channel.STEREO || currentChannel == Channel.MONO) return amplitudesMono;
		if(currentChannel == Channel.LEFT) return amplitudesLeft;
		if(currentChannel == Channel.RIGHT) return amplitudesRight;
		return null;
	}
	
	public static TreeMap<Integer, TreeSet<Integer>> getMaximas() {
		if(currentChannel == Channel.STEREO || currentChannel == Channel.MONO) return timeToFreqsAtMaximaMono;
		if(currentChannel == Channel.LEFT) return timeToFreqsAtMaximaLeft;
		if(currentChannel == Channel.RIGHT) return timeToFreqsAtMaximaRight;
		return null;
	}
	
	public static TreeMap<Integer, TreeMap<Integer, FDData>> getSelectedData() {
		if(currentChannel == Channel.STEREO || currentChannel == Channel.MONO) return timeToFreqToSelectedDataMono;
		if(currentChannel == Channel.LEFT) return timeToFreqToSelectedDataLeft;
		if(currentChannel == Channel.RIGHT) return timeToFreqToSelectedDataRight;
		return null;
	}
	
	public static TreeMap<Long, Harmonic> getHarmonicIDToHarmonic() {
		if(currentChannel == Channel.STEREO || currentChannel == Channel.MONO) return harmonicIDToHarmonicMono;
		if(currentChannel == Channel.LEFT) return harmonicIDToHarmonicLeft;
		if(currentChannel == Channel.RIGHT) return harmonicIDToHarmonicRight;
		return null;
	}
	
	public static float getAmplitude(int time, int freq) {
		float[][] amplitudes = getAmplitudes();
		if(time >= amplitudes.length) return 0.0f;
		if(freq >= amplitudes[0].length) return 0.0f;
		return amplitudes[time][freq];
	}
	
	public static boolean isMaxima(int time, int freq) {
		if(time >= maxTime && freq >= maxScreenFreq) return false;
		TreeMap<Integer, TreeSet<Integer>> timeToFreqsAtMaxima = getMaximas();
		return timeToFreqsAtMaxima.get(time).contains(freq);
	}
	
	public static int noteToFreq(int note) {
		return maxScreenNote - note;
	}
	
	public static int freqToNote(int freq) {
		return maxScreenNote - freq;
	}

	public static Set<Integer> timesWithMaxima() {
		TreeMap<Integer, TreeSet<Integer>> timeToFreqsAtMaxima = getMaximas();
		return timeToFreqsAtMaxima.keySet();
	}
	
	public static TreeSet<Integer> maximasAtTime(int time) {
		TreeMap<Integer, TreeSet<Integer>> timeToFreqsAtMaxima = getMaximas();
		if(!timeToFreqsAtMaxima.containsKey(time)) return new TreeSet<Integer>();
		return timeToFreqsAtMaxima.get(time);
	}
	
	public static boolean isSelected(int time, int freq) {
		TreeMap<Integer, TreeMap<Integer, FDData>> timeToFreqToSelectedData = getSelectedData();
		if(!timeToFreqToSelectedData.containsKey(time)) return false;
		return timeToFreqToSelectedData.get(time).containsKey(freq);
	}
	
	public static FDData getSelected(int time, int freq) {
		TreeMap<Integer, TreeMap<Integer, FDData>> timeToFreqToSelectedData = getSelectedData();
		if(!isSelected(time, freq)) return null;
		return timeToFreqToSelectedData.get(time).get(freq);
	}
	
	public static void newSelection(boolean setInitialData) {
		if(selections.isEmpty()) {
			System.out.println("DFTEditor.newSelection: this function should only be called by a Selection");
			return;
		}
		Selection newSelection = new Selection(selectionArea, deleteSelected);
		if(setInitialData) {
			newSelection.addData(getCurrentSelection().getInputData().get(1));
		}
		selections.add(newSelection);
	}
		
	public static void addSelected(FDData data) {
		TreeMap<Integer, TreeMap<Integer, FDData>> timeToFreqToSelectedData = getSelectedData();
		int time = data.getTime();
		int freq = DFTEditor.noteToFreq(data.getNote());
		if(!timeToFreqToSelectedData.containsKey(time)) {
			timeToFreqToSelectedData.put(time, new TreeMap<Integer, FDData>());
		}
		// overwrite is OK
		timeToFreqToSelectedData.get(time).put(freq, data);
	}
	
	public static void removeSelected(FDData data) {
		TreeMap<Integer, TreeMap<Integer, FDData>> timeToFreqToSelectedData = getSelectedData();
		int time = data.getTime();
		int freq = DFTEditor.noteToFreq(data.getNote());
		if(!timeToFreqToSelectedData.containsKey(time)) return;
		if(!timeToFreqToSelectedData.get(time).containsKey(freq)) return;
		timeToFreqToSelectedData.get(time).remove(freq);
	}

	public static void setSelectionArea(Selection.Area area) {
		DFTEditor.selectionArea = area;
		clearCurrentSelection();
	}
	
	public static void handleSelection(FDData data) {
		if(selections.isEmpty()) {
			selections.add(new Selection(selectionArea, deleteSelected));
		}
		selections.get(selections.size() - 1).addData(data);
		view.repaint();
	}
	
	public static void clearCurrentSelection() {
		if(selections.isEmpty()) return;
		selections.remove(selections.size() - 1);
		selections.add(new Selection(selectionArea, deleteSelected));
	}
	
	public static void undoPreviousSelection() {
		if(selections.size() < 1) return;
		selections.get(selections.size() - 2).undo();
		selections.remove(selections.size() - 2);
		clearCurrentSelection();
		view.repaint();
	}
	
	public static Selection getCurrentSelection() {
		return selections.get(selections.size() - 1);
	}
	
	public static Set<Integer> selectedFreqsAtTime(int time) {
		TreeMap<Integer, TreeMap<Integer, FDData>> timeToFreqToSelectedData = getSelectedData();
		return timeToFreqToSelectedData.get(time).keySet();
	}
	
	public int getTimeAxisWidthInMillis() {
		return view.getTimeAxisWidthInMillis();
	}
	
	public void playSelectedDataInCurrentWindow() {
		new PlayDataInWindow(this, 50, view.getTimeAxisWidthInMillis(), PlayDataInWindow.SynthType.Log);
	}
	
	public void playSelectedDataInCurrentWindowLinear() {
		new PlayDataInWindow(this, 50, view.getTimeAxisWidthInMillis(), PlayDataInWindow.SynthType.Linear);
	}
	
	public void playSelectedDataInCurrentWindowLinearCubicSpline() {
		new PlayDataInWindow(this, 50, view.getTimeAxisWidthInMillis(), PlayDataInWindow.SynthType.LinearCubicSpline);
	}

	public void drawPlayTime(int offsetInMillis, int refreshRateInMillis) {
		view.drawPlayTime(offsetInMillis, refreshRateInMillis);
		refreshView();
	}
	
	public static void refreshView() {
		view.repaint();
	}

	public static long getRandomID() {
		return randomIDGenerator.nextLong();
	}
	
	public static void autoSelect() {
		currentChannel = Channel.MONO;
		autoSelectChannel();
		currentChannel = Channel.RIGHT;
		autoSelectChannel();
		currentChannel = Channel.LEFT;
		autoSelectChannel();
		currentChannel = Channel.STEREO;
	}
	
	public static void autoSelectChannel() {
		// add all maximas
		calculateTimeToFreqsAtMaxima();
		float[][] amplitudes = getAmplitudes();
		TreeMap<Integer, TreeSet<Integer>> timeToFreqsAtMaxima = getMaximas();
		TreeMap<Integer, TreeMap<Integer, FDData>>  timeToFreqToSelectedData = getSelectedData();
		TreeMap<Long, Harmonic> harmonicIDToHarmonic = getHarmonicIDToHarmonic();
		TreeMap<Integer, ArrayList<Integer>> unselect = new TreeMap<Integer, ArrayList<Integer>>();
		for(int time: timeToFreqsAtMaxima.keySet()) {
			for(int freq:  timeToFreqsAtMaxima.get(time)) {
				FDData data = null;
				try {
					data = new FDData(time, freqToNote(freq), amplitudes[time][freq], 1L);
				} catch (Exception e){
					System.out.println("DFTEditor.autoSelect(): unable to create FDData");
				}
				addSelected(data);
			}
		}
		// refreshView();
		// create Harmonics from selected
		SynthTools.createHarmonics(timeToFreqToSelectedData);
		for(int time: timeToFreqToSelectedData.keySet()) {
			for(int freq: timeToFreqToSelectedData.get(time).keySet()) {
				FDData data = timeToFreqToSelectedData.get(time).get(freq);
				long harmonicID = data.getHarmonicID();
				Harmonic harmonic = harmonicIDToHarmonic.get(harmonicID);
				// have to put in unselect to avoid ConcurrentModificationException
				if(!harmonic.isSynthesized()) {
					if(!unselect.containsKey(time)) unselect.put(time, new ArrayList<Integer>());
					unselect.get(time).add(freq);
				}
			}
		}
		// unselect data that is not played
		for(int time: unselect.keySet()) {
			for(int freq: unselect.get(time)) {
				timeToFreqToSelectedData.get(time).remove(freq);
			}
		}
		refreshView();
	}

	// NOTE: maxima test is not performed for freq = 0 and freq = maxFreq
	public static void calculateTimeToFreqsAtMaxima() {
		float[][] amplitudes = getAmplitudes();
		TreeMap<Integer, TreeSet<Integer>> timeToFreqsAtMaxima = getMaximas();
		for(int timeIndex = 0; timeIndex <= maxTime; timeIndex++) {
			TreeSet<Integer> freqsAtMaxima = new TreeSet<Integer>();
			float upperVal = amplitudes[timeIndex][0];
			float centerVal = amplitudes[timeIndex][1]; // should be initialized in for loop
			for(int freqIndex = 2; freqIndex <= maxScreenFreq; freqIndex++) {
				float lowerVal = amplitudes[timeIndex][freqIndex];
				if((centerVal >= upperVal) && (centerVal >= lowerVal)) {
					if(centerVal == 0.0) continue;
					freqsAtMaxima.add(freqIndex - 1);
				}
				upperVal = centerVal;
				centerVal = lowerVal;
			}
			timeToFreqsAtMaxima.put(timeIndex, freqsAtMaxima);
		}
		// collapse maximas at adjacent freqs
		for(int time: timeToFreqsAtMaxima.keySet()) {
			TreeSet<Integer> freqSet = new TreeSet<Integer>();
			// create deep copy of timeToFreqsAtMaximaMono.get(time)
			for(Integer value: timeToFreqsAtMaxima.get(time)) freqSet.add(value);;
			while(!freqSet.isEmpty()) {
				int freq = freqSet.first();
				int startFreq = freq;
				freqSet.remove(startFreq);
				timeToFreqsAtMaxima.get(time).remove(startFreq);
				while(freqSet.contains(freq + 1)) {
					freq++;
					freqSet.remove(freq);
					timeToFreqsAtMaxima.get(time).remove(freq);
				}
				int centerFreq = startFreq + (freq - startFreq) / 2;
				timeToFreqsAtMaxima.get(time).add(centerFreq);
			}
		}
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
	
	public void openFileInDFTEditor() {
        String fileName = FileTools.PromptForFileOpen(view);
        String fileNameTrimmed = fileName.substring(0, fileName.length() - 8); // ".mono5ms"
        FileInput.ReadBinaryFileData(this, fileName, "mono5ms");
        DFTFileInput.ReadSelectedFileData(fileNameTrimmed);
        // Ensure that all previously selected data will be viewable/played
    	minLogAmplitudeThreshold = 1.0; // used by autoSelect
    	SynthTools.refresh = true;
    	autoSelect();
        view.repaint();
	}
	
	public void exportAllFiles() {
        FileOutput.selectedExportAll(this);
        JOptionPane.showMessageDialog(this, "Finished exporting all files");
	}
	
	public void saveSelectedToFile() {
		String fileName = this.getTitle();
		String fileNameTrimmed = fileName.substring(0, fileName.length() - 8); // ".mono5ms"
        FileOutput.OutputSelectedToFile(fileNameTrimmed);
        JOptionPane.showMessageDialog(this, "Finished saving: " + fileNameTrimmed + ".selected");
	}
	
    public DFTEditor() {
    	FileConvert.wavImportAll();
        view = new DFTView();
        view.setBackground(Color.black);
        controller = new DFTController();
        setJMenuBar(createMenuBar());
        add(createNavigationBar(), BorderLayout.PAGE_START);
        view.addMouseListener(controller);
        add(view);
        setSize(1500, 800);
        selections = new ArrayList<Selection>();
        selections.add(new Selection(DFTEditor.selectionArea, deleteSelected));
        randomIDGenerator = new Random();
        openFileInDFTEditor();
        //DFTUtils.testGetConsonantOvertonesBase31();
    }
    
	private static void createAndShowGUI() {
		// Create and set up the window.
		parent = new MultiWindow();
		parent.dftEditorFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		parent.dftEditorFrame.pack();
		parent.dftEditorFrame.setVisible(true);
		//parent.harmonicsEditorFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//parent.harmonicsEditorFrame.pack();
		//parent.harmonicsEditorFrame.setVisible(true);
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

}
