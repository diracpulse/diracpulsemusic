import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;


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
	
	public static ArrayList<FDData> dataInterpolate(ArrayList<FDData> input) {
		ArrayList<FDData> output = new ArrayList<FDData>();
		int lowerTime = input.get(0).getTime();
		double lowerValue = input.get(0).getLogAmplitude();
		FDData currentData = input.get(0);
		for(int index = 1; index < input.size(); index++) {
			int upperTime = input.get(index).getTime();
			double upperValue = input.get(index).getLogAmplitude();
			double slope = (upperValue - lowerValue) / (upperTime - lowerTime);
			for(int timeIndex = lowerTime; timeIndex < upperTime; timeIndex++) {
				double value = lowerValue + (timeIndex - lowerTime) * slope;
				try {
					output.add(new FDData(timeIndex, currentData.getNote(), value, currentData.getHarmonicID()));
				} catch (Exception e) {
					System.out.println("LogLinear.dataInterpolate(): error creating data");
					return null;
				}
				currentData = input.get(index);
			}
		}
		return output;
	}

}
