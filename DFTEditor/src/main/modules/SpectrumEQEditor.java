
package main.modules;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import main.AudioPlayer;
import main.FFT;
import main.Filter;
import main.ModuleEditor;
import main.MultiWindow;
import main.SynthTools;
import main.modules.BasicWaveformController;
import main.modules.BasicWaveformView;
import main.modules.BasicWaveformEditor.ControlRect;
import main.modules.Envelope.EnvelopePoint;
import main.modules.SpectrumEQ.EQBand;

public class SpectrumEQEditor extends JPanel implements WindowListener {
	
	MultiWindow multiWindow;
	ModuleEditor moduleEditor;
	SpectrumEQView view;
	SpectrumEQController controller;
	JToolBar navigationBar = null;
	SpectrumEQ parent;
	double alpha = 5.0;

	TreeMap<Double, TreeMap<Double, Double>> freqToTimeToAmplitude = new TreeMap<Double, TreeMap<Double, Double>>();
	double[] ifft;
	double minTime = 0;
	double maxTime = 0;
	double minFreq = Math.log((SynthTools.sampleRate / 2048.0)) / Math.log(2.0);
	double maxFreq = Math.log((SynthTools.sampleRate / 2.0)) / Math.log(2.0);
	double absoluteMaxAmplitude = Math.log(Short.MAX_VALUE) / Math.log(2.0);
	double fftMaxAmplitude = 0.0;
	double minAmplitude = 0.0;
	
	public static final int fontSize = 12;
	public static final int xPadding = fontSize * 4;
	public static final int yPadding = fontSize + 6;
	
	PixelValue freqPV = new PixelValue(minFreq, maxFreq, xPadding, true);
	PixelValue amplitudePV = new PixelValue(0.0, absoluteMaxAmplitude, yPadding, false);
	PixelValue gainPV = new PixelValue(-16.0, 0.0, yPadding, false);
	PixelValue overshootPV = new PixelValue(Filter.minOvershootLog2, Filter.maxOvershootLog2, yPadding, false);
	PixelValue filterQPV = new PixelValue(Filter.minFilterQLog2, Filter.maxFilterQLog2, yPadding, false);
	
	SelectionMode selectionMode = SelectionMode.NONE;
	
	public enum SelectionMode {
		NONE,
		GAIN,
		OVERSHOOT,
		FILTER_EQ;
	}
	
	public class PixelValue {
		
		int padding;
		boolean lowToHigh;
		double maxValue;
		double minValue;
		
		PixelValue(double minValue, double maxValue, int padding, boolean lowToHigh) {
			this.minValue = minValue;
			this.maxValue = maxValue;
			this.padding = padding;
			this.lowToHigh = lowToHigh;
		}
		
		public int valueToPixel(double value, int maxPixel) {
			if(lowToHigh) {
				double valuePerPixel = (maxValue - minValue) / (maxPixel - padding * 2);
				return (int) Math.round((value - minValue) / valuePerPixel + padding);
			}
			double valuePerPixel = (maxValue - minValue) / (maxPixel - padding * 2);
			return (int) Math.round(maxPixel - (value - minValue) / valuePerPixel - padding);
		}
		
		public double pixelToValue(int pixel, int maxPixel) {
			if(lowToHigh) {
				double valuesPerPixel = (maxValue - minValue) / (maxPixel - padding * 2);
				return (pixel - padding) * valuesPerPixel + minValue;
			}
			double valuesPerPixel = (maxValue - minValue) / (maxPixel - padding * 2);
			return (maxPixel - pixel - padding) * valuesPerPixel + minValue;
		}
		
	}
	
	public int freqToX(double freq) {
		return freqPV.valueToPixel(freq, view.getWidth());
	}

	public int amplitudeToY(double amplitude) {
		return amplitudePV.valueToPixel(amplitude, view.getHeight());
	}
	
	public int gainToY(double gain) {
		return gainPV.valueToPixel(gain, view.getHeight());
	}
	
	public double yToGain(int y) {
		return gainPV.pixelToValue(y, view.getHeight());
	}
	
