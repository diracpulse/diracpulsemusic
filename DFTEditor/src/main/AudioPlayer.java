package main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import main.PlayDataInWindow.SynthType;

public class AudioPlayer extends Thread implements LineListener, ActionListener {

	SourceDataLine line;
	
	final float sampleRate = 44100f;
	final int bitsPerSample = 16; 
	final int channels = 2;
	final boolean signed = true;
	final boolean bigEndian = false;
	final double fullScale = Short.MAX_VALUE - 1;
	private boolean stereo;
	private double[] mono;
	private double[] left;
	private double[] right;
	private double masterVolume = 1.0;
	Timer timer = null;
	boolean refresh = false;
	int playCounter = 0;
	
	private ModuleEditor moduleEditor = null;

	AudioPlayer (double[] mono, double masterVolume) {
		this.stereo = false;
		this.mono = mono;
		this.masterVolume = masterVolume;
		//timer = new Timer(100, this);
        //timer.setInitialDelay(0);
        //timer.start();
	}
	
	AudioPlayer (double[] left, double[] right, double masterVolume) {
		this.stereo = true;
		this.left = left;
		this.right = right;
		this.masterVolume = masterVolume;
		//timer = new Timer(100, this);
        //timer.setInitialDelay(0);
        //timer.start();
	}
	
	AudioPlayer (double[] left, double[] right, double masterVolume, ModuleEditor editor) {
		this.stereo = true;
		this.left = left;
		this.right = right;
		this.masterVolume = masterVolume;
		this.moduleEditor = editor;
		timer = new Timer(100, this);
        timer.setInitialDelay(0);
        timer.start();
	}
	
	public void run() {

		AudioFormat format = new AudioFormat(sampleRate, bitsPerSample, channels, signed, bigEndian);
		
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format); 
		
		if (AudioSystem.isLineSupported(info)) {
    		try {
        		line = (SourceDataLine) AudioSystem.getLine(info);
        		line.addLineListener(this);
        		line.open(format);
 			} catch (LineUnavailableException ex) {
				System.out.println("Cannot open speaker port");
				System.exit(1);
			}
		} else {
			System.out.println("Speaker port unsupported");
			System.exit(1);
		}
		line.start();
		if(!stereo) {
			PlayBuffer(mono, masterVolume);
		} else {
			PlayBuffer(left, right, masterVolume);
		}
	}

	public double getSampleRate() {
		return (double) sampleRate;
	}
	
	public void PlayBuffer(byte[] stereo) {
		line.write(stereo, 0, stereo.length);
	}
	
	public void PlayBuffer(double[] mono, double masterVolume) {
		if(mono == null) return;
		final int numberOfSamples = mono.length;
		double[] left = new double[numberOfSamples];
		double[] right = new double[numberOfSamples];
		int index;
		for (index = 0; index < numberOfSamples; index++) { 
			left[index] = mono[index];
			right[index] = mono[index];
		}
		PlayBuffer(left, right, masterVolume);
	}
	
	/* NOTE: this version of PlayBuffer scales maxAmplitude to 1.0 */
	public void PlayBuffer(double[] left, double[] right, double masterVolume) {
		if(left == null || right == null) return;
		int numberOfSamples = right.length;
		if (left.length < right.length) numberOfSamples = left.length;
		//System.out.println("AudioPlayer.PlayBuffer: left samples = " + left.length + " | right samples = " + right.length);
		int numBytesToWrite = numberOfSamples * 4;
		byte[] audioByteData = new byte[numBytesToWrite];
		double maxAmplitude = 0.0;
		double leftAmplitude;
		double rightAmplitude;
		int index;
		for (index = 0; index < numberOfSamples; index++) {
			leftAmplitude = Math.abs(left[index]);
			rightAmplitude = Math.abs(right[index]);
			if (leftAmplitude > maxAmplitude) maxAmplitude = leftAmplitude;
			if (rightAmplitude > maxAmplitude) maxAmplitude = rightAmplitude;
		}
		if (maxAmplitude == 0.0) return;
		if (masterVolume < 0.0) masterVolume = -1.0 * masterVolume;
		if (masterVolume > 1.0) masterVolume = 1.0;
		double volume = masterVolume * fullScale/maxAmplitude;
		int leftSample;
		int rightSample;
		int sampleIndex;
		for (index = 0; index < numberOfSamples; index++) {
			sampleIndex = index * 4;
			leftSample = (int) Math.round(left[index] * volume);
			audioByteData[sampleIndex] = (byte) (leftSample & 0xFF);
			audioByteData[sampleIndex + 1] = (byte) (leftSample >> 8);
			rightSample = (int) Math.round(right[index] * volume);
			audioByteData[sampleIndex + 2] = (byte) (rightSample & 0xFF);
			audioByteData[sampleIndex + 3] = (byte) (rightSample >> 8);			
		}
		int offset = 0;
		line.start();
		line.write(audioByteData, offset, numBytesToWrite);
		line.addLineListener(this);
		line.drain();
		line.stop();
	}

	public void SoundOff() {
		
		line.drain();
		line.stop();
		line.close();
		line = null;
		
		return;
	}

	public void actionPerformed(ActionEvent arg0) {
		playCounter++;
		if(moduleEditor != null) {
			if(playCounter % 3 == 0) {
				ArrayList<double[]> channels = moduleEditor.getSamplesContinuous();
				if(channels == null) return;
				PlayBuffer(channels.get(0), channels.get(1), 1.0);
				refresh = false;
			}
		}
	}

	@Override
	public void update(LineEvent arg0) {
		//System.out.println(arg0.getType());
		if(arg0.getType() == LineEvent.Type.STOP) {
			//System.out.println("STOP");
			if(moduleEditor != null) {
			}
		}
		
	}

}
