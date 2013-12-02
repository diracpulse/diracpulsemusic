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
	private static CubicSpline barkRatioToMaskingLevel = null;
	private static double[] noteToBarkRatio = null;
	private static CubicSpline freqInHzToAWeighting = null; // Actually ISO-226
	private static double[] noteToAWeighting = null; // Actually ISO-226
	
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
	
	static void createPCMDataLinearNoise() {
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
		// START: Remove Harmonic Fragments
		ArrayList<Long> harmonicIDsToRemove = new ArrayList<Long>();
		for(Long harmonicID: DFTEditor.harmonicIDToHarmonic.keySet()) {
			Harmonic harmonic = DFTEditor.harmonicIDToHarmonic.get(harmonicID);
			if(harmonic.getChannel() != channel) continue;
			int length = harmonic.getLengthNoTaper();
			if(length < 4) {
				harmonicIDsToRemove.add(harmonicID);
				continue;
			}
			double lengthInSeconds = length * (FDData.timeStepInMillis / 1000.0);
			double cycleLengthInSeconds = 1.0 / DFT2.noteToFrequency(harmonic.getMaxNote());
			if(lengthInSeconds / cycleLengthInSeconds < 4.0) harmonicIDsToRemove.add(harmonicID);
		}
		for(Long harmonicID: harmonicIDsToRemove) {
			for(FDData data: DFTEditor.harmonicIDToHarmonic.get(harmonicID).getAllDataRaw()) {
				if(channel == FDData.Channel.LEFT) DFTEditor.timeToFreqsAtMaximaLeft.get(data.getTime()).remove(DFTEditor.noteToFreq(data.getNote()));
				if(channel == FDData.Channel.RIGHT) DFTEditor.timeToFreqsAtMaximaRight.get(data.getTime()).remove(DFTEditor.noteToFreq(data.getNote()));
			}
			DFTEditor.harmonicIDToHarmonic.remove(harmonicID);
		}
		// END: Remove Harmonic Fragments
		// START: Refilter Data Based on Harmonic Length
		for(time = 0; time < numTimes; time++) timeToFreqToData.put(time, new TreeMap<Integer, FDData>());
		for(Long harmonicID: DFTEditor.harmonicIDToHarmonic.keySet()) {
			if(DFTEditor.harmonicIDToHarmonic.get(harmonicID).getChannel() == channel) {
				for(FDData data: DFTEditor.harmonicIDToHarmonic.get(harmonicID).getAllDataRaw()) {
					timeToFreqToData.get(data.getTime()).put(DFTEditor.noteToFreq(data.getNote()), data);
					amplitudeToTimeAndFreq.put(data.getLogAmplitude(), new IntPair(data.getTime(), DFTEditor.noteToFreq(data.getNote())));
				}
			}
		}
		Filter.applyCriticalBandFiltering(channel, timeToFreqToData, amplitudeToTimeAndFreq);
		createHarmonics(channel, timeToFreqToData, amplitudeToTimeAndFreq, numTimes, numFreqs);
		// END: Refilter Data Based on Harmonic Length
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
				boolean continueHarmonic = false;
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
				boolean continueHarmonic = false;
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
		double[][] amplitudes = null;
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
	
	static boolean isAudible(FDData data, double zeroDb) {
		if(noteToAWeighting == null) {
			ArrayList<XYPair> weighting = new ArrayList<XYPair>();
			weighting.add(new XYPair(FDData.minFrequencyInHz,-31.6));
			weighting.add(new XYPair(20,-31.6));
			weighting.add(new XYPair(25,-27.2));
			weighting.add(new XYPair(31.5,-23.0));
			weighting.add(new XYPair(40,-19.1));
			weighting.add(new XYPair(50,-15.9));
			weighting.add(new XYPair(63,-13.0));
			weighting.add(new XYPair(80,-10.3));
			weighting.add(new XYPair(100,-8.1));
			weighting.add(new XYPair(125,-6.2));
			weighting.add(new XYPair(160,-4.5));
			weighting.add(new XYPair(200,-3.1));
			weighting.add(new XYPair(250,-2.0));
			weighting.add(new XYPair(315,-1.1));
			weighting.add(new XYPair(400,-0.4));
			weighting.add(new XYPair(500,0.0));
			weighting.add(new XYPair(630,0.3));
			weighting.add(new XYPair(800,0.5));
			weighting.add(new XYPair(1000,0.0));
			weighting.add(new XYPair(1250,-2.7));
			weighting.add(new XYPair(1600,-4.1));
			weighting.add(new XYPair(2000,-1.0));
			weighting.add(new XYPair(2500,1.7));
			weighting.add(new XYPair(3150,2.5));
			weighting.add(new XYPair(4000,1.2));
			weighting.add(new XYPair(5000,-2.1));
			weighting.add(new XYPair(6300,-7.1));
			weighting.add(new XYPair(8000,-11.2));
			weighting.add(new XYPair(10000,-10.7));
			weighting.add(new XYPair(12500,-3.1));
			weighting.add(new XYPair(FDData.maxFrequencyInHz, -31.6));
			double[] x = new double[weighting.size()];
			double[] y = new double[weighting.size()];
			int index = 0;
			for(XYPair xyPair: weighting) {
				x[index] = Math.log(xyPair.x) / Math.log(2.0);
				y[index] = xyPair.y / 10.0 * Math.log(10.0) / Math.log(2.0) / 2.0;
				System.out.println("Weighting:" + x[index] + " : " + y[index]);
				index++;
			}
			freqInHzToAWeighting = new CubicSpline(x, y);
			noteToAWeighting = new double[DFTEditor.maxScreenNote + 1];
			for(int note = 0; note < noteToAWeighting.length; note++) {
				noteToAWeighting[note] = freqInHzToAWeighting.interpolate(Math.log(DFT2.noteToFrequency(note)) / Math.log(2.0));
			}
		}
		double logMinAudibleAmplitude = noteToAWeighting[data.getNote()];
		if(data.getLogAmplitude() + logMinAudibleAmplitude < zeroDb) return true;
		return false;
	}
	
	static boolean isMaskedByData1(FDData data1, FDData data2) {
		if(data1 == null || data2 == null) return false;
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
