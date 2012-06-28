import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JOptionPane;
import javax.swing.Timer;

public class Loop implements ActionListener {
	
	public static class Beat {
		
		int baseNote;
		int[] chord;
		int duration;
		
		public Beat(int baseNote, int[] chord, int duration) {
			this.baseNote = baseNote;
			this.chord = new int[chord.length];
			for(int index = 0; index < chord.length; index++) this.chord[index] = chord[index];
			this.duration = duration;
		}
		
		public int getBaseNote() {
			return baseNote;
		}
		
		public int[] getChord() {
			return chord;
		}
		
		public int getDuration() {
			return duration; 
		}
		
		public String toString() {
			StringBuffer returnVal = new StringBuffer();
			returnVal.append(baseNote);
			for(int chordVal: chord) returnVal.append(":" + chordVal);
			returnVal.append(":" + duration);
			return returnVal.toString();
		}
	}
	
	public static String getLoopDataString(ArrayList<Beat> loop) {
		if(loop == null) return null;
		if(loop.isEmpty()) return "";
		StringBuffer returnVal = new StringBuffer();
		returnVal.append(loop.get(0));
		if(loop.size() == 1) return returnVal.toString() + "\n";
		for(int index = 1; index < loop.size(); index++) {
			returnVal.append("|" + loop.get(index));
		}
		return returnVal.toString() + "\n";
	}
	
	public static void randomChord(HarmonicsEditor parent) {
		ArrayList<ArrayList<Integer>> allChords = new ArrayList<ArrayList<Integer>>();
		String fileName = "Chord" + System.currentTimeMillis() + ".txt";
		for(int baseNote = 8 * 31; baseNote <= 9 * 31; baseNote += 6) {
			for(int chord1 = 6; chord1 < 13; chord1 += 1) {
				for(int chord2 = 6; chord2 < 13; chord2 += 1) {
					ArrayList<Integer> chord = new ArrayList<Integer>();
					chord.add(baseNote);
					chord.add(chord1);
					chord.add(chord2);
					allChords.add(chord);
				}
			}
		}
		Random random = new Random();
		while(!allChords.isEmpty()) {
			int numChords = allChords.size();
			int randomIndex = random.nextInt(numChords);
			ArrayList<Integer> currentChord = allChords.get(randomIndex);
			int baseNote = currentChord.get(0);
			int chord1 = currentChord.get(1);
			int chord2 = currentChord.get(2);
			String loopDescriptor = baseNote + " " + chord1 + " " + chord2;
			synthChord(baseNote, chord1, chord2);
			SoftSynth.addDataToHarmonicsEditor();
			HarmonicsEditor.playSelectedDataInCurrentWindow(parent);
			Integer result = getRating(parent, loopDescriptor);
			if(result != null) {
				System.out.println(result);
				String fileString = loopDescriptor + " " + result;
				HarmonicsFileOutput.OutputStringToFile(fileName, fileString + "\n");
			}
			allChords.remove(randomIndex);
		}
	}
	
	public static void synthChord(int baseNote, int chord1, int chord2) {
		HarmonicsEditor.clearCurrentData();
		SoftSynth.initLoop();
		int duration = 100;
		int[] chord = {chord1, chord2};
		SoftSynth.addBeat(0, baseNote, chord, duration, false);
		SoftSynth.addBeat(duration, baseNote, chord, duration, false);
	}

	public static Integer getRating(HarmonicsEditor parent, String loopDescriptor) {
		Object[] ratings = {5, 4, 3, 2, 1, 0};
		return (Integer) JOptionPane.showInputDialog(parent, loopDescriptor, "Select Rating",
													 JOptionPane.PLAIN_MESSAGE, null, ratings, 2);
	}
	
