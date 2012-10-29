import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

class SynthTools {
	
	static double sampleRate = 44100.0;
	static double twoPI = 2.0 * Math.PI;
	static float[] PCMDataMono = null;
	static float[] PCMDataLeft = null;
	static float[] PCMDataRight = null;
	static float[] WAVDataMono = null;
	static float[] WAVDataLeft = null;
	static float[] WAVDataRight = null;	
	static int deltaHarmonic = 1;
	static DFTEditor parent;
	public static boolean refresh = true;
	public static boolean flatHarmonics = false;
	static double maxDeltaFreq = 1;
	static double binRangeFactor = 1.0;
	
	static int freqToBinRange(int freq) {
		int note = DFTEditor.freqToNote(freq);
		double freqInHz = Math.pow(FDData.logBase, (double) note / (double) FDData.noteBase);
		if(DFT.bassFreq > DFT.midFreq) {
			return (int) Math.ceil(binRangeFactor * DFT.bassFreq / freqInHz);
		}
		return (int) Math.ceil(binRangeFactor * DFT.midFreq / (2.0 * freqInHz));
	}

	static void createPCMDataLinear() {
		if(DFTEditor.currentChannelMixer == DFTEditor.ChannelMixer.WAV) return;
		if(DFTEditor.harmonicIDToHarmonic == null || DFTEditor.harmonicIDToHarmonic.isEmpty()) return;
		ArrayList<Harmonic> synthHarmonics = new ArrayList<Harmonic>();
		for(Harmonic harmonic: DFTEditor.harmonicIDToHarmonic.values()) {
			if(!harmonic.isSynthesized()) continue;
			if(harmonic.getStartTime() > DFTEditor.getMaxViewTime()) continue;
			if(harmonic.getEndTime() < DFTEditor.minViewTime) continue;
			synthHarmonics.add(new Harmonic(harmonic.getHarmonicID()));
			for(FDData data: harmonic.getTrimmedHarmonic(DFTEditor.minViewTime, DFTEditor.getMaxViewTime(), 1.0)) {
				synthHarmonics.get(synthHarmonics.size() - 1).addData(data);
			}
		}
		PCMDataMono = (float[]) FastSynth.synthHarmonicsLinear((byte) 0, new ArrayList<Harmonic>(synthHarmonics));
		PCMDataLeft = (float[]) FastSynth.synthHarmonicsLinear((byte) 1, new ArrayList<Harmonic>(synthHarmonics));
		PCMDataRight = (float[]) FastSynth.synthHarmonicsLinear((byte) 2, new ArrayList<Harmonic>(synthHarmonics));
	}
	
	static void createPCMDataLinearCubicSpline() {
		if(DFTEditor.currentChannelMixer == DFTEditor.ChannelMixer.WAV) return;
		if(DFTEditor.harmonicIDToHarmonic == null || DFTEditor.harmonicIDToHarmonic.isEmpty()) return;
		ArrayList<Harmonic> synthHarmonics = new ArrayList<Harmonic>();
		for(Harmonic harmonic: DFTEditor.harmonicIDToHarmonic.values()) {
			if(!harmonic.isSynthesized()) continue;
			if(harmonic.getStartTime() > DFTEditor.getMaxViewTime()) continue;
			if(harmonic.getEndTime() < DFTEditor.minViewTime) continue;
			synthHarmonics.add(new Harmonic(harmonic.getHarmonicID()));
			for(FDData data: harmonic.getTrimmedHarmonic(DFTEditor.minViewTime, DFTEditor.getMaxViewTime(), 1.0)) {
				synthHarmonics.get(synthHarmonics.size() - 1).addData(data);
			}
		}
		PCMDataMono = (float[]) FastSynth.synthHarmonicsLinearCubicSpline((byte) 0, new ArrayList<Harmonic>(synthHarmonics));
		PCMDataLeft = (float[]) FastSynth.synthHarmonicsLinearCubicSpline((byte) 1, new ArrayList<Harmonic>(synthHarmonics));
		PCMDataRight = (float[]) FastSynth.synthHarmonicsLinearCubicSpline((byte) 2, new ArrayList<Harmonic>(synthHarmonics));
	}
	
