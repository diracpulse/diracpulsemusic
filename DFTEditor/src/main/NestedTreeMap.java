package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;


public class NestedTreeMap {
	
	private TreeMap<Minor.Triad, NestedTreeMap> data;
	// scores should be between 0 and 1.0
	private ArrayList<Float> scores;
	
	public NestedTreeMap() {
		data = new TreeMap<Minor.Triad, NestedTreeMap>();
		scores = new ArrayList<Float>();
	}
	
	private void updateScore(Float score) {
		if(score == null) return;
		scores.add(score);
	}

	// score == null for no current score
	public void updateArray(ArrayList<Minor.Triad> array, Float score) {
		if(array == null || array.size() == 0) {
			System.out.println("Error: NestedTreeMap.addArray: null or empty array");
		}
		Minor.Triad key = array.get(0);
		if(array.size() == 1) {
			if(!data.containsKey(key)) data.put(key, new NestedTreeMap());
			data.get(key).updateScore(score);
			return;
		}
		if(!data.containsKey(key)) data.put(key, new NestedTreeMap());
		ArrayList<Minor.Triad> subArray = new ArrayList<Minor.Triad>(array.subList(1, array.size()));
		data.get(key).updateArray(subArray, score);
	}
	
	public void printTree() {
		printTree(0);
	}

	private void printTree(int depth) {
		StringBuffer sb = new StringBuffer();
		for(int indent = 0; indent < depth; indent++) {
			sb.append(" -- ");
		}
		for(Minor.Triad key: data.keySet()) {
			System.out.print(sb.toString());
			System.out.print(key.name() + " ");
			System.out.print(getAverageScore() + "/");
			System.out.print(getNumTrials());
			System.out.println();
			data.get(key).printTree(depth + 1);
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

}
