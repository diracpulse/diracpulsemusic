package main.modules;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
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

import main.ModuleEditor;

public class BasicWaveformEditor extends JPanel {

	private static final long serialVersionUID = 3138005743637187863L;
	
	BasicWaveform basicWaveform;
	BasicWaveformView view;
	BasicWaveformController controller;
	
	Rectangle freqControl = null;
	Rectangle ampControl = null;
	Rectangle fmModControl = null;
	
    public BasicWaveformEditor(BasicWaveform basicWaveform) {
		super(new BorderLayout());
		this.basicWaveform = basicWaveform;
        view = new BasicWaveformView(this);
        view.setBackground(Color.black);
        controller = new BasicWaveformController(this);
        view.addMouseListener(controller);
        view.addMouseMotionListener(controller);
        view.setPreferredSize(new Dimension(800, 600));
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setSize(800, 600);
        add(scrollPane, BorderLayout.CENTER);
        basicWaveform.getMultiWindow().newWindow(this);
    }

    public int amplitudeToX(double amplitude) {
    	double logRange = (ModuleEditor.maxAmplitudeIn_dB - ModuleEditor.minAmplitudeIn_dB);
    	double xVal = (Math.log(amplitude) / Math.log(10) * 20.0 - ModuleEditor.minAmplitudeIn_dB) / logRange;
    	return (int) Math.round(xVal * view.getWidth());
    }
    
    public double xToAmplitude(int x) {
    	double logRange = (ModuleEditor.maxAmplitudeIn_dB - ModuleEditor.minAmplitudeIn_dB);
    	double logVal = ((double) x / view.getWidth()) * logRange + ModuleEditor.minAmplitudeIn_dB;
    	return Math.pow(10.0, logVal / 20);
    }
    
    public int fmModToX(double amplitude) {
    	double logRange = (ModuleEditor.maxFMModIn_dB - ModuleEditor.minFMModIn_dB);
    	double xVal = (Math.log(amplitude) / Math.log(10) * 20.0 - ModuleEditor.minFMModIn_dB) / logRange;
    	return (int) Math.round(xVal * view.getWidth());
    }
    
    public double xToFMMod(int x) {
    	double logRange = (ModuleEditor.maxFMModIn_dB - ModuleEditor.minFMModIn_dB);
    	double logVal = ((double) x / view.getWidth()) * logRange + ModuleEditor.minFMModIn_dB;
    	return Math.pow(10.0, logVal / 20);
    }
    
    public int freqInHzToX(double freqInHz) {
    	double logMinFreq = Math.log(ModuleEditor.minFrequency) / Math.log(2.0);
    	double logMaxFreq = Math.log(ModuleEditor.maxFrequency) / Math.log(2.0);
    	double logFreqRange = logMaxFreq - logMinFreq;
    	double xVal = (Math.log(freqInHz) / Math.log(2.0) - logMinFreq) / logFreqRange;
    	return (int) Math.round(xVal * view.getWidth());
    }
    
    public double xToFreqInHz(int x) {
    	double logMinFreq = Math.log(ModuleEditor.minFrequency) / Math.log(2.0);
    	double logMaxFreq = Math.log(ModuleEditor.maxFrequency) / Math.log(2.0);
    	double logFreqRange = logMaxFreq - logMinFreq;
    	double logVal = ((double) x / view.getWidth()) * logFreqRange + logMinFreq;
    	return Math.pow(2.0, logVal);
    }
    
    public void pointSelected(int x, int y) {
    	if(freqControl.contains(x, y)) {
    		basicWaveform.setFreqInHz(xToFreqInHz(x));
    		view.repaint();
    		basicWaveform.getMultiWindow().sequencerFrame.repaint();
    	}
       	if(ampControl.contains(x, y)) {
    		basicWaveform.setAmplitude(xToAmplitude(x));
    		view.repaint();
    		basicWaveform.getMultiWindow().sequencerFrame.repaint();
    	}
       	if(fmModControl.contains(x, y)) {
    		basicWaveform.setFMMod(xToFMMod(x));
    		view.repaint();
    		basicWaveform.getMultiWindow().sequencerFrame.repaint();
    	}
    }
    
}
