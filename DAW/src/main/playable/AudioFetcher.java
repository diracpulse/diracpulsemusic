
package main.playable;

import java.util.concurrent.atomic.AtomicLong;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class AudioFetcher extends Thread implements LineListener {
	
	final static float sampleRate = 44100f;
	final static int bitsPerSample = 16; 
	final static int channels = 2;
	final static boolean signed = true;
	final static boolean bigEndian = false;
	final static double fullScale = Short.MAX_VALUE;
	final static int frameSize = 4096;
	private AudioSource audioSource;
	private static AtomicLong framePosition = new AtomicLong(0);
	private static SourceDataLine line;
	
	public AudioFetcher(AudioSource audioSource) {
		this.audioSource = audioSource;
	}
	
	private SourceDataLine getLine() {
		
		SourceDataLine line = null;
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
		return line;
	}

	public void run() {
		line = getLine();
		line.addLineListener(this);
		framePosition.set(frameSize * 2);
		byte[] audioByteData = getAudioBytes(audioSource.getNextSamples(frameSize * 2));
		line.write(audioByteData, 0, audioByteData.length);
		while(true) {
			if(line.getLongFramePosition() + frameSize > framePosition.get()) {
				framePosition.set(framePosition.get() + frameSize);
				double[] samples = audioSource.getNextSamples(frameSize);
				boolean clippingReported = false;
				for(int index = 0; index < samples.length; index++) {
					if(Math.abs(samples[index]) > 1.0) {
						if(!clippingReported) {
							System.out.println("Clipping");
							clippingReported = true;
						}
						if(samples[index] > 1.0) { 
							samples[index] = 1.0;
						} else {
							samples[index] = -1.0;
						}
					}
				}
				audioByteData = getAudioBytes(samples);
				line.write(audioByteData, 0, audioByteData.length);
			}
		}
	}

	private byte[] getAudioBytes(double[] mono) {
		byte[] audioByteData = new byte[mono.length * 4];
		for (int index = 0; index < mono.length; index++) {
			int sampleIndex = index * 4;
			int sample = (int) Math.round(mono[index] * fullScale);
			audioByteData[sampleIndex] = (byte) (sample & 0xFF);
			audioByteData[sampleIndex + 1] = (byte) (sample >> 8);
			audioByteData[sampleIndex + 2] = (byte) (sample & 0xFF);
			audioByteData[sampleIndex + 3] = (byte) (sample >> 8);			
		}
		return audioByteData;
	}

	@Override
	public void update(LineEvent arg0) {
		//System.out.println(arg0.toString());
	}

}