	static void createPCMDataLinearNoise() {
		if(DFTEditor.currentChannelMixer == DFTEditor.ChannelMixer.WAV) return;
		if(DFTEditor.harmonicIDToHarmonic == null || DFTEditor.harmonicIDToHarmonic.isEmpty()) return;
		ArrayList<Harmonic> synthHarmonics = new ArrayList<Harmonic>();
		for(Harmonic harmonic: DFTEditor.harmonicIDToHarmonic.values()) {
			if(!harmonic.isSynthesized()) continue;
			if(harmonic.getStartTime() > DFTEditor.getMaxViewTime()) continue;
			if(harmonic.getEndTime() < DFTEditor.minViewTime) continue;
			synthHarmonics.add(new Harmonic(harmonic.getHarmonicID()));
			for(FDData data: harmonic.getTrimmedHarmonic(DFTEditor.minViewTime, DFTEditor.getMaxViewTime(), 1.0)) {
				synthHarmonics.get(synthHarmonics.size() - 1).addData(data);
			}
		}
		PCMDataMono = (float[]) FastSynth.synthHarmonicsLinearNoise((byte) 0, new ArrayList<Harmonic>(synthHarmonics));
		PCMDataLeft = (float[]) FastSynth.synthHarmonicsLinearNoise((byte) 1, new ArrayList<Harmonic>(synthHarmonics));
		PCMDataRight = (float[]) FastSynth.synthHarmonicsLinearNoise((byte) 2, new ArrayList<Harmonic>(synthHarmonics));
	}
	
	static void playPCMData() {
		AudioPlayer ap = new AudioPlayer(PCMDataMono, 1.0);
		if(DFTEditor.currentChannel == DFTEditor.Channel.STEREO) {
			if(DFTEditor.currentChannelMixer == DFTEditor.ChannelMixer.LEFT_RIGHT) {
				ap = new AudioPlayer(PCMDataLeft, PCMDataRight, 1.0);
			}
			if(DFTEditor.currentChannelMixer == DFTEditor.ChannelMixer.WAV_RIGHT) {
				ap = new AudioPlayer(WAVDataLeft, PCMDataRight, 1.0);
			}
			if(DFTEditor.currentChannelMixer == DFTEditor.ChannelMixer.LEFT_WAV) {
				ap = new AudioPlayer(PCMDataLeft, WAVDataRight, 1.0);
			}
			if(DFTEditor.currentChannelMixer == DFTEditor.ChannelMixer.WAV) {
				ap = new AudioPlayer(WAVDataLeft, WAVDataRight, 1.0);
			}
		}
		if(DFTEditor.currentChannel == DFTEditor.Channel.LEFT) {
			ap = new AudioPlayer(PCMDataLeft, 1.0);
		}
		if(DFTEditor.currentChannel == DFTEditor.Channel.RIGHT) {
			ap = new AudioPlayer(PCMDataRight, 1.0);
		}
		ap.start();
		//ap = new AudioPlayer(parent, PCMDataRight, 1.0);
		//ap.start();
	}
	
	static void createHarmonics() {
		DFTEditor.harmonicIDToHarmonic = new TreeMap<Long, Harmonic>();
		createHarmonicsChannel((byte) 0);
		createHarmonicsChannel((byte) 1);
		createHarmonicsChannel((byte) 2);
		DFTEditor.parent.graphEditorFrame.addHarmonicsToGraphEditor(DFTEditor.harmonicIDToHarmonic);
		DFTEditor.parent.fdEditorFrame.addHarmonicsToFDEditor(DFTEditor.harmonicIDToHarmonic);
	}

