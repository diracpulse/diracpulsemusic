
package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import main.modules.BasicWaveformController;
import main.modules.BasicWaveformView;
import main.modules.BasicWaveformEditor.ControlRect;

public class Spectrum extends JPanel implements WindowListener, ActionListener {
	
	MultiWindow multiWindow;
	ModuleEditor moduleEditor;
	SpectrumView view;
	double alpha = 5.0;
	DataView dataView = DataView.LEFT;
	
	public enum SpectrumType {
		DFT,
		FFT;
	}
	
	public enum Channel {
		LEFT,
		RIGHT;
	}
	
	public enum DataView {
		LEFT,
		RIGHT,
		STEREO;
	}
	
	public static class Data {
		
		public TreeMap<Double, TreeMap<Double, Double>> freqToTimeToAmplitude = new TreeMap<Double, TreeMap<Double, Double>>();
		public double minTime = 0.0;
		public double maxTime = 0.0;
		public double minFreq = 0.0;
		public double maxFreq = 0.0;
		public double minAmplitude = 0.0;
		public double maxAmplitude = 0.0;
		public double[] ifft;
		public double[][] amplitudes;
	
	}
	
	TreeMap<Channel, Data> channelToData = new TreeMap<Channel, Data>();
	private SpectrumType type = null;
	private JToolBar navigationBar;
	
	public static final int fontSize = 12;
	public static final int xPadding = fontSize * 4;
	public static final int yPadding = fontSize + 6;

	public void addNavigationButton(String buttonText) {
		JButton button = new JButton(buttonText);
		button.addActionListener((ActionListener) this);
		navigationBar.add(button);
	}
	
	public JToolBar createNavigationBar() {
		navigationBar = new JToolBar("Navigation Bar");
        // Create Navigation Buttons
        addNavigationButton("Left");
        addNavigationButton("Right");
    	return navigationBar;
	}
	
	public Spectrum(ModuleEditor moduleEditor) {
		super(new BorderLayout());
        view = new SpectrumView(this);
        view.setBackground(Color.black);
        view.setPreferredSize(new Dimension(1500, 800));
        add(createNavigationBar(), BorderLayout.PAGE_START);
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setSize(1500, 800);
        add(scrollPane, BorderLayout.CENTER);
        this.moduleEditor = moduleEditor;
        this.multiWindow = moduleEditor.parent;
	}
	
	public void initFFTData(double[] left, double[] right) {
		this.type = SpectrumType.FFT;
		initFFTData(Channel.LEFT, left);
		initFFTData(Channel.RIGHT, right);
		AudioPlayer.playAudio(channelToData.get(Channel.LEFT).ifft, channelToData.get(Channel.RIGHT).ifft);
	}
	
	public void initFFTData(Channel channel, double[] samples) {
		channelToData.put(channel, new Data());
		if(samples == null) return;
		if(samples.length == 0) return;
		channelToData.get(channel).freqToTimeToAmplitude = new TreeMap<Double, TreeMap<Double, Double>>();
		channelToData.get(channel).ifft = new double[samples.length];
		for(int index = 0; index < samples.length; index++) channelToData.get(channel).ifft[index] = 0.0;
		int minWindowLength = 512;
		int minStepSize = 512 / 2;
		channelToData.get(channel).minFreq = Math.log(SynthTools.sampleRate / 2048.0) / Math.log(2.0);
		channelToData.get(channel).maxFreq = Math.log(SynthTools.sampleRate / 2.0) / Math.log(2.0);
		channelToData.get(channel).minTime = 0.0;
		channelToData.get(channel).maxTime = samples.length / SynthTools.sampleRate;
		// calculate minStepSize, use lowest window length to avoid time being slightly different
		double[] kbdWindow = new double[minWindowLength];
		Filter.CreateWindow(kbdWindow, kbdWindow.length, alpha);
		for(int index = 0; index < kbdWindow.length; index++) {
			if(index > 0) {
				if(kbdWindow[index - 1] >= 0.5 && kbdWindow[index] <= 0.5) {
					//minStepSize = index;
				}
			}
		}
		initFFTData(channel, samples, SynthTools.sampleRate / 2048.0, SynthTools.sampleRate / 256.0, minWindowLength * 16, minStepSize * 16);
		initFFTData(channel, samples, SynthTools.sampleRate / 256.0, SynthTools.sampleRate / 128.0, minWindowLength * 8, minStepSize * 8);
		initFFTData(channel, samples, SynthTools.sampleRate / 128.0, SynthTools.sampleRate / 64.0, minWindowLength * 4, minStepSize * 4);
		initFFTData(channel, samples, SynthTools.sampleRate / 64.0, SynthTools.sampleRate / 32.0, minWindowLength * 2, minStepSize * 2);
		initFFTData(channel, samples, SynthTools.sampleRate / 32.0, SynthTools.sampleRate / 2.0, minWindowLength, minStepSize);
	}

