
class LogLinear {

	private int numSamples;
	public double[] envelope;
	public static double timeToSample = SynthTools.timeToSample;
	
	LogLinear(Double[] times, Double[] values) {
		int numTimes = times.length;
		if(numTimes != values.length) {
			System.out.println("ERROR: LogLinear: array length mismatch");
			return;
		}
		numSamples = (int) Math.round(times[times.length - 1] * timeToSample);
		int envIndex = 0;
		envelope = new double[numSamples];
		for(int arrayIndex = 0; arrayIndex < (numTimes - 1); arrayIndex++) {
			int lowerTime = (int) Math.round(times[arrayIndex] * timeToSample);
			int upperTime = (int) Math.round(times[arrayIndex + 1]* timeToSample);
			double lowerValue = values[arrayIndex];
			double upperValue = values[arrayIndex + 1];
			double slope = (upperValue - lowerValue) / (upperTime - lowerTime);
			for(int timeIndex = lowerTime; timeIndex < upperTime; timeIndex++) {
				double envValue = lowerValue + (timeIndex - lowerTime) * slope;
				envelope[envIndex] = Math.exp(envValue * Math.log(2.0));
				if(envIndex == numSamples) {
					//System.out.println("LogLinear: numSamples reached in loop");
					return;
				}
				if((envIndex % 441) == 0) {
					//System.out.println("LogLinear: " + envIndex + " | " + envelope[envIndex]);
				}
				envIndex++;
			}	
		}
		//System.out.println("LogLinear: envIndex = " + envIndex + ", numSamples = " + numSamples);
	}
	
	public double getSample(int sample) {
		int sampleIndex = sample;
		if((sampleIndex % 441) == 0) {
			//System.out.println("LogLinear.getSample(int): sample=" + sample + ", sample= " + envelope[sampleIndex]);
		}
		if((sampleIndex >= 0) && (sampleIndex < numSamples)) {
			return envelope[sampleIndex];
		}
		return 0.0;
	}
	
	public int getNumSamples() {
		return numSamples;
	}

}
