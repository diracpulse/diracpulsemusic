
package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Minor {
	
	private JPanel parent = null;
	private static int branchCount;
	private NestedTreeMap sequenceTree = null;
	
	private static Random random = new Random();
	public static final int[] majorScale = {0, 2, 4, 5, 7, 9, 11};
	public static final int[] minorScale = {0, 2, 3, 5, 7, 8, 10};
	public static final int[] ascendingMinorScale = {0, 2, 3, 5, 7, 9, 11};
	public static final int[] harmonicMinorScale = {0, 2, 3, 5, 7, 8, 11};
	public static final int[] majorTriad = {4, 7};
	public static final int[] minorTriad = {3, 7};
	public static final int[] diminsihedTriad = {3, 6};
	public static final int[] augmentedTriad = {4, 8};
	public static final double[] allJustIntonation = {1.0, 9.0 / 8.0, 6.0 / 5.0, 5.0 / 4.0, 4.0 / 3.0, 3.0 / 2.0, 5.0 / 3.0, 8.0 / 5.0, 9.0 / 5.0, 15.0 / 8.0};

	public enum Scale {
		minor, acendingMinor, harmonicMinor
	}
	
	public enum Triad {
		i, ii, iiDim, III, IIIAug, iv, IV, V, v, VI, viSharpDim, viiDim, VII 
	}

	public static class TriadNotes {
		
		static int[] i = {0, 3, 7};
		static int[] ii = {2, 2 + 3, 2 + 7};
		static int[] iiDim = {2, 2 + 3, 2 + 6};
		static int[] III = {3, 3 + 4, 3 + 7};
		static int[] IIIAug = {3, 3 + 4, 3 + 8};
		static int[] iv = {5, 5 + 3, 5 + 7};
		static int[] IV = {5, 5 + 4, 5 + 7};
		static int[] V = {7, 7 + 4, 7 + 7};
		static int[] v = {7, 7 + 3, 7 + 7};
		static int[] VI = {8, 8 + 4, 8 + 7};
		static int[] viSharpDim = {9, 9 + 3, 9 + 6};
		static int[] viiDim = {10, 10 + 3, 10 + 6};
		static int[] VII = {10, 10 + 4, 10 + 7};

		static int[] VIAsc = {9, 9 + 4, 9 + 7};
		static int[] viSharpDimAsc = {10, 10 + 3, 10 + 6};
		static int[] viiDimAsc = {11, 11 + 3, 11 + 6};
		static int[] VIIAsc = {11, 11 + 4, 11 + 7};
		
		public static Triad getTriadFromNotes(Scale scale, int[] notes) {
			boolean match = true;
			for(Triad triad: Triad.values()) {
				int[] triadNotes = getNotes(scale, triad);
				if(notes.length != triadNotes.length) continue;
				match = true;
				for(int index = 0; index < notes.length; index++) {
					if((notes[index] % 12) == triadNotes[index]) {
						continue;
					} else {
						match = false;
						break;
					}
				}
				if(match) return triad;
			}
			return null;
		}
		
		public static int[] getNotes(Scale scale, Triad triad) {
			switch(triad) {
			case i:
				return i;
			case ii:
				return ii;
			case iiDim:
				return iiDim;
			case III:
				return III;
			case IIIAug:
				return IIIAug;
			case iv:
				return iv;
			case IV:
				return IV;
			case V:
				return V;
			case v:
				return v;
			case VI:
				if(scale == Scale.acendingMinor) {
					return VIAsc;
				} else {
					return VI;
				}
			case viSharpDim:
				if(scale == Scale.acendingMinor) {
					return viSharpDimAsc;
				} else {
					return viSharpDim;
				}
			case viiDim:
				if(scale == Scale.minor) {
					return viiDim;
				} else {
					return viiDimAsc;
				}
			case VII:
				if(scale == Scale.minor) {
					return VII;
				} else {
					return VIIAsc;
				}
			}
			return null;
		}
	}
	
	public static class Progressions {	
		
		private static Scale currentScale = Scale.minor;
		private static Triad[] i = {Triad.ii, Triad.iiDim, Triad.III, Triad.IIIAug, Triad.iv, Triad.IV, Triad.V, Triad.v, Triad.VI, Triad.viSharpDim, Triad.viiDim, Triad.VII};
		private static Triad[] ii = {Triad.III, Triad.V, Triad.v, Triad.viiDim, Triad.VII};
		private static Triad[] iiDim = {Triad.III, Triad.V, Triad.v, Triad.viiDim, Triad.VII};
		private static Triad[] III = {Triad.iv, Triad.IV, Triad.VI, Triad.viSharpDim, Triad.viiDim, Triad.VI};
		private static Triad[] IIIAug = {Triad.iv, Triad.IV, Triad.VI, Triad.viSharpDim, Triad.viiDim, Triad.VI};;
		private static Triad[] iv = {Triad.V, Triad.v, Triad.viiDim, Triad.VII};
		private static Triad[] IV = {Triad.V, Triad.v, Triad.viiDim, Triad.VII};
		private static Triad[] v = {Triad.VI, Triad.viSharpDim};
		private static Triad[] V = {Triad.VI, Triad.viSharpDim};
		private static Triad[] VI = {Triad.III, Triad.IIIAug, Triad.iv, Triad.IV, Triad.V, Triad.v, Triad.viiDim, Triad.VII};
		private static Triad[] viSharpDim = {Triad.III, Triad.IIIAug, Triad.iv, Triad.IV, Triad.V, Triad.v, Triad.viiDim, Triad.VII};
		private static Triad[] viiDim = {Triad.i};
		private static Triad[] VII = {Triad.i};
		private static Triad[] allNoTonic = {Triad.ii, Triad.iiDim, Triad.III, Triad.IIIAug, Triad.iv, Triad.IV, Triad.V, Triad.v, Triad.VI, Triad.viSharpDim, Triad.viiDim, Triad.VII};
		
		public static Triad[] getProgressions() {
			return allNoTonic;
		}
		
		public static Triad[] getProgressions(Triad current) {
			switch(current) {
			case i:
				return i;
			case ii:
				return ii;
			case iiDim:
				return iiDim;
			case III:
				return III;
			case IIIAug:
				return IIIAug;
			case iv:
				return iv;
			case IV:
				return IV;
			case v:
				return v;
			case V:
				return V;
			case VI:
				return VI;
			case viSharpDim:
				return viSharpDim;
			case viiDim:
				return viiDim;
			case VII:
				return VII;
			}
			return null;
		}
		
		private static Triad getNextTriad(Triad current) {
			Triad[] progressions = getProgressions(current);
			Triad returnVal = progressions[random.nextInt(progressions.length)];
			while(returnVal == current) {
				returnVal = progressions[random.nextInt(progressions.length)];
			}
			return returnVal;
		}
		
		public static ArrayList<ArrayList<Integer>> getNextSequence(int length) {
			ArrayList<ArrayList<Integer>> returnVal = new ArrayList<ArrayList<Integer>>();
			ArrayList<Triad> triads = new ArrayList<Triad>();
			triads.add(Triad.i);
			ArrayList<Integer> chord = new ArrayList<Integer>();
			int[] notes = TriadNotes.getNotes(currentScale, Triad.i);
			for(int note: notes) {
				System.out.print(note + " ");
				chord.add(note);
			}
			returnVal.add(chord);
			System.out.println();
			for(int chordIndex = 1; chordIndex < length - 1; chordIndex++) {
				chord = new ArrayList<Integer>();
				Triad next = getNextTriad(triads.get(triads.size() - 1));
				triads.add(next);
				notes = TriadNotes.getNotes(currentScale, next);
				for(int note: notes) {
					System.out.print(note + " ");
					chord.add(note);
				}
				returnVal.add(chord);
				System.out.println();
			}
			// Add tail
			triads.add(Triad.i);
			chord = new ArrayList<Integer>();
			notes = TriadNotes.getNotes(currentScale, Triad.i);
			for(int note: notes) {
				System.out.print(note + " ");
				chord.add(note);
			}
			returnVal.add(chord);
			System.out.println();
			
			/*
			chord = new ArrayList<Integer>();
			triads.add(Triad.v);
			notes = TriadNotes.getNotes(Scale.minor, Triad.iv);
			for(int note: notes) {
				System.out.print(note + " ");
				chord.add(note);
			}
			returnVal.add(chord);
			System.out.println();
			chord = new ArrayList<Integer>();
			triads.add(Triad.i);
			notes = TriadNotes.getNotes(Scale.minor, Triad.i);
			for(int note: notes) {
				System.out.print(note + " ");
				chord.add(note);
			}
			returnVal.add(chord);
			System.out.println();
			*/
			return closestNotes(returnVal);
		}
		
		public static ArrayList<ArrayList<Integer>> closestNotes(ArrayList<ArrayList<Integer>> input) {
			int index = 0;
			for(int chordIndex = 1; chordIndex < input.size(); chordIndex++) {
				int adjust = 0;
				int currentNote = input.get(chordIndex - 1).get(0);
				int nextNote = input.get(chordIndex).get(0);
				int octaveStart = (int) Math.floor(Math.pow(2.0, (nextNote / 12))) * 12;
				if(Math.abs(currentNote - (octaveStart - nextNote)) < Math.abs(currentNote - nextNote)) {
					adjust = -octaveStart;
				} else {
					adjust = 0;
				}
				index = 0;
				for(int note: input.get(chordIndex)) {
					input.get(chordIndex).set(index, note + adjust);
					index++;
				}
			}
			for(int chordIndex = 0; chordIndex < input.size(); chordIndex++) {
				index = 0;
				for(int note: input.get(chordIndex)) {
					input.get(chordIndex).set(index, note + 12);
					index++;
				}
			}
			return input;
		}
		
		public static ArrayList<ArrayList<Integer>> centerNotes(ArrayList<ArrayList<Integer>> input) {
			for(int chordIndex = 0; chordIndex < input.size(); chordIndex++) {
				int currentNote = input.get(chordIndex).get(0);
				int adjust = 0;
				if(currentNote > 12 + 5) adjust = -12;
				int index = 0;
				for(int note: input.get(chordIndex)) {
					input.get(chordIndex).set(index, note + adjust);
					index++;
				}
			}
			return input;
		}
		
		public static ArrayList<ArrayList<Integer>> jumpControl(ArrayList<ArrayList<Integer>> input) {
			int startNote = 12;
			for(int chordIndex = 1; chordIndex < input.size(); chordIndex++) {
				int currentNote = input.get(chordIndex).get(0);
				int adjust = 0;
				if(currentNote - startNote > 5) {
					adjust = -12;
				}
				if(currentNote - startNote < -5) {
					adjust = 12;
				}
				if((currentNote + adjust) < 0) adjust = 0;
				if((currentNote + adjust) > 23) adjust = 0;
				int index = 0;
				for(int note: input.get(chordIndex)) {
					input.get(chordIndex).set(index, note + adjust);
					index++;
				}
				startNote = currentNote + adjust;
			}
			return input;
		}
	}

	public static void writeToLogFile(ArrayList<ArrayList<Integer>> sequence, float score, JPanel parent) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter("MINOR TRIADS.txt", true));
			int numVoices = sequence.size();
			int numBeats = sequence.get(0).size();
			for(int currentBeat = 0; currentBeat < numBeats; currentBeat++) {
				for(int currentVoice = 0; currentVoice < numVoices - 1; currentVoice++) {
					writer.write(sequence.get(currentVoice).get(currentBeat).intValue() + "");
					writer.write(" ");
				}
				writer.write(sequence.get(numVoices - 1).get(currentBeat).intValue() + "");
				writer.newLine();
			}
			writer.write("SCORE");
			writer.newLine();
			writer.write(new Float(score).toString());
			writer.newLine();
			writer.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(parent, "Scale: There was a problem writing to the log file");
			return;
		}
		
	}
	
	public static void loadLogFile(NestedTreeMap scored) {
		int maxLength = 0;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader("MINOR TRIADS.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		try {
			while(true) {
				Float score = null;
				ArrayList<Minor.Triad> sequence = new ArrayList<Minor.Triad>();
				while(true) {
					String currentLine = reader.readLine();
					if(currentLine.contains("SCORE")) {
						score = new Float(reader.readLine());
						currentLine = reader.readLine();
						break;
					}
					
					String[] chord = currentLine.split(" ");
					int[] notes = new int[chord.length];
					int index = 0;
					for(String note: chord) {
						notes[index] = new Integer(note);
					}
					sequence.add(TriadNotes.getTriadFromNotes(Scale.minor, notes));
				}
				if(sequence.size() > maxLength) maxLength = sequence.size();
				scored.updateArray(sequence, score);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		try {
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
