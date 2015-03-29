
package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import main.modules.BasicWaveformController;
import main.modules.BasicWaveformView;
import main.modules.EnvelopeController;
import main.modules.BasicWaveformEditor.ControlRect;

public class SamplesEditor extends JPanel implements WindowListener {

	private static final long serialVersionUID = 2032699787503260431L;
	MultiWindow multiWindow;
	ModuleEditor moduleEditor;
	SamplesView view;
	SamplesController controller;
	JToolBar navigationBar = null;
	
	public enum Channel {
		LEFT,
		RIGHT;
	}
	
	TreeMap<Channel, double[]> channels = new TreeMap<Channel, double[]>();
	double maxTime = 0;
	double minViewTime = 0;
	double maxAmplitude = 0.0;
	
	public double samplesPerPixel = 16;
	public static final int fontSize = 12;
	public static final int xPadding = fontSize * 6;
	public static final int yPadding = fontSize + 6;

	public void addNavigationButton(String buttonText) {
		JButton button = new JButton(buttonText);
		button.addActionListener((ActionListener) controller);
		navigationBar.add(button);
	}
	
	public JToolBar createNavigationBar() {
		navigationBar = new JToolBar("Navigation Bar");
        addNavigationButton("Zoom In");
        addNavigationButton("Zoom Out");
        addNavigationButton("Forward");
        addNavigationButton("Backward");
    	return navigationBar;
	}

	public SamplesEditor(ModuleEditor moduleEditor) {
		super(new BorderLayout());
        view = new SamplesView(this);
        view.setBackground(Color.black);
        view.setPreferredSize(new Dimension(1500, 800));
        controller = new SamplesController(this);
        add(createNavigationBar(), BorderLayout.PAGE_START);
        view.addMouseListener(controller);
        view.addMouseMotionListener(controller);
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setSize(1500, 800);
        add(scrollPane, BorderLayout.CENTER);
        this.multiWindow = moduleEditor.parent;
        this.moduleEditor = moduleEditor;
	}
	
	public void initData(double[] left, double[] right) {
        channels.put(Channel.LEFT, left);
        channels.put(Channel.RIGHT, right);
        initData(Channel.LEFT);
        initData(Channel.RIGHT);
        view.repaint();
	}
	
	private void initData(Channel channel) {
		double[] samples = channels.get(channel);
		if(samples == null) {
        	channels.put(channel, new double[0]);
        	samples = channels.get(channel);
        }
        double channelMaxTime = samples.length / SynthTools.sampleRate;
        if(channelMaxTime > maxTime) maxTime = channelMaxTime;
        double channelMaxAmplitude = 0.0;
        for(int index = 0; index < samples.length; index++) {
        	if(Math.abs(samples[index]) > channelMaxAmplitude) {
        		channelMaxAmplitude = Math.abs(samples[index]);
        	}
        }
        if(channelMaxAmplitude > maxAmplitude) maxAmplitude = channelMaxAmplitude;
	}
	
	public int amplitudeToY(double amplitude) {
		double dy = (maxAmplitude - amplitude) / (maxAmplitude * 2.0);
		return (int) Math.round(dy * (view.getHeight() - yPadding * 2)) + yPadding;
	}
	
	public int yToAmplitude(int y) {
		double dy = (double) (view.getHeight() - y - yPadding)  / (view.getHeight() - yPadding * 2);
		return (int) Math.round((dy - 0.5) * maxAmplitude * 2.0);
	}
	
	public int timeToX(double time) {
		double dx = (time - minViewTime) / (getMaxViewTime() - minViewTime);
		return (int) Math.round(dx * (view.getWidth() - xPadding)) + xPadding;
		//return (int) (Math.random() * view.getWidth());
	}
	
	public double xToTime(int x) {
		double dx = (double) (x - xPadding) / ((double) view.getWidth() - xPadding);
		return (dx - minViewTime) * (getMaxViewTime() - minViewTime);
	}
	
	public double getTimePerPixel() {
		return samplesPerPixel / SynthTools.sampleRate;
	}
	
	public double getMaxViewTime() {
		return minViewTime + view.getWidth() * getTimePerPixel();
	}
	
	public void adjustSamplesPerPixel(double factor) {
		double sppTest = Math.round(samplesPerPixel * factor);
		if(sppTest < 1.0) return;
		if((factor > 1.0) && sppTest > maxTime / view.getWidth()) return;
		samplesPerPixel *= factor;
		view.repaint();
	}
	
	public void adjustMinTime(double factor) {
		double mvtTest = minViewTime + (getTimePerPixel() * view.getWidth()) * factor;
		if(mvtTest < 0) return;
		if((factor > 0) && mvtTest > maxTime) return;
		minViewTime = mvtTest;
		view.repaint();
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		moduleEditor.closeSamplesEditor();
		
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
}
	