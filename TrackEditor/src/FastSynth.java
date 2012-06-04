import java.util.ArrayList;


public class FastSynth {
	
	public static int numSamples = 0;
	private static double timeToSample = SynthTools.sampleRate * (FDData.timeStepInMillis / 1000.0);
	private static double[] sharedPCMData;
	
	public static double[] synthHarmonicsLinear(ArrayList<Harmonic> harmonics) {
		initSharedPCMData(harmonics);
		for(Harmonic harmonic: harmonics) synthHarmonicLinear(harmonic);
		return sharedPCMData;
	}
	
	public static double[] synthHarmonicsLinearCubicSpline(ArrayList<Harmonic> harmonics) {
		initSharedPCMData(harmonics);
		for(Harmonic harmonic: harmonics) synthHarmonicLinearCubicSpline(harmonic);
		return sharedPCMData;
	}
	
	private static void initSharedPCMData(ArrayList<Harmonic> harmonics) {
		double maxEndTime = 0;
		for(Harmonic harmonic: harmonics) {
			double harmonicEndTime = Math.ceil(harmonic.getEndTime());
			if(harmonicEndTime > maxEndTime) maxEndTime = harmonicEndTime;
		}
		int numSamples = (int) Math.ceil(maxEndTime * timeToSample);
		sharedPCMData = new double[numSamples];
		for(int index = 0; index < numSamples; index++) sharedPCMData[index] = 0.0;
	}
	
	private static void synthHarmonicLinear(Harmonic harmonic) {
		ArrayList<FDData> dataArray = new ArrayList<FDData>(harmonic.getAllDataInterpolated().values());
		int maxArrayIndex = dataArray.size();
		double currentPhase = 0.0;
		for(int arrayIndex = 0; arrayIndex < maxArrayIndex - 1; arrayIndex++) {
			int lowerTime = (int) Math.round(dataArray.get(arrayIndex).getTime() * timeToSample);
			int upperTime = (int) Math.round(dataArray.get(arrayIndex + 1).getTime() * timeToSample);
			double lowerAmplitude = Math.pow(2.0, dataArray.get(arrayIndex).getLogAmplitude());
			double upperAmplitude = Math.pow(2.0, dataArray.get(arrayIndex + 1).getLogAmplitude());
			double lowerFreq = Math.pow(2.0, (double) dataArray.get(arrayIndex).getNoteComplete() / FDData.noteBase);
			double upperFreq = Math.pow(2.0, (double) dataArray.get(arrayIndex + 1).getNoteComplete() / FDData.noteBase);
			double lowerDeltaPhase = (lowerFreq / SynthTools.sampleRate) * SynthTools.twoPI;
			double upperDeltaPhase = (upperFreq / SynthTools.sampleRate) * SynthTools.twoPI;
			double ampSlope = (upperAmplitude - lowerAmplitude) / (upperTime - lowerTime);
			double deltaPhaseSlope = (upperDeltaPhase - lowerDeltaPhase) / (upperTime - lowerTime);
			for(int timeIndex = lowerTime; timeIndex < upperTime; timeIndex++) {
				double amplitude = lowerAmplitude + (timeIndex - lowerTime) * ampSlope;
				double deltaPhase = lowerDeltaPhase + (timeIndex - lowerTime) * deltaPhaseSlope;
				sharedPCMData[timeIndex] += Math.sin(currentPhase) * amplitude;
				currentPhase += deltaPhase;
				if(currentPhase > SynthTools.twoPI) currentPhase -= SynthTools.twoPI;
			}	
		}
	}
	
	private static void synthHarmonicLinearCubicSpline(Harmonic harmonic) {
		ArrayList<FDData> dataArray = new ArrayList<FDData>(harmonic.getAllDataInterpolated().values());
		if(dataArray.size() < 2) return;
		double[] times = new double[dataArray.size()];
		double[] amps = new double[dataArray.size()];
		double[] freqs = new double[dataArray.size()];
		for(int index = 0; index < dataArray.size(); index++) {
			times[index] = Math.round(dataArray.get(index).getTime() * timeToSample);
			amps[index] = dataArray.get(index).getAmplitude();
			freqs[index] = Math.pow(2.0, (double) dataArray.get(index).getNoteComplete() / FDData.noteBase);
		}
		CubicSpline timeToFreq = new CubicSpline(times, freqs);
		CubicSpline timeToAmp = new CubicSpline(times, amps);
		int lowerTime = (int) Math.round(dataArray.get(0).getTime() * timeToSample);
		int upperTime = (int) Math.round(dataArray.get(dataArray.size() - 1).getTime() * timeToSample);
		double currentPhase = 0.0;
		for(int timeIndex = lowerTime; timeIndex < upperTime; timeIndex++) {
			double amplitude = timeToAmp.interpolate(timeIndex);
			double frequency = timeToFreq.interpolate(timeIndex);
			double deltaPhase = (frequency / SynthTools.sampleRate) * SynthTools.twoPI;
			sharedPCMData[timeIndex] += Math.sin(currentPhase) * amplitude;
			currentPhase += deltaPhase;
			if(currentPhase > SynthTools.twoPI) currentPhase -= SynthTools.twoPI;
		}	
	}

}