	public void initFFTData(Channel channel, double[] input, double lowCutoff, double highCutoff, int windowLength, int samplesPerStep) {
		//System.out.println("initFFTData");
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
		Filter.CreateWindow(kbdWindow, kbdWindow.length, 5.0);
		for(int index = 0; index < kbdWindow.length; index++) {
			windowGain += kbdWindow[index];
		}
		for(int index = 0; index < (input.length - windowLength); index += samplesPerStep) {
			for(int windowIndex = 0; windowIndex < windowLength; windowIndex++) {
				samples[windowIndex * 2] = input[index + windowIndex] * kbdWindow[windowIndex];
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
				channelToData.get(channel).ifft[idftIndex / 2 + index] += real;
			}
			double time = index / SynthTools.sampleRate;
			//System.out.println(time + " " + minFreq + " " + maxFreq);
			for(int freq = minFreq; freq < maxFreq; freq++) {
				double currentFreq = logFreq[freq];
				double real = samples[freq * 2] / windowGain;
				double imag = samples[freq * 2 + 1] / windowGain;
				double amplitude = Math.log(Math.sqrt(real * real + imag * imag)) / Math.log(2.0);
				if(amplitude <= 0.0) continue;
				if(amplitude > channelToData.get(channel).maxAmplitude) channelToData.get(channel).maxAmplitude = amplitude;
				//System.out.println(channel.toString() + " " + time + " " + freq + " " + amplitude);
				if(!channelToData.get(channel).freqToTimeToAmplitude.containsKey(currentFreq)) {
					channelToData.get(channel).freqToTimeToAmplitude.put(currentFreq, new TreeMap<Double, Double>());
				}
				if(!channelToData.get(channel).freqToTimeToAmplitude.get(currentFreq).containsKey(time)) {
					channelToData.get(channel).freqToTimeToAmplitude.get(currentFreq).put(time, amplitude);
				} else {
					double prevAmplitude = channelToData.get(channel).freqToTimeToAmplitude.get(currentFreq).get(time);
					amplitude = Math.log(Math.pow(2.0, prevAmplitude) + Math.pow(2.0, amplitude)) / Math.log(2.0);
					channelToData.get(channel).freqToTimeToAmplitude.get(currentFreq).put(time, amplitude);
				}
				if(amplitude > channelToData.get(channel).maxAmplitude) channelToData.get(channel).maxAmplitude = amplitude;
			}
		}
	}
	
	public void initDFTData(double[] left, double[] right) {
		this.type = SpectrumType.DFT;
        multiWindow.dftEditorFrame.ModuleDFT(left, right);
        initDFTData();
        view.repaint();
	}
	
	
	public void initDFTData() {
		channelToData.put(Channel.LEFT, new Data());
		channelToData.put(Channel.RIGHT, new Data());
		channelToData.get(Channel.LEFT).amplitudes = DFTEditor.amplitudesLeft;
		channelToData.get(Channel.RIGHT).amplitudes = DFTEditor.amplitudesRight;
		for(Channel channel: Channel.values()) {
		    channelToData.get(channel).maxFreq = Math.log(DFTEditor.maxFreqHz) / Math.log(2.0);
		    channelToData.get(channel).minFreq = Math.log(DFTEditor.minFreqHz) / Math.log(2.0);
		    channelToData.get(channel).minTime = 0.0;
		    channelToData.get(channel).maxTime = (DFTEditor.timeStepInMillis / 1000.0) * (channelToData.get(channel).amplitudes.length - 1);
	        loadData(channel);
		}
        view.repaint();
	}
	
