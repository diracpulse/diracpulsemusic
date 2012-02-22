
import java.lang.*;
import java.util.*;
import java.io.*;
import javax.sound.sampled.*;

class AudioBuffer {

	private double[] buffer;
	private int bufferLength;
	private int bufferIndex;
	private double samplingRate = 44100.0;
	
	AudioBuffer (int bufferLength) {
		this.bufferLength = bufferLength;
		buffer = new double[bufferLength];
		bufferIndex = 0;
		Arrays.fill(buffer, 0.0);
	}
	
	public boolean hasMore() {
		return bufferIndex < bufferLength;
	}

	public double[] getBuffer(int numSamples) {
		int endIndex = bufferIndex + numSamples;
		if(endIndex > bufferLength) endIndex = bufferLength;
		double[] returnVal = new double[numSamples];
		Arrays.fill(returnVal, 0.0);
		for(int index = bufferIndex; index < endIndex; index++) {
			returnVal[index - bufferIndex] = buffer[index];
		}
		bufferIndex = endIndex;
		return returnVal;	
	}
	
	public void addFrequency(double freqInHz) {
		double deltaPhase = (freqInHz * 2.0 * Math.PI) / samplingRate;
		double currentPhase = 0.0;
		for (int index = 0; index < bufferLength; index++) {
			buffer[index] = Math.sin(currentPhase);
			currentPhase += deltaPhase;
		}
		taperOff();
	}
	
	public void halfTaperOff() {
		int startTime = bufferLength / 2;
		for(int index = startTime; index < bufferLength; index++) {
			double numerator = index - startTime;
			double denominator = startTime;
			double window = numerator / denominator;
			buffer[index] *= (1.0 - window);
			//System.out.println(window);
		}
	}
	
	public void taperOff() {
		int startTime = 0;
		for(int index = startTime; index < bufferLength; index++) {
			double numerator = index;
			double denominator = bufferLength;
			double window = numerator / denominator;
			buffer[index] *= (1.0 - window);
			//System.out.println(window);
		}
	}
}