	public int overshootToY(double overshoot) {
		return overshootPV.valueToPixel(overshoot, view.getHeight());
	}
	
	public double yToOvershoot(int y) {
		return overshootPV.pixelToValue(y, view.getHeight());
	}
	
	public int filterQToY(double filterQ) {
		return filterQPV.valueToPixel(filterQ, view.getHeight());
	}
	
	public double yToFilterQ(int y) {
		return filterQPV.pixelToValue(y, view.getHeight());
	}
	
    public int getControlPointWidth() {
    	return 16;
    }
	
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
	

	public SpectrumEQEditor(SpectrumEQ parent) {
		super(new BorderLayout());
        view = new SpectrumEQView(this);
        view.setBackground(Color.black);
        view.setPreferredSize(new Dimension(1500, 800));
        controller = new SpectrumEQController(this);
        add(createNavigationBar(), BorderLayout.PAGE_START);
        view.addMouseListener(controller);
        view.addMouseMotionListener(controller);
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setSize(1500, 800);
        add(scrollPane, BorderLayout.CENTER);
        this.parent = parent;
        this.moduleEditor = parent.getParent();
        this.multiWindow = moduleEditor.parent;
        initFFTData();
	}
	
    public ArrayList<Rectangle> getGainControlAreas() {
    	int height = getControlPointWidth();
    	ArrayList<Rectangle> returnVal = new ArrayList<Rectangle>();
    	for(EQBand eqBand: parent.eqBands) {
    		double gain = Math.log(eqBand.gain) / Math.log(2.0);
    		int x = freqToX(Math.log(eqBand.criticalBand.getCenterFreq()) / Math.log(2.0));
    		int leftX = x - height / 2;
    		int y = gainToY(gain);
       		int upperY = y - height / 2;
    		returnVal.add(new Rectangle(leftX, upperY, height, height));
    		System.out.println(leftX + " " + upperY);
    	}
    	return returnVal;
    }
    
    public ArrayList<Rectangle> getOvershootControlAreas() {
    	int height = getControlPointWidth();
    	ArrayList<Rectangle> returnVal = new ArrayList<Rectangle>();
    	for(EQBand eqBand: parent.eqBands) {
    		double overshoot = Math.log(eqBand.criticalBand.getOvershoot()) / Math.log(2.0);
    		int x = freqToX(Math.log(eqBand.criticalBand.getCenterFreq()) / Math.log(2.0));
    		int leftX = x - height / 2;
    		int y = overshootToY(overshoot);
       		int upperY = y - height / 2;
    		returnVal.add(new Rectangle(leftX, upperY, height, height));
    		System.out.println(leftX + " " + upperY);
    	}
    	return returnVal;
    }
    
    public ArrayList<Rectangle> getFilterQControlAreas() {
    	int height = getControlPointWidth();
    	ArrayList<Rectangle> returnVal = new ArrayList<Rectangle>();
    	for(EQBand eqBand: parent.eqBands) {
    		double filterQ = Math.log(eqBand.criticalBand.getFilterQ()) / Math.log(2.0);
    		int x = freqToX(Math.log(eqBand.criticalBand.getCenterFreq()) / Math.log(2.0));
    		int leftX = x - height / 2;
    		int y = filterQToY(filterQ);
       		int upperY = y - height / 2;
    		returnVal.add(new Rectangle(leftX, upperY, height, height));
    		System.out.println(leftX + " " + upperY);
    	}
    	return returnVal;
    }
	
	public void initFFTData() {
		double[] control = new double[(int) Math.round(ModuleEditor.defaultDuration * SynthTools.sampleRate)];
		for(int index = 0; index < control.length; index++) {
			control[index] = 1.0;
		}
		//double maxAmplitude = getMaxAmplitude(parent.masterGetSamples(new HashSet<Integer>(), control, true));
		double[] filteredOutput = scaleData(parent.masterGetSamples(new HashSet<Integer>(), control)); //, maxAmplitude);
		if(filteredOutput == null) {
			JOptionPane.showMessageDialog(parent.getParent(), "Filter is unstable");
			return;
		}
		initFFTData(filteredOutput);
		AudioPlayer.playAudio(filteredOutput);
		view.repaint();
	}
	