	public static void nonRandomDoublet(HarmonicsEditor parent) {
		String fileName = "Doublet" + System.currentTimeMillis() + ".txt";
			for(int deltaNote1 = -15; deltaNote1 <= 15; deltaNote1++) {
				for(int deltaNote2 = -15; deltaNote2 <= 15; deltaNote2++) {
					for(int chord1 = 0; chord1 < 10; chord1 += 1) {
						for(int chord2 = 0; chord2 < 10; chord2 += 1) {
						synthDoublet(chord1, chord2, deltaNote1, deltaNote2);
						String loopDescriptor = "CCNNR1-5" + " " + chord1 + " " + chord2 + " " + " " + deltaNote1 + " " + deltaNote2; 
						SoftSynth.addDataToHarmonicsEditor();
						HarmonicsEditor.playSelectedDataInCurrentWindow(parent);
						String fileString = loopDescriptor + " " + getRating(parent, loopDescriptor); 
						HarmonicsFileOutput.OutputStringToFile(fileName, fileString + "\n");
					}
				}
			}
		}
	}
	
	public static void randomDoublet(HarmonicsEditor parent) {
		int arraySize = 3;
		int numArrays = 0;
		String fileName = "Doublets.txt";
		NestedHashMap nhm = new NestedHashMap();
		for(int chord1 = 0; chord1 < 10; chord1 += 1) {
			for(int chord2 = 0; chord2 < 10; chord2 += 1) {
				for(int deltaNote1 = -15; deltaNote1 <= 15; deltaNote1++) {
					int[] array = new int[]{chord1,chord2,deltaNote1};
					nhm.addArray(array);
					numArrays++;
				}
			}
		}
		int currentRatedArrays = 0;
		ArrayList<String> recordedDoublets = HarmonicsFileInput.ReadTextFileData(fileName);
		for(String line: recordedDoublets) {
			if(line == null) break;
			if(line.isEmpty()) break;
			String[] data = line.split(" ");
			String dataFormat = data[0];
			int baseNote = new Integer(data[1]);
			int chord1 = new Integer(data[2]);
			int chord2 = new Integer(data[3]);
			int deltaNote1 = new Integer(data[4]);
			int rating = new Integer(data[5]);
			int[] array = new int[]{chord1,chord2,deltaNote1};
			nhm.removeArray(array);
			currentRatedArrays++;
		}
		int loopIndex = 0;
		while(loopIndex < numArrays) {
			ArrayList<Integer> randomDoublet = nhm.getRandomArray();
			if(randomDoublet.size() < arraySize) continue;
			loopIndex++;
			int chord1 = randomDoublet.get(0);
			int chord2 = randomDoublet.get(1);
			int deltaNote1 = randomDoublet.get(2);
			StringBuffer loopDescriptor = null;
			boolean playDoublet = true;
			int rating = 0;
			while(playDoublet) {
				int baseNote = HarmonicsEditor.frequencyInHzToNote(330.0);
				int deltaAdjust = (int) Math.abs(deltaNote1);
				baseNote += -1 * HarmonicsEditor.randomGenerator.nextInt(16 - deltaAdjust);
				loopDescriptor = new StringBuffer();
				loopDescriptor.append(baseNote + " ");
				for(int index = 0; index < arraySize; index++) loopDescriptor.append(randomDoublet.get(index) + " ");
				String displayString = "Doublet " + currentRatedArrays + " of " + numArrays + "\n" + loopDescriptor;
				synthDoublet(baseNote, chord1, chord2, deltaNote1);
				SoftSynth.addDataToHarmonicsEditor();
				HarmonicsEditor.playSelectedDataInCurrentWindow(parent);
				Integer ratingInteger = getRating(parent, displayString.toString());
				if(ratingInteger == null) return;
				rating = ratingInteger;
				if(rating != 0) playDoublet = false;
			}
			// space between loopDescriptor and rating added above
			String fileString = "BASENOTE|C1|C2|DN1|R " + loopDescriptor + rating;
			HarmonicsFileOutput.OutputStringToFile(fileName, fileString + "\n");
			currentRatedArrays++;
		}
	}
	
	public static void synthDoublet(int baseNote, int chord1, int chord2, int deltaNote1) {
		HarmonicsEditor.clearCurrentData();
		SoftSynth.initLoop();
		int duration = 75;
		int note2 = baseNote + deltaNote1;
		SoftSynth.addBeat(0, baseNote, getChord(chord1), duration, false);
		SoftSynth.addBeat(duration, note2, getChord(chord2), duration, true);
	}
	
