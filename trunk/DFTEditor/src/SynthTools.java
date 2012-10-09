import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

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
	
	static void createHarmonics() {
		createHarmonicsMono();
		createHarmonicsLeft();
		createHarmonicsRight();
	}

	static void createHarmonicsMono() {
		TreeMap<Integer, TreeMap<Integer, FDData>> timeToFreqToData = new TreeMap<Integer, TreeMap<Integer, FDData>>();
		DFTEditor.harmonicIDToHarmonicMono = new TreeMap<Long, Harmonic>();
		DFTEditor.timeToFreqsAtMaximaMono = new TreeMap<Integer, TreeSet<Integer>>();
		float[][] amplitudes = DFTEditor.amplitudesMono;
		int numTimes = amplitudes.length;
		int numFreqs = amplitudes[0].length;
		int time = 0;
		int freq = 0;
		for(time = 0; time < numTimes; time++) {
			timeToFreqToData.put(time, new TreeMap<Integer, FDData>());
			DFTEditor.timeToFreqsAtMaximaMono.put(time, new TreeSet<Integer>());
			for(freq = 1; freq < numFreqs - 1; freq++) {
				if(amplitudes[time][freq] >= amplitudes[time][freq - 1]) {
					if(amplitudes[time][freq] >= amplitudes[time][freq + 1]) {
						if(amplitudes[time][freq] <= 0) continue;
						FDData data = null;
						try {
							data = new FDData(time, DFTEditor.freqToNote(freq), amplitudes[time][freq + 1], 1L);
						} catch (Exception e) {
							System.out.println("SynthTools.createHarmonics: Error creating data time: " + time + " freq: " + freq);
						}
						timeToFreqToData.get(time).put(freq, data);
						DFTEditor.timeToFreqsAtMaximaMono.get(time).add(freq);
						//System.out.println(data);
					}
				}
			}
		}
		for(time = 0; time < numTimes; time++) {
			TreeMap<Integer, FDData> outerFreqToData = timeToFreqToData.get(time);
			while(!outerFreqToData.isEmpty()) {
				long harmonicID = DFTEditor.getRandomID();
				Harmonic newHarmonic = new Harmonic(harmonicID);
				freq = outerFreqToData.firstKey();
				FDData currentData = outerFreqToData.get(freq);
				outerFreqToData.remove(freq);
				currentData.setHarmonicID(harmonicID);
				newHarmonic.addData(currentData);
				for(int innerTime = time + 1; innerTime < numTimes; innerTime++) {
					TreeMap<Integer, FDData> innerFreqToData = timeToFreqToData.get(innerTime);
					if(innerFreqToData.containsKey(freq)) {
						FDData innerData = innerFreqToData.get(freq);
						innerFreqToData.remove(freq);
						innerData.setHarmonicID(harmonicID);
						newHarmonic.addData(innerData);
						//System.out.println("0:" + harmonicID + " " + innerData);
						continue;
					}
					if(innerFreqToData.containsKey(freq - 1)) {
						freq -= 1;
						FDData innerData = innerFreqToData.get(freq);
						innerFreqToData.remove(freq);
						innerData.setHarmonicID(harmonicID);
						newHarmonic.addData(innerData);
						//System.out.println("0:" + harmonicID + " " + innerData);
						continue;
					}
					if(innerFreqToData.containsKey(freq + 1)) {
						freq += 1;
						FDData innerData = innerFreqToData.get(freq);
						innerFreqToData.remove(freq);
						innerData.setHarmonicID(harmonicID);
						newHarmonic.addData(innerData);
						//System.out.println("0:" + harmonicID + " " + innerData);
						continue;
					}
					break;
				}
				DFTEditor.harmonicIDToHarmonicMono.put(harmonicID, newHarmonic);
			}
		}
	}
	

	static void createHarmonicsLeft() {
		TreeMap<Integer, TreeMap<Integer, FDData>> timeToFreqToData = new TreeMap<Integer, TreeMap<Integer, FDData>>();
		DFTEditor.harmonicIDToHarmonicLeft = new TreeMap<Long, Harmonic>();
		DFTEditor.timeToFreqsAtMaximaLeft = new TreeMap<Integer, TreeSet<Integer>>();
		float[][] amplitudes = DFTEditor.amplitudesLeft;
		int numTimes = amplitudes.length;
		int numFreqs = amplitudes[0].length;
		int time = 0;
		int freq = 0;
		for(time = 0; time < numTimes; time++) {
			timeToFreqToData.put(time, new TreeMap<Integer, FDData>());
			DFTEditor.timeToFreqsAtMaximaLeft.put(time, new TreeSet<Integer>());
			for(freq = 1; freq < numFreqs - 1; freq++) {
				if(amplitudes[time][freq] >= amplitudes[time][freq - 1]) {
					if(amplitudes[time][freq] >= amplitudes[time][freq + 1]) {
						if(amplitudes[time][freq] == 0) continue;
						FDData data = null;
						try {
							data = new FDData(time, DFTEditor.freqToNote(freq), amplitudes[time][freq + 1], 1L);
						} catch (Exception e) {
							System.out.println("SynthTools.createHarmonics: Error creating data time: " + time + " freq: " + freq);
						}
						timeToFreqToData.get(time).put(freq, data);
						DFTEditor.timeToFreqsAtMaximaLeft.get(time).add(freq);
						//System.out.println(data);
					}
				}
			}
		}
		for(time = 0; time < numTimes; time++) {
			TreeMap<Integer, FDData> outerFreqToData = timeToFreqToData.get(time);
			while(!outerFreqToData.isEmpty()) {
				long harmonicID = DFTEditor.getRandomID();
				Harmonic newHarmonic = new Harmonic(harmonicID);
				freq = outerFreqToData.firstKey();
				FDData currentData = outerFreqToData.get(freq);
				outerFreqToData.remove(freq);
				currentData.setHarmonicID(harmonicID);
				newHarmonic.addData(currentData);
				for(int innerTime = time + 1; innerTime < numTimes; innerTime++) {
					TreeMap<Integer, FDData> innerFreqToData = timeToFreqToData.get(innerTime);
					if(innerFreqToData.containsKey(freq)) {
						FDData innerData = innerFreqToData.get(freq);
						innerFreqToData.remove(freq);
						innerData.setHarmonicID(harmonicID);
						newHarmonic.addData(innerData);
						//System.out.println("0:" + harmonicID + " " + innerData);
						continue;
					}
					if(innerFreqToData.containsKey(freq - 1)) {
						freq -= 1;
						FDData innerData = innerFreqToData.get(freq);
						innerFreqToData.remove(freq);
						innerData.setHarmonicID(harmonicID);
						newHarmonic.addData(innerData);
						//System.out.println("1:" + harmonicID + " " + innerData);
						continue;
					}
					if(innerFreqToData.containsKey(freq + 1)) {
						freq += 1;
						FDData innerData = innerFreqToData.get(freq);
						innerFreqToData.remove(freq);
						innerData.setHarmonicID(harmonicID);
						newHarmonic.addData(innerData);
						//System.out.println("2:" + harmonicID + " " + innerData);
						continue;
					}
					break;
				}
				DFTEditor.harmonicIDToHarmonicLeft.put(harmonicID, newHarmonic);
			}
		}
	}
	
	static void createHarmonicsRight() {
		TreeMap<Integer, TreeMap<Integer, FDData>> timeToFreqToData = new TreeMap<Integer, TreeMap<Integer, FDData>>();
		DFTEditor.harmonicIDToHarmonicRight = new TreeMap<Long, Harmonic>();
		DFTEditor.timeToFreqsAtMaximaRight = new TreeMap<Integer, TreeSet<Integer>>();
		float[][] amplitudes = DFTEditor.amplitudesRight;
		int numTimes = amplitudes.length;
		int numFreqs = amplitudes[0].length;
		int time = 0;
		int freq = 0;
		for(time = 0; time < numTimes; time++) {
			timeToFreqToData.put(time, new TreeMap<Integer, FDData>());
			DFTEditor.timeToFreqsAtMaximaRight.put(time, new TreeSet<Integer>());
			for(freq = 1; freq < numFreqs - 1; freq++) {
				if(amplitudes[time][freq] >= amplitudes[time][freq - 1]) {
					if(amplitudes[time][freq] >= amplitudes[time][freq + 1]) {
						if(amplitudes[time][freq] == 0) continue;
						FDData data = null;
						try {
							data = new FDData(time, DFTEditor.freqToNote(freq), amplitudes[time][freq + 1], 1L);
						} catch (Exception e) {
							System.out.println("SynthTools.createHarmonics: Error creating data time: " + time + " freq: " + freq);
						}
						timeToFreqToData.get(time).put(freq, data);
						DFTEditor.timeToFreqsAtMaximaRight.get(time).add(freq);
						//System.out.println(data);
					}
				}
			}
		}
		for(time = 0; time < numTimes; time++) {
			TreeMap<Integer, FDData> outerFreqToData = timeToFreqToData.get(time);
			while(!outerFreqToData.isEmpty()) {
				long harmonicID = DFTEditor.getRandomID();
				Harmonic newHarmonic = new Harmonic(harmonicID);
				freq = outerFreqToData.firstKey();
				FDData currentData = outerFreqToData.get(freq);
				outerFreqToData.remove(freq);
				currentData.setHarmonicID(harmonicID);
				newHarmonic.addData(currentData);
				for(int innerTime = time + 1; innerTime < numTimes; innerTime++) {
					TreeMap<Integer, FDData> innerFreqToData = timeToFreqToData.get(innerTime);
					if(innerFreqToData.containsKey(freq)) {
						FDData innerData = innerFreqToData.get(freq);
						innerFreqToData.remove(freq);
						innerData.setHarmonicID(harmonicID);
						newHarmonic.addData(innerData);
						//System.out.println("0:" + harmonicID + " " + innerData);
						continue;
					}
					if(innerFreqToData.containsKey(freq - 1)) {
						freq -= 1;
						FDData innerData = innerFreqToData.get(freq);
						innerFreqToData.remove(freq);
						innerData.setHarmonicID(harmonicID);
						newHarmonic.addData(innerData);
						//System.out.println("1:" + harmonicID + " " + innerData);
						continue;
					}
					if(innerFreqToData.containsKey(freq + 1)) {
						freq += 1;
						FDData innerData = innerFreqToData.get(freq);
						innerFreqToData.remove(freq);
						innerData.setHarmonicID(harmonicID);
						newHarmonic.addData(innerData);
						//System.out.println("2:" + harmonicID + " " + innerData);
						continue;
					}
					break;
				}
				DFTEditor.harmonicIDToHarmonicRight.put(harmonicID, newHarmonic);
			}
		}
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
