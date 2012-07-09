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
	
	public static void initLoop() {
		beatStartTimeToHarmonics = new TreeMap<Integer, ArrayList<Harmonic>>();
	}
	
	public static void addBeat(int startTime, double baseNote, double[] chords, int duration, boolean useHighFreq) {
		beatStartTimeToHarmonics.put(startTime, new ArrayList<Harmonic>());
		double maxNote = Math.floor(HarmonicsEditor.frequencyInHzToNote(FDData.maxFrequencyInHz)) - 1.0;
		double minNote = HarmonicsEditor.frequencyInHzToNote(40.0);
		int endTime = startTime + duration - 1;
		double lowestNote = baseNote;
		int currentChord = 0;
		while(lowestNote >= minNote) lowestNote -= FDData.noteBase;
		lowestNote += FDData.noteBase;
		// Synth Main Instrument
		if(harmonicIDToInstrumentHarmonic != null) {
			synthInstrumentEQ(startTime, endTime, baseNote, chords, harmonicIDToInstrumentHarmonic, 0.0);
			for(double chord: chords) {
				currentChord += chord;
				double note = baseNote + currentChord;
				//synthInstrument(startTime, endTime, (int) note, harmonicIDToInstrumentHarmonic, 0.0);
			}
			int harmonicMaxNote = 0;
			for(Harmonic harmonic: beatStartTimeToHarmonics.get(startTime)) {
				if(harmonic.getAverageNote() > harmonicMaxNote) harmonicMaxNote = harmonic.getAverageNote();
			}
			//int highFreqStart = baseNote;
			//while(highFreqStart <= harmonicMaxNote) highFreqStart += FDData.noteBase;
			//System.out.println(highFreqStart);
			//synthHarmonicWithOvertones(startTime, endTime, highFreqStart, maxNote, harmonicIDToInstrumentHarmonic.firstEntry().getValue(), -4.0);
		} else {
			if(chords.length == 0) {
				synthTrebleNoteWithOvertones(startTime, endTime, baseNote, maxNote, 14.0);
			}
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
		//fitHarmonicsToChords(startTime, baseNote, chords, true);
		// Synth Bass Instrument
		if(harmonicIDToBassSynthHarmonic != null) {
			synthInstrumentEQ(startTime, endTime, lowestNote, null, harmonicIDToInstrumentHarmonic, 0.0);
		} else {
			//synthBassNoteWithOvertones(startTime, endTime, lowestNote, baseNote, 15.0, Harmonic.Waveform.SAWTOOTH);
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
	
	public static void synthInstrumentEQ(int startTime, int endTime, double baseNote, double[] chords, TreeMap<Long, Harmonic> harmonics, double gain) {
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
				if(currentLogAmp == -1.0) currentLogAmp = noteToHarmonic.get(note).getAllData().get(0).getLogAmplitude();
				if(noteToHarmonic.get(note).getDataAtTime(normalTime) != null) {
					currentLogAmp = noteToHarmonic.get(note).getDataAtTime(normalTime).getLogAmplitude();
					timeToNoteToEQ.get(normalTime).put(note, currentLogAmp);
				} else {
					timeToNoteToEQ.get(normalTime).put(note, currentLogAmp);
				}
			}
		}
		ArrayList<Integer> notes = new ArrayList<Integer>(noteToHarmonic.keySet());
		for(int time = 0; time < numTimes; time++) {
			if(minEQNote > FDData.getMinNote()) {
				int startNote = FDData.getMinNote();
				int endNote = minEQNote;
				double logAmpVal = timeToNoteToEQ.get(time).get(minEQNote);
				for(int note = startNote; note < endNote; note++) {
					EQMatrix[time][note - FDData.getMinNote()] = logAmpVal; 
				}
			}
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
			if(maxEQNote < FDData.getMaxNote()) {
				int startNote = maxEQNote;
				int endNote = FDData.getMaxNote();
				double startLogAmp = timeToNoteToEQ.get(time).get(startNote);
				double endLogAmp = 0.0;
				double slope = (endLogAmp - startLogAmp) / (endNote - startNote);
				for(int note = startNote; note < endNote; note++) {
					EQMatrix[time][note - FDData.getMinNote()] = 0.0; // (note - startNote) * slope + startLogAmp; 
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
						if(currentNoteIndex == 1) logAmpVal -= 2.0;
						if(currentNoteIndex == 2) logAmpVal -= 1.0;
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
	
	public static void synthHarmonicWithOvertones(int startTime, int endTime, int note, int maxNote, Harmonic harmonic, double gain) {
		int harmonicNote = harmonic.getAverageNote();
		int deltaNote = note - harmonic.getAverageNote();
		while((harmonicNote + deltaNote) < maxNote) {
			long harmonicID = HarmonicsEditor.getRandomID();
			ArrayList<FDData> harmonicData = harmonic.getScaledHarmonic(startTime, endTime, deltaNote, harmonicID);
			Harmonic newHarmonic = new Harmonic(harmonicID);
			for(FDData data: harmonicData) {
				data.setLogAmplitude(data.getLogAmplitude() + gain);
				newHarmonic.addData(data);
			}
			gain -= 2.0;
			beatStartTimeToHarmonics.get(startTime).add(newHarmonic);
			deltaNote += FDData.noteBase;
		}
	}
	
	public static void fitHarmonicsToChords(int startTime, int lowestNote, int[] chords, boolean chordsOnly) { 
		ArrayList<Harmonic> harmonics = beatStartTimeToHarmonics.get(startTime);
		TreeMap<Integer, Harmonic> noteToHarmonic = new TreeMap<Integer, Harmonic>();
		TreeMap<Integer, Harmonic> noteToHarmonicChordsOnly = new TreeMap<Integer, Harmonic>();
		for(Harmonic harmonic: harmonics) {
			int note = harmonic.getAverageNote();
			if(noteToHarmonic.containsKey(note)) {
				//System.out.println("SoftSynth.fitHarmonicsToChords: duplicate note");
				noteToHarmonic.put(note, mergeHarmonics(noteToHarmonic.get(note), harmonic));
			}
			noteToHarmonic.put(note, harmonic);
		}
		for(int baseNote = lowestNote; baseNote < FDData.getMaxNote(); baseNote += FDData.noteBase) {
			int currentChord = 0;
			int chordIndex = 0;
			while(true) {
				int currentNote = baseNote + currentChord;
				if(currentNote > FDData.getMaxNote()) break;
				ArrayList<Harmonic> harmonicsToMerge = new ArrayList<Harmonic>();
				for(int mergeNote = currentNote - 3; mergeNote <= currentNote + 3; mergeNote++) {
					if(noteToHarmonic.containsKey(mergeNote)) {
						harmonicsToMerge.add(noteToHarmonic.get(mergeNote));
						noteToHarmonic.remove(mergeNote);
					}
				}
				Harmonic mergedHarmonic = mergeHarmonics(harmonicsToMerge, currentNote);
				if(mergedHarmonic != null) {
					noteToHarmonic.put(currentNote, mergedHarmonic);
					noteToHarmonicChordsOnly.put(currentNote, mergedHarmonic);
				}
				if(chordIndex >= chords.length) break;
				currentChord += chords[chordIndex];
				chordIndex++;
			}
		}
		if(chordsOnly) {
			beatStartTimeToHarmonics.put(startTime, new ArrayList<Harmonic>(noteToHarmonicChordsOnly.values()));
		} else {
			beatStartTimeToHarmonics.put(startTime, new ArrayList<Harmonic>(noteToHarmonic.values()));
		}
	}
		
	public static Harmonic mergeHarmonics(ArrayList<Harmonic> harmonics, int mergeNote) {
		if(harmonics.size() == 0) return null;
		long harmonicID = HarmonicsEditor.getRandomID();
		Harmonic mergedHarmonic = harmonics.get(0);
		for(int index = 1; index < harmonics.size(); index++) {
			mergedHarmonic = mergeHarmonics(mergedHarmonic, harmonics.get(index));
		}
		ArrayList<FDData> harmonicData = mergedHarmonic.getScaledAverageNote(mergeNote);
		Harmonic newHarmonic = new Harmonic(harmonicID);
		for(FDData data: harmonicData) newHarmonic.addData(data);
		return newHarmonic;
	}

	public static Harmonic mergeHarmonics(Harmonic h1, Harmonic h2) {
		long harmonicID = HarmonicsEditor.getRandomID();
		Harmonic returnVal = new Harmonic(harmonicID);
		TreeMap<Integer, FDData> timeToData1 = new TreeMap<Integer, FDData>();
		TreeMap<Integer, FDData> timeToData2 = new TreeMap<Integer, FDData>();
		for(FDData data: h1.getAllDataInterpolated()) {
			timeToData1.put(data.getTime(), data);
		}
		for(FDData data: h2.getAllDataInterpolated()) {
			timeToData2.put(data.getTime(), data);
		}
		if(timeToData1.isEmpty() && timeToData2.isEmpty()) return returnVal;
		if(timeToData1.isEmpty()) return h2;
		if(timeToData2.isEmpty()) return h1;
		int startTime = timeToData1.firstKey();
		int endTime = timeToData1.lastKey();
		if(timeToData2.firstKey() < startTime) startTime = timeToData2.firstKey();
		if(timeToData2.lastKey() > endTime) endTime = timeToData2.lastKey();
		for(int time = startTime; time <= endTime; time++) {
			if(timeToData1.containsKey(time)) {
				FDData d1 = timeToData1.get(time);
				if(timeToData2.containsKey(time)) {
					FDData d2 = timeToData2.get(time);
					double amplitude1 = Math.pow(2.0, d1.getLogAmplitude());
					double amplitude2 = Math.pow(2.0, d2.getLogAmplitude());
					double logAmplitude = Math.log(amplitude1 + amplitude2) / Math.log(2.0);
					int note = (d1.getNote() + d2.getNote()) / 2;
					try {
						FDData combined = new FDData(time, note, logAmplitude, harmonicID);
						returnVal.addData(combined);
					} catch (Exception e) {
						System.out.println("SoftSynth.mergeHarmonics: Error creating data");
					}
				} else {
					returnVal.addData(timeToData1.get(time));
				}
			} else {
				if(timeToData2.containsKey(time)) {
					returnVal.addData(timeToData2.get(time));
				} else {
					returnVal.addData(getData(time, h1.getAverageNote(), 0.0, harmonicID));
				}
			}
		}
		returnVal.setAllHarmonicIDsEqual();
		return returnVal;
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
	
	

	public static void addDataToHarmonicsEditor() {
		for(int beatStartTime: beatStartTimeToHarmonics.keySet()) {
			for(Harmonic harmonic: beatStartTimeToHarmonics.get(beatStartTime)) {
				//harmonic.addCompressionWithLimiter(4.0, 14.0);
				for(FDData data: harmonic.getAllData()) {
					HarmonicsEditor.addData(data);
				}
			}
		}
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
			double AMPhase = Math.PI;
			for(double loopNote = minNote; loopNote < maxNote - 3; loopNote += FDData.noteBase) {
				double note = loopNote;
				Harmonic harmonic = new Harmonic(HarmonicsEditor.getRandomID());
				double taper = (note - minNote) / (double) FDData.noteBase;
				for(int time = startTime; time < endTime - 10; time++) {
					double amplitudeAdjust = Math.sin(AMPhase * taper);
					double currentAmplitude = amplitude + amplitudeAdjust - taper / 2.0;
					if(currentAmplitude < 4.0) break;
					FDData data = new FDData(time, note, currentAmplitude, harmonic.getHarmonicID());
					harmonic.addData(data);
					AMPhase += (2.0 * Math.PI) / (endTime - startTime) * taper;
				}
				beatStartTimeToHarmonics.get(startTime).add(harmonic);
			}
			for(double note = minNote + FDData.noteBase * 2; note < maxNote - 3; note += FDData.noteBase) {
				if(note < 8 * FDData.noteBase) continue;
				double taper = (note - minNote) / (double) FDData.noteBase;
				Harmonic formant = new Harmonic(HarmonicsEditor.getRandomID());
				formant.addData(new FDData(startTime, note, amplitude, formant.getHarmonicID()));
				formant.addData(new FDData(startTime + 100, note, 0.0, formant.getHarmonicID()));
				beatStartTimeToHarmonics.get(startTime).add(formant);
			}
		} catch (Exception e) {
			System.out.println("HarmonicsEditor.synthNoteWithOvertones() error creating data:" + e.getMessage());
		}
	}
	
	public static void synthBassNoteWithOvertones(int startTime, int endTime, double minNote, double maxNote, double amplitude) {
		try {
			double AMPhase = Math.PI;
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