	public double[] scaleData(double[] samples) {
		double maxAmplitude = getMaxAmplitude(samples);
		return scaleData(samples, maxAmplitude);
	}
	
	public double getMaxAmplitude(double[] samples) {
		double maxAmplitude = 0.0;
		for(int index = 0; index < samples.length; index++) {
			if(Math.abs(samples[index]) > maxAmplitude) {
				maxAmplitude = Math.abs(samples[index]);
			}
		}
		if(maxAmplitude > absoluteMaxAmplitude) {
			//JOptionPane.showMessageDialog(parent.getParent(), "Filter is unstable");
			//return -1;
		}
		return 1.0; // maxAmplitude;
	}
	
	public double[] scaleData(double[] samples, double maxAmplitude) {
		if(maxAmplitude == 0) return samples;
		if(maxAmplitude == -1) {
			for(int index = 0; index < samples.length; index++) samples[index] = 0.0;
			return samples;
		}
		for(int index = 0; index < samples.length; index++) samples[index] *= Short.MAX_VALUE / maxAmplitude;
		return samples;
	}
	
	public void initFFTData(double[] channel) {
		if(channel == null) return;
		if(channel.length == 0) return;
		fftMaxAmplitude = 0.0;
		freqToTimeToAmplitude = new TreeMap<Double, TreeMap<Double, Double>>();
		ifft = new double[channel.length];
		for(int index = 0; index < ifft.length; index++) {
			ifft[index] = 0.0;
		}
		int minWindowLength = 512;
		int minStepSize = 512 / 2;
		minFreq = Math.log((SynthTools.sampleRate / 2048.0)) / Math.log(2.0);
		maxFreq = Math.log((SynthTools.sampleRate / 2.0)) / Math.log(2.0);
		minTime = 0.0;
		maxTime = channel.length / SynthTools.sampleRate;
		// calculate minStepSize, use lowest window length to avoid time being slightly different
		double[] kbdWindow = new double[minWindowLength];
		Filter.CreateWindow(kbdWindow, kbdWindow.length, 1.0);
		for(int index = 0; index < kbdWindow.length; index++) {
			if(index > 0) {
				if(kbdWindow[index - 1] >= 0.5 && kbdWindow[index] <= 0.5) {
					//minStepSize = index;
				}
			}
		}
		filterAndFFTData(channel, SynthTools.sampleRate / 2048.0, SynthTools.sampleRate / 256.0, minWindowLength * 16, minStepSize * 16);
		filterAndFFTData(channel, SynthTools.sampleRate / 256.0, SynthTools.sampleRate / 128.0, minWindowLength * 8, minStepSize * 8);
		filterAndFFTData(channel, SynthTools.sampleRate / 128.0, SynthTools.sampleRate / 64.0, minWindowLength * 4, minStepSize * 4);
		filterAndFFTData(channel, SynthTools.sampleRate / 64.0, SynthTools.sampleRate / 32.0, minWindowLength * 2, minStepSize * 2);
		filterAndFFTData(channel, SynthTools.sampleRate / 32.0, SynthTools.sampleRate / 2.0, minWindowLength, minStepSize);
	}
	
	public void filterAndFFTData(double[] channel, double lowCutoff, double highCutoff, int windowLength, int samplesPerStep) {
		//double[] filtered = Filter.butterworthHighpass(channel, lowCutoff, 2);
		//if(highCutoff < SynthTools.sampleRate / 2.0) filtered = Filter.butterworthLowpass(filtered, highCutoff, 2);
		initFFTData(channel, lowCutoff, highCutoff, windowLength, samplesPerStep);
	}
	
