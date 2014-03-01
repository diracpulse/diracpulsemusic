package main;

import java.util.ArrayList;

public class FastSynth {
	
	public static int numSamples = 0;
	private static double[] sharedPCMData;
	private static double timeToSample;
	
	public static double[] synthHarmonicsLinear(FDData.Channel channel, ArrayList<Harmonic> harmonics) {
		initSharedPCMData(channel, harmonics);
		for(Harmonic harmonic: harmonics) {
			if(harmonic.getChannel() != channel) continue; 
			synthHarmonicLinear(harmonic);
		}
		return sharedPCMData;
	}
	
	public static double[] synthHarmonicsLinearCubicSpline(FDData.Channel channel, ArrayList<Harmonic> harmonics) {
		initSharedPCMData(channel, harmonics);
		for(Harmonic harmonic: harmonics) {
			if(harmonic.getChannel() != channel) continue;
			synthHarmonicLinearCubicSpline(harmonic);
		}
		return sharedPCMData;
	}
	
	private static void initSharedPCMData(FDData.Channel channel, ArrayList<Harmonic> harmonics) {
		timeToSample = SynthTools.sampleRate * (FDData.timeStepInMillis / 1000.0);
		double maxEndTime = 0;
		for(Harmonic harmonic: harmonics) {
			if(harmonic.getChannel() != channel) continue;
			double harmonicEndTime = Math.ceil(harmonic.getEndTime());
			if(harmonicEndTime > maxEndTime) maxEndTime = harmonicEndTime;
		}
		int numSamples = (int) Math.ceil(DFTEditor.maxTime * timeToSample); // (int) Math.ceil(maxEndTime * timeToSample);
		sharedPCMData = new double[numSamples];
		for(int index = 0; index < numSamples; index++) sharedPCMData[index] = 0.0f;
	}

	private static void synthHarmonicLinear(Harmonic harmonic) {
		double timeToSample = SynthTools.sampleRate * (FDData.timeStepInMillis / 1000.0);
		ArrayList<FDData> dataArray = new ArrayList<FDData>(harmonic.getAllDataInterpolated().values());
		if(dataArray.size() < 2) return; // this is here to make identical to cubic spline
		int maxArrayIndex = dataArray.size();
		double currentPhase = 0.0;
		for(int arrayIndex = 0; arrayIndex < maxArrayIndex - 1; arrayIndex++) {
			int lowerTime = (int) Math.round(dataArray.get(arrayIndex).getTime() * timeToSample);
			int upperTime = (int) Math.round(dataArray.get(arrayIndex + 1).getTime() * timeToSample);
			double lowerAmplitude = Math.pow(2.0, dataArray.get(arrayIndex).getLogAmplitude());
			double upperAmplitude = Math.pow(2.0, dataArray.get(arrayIndex + 1).getLogAmplitude());
			double lowerFreq = Math.pow(2.0, (double) dataArray.get(arrayIndex).getNote() / FDData.noteBase);
			double upperFreq = Math.pow(2.0, (double) dataArray.get(arrayIndex + 1).getNote() / FDData.noteBase);
			double lowerDeltaPhase = (lowerFreq / SynthTools.sampleRate) * SynthTools.twoPI;
			double upperDeltaPhase = (upperFreq / SynthTools.sampleRate) * SynthTools.twoPI;
			double ampSlope = (upperAmplitude - lowerAmplitude) / (upperTime - lowerTime);
			double deltaPhaseSlope = (upperDeltaPhase - lowerDeltaPhase) / (upperTime - lowerTime);
			for(int timeIndex = lowerTime; timeIndex < upperTime; timeIndex++) {
				if(timeIndex >= sharedPCMData.length) break;
				double amplitude = lowerAmplitude + (timeIndex - lowerTime) * ampSlope;
				double deltaPhase = lowerDeltaPhase + (timeIndex - lowerTime) * deltaPhaseSlope;
				sharedPCMData[timeIndex] += Math.sin(currentPhase) * amplitude;
				currentPhase += deltaPhase;
				if(currentPhase > SynthTools.twoPI) currentPhase -= SynthTools.twoPI;
			}	
		}
	}
	
	private static void synthHarmonicLinearCubicSpline(Harmonic harmonic) {
		double timeToSample = SynthTools.sampleRate * (FDData.timeStepInMillis / 1000.0);
		ArrayList<FDData> dataArray = new ArrayList<FDData>(harmonic.getAllDataInterpolated().values());
		if(dataArray.size() < 2) return;  // this is here because of cubic spline
		double[] times = new double[dataArray.size()];
		double[] amps = new double[dataArray.size()];
		double[] freqs = new double[dataArray.size()];
		for(int index = 0; index < dataArray.size(); index++) {
			times[index] = Math.round(dataArray.get(index).getTime() * timeToSample);
			amps[index] = dataArray.get(index).getAmplitude();
			freqs[index] = Math.pow(2.0, (double) dataArray.get(index).getNote() / FDData.noteBase);
		}
		CubicSpline timeToFreq = new CubicSpline(times, freqs);
		CubicSpline timeToAmp = new CubicSpline(times, amps);
		int lowerTime = (int) Math.round(dataArray.get(0).getTime() * timeToSample);
		int upperTime = (int) Math.round(dataArray.get(dataArray.size() - 1).getTime() * timeToSample);
		double currentPhase = 0.0;
		for(int timeIndex = lowerTime; timeIndex < upperTime; timeIndex++) {
			if(timeIndex >= sharedPCMData.length) break;
			double amplitude = timeToAmp.interpolate(timeIndex);
			double frequency = timeToFreq.interpolate(timeIndex);
			double deltaPhase = (frequency / SynthTools.sampleRate) * SynthTools.twoPI;
			sharedPCMData[timeIndex] += Math.sin(currentPhase) * amplitude;
			currentPhase += deltaPhase;
			if(currentPhase > SynthTools.twoPI) currentPhase -= SynthTools.twoPI;
		}	
	}

}
