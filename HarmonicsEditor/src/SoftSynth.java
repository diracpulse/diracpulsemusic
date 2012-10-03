import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JOptionPane;


public class SoftSynth {
	
	public static TreeMap<Long, Harmonic> harmonicIDToInstrumentHarmonicMono = null;
	public static TreeMap<Long, Harmonic> harmonicIDToKickDrumHarmonicMono = null;
	public static TreeMap<Long, Harmonic> harmonicIDToHighFreqHarmonicMono = null;
	public static TreeMap<Long, Harmonic> harmonicIDToBassSynthHarmonicMono = null;
	public static TreeMap<Long, Harmonic> harmonicIDToSnareHarmonicMono = null;
	public static TreeMap<Long, Harmonic> harmonicIDToInstrumentHarmonicLeft = null;
	public static TreeMap<Long, Harmonic> harmonicIDToKickDrumHarmonicLeft = null;
	public static TreeMap<Long, Harmonic> harmonicIDToHighFreqHarmonicLeft = null;
	public static TreeMap<Long, Harmonic> harmonicIDToBassSynthHarmonicLeft = null;
	public static TreeMap<Long, Harmonic> harmonicIDToSnareHarmonicLeft = null;
	public static TreeMap<Long, Harmonic> harmonicIDToInstrumentHarmonicRight = null;
	public static TreeMap<Long, Harmonic> harmonicIDToKickDrumHarmonicRight = null;
	public static TreeMap<Long, Harmonic> harmonicIDToHighFreqHarmonicRight = null;
	public static TreeMap<Long, Harmonic> harmonicIDToBassSynthHarmonicRight = null;
	public static TreeMap<Long, Harmonic> harmonicIDToSnareHarmonicRight = null;
	public static TreeMap<Integer, ArrayList<Harmonic>> beatStartTimeToHarmonicsMono = null;
	public static TreeMap<Integer, ArrayList<Harmonic>> beatStartTimeToHarmonicsLeft = null;
	public static TreeMap<Integer, ArrayList<Harmonic>> beatStartTimeToHarmonicsRight = null;
	public static final double logAmplitudeLimit = 12.0;

	public static void synthLoopInHarmonicsEditor(ArrayList<Beat> beatArray) {
		HarmonicsEditor.clearCurrentData();
		synthAllBeats(beatArray);
		for(int beatStartTime: beatStartTimeToHarmonicsMono.keySet()) {
			for(Harmonic harmonic: beatStartTimeToHarmonicsMono.get(beatStartTime)) {
				//harmonic.addCompressionWithLimiter(4.0, 14.0);
				for(FDData data: harmonic.getAllData()) {
					HarmonicsEditor.addData(data, 0);
				}
			}
		}
		for(int beatStartTime: beatStartTimeToHarmonicsLeft.keySet()) {
			for(Harmonic harmonic: beatStartTimeToHarmonicsLeft.get(beatStartTime)) {
				//harmonic.addCompressionWithLimiter(4.0, 14.0);
				for(FDData data: harmonic.getAllData()) {
					HarmonicsEditor.addData(data, 1);
				}
			}
		}
		for(int beatStartTime: beatStartTimeToHarmonicsRight.keySet()) {
			for(Harmonic harmonic: beatStartTimeToHarmonicsRight.get(beatStartTime)) {
				//harmonic.addCompressionWithLimiter(4.0, 14.0);
				for(FDData data: harmonic.getAllData()) {
					HarmonicsEditor.addData(data, 2);
				}
			}
		}		
	}
	
