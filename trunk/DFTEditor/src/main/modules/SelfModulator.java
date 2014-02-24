package main.modules;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import main.FDData;
import main.Module;
import main.ModuleEditor;
import main.TestSignals.Generator;
import main.TestSignals.TAPair;
import main.modules.BasicWaveform.WaveformType;
import main.SynthTools;

public class SelfModulator implements Module {

	public enum ModulationType {
		SELFMODULATION,
		TEST;
	}
	
	ModuleEditor parent = null;
	Integer moduleID = null;
	double duration = ModuleEditor.maxDuration;
	double fmMod = 1.0;
	int cornerX;
	int cornerY;
	int width = 150; // should be >= value calculated by init
	int height = 150; // calculated by init
	ModulationType type = ModulationType.SELFMODULATION;
	
	
	Rectangle typeControl = null;
	Rectangle fmModControl = null;
	ArrayList<Integer> inputs;
	ArrayList<Integer> outputs;
	ArrayList<Integer> inputADD;
	ArrayList<Integer> inputAM;
	ArrayList<Integer> inputFMMod;
	//HashMap<Integer, Long> outputToModuleID = null;
	//HashMap<Integer, Long> inputAddToModuleID = null;
	
	private class Input extends Module.Input {

		public Input(Module parent, Rectangle selectArea) {
			super(parent, selectArea);
			// TODO Auto-generated constructor stub
		}
		
	}
	
	private class Output extends Module.Output {

		private double[] calculatedSamples = null;
		
		public Output(Module parent, Rectangle selectArea) {
			super(parent, selectArea);
			// TODO Auto-generated constructor stub
		}

		@Override
		public double[] getSamples(HashSet<Integer> waitingForModuleID, double[] control) {
			if(calculatedSamples != null) return calculatedSamples;
			calculatedSamples = masterGetSamples(waitingForModuleID, control);
			return calculatedSamples;
		}
		
		public void clearSamples() {
			calculatedSamples = null;
		}
		
	}

