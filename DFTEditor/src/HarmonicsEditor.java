
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.io.*;

public class HarmonicsEditor extends JFrame {
	
	public static HarmonicsView view;
	public static HarmonicsController controller;
	public static Harmonics harmonics;
	// Swing components
	public static JMenuBar menuBar = null;
	public static JToolBar navigationBar = null;
	
	//public static TreeMap<Integer, TreeMap<Integer, Float>> timeToFreqToAmp;
	private static float[][] amplitudes; // amplitude = amplitudes[time][freq]
	public static TreeMap<Integer, Float> timeToAmpSum;
	public static TreeSet<Integer> ampMaximaTimes;
	public static ArrayList<Integer> timeAtAmpMaximas;
	public static ArrayList<Integer> timeAtAmpMinimas;
	public static TreeMap<Integer, Float> freqToMaxAmp;
	public static TreeMap<Integer, Integer> floorAmpToCount;
	public static int xStep = 6;
	public static int yStep = 9 * 2; // two digits;
	public static int topYStep = 8; // used by DrawUpperTimes
	public static int leftOffset = xStep * 6; // start of first data cell
	public static int upperOffset = topYStep * 8; // start of first data cell
	public static int leftX = 0; // index of freq in leftmost data cell
	public static int upperY = 0; // index of time in uppermost data cell
	public static int timeStepInMillis = 5; // time in ms = time * timeStepInMillis
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
	public static int freqsPerOctave = 31;
	public static int minRealFreq;
	public static int maxRealFreq;
	public static int maxScreenFreq;
	// maxTime = length of file in ms / timeStepInMillis
	public static int maxTime; 
	//these determine how many values per (collapsed) segement
	public static int timeIncrement = 1;
	public static int freqIncrement = 1;
	
	public static float getAmplitude(int time, int freq) {
		if(time < maxTime && freq < maxScreenFreq) return amplitudes[time][freq];
		return 0.0f;
	}
	
	public JMenuBar createMenuBar() {
        HarmonicsAction actionHandler = new HarmonicsAction(this);
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
        addNavigationButton("+500ms");
        addNavigationButton("+1s");
        addNavigationButton("+2s");
        addNavigationButton("+5s");
        addNavigationButton("+10s");
        addNavigationButton("+30s");
    	addNavigationButton("+1min");
        addNavigationButton("-250ms");
        addNavigationButton("-500ms");
        addNavigationButton("-1s");
        addNavigationButton("-2s");
        addNavigationButton("-5s");
        addNavigationButton("-10s");
        addNavigationButton("-30s");
    	addNavigationButton("-1min");
    	return navigationBar;
	}
	
	public void addNavigationButton(String buttonText) {
		JButton button = new JButton(buttonText);
		button.addActionListener(controller);
		navigationBar.add(button);
	}
	
	public void openFileInHarmonicsEditor() {
        String fileName = FileTools.PromptForFileOpen(view);
        String fileNameTrimmed = fileName.substring(0, fileName.length() - 4);
        harmonics = new Harmonics(fileNameTrimmed);
        view.repaint();
	}
	
    public HarmonicsEditor() {
    	FileConvert.wavImport();
        view = new HarmonicsView();
        view.setBackground(Color.black);
        controller = new HarmonicsController(this);
        setJMenuBar(createMenuBar());
        add(createNavigationBar(), BorderLayout.PAGE_START);
        view.addMouseListener(controller);
        controller.setView(view);
        add(view);
        setSize(1500, 800);
        //openFileInHarmonicsEditor();
    }
    
}
