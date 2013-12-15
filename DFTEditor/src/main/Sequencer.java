package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

public class Sequencer extends JPanel {

	private static final long serialVersionUID = 3138005743637187863L;
	
	MultiWindow parent;
	SequencerView view;
	SequencerController controller;
	ArrayList<ModuleEditor> moduleEditors = null;
	public JScrollPane scrollPane;
	private JToolBar navigationBar = null;
	public static int noteBase = 53;
	public static double minFreq = 256.0;
	public static int noteHeight = 13;
	public static int numPercussion = 3;
	public static double bpm = 120;
	public static int maxBeats = 20;
	public static int pixelsPerBeat = 128;
	public static int totalPixels = maxBeats * pixelsPerBeat;
	public static int divisionsPerBeat = 8;
	public static int pixelsPerDivision = pixelsPerBeat / divisionsPerBeat;
	public static double secondsPerBeat = 60.0 / bpm;
	public static double maxTimeInSeconds = maxBeats * secondsPerBeat;
	public static int leftDigits = 9;
	public static int scrollableWidth = totalPixels + SequencerUtils.digitWidth * leftDigits;
	public static double secondsPerPixel = secondsPerBeat / pixelsPerBeat;
	public static int scrollableHeight = (noteBase + 3) * noteHeight;
	public ArrayList<double[]> freqRatiosAtTime;
	public ArrayList<MultiWindow.ModuleEditorInfo> moduleInfo;
	public int currentModuleIndex = 0;
	
	public void addNavigationButton(String buttonText) {
		JButton button = new JButton(buttonText);
		button.addActionListener((ActionListener) controller);
		navigationBar.add(button);
	}
	
	public JToolBar createNavigationBar() {
		navigationBar = new JToolBar("Navigation Bar");
        // Create Navigation Buttons
        addNavigationButton("Play");
        addNavigationButton("DFT");
        addNavigationButton("Load");
        addNavigationButton("Save");
        addNavigationButton("Get Module");
    	return navigationBar;
	}

	
    public Sequencer(MultiWindow parent) {
		super(new BorderLayout());
		this.parent = parent;
        view = new SequencerView(this);
        view.setBackground(Color.black);
        controller = new SequencerController(this);
        add(createNavigationBar(), BorderLayout.PAGE_START);
        view.addMouseListener(controller);
        view.addMouseMotionListener(controller);
        view.setPreferredSize(new Dimension(scrollableWidth, scrollableHeight));
        scrollPane = new JScrollPane(view);
        scrollPane.setSize(800, 600);
        add(scrollPane, BorderLayout.CENTER);
        freqRatiosAtTime = new ArrayList<double[]>();
        moduleInfo = new ArrayList<MultiWindow.ModuleEditorInfo>();
        for(MultiWindow.ModuleEditorInfo info: parent.moduleEditorInfo) {
        	freqRatiosAtTime.add(new double[maxBeats * pixelsPerBeat]);
        	double[] freqRatioAtTime = freqRatiosAtTime.get(freqRatiosAtTime.size() - 1);
        	for(int time = 0; time < freqRatioAtTime.length; time++) {
        		freqRatioAtTime[time] = -1.0;
        	}
        	moduleInfo.add(info);
        }
    }

}
