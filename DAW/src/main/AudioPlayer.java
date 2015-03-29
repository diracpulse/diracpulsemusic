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
	public final static double fullScale = Short.MAX_VALUE;
	final static int frameSize = 1024;
	private static volatile boolean playContinuous = false;
	private static volatile byte[] audioByteData = null;
	private static volatile AudioPlayer currentThread = null;
	public static volatile long prevFramePosition = 0;
	
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
				int available = line.available();
				if(bytesLeftToWrite > available) {
					line.write(audioByteData, position, available);
					position += available;
					//System.out.println("Available");
				} else {
					line.write(audioByteData, position, bytesLeftToWrite);
					position = audioByteData.length;
					//System.out.println("Finished");
				}
				if(this != currentThread) return;
				if(interrupted()) return;
			}
			if(!playContinuous) {
				//line.drain();
				//System.out.println("Non-continuous");
				return;
			}
		}
	}
	
	public synchronized static void addToLine(double[] mono) {
		if(line == null) {
			getLine();
		}
		getAudioBytes(mono);
		while(line.available() < audioByteData.length) continue;
		line.write(audioByteData, 0, audioByteData.length);
	}
	
	public synchronized static void addToLine(double[] left, double[] right) {
		if(line == null) {
			getLine();
		}
		getAudioBytes(left, right);
		while(line.available() < audioByteData.length) continue;
		line.write(audioByteData, 0, audioByteData.length);
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

	public static void getAudioBytes(double[] mono, double masterVolume) {
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

	public static void getAudioBytes(double[] left, double[] right, double masterVolume) {
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
	
	public static void getAudioBytes(double[] mono) {
		if(mono == null) return;
		final int numberOfSamples = mono.length;
		double[] left = new double[numberOfSamples];
		double[] right = new double[numberOfSamples];
		int index;
		for (index = 0; index < numberOfSamples; index++) { 
			left[index] = mono[index];
			right[index] = mono[index];
		}
		getAudioBytes(left, right);
	}

	
	public static void getAudioBytes(double[] left, double[] right) {
		if(left == null || right == null) return;
		int numberOfSamples = right.length;
		if (left.length < right.length) numberOfSamples = left.length;
		//System.out.println("AudioPlayer.PlayBuffer: left samples = " + left.length + " | right samples = " + right.length);
		int numBytesToWrite = numberOfSamples * 4;
		audioByteData = new byte[numBytesToWrite];
		int leftSample;
		int rightSample;
		int sampleIndex;
		for (int index = 0; index < numberOfSamples; index++) {
			sampleIndex = index * 4;
			leftSample = (int) Math.round(left[index]);
			audioByteData[sampleIndex] = (byte) (leftSample & 0xFF);
			audioByteData[sampleIndex + 1] = (byte) (leftSample >> 8);
			rightSample = (int) Math.round(right[index]);
			audioByteData[sampleIndex + 2] = (byte) (rightSample & 0xFF);
			audioByteData[sampleIndex + 3] = (byte) (rightSample >> 8);			
		}
	}

}