	public static void randomTriplet(HarmonicsEditor parent) {
		int arraySize = 5;
		int numArrays = 0;
		String fileName = "Triplet" + System.currentTimeMillis() + ".txt";
		NestedHashMap nhm = new NestedHashMap();
		for(int chord1 = 0; chord1 < 10; chord1 += 1) {
			for(int chord2 = 0; chord2 < 10; chord2 += 1) {
				for(int chord3 = 0; chord3 < 10; chord3++) {
					for(int deltaNote1 = -15; deltaNote1 <= 15; deltaNote1++) {
						for(int deltaNote2 = -15; deltaNote2 <= 15; deltaNote2++) {
							int[] array = new int[]{chord1,chord2,chord3,deltaNote1,deltaNote2};
							nhm.addArray(array);
							numArrays++;
						}
					}
				}
			}
		}
		int loopIndex = 0;
		while(loopIndex < numArrays) {
			ArrayList<Integer> randomTriplet = nhm.getRandomArray();
			if(randomTriplet.size() < arraySize) continue;
			loopIndex++;
			int chord1 = randomTriplet.get(0);
			int chord2 = randomTriplet.get(1);
			int chord3 = randomTriplet.get(2);
			int deltaNote1 = randomTriplet.get(3);
			int deltaNote2 = randomTriplet.get(4);
			StringBuffer loopDescriptor = new StringBuffer();
			for(int index = 0; index < arraySize; index++) loopDescriptor.append(randomTriplet.get(index) + " ");
			System.out.println(loopDescriptor);
			synthTriplet(chord1, chord2, chord3, deltaNote1, deltaNote2);
			SoftSynth.addDataToHarmonicsEditor();
			HarmonicsEditor.playSelectedDataInCurrentWindow(parent);
			String fileString = loopDescriptor.toString();
			Integer rating = getRating(parent, loopDescriptor.toString());
			if(rating == null) return;
			HarmonicsFileOutput.OutputStringToFile(fileName, fileString + " " + rating + "\n");
		}
	}
	
	public static void synthTriplet(int chord1, int chord2, int chord3, int deltaNote1, int deltaNote2) {
		HarmonicsEditor.clearCurrentData();
		SoftSynth.initLoop();
		int duration = 75;
		int note1 = HarmonicsEditor.frequencyInHzToNote(440.0) + HarmonicsEditor.randomGenerator.nextInt(12) - 6;
		int note2 = note1 + deltaNote1;
		int note3 = note2 + deltaNote2;
		SoftSynth.addBeat(0, note1, getChord(chord1), duration, false);
		SoftSynth.addBeat(duration, note2, getChord(chord2), duration, true);
		SoftSynth.addBeat(duration * 2, note3, getChord(chord3), duration, false);
	}
	
	public static void synthRandomLoopRepeat(HarmonicsEditor parent) {
		Integer numBeatsInteger = getNumBeats(parent);
		if(numBeatsInteger == null) return;
		numBeats = numBeatsInteger;
		if(numBeats == 1) {
			initRandomBeats();
			synthRandomBeat();
			new Loop(parent);
			return;
		}
		synthRandomLoop(numBeats);
		new Loop(parent);
	}
	
	public static void synthRandomBeat() {
		if(beatList.size() == 0) {
			timer.stop();
			return;
		}
		ArrayList<Beat> loop = new ArrayList<Beat>();
		int randomBeatIndex = HarmonicsEditor.getRandomInt(beatList.size());
		Beat beat = beatList.get(randomBeatIndex);
		beatList.remove(randomBeatIndex);
		System.out.print(getLoopDataString(loop));
		HarmonicsEditor.clearCurrentData();
		SoftSynth.initLoop();
		SoftSynth.addBeat(0, beat.getBaseNote(), beat.getChord(), beat.getDuration(), false);
		SoftSynth.addDataToHarmonicsEditor();
	}
	
