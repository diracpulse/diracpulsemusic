package main;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class AudioPlayer extends Thread {

	private static volatile SourceDataLine line = null;
	
	final static float sampleRate = 44100f;
	final static int bitsPerSample = 16; 
	final static int channels = 2;
	final static boolean signed = true;
	final static boolean bigEndian = false;
	final static double fullScale = Short.MAX_VALUE;
	final static int frameSize = 1024;
	private static volatile boolean playContinuous = false;
	private static volatile byte[] audioByteData = null;
	private static volatile AudioPlayer currentThread = null;
	
	private static void getLine() {
		
		AudioFormat format = new AudioFormat(sampleRate, bitsPerSample, channels, signed, bigEndian);
		
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format); 
		
		if (AudioSystem.isLineSupported(info)) {
    		try {
        		line = (SourceDataLine) AudioSystem.getLine(info);
        		line.open(format);
        		line.start();
 			} catch (LineUnavailableException ex) {
				System.out.println("Cannot open speaker port");
				System.exit(1);
			}
		} else {
			System.out.println("Speaker port unsupported");
			System.exit(1);
		}
	}
	
	public static void stopPlaying() {
		currentThread = null;
		if(line != null) {
			line.drain();
			line.stop();
			line.close();
			line = null;
		}
	}

	public void run() {
		int position = 0;
		while(true) {
			position = 0;
			if(audioByteData == null) continue;
			while(position < audioByteData.length) {
				int bytesLeftToWrite = audioByteData.length - position;
				int bytesToWrite = 0;
				if(line != null) {
					if(line.available() > frameSize) {
						bytesToWrite = frameSize;
					} else {
						bytesToWrite = line.available();
					}
				} else {
					getLine();
				}
				//if(bytesToWrite > frameSize) bytesToWrite = frameSize;
				if(bytesToWrite <= 0) continue;
				if(bytesLeftToWrite > bytesToWrite) {
					if(line == null) getLine();
					line.write(audioByteData, position, bytesToWrite);
					position += bytesToWrite;
					//System.out.println("Available");
				} else {
					if(line == null) getLine();
					line.write(audioByteData, position, bytesLeftToWrite);
					position = audioByteData.length;
					//System.out.println("Finished");
				}
				if(this != currentThread) {
					if(line != null) {
						line.drain();
					} else {
						getLine();
					}
				}
				if(interrupted()) {
					if(line != null) {
						line.drain();
					} else {
						getLine();
					}
					//System.out.println("Interrupted");
					return;
				}
			}
			if(!playContinuous) {
				//line.drain();
				//System.out.println("Non-continuous");
				return;
			}
		}
	}
	
	public static void playAudio(double[] mono) {
		if(line == null) getLine();
		getAudioBytes(mono, 1.0);
		playContinuous = false;
		currentThread = new AudioPlayer();
		currentThread.start();
		//System.out.println(Thread.getAllStackTraces().keySet().size());
	}
	
	public static void playAudio(double[] left, double[] right) {
		if(line == null) getLine();
		getAudioBytes(left, right, 1.0);
		playContinuous = false;
		currentThread = new AudioPlayer();
		currentThread.start();
		//System.out.println(Thread.getAllStackTraces().keySet().size());
	}
	
	public static void playAudioLoop(double[] mono) {
		if(line == null) getLine();
		getAudioBytes(mono, 1.0);
		playContinuous = true;
		currentThread = new AudioPlayer();
		currentThread.start();
		//System.out.println(Thread.getAllStackTraces().keySet().size());
	}

	
	public static void playAudioLoop(double[] left, double[] right) {
		if(line == null) getLine();
		getAudioBytes(left, right, 1.0);
		playContinuous = true;
		currentThread = new AudioPlayer();
		currentThread.start();
		//System.out.println(Thread.getAllStackTraces().keySet().size());
	}

	private static void getAudioBytes(double[] mono, double masterVolume) {
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

	private static void getAudioBytes(double[] left, double[] right, double masterVolume) {
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
