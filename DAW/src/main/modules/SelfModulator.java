package main.modules;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JOptionPane;

import main.Module;
import main.ModuleEditor;

public class SelfModulator implements Module {

	public enum ModulationType {
		COMPRESSOR,
		RECTIFIER;
	}
	
	ModuleEditor parent = null;
	Integer moduleID = null;
	double exponent = 0.5;
	double gain = 1.0;
	int cornerX;
	int cornerY;
	int width = 150; // should be >= value calculated by init
	int height = 150; // calculated by init
	ModulationType type = ModulationType.COMPRESSOR;
	
	Rectangle typeControl = null;
	Rectangle exponentControl = null;
	Rectangle gainControl = null;
	ArrayList<Integer> inputs;
	ArrayList<Integer> outputs;
	ArrayList<Integer> controlInputs;
	//HashMap<Integer, Long> outputToModuleID = null;
	//HashMap<Integer, Long> inputAddToModuleID = null;
	private double[] calculatedSamples = null;
	
	private class Input extends Module.Input {

		public Input(Module parent, Rectangle selectArea) {
			super(parent, selectArea);
			// TODO Auto-generated constructor stub
		}
		
	}
	
	private class Output extends Module.Output {

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
		controlInputs = new ArrayList<Integer>();
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

	public double[] masterGetSamples(HashSet<Integer> waitingForModuleIDs, double[] control) {
		if(waitingForModuleIDs == null) waitingForModuleIDs = new HashSet<Integer>();
		if(waitingForModuleIDs.contains(moduleID)) {
			JOptionPane.showMessageDialog(parent.getParentFrame(), "Loop in Self Modulator");
			return new double[0];
		}
		ArrayList<double[]> inputArray = new ArrayList<double[]>();
		for(Integer inputID: inputs) {
			Input input = (Input) parent.connectors.get(inputID);
			if(input.getConnection() == null) continue;
			waitingForModuleIDs.add(moduleID);
			Module.Output output = (Module.Output) parent.connectors.get(input.getConnection());
			inputArray.add(output.getSamples(waitingForModuleIDs, control));
			waitingForModuleIDs.remove(moduleID);
		}
		double[] inputSamples = new double[control.length];
		for(double[] inputIn: inputArray) {
			for(int index = 0; index < inputSamples.length - 1; index++) {
				if(index >= inputIn.length) break;
				inputSamples[index] += inputIn[index]; 
			}
		}
		double[] controlSamples = new double[control.length];
		ArrayList<double[]> controlInputArray = new ArrayList<double[]>();
		for(Integer inputID: controlInputs) {
			Input input = (Input) parent.connectors.get(inputID);
			if(input.getConnection() == null) continue;
			waitingForModuleIDs.add(moduleID);
			Module.Output output = (Module.Output) parent.connectors.get(input.getConnection());
			controlInputArray.add(output.getSamples(waitingForModuleIDs, control));
			waitingForModuleIDs.remove(moduleID);
		}
		if(!controlInputArray.isEmpty()) {
			for(int index = 0; index < controlSamples.length; index++) controlSamples[index] = 0.0;
		} else {
			for(int index = 0; index < controlSamples.length; index++) controlSamples[index] = 1.0;
		}
		for(double[] controlSamplesIn: controlInputArray) {
			for(int index = 0; index < inputSamples.length; index++) {
				if(index >= controlSamplesIn.length) continue;
				controlSamples[index] += controlSamplesIn[index]; 
			}
		}
		switch(type) {
			case COMPRESSOR:
				for(int index = 0; index < inputSamples.length; index++) {
					if(exponent != 1.0) {
						if(inputSamples[index] < 0.0) {
							inputSamples[index] = Math.pow(Math.abs(inputSamples[index]), exponent) * -1.0 * gain;
						} else {
							inputSamples[index] = Math.pow(Math.abs(inputSamples[index]), exponent) * gain;
						}
					} else {
						inputSamples[index] *= gain;
					}
				}
				break;
			case RECTIFIER:
				for(int index = 0; index < inputSamples.length; index++) {
					inputSamples[index] = Math.abs(inputSamples[index]);
				}
				break;				
		}
		return inputSamples;
	}
	

