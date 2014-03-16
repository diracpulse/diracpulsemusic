
package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import main.modules.BasicWaveformController;
import main.modules.BasicWaveformView;
import main.modules.BasicWaveformEditor.ControlRect;

public class Spectrum extends JPanel implements WindowListener {
	
	MultiWindow multiWindow;
	SpectrumView view;
	
	TreeMap<Double, TreeMap<Double, Double>> freqToTimeToAmplitude = new TreeMap<Double, TreeMap<Double, Double>>();
	double minTime = 0;
	double maxTime = 0;
	double minFreq = 0;
	double maxFreq = 0;
	double maxAmplitude = 0.0;
	double minAmplitude = 0.0;
	
	public static final int fontSize = 12;
	public static final int xPadding = fontSize * 4;
	public static final int yPadding = fontSize + 6;

	public Spectrum(MultiWindow multiWindow) {
		super(new BorderLayout());
        view = new SpectrumView(this);
        view.setBackground(Color.black);
        view.setPreferredSize(new Dimension(1500, 800));
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setSize(1500, 800);
        add(scrollPane, BorderLayout.CENTER);
        this.multiWindow = multiWindow;
	}
	
	public void initFFTData(double[] left, double[] right) {
		int windowLength = 1024;
		double[] logFreq = new double[windowLength];
		double[] samples = new double[windowLength * 2];
		double[] kbdWindow = new double[windowLength];
		Filter.CreateWindow(kbdWindow, kbdWindow.length, 5.0);
		for(int freq = 1; freq < windowLength / 2; freq++) {
			double currentFreq = Math.log((SynthTools.sampleRate / windowLength) * freq) / Math.log(2.0);
			logFreq[freq] = currentFreq;
			freqToTimeToAmplitude.put(currentFreq, new TreeMap<Double, Double>());
		}
		minFreq = Math.log((SynthTools.sampleRate / windowLength)) / Math.log(2.0);
		maxFreq = Math.log((SynthTools.sampleRate / 2.0)) / Math.log(2.0);
		minTime = 0.0;
		maxTime = left.length / SynthTools.sampleRate;
		for(int index = 0; index < (left.length - windowLength); index += windowLength) {
			for(int windowIndex = 0; windowIndex < windowLength; windowIndex++) {
				samples[windowIndex * 2] = left[index + windowIndex] * kbdWindow[windowIndex];
				samples[windowIndex * 2 + 1] = 0.0;
			}
			FFT.runFFT(samples, samples.length / 2, 1);
			double time = index / SynthTools.sampleRate;
			for(int freq = 1; freq < windowLength / 2; freq++) {
				double currentFreq = logFreq[freq];
				double real = samples[freq * 2] / (windowLength / 2);
				double complex = samples[freq * 2 + 1] / (windowLength / 2);
				double amplitude = Math.log(Math.sqrt(real * real + complex * complex)) / Math.log(2.0);
				if(amplitude <= 0.0) continue;
				if(amplitude > maxAmplitude) maxAmplitude = amplitude;
				//if(amplitude < minAmplitude) minAmplitude = amplitude;
				freqToTimeToAmplitude.get(currentFreq).put(time, amplitude);
			}
		}
		view.repaint();
	}
	
	public void initDFTData(double[] left, double[] right) {
        multiWindow.dftEditorFrame.ModuleDFT(left, right);
        maxFreq = Math.log(DFTEditor.maxFreqHz) / Math.log(2.0);
        minFreq = Math.log(DFTEditor.minFreqHz) / Math.log(2.0);
        minTime = 0.0;
        maxTime = (DFTEditor.timeStepInMillis / 1000.0) * (DFTEditor.amplitudesLeft.length - 1);
        loadData(DFTEditor.amplitudesLeft, minTime, maxTime, maxFreq, minFreq);
        view.repaint();
	}
	
	public void loadData(double[][] timeLogFreqLogAmp, double startTime, double endTime, double startFreq, double endFreq) {
		int numTimes = timeLogFreqLogAmp.length;
		int numFreqs = timeLogFreqLogAmp[0].length;
		double timeStep = (endTime - startTime) / numTimes;
		double freqStep = (endFreq - startFreq) / numFreqs;
		for(int timeIndex = 0; timeIndex < numTimes; timeIndex++) {
			double currentTime = startTime + timeStep * timeIndex;
			System.out.println("Time " + currentTime);
			for(int freqIndex = 0; freqIndex < numFreqs; freqIndex++) {
				double currentFreq = startFreq + freqStep * freqIndex;
				double amplitude = timeLogFreqLogAmp[timeIndex][freqIndex];
				if(amplitude == 0.0) continue;
				if(amplitude > maxAmplitude) maxAmplitude = amplitude;
				if(amplitude < minAmplitude) minAmplitude = amplitude;
				if(!freqToTimeToAmplitude.containsKey(currentFreq)) {
					System.out.println("Freq" + currentFreq);
					freqToTimeToAmplitude.put(currentFreq, new TreeMap<Double, Double>());
				}
				freqToTimeToAmplitude.get(currentFreq).put(currentTime, timeLogFreqLogAmp[timeIndex][freqIndex]);
			}
		}
		System.out.println("Time " + minTime + " " + maxTime + " " + minFreq + " " + maxFreq);
	}
	
	public double xToFreq(int x) {
		double pixelsPerFreqStep = (view.getWidth() - xPadding) / (maxFreq - minFreq);
		return Math.round(pixelsPerFreqStep * x + xPadding);
	}
	
	public double yToAmplitude(int y) {
		double pixelsPerAmplitudeStep = (view.getHeight() - yPadding) / (maxAmplitude - minAmplitude);
		return Math.round(pixelsPerAmplitudeStep * y + yPadding);
	}
	
	public int freqToX(double freq) {
		//return (int) Math.round(Math.random() * view.getWidth());
		double freqPerPixel = (maxFreq - minFreq) / (view.getWidth() - xPadding);
		return (int) Math.round((freq - minFreq) / freqPerPixel + xPadding);
	}
	
	public int amplitudeToY(double amplitude) {
		//return (int) Math.round(Math.random() * view.getHeight());
		double amplitudePerPixel = (maxAmplitude - minAmplitude) / (view.getHeight() - yPadding);
		return (int) Math.round(view.getHeight() - (amplitude - minAmplitude) / amplitudePerPixel - yPadding);
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
		// TODO Auto-generated method stub
		
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
