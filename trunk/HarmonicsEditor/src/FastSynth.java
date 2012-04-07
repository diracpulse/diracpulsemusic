import java.util.ArrayList;


public class FastSynth {
	
	public static int numSamples = 0;
	private static double timeToSample = SynthTools.sampleRate * (FDData.timeStepInMillis / 1000.0);
	private static double[] sharedPCMData;
	
	public static double[] synthHarmonics(ArrayList<Harmonic> harmonics) {
		initSharedPCMData(harmonics);
		for(Harmonic harmonic: harmonics) synthHarmonic(harmonic);
		return sharedPCMData;
	}
	
	private static void initSharedPCMData(ArrayList<Harmonic> harmonics) {
		double maxEndTime = 0;
		for(Harmonic harmonic: harmonics) {
			double harmonicEndTime = Math.ceil(harmonic.getEndTime() + harmonic.getTaperLength());
			if(harmonicEndTime > maxEndTime) maxEndTime = harmonicEndTime;
		}
		int numSamples = (int) Math.ceil(maxEndTime * timeToSample);
		sharedPCMData = new double[numSamples];
		for(int index = 0; index < numSamples; index++) sharedPCMData[index] = 0.0;
	}
	
	private static void synthHarmonic(Harmonic harmonic) {
		int minNote = FDData.getMaxNote() + 1;
		int maxNote = FDData.getMinNote() - 1;
		for(FDData data: harmonic.getAllData()) {
			if(data.getNote() < minNote) minNote = data.getNote();
			if(data.getNote() > maxNote) maxNote = data.getNote();
		}
		if(maxNote - minNote < 3) {
			synthHarmonicFlat(harmonic);
		}
		synthHarmonicFlat(harmonic);
	}
	
	private static void synthHarmonicFlat(Harmonic harmonic) {
		ArrayList<FDData> dataArray = harmonic.getAllData();
		FDData taperData = null;
		FDData endData = dataArray.get(dataArray.size() - 1);
		int harmonicEndTime = (int) Math.ceil(harmonic.getEndTime() + harmonic.getTaperLength());
		try {
			taperData = new FDData(harmonicEndTime, endData.getNote(), 0.0, endData.getHarmonicID());
		} catch (Exception e) {
			System.out.println("FastSynth.synthHarmonicFlat: Error creating taper data");
		}
		dataArray.add(taperData);
		int maxArrayIndex = dataArray.size();
		double currentPhase = 0.0;
		double averageFreq = Math.pow(2.0, (double) harmonic.getAverageNote() / (double) FDData.noteBase);
		double deltaPhase = (averageFreq / SynthTools.sampleRate) * SynthTools.twoPI;
		for(int arrayIndex = 0; arrayIndex < maxArrayIndex - 1; arrayIndex++) {
			int lowerTime = (int) Math.round(dataArray.get(arrayIndex).getTime() * timeToSample);
			int upperTime = (int) Math.round(dataArray.get(arrayIndex + 1).getTime() * timeToSample);
			double lowerLogAmplitude = dataArray.get(arrayIndex).getLogAmplitude();
			double upperLogAmplitude = dataArray.get(arrayIndex + 1).getLogAmplitude();
			double slope = (upperLogAmplitude - lowerLogAmplitude) / (upperTime - lowerTime);
			for(int timeIndex = lowerTime; timeIndex < upperTime; timeIndex++) {
				double logAmplitude = lowerLogAmplitude + (timeIndex - lowerTime) * slope;
				double amplitude = Math.exp(logAmplitude * Math.log(2.0));
				sharedPCMData[timeIndex] += Math.sin(currentPhase) * amplitude;
				currentPhase += deltaPhase;
				if(currentPhase > SynthTools.twoPI) currentPhase -= SynthTools.twoPI;
			}	
		}
	}

}
