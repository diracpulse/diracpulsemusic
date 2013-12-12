package main.modules;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
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
import main.SynthTools;

public class BasicWaveform implements Module {
	
	public enum WaveformType {
		SINE,
		TRIANGLE,
		SQUAREWAVE,
		SAWTOOTH,
	}

	ModuleEditor parent = null;
	Integer moduleID = null;
	double amplitude = 1.0;
	double freqInHz = 440.0;
	double duration = ModuleEditor.maxDuration;
	int width = 150; // should be >= value calculated by init
	int height = 150; // calculated by init
	WaveformType type = WaveformType.SINE;
	
	Rectangle typeControl = null;
	Rectangle freqControl = null;
	Rectangle ampControl = null;
	ArrayList<Output> outputs;
	ArrayList<Input> inputADD;
	ArrayList<Input> inputAM;
	ArrayList<Input> inputFM;
	HashMap<Integer, Long> outputToModuleID = null;
	HashMap<Integer, Long> inputAddToModuleID = null;
	
	private class Input extends Module.Input {

		public Input(Module parent, Rectangle selectArea, Integer connectionID) {
			super(parent, selectArea, connectionID);
			// TODO Auto-generated constructor stub
		}
		
	}
	
	private class Output extends Module.Output {

		private double[] calculatedSamples = null;
		
		public Output(Module parent, Rectangle selectArea, Integer connectionID) {
			super(parent, selectArea, connectionID);
			// TODO Auto-generated constructor stub
		}

		@Override
		public double[] getSamples(HashSet<Integer> waitingForModuleID) {
			if(calculatedSamples != null) return calculatedSamples;
			calculatedSamples = masterGetSamples(waitingForModuleID);
			return calculatedSamples;
		}
		
		public void clearSamples() {
			calculatedSamples = null;
		}
		
	}
	
