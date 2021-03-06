package main.modules;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import main.Module.ModuleType;
import main.modules.Envelope.EnvelopePoint;

public class EnvelopeEditor extends JPanel implements WindowListener {

	private static final long serialVersionUID = 3138005743637187863L;
	
	Envelope envelope;
	EnvelopeView view;
	EnvelopeController controller;
	JToolBar navigationBar = null;
	
	public void addNavigationButton(String buttonText) {
		JButton button = new JButton(buttonText);
		button.addActionListener((ActionListener) controller);
		navigationBar.add(button);
	}
	
	public JToolBar createNavigationBar() {
		navigationBar = new JToolBar("Navigation Bar");
        addNavigationButton("Reset");
    	return navigationBar;
	}
	
    public EnvelopeEditor(Envelope envelope) {
		super(new BorderLayout());
		this.envelope = envelope;
        view = new EnvelopeView(this);
        view.setBackground(Color.black);
        controller = new EnvelopeController(this);
        add(createNavigationBar(), BorderLayout.PAGE_START);
        view.addMouseListener(controller);
        view.addMouseMotionListener(controller);
        view.setPreferredSize(new Dimension((int) Math.round(Envelope.maxEnvelopeDuration * 1000.0 / getMillisPerPixel()), 600));
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setSize(800, 600);
        add(scrollPane, BorderLayout.CENTER);
        envelope.getParent().viewEnvelopeEditor(this);
    }
    
    public void reset() {
    	envelope.initEnvelopePoints();
    	view.repaint();
    	envelope.parent.refreshData();
    }
    
    public Envelope getEnvelope() {
    	return envelope;
    }
    
    public double getMillisPerPixel() {
    	return 1.0;
    }
    
    public int getControlPointWidth() {
    	return 16;
    }   
    
    public boolean isOverEnvelopePoint(int x, int y) {
    	return false;
    }

    public ArrayList<Rectangle> getControlAreas() {
    	int width = getControlPointWidth();
    	ArrayList<Rectangle> returnVal = new ArrayList<Rectangle>();
    	for(double time: envelope.getEnvelopeTimes()) {
    		EnvelopePoint point = envelope.getEnvelopePoint(time);
    		double amplitude = point.amplitude;
    		int leftX = timeToX(time) - width / 2;
       		int upperY = amplitudeToY(amplitude) - width / 2;
    		returnVal.add(new Rectangle(leftX, upperY, width, width));
    	}
    	return returnVal;
    }
    
    public EnvelopePoint getEnvelopePoint(int x, int y) {
    	int width = getControlPointWidth();
    	for(double time: envelope.getEnvelopeTimes()) {
    		EnvelopePoint point = envelope.getEnvelopePoint(time);
    		double amplitude = point.amplitude;
    		int leftX = timeToX(time) - width / 2;
       		int upperY = amplitudeToY(amplitude) - width / 2;
    		if(new Rectangle(leftX, upperY, width, width).contains(x, y)) return point;
    	}
    	return null;
    }
    
    public int timeToX(double time) {
    	return (int) Math.round(time / (getMillisPerPixel() / 1000.0)) + EnvelopeView.xPadding;
    }
    
    public int amplitudeToY(double amplitude) {
    	return (int) Math.round((view.getHeight() - EnvelopeView.yPadding * 2.0) * (1.0 - amplitude) + EnvelopeView.yPadding);
    }
    
    public double xToTime(int x) {
    	double time = (x - EnvelopeView.xPadding) * (getMillisPerPixel() / 1000.0);
    	if(time < 0.0) time = 0.0;
    	return time;
    }
    
    public double yToAmplitude(int y) {
    	double dHeight = (double) view.getHeight() - EnvelopeView.yPadding * 2;
    	double amplitude = (view.getHeight() - y - EnvelopeView.yPadding) / dHeight;
    	if(amplitude < 0.0) amplitude = 0.0;
    	if(amplitude > 1.0) amplitude = 1.0;
    	return amplitude;
    }

	@Override
	public void windowActivated(WindowEvent arg0) {}

	@Override
	public void windowClosed(WindowEvent arg0) {
	}
	
	@Override
	public void windowClosing(WindowEvent arg0) {
		envelope.getParent().closeEnvelopeEditor(envelope);
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {}

	@Override
	public void windowDeiconified(WindowEvent arg0) {}

	@Override
	public void windowIconified(WindowEvent arg0) {}

	@Override
	public void windowOpened(WindowEvent arg0) {}
    
}
