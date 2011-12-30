import java.util.TreeMap;

class SynthTools {
	
	static double sampleRate = 44100.0;
	static double twoPI = 2.0 * Math.PI;
	
	static void playFileData(TreeMap<Integer, TreeMap<Integer, FDData>> input) {
		
	}
	
	static TreeMap<Integer, TreeMap<Integer, FDData>> copyTreeMap(TreeMap<Integer, TreeMap<Integer, FDData>> input) {
		TreeMap<Integer, TreeMap<Integer, FDData>> output = new TreeMap<Integer, TreeMap<Integer, FDData>>();
		for(Integer time: input.keySet()) {
			output.put(time, new TreeMap<Integer, FDData>());
			for(Integer note: input.get(time).keySet()) {
				FDData data = input.get(time).get(note);
				output.get(time).put(note, data);
			}
		}
		return output;
	}
	
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
