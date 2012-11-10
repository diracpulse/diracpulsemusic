import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JOptionPane;


public class SoftSynth {
	
	public static TreeMap<Long, Harmonic> harmonicIDToInstrumentHarmonic = null;
	public static TreeMap<Long, Harmonic> harmonicIDToKickDrumHarmonic = null;
	public static TreeMap<Long, Harmonic> harmonicIDToHighFreqHarmonic = null;
	public static TreeMap<Long, Harmonic> harmonicIDToBassSynthHarmonic = null;
	public static TreeMap<Long, Harmonic> harmonicIDToSnareHarmonic = null;
	public static TreeMap<Integer, ArrayList<Harmonic>> beatStartTimeToHarmonics = null;
	public static final double logAmplitudeLimit = 12.0;

	public static void synthLoopInHarmonicsEditor(ArrayList<Beat> beatArray) {
		HarmonicsEditor.clearCurrentData();
		synthAllBeats(beatArray);
		for(int beatStartTime: beatStartTimeToHarmonics.keySet()) {
			for(Harmonic harmonic: beatStartTimeToHarmonics.get(beatStartTime)) {
				//harmonic.addCompressionWithLimiter(4.0, 14.0);
				for(FDData data: harmonic.getAllData()) {
					HarmonicsEditor.addData(data);
				}
			}
		}
	}
	
	private static void synthAllBeats(ArrayList<Beat> beatArray) {
		beatStartTimeToHarmonics = new TreeMap<Integer, ArrayList<Harmonic>>();
		int startTime = 0;
		int beatIndex = 0;
		for(Beat beat: beatArray) {
			if(beatIndex % 2 == 0) synthBeat(startTime, beat.baseNote, beat.chords, beat.duration, false);
			if(beatIndex % 2 == 1) synthBeat(startTime, beat.baseNote, beat.chords, beat.duration, true);
			startTime += beat.duration;
			beatIndex++;
		}
	}
	
	private static void synthBeat(int startTime, int baseNote, int[] chords, int duration, boolean useHighFreq) {
		beatStartTimeToHarmonics.put(startTime, new ArrayList<Harmonic>());
		int maxNote = (int) Math.round(Math.floor(HarmonicsEditor.frequencyInHzToNote(FDData.maxFrequencyInHz)) - 1.0);
		int minNote = HarmonicsEditor.frequencyInHzToNote(32.0);
		int endTime = startTime + duration - 1;
		int lowestNote = baseNote;
		while(lowestNote >= minNote) lowestNote -= FDData.noteBase;
		lowestNote += FDData.noteBase;
		// Synth Main Instrument
		if(harmonicIDToInstrumentHarmonic != null) {
			synthInstrumentHarmonics(startTime, endTime, baseNote, chords, harmonicIDToInstrumentHarmonic, 0.0);
			//synthInstrument(startTime, endTime, baseNote, chords, harmonicIDToInstrumentHarmonicMono, -6.0, 0);
			//synthInstrument(startTime, endTime, baseNote, chords, harmonicIDToInstrumentHarmonicLeft, -6.0, 1);
			//synthInstrument(startTime, endTime, baseNote, chords, harmonicIDToInstrumentHarmonicRight, -6.0, 2);
		} else {
			int note = baseNote;
			for(FDData.Channel channel: FDData.Channel.values()) {
				synthTrebleNoteWithOvertones(startTime, endTime, note, maxNote, 14.0, channel);
				for(int chord: chords) {
					note += chord;
					synthTrebleNoteWithOvertones(startTime, endTime, note, maxNote, 14.0, channel);
				}
			}
		}
		// Synth Bass Instrument
		if(harmonicIDToBassSynthHarmonic != null) {
			synthInstrument(startTime, endTime, baseNote, null, harmonicIDToBassSynthHarmonic, 0.0);
		} else {
			//synthBassNoteWithOvertones(startTime, endTime, lowestNote, baseNote, 14.0);
		}
		// Synth Noise Sources
		if(harmonicIDToKickDrumHarmonic != null) {
			synthInstrument(startTime, endTime, -1, harmonicIDToKickDrumHarmonic, 0.0);
		}
		if(useHighFreq) {
			if(harmonicIDToHighFreqHarmonic != null) {
				synthInstrument(startTime, endTime, -1, harmonicIDToHighFreqHarmonic, 0.0);
			}
		} else {
			if(harmonicIDToSnareHarmonic != null) {
				synthInstrument(startTime, endTime, -1, harmonicIDToSnareHarmonic, 0.0);
			}
		}
		//fitHarmonicsToChords(startTime, lowestNote, chords, false);
		//removeDissonance(startTime, endTime);
		HarmonicsEditor.refreshView();
	}
	
