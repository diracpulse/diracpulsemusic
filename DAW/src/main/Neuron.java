package main;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Neuron {
	
	TreeMap<Integer, Neuron> branches;
	private int id = 0;
	private int depth = 0;
	private int trials = 0;
	private double trialScore = 0.0;
	
	Neuron(int id, int numBranches, int depth) {
		if(depth == 0) return;
		branches = new TreeMap<Integer, Neuron>();
		for(int n = 0; n < numBranches; n++) {
			branches.put(n, new Neuron(n, numBranches, depth - 1));
		}
	}
	
	Neuron(int id, int numBranches, int depth, boolean dummy) {
		branches = new TreeMap<Integer, Neuron>();
	}
	
	public void addBranch(int id, Neuron neuron) {
		branches.put(id, neuron);
	}
	
	Neuron(int trials, double trialScore) {
		this.trials = trials;
		this.trialScore = trialScore;
	}
	
	public double getWeighting(Component parent) {
		if(depth == 0) {
			Object[] options = {"Yes", "Neutral", "Remove"};
			Integer result = JOptionPane.showOptionDialog(parent, "Do you like this sequence", "Rate Sequence", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			if(result == null) {
				trialScore += 0.5;
			} else {
				if(result == 0) trialScore += 1.0;
				if(result == 1) trialScore += 0.5;
				if(result == 2) return 0.0;
			}
			trials++;
			return trialScore / trials;
		}
		ArrayList<Integer> branchesToRemove = new ArrayList<Integer>();
		double trialSum = 0.0;
		int numBranches = 0;
		for(Integer branchID: branches.keySet()) {
			double result = branches.get(branchID).getWeighting(parent);
			trialSum += result;
			if(result == 0.0) branchesToRemove.add(branchID);
			numBranches++;
		}
		for(Integer branchID: branchesToRemove) {
			branches.remove(branchID);
		}
		if(branches.isEmpty()) return 0.0;
		return trialSum / numBranches;
	}
	
	public int getNumTrials() {
		return trials;
	}
	
	public void saveNeuron(String descriptor, Component parent, BufferedWriter writer, ArrayList<Integer> branchIDs) {
		if(writer == null) {
			try {
				writer = new BufferedWriter(new FileWriter(descriptor + depth + ".txt", false));
			} catch (Exception e) {
				JOptionPane.showMessageDialog(parent, "Neuron: There was a problem writing to the log file");
				return;
			}
			for(Integer branchID: branches.keySet()) {
				branchIDs = new ArrayList<Integer>();
				branchIDs.add(branchID);
				saveNeuron(descriptor, parent, writer, branchIDs);
			}
			try {
				writer.close();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(parent, "Neuron: There was a problem writing to the log file");
				return;
			}

		}
		String returnVal = "";
		if(depth == 0) {
			for(int branchID: branchIDs) {
				returnVal += branchID + " ";
			}
			returnVal += trials + " " + trialScore + "\n";
			try {
				writer.write(returnVal);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(parent, "Neuron: There was a problem writing to the log file");
				return;
			}
			return;
		}
		for(Integer branchID: branches.keySet()) {
			branchIDs.add(branchID);
			saveNeuron(descriptor, parent, writer, branchIDs);
			branchIDs.remove(branchID);
		}	
	}
	
	public static Neuron loadChildren(String descriptor, int depthIn, Component parent, ArrayList<Integer> branchIDs) {
		ArrayList<Double> trialScores = new ArrayList<Double>();
		ArrayList<ArrayList<Integer>> allBranches = new ArrayList<ArrayList<Integer>>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(descriptor + depthIn + ".txt"));
			String strLine;
			while ((strLine = reader.readLine()) != null)   {
				allBranches.add(new ArrayList<Integer>());
				String[] arrayLine= strLine.split(" ");
				for(String intVal: arrayLine) {
					allBranches.get(allBranches.size() - 1).add(new Integer(intVal));
				}
				trialScores.add(new Double(arrayLine[arrayLine.length - 1]));
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(parent, "Neuron: There was a problem reading the log file");
			return null;
		}
		int numBranches = allBranches.get(allBranches.size() - 1).get(0);
		Neuron returnVal = new Neuron(0, numBranches + 1, depthIn, false);
		int index = 0;
		for(ArrayList<Integer> branch: allBranches) {
			int depth = depthIn;
			for(int id: branch) {
				//returnVal
			}
		}
		return null; // return Neuron
	}
	
	public static Neuron addChildren(ArrayList<Integer> branchList, int trials, double trialScoreIn) {
		if(branchList.size() == 0) {
			return new Neuron(trials, trialScoreIn);
		}
		int currentID = branchList.get(0);
		branchList.remove(0);
		return null; // addChildren;
	}

}
