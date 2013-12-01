package main.generators;

import main.TestSignals.Generator;
import main.TestSignals.TAPair;
import main.SynthTools;

public class Sawtooth implements Generator {
	
	double[] samples = null;
	double amplitude = 0.0;
	double freqInHz = 0.0;
	
	public Sawtooth(double freqInHz, TAPair durationAndAmplitude) {
		this.freqInHz = freqInHz;
		samples = new double[durationAndAmplitude.getTimeInSamples()];
		double deltaPhase = freqInHz / SynthTools.sampleRate * Math.PI;
		double phase = 0;
		amplitude = durationAndAmplitude.getAbsoluteAmplitude();
		for(int index  = 0; index < samples.length; index++) {
			samples[index] = Math.sin(phase) * amplitude;
			phase += deltaPhase;
		}
	}
	
	public double[] getSamples() {
		return samples;
	}
	
	public double[] addTo(double[] input) {
		double[] returnVal = null;
		if(input.length > samples.length) {
			returnVal = new double[input.length];
		} else {
			returnVal = new double[samples.length];
		}
		for(int index = 0; index < returnVal.length; index++) {
			if(index >= samples.length || index >= input.length) {
				returnVal[index] = 0.0;
				continue;
			}
			returnVal[index] = samples[index] + input[index];
		}
		return returnVal;
	}
	
	public double[] modulateAM(double[] input) {
		double[] returnVal = null;
		if(input.length > samples.length) {
			returnVal = new double[input.length];
		} else {
			returnVal = new double[samples.length];
		}
		for(int index = 0; index < returnVal.length; index++) {
			if(index >= samples.length || index >= input.length) {
				returnVal[index] = 0.0;
				continue;
			}
			returnVal[index] = samples[index] * input[index];
		}
		return returnVal;
	}
	
	public double[] modulateFM(double[] input) {
		double[] returnVal = null;
		if(input.length > samples.length) {
			returnVal = new double[input.length];
		} else {
			returnVal = new double[samples.length];
		}
		double deltaPhase = freqInHz / SynthTools.sampleRate * Math.PI;
		double phase = 0;
		for(int index  = 0; index < returnVal.length; index++) {
			returnVal[index] = Math.sin(phase + input[index]) * amplitude;
			phase += deltaPhase;
		}
		return returnVal;
	}
}
