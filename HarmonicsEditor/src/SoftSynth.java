import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


public class SoftSynth {
	
	public static TreeMap<Long, Harmonic> harmonicIDToInstrumentHarmonic = null;
	public static TreeMap<Long, Harmonic> harmonicIDToKickDrumHarmonic = null;
	public static TreeMap<Long, Harmonic> harmonicIDToHighFreqHarmonic = null;
	public static TreeMap<Long, Harmonic> harmonicIDToBassSynthHarmonic = null;
	public static TreeMap<Integer, TreeMap<Integer, Harmonic>> timeToNoteToHarmonic = null;
	public static TreeSet<Long> harmonicIDs;
	
	public static void initLoop() {
		timeToNoteToHarmonic = new TreeMap<Integer, TreeMap<Integer, Harmonic>>();
		harmonicIDs = new TreeSet<Long>();
	}
	
	public static void addBeat(int startTime, int baseNote, int[] chords, int duration, boolean useHighFreq) {
		int maxNote = HarmonicsEditor.frequencyInHzToNote(FDData.maxFrequencyInHz);
		int minNote = HarmonicsEditor.frequencyInHzToNote(40.0);
		int endTime = startTime + duration;
		int lowestNote = maxNote;
		int currentChord = 0;
		while(lowestNote >= minNote) lowestNote -= 31;
		lowestNote += 31;
		//synthInstrument(startTime, endTime, lowestNote, harmonicIDToBassSynthHarmonic, true);
		synthInstrument(startTime, endTime, baseNote, harmonicIDToInstrumentHarmonic, true);
		for(int chord: chords) {
			currentChord += chord;
			int note = baseNote + currentChord;
			synthInstrument(startTime, endTime, note, harmonicIDToInstrumentHarmonic, true);
		}
		synthInstrument(startTime, endTime, -1, harmonicIDToKickDrumHarmonic, true);
		if(useHighFreq) {
			//synthInstrument(startTime, endTime, -1, harmonicIDToHighFreqHarmonic, false);
		}
		removeDissonance(startTime);
		HarmonicsEditor.refreshView();
	}
	
	public static void synthInstrument(int startTime, int endTime, int note, TreeMap<Long, Harmonic> harmonics, boolean overwrite) {
		TreeMap<Integer, Harmonic> noteToHarmonic = new TreeMap<Integer, Harmonic>();
		if(!timeToNoteToHarmonic.containsKey(startTime)) {
			timeToNoteToHarmonic.put(startTime, new TreeMap<Integer, Harmonic>());
		}
		for(Harmonic harmonic: harmonics.values()) {
			noteToHarmonic.put(harmonic.getAverageNote(), harmonic);
		}
		int firstNote = noteToHarmonic.firstKey();
		int deltaNote = 0;
		if(note > 0) {
			deltaNote = note - firstNote;
		}
		ArrayList<Harmonic> values = new ArrayList<Harmonic>(noteToHarmonic.values());
		for(Harmonic harmonic: values) {
			long harmonicID = HarmonicsEditor.getRandomID();
			ArrayList<FDData> harmonicData = harmonic.scaledHarmonic(startTime, endTime, deltaNote, harmonicID);
			Harmonic newHarmonic = new Harmonic(harmonicID);
			for(FDData data: harmonicData) newHarmonic.addData(data);
			if(timeToNoteToHarmonic.get(startTime).containsKey(newHarmonic.getAverageNote()) && !overwrite) continue;
			timeToNoteToHarmonic.get(startTime).put(newHarmonic.getAverageNote(), newHarmonic);
			//System.out.println("synth note: " + newHarmonic.getAverageNote());
		}
	}
	
	public static void removeDissonance(int startTime) {
		int time = startTime;
		TreeSet<Integer> notesCopy = new TreeSet<Integer>();
		for(int note: timeToNoteToHarmonic.get(time).keySet()) {
			notesCopy.add(note);
		}
		int prevNote = -1;
		int currentNote = -1;
		for(int note: notesCopy) {
			prevNote = currentNote;
			currentNote = note;
			Harmonic prevHarmonic = timeToNoteToHarmonic.get(time).get(prevNote);
			Harmonic currentHarmonic = timeToNoteToHarmonic.get(time).get(currentNote);
			if((currentNote - prevNote) < 2) {
				System.out.println("dissonance");
				if(prevHarmonic == null || currentHarmonic == null) break;
				if(prevHarmonic.getMaxLogAmplitude() > currentHarmonic.getMaxLogAmplitude()) {
					System.out.println("lower");
					timeToNoteToHarmonic.get(time).remove(currentNote);
					currentNote = prevNote;
					continue;
				} else {
					System.out.println("upper");
					timeToNoteToHarmonic.get(time).remove(prevNote);
					continue;
				}
			}
		}
	}
	
