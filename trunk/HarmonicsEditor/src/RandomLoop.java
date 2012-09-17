import java.util.Random;



public class RandomLoop {
	
	HarmonicsEditor parent;
	int numBeats = 8;
	int[] durations = {75, 100, 100, 125};
	int[] steps = {-6,-5,-4,-3,-3,-2,-2,-1,-1,-1,0,0,0,1,1,1,2,2,3,3,4,5,6};
	int numNotes = steps[steps.length - 1];
	int[] key = new int[numNotes];
	int[] deltaKey = new int[numNotes];
	int[][] allChords = new int[numNotes][2];
	int defaultDuration = 100;
	
	public RandomLoop(HarmonicsEditor parent) {
		this.parent = parent;
		HarmonicsEditor.clearCurrentData();
		Random randomGenerator = new Random();
		SoftSynth.initLoop(numBeats, defaultDuration);
		int minNote = HarmonicsEditor.frequencyInHzToNote(256.0);
		int centerNote = minNote + FDData.noteBase / 2;
		int maxNote = minNote + FDData.noteBase - 1;
		int note = -1;
		int lastNote = maxNote;
		while(lastNote > maxNote - 3) {
			note = minNote;
			for(int keyIndex = 0; keyIndex < numNotes; keyIndex++) {
				deltaKey[keyIndex] = randomGenerator.nextInt(3) + 3;
				note += deltaKey[keyIndex];
				key[keyIndex] = note;
			}
			lastNote = key[numNotes - 1];
		}
		for(int keyIndex = 0; keyIndex < numNotes; keyIndex++) {
			int randKeyIndex = keyIndex; // randomGenerator.nextInt(numNotes);
			allChords[keyIndex][0] = deltaKey[randKeyIndex] + deltaKey[(randKeyIndex + 1) % numNotes];
			allChords[keyIndex][1] = deltaKey[(randKeyIndex + 2) % numNotes] + deltaKey[(randKeyIndex + 3) % numNotes];
		}
		for(int keyIndex = 0; keyIndex < numNotes; keyIndex++) {
			allChords[keyIndex][0] = randomGenerator.nextInt(6) + 7;
			allChords[keyIndex][1] = randomGenerator.nextInt(6) + 7;
		}
		int currentNoteIndex = numNotes / 2;
		for(int beat = 0; beat < numBeats; beat++) {
			note = key[currentNoteIndex];
			int[] chords = allChords[currentNoteIndex];
			SoftSynth.modifyBeat(beat, note, chords, durations[randomGenerator.nextInt(durations.length)]);
			currentNoteIndex += steps[randomGenerator.nextInt(steps.length)];
			if(currentNoteIndex < 0) {
				currentNoteIndex = numNotes - 1 - Math.abs(steps[randomGenerator.nextInt(steps.length)]) / 2;
			}
			if(currentNoteIndex >= numNotes) {
				currentNoteIndex = Math.abs(steps[randomGenerator.nextInt(steps.length)]) / 2;
			}
		}
	}
	
	public void playRandomLoop() {
		SoftSynth.addDataToHarmonicsEditor();
		HarmonicsEditor.playSelectedDataInCurrentWindow(parent);
	}
	
}