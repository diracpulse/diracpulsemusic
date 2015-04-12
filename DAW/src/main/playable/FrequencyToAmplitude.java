
package main.playable;

import main.SynthTools;

public class FrequencyToAmplitude {
	
	private double[] inputHP = {0, 0};
	private double[] yHP = {0, 0};
	private double[] inputHPInv = {0, 0};
	private double[] yHPInv = {0, 0};
	private double[] inputLP = {0, 0};
	private double[] yLP = {0, 0};
	private double hpFreq = 1024.0;
	private double lpFreq = 1.0;
	
	double gamma = Math.tan((Math.PI * hpFreq) / SynthTools.sampleRate);
	double b0 = 1.0 / (gamma + 1);
	double b1 = -1.0 / (gamma + 1);
	double a0 = (gamma - 1) / (gamma + 1); 
	
	double gamma_16 = Math.tan((Math.PI * 16) / SynthTools.sampleRate);
	double b0_16 = 1.0 / (gamma_16 + 1);
	double b1_161 = -1.0 / (gamma_16 + 1);
	double a0_16 = (gamma_16 - 1) / (gamma_16 + 1); 
	
	public FrequencyToAmplitude() {}
	
	public double getAmplitude(double sample) {
		double out = butterworthHighpass(sample);
		double outInv = butterworthHighpassInverting(sample);
		if(out < 0) out = 0;
		if(outInv < 0) outInv = 0;
		return (out + outInv) / 2.0;
		//return butterworthLowpass((out + outInv) / 2.0, lpFreq);
	}
	
	private double butterworthHighpass(double sample) {
		inputHP[0] = inputHP[1];
		inputHP[1] = sample;
		yHP[0] = yHP[1];
		yHP[1] = b0 * inputHP[1] + b1 * inputHP[0] - a0 * yHP[0];
		return yHP[1];
	}
	
	public double butterworthHighpass16(double sample, double freq) {
		inputHP[0] = inputHP[1];
		inputHP[1] = sample;
		yHP[0] = yHP[1];
		double gamma = Math.tan((Math.PI * hpFreq) / SynthTools.sampleRate);
		double b0 = 1.0 / (gamma + 1);
		double b1 = -1.0 / (gamma + 1);
		double a0 = (gamma - 1) / (gamma + 1); 
		yHP[1] = b0 * inputHP[1] + b1 * inputHP[0] - a0 * yHP[0];
		return yHP[1];
	}
	
	private double butterworthHighpassInverting(double sample) {
		inputHPInv[0] = inputHPInv[1];
		inputHPInv[1] = -1.0 * sample;
		yHPInv[0] = yHPInv[1];
		yHPInv[1] = b0 * inputHPInv[1] + b1 * inputHPInv[0] - a0 * yHPInv[0];
		return yHPInv[1];
	}
	
	private double butterworthLowpass(double sample, double freq) {
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