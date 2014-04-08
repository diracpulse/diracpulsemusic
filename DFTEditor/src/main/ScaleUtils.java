package main;

import java.util.Random;

public class ScaleUtils {
	
	public static final double[] minorChords = {2.0 / 3.0, 5.0 / 6.0, 1.0, 6.0 / 5.0, 3.0 / 2.0};
	
	public static double[] addMinorChords(double[] freqRatios, double noteBase, double minRatio) {
		Random random = new Random();
		double[] returnVal = new double[freqRatios.length];
		for(int index = 0; index < returnVal.length; index++) returnVal[index] = -1.0;
		int index = 0;
		while(index < freqRatios.length) {
			if(freqRatios[index] < 0) {
				index++;
				continue;
			}
			double currentRatio = freqRatios[index];
			double chordRatio = minorChords[random.nextInt(minorChords.length)];
			double outputRatio = currentRatio * chordRatio;
			while(outputRatio < minRatio || outputRatio >= minRatio + 1.0) outputRatio = currentRatio * minorChords[random.nextInt(minorChords.length)];
			outputRatio = Math.pow(2.0, Math.round(Math.log(outputRatio) / Math.log(2.0) * noteBase) / noteBase);
			while(freqRatios[index] == currentRatio) {
				returnVal[index] = outputRatio;
				index++;
				if(index == freqRatios.length) return returnVal;
			}
		}
		return returnVal;
	}
	
	public static double[] rhythm(double[] freqRatios) {
		return freqRatios;
	}
	
}
