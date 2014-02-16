package main;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import main.TestSignals.TAPair.AmplitudeFormat;
import main.TestSignals.TAPair.TimeFormat;


public class Interpolate {
		
	public static class TAPair {
		
		double timeInSeconds = 0.0;
		double absoluteAmplitude = 0.0;
		
		public TAPair(double time, double amplitude) {
			timeInSeconds = time;
			absoluteAmplitude = amplitude;
		}
		
		public double getLogAmplitude() {
			return Math.log(absoluteAmplitude)/Math.log(FDData.logBase);
		}
		
		public double getAbsoluteAmplitude() {
			return absoluteAmplitude;
		}
		
		public double getTimeInSeconds() {
			return timeInSeconds;
		}
		
		public int getTimeInSamples() {
			return (int) Math.round(timeInSeconds * SynthTools.sampleRate);
		}
	}		
		
	
	public static TreeMap<Integer, FDData> dataInterpolate(FDData.Channel channel, TreeMap<Integer, FDData> input) {
		TreeMap<Integer, FDData> output = new TreeMap<Integer, FDData>();
		if(input.isEmpty()) return output;
		if(input.size() == 1) {
			output.put(input.firstKey(), input.firstEntry().getValue());
			return output;
		}
		long harmonicID = input.firstEntry().getValue().getHarmonicID();
		int lowerTime = input.firstKey();
		double lowerAmpValue = input.get(lowerTime).getLogAmplitude();
		double lowerNoteValue = input.get(lowerTime).getNote();
		for(int upperTime: input.keySet()) {
			if(upperTime == lowerTime) continue;
			double upperAmpValue = input.get(upperTime).getLogAmplitude();
			double upperNoteValue = input.get(upperTime).getNote();
			double ampSlope = (upperAmpValue - lowerAmpValue) / (upperTime - lowerTime);
			double noteSlope = (upperNoteValue - lowerNoteValue) / (upperTime - lowerTime);
			for(int timeIndex = lowerTime; timeIndex < upperTime; timeIndex++) {
				double ampValue = lowerAmpValue + (timeIndex - lowerTime) * ampSlope;
				int noteValue = (int) Math.round(lowerNoteValue + (timeIndex - lowerTime) * noteSlope);
				try {
					//System.out.println(timeIndex + " " +  noteValue + " " +  ampValue + " " +  harmonicID);
					output.put(timeIndex, new FDData(channel, timeIndex, noteValue, ampValue, harmonicID));
				} catch (Exception e) {
					System.out.println("Interpolate.dataInterpolate(): error creating data");
					return null;
				}
			}
			lowerAmpValue = upperAmpValue;
			lowerNoteValue = upperNoteValue;
			lowerTime = upperTime;
		}
		return output;
	}
	
	public static double[] synthTAPairsLinear(ArrayList<TAPair> TAPairs) {
		double[] returnVal;
		if(TAPairs == null) return null;
		if(TAPairs.size() < 2) return null;
		returnVal = new double[TAPairs.get(TAPairs.size() - 1).getTimeInSamples() + 1];
		for(int index = 0; index < returnVal.length - 1; index++) {
			returnVal[index] = 0.0;
		}
		int maxArrayIndex = TAPairs.size();
		for(int arrayIndex = 0; arrayIndex < maxArrayIndex - 1; arrayIndex++) {
			int lowerTime = TAPairs.get(arrayIndex).getTimeInSamples();
			int upperTime = TAPairs.get(arrayIndex + 1).getTimeInSamples();
			double lowerAmplitude = TAPairs.get(arrayIndex).getAbsoluteAmplitude();
			double upperAmplitude = TAPairs.get(arrayIndex + 1).getAbsoluteAmplitude();
			if(upperTime - lowerTime <= 0) continue;
			double ampSlope = (upperAmplitude - lowerAmplitude) / (upperTime - lowerTime);
			for(int timeIndex = lowerTime; timeIndex < upperTime; timeIndex++) {
				if(timeIndex >= returnVal.length) break;
				returnVal[timeIndex] += lowerAmplitude + (timeIndex - lowerTime) * ampSlope;
			}	
		}
		return returnVal;
	}
	
	public static double[] synthTAPairsLog(ArrayList<TAPair> TAPairs) {
		double[] returnVal;
		if(TAPairs == null) return null;
		if(TAPairs.size() < 2) return null;
		returnVal = new double[TAPairs.get(TAPairs.size() - 1).getTimeInSamples() + 1];
		for(int index = 0; index < returnVal.length - 1; index++) {
			returnVal[index] = 0.0;
		}
		int maxArrayIndex = TAPairs.size();
		for(int arrayIndex = 0; arrayIndex < maxArrayIndex - 1; arrayIndex++) {
			int lowerTime = TAPairs.get(arrayIndex).getTimeInSamples();
			int upperTime = TAPairs.get(arrayIndex + 1).getTimeInSamples();
			double lowerAmplitude = TAPairs.get(arrayIndex).getLogAmplitude();
			double upperAmplitude = TAPairs.get(arrayIndex + 1).getLogAmplitude();
			if(upperTime - lowerTime <= 0) return null;
			double ampSlope = (upperAmplitude - lowerAmplitude) / (upperTime - lowerTime);
			for(int timeIndex = lowerTime; timeIndex < upperTime; timeIndex++) {
				if(timeIndex >= returnVal.length) break;
				returnVal[timeIndex] += lowerAmplitude + (timeIndex - lowerTime) * ampSlope;
			}	
		}
		for(int index = 0; index < returnVal.length; index++) {
			returnVal[index] = Math.pow(FDData.logBase, returnVal[index]);
		}
		return returnVal;
	}
	
	public static double[] synthTAPairsCubicSpline(ArrayList<TAPair> TAPairs) {
		double[] returnVal;
		if(TAPairs == null) return null;
		if(TAPairs.size() < 2) return null;
		returnVal = new double[TAPairs.get(TAPairs.size() - 1).getTimeInSamples() + 1];
		for(int index = 0; index < returnVal.length - 1; index++) {
			returnVal[index] = 0.0;
		}
		double[] times = new double[TAPairs.size()];
		double[] amps = new double[TAPairs.size()];
		for(int index = 0; index < TAPairs.size(); index++) {
			amps[index] = TAPairs.get(index).absoluteAmplitude;
			times[index] = TAPairs.get(index).getTimeInSamples();
		}
		CubicSpline timeToAmp = new CubicSpline(times, amps);
		int lowerTime = 0;
		int upperTime = (int) times[times.length - 1];
		for(int timeIndex = lowerTime; timeIndex < upperTime; timeIndex++) {
			if(timeIndex >= returnVal.length) break;
			returnVal[timeIndex] = timeToAmp.interpolate(timeIndex);
		}
		return returnVal;
	}
	
}
