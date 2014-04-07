package main.modules;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import main.Module.ModuleType;
import main.ModuleEditor;
import main.MultiWindow;
import main.Scale;

public class BasicWaveformEditor extends JPanel implements WindowListener {

	private static final long serialVersionUID = 3138005743637187863L;
	
	ArrayList<BasicWaveform> basicWaveforms;
	TreeMap<Integer, ArrayList<ControlRect>> controlRects;
	BasicWaveformView view;
	BasicWaveformController controller;
	ModuleEditor moduleEditor;
	JToolBar navigationBar = null;
	Random random = new Random();
	double logAmplitudeStandardDeviation = 1.0;
	double logFrequencyStandardDeviation = 2.0;
	
	public enum ControlType {
		FREQUENCY,
		AMPLITUDE,
	}
	
	public class ControlRect {
		
		public double minValue;
		public double maxValue;
		public double range;
		public double steps;
		public Rectangle coarse = null;
		public Rectangle fine = null;
		BasicWaveform basicWaveform = null;
		ControlType controlType = null;
		
		public ControlRect(BasicWaveform basicWaveform, ControlType controlType) {
			this.basicWaveform = basicWaveform;
			this.controlType = controlType;
			switch(controlType) {
			case FREQUENCY:
				this.minValue = ModuleEditor.minFrequencyLog2;
				this.maxValue = Math.floor(ModuleEditor.maxFrequencyLog2);
				this.range = maxValue - minValue;
				this.steps = 53.0;
				this.controlType = controlType;
				return;
			case AMPLITUDE:
				this.minValue = ModuleEditor.minAmplitudeLog2;
				this.maxValue = ModuleEditor.maxAmplitudeLog2;
				this.range = maxValue - minValue;
				this.steps = 64.0;
				return;
			}
		}
		
		public ControlType getControlType() {
			return controlType;
		}
		
		public int getWaveformID() {
			return basicWaveform.getTypeID();
		}
		
		public String getIDString() {
			switch(controlType) {
			case FREQUENCY:
				return "Basic Waveform: " + getWaveformID() + " | Frequency = " + getValue();
			case AMPLITUDE:
				return "Basic Waveform: " + getWaveformID() + " | Amplitude = " + getValue();
		}
		return null;
		}
		
		public double getValue() {
			switch(controlType) {
			case FREQUENCY:
				return basicWaveform.getFreqInHz();
			case AMPLITUDE:
				return basicWaveform.getAmplitude();
			}
			return -1;
		}
		
		public double getLogValue() {
			switch(controlType) {
			case FREQUENCY:
				return Math.log(basicWaveform.getFreqInHz()) / Math.log(2.0);
			case AMPLITUDE:
				return Math.log(basicWaveform.getAmplitude()) / Math.log(2.0);
			}
			return -1;
		}
		
		public void setToRandomValue(ControlType inputType, double randomness) {
			boolean inBounds = false;
			switch(controlType) {
			case FREQUENCY:
				if(inputType != ControlType.FREQUENCY) return;
				while(!inBounds) {
					double freqInHzLog2 = Math.log(basicWaveform.getFreqInHz()) / Math.log(2.0);
					if(randomness < 0.0) {
						freqInHzLog2 = random.nextGaussian() * logFrequencyStandardDeviation + Math.log(ModuleEditor.defaultOctave) / Math.log(2.0);
					} else {
						freqInHzLog2 += random.nextGaussian() * randomness; 
					}
	    			inBounds = basicWaveform.setFreqInHz(Math.pow(2.0, Math.round(freqInHzLog2 * steps) / steps));
				}
			case AMPLITUDE:
				if(inputType != ControlType.AMPLITUDE) return;
		   		while(!inBounds) {
		   			double logAmplitude = Math.log(basicWaveform.getAmplitude()) / Math.log(2.0);
		   			if(randomness < 0.0) {
		   				logAmplitude = random.nextGaussian() * logAmplitudeStandardDeviation;
		   			} else {
		   				logAmplitude += random.nextGaussian() * randomness;
		   			}
 	    			inBounds = basicWaveform.setAmplitude(Math.pow(2.0, Math.round(logAmplitude * steps) / steps));
	    		}
			}
		}
		
