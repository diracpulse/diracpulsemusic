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

public class PlayableWaveformEditor extends JPanel implements ActionListener {

	private static final long serialVersionUID = 3138005743637187863L;
	
	MultiWindow parent;
	PlayableWaveform osc1;
	PlayableWaveformView view;
	PlayableWaveformController controller;
	JToolBar navigationBar = null;
	Timer timer = null;
	volatile long prevTime = 0; 

	public void addNavigationButton(String buttonText) {
		JButton button = new JButton(buttonText);
		button.addActionListener((ActionListener) controller);
		navigationBar.add(button);
	}
	
	public JToolBar createNavigationBar() {
        addNavigationButton("Record");
        addNavigationButton("Stop");
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
    }
    
    public void pointSelected(int x, int y) {
    	osc1.pointSelected(x, y);
    }
    
    public void record() {
    	int latency = 0;
    	// let JIT Compiler Optimize
    	for(int index = 0; index < 10; index++) {
    		latency += getLatency();
    	}
    	latency = 0;
    	for(int index = 0; index < 10; index++) {
    		latency += getLatency();
    	}
    	latency = (int) Math.round(latency / 10.0);
    	if(latency == 0) latency = 1;
    	System.out.println(latency);
		timer = new Timer(20 - latency, this);
        timer.setInitialDelay(0);
		getAudioData();
		AudioPlayer.addToLine();
        timer.start();
    }
    
    public void stop() {
    	timer.stop();
    }

	@Override
	public synchronized void actionPerformed(ActionEvent arg0) {
		getAudioData();
		AudioPlayer.addToLine();
	}
	
	public void getAudioData() {
		double[] mono = osc1.masterGetSamples(new double[882]);
		AudioPlayer.getAudioBytes(mono, 1.0);
	}
	
	public int getLatency() {
		long startTime = System.currentTimeMillis();
		getAudioData();
		return (int) (System.currentTimeMillis() - startTime);
	}
}
