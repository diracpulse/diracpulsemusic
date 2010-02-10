
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
	public static boolean fileDataRead = false; 
	
	public static TreeMap<Integer, TreeMap<Integer, Float>> timeToFreqToAmp;
	public static TreeMap<Integer, Float> timeToAmpSum;
	public static TreeMap<Integer, Float> freqToMaxAmp;
	public static TreeMap<Integer, Boolean> isFreqCollapsed;
	public static TreeMap<Integer, Boolean> isTimeCollapsed;
	public static int xStep = 6;
	public static int yStep = 9; // one digit;
	public static int topYStep = 8; // used by DrawUpperTimes
	public static int leftOffset = xStep * 6; // start of first data cell
	public static int upperOffset = topYStep * 5; // start of first data cell
	public static int leftX = 0; // index of freq in leftmost data cell
	public static int upperY = 0; // index of time in uppermost data cell
	public static float timeStep = 0.005f;
	public static float maxAmplitude;
	public static float minAmplitude;
	public static float maxAmplitudeSum;
	public static int freqsPerOctave = 31;
	public static int minFreq;
	public static int maxFreq;
	public static int maxTime;
	public static int dataXDim;
	public static int dataYDim;
	public static int timeCollapse = 4;
	public static int freqCollapse = 4;
	
	public void ReadFileData(String inputFile) {
		timeToFreqToAmp = new TreeMap<Integer, TreeMap<Integer, Float>>();
		TreeMap<Integer, Float> freqToAmp;
		String[] tokens;
		RandomAccessFile file = null;
		String linein = "";
		maxAmplitude = 0.0f;
		minAmplitude = 15.0f;
		minFreq = freqsPerOctave * 100;
		maxFreq = 0;
		maxTime = 0;
		Integer time;
		Integer freq;
		Float amp;
	    try {
			file = new RandomAccessFile("out.txt", "r");
		} catch (FileNotFoundException nf) {
			System.out.println("DFTEditor: " + inputFile + " not found");
			System.exit(0);
		}
		try {
			linein = file.readLine();
			while(linein != null) {
				tokens = linein.split(" ");
				time = new Integer(tokens[0]);
				freq = new Integer(tokens[1]);
				amp = new Float(tokens[2]);
				if(amp.floatValue() > maxAmplitude) {
					maxAmplitude = amp.floatValue();
				}
				if(amp.floatValue() < minAmplitude) {
					minAmplitude = amp.floatValue();
				}
				if(freq.intValue() > maxFreq) {
					maxFreq = freq.intValue();
				}
				if(freq.intValue() < minFreq) {
					minFreq = freq.intValue();
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
				if(maxTime > 10000) break;
			}
			file.close();
		} catch (IOException ie) {
			System.out.println("DFTEditor: error reading from: " + inputFile);
			System.exit(0);
		}
		dataXDim = maxTime;
		dataYDim = maxFreq - minFreq;
		calculateAmpSum();
		calculateMaxAmpAtFreq();
		setAllCollapsed();
		fileDataRead = true;
	}
	
	public void calculateAmpSum() {
		maxAmplitudeSum = 0.0f;
		timeToAmpSum = new TreeMap<Integer, Float>();
		for(int time = 0; time < dataXDim; time++) {
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
	
	public void calculateMaxAmpAtFreq() {
		freqToMaxAmp = new TreeMap<Integer, Float>();
		for(int iFreq = minFreq; iFreq <= maxFreq; iFreq++) {
			freqToMaxAmp.put(new Integer(iFreq), new Float(0.0f));
		}
		TreeMap<Integer, Float> freqToAmp;
		for(int time = 0; time < dataXDim; time++) {
			freqToAmp = timeToFreqToAmp.get(new Integer(time));
			if(freqToAmp == null) continue;
			for(Integer IFreq: freqToAmp.keySet()) {
				if(freqToMaxAmp.get(IFreq) < freqToAmp.get(IFreq)) {
					freqToMaxAmp.put(IFreq, freqToAmp.get(IFreq));
				}
			}
		}
	}
	
	public void setAllCollapsed() {
		isFreqCollapsed = new TreeMap<Integer, Boolean>();
		isTimeCollapsed = new TreeMap<Integer, Boolean>();
		for(int time = 0; time < dataXDim; time += timeCollapse) {
			isTimeCollapsed.put(new Integer(time / timeCollapse), new Boolean(true));
		}
		for(int freq = 0; freq < dataYDim; freq += freqCollapse) {
			isFreqCollapsed.put(new Integer(freq / freqCollapse), new Boolean(true));
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
        ReadFileData("out.txt");
        setSize(1500, 600);
        view = new DFTView();
        controller = new DFTController();
        view.setBackground(Color.black);
        setPreferredSize(new Dimension(928, 762));
        add(view);
        addMouseListener(controller);
        setJMenuBar(createMenuBar());
    }
    
    public static void main(String[] args)
    {
       //GenerateWavelets.printParams();
       DFTEditor frame = new DFTEditor();
       frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       frame.setVisible(true);
    }
}
