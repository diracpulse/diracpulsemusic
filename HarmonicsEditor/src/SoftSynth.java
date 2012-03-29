
public class SoftSynth {
	
	public static void addBeat(int startTime, int baseNote, int[] chords, int duration) {
		int maxNote = HarmonicsEditor.frequencyInHzToNote(FDData.maxFrequencyInHz);
		int minNote = HarmonicsEditor.frequencyInHzToNote(40.0);
		int endTime = startTime + duration;
		int currentChord = 0;
		int lowestNote = baseNote;
		while(lowestNote >= minNote) lowestNote -= 31;
		lowestNote += 31;
		double amplitude = 14.0;
		synthNoteWithOvertones(startTime, endTime, lowestNote, baseNote, amplitude);
		synthNoteWithOvertones(startTime, endTime, baseNote, maxNote, amplitude);
		for(int chord: chords) {
			currentChord += chord;
			int note = baseNote + currentChord;
			synthNoteWithOvertones(startTime, endTime, note, maxNote, amplitude);
		}
		HarmonicsEditor.refreshView();
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