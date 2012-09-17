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
	public static ArrayList<Beat> beatArray = null;
	public static TreeMap<Integer, ArrayList<Harmonic>> beatStartTimeToHarmonics = null;
	public static final double logAmplitudeLimit = 12.0;
	
	public static void clearLoop() {
		beatArray = null;
	}
	
	public static void initLoop(int numBeats, int duration) {
		if(beatArray == null) {
			beatArray = new ArrayList<Beat>();
			for(int beat = 0; beat < numBeats; beat++) {
				beatArray.add(new Beat(-1, null, duration));
			}
		}
		beatStartTimeToHarmonics = new TreeMap<Integer, ArrayList<Harmonic>>();
	}
	
	public static void addDataToHarmonicsEditor() {
		synthAllBeats();
		for(int beatStartTime: beatStartTimeToHarmonics.keySet()) {
			for(Harmonic harmonic: beatStartTimeToHarmonics.get(beatStartTime)) {
				//harmonic.addCompressionWithLimiter(4.0, 14.0);
				for(FDData data: harmonic.getAllData()) {
					HarmonicsEditor.addData(data);
				}
			}
		}
	}
	
	public static void modifyBeat(int beatIndex, int baseNote, int[] chords, int duration) {
		beatArray.get(beatIndex).baseNote = baseNote;
		beatArray.get(beatIndex).chords = chords;
		beatArray.get(beatIndex).duration = duration;
	}
	
	public static void synthAllBeats() {
		int startTime = 0;
		for(Beat beat: beatArray) {
			synthBeat(startTime, beat.baseNote, beat.chords, beat.duration, false);
			startTime += beat.duration;
		}
	}
	
	public static void synthBeat(int startTime, int baseNote, int[] chords, int duration, boolean useHighFreq) {
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
			int harmonicMaxNote = 0;
			for(Harmonic harmonic: beatStartTimeToHarmonics.get(startTime)) {
				if(harmonic.getAverageNote() > harmonicMaxNote) harmonicMaxNote = harmonic.getAverageNote();
			}
			//int highFreqStart = baseNote;
			//while(highFreqStart <= harmonicMaxNote) highFreqStart += FDData.noteBase;
			//System.out.println(highFreqStart);
			//synthHarmonicWithOvertones(startTime, endTime, highFreqStart, maxNote, harmonicIDToInstrumentHarmonic.firstEntry().getValue(), -4.0);
		} else {
			if(chords == null) {
				synthTrebleNoteWithOvertones(startTime, endTime, baseNote, maxNote, 14.0);
			} else {
				if(chords.length == 1) {
					synthTrebleNoteWithOvertones(startTime, endTime, baseNote, maxNote, 14.0);
					baseNote += chords[0];
					synthTrebleNoteWithOvertones(startTime, endTime, baseNote, maxNote, 13.0);
				}
				if(chords.length == 2) {
					synthTrebleNoteWithOvertones(startTime, endTime, baseNote, maxNote, 14.0);
					baseNote += chords[0];
					synthTrebleNoteWithOvertones(startTime, endTime, baseNote, maxNote, 13.0);
					baseNote += chords[1];
					synthTrebleNoteWithOvertones(startTime, endTime, baseNote, maxNote, 13.0);
				}
				if(chords.length == 3) {
					synthTrebleNoteWithOvertones(startTime, endTime, baseNote, maxNote ,14.0);
					baseNote += chords[0];
					synthTrebleNoteWithOvertones(startTime, endTime, baseNote, maxNote, 13.0);
					baseNote += chords[1];
					synthTrebleNoteWithOvertones(startTime, endTime, baseNote, maxNote, 13.0);
					baseNote += chords[2];
					synthTrebleNoteWithOvertones(startTime, endTime, baseNote, maxNote, 13.0);
				}
			}
		}
		//fitHarmonicsToChords(startTime, baseNote, chords, true);
		// Synth Bass Instrument
		if(harmonicIDToBassSynthHarmonic != null) {
			synthInstrumentEQ(startTime, endTime, lowestNote, null, harmonicIDToInstrumentHarmonic, 0.0);
		} else {
			// synthBassNoteWithOvertones(startTime, endTime, lowestNote, baseNote, 15.0);
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
	
	public static void synthInstrument(int startTime, int endTime, int note, TreeMap<Long, Harmonic> harmonics, double gain) {
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
	
	public static void synthInstrumentEQ(int startTime, int endTime, int baseNote, int[] chords, TreeMap<Long, Harmonic> harmonics, double gain) {
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
					EQMatrix[time][note - FDData.getMinNote()] = (note - startNote) * slope + startLogAmp; 
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
					EQMatrix[time][note - FDData.getMinNote()] = (note - startNote) * slope + startLogAmp; 
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
	}

	public static FDData getData(int time, int note, double logAmplitude, long harmonicID) {
		FDData returnVal = null;
		try {
			returnVal = new FDData(time, note, 0.0, harmonicID);
		} catch (Exception e) {
			System.out.println("SoftSynth.getData: Error creating data");
		}
		return returnVal;
	}
	
	
	public static double sawTooth(double phase) {
		phase /= 2.0 * Math.PI;
		phase -= Math.floor(phase);
		return (phase - 0.5) / 2.0;
	}
	
	public static void synthNote(int startTime, int endTime, double note, double amplitude, double taper) {
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
	
	public static void synthTrebleNoteWithOvertones(int startTime, int endTime, double minNote, double maxNote, double amplitude) {
		try {
			for(double note = minNote; note < maxNote; note += FDData.noteBase) {
				Harmonic harmonic = new Harmonic(HarmonicsEditor.getRandomID());
				harmonic.addData(new FDData(startTime, note, amplitude, harmonic.getHarmonicID()));
				harmonic.addData(new FDData(endTime - 10, note, amplitude, harmonic.getHarmonicID()));
				harmonic.addData(new FDData(endTime, note, 0.0, harmonic.getHarmonicID()));
				beatStartTimeToHarmonics.get(startTime).add(harmonic);
				amplitude -= 1.0;
				if(amplitude < 2.0) break;
			}
		} catch (Exception e) {
			System.out.println("HarmonicsEditor.synthNoteWithOvertones() error creating data:" + e.getMessage());
		}
	}
	
	public static void synthBassNoteWithOvertones(int startTime, int endTime, double minNote, double maxNote, double amplitude) {
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