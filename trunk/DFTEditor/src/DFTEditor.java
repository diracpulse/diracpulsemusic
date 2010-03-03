
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.io.*;

public class DFTEditor extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2291799595853726615L;
	public static DFTView view;
	public static DFTController controller;
	// used by view.paintComponent
	public static boolean fileDataRead = false; 
	
	public static TreeMap<Integer, TreeMap<Integer, Float>> timeToFreqToAmp;
	public static TreeMap<Integer, Float> timeToAmpSum;
	public static TreeMap<Integer, Float> freqToMaxAmp;
	// see DFTUtils.getMaxValue for how these are used
	public static TreeMap<Integer, Boolean> isFreqCollapsed;
	public static TreeMap<Integer, Boolean> isTimeCollapsed;
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
	//these determine how many values per (collapsed) segement
	public static int timeCollapse = 4;
	public static int freqCollapse = 4;
	
	// This function reads from a binary file
	public void ReadFileData(String fileName) {
		timeToFreqToAmp = new TreeMap<Integer, TreeMap<Integer, Float>>();
		TreeMap<Integer, Float> freqToAmp;
		DataInputStream in = null;
		maxAmplitude = 0.0f;
		minAmplitude = 15.0f;
		minRealFreq = freqsPerOctave * 100;
		maxRealFreq = 0;
		maxTime = 0;
		int time;
		short freq;
		float amp;
	    try {
	    	in = new DataInputStream(new
	                BufferedInputStream(new FileInputStream(new String(fileName + ".mono5ms"))));
		} catch (FileNotFoundException nf) {
			System.out.println("DFTEditor: " + fileName + ".[suffix] not found");
			System.out.println("Attemping to load from text file");
			ReadTextFileData(fileName);
			return;
		}
		try {
			while(true) {
				time = in.readInt();
				freq = in.readShort();
				amp = in.readFloat();
				if(amp > maxAmplitude) {
					maxAmplitude = amp;
				}
				if(amp < minAmplitude) {
					minAmplitude = amp;
				}
				if(freq > maxRealFreq) {
					maxRealFreq = freq;
				}
				if(freq < minRealFreq) {
					minRealFreq = freq;
				}				
				if(time > maxTime) {
					maxTime = time;
				}				
				if (timeToFreqToAmp.containsKey(time)) {
					freqToAmp = timeToFreqToAmp.get(time);
				} else {
					timeToFreqToAmp.put(time, new TreeMap<Integer, Float>());
					freqToAmp = timeToFreqToAmp.get(time);
				}
				freqToAmp.put((int) freq, amp);
			}
		} catch (IOException e) {
			if(e instanceof EOFException) {
				System.out.println("Finished reading from: " + fileName);
			} else {
				System.out.println("DFTEditor: error reading from: " + fileName);
			}
		}
		maxScreenFreq = maxRealFreq - minRealFreq;
		calculateAmpSum();
		calculateMaxAmpAtFreq();
		setAllCollapsed();
		fileDataRead = true;
	}
	
	// This function reads from a (newly created) text file
	// It also creates a binary clone for future use
	public void ReadTextFileData(String fileName) {
		timeToFreqToAmp = new TreeMap<Integer, TreeMap<Integer, Float>>();
		TreeMap<Integer, Float> freqToAmp;
		String[] tokens;
		RandomAccessFile file = null;
		DataOutputStream binaryOut = null;
		String linein = "";
		maxAmplitude = 0.0f;
		minAmplitude = 15.0f;
		minRealFreq = freqsPerOctave * 100;
		maxRealFreq = 0;
		maxTime = 0;
		Integer time;
		Integer freq;
		Float amp;
	    try {
			file = new RandomAccessFile(new String(fileName + ".txt"), "r");
			binaryOut = new DataOutputStream(new
		            BufferedOutputStream(new FileOutputStream(new String(fileName + ".mono5ms"))));
		} catch (FileNotFoundException nf) {
			System.out.println("DFTEditor: " + fileName + ".[suffix] not found");
			System.exit(0);
		}
		try {
			linein = file.readLine();
			while(linein != null) {
				tokens = linein.split(" ");
				time = new Integer(tokens[0]);
				freq = new Integer(tokens[1]);
				amp = new Float(tokens[2]);
				binaryOut.writeInt(time);
				binaryOut.writeShort(freq);
				binaryOut.writeFloat(amp);
				if(amp.floatValue() > maxAmplitude) {
					maxAmplitude = amp.floatValue();
				}
				if(amp.floatValue() < minAmplitude) {
					minAmplitude = amp.floatValue();
				}
				if(freq.intValue() > maxRealFreq) {
					maxRealFreq = freq.intValue();
				}
				if(freq.intValue() < minRealFreq) {
					minRealFreq = freq.intValue();
				}				
				if(time.intValue() > maxTime) {
					maxTime = time.intValue();
				}				
				if (timeToFreqToAmp.containsKey(time)) {
					freqToAmp = timeToFreqToAmp.get(time);
				} else {
					timeToFreqToAmp.put(time, new TreeMap<Integer, Float>());
					freqToAmp = timeToFreqToAmp.get(time);
				}
				freqToAmp.put(freq, amp);
				linein = file.readLine();
			}
			file.close();
			binaryOut.close();
		} catch (IOException ie) {
			System.out.println("DFTEditor: error reading from: " + fileName);
			System.exit(0);
		}
		maxScreenFreq = maxRealFreq - minRealFreq;
		calculateAmpSum();
		calculateMaxAmpAtFreq();
		setAllCollapsed();
		fileDataRead = true;
	}
	
	// This calculates the sum of all amplitudes at a given time
	public void calculateAmpSum() {
		maxAmplitudeSum = 0.0f;
		timeToAmpSum = new TreeMap<Integer, Float>();
		for(int time = 0; time < maxTime; time++) {
			double dsum = 0.0;
			double dexponent = 1.0;
			if(timeToFreqToAmp.get(new Integer(time)) == null) {
				timeToAmpSum.put(new Integer(time), new Float(0.0f));
				continue;
			}
			for(Float f: timeToFreqToAmp.get(new Integer(time)).values()) {
				dexponent = f.doubleValue();
				dsum += Math.pow(2.0, dexponent);
			}
			dsum = Math.log(dsum) / Math.log(2.0);
			if (maxAmplitudeSum < dsum) maxAmplitudeSum = (float) dsum;
			timeToAmpSum.put(new Integer(time), new Float(dsum));
		}
	}
	
	// This calculates the maximum amplitude at a particular frequency
	public void calculateMaxAmpAtFreq() {
		freqToMaxAmp = new TreeMap<Integer, Float>();
		for(int iFreq = minRealFreq; iFreq <= maxRealFreq; iFreq++) {
			freqToMaxAmp.put(new Integer(iFreq), new Float(0.0f));
		}
		TreeMap<Integer, Float> freqToAmp;
		for(int time = 0; time < maxTime; time++) {
			freqToAmp = timeToFreqToAmp.get(new Integer(time));
			if(freqToAmp == null) continue;
			for(Integer IFreq: freqToAmp.keySet()) {
				if(freqToMaxAmp.get(IFreq) < freqToAmp.get(IFreq)) {
					freqToMaxAmp.put(IFreq, freqToAmp.get(IFreq));
				}
			}
		}
	}
	
	// initializes the TreeMaps controlling if displayed data is collapsed
	public void setAllCollapsed() {
		isFreqCollapsed = new TreeMap<Integer, Boolean>();
		isTimeCollapsed = new TreeMap<Integer, Boolean>();
		// make sure we don't run out at the end
		int endTime = maxTime + timeCollapse + 1;
		int endScreenFreq = maxScreenFreq + freqCollapse + 1;
		for(int time = 0; time < endTime; time += timeCollapse) {
			isTimeCollapsed.put(new Integer(time / timeCollapse), new Boolean(true));
		}
		for(int screenFreq = 0; screenFreq <= endScreenFreq; screenFreq += freqCollapse) {
			isFreqCollapsed.put(new Integer(screenFreq / freqCollapse), new Boolean(true));
		}
	}

	public JMenuBar createMenuBar() {
	    JMenuBar menuBar;
        JMenu menu;
        JMenuItem menuItem;
        JButton button;
        
        //Create the menu bar.
        menuBar = new JMenuBar();

        //Build the first menu.
        menu = new JMenu("File");
        menuBar.add(menu);
        
        menuItem = new JMenuItem("Open");
        menuItem.addActionListener(controller);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Print Params");
        menuItem.addActionListener(controller);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Save Params");
        menuItem.addActionListener(controller);
        menu.add(menuItem);        
        
        menuItem = new JMenuItem("Exit");
        menuItem.addActionListener(controller);
        menu.add(menuItem);
 
        button = new JButton("F+1");
        button.addActionListener(controller);
        menuBar.add(button);
        button = new JButton("F+6");
        button.addActionListener(controller);
        menuBar.add(button);
        button = new JButton("F+31");
        button.addActionListener(controller);
        menuBar.add(button);       
        button = new JButton("T+1");
        button.addActionListener(controller);
        menuBar.add(button);
        button = new JButton("T+10");
        button.addActionListener(controller);
        menuBar.add(button);
        button = new JButton("T+100");
        button.addActionListener(controller);
        menuBar.add(button);        
        button = new JButton("F-1");
        button.addActionListener(controller);
        menuBar.add(button);
        button = new JButton("F-6");
        button.addActionListener(controller);
        menuBar.add(button);
        button = new JButton("F-31");
        button.addActionListener(controller);
        menuBar.add(button);
        button = new JButton("T-1");
        button.addActionListener(controller);
        menuBar.add(button);
        button = new JButton("T-10");
        button.addActionListener(controller);
        menuBar.add(button);       
        button = new JButton("T-100");
        button.addActionListener(controller);
        menuBar.add(button);             
        return menuBar;

    }
	
    public DFTEditor() {
        ReadFileData("out");
        view = new DFTView();
        view.setBackground(Color.black);
        controller = new DFTController();
        setJMenuBar(createMenuBar());
        view.addMouseListener(controller);
        controller.setView(view);
        add(view);
        setSize(1500, 800);
    }
    
    public static void main(String[] args)
    {
       //GenerateWavelets.printParams();
       DFTEditor frame = new DFTEditor();
       frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       frame.setVisible(true);
    }
}
