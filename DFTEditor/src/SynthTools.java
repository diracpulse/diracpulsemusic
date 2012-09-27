import java.util.ArrayList;
import java.util.TreeMap;

class SynthTools {
	
	static double sampleRate = 44100.0;
	static double twoPI = 2.0 * Math.PI;
	static double timeToSample = sampleRate * (FDData.timeStepInMillis * 1.0 / 1000.0);
	static double[] PCMDataMono = null;
	static double[] PCMDataLeft = null;
	static double[] PCMDataRight = null;
	static int deltaHarmonic = 1;
	static DFTEditor parent;
	public static boolean refresh = true;

	static void createPCMData() {
		//TreeMap<Integer, TreeMap<Integer, FDData>> timeToFreqToSelectedData = DFTEditor.getSelectedData();
		//createHarmonics(timeToFreqToSelectedData);
		PCMDataMono = FileOutput.SynthFDDataExternally(new ArrayList<Harmonic>(DFTEditor.harmonicIDToHarmonicMono.values()));
		PCMDataLeft = FileOutput.SynthFDDataExternally(new ArrayList<Harmonic>(DFTEditor.harmonicIDToHarmonicLeft.values()));
		PCMDataRight = FileOutput.SynthFDDataExternally(new ArrayList<Harmonic>(DFTEditor.harmonicIDToHarmonicRight.values()));
	}
	
	static void createPCMDataLinear() {
		//TreeMap<Integer, TreeMap<Integer, FDData>> timeToFreqToSelectedData = DFTEditor.getSelectedData();
		//createHarmonics(timeToFreqToSelectedData);
		PCMDataMono = FastSynth.synthHarmonicsLinear(new ArrayList<Harmonic>(DFTEditor.harmonicIDToHarmonicMono.values()));
		PCMDataLeft = FastSynth.synthHarmonicsLinear(new ArrayList<Harmonic>(DFTEditor.harmonicIDToHarmonicLeft.values()));
		PCMDataRight = FastSynth.synthHarmonicsLinear(new ArrayList<Harmonic>(DFTEditor.harmonicIDToHarmonicRight.values()));
	}
	
	static void createPCMDataLinearCubicSpline() {
		//TreeMap<Integer, TreeMap<Integer, FDData>> timeToFreqToSelectedData = DFTEditor.getSelectedData();
		//createHarmonics(timeToFreqToSelectedData);
		PCMDataMono = FastSynth.synthHarmonicsLinearCubicSpline(new ArrayList<Harmonic>(DFTEditor.harmonicIDToHarmonicMono.values()));
		PCMDataLeft = FastSynth.synthHarmonicsLinearCubicSpline(new ArrayList<Harmonic>(DFTEditor.harmonicIDToHarmonicLeft.values()));
		PCMDataRight = FastSynth.synthHarmonicsLinearCubicSpline(new ArrayList<Harmonic>(DFTEditor.harmonicIDToHarmonicRight.values()));
	}
	
	static void playPCMData() {
		AudioPlayer ap = new AudioPlayer(parent, PCMDataMono, 1.0);
		if(DFTEditor.currentChannel == DFTEditor.Channel.STEREO) {
			ap = new AudioPlayer(parent, PCMDataLeft, PCMDataRight, 1.0);
		}
		if(DFTEditor.currentChannel == DFTEditor.Channel.LEFT) {
			ap = new AudioPlayer(parent, PCMDataLeft, 1.0);
		}
		if(DFTEditor.currentChannel == DFTEditor.Channel.RIGHT) {
			ap = new AudioPlayer(parent, PCMDataRight, 1.0);
		}
		ap.start();
		//ap = new AudioPlayer(parent, PCMDataRight, 1.0);
		//ap.start();
	}

	// create Harmonics from saveInput, sets ID of FDData
	static void createHarmonics(TreeMap<Integer, TreeMap<Integer, FDData>> saveInput) {
		// create a copy of input because algorithm removes keys after added to harmonics
		TreeMap<Integer, TreeMap<Integer, FDData>> input = copyTreeMap(saveInput);
		TreeMap<Long, Harmonic> harmonicIDToHarmonic = DFTEditor.getHarmonicIDToHarmonic(); 
		//harmonicIDToHarmonic = new TreeMap<Long, Harmonic>(); 
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
					currentNote++;
					// check for data at [currentNote+1,time+1]
					if(deltaHarmonic == 0) continue;
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
				harmonicIDToHarmonic.put(id, currentHarmonic);
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

}
