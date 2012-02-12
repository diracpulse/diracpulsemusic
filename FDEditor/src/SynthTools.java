import java.util.ArrayList;
import java.util.TreeMap;

class SynthTools {
	
	static double sampleRate = 44100.0;
	static double twoPI = 2.0 * Math.PI;
	
	static void playFileData() {
		synthFileData(FDEditor.timeToNoteToData);
	}
	
	static void synthFileData(TreeMap<Integer, TreeMap<Integer, FDData>> saveInput) {
		// create a copy of input because algorithm removes keys after added to harmonics
		TreeMap<Integer, TreeMap<Integer, FDData>> input = copyTreeMap(saveInput);
		ArrayList<Harmonic> harmonics = new ArrayList<Harmonic>();
		while(!input.isEmpty()) {
			int inputTime = input.firstKey();
			// loop through all frequencies at current time 
			while(!input.get(inputTime).isEmpty()) {
				Harmonic currentHarmonic = new Harmonic();
				// ARBITRARY: start at lowest note
				int currentNote = input.get(inputTime).firstKey();
				FDData currentData = input.get(inputTime).get(currentNote);
				currentHarmonic.addData(currentData);
				int harmonicTime = inputTime;
				System.out.println(harmonicTime);
				try {
					Thread.sleep(100);
				} catch (Exception e){
					System.out.println(e);
				}
				while(input.containsKey(++harmonicTime)) {
					System.out.println(harmonicTime);
					try {
						Thread.sleep(100);
					} catch (Exception e){
						System.out.println(e);
					}
					// check for data at [currentNote,time+1]
					if(input.get(harmonicTime).containsKey(currentNote)) {
						currentHarmonic.addData(input.get(harmonicTime).get(currentNote));
						input.get(harmonicTime).remove(currentNote);
						continue;
					}					
					// check for data at [currentNote+1,time+1]
					currentNote++;
					if(input.get(harmonicTime).containsKey(currentNote)) {
						currentHarmonic.addData(input.get(harmonicTime).get(currentNote));
						input.get(harmonicTime).remove(currentNote);
						continue;
					}
					// check for data at [currentNote-1,time-1]
					currentNote -= 2;
					if(input.get(harmonicTime).containsKey(currentNote)) {
						currentHarmonic.addData(input.get(harmonicTime).get(currentNote));
						input.get(harmonicTime).remove(currentNote);
						continue;
					}
				}
				harmonics.add(currentHarmonic);
			} // while(!input.get(inputTime).isEmpty())
			input.remove(inputTime);
			inputTime++;
		} // while(!input.isEmpty())
		printHarmonics(harmonics);
	}
	
	static void printHarmonics(ArrayList<Harmonic> harmonics) {
		for(Harmonic harmonic: harmonics) {
			System.out.print(harmonic);
		}
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