	public void loadData(Channel channel) {
		double[][] timeLogFreqLogAmp = channelToData.get(channel).amplitudes;
		double startTime = channelToData.get(channel).minTime;
		double endTime = channelToData.get(channel).maxTime;
		double startFreq = channelToData.get(channel).maxFreq; // DFTEditor maxFreq == amplitudes[time][0]
		double endFreq = channelToData.get(channel).minFreq; // DFTEditor maxFreq == amplitudes[time][numFreqs]
		int numTimes = timeLogFreqLogAmp.length;
		int numFreqs = timeLogFreqLogAmp[0].length;
		double timeStep = (endTime - startTime) / numTimes;
		double freqStep = (endFreq - startFreq) / numFreqs;
		for(int timeIndex = 0; timeIndex < numTimes; timeIndex++) {
			double currentTime = startTime + timeStep * timeIndex;
			//System.out.println("Time " + currentTime);
			for(int freqIndex = 0; freqIndex < numFreqs; freqIndex++) {
				double currentFreq = startFreq + freqStep * freqIndex;
				double amplitude = timeLogFreqLogAmp[timeIndex][freqIndex];
				if(amplitude == 0.0) continue;
				if(amplitude > channelToData.get(channel).maxAmplitude) channelToData.get(channel).maxAmplitude = amplitude;
				if(amplitude < channelToData.get(channel).minAmplitude) channelToData.get(channel).minAmplitude = amplitude;
				if(!channelToData.get(channel).freqToTimeToAmplitude.containsKey(currentFreq)) {
					//System.out.println("Freq" + currentFreq);
					channelToData.get(channel).freqToTimeToAmplitude.put(currentFreq, new TreeMap<Double, Double>());
				}
				channelToData.get(channel).freqToTimeToAmplitude.get(currentFreq).put(currentTime, timeLogFreqLogAmp[timeIndex][freqIndex]);
			}
		}
		//System.out.println("Time " + minTime + " " + maxTime + " " + minFreq + " " + maxFreq);
	}
	
	public double xToFreq(Channel channel, int x) {
		double pixelsPerFreqStep = (view.getWidth() - xPadding) / (channelToData.get(channel).maxFreq - channelToData.get(channel).minFreq);
		return Math.round(pixelsPerFreqStep * x + xPadding);
	}
	
	public double yToAmplitude(Channel channel, int y) {
		double pixelsPerAmplitudeStep = (view.getHeight() - yPadding) / (channelToData.get(channel).maxAmplitude - channelToData.get(channel).minAmplitude);
		return Math.round(pixelsPerAmplitudeStep * y + yPadding);
	}
	
	public int freqToX(Channel channel, double freq) {
		//return (int) Math.round(Math.random() * view.getWidth());
		double freqPerPixel = (channelToData.get(channel).maxFreq - channelToData.get(channel).minFreq) / (view.getWidth() - xPadding);
		return (int) Math.round((freq - channelToData.get(channel).minFreq) / freqPerPixel + xPadding);
	}
	
	public int amplitudeToY(Channel channel, double amplitude) {
		//return (int) Math.round(Math.random() * view.getHeight());
		double amplitudePerPixel = (channelToData.get(channel).maxAmplitude - channelToData.get(channel).minAmplitude) / (view.getHeight() - yPadding);
		return (int) Math.round(view.getHeight() - (amplitude - channelToData.get(channel).minAmplitude) / amplitudePerPixel - yPadding);
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
		if(type == SpectrumType.FFT) moduleEditor.closeSpectrumFFT();
		if(type == SpectrumType.DFT) moduleEditor.closeSpectrumDFT();
		
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

	@Override
	public void actionPerformed(ActionEvent e) {
		if ("Left".equals(e.getActionCommand())) dataView = dataView.LEFT;
		if ("Right".equals(e.getActionCommand())) dataView = dataView.RIGHT;
		view.repaint();
	}
	
	

}