	public SelfModulator(ModuleEditor parent, int x, int y) {
		this.cornerX = x;
		this.cornerY = y;
		this.parent = parent;
		outputs = new ArrayList<Integer>();
		inputs = new ArrayList<Integer>();
		inputADD = new ArrayList<Integer>();
		inputAM = new ArrayList<Integer>();
		inputFMMod = new ArrayList<Integer>();
		init();
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public void setModuleId(Integer id) {
		this.moduleID = id;
	}
	
	public Integer getModuleId() {
		return moduleID;
	}
	
	
	public double[] getSamplesLeft(HashSet<Integer> waitingForModuleIDs) {
		return null;
	}
	
	public double[] getSamplesRight(HashSet<Integer> waitingForModuleIDs) {
		return null;
	}

	public double[] masterGetSamples(HashSet<Integer> waitingForModuleIDs, double[] controlIn) {
		if(waitingForModuleIDs == null) waitingForModuleIDs = new HashSet<Integer>();
		if(waitingForModuleIDs.contains(moduleID)) {
			JOptionPane.showMessageDialog(parent.getParentFrame(), "Loop in Self Modulator");
			return new double[0];
		}
		ArrayList<double[]> inputArray = new ArrayList<double[]>();
		for(Integer inputID: inputs) {
			Input input = (Input) parent.connectorIDToConnector.get(inputID);
			if(input.getConnection() == null) continue;
			waitingForModuleIDs.add(moduleID);
			Module.Output output = (Module.Output) parent.connectorIDToConnector.get(input.getConnection());
			inputArray.add(output.getSamples(waitingForModuleIDs, controlIn));
			waitingForModuleIDs.remove(moduleID);
		}
		double[] inputSamples = new double[(int) Math.round(duration * SynthTools.sampleRate)];
		if(controlIn != null) inputSamples = new double[controlIn.length];
		for(double[] inputIn: inputArray) {
			for(int index = 0; index < inputSamples.length - 1; index++) {
				if(index >= inputIn.length) break;
				inputSamples[index] += inputIn[index]; 
			}
		}
		double[] samplesFMMod = new double[inputSamples.length];
		for(int index = 0; index < inputSamples.length; index++) {
			samplesFMMod[index] = 1.0;
		}
		ArrayList<double[]> samplesFMModArray = new ArrayList<double[]>();
		for(Integer inputID: inputFMMod) {
			Input input = (Input) parent.connectorIDToConnector.get(inputID);
			if(input.getConnection() == null) continue;
			waitingForModuleIDs.add(moduleID);
			Module.Output output = (Module.Output) parent.connectorIDToConnector.get(input.getConnection());
			samplesFMModArray.add(output.getSamples(waitingForModuleIDs, controlIn));
			waitingForModuleIDs.remove(moduleID);
		}
		if(!samplesFMModArray.isEmpty()) {
			for(int index = 0; index < inputSamples.length; index++) samplesFMMod[index] = 0.0;
		}
		for(double[] samplesFMModIn: samplesFMModArray) {
			for(int index = 0; index < inputSamples.length; index++) {
				if(index >= samplesFMModIn.length) break;
				samplesFMMod[index] += samplesFMModIn[index]; 
			}
		}
		// Perform self modulation first
		switch(type) {
			case SELFMODULATION:
				for(int index = 0; index < inputSamples.length; index++) {
					double phase = inputSamples[index] * fmMod * samplesFMMod[index];
					if(phase > Math.PI) phase -= 2.0 * Math.PI;
					inputSamples[index] = Math.sin(phase);
				}
				break;
			case TEST:
				break;				
		}
		// Start AM and ADD operations
		double[] samplesAM = new double[inputSamples.length];
		double[] samplesADD = new double[inputSamples.length];
		for(int index = 0; index < inputSamples.length; index++) {
			samplesAM[index] = 1.0;
			samplesADD[index] = 0.0;
			samplesFMMod[index] = 1.0;
		}
		// Sum AM Inputs
		ArrayList<double[]> samplesAMArray = new ArrayList<double[]>();
		for(Integer inputID: inputAM) {
			Input input = (Input) parent.connectorIDToConnector.get(inputID);
			if(input.getConnection() == null) continue;
			waitingForModuleIDs.add(moduleID);
			Module.Output output = (Module.Output) parent.connectorIDToConnector.get(input.getConnection());
			samplesAMArray.add(output.getSamples(waitingForModuleIDs, controlIn));
			waitingForModuleIDs.remove(moduleID);
		}
		if(!samplesAMArray.isEmpty()) {
			for(int index = 0; index < samplesAM.length; index++) samplesAM[index] = 0.0;
		}
		for(double[] samplesAMIn: samplesAMArray) {
			for(int index = 0; index < inputSamples.length; index++) {
				if(index >= samplesAMIn.length) continue;
				samplesAM[index] += samplesAMIn[index]; 
			}
		}
		// Sum add inputs
		ArrayList<double[]> samplesADDArray = new ArrayList<double[]>();
		for(Integer inputID: inputADD) {
			Input input = (Input) parent.connectorIDToConnector.get(inputID);
			if(input.getConnection() == null) continue;
			waitingForModuleIDs.add(moduleID);
			Module.Output output = (Module.Output) parent.connectorIDToConnector.get(input.getConnection());
			samplesADDArray.add(output.getSamples(waitingForModuleIDs, controlIn));
			waitingForModuleIDs.remove(moduleID);
		}
		// no change if samplesADD.isEmpty()
		for(double[] samplesADDIn: samplesADDArray) {
			for(int index = 0; index < inputSamples.length; index++) {
				if(index >= samplesADDIn.length) break;
				samplesADD[index] += samplesADDIn[index]; 
			}
		}
		for(int index = 0; index < inputSamples.length; index++) {
			inputSamples[index] = inputSamples[index] * samplesAM[index] + samplesADD[index];
		}
		return inputSamples;
	}
	

	public void mousePressed(int x, int y) {
		if(typeControl.contains(x, y)) {
			ModulationType inputType = (ModulationType) JOptionPane.showInputDialog(null, "Choose a type", "Type Select", JOptionPane.INFORMATION_MESSAGE, null, ModulationType.values(),  ModulationType.SELFMODULATION);
			if(inputType == null) return;
			type = inputType;
			parent.refreshView();
			return;
		}
		if(fmModControl.contains(x, y)) {
			Double inputAmplitude = getInput("Input FMMod in dB", ModuleEditor.minFMModIn_dB, ModuleEditor.maxFMModIn_dB);
			if(inputAmplitude == null) return;
			fmMod = Math.pow(10.0, inputAmplitude / 20.0);
			parent.refreshView();
			return;
		}
		int index = 0;
		for(Integer outputID: outputs) {
			Output output = (Output) parent.connectorIDToConnector.get(outputID);
			if(output.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(outputID);
				System.out.println("Self Modulator " + "output: " + index);
			}
			index++;
		}
		index = 0;
		for(Integer inputID: inputs) {
			Input input = (Input) parent.connectorIDToConnector.get(inputID);
			if(input.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(inputID);
				System.out.println("Self Modulator" + " input: " + index);
			}
			index++;
		}
		index = 0;
		for(Integer inputID: inputADD) {
			Input input = (Input) parent.connectorIDToConnector.get(inputID);
			if(input.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(inputID);
				System.out.println("SelfModulator inputADD: " + index);
			}
			index++;
		}
		index = 0;
		for(Integer inputID: inputAM) {
			Input input = (Input) parent.connectorIDToConnector.get(inputID);
			if(input.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(inputID);
				System.out.println("SelfModulator inputAM: " + index);
			}
			index++;
		}
		index = 0;
		for(Integer inputID: inputFMMod) {
			Input input = (Input) parent.connectorIDToConnector.get(inputID);
			if(input.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(inputID);
				System.out.println(type + " inputFMMod: " + index);
			}
			index++;
		}
	}
	
	public Double getInput(String prompt, double minBound, double maxBound) {
		Double returnVal = null;
		String inputValue = JOptionPane.showInputDialog(prompt);
		if(inputValue == null) return null;
		try {
			returnVal = new Double(inputValue);
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(parent.getParentFrame(), "Could not parse string");
			return null;
		}
		if(returnVal < minBound || returnVal > maxBound) {
			JOptionPane.showMessageDialog(parent.getParentFrame(), "Input must be between: " + minBound + " and " + maxBound);
			return null;
		}
		return returnVal;
	}
	
	public void init() {
		draw(null);
	}
	
	public void draw(Graphics g) {
		int currentX = cornerX;
		int currentY = cornerY;
		Graphics2D g2 = null;
		if(g != null) g2 = (Graphics2D) g;
		if(g2 != null) g2.setColor(Color.GRAY);
		if(g2 != null) g2.setStroke(new BasicStroke(2));
		if(g2 != null) g2.drawRect(cornerX, cornerY, width, height);
		if(g2 != null) g2.setColor(Color.DARK_GRAY);
		if(g2 != null) g2.fillRect(cornerX, cornerY, width, height);
		int fontSize = 12;
		int yStep = fontSize + 6;
		int xStep = yStep;
		if(g2 != null) g2.setColor(Color.WHITE);
		Font font = new Font(Font.SANS_SERIF, Font.BOLD, fontSize);
		if(g2 != null) g2.setFont(font);
		currentX = cornerX + 4;
		currentY = cornerY + yStep;
		if(g2 != null) g2.drawString(type.toString(), currentX, currentY);
		if(g2 == null) typeControl = new Rectangle(currentX, currentY - fontSize, width, fontSize);
		if(g2 != null) g2.setColor(Color.GREEN);
		currentY += yStep;
		if(g2 != null) g2.drawString("FMMod: " + Math.round(fmMod * 100000.0) / 100000.0 + " (" + Math.round(Math.log(fmMod)/Math.log(10.0) * 2000.0) / 100.0 + "dB)", currentX, currentY);
		if(g2 == null) fmModControl = new Rectangle(currentX, currentY - fontSize, width, fontSize);
		currentY += yStep;
		if(g2 != null) g2.drawString("ADD: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) inputADD.add(parent.addConnector(new Input(this, currentRect)));
		}
		currentY += yStep;
		if(g2 != null) g2.drawString("AM: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) inputAM.add(parent.addConnector(new Input(this, currentRect)));
		}
		currentY += yStep;
		if(g2 != null) g2.drawString("FMMod: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) inputFMMod.add(parent.addConnector(new Input(this, currentRect)));
		}
		currentY += yStep;
		if(g2 != null) g2.drawString("IN: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) inputs.add(parent.addConnector(new Input(this, currentRect)));
		}
		if(g2 != null) g2.setColor(Color.BLUE);
		currentY += yStep;
		if(g2 != null) g2.drawString("OUT: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) outputs.add(parent.addConnector(new Output(this, currentRect)));
		}
		currentY += yStep;
		if(g2 != null) g2.drawString("OUT: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) outputs.add(parent.addConnector(new Output(this, currentRect)));
		}
	}

	public void loadModuleInfo(BufferedReader in) {
		try {
			String currentLine = in.readLine();
			this.type = SelfModulator.ModulationType.valueOf(currentLine);
			currentLine = in.readLine();
			this.fmMod = new Double(currentLine);
		} catch (Exception e) {
			System.out.println("SelfModulator.loadModuleInfo: Error reading from file");
		}
		
	}

	public void saveModuleInfo(BufferedWriter out) {
		try {
			out.write(this.type.toString());
			out.newLine();
			out.write(new Double(fmMod).toString());
			out.newLine();
		} catch (Exception e) {
			System.out.println("SelfModulator.loadModuleInfo: Error reading from file");
		}
		
	}

	@Override
	public ModuleType getModuleType() {
		// TODO Auto-generated method stub
		return Module.ModuleType.SELFMODULATOR;
	}

	@Override
	public int getX() {
		return cornerX;
	}

	@Override
	public int getY() {
		return cornerY;
	}
	
	@Override
	public boolean pointIsInside(int x, int y) {
		Rectangle moduleBounds = new Rectangle(this.cornerX, this.cornerY, width, height);
		return moduleBounds.contains(x, y);
	}
}
