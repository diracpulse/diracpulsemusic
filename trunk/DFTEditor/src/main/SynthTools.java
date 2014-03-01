package main;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

public class SynthTools {
	
	public static double sampleRate = 44100.0;
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
	
	static int freqToBinRange(int freq) {
		return 1;
		/*
		int note = DFTEditor.freqToNote(freq);
		double freqInHz = Math.pow(FDData.logBase, (double) note / (double) FDData.noteBase);
		if(DFT.bassFreq > DFT.midFreq) {
			return (int) Math.ceil(binRangeFactor * DFT.bassFreq / freqInHz);
		}
		return (int) Math.ceil(binRangeFactor * DFT.midFreq / (2.0 * freqInHz));
		*/
	}

	static void createPCMDataLinear() {
		if(DFTEditor.currentChannelMixer == DFTEditor.ChannelMixer.WAV) return;
		if(DFTEditor.harmonicIDToHarmonic == null) return;
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
		if(DFTEditor.harmonicIDToHarmonic == null) return;
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

	static void playPCMData() {
		if(DFTEditor.currentChannel == DFTEditor.Channel.STEREO) {
			if(DFTEditor.currentChannelMixer == DFTEditor.ChannelMixer.LEFT_RIGHT) {
				AudioPlayer.playAudio(PCMDataLeft, PCMDataRight);
			}
			if(DFTEditor.currentChannelMixer == DFTEditor.ChannelMixer.WAV_RIGHT) {
				AudioPlayer.playAudio(WAVDataLeft, PCMDataRight);
			}
			if(DFTEditor.currentChannelMixer == DFTEditor.ChannelMixer.LEFT_WAV) {
				AudioPlayer.playAudio(PCMDataLeft, WAVDataRight);
			}
			if(DFTEditor.currentChannelMixer == DFTEditor.ChannelMixer.WAV) {
				AudioPlayer.playAudio(WAVDataLeft, WAVDataRight);
			}
		}
		if(DFTEditor.currentChannel == DFTEditor.Channel.LEFT) {
			AudioPlayer.playAudio(PCMDataLeft);
		}
		if(DFTEditor.currentChannel == DFTEditor.Channel.RIGHT) {
			AudioPlayer.playAudio(PCMDataRight);
		}
	}
	
	static void createHarmonics() {
		DFTEditor.harmonicIDToHarmonic = new TreeMap<Long, Harmonic>();
		//createHarmonicsChannel((byte) 0);
		createHarmonicsChannel(FDData.Channel.LEFT);
		createHarmonicsChannel(FDData.Channel.RIGHT);
		//DFTEditor.parent.graphEditorFrame.addHarmonicsToGraphEditor(DFTEditor.harmonicIDToHarmonic);
		//DFTEditor.parent.fdEditorFrame.addHarmonicsToFDEditor(DFTEditor.harmonicIDToHarmonic);
	}
	
	public static class IntPair {
		
		public int time;
		public int freq;
		
		IntPair(int time, int freq) {
			this.time = time;
			this.freq = freq;
		}
	}

	static void createHarmonicsChannel(FDData.Channel channel) {
		TreeMap<Integer, TreeMap<Integer, FDData>> timeToFreqToData = new TreeMap<Integer, TreeMap<Integer, FDData>>();
		TreeMap<Double, IntPair> amplitudeToTimeAndFreq = new TreeMap<Double, IntPair>();
		if(channel == FDData.Channel.LEFT) DFTEditor.timeToFreqsAtMaximaLeft = new TreeMap<Integer, TreeSet<Integer>>();
		if(channel == FDData.Channel.RIGHT) DFTEditor.timeToFreqsAtMaximaRight = new TreeMap<Integer, TreeSet<Integer>>();		
		double[][] amplitudes = null;
		if(channel == FDData.Channel.LEFT) amplitudes = DFTEditor.amplitudesLeft;
		if(channel == FDData.Channel.RIGHT) amplitudes = DFTEditor.amplitudesRight;
		if(amplitudes == null) return;
		int numTimes = amplitudes.length;
		int numFreqs = amplitudes[0].length;
		int time = 0;
		int freq = 0;
		for(time = 0; time < numTimes; time++) timeToFreqToData.put(time, new TreeMap<Integer, FDData>());
		for(time = 0; time < numTimes; time++) {
			if(channel == FDData.Channel.LEFT) DFTEditor.timeToFreqsAtMaximaLeft.put(time, new TreeSet<Integer>());
			if(channel == FDData.Channel.RIGHT) DFTEditor.timeToFreqsAtMaximaRight.put(time, new TreeSet<Integer>());
			if(channel == FDData.Channel.LEFT) DFTEditor.timeToNoiseFreqsAtMaximaLeft.put(time, new TreeSet<Integer>());
			if(channel == FDData.Channel.RIGHT) DFTEditor.timeToNoiseFreqsAtMaximaRight.put(time, new TreeSet<Integer>());
			for(freq = 1; freq < numFreqs - 1; freq++) {
				double centerAmplitude = amplitudes[time][freq];
				double upperAmplitude = amplitudes[time][freq - 1];
				double lowerAmplitude = amplitudes[time][freq + 1];
				if(centerAmplitude >= upperAmplitude) {
					if(centerAmplitude >= lowerAmplitude) {
						if(centerAmplitude - lowerAmplitude > 1.0) {
							if(centerAmplitude - upperAmplitude > 1.0) {
								if(channel == FDData.Channel.LEFT) DFTEditor.timeToNoiseFreqsAtMaximaLeft.get(time).add(freq);
								if(channel == FDData.Channel.RIGHT) DFTEditor.timeToNoiseFreqsAtMaximaRight.get(time).add(freq);						
							}
						}
						if(centerAmplitude == 0.0f) continue;
						FDData data = null;
						try {
							data = new FDData(channel, time, DFTEditor.freqToNote(freq), amplitudes[time][freq], 1L);
						} catch (Exception e) {
							System.out.println("SynthTools.createHarmonics: Error " + e.getMessage());
						}
						amplitudeToTimeAndFreq.put(amplitudes[time][freq], new IntPair(time, freq));
						timeToFreqToData.get(time).put(freq, data);
						if(channel == FDData.Channel.LEFT) DFTEditor.timeToFreqsAtMaximaLeft.get(time).add(freq);
						if(channel == FDData.Channel.RIGHT) DFTEditor.timeToFreqsAtMaximaRight.get(time).add(freq);
						//System.out.println(data);
					}
				}
			}
		}
		// START: ApplyFreqMasking
		/*
		applyFreqMasking(channel, timeToFreqToData);
		ArrayList<Double> amplitudesToRemove = new ArrayList<Double>();
		for(double amplitude: amplitudeToTimeAndFreq.keySet()) {
			time = amplitudeToTimeAndFreq.get(amplitude).time;
			freq = amplitudeToTimeAndFreq.get(amplitude).freq;
			if(!timeToFreqToData.get(time).containsKey(freq)) {
				amplitudesToRemove.add(amplitude);
			}
		}
		for(double amplitude: amplitudesToRemove) {
			amplitudeToTimeAndFreq.remove(amplitude);
		}
		*/
		// END: ApplyFreqMasking
		/*
		// START: Apply A Wighting
		double zeroDb = amplitudeToTimeAndFreq.lastKey();
		ArrayList<Double> amplitudesToRemove = new ArrayList<Double>();
		for(double amplitude: amplitudeToTimeAndFreq.keySet()) {
			time = amplitudeToTimeAndFreq.get(amplitude).time;
			freq = amplitudeToTimeAndFreq.get(amplitude).freq;
			FDData data = timeToFreqToData.get(time).get(freq);
			if(!isAudible(data, zeroDb)) {
				amplitudesToRemove.add(amplitude);
			}
		}
		for(double amplitude: amplitudesToRemove) {
			time = amplitudeToTimeAndFreq.get(amplitude).time;
			freq = amplitudeToTimeAndFreq.get(amplitude).freq;
			timeToFreqToData.get(time).remove(freq);
			amplitudeToTimeAndFreq.remove(amplitude);
		}
		// END: Apply A Weighting
		*/
		createHarmonics(channel, timeToFreqToData, amplitudeToTimeAndFreq, numTimes, numFreqs);
	}
	
	static void createHarmonics(FDData.Channel channel, TreeMap<Integer, TreeMap<Integer, FDData>> timeToFreqToData,
			 TreeMap<Double, IntPair> amplitudeToTimeAndFreq, int numTimes, int numFreqs) {
		ArrayList<Long> harmonicIDsToRemove = new ArrayList<Long>();
		for(Long harmonicID: DFTEditor.harmonicIDToHarmonic.keySet()) {
			if(DFTEditor.harmonicIDToHarmonic.get(harmonicID).getChannel() == channel) {
				harmonicIDsToRemove.add(harmonicID);
			}
		}
		for(Long harmonicID: harmonicIDsToRemove) {
			DFTEditor.harmonicIDToHarmonic.remove(harmonicID);
		}
		while(!amplitudeToTimeAndFreq.isEmpty()) {
			//System.out.println(amplitudeToTimeAndFreq.size());
			IntPair timeAndFreq = amplitudeToTimeAndFreq.get(amplitudeToTimeAndFreq.lastKey());
			int outerTime = timeAndFreq.time;
			int outerFreq = timeAndFreq.freq;
			Harmonic newHarmonic = new Harmonic(DFTEditor.getRandomID());
			/*
			if(!timeToFreqToData.get(time).containsKey(freq)) {
				amplitudeToTimeAndFreq.remove(amplitudeToTimeAndFreq.lastKey());
				System.out.println(time + " " + freq);
				continue;
			}
			*/
			int maxTimeJump = 1;
			int maxFreqJump = 1;
			int continues = 0;
			int time = outerTime - 1;
			int freq = outerFreq;
			while(continues <= maxTimeJump) {
				time += 1;
				if(time == numTimes) break;
				for(int freqDifference = 1; freqDifference <= maxFreqJump; freqDifference++) {
					FDData data0 = null;
					FDData dataPlus1 = null;
					FDData dataMinus1 = null;
					if(timeToFreqToData.get(time).containsKey(freq)) data0 = timeToFreqToData.get(time).get(freq);
					if(timeToFreqToData.get(time).containsKey(freq + freqDifference)) dataPlus1 = timeToFreqToData.get(time).get(freq + freqDifference);
					if(timeToFreqToData.get(time).containsKey(freq - freqDifference)) dataMinus1 = timeToFreqToData.get(time).get(freq - freqDifference);
					if(data0 != null) {
						newHarmonic.addData(data0);
						timeToFreqToData.get(time).remove(freq);
						amplitudeToTimeAndFreq.remove(data0.getLogAmplitude());
						continues = -1;
						break;
					}
					if((dataPlus1 != null) && (dataMinus1 != null)) {
						if(dataPlus1.getLogAmplitude() > dataMinus1.getLogAmplitude()) {
							freq += freqDifference;
							newHarmonic.addData(dataPlus1);
							timeToFreqToData.get(time).remove(freq);
							amplitudeToTimeAndFreq.remove(dataPlus1.getLogAmplitude());
							continues = -1;
							break;
						} else {
							freq -= freqDifference;
							newHarmonic.addData(dataMinus1);
							timeToFreqToData.get(time).remove(freq);
							amplitudeToTimeAndFreq.remove(dataMinus1.getLogAmplitude());
							continues = -1;
							break;
						}
					}
					if(dataPlus1 != null) {
						freq += freqDifference;
						newHarmonic.addData(dataPlus1);
						timeToFreqToData.get(time).remove(freq);
						amplitudeToTimeAndFreq.remove(dataPlus1.getLogAmplitude());
						continues = -1;
						break;
					}
					if(dataMinus1 != null) {
						freq -= freqDifference;
						newHarmonic.addData(dataMinus1);
						timeToFreqToData.get(time).remove(freq);
						amplitudeToTimeAndFreq.remove(dataMinus1.getLogAmplitude());
						continues = 0;
						break;
					}
				}
				continues++;
			}
			continues = 0;
			time = outerTime;
			freq = outerFreq;
			while(continues <= maxTimeJump) {
				time--;
				if(time < 0) break;
				if(time == numTimes) break;
				for(int freqDifference = 1; freqDifference <= maxFreqJump; freqDifference++) {
					FDData data0 = null;
					FDData dataPlus1 = null;
					FDData dataMinus1 = null;
					if(timeToFreqToData.get(time).containsKey(freq)) data0 = timeToFreqToData.get(time).get(freq);
					if(timeToFreqToData.get(time).containsKey(freq + freqDifference)) dataPlus1 = timeToFreqToData.get(time).get(freq + freqDifference);
					if(timeToFreqToData.get(time).containsKey(freq - freqDifference)) dataMinus1 = timeToFreqToData.get(time).get(freq - freqDifference);
					if(data0 != null) {
						newHarmonic.addData(data0);
						timeToFreqToData.get(time).remove(freq);
						amplitudeToTimeAndFreq.remove(data0.getLogAmplitude());
						continues = -1;
						break;
					}
					if((dataPlus1 != null) && (dataMinus1 != null)) {
						if(dataPlus1.getLogAmplitude() > dataMinus1.getLogAmplitude()) {
							freq += freqDifference;
							newHarmonic.addData(dataPlus1);
							timeToFreqToData.get(time).remove(freq);
							amplitudeToTimeAndFreq.remove(dataPlus1.getLogAmplitude());
							continues = -1;
							break;
						} else {
							freq -= freqDifference;
							newHarmonic.addData(dataMinus1);
							timeToFreqToData.get(time).remove(freq);
							amplitudeToTimeAndFreq.remove(dataMinus1.getLogAmplitude());
							continues = -1;
							break;
						}
					}
					if(dataPlus1 != null) {
						freq += freqDifference;
						newHarmonic.addData(dataPlus1);
						timeToFreqToData.get(time).remove(freq);
						amplitudeToTimeAndFreq.remove(dataPlus1.getLogAmplitude());
						continues = -1;
						break;
					}
					if(dataMinus1 != null) {
						freq -= freqDifference;
						newHarmonic.addData(dataMinus1);
						timeToFreqToData.get(time).remove(freq);
						amplitudeToTimeAndFreq.remove(dataMinus1.getLogAmplitude());
						continues = 0;
						break;
					}
				}
				continues++;
			}
			DFTEditor.harmonicIDToHarmonic.put(newHarmonic.getHarmonicID(), newHarmonic);
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
