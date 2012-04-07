import java.util.ArrayList;

import javax.swing.JOptionPane;

public class Loop {
	
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
	
	public static Integer getRating(HarmonicsEditor parent, String loopDescriptor) {
		Object[] ratings = {5, 4, 3, 2, 1, 0};
		return (Integer) JOptionPane.showInputDialog(parent, loopDescriptor, "Select Rating",
													 JOptionPane.PLAIN_MESSAGE, null, ratings, 2);
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
		SoftSynth.addBeat(0, baseNote, HarmonicsEditor.getChord(chord1), duration, false);
		SoftSynth.addBeat(duration, note2, HarmonicsEditor.getChord(chord2), duration, true);
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
			String fileString = loopDescriptor.toString() + getRating(parent, loopDescriptor.toString()); 
			HarmonicsFileOutput.OutputStringToFile(fileName, fileString + "\n");
		}
	}
	
	public static void synthTriplet(int chord1, int chord2, int chord3, int deltaNote1, int deltaNote2) {
		HarmonicsEditor.clearCurrentData();
		SoftSynth.initLoop();
		int duration = 75;
		int note1 = HarmonicsEditor.frequencyInHzToNote(440.0) + HarmonicsEditor.randomGenerator.nextInt(12) - 6;
		int note2 = note1 + deltaNote1;
		int note3 = note2 + deltaNote2;
		SoftSynth.addBeat(0, note1, HarmonicsEditor.getChord(chord1), duration, false);
		SoftSynth.addBeat(duration, note2, HarmonicsEditor.getChord(chord2), duration, true);
		SoftSynth.addBeat(duration * 2, note3, HarmonicsEditor.getChord(chord3), duration, false);
	}
	
	
	public static void randomQuad(HarmonicsEditor parent) {
		int arraySize = 7;
		int numArrays = 0;
		String fileName = "Quad" + System.currentTimeMillis() + ".txt";
		NestedHashMap ntm = new NestedHashMap();
		for(int chord1 = 0; chord1 < 10; chord1 += 2) {
			for(int chord2 = 1; chord2 < 10; chord2 += 2) {
				for(int chord3 = 0; chord3 < 10; chord3 += 2) {
					for(int chord4 = 1; chord4 < 10; chord4 += 2) {
						for(int deltaNote1 = -10; deltaNote1 <= 10; deltaNote1++) {
							for(int deltaNote2 = -10; deltaNote2 <= 10; deltaNote2++) {
								for(int deltaNote3 = -10; deltaNote3 <= 10; deltaNote3++) {
									int[] array = new int[]{chord1,chord2,chord3,chord4,deltaNote1,deltaNote2,deltaNote3};
									ntm.addArray(array);
									numArrays++;
								}
							}
						}
					}
				}
			}
		}
		int loopIndex = 0;
		while(loopIndex < numArrays) {
			ArrayList<Integer> randomQuad = ntm.getRandomArray();
			if(randomQuad.size() < arraySize) continue;
			loopIndex++;
			int chord1 = randomQuad.get(0);
			int chord2 = randomQuad.get(1);
			int chord3 = randomQuad.get(2);
			int chord4 = randomQuad.get(3);
			int deltaNote1 = randomQuad.get(4);
			int deltaNote2 = randomQuad.get(5);
			int deltaNote3 = randomQuad.get(6);
			StringBuffer loopDescriptor = new StringBuffer();
			for(int index = 0; index < arraySize; index++) loopDescriptor.append(randomQuad.get(index) + " ");
			System.out.println(loopDescriptor);
			synthQuad(chord1, chord2, chord3, chord4, deltaNote1, deltaNote2, deltaNote3);
			SoftSynth.addDataToHarmonicsEditor();
			HarmonicsEditor.playSelectedDataInCurrentWindow(parent);
			int choice = JOptionPane.showConfirmDialog(parent, loopDescriptor);
			switch (choice) {
				case JOptionPane.YES_OPTION:
					HarmonicsFileOutput.OutputStringToFile(fileName, loopDescriptor.toString() + "Y\n");
					break;
				case JOptionPane.NO_OPTION:
					HarmonicsFileOutput.OutputStringToFile(fileName, loopDescriptor.toString() + "N\n");
					break;
				case JOptionPane.CANCEL_OPTION:
					HarmonicsFileOutput.OutputStringToFile(fileName, loopDescriptor.toString() + "C\n");
					HarmonicsEditor.playSelectedDataInCurrentWindow(parent);
					break;
			}
		}
	}
	
	public static void synthQuad(int chord1, int chord2, int chord3, int chord4, 
								 int deltaNote1, int deltaNote2, int deltaNote3) {
		HarmonicsEditor.clearCurrentData();
		SoftSynth.initLoop();
		int duration = 75;
		int note1 = HarmonicsEditor.frequencyInHzToNote(440.0); // + randomGenerator.nextInt(12) - 6;
		int note2 = note1 + HarmonicsEditor.getNote(deltaNote1);
		int note3 = note2 + HarmonicsEditor.getNote(deltaNote2);
		int note4 = note3 + HarmonicsEditor.getNote(deltaNote3);
		SoftSynth.addBeat(0, note1, HarmonicsEditor.getChord(chord1), duration, false);
		SoftSynth.addBeat(duration, note2, HarmonicsEditor.getChord(chord2), duration, true);
		SoftSynth.addBeat(duration * 2, note3, HarmonicsEditor.getChord(chord3), duration, false);
		SoftSynth.addBeat(duration * 3, note4, HarmonicsEditor.getChord(chord4), duration, true);
	}
	
}