	public BasicWaveform(ModuleEditor parent, int x, int y) {
		this.moduleID = ModuleEditor.getNextModuleID();
		this.parent = parent;
		outputs = new ArrayList<Output>();
		inputADD = new ArrayList<Input>();
		inputAM = new ArrayList<Input>();
		inputFM = new ArrayList<Input>();
		init(x, y);
		for(Output output: outputs) {
			parent.addOutput(output);
		}
		for(Input input: inputADD) {
			parent.addInput(input);
		}
		for(Input input: inputAM) {
			parent.addInput(input);
		}
		for(Input input: inputFM) {
			parent.addInput(input);
		}
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
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

	public double[] masterGetSamples(HashSet<Integer> waitingForModuleIDs) {
		if(waitingForModuleIDs == null) waitingForModuleIDs = new HashSet<Integer>();
		if(waitingForModuleIDs.contains(moduleID)) {
			JOptionPane.showMessageDialog((JFrame) parent, "Infinite Loop");
			return new double[0];
		}
		int numSamples = (int) Math.round(duration * SynthTools.sampleRate);
		double[] returnVal = new double[numSamples];
		double[] samplesFM = new double[numSamples];
		double[] samplesAM = new double[numSamples];
		double[] samplesADD = new double[numSamples];
		for(int index = 0; index < numSamples; index++) {
			returnVal[index] = 0.0;
			samplesFM[index] = 0.0;
			samplesAM[index] = 1.0;
			samplesADD[index] = 0.0;
		}
		ArrayList<double[]> samplesFMArray = new ArrayList<double[]>();
		for(Input input: inputFM) {
			if(input.getConnection() == null) continue;
			waitingForModuleIDs.add(moduleID);
			Module.Output output = (Module.Output) parent.connectorIDToConnector.get(input.getConnection());
			samplesFMArray.add(output.getSamples(waitingForModuleIDs));
			waitingForModuleIDs.remove(moduleID);
		}
		// no change if samplesFM.isEmpty()
		for(double[] samplesFMIn: samplesFMArray) {
			for(int index = 0; index < numSamples; index++) {
				if(index >= samplesFMIn.length) break;
				samplesFM[index] += samplesFMIn[index]; 
			}
		}
		ArrayList<double[]> samplesAMArray = new ArrayList<double[]>();
		for(Input input: inputAM) {
			if(input.getConnection() == null) continue;
			waitingForModuleIDs.add(moduleID);
			Module.Output output = (Module.Output) parent.connectorIDToConnector.get(input.getConnection());
			samplesAMArray.add(output.getSamples(waitingForModuleIDs));
			waitingForModuleIDs.remove(moduleID);
		}
		for(double[] samplesAMIn: samplesAMArray) {
			for(int index = 0; index < numSamples; index++) {
				samplesAM[index] = 0.0;
				if(index >= samplesAMIn.length) continue;
				samplesAM[index] += samplesAMIn[index]; 
			}
		}
		ArrayList<double[]> samplesADDArray = new ArrayList<double[]>();
		for(Input input: inputADD) {
			if(input.getConnection() == null) continue;
			waitingForModuleIDs.add(moduleID);
			Module.Output output = (Module.Output) parent.connectorIDToConnector.get(input.getConnection());
			samplesADDArray.add(output.getSamples(waitingForModuleIDs));
			waitingForModuleIDs.remove(moduleID);
		}
		// no change if samplesADD.isEmpty()
		for(double[] samplesADDIn: samplesADDArray) {
			for(int index = 0; index < numSamples; index++) {
				if(index >= samplesADDIn.length) break;
				samplesADD[index] += samplesADDIn[index]; 
			}
		}
		double deltaPhase = freqInHz / SynthTools.sampleRate * Math.PI * 2.0;
		double phase = 0;
		switch(type) {
			case SINE:
				for(int index = 0; index < numSamples; index++) {
					if(samplesAM[index] == 0.0) {
						phase = 0.0;
						continue;
					}
					double inputPhase =  phase + samplesFM[index];
					if(inputPhase > Math.PI) phase -= 2.0 * Math.PI;
					returnVal[index] = Math.sin(inputPhase) * samplesAM[index] * amplitude + samplesADD[index];
					phase += deltaPhase;
				}
				break;
			case SQUAREWAVE:
				for(int index = 0; index < numSamples; index++) {
					if(samplesAM[index] == 0.0) {
						phase = 0.0;
						continue;
					}
					returnVal[index] = squarewave(phase + samplesFM[index]) * samplesAM[index] * amplitude + samplesADD[index];
					phase += deltaPhase;
				}
				break;
			case TRIANGLE:
				for(int index = 0; index < numSamples; index++) {
					if(samplesAM[index] == 0.0) {
						phase = 0.0;
						continue;
					}
					returnVal[index] = triangle(phase + samplesFM[index]) * samplesAM[index] * amplitude + samplesADD[index];
					phase += deltaPhase;
				}
				break;
			case SAWTOOTH:
				for(int index = 0; index < numSamples; index++) {
					if(samplesAM[index] == 0.0) {
						phase = 0.0;
						continue;
					}
					returnVal[index] = sawtooth(phase + samplesFM[index]) * samplesAM[index] * amplitude + samplesADD[index];
					phase += deltaPhase;
				}
				break;
		}
		return returnVal;
	}
	
	public double sawtooth(double phase) {
		phase -= Math.floor(phase / (Math.PI * 2.0)) * Math.PI * 2.0;
		if(phase < Math.PI) return phase / Math.PI;
		if(phase > Math.PI) return -1.0 + (phase - Math.PI) / Math.PI;
		return Math.random() * 2.0 - 1.0; // phase == Math.PI
	}

	public double squarewave(double phase) {
		phase -= Math.floor(phase / (Math.PI * 2.0)) * Math.PI * 2.0;
		if(phase < Math.PI) return 1.0;
		if(phase > Math.PI) return -1.0;
		return Math.random() * 2.0 - 1.0; // phase == Math.PI
	}
	
	public double triangle(double phase) {
		phase -= Math.floor(phase / (Math.PI * 2.0)) * Math.PI * 2.0;
		if(phase < Math.PI / 2.0) return phase / (Math.PI / 2.0);
		if(phase < Math.PI * 1.5) return 1.0 - (phase - Math.PI / 2.0) / (Math.PI / 2.0);
		return -1.0 + (phase - Math.PI * 1.5) / (Math.PI / 2.0);
	}

	public void mousePressed(int x, int y) {
		if(typeControl.contains(x, y)) {
			WaveformType inputType = (WaveformType) JOptionPane.showInputDialog(null, "Choose a type", "Type Select", JOptionPane.INFORMATION_MESSAGE, null, WaveformType.values(),  WaveformType.SINE);
			if(inputType == null) return;
			type = inputType;
			parent.refreshView();
			return;
		}
		if(freqControl.contains(x, y)) {
			Double inputFreqInHz = getInput("Input Frequency In Hz", ModuleEditor.minFrequency, ModuleEditor.maxFrequency);
			if(inputFreqInHz == null) return;
			freqInHz = inputFreqInHz;
			parent.refreshView();
			return;
		}
		if(ampControl.contains(x, y)) {
			Double inputAmplitude = getInput("Input Amplitude in dB", ModuleEditor.minAmplitudeIn_dB, ModuleEditor.maxAmplitudeIn_dB);
			if(inputAmplitude == null) return;
			amplitude = Math.pow(10.0, inputAmplitude / 20.0);
			parent.refreshView();
			return;
		}
		int index = 0;
		for(Output output: outputs) {
			if(output.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(output.getConnectorID());
				System.out.println(type + " " + "output: " + index);
			}
			index++;
		}
		index = 0;
		for(Input inputADDval: inputADD) {
			if(inputADDval.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(inputADDval.getConnectorID());
				System.out.println(type + " inputADD: " + index);
			}
			index++;
		}
		index = 0;
		for(Input inputAMval: inputAM) {
			if(inputAMval.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(inputAMval.getConnectorID());
				System.out.println(type + " inputAM: " + index);
			}
			index++;
		}
		index = 0;
		for(Input inputFMval: inputFM) {
			if(inputFMval.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(inputFMval.getConnectorID());
				System.out.println(type + " inputFM: " + index);
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
			JOptionPane.showMessageDialog((JFrame) parent, "Could not parse string");
			return null;
		}
		if(returnVal < minBound || returnVal > maxBound) {
			JOptionPane.showMessageDialog((JFrame) parent, "Input must be between: " + minBound + " and " + maxBound);
			return null;
		}
		return returnVal;
	}
	
	public void init(int x, int y) {
		draw(null, x, y);
	}
	
	public void draw(Graphics g, int x, int y) {
		int currentX = x;
		int currentY = y;
		Graphics2D g2 = null;
		if(g != null) g2 = (Graphics2D) g;
		if(g2 != null) g2.setColor(Color.GRAY);
		if(g2 != null) g2.setStroke(new BasicStroke(2));
		if(g2 != null) g2.drawRect(x, y, width, height);
		if(g2 != null) g2.setColor(Color.DARK_GRAY);
		if(g2 != null) g2.fillRect(x, y, width, height);
		int fontSize = 12;
		int yStep = fontSize + 6;
		int xStep = yStep;
		if(g2 != null) g2.setColor(Color.WHITE);
		Font font = new Font(Font.SANS_SERIF, Font.BOLD, fontSize);
		if(g2 != null) g2.setFont(font);
		currentX = x + 4;
		currentY = y + yStep;
		if(g2 != null) g2.drawString(type.toString(), currentX, currentY);
		if(g2 == null) typeControl = new Rectangle(currentX, currentY - fontSize, width, fontSize);
		if(g2 != null) g2.setColor(Color.GREEN);
		currentY += yStep;
		if(g2 != null) g2.drawString("Frequency: " + freqInHz, currentX, currentY);
		if(g2 == null) freqControl = new Rectangle(currentX, currentY - fontSize, width, fontSize);
		currentY += yStep;
		if(g2 != null) g2.drawString("Amp: " + Math.round(amplitude * 100000.0) / 100000.0 + " (" + Math.round(Math.log(amplitude)/Math.log(10.0) * 2000.0) / 100.0 + "dB)", currentX, currentY);
		if(g2 == null) ampControl = new Rectangle(currentX, currentY - fontSize, width, fontSize);
		currentY += yStep;
		if(g2 != null) g2.drawString("ADD: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) inputADD.add(new Input(this, currentRect, ModuleEditor.getNextConnectorID()));
		}
		currentY += yStep;
		if(g2 != null) g2.drawString("AM: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) inputAM.add(new Input(this, currentRect,  ModuleEditor.getNextConnectorID()));
		}
		currentY += yStep;
		if(g2 != null) g2.drawString("FM: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) inputFM.add(new Input(this, currentRect,  ModuleEditor.getNextConnectorID()));
		}
		if(g2 != null) g2.setColor(Color.BLUE);
		currentY += yStep;
		if(g2 != null) g2.drawString("OUT: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) outputs.add(new Output(this, currentRect,  ModuleEditor.getNextConnectorID()));
		}
		//if(g2 == null) height = currentY + 6 - y;
		//if(g2 == null) width = height;
		//System.out.println(width);
	}
}
