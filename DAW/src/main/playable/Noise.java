
package main.playable;

import java.util.Random;

import main.SynthTools;

public class Noise {
	
	// Filter State
	Random random;
	final double minFreq = 64.0;
	final double maxFreq = 16000.0;
	double logMinFreq;
	double logMaxFreq;
	double logFreqRange;
	double[] y = {0.0, 0.0, 0.0}; 
	double[] input = {0.0, 0.0, 0.0};

	public Noise() {
		random = new Random();
		logMinFreq = Math.log(minFreq);
		logMaxFreq = Math.log(maxFreq);
		logFreqRange = logMaxFreq - logMinFreq;
	}
	
	// Control value varies from 0 to 1
	public double getSample(double controlVal) {
		double freq = Math.exp(logFreqRange * controlVal + logMinFreq);
		return butterworthLowpass1(random.nextDouble(), freq);
	}
	
	private double butterworthLowpass1(double sample, double freq) {
		input[0] = input[1];
		input[1] = sample;
		y[0] = y[1];
		double gamma = Math.tan((Math.PI * freq) / SynthTools.sampleRate);
		double b0 = gamma / (gamma + 1);
		double b1 = b0;
		double a0 = (gamma - 1) / (gamma + 1); 
		y[1] = b0 * input[1] + b1 * input[1] - a0 * y[0];
		return y[1];
	}
	
}