		public void courseXToSetValue(int x) {
			double value = coarseXToValue(x);
			switch(controlType) {
			case FREQUENCY:
				basicWaveform.setFreqInHz(value);
				break;
			case AMPLITUDE:
				basicWaveform.setAmplitude(value);
				break;
			}
		}
		
		public void fineXToSetValue(int x) {
			double value = fineXToValue(x);
			switch(controlType) {
			case FREQUENCY:
				basicWaveform.setFreqInHz(value);
				break;
			case AMPLITUDE:
				basicWaveform.setAmplitude(value);
				break;
			}
		}
		
	    public int getXCoarse() {
			double value = 0.0;
			switch(controlType) {
			case FREQUENCY:
				value = basicWaveform.getFreqInHz();
				break;
			case AMPLITUDE:
				value = basicWaveform.getAmplitude();
				break;
			}
	    	double logValue = Math.floor(Math.log(value) / Math.log(2.0));
	    	double pixelsPerLogValue = (view.getWidth() - (2 * BasicWaveformView.xPadding)) / range;
	    	return (int) Math.round((logValue - minValue) * pixelsPerLogValue + BasicWaveformView.xPadding);
	    }
	    
	    public int getXFine() {
			double value = 0.0;
			switch(controlType) {
			case FREQUENCY:
				value = basicWaveform.getFreqInHz();
				break;
			case AMPLITUDE:
				value = basicWaveform.getAmplitude();
				break;
			}
	    	double logValue = Math.log(value) / Math.log(2.0);
	    	double logFractionValue = Math.round((logValue - Math.floor(logValue)) * steps) / steps;
	    	return (int) Math.round((view.getWidth() - (2 * BasicWaveformView.xPadding)) * logFractionValue + BasicWaveformView.xPadding);
	    }
	    
	    private double coarseXToValue(int x) {
	    	if(x < BasicWaveformView.xPadding) x = BasicWaveformView.xPadding;
	    	if(x > view.getWidth() - BasicWaveformView.xPadding) x = view.getWidth() - BasicWaveformView.xPadding;
	    	double fractionValue = getLogValue() - Math.floor(getLogValue());
	    	double logValuesPerPixel = range / (view.getWidth() - (2 * BasicWaveformView.xPadding));
	    	double intValue = (x - BasicWaveformView.xPadding) * logValuesPerPixel + minValue;
	    	intValue = Math.floor(intValue);
	    	return Math.pow(2.0, intValue + fractionValue);
	    }
	    
	    private double fineXToValue(int x) {
	       	if(x < BasicWaveformView.xPadding) x = BasicWaveformView.xPadding;
	    	if(x > view.getWidth() - BasicWaveformView.xPadding) x = view.getWidth() - BasicWaveformView.xPadding;
	    	double intValue = Math.floor(getLogValue());
	    	double fractionValue = (x - BasicWaveformView.xPadding) / (double) (view.getWidth() - 2 * BasicWaveformView.xPadding);
	    	fractionValue = Math.round(fractionValue * steps) / steps;
	    	if(fractionValue == 1.0) fractionValue = (steps - 1.0) / steps;
	    	return Math.pow(2.0, intValue + fractionValue);
	    }
	    
		
	}

	public void addNavigationButton(String buttonText) {
		JButton button = new JButton(buttonText);
		button.addActionListener((ActionListener) controller);
		navigationBar.add(button);
	}
	
	public JToolBar createNavigationBar() {
		navigationBar = new JToolBar("Navigation Bar");
        for(int index = 0; index < moduleEditor.getNumberOfModuleType(ModuleType.BASICWAVEFORM); index++) {
        	addNavigationButton(new Integer(index).toString());
        }
        addNavigationButton("Reset");
        addNavigationButton("Round");
        addNavigationButton("Random");
        addNavigationButton("Random .5");
        addNavigationButton("Random Amp");
        addNavigationButton("Random Freq");
        addNavigationButton("Random Amp .5");
        addNavigationButton("Random Freq .5");
    	return navigationBar;
	}
	
