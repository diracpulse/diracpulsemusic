import java.util.ArrayList;
import java.util.TreeMap;

class SynthTools {
	
	static double sampleRate = 44100.0;
	static double twoPI = 2.0 * Math.PI;
	static double timeToSample = sampleRate * (FDData.timeStepInMillis * 1.0 / 1000.0);
	static double[] PCMData = null;
	static int deltaHarmonic = 1;
	static DFTEditor parent;
	public static boolean refresh = true;

	static void createPCMData() {
		createHarmonics(DFTEditor.timeToFreqToSelectedData);
		PCMData = FileOutput.SynthFDDataExternally(new ArrayList<Harmonic>(DFTEditor.harmonicIDToHarmonic.values()));
	}
	
	static void playPCMData() {
		if(PCMData == null) return;
		AudioPlayer ap = new AudioPlayer(parent, PCMData, 1.0);
		ap.start();
	}

	// create Harmonics from saveInput, sets ID of FDData
	static void createHarmonics(TreeMap<Integer, TreeMap<Integer, FDData>> saveInput) {
		// create a copy of input because algorithm removes keys after added to harmonics
		TreeMap<Integer, TreeMap<Integer, FDData>> input = copyTreeMap(saveInput);
		DFTEditor.harmonicIDToHarmonic = new TreeMap<Long, Harmonic>(); 
		while(!input.isEmpty()) {
			int inputTime = input.firstKey();
			// loop through all frequencies at current time 
			while(!input.get(inputTime).isEmpty()) {
				long id = DFTEditor.getRandomID();
				Harmonic currentHarmonic = new Harmonic(id);
				// ARBITRARY: start at lowest note
				int currentNote = input.get(inputTime).firstKey();
				int harmonicTime = inputTime;
				while(input.containsKey(harmonicTime)) {
					//System.out.println("SynthTools.createHarmonics harmonicTime" + harmonicTime);
					//sleep(10);
					// check for data at [currentNote,time+1]
					if(input.get(harmonicTime).containsKey(currentNote)) {
						FDData data = input.get(harmonicTime).get(currentNote);
						data.setHarmonicID(id);
						currentHarmonic.addData(data);
						input.get(harmonicTime).remove(currentNote);
						DFTEditor.addSelected(data);
						harmonicTime++;
						continue;
					}					
					// check for data at [currentNote+1,time+1]
					currentNote++;
					if(input.get(harmonicTime).containsKey(currentNote)) {
						FDData data = input.get(harmonicTime).get(currentNote);
						data.setHarmonicID(id);
						currentHarmonic.addData(data);
						input.get(harmonicTime).remove(currentNote);
						DFTEditor.addSelected(data);
						harmonicTime++;
						continue;
					}
					// check for data at [currentNote-1,time-1]
					currentNote -= 2;
					if(input.get(harmonicTime).containsKey(currentNote)) {
						FDData data = input.get(harmonicTime).get(currentNote);
						data.setHarmonicID(id);
						currentHarmonic.addData(data);
						input.get(harmonicTime).remove(currentNote);
						DFTEditor.addSelected(data);
						harmonicTime++;
						continue;
					}
					if(deltaHarmonic == 1) break;
					currentNote += 3;
					if(input.get(harmonicTime).containsKey(currentNote)) {
						FDData data = input.get(harmonicTime).get(currentNote);
						data.setHarmonicID(id);
						currentHarmonic.addData(data);
						input.get(harmonicTime).remove(currentNote);
						DFTEditor.addSelected(data);
						harmonicTime++;
						continue;
					}
					currentNote -= 4;
					if(input.get(harmonicTime).containsKey(currentNote)) {
						FDData data = input.get(harmonicTime).get(currentNote);
						data.setHarmonicID(id);
						currentHarmonic.addData(data);
						input.get(harmonicTime).remove(currentNote);
						DFTEditor.addSelected(data);
						harmonicTime++;
						continue;
					}				
					break;
				}
				DFTEditor.harmonicIDToHarmonic.put(id, currentHarmonic);
			} // while(!input.get(inputTime).isEmpty())
			input.remove(inputTime);
			inputTime++;
			//System.out.println("SynthTools.createHarmonics input size " + input.size());
			//sleep(10);
		} // while(!input.isEmpty())
		//printHarmonics(harmonics);
		return;
	}
	
	static void printHarmonics(ArrayList<Harmonic> harmonics) {
		for(Harmonic harmonic: harmonics) {
			System.out.print(harmonic);
		}
	}
	
	static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (Exception e){
			System.out.println(e);
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
	
	// Sythesis seems to work OK without interpolating note values
	public static ArrayList<FDData> interpolateAmplitude(ArrayList<FDData> input) {
		ArrayList<FDData> output = new ArrayList<FDData>();
		if(input.isEmpty()) return output;
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
					//System.out.println(timeIndex + " " +  currentData.getNote() + " " + (float) value + " " + currentData.getHarmonicID());
					output.add(new FDData(timeIndex, currentData.getNote(), (float) value, currentData.getHarmonicID()));
				} catch (Exception e) {
					System.out.println("LogLinear.dataInterpolate(): error creating data");
					return null;
				}
				lowerValue = upperValue;
				lowerTime = upperTime;
			}
		}
		return output;
	}

}