	private static void synthAllBeats(ArrayList<Beat> beatArray) {
		beatStartTimeToHarmonicsMono = new TreeMap<Integer, ArrayList<Harmonic>>();
		beatStartTimeToHarmonicsLeft = new TreeMap<Integer, ArrayList<Harmonic>>();
		beatStartTimeToHarmonicsRight = new TreeMap<Integer, ArrayList<Harmonic>>();
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
		beatStartTimeToHarmonicsMono.put(startTime, new ArrayList<Harmonic>());
		beatStartTimeToHarmonicsLeft.put(startTime, new ArrayList<Harmonic>());
		beatStartTimeToHarmonicsRight.put(startTime, new ArrayList<Harmonic>());
		int maxNote = (int) Math.round(Math.floor(HarmonicsEditor.frequencyInHzToNote(FDData.maxFrequencyInHz)) - 1.0);
		int minNote = HarmonicsEditor.frequencyInHzToNote(32.0);
		int endTime = startTime + duration - 1;
		int lowestNote = baseNote;
		while(lowestNote >= minNote) lowestNote -= FDData.noteBase;
		lowestNote += FDData.noteBase;
		// Synth Main Instrument
		if(harmonicIDToInstrumentHarmonicMono != null) {
			synthInstrumentEQ(startTime, endTime, baseNote, chords, harmonicIDToInstrumentHarmonicMono, 0.0, 0);
			synthInstrumentEQ(startTime, endTime, baseNote, chords, harmonicIDToInstrumentHarmonicLeft, 0.0, 1);
			synthInstrumentEQ(startTime, endTime, baseNote, chords, harmonicIDToInstrumentHarmonicRight, 0.0, 2);
			//synthInstrument(startTime, endTime, baseNote, chords, harmonicIDToInstrumentHarmonicMono, -6.0, 0);
			//synthInstrument(startTime, endTime, baseNote, chords, harmonicIDToInstrumentHarmonicLeft, -6.0, 1);
			//synthInstrument(startTime, endTime, baseNote, chords, harmonicIDToInstrumentHarmonicRight, -6.0, 2);
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
		if(harmonicIDToBassSynthHarmonicMono != null) {
			synthInstrument(startTime, endTime, lowestNote, null, harmonicIDToBassSynthHarmonicMono, 0.0, 0);
			synthInstrument(startTime, endTime, lowestNote, null, harmonicIDToBassSynthHarmonicLeft, 0.0, 1);
			synthInstrument(startTime, endTime, lowestNote, null, harmonicIDToBassSynthHarmonicRight, 0.0, 2);
		} else {
			//synthBassNoteWithOvertones(startTime, endTime, lowestNote, baseNote, 14.0);
		}
		// Synth Noise Sources
		if(harmonicIDToKickDrumHarmonicMono != null) {
			synthInstrument(startTime, endTime, -1, harmonicIDToKickDrumHarmonicMono, 0.0, 0);
			synthInstrument(startTime, endTime, -1, harmonicIDToKickDrumHarmonicLeft, 0.0, 1);
			synthInstrument(startTime, endTime, -1, harmonicIDToKickDrumHarmonicRight, 0.0, 2);
		}
		if(useHighFreq) {
			if(harmonicIDToHighFreqHarmonicMono != null) {
				synthInstrument(startTime, endTime, -1, harmonicIDToHighFreqHarmonicMono, 0.0, 0);
				synthInstrument(startTime, endTime, -1, harmonicIDToHighFreqHarmonicLeft, 0.0, 1);
				synthInstrument(startTime, endTime, -1, harmonicIDToHighFreqHarmonicRight, 0.0, 2);
			}
		} else {
			if(harmonicIDToSnareHarmonicMono != null) {
				synthInstrument(startTime, endTime, -1, harmonicIDToSnareHarmonicMono, 0.0, 0);
				synthInstrument(startTime, endTime, -1, harmonicIDToSnareHarmonicLeft, 0.0, 1);
				synthInstrument(startTime, endTime, -1, harmonicIDToSnareHarmonicRight, 0.0, 2);
			}
		}
		//fitHarmonicsToChords(startTime, lowestNote, chords, false);
		//removeDissonance(startTime, endTime);
		HarmonicsEditor.refreshView();
	}
	
	private static void synthInstrument(int startTime, int endTime, int note, int[] chords, TreeMap<Long, Harmonic> harmonics, double gain, int channel) {
		if(chords == null) {
			synthInstrument(startTime, endTime, note, harmonics, gain, channel); 
			return;
		}
		synthInstrument(startTime, endTime, note, harmonics, gain, channel);
		int currentNote = note;
		for(int chord: chords) {
			currentNote += chord;
			synthInstrument(startTime, endTime, currentNote, harmonics, gain, channel);
		}
	}
	
	private static void synthInstrument(int startTime, int endTime, int note, TreeMap<Long, Harmonic> harmonics, double gain, int channel) {
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
			if(channel == 0) beatStartTimeToHarmonicsMono.get(startTime).add(newHarmonic);
			if(channel == 1) beatStartTimeToHarmonicsLeft.get(startTime).add(newHarmonic);
			if(channel == 2) beatStartTimeToHarmonicsRight.get(startTime).add(newHarmonic);
		}
	}

