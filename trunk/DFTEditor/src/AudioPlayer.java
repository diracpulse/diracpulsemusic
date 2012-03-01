import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class AudioPlayer extends Thread {

	SourceDataLine line;
	
	final float sampleRate = 44100f;
	final int bitsPerSample = 16; 
	final int channels = 2;
	final boolean signed = true;
	final boolean bigEndian = false;
	final double fullScale = Short.MAX_VALUE - 1;
	private double[] mono;
	private double masterVolume;

	
	AudioPlayer (DFTEditor parent, double[] mono, double masterVolume) {
		this.mono = mono;
		this.masterVolume = masterVolume;
	}
	
	public void run() {

		AudioFormat format = new AudioFormat(sampleRate, bitsPerSample, channels, signed, bigEndian);
		
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format); 
		
		if (AudioSystem.isLineSupported(info)) {
    		try {
        		line = (SourceDataLine) AudioSystem.getLine(info);
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
		PlayBuffer(mono, masterVolume);
		
	}

	public double getSampleRate() {
		return (double) sampleRate;
	}
	
	public void PlayBuffer(byte[] stereo) {
		line.write(stereo, 0, stereo.length);
	}
	
	public void PlayBuffer(double[] mono, double masterVolume) {
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
	public void PlayBuffer(double[] left, double right[], double masterVolume) {
		if (left.length != right.length) return; 
		int numberOfSamples = left.length;
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
		line.write(audioByteData, offset, numBytesToWrite);
	}

	public void SoundOff() {
		
		line.drain();
		line.stop();
		line.close();
		line = null;
		
		return;
	}
}
	