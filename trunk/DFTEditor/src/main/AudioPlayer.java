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

public class AudioPlayer extends Thread {

	SourceDataLine line;
	
	final float sampleRate = 44100f;
	final int bitsPerSample = 16; 
	final int channels = 2;
	final boolean signed = true;
	final boolean bigEndian = false;
	final double fullScale = Short.MAX_VALUE - 1;
	private boolean stereo;
	public double[] mono;
	public double[] left;
	public double[] right;
	private double masterVolume = 1.0;
	volatile boolean playContinuous = false;
	AudioFormat format = null;
	byte[] audioByteData = null;
	private volatile Thread thisThread = null;
	private int frameSize = 1024;

	AudioPlayer (double[] mono, double masterVolume) {
		this.stereo = false;
		this.mono = mono;
		this.masterVolume = masterVolume;
		this.thisThread = this;
	}
	
	AudioPlayer (double[] left, double[] right, double masterVolume) {
		this.stereo = true;
		this.left = left;
		this.right = right;
		this.masterVolume = masterVolume;
		this.thisThread = this;
	}
	
	AudioPlayer (double[] left, double[] right, double masterVolume, boolean playContinuous) {
		this.stereo = true;
		this.left = left;
		this.right = right;
		this.masterVolume = masterVolume;
		this.playContinuous = playContinuous;
		this.thisThread = this;
	}
	
	public void stopPlaying() {
		thisThread = null;
	}

	public void run() {

		Thread runningThread = Thread.currentThread();
		
		format = new AudioFormat(sampleRate, bitsPerSample, channels, signed, bigEndian);
		
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format); 
		
		if (AudioSystem.isLineSupported(info)) {
    		try {
        		line = (SourceDataLine) AudioSystem.getLine(info);
        		//line.addLineListener(moduleEditor);
        		line.open(format);
 			} catch (LineUnavailableException ex) {
				System.out.println("Cannot open speaker port");
				System.exit(1);
			}
		} else {
			System.out.println("Speaker port unsupported");
			System.exit(1);
		}
		if(!stereo) {
			getAudioBytes(mono, masterVolume);
		} else {
			getAudioBytes(left, right, masterVolume);
		}
		int position = 0;
		line.start();
		//System.out.println(line.available());
		while(runningThread == thisThread) {
			while(position < audioByteData.length) {
				int bytesLeftToWrite = audioByteData.length - position;
				int bytesToWrite = line.available();
				if(bytesToWrite > frameSize) bytesToWrite = frameSize;
				if(bytesToWrite == 0) continue;
				if(bytesLeftToWrite > bytesToWrite) {
					line.write(audioByteData, position, bytesToWrite);
					position += bytesToWrite;
					//System.out.println("Available");
				} else {
					line.write(audioByteData, position, bytesLeftToWrite);
					position = audioByteData.length;
					//System.out.println("Finished");
				}
			}
			line.drain();
			position = 0;
			if(!playContinuous) {
				line.stop();
				line.close();
				thisThread = null;
			}
		}
	}

	public double getSampleRate() {
		return (double) sampleRate;
	}
	
	public void PlayBuffer(byte[] stereo) {
		line.write(stereo, 0, stereo.length);
	}
	
	public void getAudioBytes(double[] mono, double masterVolume) {
		if(mono == null) return;
		final int numberOfSamples = mono.length;
		double[] left = new double[numberOfSamples];
		double[] right = new double[numberOfSamples];
		int index;
		for (index = 0; index < numberOfSamples; index++) { 
			left[index] = mono[index];
			right[index] = mono[index];
		}
		getAudioBytes(left, right, masterVolume);
	}

	public void getAudioBytes(double[] left, double[] right, double masterVolume) {
		if(left == null || right == null) return;
		int numberOfSamples = right.length;
		if (left.length < right.length) numberOfSamples = left.length;
		//System.out.println("AudioPlayer.PlayBuffer: left samples = " + left.length + " | right samples = " + right.length);
		int numBytesToWrite = numberOfSamples * 4;
		audioByteData = new byte[numBytesToWrite];
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
	}

}