	private static void synthInstrument(int startTime, int endTime, int note, int[] chords, TreeMap<Long, Harmonic> harmonics, double gain) {
		if(chords == null) {
			synthInstrument(startTime, endTime, note, harmonics, gain); 
			return;
		}
		synthInstrument(startTime, endTime, note, harmonics, gain);
		int currentNote = note;
		for(int chord: chords) {
			currentNote += chord;
			synthInstrument(startTime, endTime, currentNote, harmonics, gain);
		}
	}
	
	private static void synthInstrument(int startTime, int endTime, int note, TreeMap<Long, Harmonic> harmonics, double gain) {
		TreeSet<Integer> notes = new TreeSet<Integer>();
		for(Harmonic harmonic: harmonics.values()) notes.add(harmonic.getAverageNote()); 
		int firstNote = notes.first();
		int deltaNote = 0;
		if(note > 0) {
			deltaNote = note - firstNote;
		}
		while(deltaNote > FDData.noteBase) deltaNote -= FDData.noteBase;
		while(deltaNote < -1 * FDData.noteBase) deltaNote += FDData.noteBase;
		ArrayList<Harmonic> values = new ArrayList<Harmonic>(harmonics.values());
		for(Harmonic harmonic: values) {
			long harmonicID = HarmonicsEditor.getRandomID();
			ArrayList<FDData> harmonicData = harmonic.getScaledHarmonic(startTime, endTime, deltaNote, harmonicID);
			Harmonic newHarmonic = new Harmonic(harmonicID);
			for(FDData data: harmonicData) {
				double logAmplitude = data.getLogAmplitude() + gain;
				if(logAmplitude < 0.0) logAmplitude = 0.0;
				data.setLogAmplitude(logAmplitude);
				newHarmonic.addData(data);
			}
			beatStartTimeToHarmonics.get(startTime).add(newHarmonic);
		}
	}
	
