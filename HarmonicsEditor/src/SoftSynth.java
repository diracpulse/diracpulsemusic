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
	
	public static void addBeat(int startTime, int baseNote, int[] chords, int duration, boolean useHighFreq) {
		beatStartTimeToHarmonics.put(startTime, new ArrayList<Harmonic>());
		int maxNote = HarmonicsEditor.frequencyInHzToNote(FDData.maxFrequencyInHz);
		int minNote = HarmonicsEditor.frequencyInHzToNote(40.0);
		int endTime = startTime + duration - 1;
		int lowestNote = baseNote;
		int currentChord = 0;
		while(lowestNote >= minNote) lowestNote -= FDData.noteBase;
		lowestNote += FDData.noteBase;
		// Synth Main Instrument
		if(harmonicIDToInstrumentHarmonic != null) {
			synthInstrument(startTime, endTime, baseNote, harmonicIDToInstrumentHarmonic, 0.0);
			for(int chord: chords) {
				currentChord += chord;
				int note = baseNote + currentChord;
				synthInstrument(startTime, endTime, note, harmonicIDToInstrumentHarmonic, -1.0);
			}
			int harmonicMaxNote = 0;
			for(Harmonic harmonic: beatStartTimeToHarmonics.get(startTime)) {
				if(harmonic.getAverageNote() > harmonicMaxNote) harmonicMaxNote = harmonic.getAverageNote();
			}
			int highFreqStart = baseNote;
			while(highFreqStart <= harmonicMaxNote) highFreqStart += FDData.noteBase;
			//System.out.println(highFreqStart);
			synthHarmonicWithOvertones(startTime, endTime, highFreqStart, maxNote, harmonicIDToInstrumentHarmonic.firstEntry().getValue(), -4.0);
		} else {
			synthNoteWithOvertones(startTime, endTime, baseNote, maxNote, 14.0);
			for(int chord: chords) {
				currentChord += chord;
				int note = baseNote + currentChord;
				synthNoteWithOvertones(startTime, endTime, note, maxNote, 14.0);
			}
		}
		// Synth Bass Instrument
		if(harmonicIDToBassSynthHarmonic != null) {
			synthInstrument(startTime, endTime, lowestNote, harmonicIDToBassSynthHarmonic, 0.0);
		} else {
			synthNoteWithOvertones(startTime, endTime, lowestNote, baseNote, 14.0);
		}
		fitHarmonicsToChords(startTime, lowestNote, chords, true);
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
				for(int mergeNote = currentNote - 2; mergeNote <= currentNote + 2; mergeNote++) {
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

	public static void synthSingleNote(int startTime, int endTime, int note, double amplitude) {
		try {
			long harmonicID =  HarmonicsEditor.getRandomID();
			int attackTime = startTime + 2;
			FDData start = new FDData(startTime, note, amplitude - 2.0, harmonicID);
			FDData attack = new FDData(attackTime, note, amplitude, harmonicID);
			FDData end = new FDData(endTime, note, amplitude - 2.0, harmonicID);
			Harmonic harmonic = new Harmonic(harmonicID);
			harmonic.addData(start);
			harmonic.addData(attack);
			harmonic.addData(end);
			TreeMap<Long, Harmonic> harmonicsArg = new TreeMap<Long, Harmonic>();
			harmonicsArg.put(harmonicID, harmonic);
			synthInstrument(0, endTime, note, harmonicsArg, 0.0);
		} catch (Exception e) {
			System.out.println("HarmonicsEditor.synthNoteWithOvertones() error creating data:" + e.getMessage());
		}		
	}

	public static void synthNoteWithOvertones(int startTime, int endTime, int minNote, int maxNote, double maxAmplitude) {
		int note = minNote;
		int duration = endTime - startTime;
		try {
			while(note < maxNote) {
				double taper = (note - minNote) / (double) FDData.noteBase;
				taper = Math.pow(taper, 1.25);
				double currentAmplitude = maxAmplitude - taper;
				int currentEndTime = startTime + (int) Math.round(duration / (taper * 0.10 + 1.0));
				if(currentAmplitude < 2.0) break;
				synthSingleNote(startTime, currentEndTime, note, currentAmplitude);
				note += FDData.noteBase;
			}	
		} catch (Exception e) {
			System.out.println("HarmonicsEditor.synthNoteWithOvertones() error creating data:" + e.getMessage());
		}
	}

}