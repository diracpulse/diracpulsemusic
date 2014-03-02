package main.modules;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import main.modules.Envelope.EnvelopePoint;

public class EnvelopeEditor extends JPanel {

	private static final long serialVersionUID = 3138005743637187863L;
	
	Envelope envelope;
	EnvelopeView view;
	EnvelopeController controller;
	
    public EnvelopeEditor(Envelope envelope) {
		super(new BorderLayout());
		this.envelope = envelope;
        view = new EnvelopeView(this);
        view.setBackground(Color.black);
        controller = new EnvelopeController(this);
        view.addMouseListener(controller);
        view.addMouseMotionListener(controller);
        view.setPreferredSize(new Dimension((int) Math.round(Envelope.maxEnvelopeDuration * 1000.0 / getMillisPerPixel()), 600));
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setSize(800, 600);
        add(scrollPane, BorderLayout.CENTER);
        envelope.getMultiWindow().newWindow(this);
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
    	return (int) Math.round(time / (getMillisPerPixel() / 1000.0));
    }
    
    public int amplitudeToY(double amplitude) {
    	return (int) Math.round(view.getHeight() * (1.0 - amplitude));
    }
    
    public double xToTime(int x) {
    	return x * (getMillisPerPixel() / 1000.0);
    }
    
    public double yToAmplitude(int y) {
    	double dHeight = (double) view.getHeight();
    	return (dHeight - y) / dHeight;
    }
    
}
