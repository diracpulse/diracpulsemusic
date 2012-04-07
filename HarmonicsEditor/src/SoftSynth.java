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
	public static TreeMap<Integer, TreeMap<Integer, Harmonic>> timeToNoteToHarmonic = null;
	public static TreeSet<Long> harmonicIDs;
	public static final double logAmplitudeLimit = 12.0;
	
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
		double maxAmplitude = 0.0;
		for(Harmonic harmonic: harmonicIDToInstrumentHarmonic.values()) {
			if(harmonic.getMaxLogAmplitude() > maxAmplitude) maxAmplitude = harmonic.getMaxLogAmplitude();
		}
		if(harmonicIDToBassSynthHarmonic != null) {
			synthInstrument(startTime, endTime, lowestNote, harmonicIDToBassSynthHarmonic, true);
		} else {
			synthNoteWithOvertones(startTime, startTime + duration, lowestNote, baseNote, maxAmplitude);
		}
		if(harmonicIDToInstrumentHarmonic != null) {
			synthInstrument(startTime, endTime, baseNote, harmonicIDToInstrumentHarmonic, true);
		} else {
			System.out.println("No Main Instrument Selected");
			return;
		}
		for(int chord: chords) {
			currentChord += chord;
			int note = baseNote + currentChord;
			// Already checked for null above
			synthInstrument(startTime, endTime, note, harmonicIDToInstrumentHarmonic, true);
		}
		if(harmonicIDToKickDrumHarmonic != null) {
			synthInstrument(startTime, endTime, -1, harmonicIDToKickDrumHarmonic, true);
		}
		if(useHighFreq) {
			if(harmonicIDToHighFreqHarmonic != null) {
				synthInstrument(startTime, endTime, -1, harmonicIDToHighFreqHarmonic, false);
				removeDissonance(startTime);
			} else {
				removeDissonance(startTime);
			}
		} else {
			if(harmonicIDToSnareHarmonic != null) {
				removeDissonance(startTime);
				synthInstrument(startTime, endTime, -1, harmonicIDToSnareHarmonic, false);
			} else {
				removeDissonance(startTime);
			}
		}
		//removeDissonance(startTime);
		HarmonicsEditor.refreshView();
	}
	
	public static void synthInstrument(int startTime, int endTime, int note, TreeMap<Long, Harmonic> harmonics, boolean overwrite) {
		if(!timeToNoteToHarmonic.containsKey(startTime)) {
			timeToNoteToHarmonic.put(startTime, new TreeMap<Integer, Harmonic>());
		}
		TreeMap<Integer, Harmonic> noteToHarmonic = new TreeMap<Integer, Harmonic>();
		for(Harmonic harmonic: harmonics.values()) {
			noteToHarmonic.put(harmonic.getAverageNote(), harmonic);
		}
		int firstNote = noteToHarmonic.firstKey();
		int deltaNote = 0;
		if(note > 0) {
			deltaNote = note - firstNote;
		}
		while(deltaNote > FDData.noteBase) deltaNote -= FDData.noteBase;
		while(deltaNote < -1 * FDData.noteBase) deltaNote += FDData.noteBase;
		ArrayList<Harmonic> values = new ArrayList<Harmonic>(noteToHarmonic.values());
		for(Harmonic harmonic: values) {
			long harmonicID = HarmonicsEditor.getRandomID();
			ArrayList<FDData> harmonicData = harmonic.scaledHarmonic(startTime, endTime, deltaNote, harmonicID);
			Harmonic newHarmonic = new Harmonic(harmonicID);
			for(FDData data: harmonicData) newHarmonic.addData(data);
			int newNote = newHarmonic.getAverageNote();
			if(timeToNoteToHarmonic.get(startTime).containsKey(newNote)) {
				Harmonic currentHarmonic = timeToNoteToHarmonic.get(startTime).get(newNote);
				Harmonic mergedHarmonic = mergeHarmonics(currentHarmonic, newHarmonic);
				timeToNoteToHarmonic.get(startTime).put(newNote, mergedHarmonic);
			} else {
				timeToNoteToHarmonic.get(startTime).put(newNote, newHarmonic);
			}
		}
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
				// timeToData1.containsKey(time) == false, so must be just data2
				returnVal.addData(timeToData2.get(time));
			}
		}
		returnVal.setAllHarmonicIDsEqual();
		return returnVal;
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
				harmonic.addCompressionWithLimiter(4.0, 14.0);
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
			FDData end = new FDData(endTime, note, amplitude -2.0, harmonicID);
			Harmonic harmonic = new Harmonic(harmonicID);
			harmonic.addData(start);
			harmonic.addData(attack);
			harmonic.addData(end);
			TreeMap<Long, Harmonic> harmonicsArg = new TreeMap<Long, Harmonic>();
			harmonicsArg.put(harmonicID, harmonic);
			synthInstrument(startTime, endTime, note, harmonicsArg, true);
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