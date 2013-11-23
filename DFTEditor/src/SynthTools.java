import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

class SynthTools {
	
	static double sampleRate = 44100.0;
	static double twoPI = 2.0 * Math.PI;
	static double[] PCMDataLeft = null;
	static double[] PCMDataRight = null;
	static double[] WAVDataLeft = null;
	static double[] WAVDataRight = null;	
	static int deltaHarmonic = 1;
	static DFTEditor parent;
	public static boolean refresh = true;
	public static boolean flatHarmonics = false;
	static double maxDeltaFreq = 1;
	static double binRangeFactor = 1.0;
	private static CubicSpline barkRatioToMaskingLevel = null;
	private static double[] noteToBarkRatio = null;
	
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
		PCMDataLeft = FastSynth.synthHarmonicsLinear(FDData.Channel.LEFT, synthHarmonics);
		PCMDataRight = FastSynth.synthHarmonicsLinear(FDData.Channel.RIGHT, synthHarmonics);
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
		PCMDataLeft = FastSynth.synthHarmonicsLinearCubicSpline(FDData.Channel.LEFT, synthHarmonics);
		PCMDataRight = FastSynth.synthHarmonicsLinearCubicSpline(FDData.Channel.RIGHT, synthHarmonics);
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
		PCMDataLeft = FastSynth.synthHarmonicsLinearNoise(FDData.Channel.LEFT, synthHarmonics);
		PCMDataRight = FastSynth.synthHarmonicsLinearNoise(FDData.Channel.RIGHT, synthHarmonics);
	}
	
	static void playPCMData() {
		AudioPlayer ap = null;
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
	}
	
	static void createHarmonics() {
		DFTEditor.harmonicIDToHarmonic = new TreeMap<Long, Harmonic>();
		//createHarmonicsChannel((byte) 0);
		createHarmonicsChannel(FDData.Channel.LEFT);
		createHarmonicsChannel(FDData.Channel.RIGHT);
		DFTEditor.parent.graphEditorFrame.addHarmonicsToGraphEditor(DFTEditor.harmonicIDToHarmonic);
		DFTEditor.parent.fdEditorFrame.addHarmonicsToFDEditor(DFTEditor.harmonicIDToHarmonic);
	}

	static void createHarmonicsChannel(FDData.Channel channel) {
		TreeMap<Integer, TreeMap<Integer, FDData>> timeToFreqToData = new TreeMap<Integer, TreeMap<Integer, FDData>>();
		if(channel == FDData.Channel.LEFT) DFTEditor.timeToFreqsAtMaximaLeft = new TreeMap<Integer, TreeSet<Integer>>();
		if(channel == FDData.Channel.RIGHT) DFTEditor.timeToFreqsAtMaximaRight = new TreeMap<Integer, TreeSet<Integer>>();		
		float[][] amplitudes = null;
		if(channel == FDData.Channel.LEFT) amplitudes = DFTEditor.amplitudesLeft;
		if(channel == FDData.Channel.RIGHT) amplitudes = DFTEditor.amplitudesRight;
		if(amplitudes == null) return;
		int numTimes = amplitudes.length;
		int numFreqs = amplitudes[0].length;
		int time = 0;
		int freq = 0;
		for(time = 0; time < numTimes; time++) {
			timeToFreqToData.put(time, new TreeMap<Integer, FDData>());
			if(channel == FDData.Channel.LEFT) DFTEditor.timeToFreqsAtMaximaLeft.put(time, new TreeSet<Integer>());
			if(channel == FDData.Channel.RIGHT) DFTEditor.timeToFreqsAtMaximaRight.put(time, new TreeSet<Integer>());
			for(freq = 1; freq < numFreqs - 1; freq++) {
				if(amplitudes[time][freq] >= amplitudes[time][freq - 1]) {
					if(amplitudes[time][freq] >= amplitudes[time][freq + 1]) {
						if(amplitudes[time][freq] <= 0) continue;
						FDData data = null;
						try {
							data = new FDData(channel, time, DFTEditor.freqToNote(freq), amplitudes[time][freq], 1L);
						} catch (Exception e) {
							System.out.println("SynthTools.createHarmonics: Error " + e.getMessage());
						}
						timeToFreqToData.get(time).put(freq, data);
						if(channel == FDData.Channel.LEFT) DFTEditor.timeToFreqsAtMaximaLeft.get(time).add(freq);
						if(channel == FDData.Channel.RIGHT) DFTEditor.timeToFreqsAtMaximaRight.get(time).add(freq);
						//System.out.println(data);
					}
				}
			}
		}
		Filter.removeNoiseMaximas(channel, timeToFreqToData);
		timeToFreqToData = applyFreqMasking(channel, timeToFreqToData);
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
		// Remove Harmonic Fragments
		ArrayList<Long> harmonicIDsToRemove = new ArrayList<Long>();
		for(Long harmonicID: DFTEditor.harmonicIDToHarmonic.keySet()) {
			Harmonic harmonic = DFTEditor.harmonicIDToHarmonic.get(harmonicID);
			if(harmonic.getChannel() != channel) continue;
			int length = harmonic.getLengthNoTaper();
			double lengthInSeconds = length * (FDData.timeStepInMillis / 1000.0);
			double cycleLengthInSeconds = 1.0 / DFT2.noteToFrequency(harmonic.getMaxNote());
			if(lengthInSeconds / cycleLengthInSeconds < 4.0) harmonicIDsToRemove.add(harmonicID);
		}
		for(Long harmonicID: harmonicIDsToRemove) {
			DFTEditor.harmonicIDToHarmonic.remove(harmonicID);
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
	
	static class XYPair {
		
		public double x;
		public double y;
		
		XYPair(double x, double y) {
			this.x = x;
			this.y = y;
		}
		
	}
	
	static TreeMap<Integer, TreeMap<Integer, FDData>> applyFreqMasking(FDData.Channel channel, TreeMap<Integer, TreeMap<Integer, FDData>> timeToFreqToData) {
		TreeMap<Integer, TreeSet<Integer>> timeToFreqsAtMaxima = null;
		if(channel == FDData.Channel.LEFT) timeToFreqsAtMaxima = DFTEditor.timeToFreqsAtMaximaLeft;
		if(channel == FDData.Channel.RIGHT) timeToFreqsAtMaxima = DFTEditor.timeToFreqsAtMaximaRight;
		float[][] amplitudes = null;
		if(channel == FDData.Channel.LEFT) amplitudes = DFTEditor.amplitudesLeft;
		if(channel == FDData.Channel.RIGHT) amplitudes = DFTEditor.amplitudesRight;
		int numTimes = amplitudes.length;
		for(int time = 0; time < numTimes; time++) {
			TreeSet<Integer> outputFreqsAtMaxima = new TreeSet<Integer>();
			TreeMap<Integer, FDData> outputFreqToData = new TreeMap<Integer, FDData>();
			TreeSet<Integer> freqsAtMaxima = timeToFreqsAtMaxima.get(time);
			TreeMap<Integer, FDData> freqToData = timeToFreqToData.get(time);
			for(Integer freq: freqToData.keySet()) outputFreqToData.put(freq, freqToData.get(freq));
			for(Integer freq: freqsAtMaxima) outputFreqsAtMaxima.add(freq);
			for(Integer freq: freqsAtMaxima) {
				if(!outputFreqsAtMaxima.contains(freq)) continue;
				for(Integer innerFreq: freqsAtMaxima) {
					if(freq == innerFreq) continue;
					if(!outputFreqsAtMaxima.contains(innerFreq)) continue;
					if(isMaskedByData1(freqToData.get(freq), freqToData.get(innerFreq))) {
						outputFreqsAtMaxima.remove(innerFreq);
						outputFreqToData.remove(innerFreq);
						//System.out.print("m");
					}
				}
			}
			timeToFreqsAtMaxima.remove(time);
			timeToFreqsAtMaxima.put(time, outputFreqsAtMaxima);
			timeToFreqToData.remove(time);
			timeToFreqToData.put(time, outputFreqToData);
		}
		return timeToFreqToData;
	}
	
	static boolean isMaskedByData1(FDData data1, FDData data2) {
		if(data2.getLogAmplitude() > data1.getLogAmplitude()) return false;
		if(noteToBarkRatio == null) {
			noteToBarkRatio = new double[DFTEditor.maxScreenNote + 1];
			for(int note = 0; note < noteToBarkRatio.length; note++) {
				noteToBarkRatio[note] = 13.0 * Math.atan(0.00076 * DFT2.noteToFrequency(note)) + 3.5 * Math.atan((DFT2.noteToFrequency(note) / 7500.0) * (DFT2.noteToFrequency(note) / 7500.0));
			}
		}
		double bark1 = noteToBarkRatio[data1.getNote()];
		double bark2 = noteToBarkRatio[data2.getNote()];
		double barkRatio = bark2 / bark1;
		if(barkRatio < 87.0/104.0 || barkRatio > 109.0/78.0) return false;
		if(barkRatioToMaskingLevel == null) {
			ArrayList<XYPair> masking = new ArrayList<XYPair>();
			masking.add(new XYPair(87.0/104.0, Math.pow(10.0, -5.0 / 2)));
			masking.add(new XYPair(45.0/52.0, Math.pow(10.0, -4.0 / 2)));
			masking.add(new XYPair(93.0/104.0, Math.pow(10.0, -3.0 / 2)));
			masking.add(new XYPair(11.0/12.0, Math.pow(10.0, -2.0 / 2)));
			masking.add(new XYPair(49.0/52.0, Math.pow(10.0, -1.0 / 2)));
			masking.add(new XYPair(23.0/24.0, Math.pow(10.0, -0.5 / 2)));
			masking.add(new XYPair(101.0/104.0, Math.pow(10.0, -0.25 / 2)));
			masking.add(new XYPair(1.0/1.0, Math.pow(10.0, -0.0)));
			masking.add(new XYPair(107.0/104.0, Math.pow(10.0, -0.25 / 2)));
			masking.add(new XYPair(109.0/104.0, Math.pow(10.0, -0.5 / 2)));
			masking.add(new XYPair(14.0/13.0, Math.pow(10.0, -1.0 / 2)));
			masking.add(new XYPair(119.0/104.0, Math.pow(10.0, -2.0 / 2)));
			masking.add(new XYPair(95.0/78.0, Math.pow(10.0, -3.0 / 2)));
			masking.add(new XYPair(17.0/13.0, Math.pow(10.0, -4.0 / 2)));
			masking.add(new XYPair(109.0/78.0, Math.pow(10.0, -5.0 / 2)));
			double[] x = new double[masking.size()];
			double[] y = new double[masking.size()];
			int index = 0;
			for(XYPair xyPair: masking) {
				x[index] = xyPair.x;
				y[index] = Math.log(xyPair.y) / Math.log(2.0);
				System.out.println("Masking:" + x[index] + " : " + y[index]);
				index++;
			}
			barkRatioToMaskingLevel = new CubicSpline(x, y);
		}
		double maskingLevel = barkRatioToMaskingLevel.interpolate(barkRatio);
		if(data2.getLogAmplitude() - data1.getLogAmplitude() > maskingLevel) return false;
		return true;
	}
	

}
