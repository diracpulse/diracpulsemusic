import java.util.ArrayList;
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
			synthInstrumentMatrix(startTime, endTime, baseNote, chords, harmonicIDToInstrumentHarmonic, 0.0, FDData.Channel.LEFT);
			synthInstrumentMatrix(startTime, endTime, baseNote, chords, harmonicIDToInstrumentHarmonic, 0.0, FDData.Channel.RIGHT);
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
			synthInstrument(startTime, endTime, lowestNote, null, harmonicIDToBassSynthHarmonic, 0.0);
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
	
	private static void synthInstrumentMatrix(int startTime, int endTime, int baseNote, int[] chords, TreeMap<Long, Harmonic> harmonics, double gain, FDData.Channel channel) {
		int numTimes = endTime - startTime;
		int numNotes = FDData.getMaxNote() - FDData.getMinNote();
		double[][] EQMatrix = new double[numTimes][numNotes];
		for(int time = 0; time < numTimes; time++) {
			for(int note = 0; note < numNotes; note++) {
				EQMatrix[time][note] = 0.0;			
			}
		}
		double maxAmplitude = 0.0;
		int maxNote = FDData.getMinNote();
		for(Harmonic harmonic: harmonics.values()) {
			for(FDData data: harmonic.getAllData()) {
				if(data.getLogAmplitude() > maxAmplitude) {
					maxAmplitude = data.getLogAmplitude();
					maxNote = data.getNote();
				}
				if(data.getTime() >= numTimes) continue;
				EQMatrix[data.getTime()][data.getNote() - FDData.getMinNote()] = data.getLogAmplitude();
			}
		}
		int deltaNote = baseNote - maxNote;
		ArrayList<Integer> baseNotes = new ArrayList<Integer>();
		int noteVal = baseNote;
		baseNotes.add(noteVal);
		if(chords != null) {
			for(int chord: chords) {
				noteVal += chord;
				baseNotes.add(noteVal);
			}
		}
		for(int currentNote: baseNotes) {
			int innerDeltaNote = (int) Math.round(currentNote - baseNote);
			for(int note = currentNote - FDData.noteBase; note < FDData.getMaxNote(); note++) {
				long harmonicID = HarmonicsEditor.getRandomID();
				Harmonic newHarmonic = new Harmonic(harmonicID);
				double logAmpVal = 0.0; 
				for(int time = 0; time < numTimes; time++) {
					try {
						int noteIndex = (int) Math.round(note) - FDData.getMinNote() - innerDeltaNote - deltaNote;
						if(noteIndex < 0 || noteIndex >= numNotes) break;
						double upperlogAmpVal = EQMatrix[time][noteIndex + 1];
						logAmpVal = EQMatrix[time][noteIndex];
						double lowerlogAmpVal = EQMatrix[time][noteIndex - 1];
						if(logAmpVal < upperlogAmpVal || logAmpVal < lowerlogAmpVal) continue;
						if(logAmpVal <= 0.0) continue;
						FDData data = new FDData(channel, time + startTime, note, logAmpVal, harmonicID);
						newHarmonic.addData(data);
					} catch (Exception e) {
						System.out.println("synthInstrumentEQ: Error creating data: " + time + startTime + " " + note + " " + logAmpVal);
					}
				}
				beatStartTimeToHarmonics.get(startTime).add(newHarmonic);
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