    public BasicWaveformEditor(ModuleEditor moduleEditor) {
		super(new BorderLayout());
		this.moduleEditor = moduleEditor;
		controlRects = new TreeMap<Integer, ArrayList<ControlRect>>();
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
 
    public void pointSelected(int x, int y) {
    	for(ArrayList<ControlRect> controlArray: controlRects.values())
    	for(ControlRect controlRect: controlArray) {
	    	if(controlRect.coarse.contains(x, y)) {
	    		controlRect.courseXToSetValue(x);
	    		view.repaint();
	       	}
	    	if(controlRect.fine.contains(x, y)) {
	    		controlRect.fineXToSetValue(x);
	    		view.repaint();
	       	}
    	}
    }
    
    public void toggleWaveformDisplayed(int index) {
    	boolean remove = false;
    	if(controlRects.containsKey(index)) {
    		controlRects.remove(index);
    	} else {
    		controlRects.put(index, new ArrayList<ControlRect>());
    		BasicWaveform basicWaveform = (BasicWaveform) moduleEditor.getModuleFromTypeID(index, ModuleType.BASICWAVEFORM);
    		controlRects.get(index).add(new ControlRect(basicWaveform, ControlType.FREQUENCY));
    		controlRects.get(index).add(new ControlRect(basicWaveform, ControlType.AMPLITUDE));
    	}
    	view.repaint();
    }
    
    public void randomize() {
       	for(ArrayList<ControlRect> controlArray: controlRects.values()) {
	    	for(ControlRect controlRect: controlArray) {
	    		controlRect.setToRandomValue(ControlType.AMPLITUDE, -1.0);
	    		controlRect.setToRandomValue(ControlType.FREQUENCY, -1.0);
	    	}
       	}
    	view.repaint();
    	moduleEditor.refreshData();
    }
    
    public void reset() {
       	for(ArrayList<ControlRect> controlArray: controlRects.values()) {
	    	for(ControlRect controlRect: controlArray) {
	    		controlRect.basicWaveform.setAmplitude(1.0);
	    		controlRect.basicWaveform.setFreqInHz(ModuleEditor.defaultOctave);
	    	}
       	}
    	view.repaint();
    	moduleEditor.refreshData();
    }
    
    public void round() {
       	for(ArrayList<ControlRect> controlArray: controlRects.values()) {
	    	for(ControlRect controlRect: controlArray) {
	    		double amplitude = controlRect.basicWaveform.getAmplitude();
	    		controlRect.basicWaveform.setAmplitude(Math.pow(2.0, Math.round(Math.log(amplitude) / Math.log(2.0))));
	    		double frequency = controlRect.basicWaveform.getFreqInHz();
	    		controlRect.basicWaveform.setFreqInHz(Math.pow(2.0, Math.round(Math.log(frequency) / Math.log(2.0))));
	    	}
       	}
    	view.repaint();
    	moduleEditor.refreshData();
    }
    
    public void randomize(double random) {
       	for(ArrayList<ControlRect> controlArray: controlRects.values()) {
	    	for(ControlRect controlRect: controlArray) {
	    		controlRect.setToRandomValue(ControlType.AMPLITUDE, random);
	    		controlRect.setToRandomValue(ControlType.FREQUENCY, random);
	    	}
       	}
    	view.repaint();
    	moduleEditor.refreshData();
    }
    
    public void randomize(ControlType inputType) {
       	for(ArrayList<ControlRect> controlArray: controlRects.values()) {
	    	for(ControlRect controlRect: controlArray) {
	    		controlRect.setToRandomValue(inputType, -1.0);
	    	}
       	}
    	view.repaint();
    	moduleEditor.refreshData();
    }
    
    public void randomize(ControlType inputType, double random) {
       	for(ArrayList<ControlRect> controlArray: controlRects.values()) {
	    	for(ControlRect controlRect: controlArray) {
	    		controlRect.setToRandomValue(inputType, random);
	    	}
       	}
    	view.repaint();
    	moduleEditor.refreshData();
    }
    
    
    
	@Override
	public void windowActivated(WindowEvent arg0) {}

	@Override
	public void windowClosed(WindowEvent arg0) {
	}
	
	@Override
	public void windowClosing(WindowEvent arg0) {
		moduleEditor.closeBasicWaveformEditor();
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