	public static void initRandomBeats() {
		beatList = new ArrayList<Beat>();
		for(int chord = 0; chord < 10; chord += 1) {
			for(int note = 0; note <= FDData.noteBase; note++) {
				int baseNote = HarmonicsEditor.frequencyInHzToNote(256.0) + note;
				Beat currentBeat = new Beat(baseNote, getChord(chord), 100);
				beatList.add(currentBeat);
			}
		}
	}
	
	public static Integer getNumBeats(HarmonicsEditor parent) {
		Object[] numLoops = {4, 3, 2, 1};
		return (Integer) JOptionPane.showInputDialog(parent, null, "Select Number Of Beats",
													 JOptionPane.PLAIN_MESSAGE, null, numLoops, 4);
	}
	
	public static void synthRandomLoop(int numBeats) {
		ArrayList<Beat> loop = new ArrayList<Beat>();
		ArrayList<Integer> deltaNotes = new ArrayList<Integer>();
		ArrayList<Integer> chordIndices = new ArrayList<Integer>();
		int duration = 100;
		int centerNote = HarmonicsEditor.frequencyInHzToNote(256.0);
		for(int index = 0; index < numBeats; index++) {
			deltaNotes.add(HarmonicsEditor.getRandomInt(31));
			chordIndices.add(HarmonicsEditor.getRandomInt(10));
		}
		for(int deltaBN = 0; deltaBN < 1; deltaBN += 10) {
			for(int beat = 0; beat < numBeats; beat++) {
				int baseNote = centerNote + deltaBN + deltaNotes.get(beat);
				int chordIndex = chordIndices.get(beat);
				int[] chords = getChord(chordIndex);
				loop.add(new Beat(baseNote, chords, duration));
			}
			loop.add(new Beat(0, getChord(0), duration));
		}
		System.out.print(getLoopDataString(loop));
		synthLoop(loop);
		SoftSynth.addDataToHarmonicsEditor();
	}

	public static void synthLoop(ArrayList<Loop.Beat> loop) {
		HarmonicsEditor.clearCurrentData();
		SoftSynth.initLoop();
		int currentTime = 0;
		for(Loop.Beat beat: loop) {
			if(beat.getBaseNote() > 0) {
				SoftSynth.addBeat(currentTime, beat.getBaseNote(), beat.getChord(), beat.getDuration(), false);
			}
			currentTime += beat.getDuration();
		}
	}
	
	public static int[] getChord(int index) {
		switch(index) {
			case 0:
				return new int[] {6, 7, 8};
			case 1:
				return new int[] {7, 6, 8};
			case 2:
				return new int[] {6, 7, 10};
			case 3:
				return new int[] {7, 6, 10};
			case 4:
				return new int[] {8, 10, 6};
			case 5:
				return new int[] {8, 10, 7};
			case 6:
				return new int[] {10, 8, 6};
			case 7:
				return new int[] {10, 8, 7};
			case 8:
				return new int[] {13, 10};
			case 9:
				return new int[] {10, 13};
		}
		return null;
	}
	
	public static int getDiatonic(int index) {
		switch(index) {
			case 0:
				return 0;
			case 1:
				return 3;
			case 2:
				return 6;
			case 3:
				return 8;
			case 4:
				return 10;
			case 5:
				return 13;
			case 6:
				return 15;
			case 7:
				return 18;
			case 8:
				return 21;
			case 9:
				return 23;
			case 10:
				return 25;
			case 11:
				return 28;
		}
		return 31;
	}
	
	static int numBeats = 4;
	static Timer timer = null;
	static HarmonicsEditor parentHE;
	static boolean play = false;
	static ArrayList<Beat> beatList = null;
	
	Loop(HarmonicsEditor parent) {
		play = true;
		parentHE = parent;
		timer = new Timer((5000 * numBeats), this);
        timer.setInitialDelay(0);
        timer.start();
	}

	public void actionPerformed(ActionEvent e) {
		if(play) {
			//System.out.println("Timer1");
			SynthTools.createPCMData(parentHE);
			SynthTools.playWindow();
			play = false;
		}
		if(numBeats == 1) {
			synthRandomBeat();
		} else {
			synthRandomLoop(numBeats);
		}
		play = true;
	}
	
	
}