	private static void synthInstrumentEQ(int startTime, int endTime, int baseNote, int[] chords, TreeMap<Long, Harmonic> rawHarmonics, double gain, int channel) {
		TreeMap<Long, Harmonic> harmonics = getScaledHarmonics(rawHarmonics, baseNote);
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
			double currentLogAmp = 0.0;
			for(int note: noteToHarmonic.keySet()) {
				// use first value occuring in lowest note (may not be at time == 1)
				// if(currentLogAmp == -1.0) currentLogAmp = noteToHarmonic.get(note).getAllData().get(0).getLogAmplitude();
				if(noteToHarmonic.get(note).getDataAtTime(normalTime) != null) {
					currentLogAmp = noteToHarmonic.get(note).getDataAtTime(normalTime).getLogAmplitude();
					timeToNoteToEQ.get(normalTime).put(note, currentLogAmp);
				} else {
					// use currentLogAmp from previous note
					if(currentLogAmp > 2.0) {
						timeToNoteToEQ.get(normalTime).put(note, 0.0) ; // currentLogAmp - 2.0);
					} else {
						timeToNoteToEQ.get(normalTime).put(note, 0.0);
					}
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
					EQMatrix[time][note - FDData.getMinNote()] = 0.0; 
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
		ArrayList<Double> baseNotes = new ArrayList<Double>();
		double noteVal = baseNote;
		baseNotes.add(noteVal);
		if(chords != null) {
			for(double chord: chords) {
				noteVal += chord;
				baseNotes.add(noteVal);
			}
		}
		int halfStep = (int) Math.round(Math.log(1.5) / Math.log(2.0) * FDData.noteBase);
		for(double currentNote: baseNotes) {
			int deltaNote = (int) Math.round(currentNote - baseNote);
			int currentStep = FDData.noteBase;
			double logAdjust = 0.0;
			for(double note = currentNote; note < FDData.getMaxNote(); note += currentStep) {
				long harmonicID = HarmonicsEditor.getRandomID();
				Harmonic newHarmonic = new Harmonic(harmonicID);
				double logAmpVal = 0.0;
				for(int time = 0; time < numTimes; time++) {
					try {
						int noteIndex = (int) Math.round(note) - FDData.getMinNote() - deltaNote;
						if(noteIndex < 0 || noteIndex >= numNotes) break;
						logAmpVal = EQMatrix[time][noteIndex];
						logAmpVal += logAdjust;
						if(logAmpVal < 0.0) continue;
						FDData data = new FDData(time + startTime, note, logAmpVal, harmonicID);
						newHarmonic.addData(data);
					} catch (Exception e) {
						System.out.println("synthInstrumentEQ: Error creating data: " + time + startTime + " " + note + " " + logAmpVal);
					}
				}
				if(channel == 0) beatStartTimeToHarmonicsMono.get(startTime).add(newHarmonic);
				if(channel == 1) beatStartTimeToHarmonicsRight.get(startTime).add(newHarmonic);
				if(channel == 2) beatStartTimeToHarmonicsLeft.get(startTime).add(newHarmonic);
				if(note > FDData.noteBase * 9) {
					if(Math.round(note - currentNote) % FDData.noteBase == 0) {
						currentStep = FDData.noteBase; // halfStep;
						logAdjust = 0.0;
					} else {
						currentStep = FDData.noteBase; // (FDData.noteBase - halfStep);
						logAdjust = 0.0;
					}
				}
			}
		}
	}
	
	private static TreeMap<Long, Harmonic> getScaledHarmonics(TreeMap<Long, Harmonic> harmonics, int baseNote) {
		TreeMap<Long, Harmonic> scaledHarmonics = new TreeMap<Long, Harmonic>();
		double currentAmplitude = 0.0;
		double maxAmplitude = 0.0;
		int maxAmplitudeNote = FDData.getMinNote();
		for(Harmonic harmonic: harmonics.values()) {
			currentAmplitude = harmonic.getMaxLogAmplitude();
			if(currentAmplitude > maxAmplitude) {
				maxAmplitude = currentAmplitude;
				maxAmplitudeNote = harmonic.getAverageNote();
			}
		}
		int deltaNote = baseNote - maxAmplitudeNote;
		for(Harmonic harmonic: harmonics.values()) {
			Harmonic scaledHarmonic = new Harmonic(HarmonicsEditor.getRandomID());
			for(FDData data: harmonic.getScaledHarmonic(0, FDData.maxTime, deltaNote, scaledHarmonic.getHarmonicID())) {
				scaledHarmonic.addData(data);
			}
			if(scaledHarmonic.getAverageNote() >= FDData.getMinNote()) {
				scaledHarmonics.put(scaledHarmonic.getHarmonicID(), scaledHarmonic);
			}
		}
		return scaledHarmonics;
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
			beatStartTimeToHarmonicsMono.get(startTime).add(currentHarmonic);
			beatStartTimeToHarmonicsLeft.get(startTime).add(currentHarmonic);
			beatStartTimeToHarmonicsRight.get(startTime).add(currentHarmonic);
		} catch (Exception e) {
			System.out.println("HarmonicsEditor.synthNote() error creating data:" + e.getMessage());
		}
	}
	
	private static void synthTrebleNoteWithOvertones(int startTime, int endTime, double minNote, double maxNote, double amplitude) {
		try {
			for(double note = minNote; note < maxNote; note += FDData.noteBase) {
				synthHarmonic(startTime, endTime, note, amplitude);
				//synthFormant(startTime, endTime, note, amplitude);
				amplitude -= 1.25;
				if(amplitude < 2.0) break;
			}
		} catch (Exception e) {
			System.out.println("HarmonicsEditor.synthNoteWithOvertones() error creating data:" + e.getMessage());
		}
	}
	
	private static void synthHarmonic(int startTime, int endTime, double note, double amplitude) {
		try {
			Harmonic harmonic = new Harmonic(HarmonicsEditor.getRandomID());
			harmonic.addData(new FDData(startTime, note, 0.0, harmonic.getHarmonicID()));
			harmonic.addData(new FDData(startTime + 4, note, amplitude, harmonic.getHarmonicID()));
			harmonic.addData(new FDData(endTime - 10, note, amplitude, harmonic.getHarmonicID()));
			harmonic.addData(new FDData(endTime, note, 0.0, harmonic.getHarmonicID()));
			beatStartTimeToHarmonicsMono.get(startTime).add(harmonic);
			beatStartTimeToHarmonicsLeft.get(startTime).add(harmonic);
			beatStartTimeToHarmonicsRight.get(startTime).add(harmonic);
		} catch (Exception e) {
			System.out.println("HarmonicsEditor.synthNoteWithOvertones() error creating data:" + e.getMessage());
		}
	}
	
	private static void synthFormant(int startTime, int endTime, double note, double amplitude) {
		try {
			int length = endTime - startTime;
			for(double formant = note - 3; formant <= note + 3; formant++) {
				int formantLength = (int) Math.round(4.0 * Math.random() + 4.0);
				int formantPeakOnset = (int) Math.round(4.0 * Math.random());
				int formantEndTime = formantLength + startTime;
				int formantPeak = formantPeakOnset + startTime;
				if(formantPeak >= formantEndTime) continue;
 				Harmonic harmonic = new Harmonic(HarmonicsEditor.getRandomID());
				harmonic.addData(new FDData(startTime, formant, 0.0, harmonic.getHarmonicID()));
				harmonic.addData(new FDData(formantPeak, formant, amplitude - Math.random(), harmonic.getHarmonicID()));
				harmonic.addData(new FDData(formantEndTime, formant, 0.0, harmonic.getHarmonicID()));
				beatStartTimeToHarmonicsMono.get(startTime).add(harmonic);
				beatStartTimeToHarmonicsLeft.get(startTime).add(harmonic);
				beatStartTimeToHarmonicsRight.get(startTime).add(harmonic);
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
				beatStartTimeToHarmonicsMono.get(startTime).add(harmonic);
				beatStartTimeToHarmonicsLeft.get(startTime).add(harmonic);
				beatStartTimeToHarmonicsRight.get(startTime).add(harmonic);
			}
		} catch (Exception e) {
			System.out.println("HarmonicsEditor.synthNoteWithOvertones() error creating data:" + e.getMessage());
		}
	}

}