	public void mousePressed(int x, int y) {
		if(typeControl.contains(x, y)) {
			ModulationType inputType = (ModulationType) JOptionPane.showInputDialog(null, "Choose a type", "Type Select", JOptionPane.INFORMATION_MESSAGE, null, ModulationType.values(),  ModulationType.COMPRESSOR);
			if(inputType == null) return;
			type = inputType;
			parent.refreshData();
			return;
		}
		if(exponentControl.contains(x, y)) {
			Double inputExponent = getInput("Input Exponent", 0.0, 16.0);
			if(inputExponent == null) return;
			exponent = inputExponent;
			parent.refreshData();
			return;
		}
		if(gainControl.contains(x, y)) {
			Double inputGain = getInput("Input Gain (Log2)", ModuleEditor.minAmplitudeLog2, ModuleEditor.maxAmplitudeLog2);
			if(inputGain == null) return;
			gain = Math.pow(2.0, inputGain);
			parent.refreshData();
			return;
		}
		int index = 0;
		for(Integer outputID: outputs) {
			Output output = (Output) parent.connectors.get(outputID);
			if(output.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(outputID);
				System.out.println("Self Modulator " + "output: " + index);
			}
			index++;
		}
		index = 0;
		for(Integer inputID: inputs) {
			Input input = (Input) parent.connectors.get(inputID);
			if(input.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(inputID);
				System.out.println(type.toString() + " input: " + index);
			}
			index++;
		}
		index = 0;
		for(Integer inputID: controlInputs) {
			Input input = (Input) parent.connectors.get(inputID);
			if(input.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(inputID);
				System.out.println(type.toString() + " control input: " + index);
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
		if(g2 != null) g2.setColor(Color.WHITE);
		Font font = new Font(Font.SANS_SERIF, Font.BOLD, fontSize);
		if(g2 != null) g2.setFont(font);
		currentX = cornerX + 4;
		currentY = cornerY + yStep;
		if(g2 != null) g2.drawString(type.toString(), currentX, currentY);
		if(g2 == null) typeControl = new Rectangle(currentX, currentY - fontSize, width, fontSize);
		if(g2 != null) g2.setColor(Color.GREEN);
		currentY += yStep;
		if(g2 != null) g2.drawString("Gain: " + Math.round(gain * 10000.0) / 10000.0 + " (" + Math.round(Math.log(gain)/Math.log(2.0) * 100.0) / 100.0 + " Log2)", currentX, currentY);
		if(g2 == null) gainControl = new Rectangle(currentX, currentY - fontSize, width, fontSize);
		currentY += yStep;
		if(g2 != null) g2.drawString("Exponent: " + exponent, currentX, currentY);
		if(g2 == null) exponentControl = new Rectangle(currentX, currentY - fontSize, width, fontSize);
		currentY += yStep;
		if(g2 != null) g2.drawString("Control: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) controlInputs.add(parent.addConnector(new Input(this, currentRect)));
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
	}

	public void loadModuleInfo(BufferedReader in) {
		try {
			String currentLine = in.readLine();
			this.type = SelfModulator.ModulationType.valueOf(currentLine);
			currentLine = in.readLine();
			this.exponent = new Double(currentLine);
			currentLine = in.readLine();
			this.gain = new Double(currentLine);
		} catch (Exception e) {
			System.out.println("SelfModulator.loadModuleInfo: Error reading from file");
		}
		
	}

	public void saveModuleInfo(BufferedWriter out) {
		try {
			out.write(this.type.toString());
			out.newLine();
			out.write(new Double(exponent).toString());
			out.newLine();
			out.write(new Double(gain).toString());
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
