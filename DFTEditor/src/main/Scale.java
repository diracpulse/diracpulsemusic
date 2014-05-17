package main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeSet;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import main.Module.Connector;
import main.modules.MasterInput;


public class Scale {
	
	public static final int[] pythagorian53 = {0, 1, 4, 5, 9, 13, 14, 18, 22, 26, 27, 31, 35, 36, 40, 44, 45, 48, 49};
	public static final double[] carlosSuperJustIntonation = {1.0, 17.0 / 16.0, 9.0 / 8.0, 6.0 / 5.0, 5.0 / 4.0, 4.0 / 3.0, 11.0 / 8.0, 3.0 / 2.0, 13.0 / 8.0, 27.0 / 16.0, 7.0 / 4.0, 15.0 / 8.0};
	public static final double[] majorJustIntonation = {1.0, 9.0 / 8.0, 5.0 / 4.0, 4.0 / 3.0, 3.0 / 2.0, 5.0 / 3.0, 15.0 / 8.0};
	public static final double[] minorJustIntonation = {1.0, 9.0 / 8.0, 6.0 / 5.0, 4.0 / 3.0, 3.0 / 2.0, 8.0 / 5.0, 9.0 / 5.0};
	public static final double[] allJustIntonation = {1.0, 9.0 / 8.0, 6.0 / 5.0, 5.0 / 4.0, 4.0 / 3.0, 3.0 / 2.0, 5.0 / 3.0, 8.0 / 5.0, 9.0 / 5.0, 15.0 / 8.0};
	
	public enum Type {
		PYTHAGOREAN_53,
		SUPER_JUST_INTONATION_53,
		MAJOR_JUST_INTONATION_53,
		MINOR_JUST_INTONATION_53,
		ALL_JUST_INTONATION_53;
	}
	
	private JPanel parent = null;
	private Random random = null;
	private Type scaleType = null;
	private int minLength = 8;
	private int maxLength = 8;
	private ArrayList<Integer> prevSequence = null;
	private BufferedWriter writer = null;

	public Scale(JPanel parent, Type type, int minLength, int maxLength) {
		this.parent = parent;
		this.scaleType = type;
		this.minLength = minLength;
		this.maxLength = maxLength;
		this.random = new Random();
		initLogFile();
	}
	
	private void initLogFile() {
		try {
			writer = new BufferedWriter(new FileWriter(scaleType.toString(), true));
			writer.write("0 NEW SESSION");
			writer.newLine();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(parent, "Scale: There was a problem writing to the log file");
			return;
		}
	}

	private void writeToLogFile(ArrayList<Integer> sequence, boolean rating) {
		try {
			writer.write(sequence.size() + " ");
			for(int note: sequence) {
				writer.write(note + " ");
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
	
	public TreeSet<Integer> getNotes() {
		TreeSet<Integer> returnVal = new TreeSet<Integer>();
		switch(scaleType) {
		case PYTHAGOREAN_53:
			for(int note: pythagorian53) returnVal.add(note);
			return returnVal;
		case SUPER_JUST_INTONATION_53:
			for(double note: carlosSuperJustIntonation) {
				returnVal.add((int) Math.round(Math.log(note) / Math.log(2.0) * 53.0));
			}
			return returnVal;
		case MAJOR_JUST_INTONATION_53:
			for(double note: majorJustIntonation) {
				returnVal.add((int) Math.round(Math.log(note) / Math.log(2.0) * 53.0));
			}
			return returnVal;
		case MINOR_JUST_INTONATION_53:
			for(double note: minorJustIntonation) {
				returnVal.add((int) Math.round(Math.log(note) / Math.log(2.0) * 53.0));
			}
			return returnVal;
		case ALL_JUST_INTONATION_53:
			for(double note: minorJustIntonation) {
			returnVal.add((int) Math.round(Math.log(note) / Math.log(2.0) * 53.0));
			}
			return returnVal;
		}
		return null;
	}
	
	private static double randomStepStdDev = 2.0;
	private static double stepRepeatProbability = 0.25;
	private static double noteRepeatProbability = 0.25;
	
	public ArrayList<Integer> getNextSequence(boolean prevRating) {
		if(prevSequence != null) writeToLogFile(prevSequence, prevRating);
		ArrayList<Integer> notes = new ArrayList<Integer>(getNotes());
		TreeSet<Integer> noteIndices = new TreeSet<Integer>();
		TreeSet<Integer> noteSteps = new TreeSet<Integer>();
		ArrayList<Integer> returnVal = new ArrayList<Integer>();
		boolean repeat = true;
		int length = minLength + random.nextInt(maxLength - minLength + 1);
		int noteIndex = -1;
		int noteStep = -1;
		int prevNoteIndex = random.nextInt(notes.size());
		returnVal.add(notes.get(prevNoteIndex));
		noteIndices.add(prevNoteIndex);
		for(int index = 1; index < length; index++) {
			while (noteIndex < 0 || noteIndex >= notes.size() || repeat) {
				repeat = true;
				noteStep = (int) Math.round(random.nextGaussian() * randomStepStdDev);
				if(noteSteps.contains(noteStep)) if(Math.random() < stepRepeatProbability) continue;
				noteIndex = prevNoteIndex + noteStep;
				if(noteIndices.contains(noteStep)) if(Math.random() < noteRepeatProbability) continue;
				repeat = false;
			}
			noteIndices.add(noteIndex);
			noteSteps.add(noteStep);
			returnVal.add(notes.get(noteIndex));
			prevNoteIndex = noteIndex;
			repeat = true;
		}
		prevSequence = new ArrayList<Integer>(returnVal);
		return returnVal;
	}
}
