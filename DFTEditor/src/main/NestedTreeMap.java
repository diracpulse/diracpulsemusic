package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;

import main.Minor.Progressions;
import main.Minor.Triad;
import main.Minor.TriadNotes;


public class NestedTreeMap {
	
	private TreeMap<Minor.Triad, NestedTreeMap> scoredData;
	private TreeMap<Minor.Triad, NestedTreeMap> unscoredData;
	// scores should be between 0 and 1.0
	private ArrayList<Float> scores;
	static int branchCount = 0;
	
	private Random random;
	
	public NestedTreeMap() {
		random = new Random();
		scoredData = new TreeMap<Minor.Triad, NestedTreeMap>();
		unscoredData = new TreeMap<Minor.Triad, NestedTreeMap>();
		scores = new ArrayList<Float>();
	}

	private void updateScore(Float score) {
		scores.add(score);
	}

	public void removeArrayFromUnscored(ArrayList<Minor.Triad> array) {
		NestedTreeMap child = unscoredData.get(array.get(0));
		ArrayList<Minor.Triad> subArray = new ArrayList<Minor.Triad>(array.subList(1, array.size()));
		removeArrayFromUnscored(subArray, child);
	}

	public void removeArrayFromUnscored(ArrayList<Minor.Triad> array, NestedTreeMap parent) {
		if(array.size() == 1) {
			parent.unscoredData.remove(array.get(0));
			return;
		} else {
			NestedTreeMap child = unscoredData.get(array.get(0));
			ArrayList<Minor.Triad> subArray = new ArrayList<Minor.Triad>(array.subList(1, array.size()));
			removeArrayFromUnscored(subArray, child);
		}
		if(parent.unscoredData.keySet().size() == 0) {
			parent = null;
		}
		return;
	}
	
	public ArrayList<ArrayList<Integer>> getUnscoredSequence() {
		if(unscoredData.size() == 0) return null;
		ArrayList<Triad> triads = new ArrayList<Triad>();
		ArrayList<Triad> values = new ArrayList<Triad>(unscoredData.keySet());
		Triad value = values.get(random.nextInt(values.size()));
		NestedTreeMap child = unscoredData.get(value);
		triads.add(0, getUnscoredSequence(triads, child));
		triads.add(0, value);
		ArrayList<ArrayList<Integer>> returnVal = new ArrayList<ArrayList<Integer>>();
		for(Triad triad: triads) {
			ArrayList<Integer> chord = new ArrayList<Integer>();
			int[] notes = TriadNotes.getNotes(Minor.Scale.minor, triad);
			for(int note: notes) {
				System.out.print(note + " ");
				chord.add(note);
			}
			returnVal.add(chord);
			System.out.println();
		}
		return Minor.Progressions.closestNotes(returnVal);
	}
	
	private Minor.Triad getUnscoredSequence(ArrayList<Triad> returnVal, NestedTreeMap parent) {
		if(parent.unscoredData.size() == 0) return null;
		ArrayList<Triad> values = new ArrayList<Triad>(parent.unscoredData.keySet());
		Triad value = values.get(random.nextInt(values.size()));
		NestedTreeMap child = parent.unscoredData.get(value);
		Triad result = getUnscoredSequence(returnVal, child);
		if(result != null) returnVal.add(0, result);
		return value;
	}
	
	public void createArray(ArrayList<Minor.Triad> array) {
		if(array == null || array.size() == 0) {
			System.out.println("Error: NestedTreeMap.addArray: null or empty array");
		}
		Minor.Triad key = array.get(0);
		if(array.size() == 1) {
			unscoredData.put(key, new NestedTreeMap());
			return;
		}
		if(!unscoredData.containsKey(key)) unscoredData.put(key, new NestedTreeMap());
		ArrayList<Minor.Triad> subArray = new ArrayList<Minor.Triad>(array.subList(1, array.size()));
		unscoredData.get(key).createArray(subArray);
	}
	
	
	// score == null for no current score
	public void updateArray(ArrayList<Minor.Triad> array, Float score) {
		if(array == null || array.size() == 0) {
			System.out.println("Error: NestedTreeMap.addArray: null or empty array");
		}
		removeArrayFromUnscored(array);
		Minor.Triad key = array.get(0);
		if(array.size() == 1) {
			if(!scoredData.containsKey(key)) scoredData.put(key, new NestedTreeMap());
			scoredData.get(key).updateScore(score);
			return;
		}
		if(!scoredData.containsKey(key)) scoredData.put(key, new NestedTreeMap());
		ArrayList<Minor.Triad> subArray = new ArrayList<Minor.Triad>(array.subList(1, array.size()));
		scoredData.get(key).updateArray(subArray, score);
	}
	
	public void printTree() {
		printTree(0);
	}

	private void printTree(int depth) {
		StringBuffer sb = new StringBuffer();
		for(int indent = 0; indent < depth; indent++) {
			sb.append(" -- ");
		}
		for(Minor.Triad key: scoredData.keySet()) {
			System.out.print(sb.toString());
			System.out.print(key.name() + " ");
			System.out.print(getAverageScore() + "/");
			System.out.print(getNumTrials());
			System.out.println();
			scoredData.get(key).printTree(depth + 1);
		}
	}
	
	private int getNumTrials() {
		return scores.size();
	}
	
	private Float getAverageScore() {
		if(scores.size() == 0) return null;
		float averageScore = 0.0f;
		for(float score: scores) averageScore += score;
		return averageScore / scores.size();
	}
	

	public static NestedTreeMap createProgressionTree(int length) {
		NestedTreeMap returnVal = new NestedTreeMap();
		ArrayList<Triad> root = new ArrayList<Triad>();
		root.add(Triad.i);
		returnVal.createArray(root);
		branchCount++;
		createProgressionTree(returnVal, root, length - 2);
		//if(length == 4) returnVal.printTree();
		return returnVal;
	}
	
	private static void createProgressionTree(NestedTreeMap returnVal, ArrayList<Triad> array, int length) {
		for(Triad triad: Minor.Progressions.getProgressions()) {
			ArrayList<Triad> argVal = new ArrayList<Triad>();
			argVal.addAll(array);
			argVal.add(triad);
			returnVal.createArray(argVal);
			branchCount++;
			if(length > 1) {
				createProgressionTree(returnVal, argVal, length - 1);
			} else {
				argVal.add(Triad.i);
				returnVal.createArray(argVal);
			}
		}
	}
	
	

}
