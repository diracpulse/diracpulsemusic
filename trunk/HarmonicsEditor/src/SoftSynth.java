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
			synthInstrumentEQ(startTime, endTime, baseNote, chords, harmonicIDToInstrumentHarmonic, 0.0);
		} else {
			if(chords == null) {
				synthTrebleNoteWithOvertones(startTime, endTime, baseNote, maxNote, 14.0);
			} else {
				int chordBaseNote = baseNote;
				if(chords.length == 1) {
					synthTrebleNoteWithOvertones(startTime, endTime, chordBaseNote, maxNote, 14.0);
					chordBaseNote += chords[0];
					synthTrebleNoteWithOvertones(startTime, endTime, chordBaseNote, maxNote, 14.0);
				}
				if(chords.length == 2) {
					synthTrebleNoteWithOvertones(startTime, endTime, chordBaseNote, maxNote, 14.0);
					chordBaseNote += chords[0];
					synthTrebleNoteWithOvertones(startTime, endTime, chordBaseNote, maxNote, 14.0);
					chordBaseNote += chords[1];
					synthTrebleNoteWithOvertones(startTime, endTime, chordBaseNote, maxNote, 14.0);
				}
				if(chords.length == 3) {
					synthTrebleNoteWithOvertones(startTime, endTime, chordBaseNote, maxNote ,14.0);
					chordBaseNote += chords[0];
					synthTrebleNoteWithOvertones(startTime, endTime, chordBaseNote, maxNote, 14.0);
					chordBaseNote += chords[1];
					synthTrebleNoteWithOvertones(startTime, endTime, chordBaseNote, maxNote, 14.0);
					chordBaseNote += chords[2];
					synthTrebleNoteWithOvertones(startTime, endTime, chordBaseNote, maxNote, 14.0);
				}
			}
		}
		// Synth Bass Instrument
		if(harmonicIDToBassSynthHarmonic != null) {
			synthInstrumentEQ(startTime, endTime, lowestNote, null, harmonicIDToBassSynthHarmonic, 0.0);
		} else {
			synthBassNoteWithOvertones(startTime, endTime, lowestNote, baseNote, 14.0);
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
				data.setLogAmplitude(data.getLogAmplitude() + gain);
				newHarmonic.addData(data);
			}
			beatStartTimeToHarmonics.get(startTime).add(newHarmonic);
		}
	}

	private static void synthInstrumentEQ(int startTime, int endTime, int baseNote, int[] chords, TreeMap<Long, Harmonic> harmonics, double gain) {
		double minAmpVal = 0.0;
		TreeMap<Integer, Harmonic> noteToHarmonic = new TreeMap<Integer, Harmonic>();
		TreeMap<Integer, TreeMap<Integer, Double>> timeToNoteToEQ = new TreeMap<Integer, TreeMap<Integer, Double>>(); 
		for(int note = FDData.getMinNote(); note < FDData.getMaxNote(); note++) {
			for(Harmonic harmonic: harmonics.values()) {
				if(harmonic.getAverageNote() == note) {
					noteToHarmonic.put(note, harmonic);
				}
			}
		}
		int minEQNote = noteToHarmonic.firstKey();
		int maxEQNote = noteToHarmonic.lastKey();
		int numTimes = endTime - startTime;
		int numNotes = FDData.getMaxNote() - FDData.getMinNote();
		double[][] EQMatrix = new double[numTimes][numNotes];
		for(int time = startTime; time < endTime; time++) {
			int normalTime = time - startTime;
			timeToNoteToEQ.put(normalTime, new TreeMap<Integer, Double>());
			double currentLogAmp = -1.0;
			for(int note: noteToHarmonic.keySet()) {
				// use first value occuring in lowest note (may not be at time == 1)
				if(currentLogAmp == -1.0) currentLogAmp = noteToHarmonic.get(note).getAllData().get(0).getLogAmplitude();
				if(noteToHarmonic.get(note).getDataAtTime(normalTime) != null) {
					currentLogAmp = noteToHarmonic.get(note).getDataAtTime(normalTime).getLogAmplitude();
					timeToNoteToEQ.get(normalTime).put(note, currentLogAmp);
				} else {
					// use currentLogAmp from previous note
					timeToNoteToEQ.get(normalTime).put(note, currentLogAmp);
					timeToNoteToEQ.get(normalTime).put(note, 0.0);
				}
			}
		}
		ArrayList<Integer> notes = new ArrayList<Integer>(noteToHarmonic.keySet());
		for(int time = 0; time < numTimes; time++) {
			// fill in values for notes < min note in instrument data
			if(minEQNote > FDData.getMinNote()) {
				int startNote = FDData.getMinNote();
				int endNote = minEQNote;
				double logAmpVal = timeToNoteToEQ.get(time).get(minEQNote);
				if(logAmpVal < minAmpVal) logAmpVal = minAmpVal;
				for(int note = startNote; note < endNote; note++) {
					EQMatrix[time][note - FDData.getMinNote()] = logAmpVal; 
				}
			}
			// fill in values for notes between min and max notes in instrument data
			for(int noteIndex = 0; noteIndex < notes.size() - 1; noteIndex++) {
				int startNote = notes.get(noteIndex);
				int endNote = notes.get(noteIndex + 1);
				double startLogAmp = timeToNoteToEQ.get(time).get(startNote);
				double endLogAmp = timeToNoteToEQ.get(time).get(endNote);
				double slope = (endLogAmp - startLogAmp) / (endNote - startNote);
				for(int note = startNote; note < endNote; note++) {
					double ampVal = (note - startNote) * slope + startLogAmp;
					if(ampVal < minAmpVal) ampVal = minAmpVal;
					EQMatrix[time][note - FDData.getMinNote()] = ampVal;
				}
			}
			// fill in values for notes > max note in instrument data
			if(maxEQNote < FDData.getMaxNote()) {
				int startNote = maxEQNote;
				int endNote = FDData.getMaxNote();
				double numOctaves = (double) (endNote - startNote) / FDData.noteBase;
				double startLogAmp = timeToNoteToEQ.get(time).get(startNote);
				double endLogAmp = startLogAmp / 2.0;
				if(endLogAmp < 0.0) endLogAmp = 0.0;
				double slope = (endLogAmp - startLogAmp) / (endNote - startNote);
				for(int note = startNote; note < endNote; note++) {
					double ampVal = (note - startNote) * slope + startLogAmp;
					if(ampVal < minAmpVal) ampVal = minAmpVal;
					EQMatrix[time][note - FDData.getMinNote()] = ampVal;
				}
			}
		}
		double lowestNote = baseNote;
		while(lowestNote >= FDData.getMinNote()) lowestNote -= FDData.noteBase;
		lowestNote += FDData.noteBase;
		ArrayList<Double> baseNotes = new ArrayList<Double>();
		baseNotes.add(lowestNote); // baseNote synth low bass as well
		double noteVal = baseNote;
		if(chords != null) {
			for(double chord: chords) {
				noteVal += chord;
				baseNotes.add(noteVal);
			}
		}
		int currentNoteIndex = 0;
		for(double currentNote: baseNotes) {
			for(double note = currentNote; note < FDData.getMaxNote(); note += FDData.noteBase) {
				long harmonicID = HarmonicsEditor.getRandomID();
				Harmonic newHarmonic = new Harmonic(harmonicID);
				double taperVal = ((note - baseNote) / FDData.noteBase) * 0.25;
				for(int time = 0; time < numTimes; time++) {
					try {
						double logAmpVal = EQMatrix[time][(int) Math.round(note) - FDData.getMinNote()];
						//if(currentNoteIndex == 1) logAmpVal -= 2.0;
						//if(currentNoteIndex == 2) logAmpVal -= 1.0;
						if(chords != null) {
							//logAmpVal -= taperVal;
						}
						if(logAmpVal < 0.0) continue;
						FDData data = new FDData(time + startTime, note, logAmpVal, harmonicID);
						newHarmonic.addData(data);
					} catch (Exception e) {
						System.out.println("synthInstrumentEQ: Error creating data");
					}
				}
			beatStartTimeToHarmonics.get(startTime).add(newHarmonic);
			}
			currentNoteIndex++;
		}
		synthInstrument(startTime, endTime, -1, harmonics, -2.0);
	}

	private static double sawTooth(double phase) {
		phase /= 2.0 * Math.PI;
		phase -= Math.floor(phase);
		return (phase - 0.5) / 2.0;
	}
	
	private static void synthNote(int startTime, int endTime, double note, double amplitude, double taper) {
		try {
			double AMPhase = Math.PI;
			Harmonic currentHarmonic = new Harmonic(HarmonicsEditor.getRandomID());
			for(int time = startTime; time <= endTime; time++) {
				double amplitudeAdjust = Math.sin(AMPhase * taper); // * Math.random();
				double currentAmplitude = amplitude + amplitudeAdjust;
				if(currentAmplitude < 2.0) break;
				FDData data = new FDData(time, note, currentAmplitude, currentHarmonic.getHarmonicID());
				currentHarmonic.addData(data);
			}
			beatStartTimeToHarmonics.get(startTime).add(currentHarmonic);
		} catch (Exception e) {
			System.out.println("HarmonicsEditor.synthNote() error creating data:" + e.getMessage());
		}
	}
	
	private static void synthTrebleNoteWithOvertones(int startTime, int endTime, double minNote, double maxNote, double amplitude) {
		try {
			for(double note = minNote; note < maxNote; note += FDData.noteBase) {
				Harmonic harmonic = new Harmonic(HarmonicsEditor.getRandomID());
				harmonic.addData(new FDData(startTime, note, amplitude, harmonic.getHarmonicID()));
				harmonic.addData(new FDData(endTime - 10, note, amplitude, harmonic.getHarmonicID()));
				harmonic.addData(new FDData(endTime, note, 0.0, harmonic.getHarmonicID()));
				beatStartTimeToHarmonics.get(startTime).add(harmonic);
				amplitude -= 1.25;
				if(amplitude < 2.0) break;
			}
		} catch (Exception e) {
			System.out.println("HarmonicsEditor.synthNoteWithOvertones() error creating data:" + e.getMessage());
		}
	}
	
	private static void synthBassNoteWithOvertones(int startTime, int endTime, double minNote, double maxNote, double amplitude) {
		try {
			for(double note = minNote; note < maxNote; note += FDData.noteBase) {
				Harmonic harmonic = new Harmonic(HarmonicsEditor.getRandomID());
				harmonic.addData(new FDData(startTime, note, amplitude, harmonic.getHarmonicID()));
				harmonic.addData(new FDData(endTime - 10, note, amplitude, harmonic.getHarmonicID()));
				harmonic.addData(new FDData(endTime, note, 0.0, harmonic.getHarmonicID()));
				beatStartTimeToHarmonics.get(startTime).add(harmonic);
			}
		} catch (Exception e) {
			System.out.println("HarmonicsEditor.synthNoteWithOvertones() error creating data:" + e.getMessage());
		}
	}

}