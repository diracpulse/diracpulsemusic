package main.modules;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import main.ModuleEditor;
import main.MultiWindow;

public class BasicWaveformEditor extends JPanel {

	private static final long serialVersionUID = 3138005743637187863L;
	
	ArrayList<BasicWaveform> basicWaveforms;
	ArrayList<ControlRect> controlRects;
	BasicWaveformView view;
	BasicWaveformController controller;
	ModuleEditor moduleEditor;
	JToolBar navigationBar = null;
	
	public class ControlRect {
		
		public ControlRect(BasicWaveform basicWaveform) {
			this.basicWaveform = basicWaveform;
		}
		
		Rectangle coarseFreqControl = null;
		Rectangle coarseAmpControl = null;
		Rectangle coarseFMModControl = null;
		Rectangle fineFreqControl = null;
		Rectangle fineAmpControl = null;
		Rectangle fineFMModControl = null;
		BasicWaveform basicWaveform;
	}
	
	public enum AdjustmentType {
		COARSE,
		FINE;
	}
	
	public void addNavigationButton(String buttonText) {
		JButton button = new JButton(buttonText);
		button.addActionListener((ActionListener) controller);
		navigationBar.add(button);
	}
	
	public JToolBar createNavigationBar() {
		navigationBar = new JToolBar("Navigation Bar");
        for(int index = 0; index < moduleEditor.getNumWaveforms(); index++) {
        	addNavigationButton(new Integer(index).toString());
        }
    	return navigationBar;
	}
	
	
    public BasicWaveformEditor(ArrayList<BasicWaveform> basicWaveforms, ModuleEditor moduleEditor) {
		super(new BorderLayout());
		this.moduleEditor = moduleEditor;
		controlRects = new ArrayList<ControlRect>();
		for(BasicWaveform basicWaveform: basicWaveforms) {
			controlRects.add(new ControlRect(basicWaveform));
		}
        view = new BasicWaveformView(this);
        view.setBackground(Color.black);
        controller = new BasicWaveformController(this);
        add(createNavigationBar(), BorderLayout.PAGE_START);
        view.addMouseListener(controller);
        view.addMouseMotionListener(controller);
        view.setPreferredSize(new Dimension(800, 600));
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setSize(800, 600);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    public int amplitudeToXCoarse(double amplitude) {
    	return valueToXCoarse(amplitude, ModuleEditor.minAmplitudeLog2, ModuleEditor.maxAmplitudeLog2);
    }
 
    public double coarseXToAmplitude(int x) {
    	return coarseXToValue(x, ModuleEditor.minAmplitudeLog2, ModuleEditor.maxAmplitudeLog2);
    }
 
    public int freqToXCoarse(double freqInHz) {
    	return valueToXCoarse(freqInHz, ModuleEditor.minFrequencyLog2, Math.floor(ModuleEditor.maxFrequencyLog2));
    }
    
    public double coarseXToFreq(int x) {
    	return coarseXToValue(x, ModuleEditor.minFrequencyLog2, Math.floor(ModuleEditor.maxFrequencyLog2));
    }
 
    public int fmModToXCoarse(double fmMod) {
    	return valueToXCoarse(fmMod, ModuleEditor.minFMModLog2, ModuleEditor.maxFMModLog2);
    }

    public double coarseXToFMMod(int x) {
    	return coarseXToValue(x, ModuleEditor.minFMModLog2, ModuleEditor.maxFMModLog2);
    }
 
    public int valueToXCoarse(double value, double minValue, double maxValue) {
    	double logValue = Math.log(value) / Math.log(2.0);
    	double logRange = maxValue - minValue;
    	double pixelsPerLogValue = view.getWidth() / logRange;
    	return (int) Math.round((logValue - minValue) * pixelsPerLogValue);
    }
    
    public int valueToXFine(double value) {
    	double logValue = Math.log(value) / Math.log(2.0);
    	double logFractionValue = logValue - Math.floor(logValue);
    	return (int) Math.round(view.getWidth() * logFractionValue);
    }
    
    public double coarseXToValue(int x, double minValue, double maxValue) {
    	double logRange = maxValue - minValue;
    	double logValuesPerPixel = logRange / view.getWidth();
    	return Math.pow(2.0, Math.floor(x * logValuesPerPixel + minValue));
    }
    
    public double fineXToValue(int x) {
    	return Math.pow(2.0, (double) x / (double) view.getWidth());
    }
    
    public void pointSelected(int x, int y) {
    	for(ControlRect controlRect: controlRects) {
	    	if(controlRect.coarseFreqControl.contains(x, y)) {
	    		double logVal = Math.log(controlRect.basicWaveform.getFreqInHz()) / Math.log(2.0);
	    		double fractionLogVal = logVal - Math.floor(logVal);
	    		controlRect.basicWaveform.setFreqInHz(coarseXToFreq(x) * Math.pow(2.0, fractionLogVal));
	    		view.repaint();
	       	}
	       	if(controlRect.coarseAmpControl.contains(x, y)) {
	       		double logVal = Math.log(controlRect.basicWaveform.getAmplitude()) / Math.log(2.0);
	    		double fractionLogVal = logVal - Math.floor(logVal);
	    		controlRect.basicWaveform.setAmplitude(coarseXToAmplitude(x) * Math.pow(2.0, fractionLogVal));
	    		view.repaint();
	    	}
	       	if(controlRect.coarseFMModControl.contains(x, y)) {
	       		double logVal = Math.log(controlRect.basicWaveform.getFMMod()) / Math.log(2.0);
	    		double fractionLogVal = logVal - Math.floor(logVal);
	    		controlRect.basicWaveform.setFMMod(coarseXToFMMod(x) * Math.pow(2.0, fractionLogVal));
	    		view.repaint();
	    	}
	       	if(controlRect.fineFreqControl.contains(x, y)) {
	       		double logVal = Math.log(controlRect.basicWaveform.getFreqInHz()) / Math.log(2.0);
	    		double intLogVal = Math.floor(logVal);
	    		controlRect.basicWaveform.setFreqInHz(Math.pow(2.0, intLogVal) * fineXToValue(x));
	    		view.repaint();
	    	}
	       	if(controlRect.fineAmpControl.contains(x, y)) {
	       		double logVal = Math.log(controlRect.basicWaveform.getAmplitude()) / Math.log(2.0);
	    		double intLogVal = Math.floor(logVal);
	    		controlRect.basicWaveform.setAmplitude(Math.pow(2.0, intLogVal) * fineXToValue(x));
	    		view.repaint();
	    	}
	       	if(controlRect.fineFMModControl.contains(x, y)) {
	       		double logVal = Math.log(controlRect.basicWaveform.getFMMod()) / Math.log(2.0);
	    		double intLogVal = Math.floor(logVal);
	    		controlRect.basicWaveform.setFMMod(Math.pow(2.0, intLogVal) * fineXToValue(x));
	    		view.repaint();
	    	}
    	}
    }
    
    public void toggleWaveformDisplayed(int index) {
    	boolean remove = false;
    	int arrayListIndex = 0;
    	for(ControlRect controlRect: controlRects) {
    		if(controlRect.basicWaveform.getID() == index) {
    			remove = true;
    			break;
    		}
    		arrayListIndex++;
    	}
    	if(remove) {
    		controlRects.remove(arrayListIndex);
    	} else {
    		controlRects.add(new ControlRect(moduleEditor.getWaveformByID(index)));
    	}
    	view.repaint();
    }
    
}