	static void createHarmonicsChannel(byte channel) {
		TreeMap<Integer, TreeMap<Integer, FDData>> timeToFreqToData = new TreeMap<Integer, TreeMap<Integer, FDData>>();
		if(channel == 0) DFTEditor.timeToFreqsAtMaximaMono = new TreeMap<Integer, TreeSet<Integer>>();
		if(channel == 1) DFTEditor.timeToFreqsAtMaximaLeft = new TreeMap<Integer, TreeSet<Integer>>();		
		if(channel == 2) DFTEditor.timeToFreqsAtMaximaRight = new TreeMap<Integer, TreeSet<Integer>>();
		float[][] amplitudes = null;
		if(channel == 0) amplitudes = DFTEditor.amplitudesMono;
		if(channel == 1) amplitudes = DFTEditor.amplitudesLeft;
		if(channel == 2) amplitudes = DFTEditor.amplitudesRight;
		if(amplitudes == null) return;
		int numTimes = amplitudes.length;
		int numFreqs = amplitudes[0].length;
		int time = 0;
		int freq = 0;
		for(time = 0; time < numTimes; time++) {
			timeToFreqToData.put(time, new TreeMap<Integer, FDData>());
			if(channel == 0) DFTEditor.timeToFreqsAtMaximaMono.put(time, new TreeSet<Integer>());
			if(channel == 1) DFTEditor.timeToFreqsAtMaximaLeft.put(time, new TreeSet<Integer>());
			if(channel == 2) DFTEditor.timeToFreqsAtMaximaRight.put(time, new TreeSet<Integer>());
			for(freq = 1; freq < numFreqs - 1; freq++) {
				if(amplitudes[time][freq] >= amplitudes[time][freq - 1]) {
					if(amplitudes[time][freq] >= amplitudes[time][freq + 1]) {
						if(amplitudes[time][freq] <= 0) continue;
						FDData data = null;
						try {
							data = new FDData(channel, time, DFTEditor.freqToNote(freq), amplitudes[time][freq], 1L);
						} catch (Exception e) {
							System.out.println("SynthTools.createHarmonics: Error creating data time: " + time + " freq: " + freq);
						}
						timeToFreqToData.get(time).put(freq, data);
						if(channel == 0) DFTEditor.timeToFreqsAtMaximaMono.get(time).add(freq);
						if(channel == 1) DFTEditor.timeToFreqsAtMaximaLeft.get(time).add(freq);
						if(channel == 2) DFTEditor.timeToFreqsAtMaximaRight.get(time).add(freq);
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
				int startFreq = freq;
				boolean continueHarmonic = true;
				int maxBinRange = freqToBinRange(startFreq);
				for(int innerTime = time + 1; innerTime < numTimes; innerTime++) {
					if(!continueHarmonic) break;
					TreeMap<Integer, FDData> innerFreqToData = timeToFreqToData.get(innerTime);
					if(innerFreqToData.containsKey(freq)) {
						FDData innerData = innerFreqToData.get(freq);
						innerFreqToData.remove(freq);
						innerData.setHarmonicID(harmonicID);
						newHarmonic.addData(innerData);
						//System.out.println("0:" + harmonicID + " " + innerData);
						continue;
					}
					if(flatHarmonics) break;
					continueHarmonic = false;
					for(int binRange = 1; binRange <= maxBinRange; binRange++) {
						if(innerFreqToData.containsKey(freq + binRange)) {
							freq += binRange;
							FDData innerData = innerFreqToData.get(freq);
							innerFreqToData.remove(freq);
							innerData.setHarmonicID(harmonicID);
							newHarmonic.addData(innerData);
							continueHarmonic = true;
							break;
						}
						if(innerFreqToData.containsKey(freq - binRange)) {
							freq -= binRange;
							FDData innerData = innerFreqToData.get(freq);
							innerFreqToData.remove(freq);
							innerData.setHarmonicID(harmonicID);
							newHarmonic.addData(innerData);
							continueHarmonic = true;
							break;
						}
					}
				}
				DFTEditor.harmonicIDToHarmonic.put(harmonicID, newHarmonic);
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
