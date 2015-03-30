package main.playable;

import main.Filter;
import main.SynthTools;

public class Waveforms {

	private static boolean tablesCalculated = false;
	private final int lookupTableLength = 1024;
	private final double deltaPhase = 1.0 / lookupTableLength;
	private static double[] sawtoothTable = null;
	private static double[] squarewaveTable = null;
	private static double[] triangleTable = null;
	
	public Waveforms() {
		if(tablesCalculated) return;
		sawtoothTable = new double[lookupTableLength];
		squarewaveTable = new double[lookupTableLength];
		triangleTable = new double[lookupTableLength];
		int interpolate = 256;
		double bins = 2.0;
		double alpha = 10.0;
		double samplesPerCycle = 4.0;
		int filterLength = (int) Math.round(bins * samplesPerCycle * interpolate);
		filterLength += filterLength % 2;
		double[] filter = Filter.getLPFilter(AudioFetcher.sampleRate / (samplesPerCycle * interpolate), filterLength, alpha);
		for(int index = 0; index < lookupTableLength * interpolate; index += interpolate) {
			int indexOut = index / interpolate;
			sawtoothTable[indexOut] = 0.0;
			squarewaveTable[indexOut] = 0.0;
			triangleTable[indexOut] = 0.0;
			for(int filterIndex = 0; filterIndex < filter.length; filterIndex++) {
				int innerIndex = index + filterIndex - filter.length / 2;
				//if(innerIndex < 0) continue;
				//if(innerIndex == lookupTableLength) break;
				double currentPhase = innerIndex * deltaPhase / interpolate;
				sawtoothTable[indexOut] += calcSawtooth(currentPhase) * filter[filterIndex];
				squarewaveTable[indexOut] += calcSquarewave(currentPhase) * filter[filterIndex];
				triangleTable[indexOut] += calcTriangle(currentPhase) * filter[filterIndex];
			}
		}
		tablesCalculated = true;
	}

	public double sawtooth(double phase) {
		phase /= Math.PI * 2.0;
		phase -= Math.floor(phase);
		double dIndex = phase * lookupTableLength;
		int index = (int) Math.floor(dIndex);
		if(index == dIndex) return sawtoothTable[index];
		double fraction = (phase * lookupTableLength) - index;
		double returnVal = sawtoothTable[index];
		double delta = sawtoothTable[(index + 1) % lookupTableLength] - returnVal;
		return returnVal + fraction * delta;
	}
	
	public double triangle(double phase) {
		phase /= Math.PI * 2.0;
		phase -= Math.floor(phase);
		double dIndex = phase * lookupTableLength;
		int index = (int) Math.floor(dIndex);
		if(index == dIndex) return triangleTable[index];
		double fraction = (phase * lookupTableLength) - index;
		double returnVal = triangleTable[index];
		double delta = triangleTable[(index + 1) % lookupTableLength] - returnVal;
		return returnVal + fraction * delta;
	}
	
	public double squarewave(double phase) {
		return squarewave(phase, 0.5);
	}
	
	public double squarewave(double phase, double pwm) {
		if(pwm < 0.1) pwm = 0.1;
		if(pwm > 0.9) pwm = 0.9;
		phase /= Math.PI * 2.0;
		phase -= Math.floor(phase);
		if(pwm > phase - 0.05 && pwm < phase - 0.05) {
			phase = 0.5 + (phase - pwm);
			double dIndex = phase * lookupTableLength;
			int index = (int) Math.floor(dIndex);
			if(index == dIndex) return squarewaveTable[index];
			double fraction = (phase * lookupTableLength) - index;
			double returnVal = squarewaveTable[index % lookupTableLength];
			double delta = squarewaveTable[(index + 1) % lookupTableLength] - returnVal;
			return returnVal + fraction * delta;
		}
		if(phase < 0.05 || phase > 0.95) {
			double dIndex = phase * lookupTableLength;
			int index = (int) Math.floor(dIndex);
			if(index == dIndex) return squarewaveTable[index];
			double fraction = (phase * lookupTableLength) - index;
			double returnVal = squarewaveTable[index % lookupTableLength];
			double delta = squarewaveTable[(index + 1) % lookupTableLength] - returnVal;
			return returnVal + fraction * delta;
		}
		if(phase < pwm) return 0.0;
		return 1.0;
	}

	private static double calcSawtooth(double phase) {
		phase -= Math.floor(phase);
		if(phase <= 0.5) return phase * 2.0;
		return (-1.0 + phase) * 2.0;
	}

	private static double calcSquarewave(double phase) {
		phase -= Math.floor(phase);
		if(phase < 0.5) return 1.0;
		return -1.0;
	}

	private static double calcTriangle(double phase) {
		phase -= Math.floor(phase);
		if(phase < 0.25) return phase / 0.25;
		if(phase < 0.75) return 1.0 - (phase - 0.25) / 0.25;
		return -1.0 + (phase - 0.75) / 0.25;
	}

	
}