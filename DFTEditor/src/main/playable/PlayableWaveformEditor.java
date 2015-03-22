package main.playable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.Timer;

import main.AudioPlayer;
import main.Module.ModuleType;
import main.PlayDataInWindow.SynthType;
import main.ModuleEditor;
import main.MultiWindow;
import main.Scale;
import main.SynthTools;

public class PlayableWaveformEditor extends JPanel implements ActionListener, AudioSource {

	private static final long serialVersionUID = 3138005743637187863L;
	
	private static AudioFetcher af;
	
	MultiWindow parent;
	PlayableWaveform osc1;
	PlayableWaveformView view;
	PlayableWaveformController controller;
	JToolBar navigationBar = null;
	Timer timer = null;
	long timerCalls = 0;
	private int latencyInMillis = 1;
	public final int framesPerSecond = 50;
	public final int frameLengthInMillis = 1000 / framesPerSecond;
	public final int frameLengthInSamples = (int) Math.round(SynthTools.sampleRate / framesPerSecond);
	double[] loopData = new double[frameLengthInSamples * framesPerSecond * 4];
	int loopPosition = 0;
	
	public void addNavigationButton(String buttonText) {
		JButton button = new JButton(buttonText);
		button.addActionListener((ActionListener) controller);
		navigationBar.add(button);
	}
	
	public JToolBar createNavigationBar() {
        addNavigationButton("Record");
        addNavigationButton("Stop");
        addNavigationButton("Arduino");
    	return navigationBar;
	}
	
    public PlayableWaveformEditor(MultiWindow parent) {
		super(new BorderLayout());
		this.parent = parent;
        view = new PlayableWaveformView(this);
        view.setBackground(Color.black);
        controller = new PlayableWaveformController(this);
        navigationBar = new JToolBar();
        add(createNavigationBar(), BorderLayout.PAGE_START);
        view.addMouseListener(controller);
        view.addMouseMotionListener(controller);
        view.setPreferredSize(new Dimension(800, 600));
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setSize(800, 600);
        add(scrollPane, BorderLayout.CENTER);
        osc1 = new PlayableWaveform(this);
        af = new AudioFetcher(this);
        af.start();
    }
    
    public void mousePressed(int x, int y) {
    	osc1.pointSelected(x, y);
    }
    
    public void mouseReleased(int x, int y) {
    	
    }
    
    public void mouseDragged(int x, int y) {
    	osc1.pointSelected(x, y);
    }

	@Override
	public double[] getNextSamples(int numSamples) {
		return osc1.masterGetSamples(numSamples);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
