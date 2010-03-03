
class LogLinear {

	boolean logInterpolate = true;
	int numSamples;
	int startSample;
	int endSample;
	double[] envelope;
	double sampleRate = 44100.0;
	
	LogLinear(double[] times, double[] amps) {
		int numTimes = times.length;
		if(numTimes != amps.length) {
			System.out.println("ERROR: LogLinear: array length mismatch");
			return;
		}
		// double logValue; changed from logarithmic interpolation
		double envValue;
		int arrayIndex;
		double timeIndex;
		int envIndex = 0;
		double timeStep = 1.0 / sampleRate;
		startSample = (int) Math.round(times[0] * sampleRate);
		endSample = (int) Math.round(times[numTimes - 1] * sampleRate);
		numSamples = endSample - startSample;
		// System.out.println("LogLinear: endSample = " + endSample + ", numSamples = " + numSamples);
		envelope = new double[numSamples];
		// System.out.println("LogLinear: endSample = " + endSample + ", numSamples = " + numSamples);
		for(arrayIndex = 0; arrayIndex < (numTimes - 1); arrayIndex++) {
			double lowerAmp;
			double upperAmp;
			double lowerTime = times[arrayIndex];
			double upperTime = times[arrayIndex + 1];
			if (logInterpolate) {
				lowerAmp = amps[arrayIndex]; // log interpolate
				upperAmp = amps[arrayIndex + 1]; // log interpolate
			} else {
				lowerAmp = Math.pow(2.0, amps[arrayIndex]); // linear interpolate
				upperAmp = Math.pow(2.0, amps[arrayIndex + 1]); // linear interpolate
			}		
			double slope = (upperAmp - lowerAmp) / (upperTime - lowerTime);
			for(timeIndex = lowerTime; timeIndex < upperTime; timeIndex += timeStep) {
				envValue = lowerAmp + (timeIndex - lowerTime) * slope;
				if (logInterpolate) {
					envValue = Math.pow(2.0, envValue);
				}
				envelope[envIndex] = envValue;
				envIndex++;
				if(envIndex == numSamples) {
					// System.out.println("LogLinear: numSamples reached in loop");
					return;
				}
				if((envIndex % 441) == 0) {
					// System.out.println("LogLinear: envIndex=" + envIndex + ", envValue= " + envValue);
				}
			}	
		}
		// System.out.println("LogLinear: envIndex = " + envIndex + ", numSamples = " + numSamples);
		envelope[numSamples - 1] = 0.0; 
	}
	
	public double getSample(double time) {
		int sampleIndex = (int) Math.round(time / sampleRate);
		if((sampleIndex % 441) == 0) {
			// System.out.println("LogLinear.getSample: time=" + time + ", sample= " + envelope[sampleIndex]);
		}
		if((sampleIndex >= startSample) && (sampleIndex < endSample)) {
			return envelope[sampleIndex];
		}
		return 0.0;
	}
	
	public double getSample(int sample) {
		int sampleIndex = sample;
		if((sampleIndex % 441) == 0) {
			// System.out.println("LogLinear.getSample(int): sample=" + sample + ", sample= " + envelope[sampleIndex]);
		}
		if((sampleIndex >= startSample) && (sampleIndex < endSample)) {
			return envelope[sampleIndex];
		}
		return 0.0;
	}
	
	public int getStartIndex() {
		return startSample;
	}
	
	public int getEndIndex() {
		return endSample;
	}	
	
}

class SynthTools {
	
	static double sampleRate = 44100.0;
	static double twoPI = 2.0 * Math.PI;
	
	static double[] getAudio(double freq, LogLinear env) {
		int sampleIndex;
		int startIndex = 0;
		int endIndex = env.getEndIndex();
		double currentPhase = 0.0;
		double deltaPhase = twoPI * (freq / sampleRate);
		double[] returnVal = new double[endIndex];
		for(sampleIndex = startIndex; sampleIndex < endIndex; sampleIndex++) {
			returnVal[sampleIndex] = env.getSample(sampleIndex) * Math.sin(currentPhase);
			currentPhase += deltaPhase;
			// System.out.println(sampleIndex + " " + returnVal[sampleIndex]);
		}
		return returnVal;
	}
	
	static double[] getAudio(double[] inputArray, double freq, double gain, LogLinear env) {
		int sampleIndex;
		int startIndex = 0;
		int endIndex = env.getEndIndex();
		double currentPhase = 0.0;
		double deltaPhase = twoPI * (freq / sampleRate);
		for(sampleIndex = startIndex; sampleIndex < endIndex; sampleIndex++) {
			inputArray[sampleIndex] += gain * env.getSample(sampleIndex) * Math.sin(currentPhase);
			currentPhase += deltaPhase;
			// System.out.println(sampleIndex + " " + returnVal[sampleIndex]);
		}
		return inputArray;
	}
	
}
