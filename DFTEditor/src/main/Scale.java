package main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import main.Module.Connector;
import main.modules.MasterInput;


public class Scale {
	
	public static final int[] pythagorian53 = {0, 1, 4, 5, 9, 13, 14, 18, 22, 26, 27, 31, 35, 36, 40, 44, 45, 48, 49};
	public static final double[] justIntonation = {1.0, 17.0 / 16.0, 9.0 / 8.0, 6.0 / 5.0, 5.0 / 4.0, 4.0 / 3.0, 11.0 / 8.0, 3.0 / 2.0, 13.0 / 8.0, 27.0 / 16.0, 7.0 / 4.0, 15.0 / 8.0};
	public static final int[] majorScale = {0, 2, 4, 5, 7, 9, 11};
	public static final int[] minorScale = {0, 2, 3, 5, 7, 8, 10};
	public static final int[] ascendingMinorScale = {0, 2, 3, 5, 7, 9, 11};
	public static final int[] harmonicMinorScale = {0, 2, 3, 5, 7, 8, 11};
	public static final int[] majorTriad = {4, 7};
	public static final int[] minorTriad = {3, 7};
	public static final int[] diminsihedTriad = {3, 6};
	public static final int[] augmentedTriad = {4, 8};
	//public static final double[] allJustIntonation = {1.0, 9.0 / 8.0, 6.0 / 5.0, 5.0 / 4.0, 4.0 / 3.0, 3.0 / 2.0, 5.0 / 3.0, 8.0 / 5.0, 9.0 / 5.0, 15.0 / 8.0};

	private JPanel parent = null;
	private Random random = null;
	private int length;
	private ArrayList<ArrayList<Integer>> prevSequence = null;
	private BufferedWriter writer = null;
	private NestedTreeMap scoring;
	
	public Scale(JPanel parent, int length) {
		this.parent = parent;
		this.length = length;
		this.random = new Random();
		scoring = NestedTreeMap.createProgressionTree(length);
		Minor.loadLogFile(scoring);
	}
	
	private void initLogFile() {
		try {
			writer = new BufferedWriter(new FileWriter("CHORDS", true));
			//writer.write("0 NEW SESSION");
			//writer.newLine();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(parent, "Scale: There was a problem writing to the log file");
			return;
		}
	}

	public void closeLogFile() {
		try {
			writer.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(parent, "Scale: There was a problem writing to the log file");
			return;
		}
	}
	
	public static double noteToFreqRatio(int note) {
		int index = note;
		while(index < 0) index += 12;
		return justIntonation[index % 12] * Math.pow(2.0, Math.floor(note / 12.0));
	}
	
	public static double getInterval(int deltaNote) {
		return justIntonation[deltaNote % 12] * Math.pow(2.0, Math.floor(deltaNote / 12.0));
	}
	
	public static ArrayList<Double> chordToFreqRatios(ArrayList<Integer> notes) {
		ArrayList<Double> returnVal = new ArrayList<Double>();
		double ratio0 = noteToFreqRatio(notes.get(0));
		returnVal.add(ratio0);
		int interval0 = notes.get(1) - notes.get(0);
		double ratio1 = ratio0 * getInterval(interval0);
		returnVal.add(ratio1);
		int interval2 = notes.get(2) - notes.get(1);
		double ratio2 = ratio1 * getInterval(interval2);
		returnVal.add(ratio2);
		return returnVal;
	}

	public ArrayList<ArrayList<Integer>> getNextChord(float prevRating) {
		if(prevSequence != null) Minor.writeToLogFile(prevSequence, prevRating, parent);
		ArrayList<ArrayList<Integer>> returnVal;
		returnVal = scoring.getUnscoredSequence();
		prevSequence = returnVal;
		return returnVal; // clone(returnVal);
	}
	
	public ArrayList<ArrayList<Integer>> clone(ArrayList<ArrayList<Integer>> input) {
		int inputLength = input.size();
		for(int index = 0; index < inputLength; index++) {
			input.add(input.get(index));
		}
		return input;
	}
	/*
	public ArrayList<ArrayList<Integer>> getNextChord(boolean prevRating) {
		if(prevSequence != null) writeToLogFile(prevSequence, prevRating);
		ArrayList<ArrayList<Integer>> returnVal = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> baseNotes = new ArrayList<Integer>();
		int length = minLength + (int) Math.round(Math.random() * (maxLength - minLength));
		baseNotes.add(12);
		int currentNote = 7;
		for(int noteIndex = 1; noteIndex < length; noteIndex++) {
			int currentNoteStep = (int) Math.round((Math.random() * 7.0 - 3.5));
			if(currentNoteStep == 0) {
				baseNotes.add(baseNotes.get(baseNotes.size() - 1));
				continue;
			}
			if(((currentNote + currentNoteStep) > 13) || ((currentNote + currentNoteStep) < 0)) currentNoteStep = -currentNoteStep;
			currentNote += currentNoteStep;
			if(currentNoteStep > 0) {
				baseNotes.add(ascendingMinorScale[currentNote % 7] + (currentNote / 7) * 12);
			} else {
				// descending minor scale = minor scale
				baseNotes.add(minorScale[currentNote % 7] + (currentNote / 7) * 12);
			}
		}
		returnVal.add(baseNotes);
		for(int triadIndex = 0; triadIndex < 2; triadIndex++) {
			ArrayList<Integer> chords = new ArrayList<Integer>();
			for(int index = 0; index < baseNotes.size(); index++) {
				int chordNote = baseNotes.get(index);
				chordNote += minorTriad[triadIndex];
				if(chordNote > 23) chordNote -= 2 * minorTriad[triadIndex];
				if(chordNote < 0) chordNote += 2 * minorTriad[triadIndex];
				chords.add(chordNote);
			}
			returnVal.add(chords);
		}
		prevSequence = returnVal;
		return returnVal;
	}
	*/

}
