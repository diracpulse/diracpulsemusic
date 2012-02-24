
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.io.*;

public class DFTEditor extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2291799595853726615L;
	public static MultiWindow parent;
	public static DFTView view;
	public static DFTController controller;
	// Swing components
	public static JMenuBar menuBar = null;
	public static JToolBar navigationBar = null;
	
	//public static TreeMap<Integer, TreeMap<Integer, Float>> timeToFreqToAmp;
	private static float[][] amplitudes; // amplitude = amplitudes[time][freq]
	private static TreeMap<Integer, TreeSet<Integer>> timeToFreqsAtMaxima;
	public static TreeMap<Integer, Float> timeToAmpSum;
	public static TreeSet<Integer> ampMaximaTimes;
	public static ArrayList<Integer> timeAtAmpMaximas;
	public static ArrayList<Integer> timeAtAmpMinimas;
	public static TreeMap<Integer, Float> freqToMaxAmp;
	public static TreeMap<Integer, Integer> floorAmpToCount;
	public static int xStep = 6;
	public static int yStep = 9; // one digit;
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
	
	// note: actual freq = maxScreenFreq - freq
	public static float getAmplitude(int time, int freq) {
		if(time < maxTime && freq < maxScreenFreq) return amplitudes[time][freq];
		return 0.0f;
	}
	
	public static int freqToNote(int freq) {
		return maxRealFreq - freq;
	}
	
	public static boolean isMaxima(int time, int freq) {
		return timeToFreqsAtMaxima.get(time).contains(freq);
	}
	
	public static Set<Integer> timesWithMaxima() {
		return timeToFreqsAtMaxima.keySet();
	}
	
	public static TreeSet<Integer> maximasAtTime(int time) {
		return timeToFreqsAtMaxima.get(time);
	}
	
	// This function reads from a binary file
	public void ReadBinaryFileData(String fileName, String type) {
		if(!type.equals("mono5ms")) {
			System.out.println("DFTEditor.ReadBinaryFileData: unsupported format");
			System.exit(0);
		}
		this.setTitle("Loading: " + fileName);
		//timeToFreqToAmp = new TreeMap<Integer, TreeMap<Integer, Float>>();
		floorAmpToCount = new TreeMap<Integer, Integer>();
		ArrayList<Float> matrixVals = new ArrayList<Float>();
		DataInputStream in = null;
		maxAmplitude = 0.0f;
		minAmplitude = 15.0f;
		minRealFreq = freqsPerOctave * 100;
		maxRealFreq = 0;
		maxTime = 0;
		float amp;
	    try {
	    	in = new DataInputStream(new
	                BufferedInputStream(new FileInputStream(new String(fileName))));
		} catch (FileNotFoundException nf) {
			System.out.println("DFTEditor: " + fileName + ".[suffix] not found");
			return;
		}
		try {
			maxRealFreq = in.readInt();
			minRealFreq = in.readInt();
			while(true) {
				amp = in.readFloat();
				matrixVals.add(amp);
				if(amp > maxAmplitude) {
					maxAmplitude = amp;
				}
				if(amp < minAmplitude) {
					minAmplitude = amp;
				}
				int floorAmp = (int) Math.floor(amp);
				int number = 0;
				if(floorAmpToCount.containsKey(floorAmp)) {
					number = floorAmpToCount.get(floorAmp);
				}
				number++;
				floorAmpToCount.put(floorAmp, number);
				// End floopAmp count
			}
		} catch (IOException e) {
			if(e instanceof EOFException) {
				System.out.println("Finished reading from: " + fileName);
			} else {
				System.out.println("DFTEditor: error reading from: " + fileName);
			}
		}
		int matrixValsSize = matrixVals.size();
		maxScreenFreq = maxRealFreq - minRealFreq;
		maxTime = matrixValsSize / (maxScreenFreq + 1);
		amplitudes = new float[maxTime + 1][maxScreenFreq + 1];
		int index = 0;
		for(int time = 0; time < maxTime; time++) {
			for(int freq = 0; freq <= maxScreenFreq; freq++) {
				if(index < matrixValsSize) amplitudes[time][freq] = matrixVals.get(index);
				index++;
			}
		}
		int msfplus1 = maxScreenFreq + 1;
		//System.out.println("maxtrixVals div size: " + matrixValsSize / msfplus1 + "index: " + index / msfplus1);
		//System.out.println("maxtrixVals mod size: " + matrixValsSize % msfplus1 + "index: " + index % msfplus1);
		calculateAmpSum();
		//calculateMaxAmpAtFreq();
		printFloorAmpCount();
		calculateTimeToFreqsAtMaxima();
		this.setTitle(fileName);
	}
	
	// NOTE: maxima test is not performed for freq = 0 and freq = maxFreq
	public void calculateTimeToFreqsAtMaxima() {
		timeToFreqsAtMaxima = new TreeMap<Integer, TreeSet<Integer>>();
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
			// create deep copy of timeToFreqsAtMaxima.get(time)
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
	
	
	// This calculates the sum of all amplitudes at a given time
	public void calculateAmpSum() {
		maxAmplitudeSum = 0.0f;
		timeToAmpSum = new TreeMap<Integer, Float>();
		for(int time = 0; time < maxTime; time++) {
			double dsum = 0.0;
			double dexponent = 1.0;
			timeToAmpSum.put(new Integer(time), new Float(0.0f));
			for(Float f: amplitudes[time]) {
				dexponent = f.doubleValue();
				dsum += Math.pow(2.0, dexponent);
			}
			dsum = Math.log(dsum) / Math.log(2.0);
			if (maxAmplitudeSum < dsum) maxAmplitudeSum = (float) dsum;
			timeToAmpSum.put(new Integer(time), new Float(dsum));
		}
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
        ReadBinaryFileData(fileName, "mono5ms");
        //String fileNameTrimmed = fileName.substring(0, fileName.length() - 4);
        view.repaint();
	}

	public void exportFileInDFTEditor() {
        //String fileName = FileTools.PromptForFileSave(view);
		String fileName = this.getTitle();
		String fileNameTrimmed = fileName.substring(0, fileName.length() - 8); // ".mono5ms"
        FileOutput.OutputMaximasToFile(fileNameTrimmed);
        JOptionPane.showMessageDialog(this, "Finished exporting: " + fileName);
	}
	
	public void exportAllFiles() {
        FileOutput.minmaxExportAll(this);
        JOptionPane.showMessageDialog(this, "Finished exporting all files");
	}
	
    public DFTEditor() {
    	FileConvert.wavImportAll();
        view = new DFTView();
        view.setBackground(Color.black);
        controller = new DFTController(this);
        setJMenuBar(createMenuBar());
        add(createNavigationBar(), BorderLayout.PAGE_START);
        view.addMouseListener(controller);
        controller.setView(view);
        add(view);
        setSize(1500, 800);
        openFileInDFTEditor();
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