	private static void synthInstrumentHarmonics(int startTime, int endTime, int baseNote, int[] chords, TreeMap<Long, Harmonic> harmonics, double gain) {
		HashMap<FDData.Channel, double[][]> channelToMatrix = new HashMap<FDData.Channel, double[][]>();
		int numTimes = endTime - startTime;
		int numNotes = FDData.getMaxNote() - FDData.getMinNote();
		// Init matrix data
		for(FDData.Channel channel: FDData.Channel.values()) {
			channelToMatrix.put(channel, new double[numTimes][numNotes]);
			double[][] currentMatrix = channelToMatrix.get(channel);
			for(int time = 0; time < numTimes; time++) {
				for(int note = 0; note < numNotes; note++) {
					currentMatrix[time][note] = 0.0;
				}
			}
		}
		// Find minimum note
		int minNoteLeft = FDData.getMaxNote();
		HashMap<FDData.Channel, Integer> channelToMinNote = new HashMap<FDData.Channel, Integer>();
		for(FDData.Channel channel: FDData.Channel.values()) {
			channelToMinNote.put(channel, FDData.getMaxNote());
		}
		for(Harmonic harmonic: harmonics.values()) {
			for(FDData data: harmonic.getAllData()) {
				if(data.getNote() < channelToMinNote.get(data.getChannel())) {
					channelToMinNote.put(data.getChannel(), data.getNote());
				}
				if(data.getTime() >= numTimes) continue;
			}
		}
		// Fill in array with harmonics, adjusted to correspond to base note of chord
		for(FDData.Channel channel: FDData.Channel.values()) {
			int deltaNote = baseNote - channelToMinNote.get(channel);
			for(Harmonic harmonic: harmonics.values()) {
				double[][] EQMatrix = channelToMatrix.get(harmonic.getChannel());
				for(FDData data: harmonic.getAllData()) {
					if(data.getTime() >= numTimes) continue;
					int innerNote = data.getNote() + deltaNote - FDData.getMinNote();
					if(innerNote <0 || innerNote >= numNotes) continue;
					EQMatrix[data.getTime()][data.getNote() + deltaNote - FDData.getMinNote()] = data.getLogAmplitude();
				}
			}
		}
		// Fill in chords
		for(FDData.Channel channel: FDData.Channel.values()) {
			double[][] EQMatrix = channelToMatrix.get(channel);
			for(int time = 0; time < numTimes; time++) {
				int note = baseNote;
				for(int chord: chords) {
					int octave = 0;
					note += chord;
					while(true) {
						if(note + octave >= FDData.getMaxNote()) break;
						double amp1 = Math.pow(FDData.logBase, EQMatrix[time][note + octave - FDData.getMinNote()]);
						double amp2 = Math.pow(FDData.logBase, EQMatrix[time][baseNote + octave - FDData.getMinNote()]);
						double logAmpVal =  Math.log(amp1 + amp2) / Math.log(2.0);
						EQMatrix[time][note + octave - FDData.getMinNote()] = logAmpVal;
						//System.out.println(time + " " + (note + octave) + " " + (baseNote + octave) + " " + logAmpVal);
						octave += FDData.noteBase;
					}
				}
			}
		}
		for(FDData.Channel channel: FDData.Channel.values()) {
			double[][] EQMatrix = channelToMatrix.get(channel);
			removeDissonance(EQMatrix);
		}
		// Synthesize
		for(FDData.Channel channel: FDData.Channel.values()) {
			double[][] EQMatrix = channelToMatrix.get(channel);
			for(int note = 0; note < numNotes; note++) {
				long harmonicID = HarmonicsEditor.getRandomID();
				Harmonic harmonic = new Harmonic(HarmonicsEditor.getRandomID());
				double maxLogAmplitude = 0.0;
				for(int time = 0; time < numTimes; time++) {
					double logAmplitude = EQMatrix[time][note];
					if(logAmplitude > maxLogAmplitude) maxLogAmplitude = logAmplitude;
					try {
						harmonic.addData(new FDData(channel, time + startTime, note + FDData.getMinNote(), logAmplitude, harmonicID));
					} catch (Exception e) {
						System.out.println("HarmonicsEditor: SoftSynth.SynthInstrumentHarmonics error creating data");
					}
				}
				if(maxLogAmplitude > 0) {
					beatStartTimeToHarmonics.get(startTime).add(harmonic);
				}
			}
		}
	}

	public static void removeDissonance(double[][] matrix) {
		double maskingFactor = -1.0;
		int bins = FDData.noteBase / 3;
		int numTimes = matrix.length;
		int numFreqs = matrix[0].length;
		for(int time = 0; time < numTimes; time++) {
			for(int freq = 0; freq < numFreqs; freq++) {
				double amplitude = matrix[time][freq];
				for(int innerFreq = freq - bins; innerFreq <= freq + bins; innerFreq++) {
					double maskingVal = amplitude + Math.abs((freq - innerFreq) / (double) bins) * maskingFactor;
					if(maskingVal < 0) continue;
					if(innerFreq < 0 || innerFreq >= numFreqs || innerFreq == freq) continue;
					if(matrix[time][innerFreq] < maskingVal) matrix[time][innerFreq] = 0.0f;
				}
			}
		}
	}
	
	private static double sawTooth(double phase) {
		phase /= 2.0 * Math.PI;
		phase -= Math.floor(phase);
		return (phase - 0.5) / 2.0;
	}
	
