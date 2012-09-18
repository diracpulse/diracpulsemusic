import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;



public class RandomLoop {
	
	HarmonicsEditor parent;
	int numBeats = 8;
	int[] durations = {100, 125, 150};
	int[] steps = {0,0,1,1,1,1,2,2,2,3,3,4};
	int direction = 1;
	int numNotes = 7;
	int[] key = new int[numNotes];
	int[] deltaKey = new int[numNotes];
	int[][] allChords = new int[numNotes][2];
	TreeMap<Integer, Integer> beatToNoteIndex = null;
	TreeMap<Integer, Integer> beatToDuration = null;
	int defaultDuration = 100;
	public ArrayList<Beat> beatArray = null;
	Random randomGenerator = new Random();
	
	
	private void initLoop(int numBeats, int duration) {
		beatArray = new ArrayList<Beat>();
		for(int beat = 0; beat < numBeats; beat++) {
			beatArray.add(new Beat(-1, null, duration));
		}
	}
	
	public RandomLoop(HarmonicsEditor parent) {
		this.parent = parent;
		HarmonicsEditor.clearCurrentData();
		initLoop(numBeats, defaultDuration);
		beatToNoteIndex = new TreeMap<Integer, Integer>();
		beatToDuration = new TreeMap<Integer, Integer>();
		int minNote = HarmonicsEditor.frequencyInHzToNote(256.0);
		int centerNote = minNote + FDData.noteBase / 2;
		int maxNote = minNote + FDData.noteBase - 1;
		int note = -1;
		int lastNote = maxNote;
		while(lastNote > maxNote - 3) {
			note = minNote;
			for(int keyIndex = 0; keyIndex < numNotes; keyIndex++) {
				key[keyIndex] = note;
				note += randomGenerator.nextInt(3) + 3;
			}
			lastNote = key[numNotes - 1];
		}
		for(int keyIndex = 0; keyIndex < numNotes; keyIndex++) {
			allChords[keyIndex][0] = randomGenerator.nextInt(6) + 7;
			allChords[keyIndex][1] = randomGenerator.nextInt(6) + 7;
		}
		int currentNoteIndex = numNotes / 2;
		for(int beat = 0; beat < numBeats; beat++) {
			note = key[currentNoteIndex];
			int[] chords = allChords[currentNoteIndex];
			int duration = durations[randomGenerator.nextInt(durations.length)];
			modifyBeat(beat, note, chords, duration);
			beatToNoteIndex.put(beat, currentNoteIndex);
			beatToDuration.put(beat, duration);
			direction = newDirection();
			currentNoteIndex += steps[randomGenerator.nextInt(steps.length)] * direction;
			if(currentNoteIndex < 0) {
				currentNoteIndex = Math.abs(steps[randomGenerator.nextInt(steps.length)]) / 2;
				direction = 1;
			}
			if(currentNoteIndex >= numNotes) {
				currentNoteIndex = numNotes - Math.abs(steps[randomGenerator.nextInt(steps.length)]) / 2 - 1;
				direction = -1;
			}
		}
	}

	public int newDirection() {
		int randVal = randomGenerator.nextInt(4);
		if(direction == 1) {
			if(randVal < 3) return 1;
			return -1;
		}
		if(direction == -1) {
			if(randVal < 3) return -1;
			return 1;
		}
		return 0;
	}
	
	private void modifyBeat(int beatIndex, int baseNote, int[] chords, int duration) {
		beatArray.get(beatIndex).baseNote = baseNote;
		beatArray.get(beatIndex).chords = chords;
		beatArray.get(beatIndex).duration = duration;
	}
	
	public void playScale() {
		initLoop(numNotes + 1, defaultDuration);
		for(int noteIndex = 0; noteIndex < numNotes; noteIndex++) {
			int note = key[noteIndex];
			int[] chords = allChords[noteIndex];
			modifyBeat(noteIndex, note, chords, defaultDuration);
		}
		modifyBeat(numNotes, key[0] + FDData.noteBase, allChords[0], defaultDuration);
		SoftSynth.synthLoopInHarmonicsEditor(beatArray);
		HarmonicsEditor.playSelectedDataInCurrentWindow(parent);
	}
	
	public void playRandomLoop() {
		initLoop(numBeats, defaultDuration);
		for(int beatIndex = 0; beatIndex < numBeats; beatIndex++) {
			int noteIndex = beatToNoteIndex.get(beatIndex);
			int note = key[noteIndex];
			int[] chords = allChords[noteIndex];
			int duration = beatToDuration.get(beatIndex);
			modifyBeat(beatIndex, note, chords, duration);
		}
		SoftSynth.synthLoopInHarmonicsEditor(beatArray);
		HarmonicsEditor.playSelectedDataInCurrentWindow(parent);
	}
	
	public void handleBeatSelected(int beat) {
		System.out.println("Beat Selected = " + beat);
		if(beat == -1) return; // out of bounds or beatArray==null
		beatArray.get(beat).modifyBaseNote = !beatArray.get(beat).modifyBaseNote;
		beatArray.get(beat).modifyChords = !beatArray.get(beat).modifyChords;
		beatArray.get(beat).modifyDuration = !beatArray.get(beat).modifyDuration;
		HarmonicsEditor.view.repaint();
	}
	
}