	public static Harmonic copyHarmonic(Harmonic harmonic) {
		Harmonic returnVal = new Harmonic(HarmonicsEditor.getRandomID());
		try {
			for(FDData data: harmonic.getAllData()) {
				returnVal.addData(new FDData(data.getTime(), data.getNote(), data.getLogAmplitude(), returnVal.getHarmonicID()));
			}
		} catch (Exception e) {
			System.out.println("SoftSynth.copyHarmonic: Error creating data");
		}
		return returnVal;
	}
		
	public static void addDataToHarmonicsEditor() {
		for(int time: timeToNoteToHarmonic.keySet()) {
			harmonicIDs = new TreeSet<Long>();
			for(Harmonic harmonic: timeToNoteToHarmonic.get(time).values()) {
				long harmonicID = harmonic.getHarmonicID();
				if(harmonicIDs.contains(harmonicID)) {
					System.out.println("Duplicate");
					continue;
				} else {
					System.out.println("New");
				}
				harmonicIDs.add(harmonicID);
				System.out.println(harmonic.getHarmonicID());
				for(FDData data: harmonic.getAllData()) {
					HarmonicsEditor.addData(data);
				}
			}
		}
	}
	
	public static boolean checkHarmonicID(long harmonicID) {
		if(harmonicIDs.contains(harmonicID)) {
			System.out.println("Duplicate!");
			return true;
		} else {
			harmonicIDs.add(harmonicID);
			return false;
		}
	}

	public static void synthSingleNote(int startTime, int endTime, int note, double amplitude) {
		try {
			long harmonicID =  HarmonicsEditor.getRandomID();
			int attackTime = startTime + 2;
			FDData start = new FDData(startTime, note, amplitude / 2.0, harmonicID);
			FDData attack = new FDData(attackTime, note, amplitude, harmonicID);
			FDData end = new FDData(endTime, note, amplitude, harmonicID);
			HarmonicsEditor.addData(start);
			HarmonicsEditor.addData(attack);
			HarmonicsEditor.addData(end);
		} catch (Exception e) {
			System.out.println("HarmonicsEditor.synthNoteWithOvertones() error creating data:" + e.getMessage());
		}		
	}

	public static void synthNoteWithOvertones(int startTime, int endTime, int minNote, int maxNote, double maxAmplitude) {
		int note = minNote;
		try {
			while(note < maxNote) {
				double taper = (note - minNote) / (double) FDData.noteBase;
				taper = Math.pow(taper, 1.25);
				double currentAmplitude = maxAmplitude - taper;
				if(currentAmplitude < 2.0) break;
				synthSingleNote(startTime, endTime, note, currentAmplitude);
				note += 31;
			}	
		} catch (Exception e) {
			System.out.println("HarmonicsEditor.synthNoteWithOvertones() error creating data:" + e.getMessage());
		}
	}
	
	
	public static void synthFormants(int startTime, int note, double maxAmplitude) {
		try {
			double taperAmplitudeMultiple = 1.0;
			double oddHarmonicAddition = -2.0;
			double evenHarmonicAddition = -4.0;
			int prevNote = note - 3;
			int harmonic = 2;
			while((harmonic * HarmonicsEditor.noteToFrequencyInHz(note)) < FDData.maxFrequencyInHz) {
				int currentNote = HarmonicsEditor.frequencyInHzToNote(HarmonicsEditor.noteToFrequencyInHz(note) * harmonic);
				if((currentNote - 3) < prevNote) {
					harmonic++;
					continue;
				}
				long harmonicID = HarmonicsEditor.getRandomID();
				double taper = (currentNote - note) / (double) FDData.noteBase;
				taper = Math.pow(taper, 1.0);
				double amplitude = maxAmplitude - (taperAmplitudeMultiple * taper);
				if(harmonic % 2 == 1) {
					amplitude += oddHarmonicAddition;
				} else {
					amplitude += evenHarmonicAddition;
				}
				if(amplitude < 0) {
					harmonic++;
					continue;
				}
				FDData start = new FDData(startTime, currentNote, amplitude, harmonicID);
				FDData attack = new FDData(startTime + 2, currentNote, amplitude, harmonicID);
				FDData end = new FDData(startTime + 10, currentNote, 0.0, harmonicID);
				HarmonicsEditor.addData(start);
				HarmonicsEditor.addData(attack);
				HarmonicsEditor.addData(end);
				harmonic++;
				prevNote = currentNote;
			}
		} catch (Exception e) {
			System.out.println("HarmonicsEditor.synthNoteWithOvertones() error creating data:" + e.getMessage());
		}	
	}
	
	public static void addFlanking(int startTime, int endTime, int note, double maxAmplitude, double minAmplitude) {
		try {
			double startAmplitude = maxAmplitude - 4;
			double endAmplitude = minAmplitude - 4;
			if(startAmplitude < 3.0) return;
			if(endAmplitude < 0.0) endAmplitude = 0.0;
			//synthSingleNote(startTime, endTime, note + 1, startAmplitude, endAmplitude);
			//synthSingleNote(startTime, endTime, note - 1, startAmplitude, endAmplitude);
		} catch (Exception e) {
			System.out.println("HarmonicsEditor.addFlanking() error creating data:" + e.getMessage());
		}
	}
	
}