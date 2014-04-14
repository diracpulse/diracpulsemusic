package main;

import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;

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
		double[] returnVal = new double[freqRatios.length];
		for(int index = 0; index < returnVal.length; index++) returnVal[index] = -1.0;
		TreeMap<Integer, Integer> startToEnd = new TreeMap<Integer, Integer>();
		ArrayList<Double> noteFreqRatios = new ArrayList<Double>();
		int controlIndex = 0;
		while(true) {
			if(freqRatios == null) break;
			if(freqRatios.length == 0) break;
			while(freqRatios[controlIndex] < 0.0) {
				controlIndex++;
				if(controlIndex == freqRatios.length) break;
			}
			if(controlIndex == freqRatios.length) break;
			int start = controlIndex;
			double freqRatio = freqRatios[controlIndex];
			while(freqRatios[controlIndex] == freqRatio) {
				controlIndex++;
				if(controlIndex == freqRatios.length) break;
			}
			if(controlIndex == freqRatios.length) {
				startToEnd.put(start, controlIndex - 1);
				break;
			}
			startToEnd.put(start, controlIndex);
			noteFreqRatios.add(freqRatio);
		}
		int numNotes = startToEnd.size();
		double sequenceLength = startToEnd.get(startToEnd.lastKey()) * Sequencer.secondsPerPixel;
		double[] noteLengths = new double[numNotes];
		double[] pauseLengths = new double[numNotes];
		double lengthSum = 0.0;
		for(int index = 0; index < numNotes; index++) {
			noteLengths[index] = (Math.random() + 1.0);
			pauseLengths[index] = Math.random();
			lengthSum += noteLengths[index] + pauseLengths[index];
		}
		for(int index = 0; index < numNotes; index++) {
			noteLengths[index] = noteLengths[index] * sequenceLength / lengthSum;
			pauseLengths[index] = pauseLengths[index] * sequenceLength / lengthSum;
		}
		int startNote = 0;
		for(int index = 0; index < numNotes; index++) {
			startNote += (int) Math.round(pauseLengths[index] / Sequencer.secondsPerPixel);
			int endNote = (int) Math.round(noteLengths[index] / Sequencer.secondsPerPixel + startNote);
			double freqRatio = noteFreqRatios.get(index);
			for(int innerIndex = startNote; innerIndex < endNote; innerIndex++) {
				returnVal[innerIndex] = freqRatio;
			}
			startNote = endNote;
		}
		return returnVal;
	}
	
}
