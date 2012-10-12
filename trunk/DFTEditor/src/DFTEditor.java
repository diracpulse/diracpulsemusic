
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
	public static float[][] amplitudesMono; // amplitude = amplitudes[time][freq]
	public static float[][] amplitudesLeft; // amplitude = amplitudes[time][freq]
	public static float[][] amplitudesRight; // amplitude = amplitudes[time][freq]
	public static TreeMap<Integer, TreeSet<Integer>> timeToFreqsAtMaximaMono;
	public static TreeMap<Integer, TreeSet<Integer>> timeToFreqsAtMaximaLeft;
	public static TreeMap<Integer, TreeSet<Integer>> timeToFreqsAtMaximaRight;
	public static TreeMap<Long, Harmonic> harmonicIDToHarmonicMono = null;
	public static TreeMap<Long, Harmonic> harmonicIDToHarmonicLeft = null;
	public static TreeMap<Long, Harmonic> harmonicIDToHarmonicRight = null;
	//public static int minHarmonicLength = 1;
	public static double minLogAmplitudeThreshold = 1.0; // used by autoSelect
	public static int minLengthThreshold = 1;
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
	public static float maxAmplitude = 17.0f;
	public static float minAmplitude = 0.0f;
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

	public int getTimeAxisWidthInMillis() {
		return view.getTimeAxisWidthInMillis();
	}
	
	public void playDataInCurrentWindow() {
		new PlayDataInWindow(this, 50, view.getTimeAxisWidthInMillis());
	}
	
	public void drawPlayTime(int offsetInMillis, int refreshRateInMillis) {
		view.drawPlayTime(offsetInMillis, refreshRateInMillis);
		refreshView();
	}
	
	public static void blankScreen() {
		view.dftInProgress = true;
		view.paintImmediately(0, 0, view.getWidth(), view.getHeight());
		view.dftInProgress = false;
	}
	
	public static void refreshView() {
		view.repaint();
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
        if(prompt || dftFileName == null) dftFileName = FileTools.PromptForFileOpenWAV(view);
        DFT.FileDFTMatrix(dftFileName);
    	SynthTools.refresh = true;
    	SynthTools.createHarmonics();
        parent.graphEditorFrame.addHarmonicsToGraphEditor(harmonicIDToHarmonicMono);
        parent.graphEditorFrame.view.repaint();
        view.repaint();
	}
	
	public void exportAllFiles() {
        FileOutput.harmonicsExportAll(this);
        JOptionPane.showMessageDialog(this, "Finished exporting all files");
	}
	
	public void saveHarmonicsToFile() {
		String fileName = this.getTitle();
		String fileNameTrimmed = fileName.substring(0, fileName.length() - 8); // ".mono5ms"
        FileOutput.OutputHarmonicsToFile(fileNameTrimmed);
        JOptionPane.showMessageDialog(this, "Finished saving: " + fileNameTrimmed + ".selected");
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
        //openFileInDFTEditor();
        //FileDFT(true);
        //parent.graphEditorFrame.addHarmonicsToGraphEditor(harmonicIDToHarmonicMono);
        //parent.graphEditorFrame.view.repaint();
        //view.repaint();
        //DFTUtils.testGetConsonantOvertonesBase31();
        newFileData();
    }
    
	private static void createAndShowGUI() {
		// Create and set up the window.
		parent = new MultiWindow();
		parent.dftEditorFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		parent.dftEditorFrame.pack();
		parent.dftEditorFrame.setVisible(true);
		parent.graphEditorFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		parent.graphEditorFrame.pack();
		parent.graphEditorFrame.setVisible(true);
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
