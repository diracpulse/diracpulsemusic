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
	public static final double[] allJustIntonation = {1.0, 9.0 / 8.0, 6.0 / 5.0, 5.0 / 4.0, 4.0 / 3.0, 3.0 / 2.0, 5.0 / 3.0, 8.0 / 5.0, 9.0 / 5.0, 15.0 / 8.0};

	private JPanel parent = null;
	private Random random = null;
	private int minLength = 8;
	private int maxLength = 8;
	private ArrayList<ArrayList<Integer>> prevSequence = null;
	private BufferedWriter writer = null;

	public Scale(JPanel parent, int minLength, int maxLength) {
		this.parent = parent;
		this.minLength = minLength;
		this.maxLength = maxLength;
		this.random = new Random();
		initLogFile();
	}
	
	private void initLogFile() {
		try {
			writer = new BufferedWriter(new FileWriter("CHORDS", true));
			writer.write("0 NEW SESSION");
			writer.newLine();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(parent, "Scale: There was a problem writing to the log file");
			return;
		}
	}

	private void writeToLogFile(ArrayList<ArrayList<Integer>> sequence, boolean rating) {
		try {
			writer.write(sequence.size() + " ");
			for(ArrayList<Integer> voice: sequence) {
				writer.write(" {");
				for(int note: voice) writer.write(note + " ");
				writer.write("} ");
			}
			writer.write(new Boolean(rating).toString());
			writer.newLine();
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
		return justIntonation[note % 12] * Math.pow(2.0, Math.floor(note / 12.0));
	}

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
}