	public void initFFTData(double[] channel, double lowCutoff, double highCutoff, int windowLength, int samplesPerStep) {
		double[] logFreq = new double[windowLength / 2];
		double binStep = SynthTools.sampleRate / windowLength;
		int minFreq = 8192 / 2048;
		if(lowCutoff > SynthTools.sampleRate / 2048.0) {
			minFreq = (int) Math.round(lowCutoff / binStep) / 2;
		}
		int maxFreq = windowLength / 2;
		if(highCutoff < SynthTools.sampleRate / 2.0) {
			maxFreq = (int) Math.round(highCutoff / binStep) * 2;
		}
		for(int freq = minFreq; freq < maxFreq; freq++) {
			logFreq[freq] = Math.log(SynthTools.sampleRate / windowLength * freq) / Math.log(2.0);
		}
		double[] samples = new double[windowLength * 2];
		double[] kbdWindow = new double[windowLength];
		double windowGain = 0.0;
		Filter.CreateWindow(kbdWindow, kbdWindow.length, alpha);
		for(int index = 0; index < kbdWindow.length; index++) {
			windowGain += kbdWindow[index];
		}
		for(int index = 0; index < (channel.length - windowLength); index += samplesPerStep) {
			for(int windowIndex = 0; windowIndex < windowLength; windowIndex++) {
				samples[windowIndex * 2] = channel[index + windowIndex] * kbdWindow[windowIndex];
				samples[windowIndex * 2 + 1] = 0.0;
			}
			FFT.runFFT(samples, samples.length / 2, 1);
			double[] idft = new double[samples.length];
			for(int idftIndex = 0; idftIndex < minFreq * 2; idftIndex++) idft[idftIndex] = 0.0;
			for(int idftIndex = maxFreq * 2; idftIndex < samples.length; idftIndex++) idft[idftIndex] = 0.0;
			for(int idftIndex = minFreq; idftIndex < maxFreq * 2; idftIndex += 2) {
				double real = samples[idftIndex * 2];
				double imag = samples[idftIndex * 2 + 1];
				idft[idftIndex * 2] = real;
				idft[idftIndex * 2 + 1] = imag;
			}
			FFT.runFFT(idft, idft.length / 2, -1);
			for(int idftIndex = 0; idftIndex < idft.length; idftIndex += 2) {
				double real = idft[idftIndex] / windowGain;
				ifft[idftIndex / 2 + index] += real;
			}
			double time = index / SynthTools.sampleRate;
			for(int freq = minFreq; freq < maxFreq; freq++) {
				double currentFreq = logFreq[freq];
				double real = samples[freq * 2] / windowGain;
				double imag = samples[freq * 2 + 1] / windowGain;
				double amplitude = Math.log(Math.sqrt(real * real + imag * imag)) / Math.log(2.0);
				if(amplitude <= 0.0) continue;
				if(amplitude > fftMaxAmplitude) fftMaxAmplitude = amplitude;
				if(!freqToTimeToAmplitude.containsKey(currentFreq)) {
					freqToTimeToAmplitude.put(currentFreq, new TreeMap<Double, Double>());
				}
				if(!freqToTimeToAmplitude.get(currentFreq).containsKey(time)) {
					freqToTimeToAmplitude.get(currentFreq).put(time, amplitude);
				} else {
					double prevAmplitude = freqToTimeToAmplitude.get(currentFreq).get(time);
					if(amplitude > prevAmplitude) {
						freqToTimeToAmplitude.get(currentFreq).put(time, amplitude);
					}
				}
				if(amplitude > fftMaxAmplitude) fftMaxAmplitude = amplitude;
			}
		}
		//System.out.println(minAmplitude);
		//System.out.println(maxAmplitude);
		//view.repaint();
	}
	

	public int xToEQBandIndex(int x) {
		int index = 0;
		for(EQBand eqBand: parent.eqBands) {
			int lowerX = freqToX(Math.log(eqBand.criticalBand.getCenterFreq()) / Math.log(2.0)) - getControlPointWidth() / 2;
			int upperX = lowerX + getControlPointWidth();
			if(x > lowerX && x < upperX) return index;
			index++;
		}
		return -1;
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
		moduleEditor.closeSpectrumEQEditor();
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
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