	private static void synthNote(int startTime, int endTime, int note, double amplitude, double taper, FDData.Channel channel) {
		try {
			double AMPhase = Math.PI;
			Harmonic currentHarmonic = new Harmonic(HarmonicsEditor.getRandomID());
			for(int time = startTime; time <= endTime; time++) {
				double amplitudeAdjust = Math.sin(AMPhase * taper); // * Math.random();
				double currentAmplitude = amplitude + amplitudeAdjust;
				if(currentAmplitude < 2.0) break;
				FDData data = new FDData(channel, time, note, currentAmplitude, currentHarmonic.getHarmonicID());
				currentHarmonic.addData(data);
			}
			beatStartTimeToHarmonics.get(startTime).add(currentHarmonic);
		} catch (Exception e) {
			System.out.println("HarmonicsEditor.synthNote() error creating data:" + e.getMessage());
		}
	}
	
	private static void synthTrebleNoteWithOvertones(int startTime, int endTime, int minNote, int maxNote, double amplitude, FDData.Channel channel) {
		try {
			for(int note = minNote; note < maxNote; note += FDData.noteBase) {
				synthHarmonic(startTime, endTime, note, amplitude, channel);
				//synthFormant(startTime, endTime, note, amplitude);
				amplitude -= 1.25;
				if(amplitude < 2.0) break;
			}
		} catch (Exception e) {
			System.out.println("HarmonicsEditor.synthNoteWithOvertones() error creating data:" + e.getMessage());
		}
	}
	
	private static void synthHarmonic(int startTime, int endTime, int note, double amplitude, FDData.Channel channel) {
		try {
			Harmonic harmonic = new Harmonic(HarmonicsEditor.getRandomID());
			harmonic.addData(new FDData(channel, startTime, note, 0.0, harmonic.getHarmonicID()));
			harmonic.addData(new FDData(channel, startTime + 4, note, amplitude, harmonic.getHarmonicID()));
			harmonic.addData(new FDData(channel, endTime - 10, note, amplitude, harmonic.getHarmonicID()));
			harmonic.addData(new FDData(channel, endTime, note, 0.0, harmonic.getHarmonicID()));
			beatStartTimeToHarmonics.get(startTime).add(harmonic);
		} catch (Exception e) {
			System.out.println("HarmonicsEditor.synthNoteWithOvertones() error creating data:" + e.getMessage());
		}
	}
	
	private static void synthFormant(int startTime, int endTime, int note, double amplitude, FDData.Channel channel) {
		try {
			int length = endTime - startTime;
			for(int formant = note - 3; formant <= note + 3; formant++) {
				int formantLength = (int) Math.round(4.0 * Math.random() + 4.0);
				int formantPeakOnset = (int) Math.round(4.0 * Math.random());
				int formantEndTime = formantLength + startTime;
				int formantPeak = formantPeakOnset + startTime;
				if(formantPeak >= formantEndTime) continue;
 				Harmonic harmonic = new Harmonic(HarmonicsEditor.getRandomID());
				harmonic.addData(new FDData(channel, startTime, formant, 0.0, harmonic.getHarmonicID()));
				harmonic.addData(new FDData(channel, formantPeak, formant, amplitude - Math.random(), harmonic.getHarmonicID()));
				harmonic.addData(new FDData(channel, formantEndTime, formant, 0.0, harmonic.getHarmonicID()));
				beatStartTimeToHarmonics.get(startTime).add(harmonic);
			}
		} catch (Exception e) {
			System.out.println("HarmonicsEditor.synthNoteWithOvertones() error creating data:" + e.getMessage());
		}
	}  
	
	private static void synthBassNoteWithOvertones(int startTime, int endTime, int minNote, int maxNote, double amplitude, FDData.Channel channel) {
		try {
			for(int note = minNote; note < maxNote; note += FDData.noteBase) {
				Harmonic harmonic = new Harmonic(HarmonicsEditor.getRandomID());
				harmonic.addData(new FDData(channel, startTime, note, amplitude, harmonic.getHarmonicID()));
				harmonic.addData(new FDData(channel, endTime - 10, note, amplitude, harmonic.getHarmonicID()));
				harmonic.addData(new FDData(channel, endTime, note, 0.0, harmonic.getHarmonicID()));
				beatStartTimeToHarmonics.get(startTime).add(harmonic);
			}
		} catch (Exception e) {
			System.out.println("HarmonicsEditor.synthNoteWithOvertones() error creating data:" + e.getMessage());
		}
	}

}