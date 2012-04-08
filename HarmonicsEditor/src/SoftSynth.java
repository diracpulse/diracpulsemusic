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
	public static TreeMap<Long, Harmonic> harmonicIDToHarmonic = null;
	public static TreeSet<Long> harmonicIDs;
	public static final double logAmplitudeLimit = 12.0;
	
	public static void initLoop() {
		harmonicIDToHarmonic = new TreeMap<Long, Harmonic>();
		harmonicIDs = new TreeSet<Long>();
	}
	
	public static void addBeat(int startTime, int baseNote, int[] chords, int duration, boolean useHighFreq) {
		int maxNote = HarmonicsEditor.frequencyInHzToNote(FDData.maxFrequencyInHz);
		int minNote = HarmonicsEditor.frequencyInHzToNote(40.0);
		int endTime = startTime + duration - 1;
		int lowestNote = maxNote;
		int currentChord = 0;
		while(lowestNote >= minNote) lowestNote -= 31;
		lowestNote += 31;
		double maxLogAmplitude = 0.0;
		// Synth Main Instrument
		if(harmonicIDToInstrumentHarmonic != null) {
			synthInstrument(startTime, endTime, baseNote, harmonicIDToInstrumentHarmonic, true);
			for(int chord: chords) {
				currentChord += chord;
				int note = baseNote + currentChord;
				synthInstrument(startTime, endTime, note, harmonicIDToInstrumentHarmonic, true);
			}
			// find Max Amplitude for bass synth (otherwise 0)
			for(Harmonic harmonic: harmonicIDToInstrumentHarmonic.values()) {
				if(harmonic.getMaxLogAmplitude() > maxLogAmplitude) maxLogAmplitude = harmonic.getMaxLogAmplitude();
			}
		}
		// Synth Bass Instrument
		if(harmonicIDToBassSynthHarmonic != null) {
			synthInstrument(startTime, endTime, lowestNote, harmonicIDToBassSynthHarmonic, true);
		} else {
			synthNoteWithOvertones(startTime, endTime, lowestNote, baseNote, 16.0);
		}
		// Synth Noise Sources
		if(harmonicIDToKickDrumHarmonic != null) {
			synthInstrument(startTime, endTime, -1, harmonicIDToKickDrumHarmonic, true);
		}
		if(useHighFreq) {
			if(harmonicIDToHighFreqHarmonic != null) {
				synthInstrument(startTime, endTime, -1, harmonicIDToHighFreqHarmonic, false);
			}
		} else {
			if(harmonicIDToSnareHarmonic != null) {
				synthInstrument(startTime, endTime, -1, harmonicIDToSnareHarmonic, false);
			}
		}
		//fitHarmonicsToChords(startTime, endTime, lowestNote, baseNote, chords);
		//removeDissonance(startTime, endTime);
		HarmonicsEditor.refreshView();
	}
	
	public static void synthInstrument(int startTime, int endTime, int note, TreeMap<Long, Harmonic> harmonics, boolean overwrite) {
		TreeMap<Integer, Harmonic> noteToHarmonic = new TreeMap<Integer, Harmonic>();
		// Merge any harmonics with the same note in the instrument
		for(Harmonic harmonic: harmonics.values()) {
			if(!noteToHarmonic.containsKey(harmonic.getAverageNote())) {
				noteToHarmonic.put(harmonic.getAverageNote(), harmonic);
			} else {
				Harmonic h1 = noteToHarmonic.get(harmonic.getAverageNote());
				Harmonic newHarmonic = mergeHarmonics(h1, harmonic);
				noteToHarmonic.put(harmonic.getAverageNote(), newHarmonic);
			}
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
			harmonicIDToHarmonic.put(harmonicID, newHarmonic);
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
				if(timeToData2.containsKey(time)) {
					returnVal.addData(timeToData2.get(time));
				} else {
					//returnVal.addData(getData(time, note, 0.0, harmonicID));
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
			System.out.println("SoftSynth.mergeHarmonics: Error creating data");
		}
		return returnVal;
	}
	

	public static void addDataToHarmonicsEditor() {
		harmonicIDs = new TreeSet<Long>();
		for(Harmonic harmonic: harmonicIDToHarmonic.values()) {
			harmonic.addCompressionWithLimiter(4.0, 14.0);
			long harmonicID = harmonic.getHarmonicID();
			if(harmonicIDs.contains(harmonicID)) {
				System.out.println("Duplicate");
				continue;
			}
			harmonicIDs.add(harmonicID);
			for(FDData data: harmonic.getAllData()) {
				HarmonicsEditor.addData(data);
			}
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
			synthInstrument(0, endTime, note, harmonicsArg, true);
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

}