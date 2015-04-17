
package main.playable;

import main.Filter;
import main.SynthTools;

public class Filters {
	
	private double[] inputHP = {0, 0};
	private double[] yHP = {0, 0};
	private double[] inputHPInv = {0, 0};
	private double[] yHPInv = {0, 0};
	private double[] inputLP = {0, 0};
	private double[] yLP = {0, 0};
	private double hpFreq = 512.0;
	private double hpFreqFIR = 128.0;
	
	private double[] filter;
	private double[] samples;
	private double[] filterInv;
	private double[] samplesInv;
	
	double gamma = Math.tan((Math.PI * hpFreq) / SynthTools.sampleRate);
	double b0 = 1.0 / (gamma + 1);
	double b1 = -1.0 / (gamma + 1);
	double a0 = (gamma - 1) / (gamma + 1); 
	
	double gamma_16 = Math.tan((Math.PI * 16) / SynthTools.sampleRate);
	double b0_16 = 1.0 / (gamma_16 + 1);
	double b1_16 = -1.0 / (gamma_16 + 1);
	double a0_16 = (gamma_16 - 1) / (gamma_16 + 1); 
	
	public Filters() {
		createFilter();
	}
	
	public double getAmplitude(double sample) {
		double out = butterworthHighpass(sample);
		double outInv = butterworthHighpassInverting(sample);
		if(out < 0) out = 0;
		if(outInv < 0) outInv = 0;
		return (out + outInv) / 2.0;
	}
	
	public double highpassFIR(double sample) {
		if(sample > 1.0) sample = 1.0;
		if(sample < -1.0) sample = -1.0;
		double returnVal = 0.0;
		for(int index = 1; index < samples.length; index++) {
			samples[index - 1] = samples[index];
		}
		samples[samples.length - 1] = sample;
		for(int filterIndex = 0; filterIndex < filter.length; filterIndex++) {
			returnVal += samples[filterIndex] * filter[filterIndex];
		}
		return returnVal;
	}
	
	public double highpassFIRInv(double sample) {
		sample *= -1.0;
		if(sample > 1.0) sample = 1.0;
		if(sample < -1.0) sample = -1.0;
		double returnVal = 0.0;
		for(int index = 1; index < samplesInv.length; index++) {
			samplesInv[index - 1] = samplesInv[index];
		}
		samplesInv[samplesInv.length - 1] = sample;
		for(int filterIndex = 0; filterIndex < filterInv.length; filterIndex++) {
			returnVal += samplesInv[filterIndex] * filterInv[filterIndex];
		}
		return returnVal;
	}
	
	private void createFilter() {
	   	double freqInHz = 64.0;
	   	double bins = 2.0;
		double alpha = 5.0;
		double samplesPerCycle = SynthTools.sampleRate / freqInHz;
		int filterLength = (int) Math.round(bins * samplesPerCycle);
		filterLength += filterLength % 2;
		filter = Filter.getHPFilter(freqInHz, filterLength, alpha);
		samples = new double[filter.length];
		filterInv = Filter.getHPFilter(freqInHz, filterLength, alpha);
		samplesInv = new double[filter.length];
	}
	
	public double butterworthHighpass(double sample) {
		if(sample > 1.0) sample = 1.0;
		if(sample < -1.0) sample = -1.0;
		inputHP[0] = inputHP[1];
		inputHP[1] = sample;
		yHP[0] = yHP[1];
		yHP[1] = b0 * inputHP[1] + b1 * inputHP[0] - a0 * yHP[0];
		//if(yHP[1] > 1.0) yHP[1] = 1.0;
		//if(yHP[1] < -1.0) yHP[1] = -1.0;
		return yHP[1];
	}
	
	public double butterworthHighpass16(double sample) {
		if(sample > 1.0) sample = 1.0;
		if(sample < -1.0) sample = -1.0;
		inputHP[0] = inputHP[1];
		inputHP[1] = sample;
		yHP[0] = yHP[1];
		yHP[1] = b0_16 * inputHP[1] + b1_16 * inputHP[0] - a0_16 * yHP[0];
		return yHP[1];
	}
	
	private double butterworthHighpassInverting(double sample) {
		if(sample > 1.0) sample = 1.0;
		if(sample < -1.0) sample = -1.0;
		inputHPInv[0] = inputHPInv[1];
		inputHPInv[1] = -1.0 * sample;
		yHPInv[0] = yHPInv[1];
		yHPInv[1] = b0 * inputHPInv[1] + b1 * inputHPInv[0] - a0 * yHPInv[0];
		//if(yHPInv[1] > 1.0) yHPInv[1] = 1.0;
		//if(yHPInv[1] < -1.0) yHPInv[1] = -1.0;
		return yHPInv[1];
	}
	
	private double butterworthLowpass(double sample, double freq) {
		if(sample > 1.0) sample = 1.0;
		if(sample < -1.0) sample = -1.0;
		inputLP[0] = inputLP[1];
		inputLP[1] = sample;
		yLP[0] = yLP[1];
		double gamma = Math.tan((Math.PI * freq) / SynthTools.sampleRate);
		double b0 = gamma / (gamma + 1);
		double b1 = b0;
		double a0 = (gamma - 1) / (gamma + 1); 
		yLP[1] = b0 * inputLP[1] + b1 * inputLP[0] - a0 * yLP[0];
		return yLP[1];
	}
	
}