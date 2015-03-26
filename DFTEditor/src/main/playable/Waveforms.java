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
		int interpolate = 32;
		double bins = 8.0;
		double alpha = 5.0;
		double freqInHz = AudioFetcher.sampleRate / (interpolate * 2.0);
		double samplesPerCycle = AudioFetcher.sampleRate / freqInHz;
		int filterLength = (int) Math.round(bins * samplesPerCycle);
		filterLength += filterLength % 2;
		double[] filter = Filter.getLPFilter(freqInHz, filterLength, alpha);
		for(int index = 0; index < lookupTableLength; index++) {
			sawtoothTable[index] = 0.0;
			squarewaveTable[index] = 0.0;
			triangleTable[index] = 0.0;
			for(int filterIndex = 0; filterIndex < filter.length; filterIndex++) {
				int innerIndex = index + filterIndex - filter.length / 2;
				if(innerIndex < 0) continue;
				if(innerIndex == lookupTableLength) break;
				double currentPhase = innerIndex * deltaPhase;
				sawtoothTable[index] += calcSawtooth(currentPhase) * filter[filterIndex];
				squarewaveTable[index] += calcSquarewave(currentPhase) * filter[filterIndex];
				triangleTable[index] += calcTriangle(currentPhase) * filter[filterIndex];
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
		phase /= Math.PI * 2.0;
		phase -= Math.floor(phase);
		double dIndex = phase * lookupTableLength;
		int index = (int) Math.floor(dIndex);
		if(index == dIndex) return squarewaveTable[index];
		double fraction = (phase * lookupTableLength) - index;
		double returnVal = squarewaveTable[index];
		double delta = squarewaveTable[(index + 1) % lookupTableLength] - returnVal;
		return returnVal + fraction * delta;
	}

	private static double calcSawtooth(double phase) {
		phase -= Math.floor(phase);
		if(phase <= Math.PI) return phase;
		return -1.0 + phase;
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