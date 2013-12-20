
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
import java.util.TreeMap;

import javax.swing.JOptionPane;

import main.Module;
import main.ModuleEditor;
import main.SynthTools;
import main.Module.ModuleType;

public class SineBank implements Module {
	
	ModuleEditor parent = null;
	Integer moduleID = null;
	double amplitude = 1.0;
	double minFreqInHz = ModuleEditor.minOctave;
	double maxFreqInHz = ModuleEditor.maxOctave;
	double[] frequencies = null;
	double[] amplitudes = null;
	double minFreqInHzNoControl = ModuleEditor.minOctave;
	double duration = ModuleEditor.maxDuration;
	int cornerX;
	int cornerY;
	int width = 150; // should be >= value calculated by init
	int height = 300; // calculated by init
	
	ArrayList<Rectangle> ampControls = null;
	ArrayList<Integer> outputs;
	ArrayList<Integer> inputADD;
	ArrayList<Integer> inputAM;
	ArrayList<Integer> inputFM;
	
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
	
	public SineBank(ModuleEditor parent, int x, int y) {
		this.cornerX = x;
		this.cornerY = y;
		this.parent = parent;
		ArrayList<Double> freqArray = new ArrayList<Double>();
		for(double freq = minFreqInHz; freq <= maxFreqInHz; freq *= 2.0) {
			if(freq >= SynthTools.sampleRate / 2.0) continue;
			freqArray.add(freq);
		}
		frequencies = new double[freqArray.size()];
		for(int index = 0; index < frequencies.length; index++) frequencies[index] = freqArray.get(index);
		amplitudes = new double[frequencies.length];
		for(int index = 0; index < frequencies.length; index++) {
			amplitudes[index] = 1.0;
		}
		outputs = new ArrayList<Integer>();
		inputADD = new ArrayList<Integer>();
		inputAM = new ArrayList<Integer>();
		inputFM = new ArrayList<Integer>();
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
		boolean nativeOutput = false;
		if(waitingForModuleIDs == null) waitingForModuleIDs = new HashSet<Integer>();
		if(waitingForModuleIDs.contains(moduleID)) {
			//JOptionPane.showMessageDialog(parent.getParentFrame(), "Infinite Loop");
			//return new double[0];
			nativeOutput = true;
		}
		double[] innerControl = null;
		if(controlIn == null) {
			innerControl = new double[(int) Math.round(duration * SynthTools.sampleRate)];
			for(int index = 0; index < innerControl.length; index++) {
				innerControl[index] = 1.0;
			}
		} else {
				innerControl = controlIn;
		}
		int numSamples = innerControl.length;
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
		for(Integer inputID: inputFM) {
			if(nativeOutput) break;
			Input input = (Input) parent.connectorIDToConnector.get(inputID);
			if(input.getConnection() == null) continue;
			waitingForModuleIDs.add(moduleID);
			Module.Output output = (Module.Output) parent.connectorIDToConnector.get(input.getConnection());
			samplesFMArray.add(output.getSamples(waitingForModuleIDs, controlIn));
			waitingForModuleIDs.remove(moduleID);
		}
		for(double[] samplesFMIn: samplesFMArray) {
			for(int index = 0; index < numSamples; index++) {
				if(index >= samplesFMIn.length) break;
				samplesFM[index] += samplesFMIn[index]; 
			}
		}
		ArrayList<double[]> samplesAMArray = new ArrayList<double[]>();
		for(Integer inputID: inputAM) {
			if(nativeOutput) break;
			Input input = (Input) parent.connectorIDToConnector.get(inputID);
			if(input.getConnection() == null) continue;
			waitingForModuleIDs.add(moduleID);
			Module.Output output = (Module.Output) parent.connectorIDToConnector.get(input.getConnection());
			samplesAMArray.add(output.getSamples(waitingForModuleIDs, controlIn));
			waitingForModuleIDs.remove(moduleID);
		}
		if(!samplesAMArray.isEmpty()) {
			for(int index = 0; index < numSamples; index++) samplesAM[index] = 0.0;
		}
		for(double[] samplesAMIn: samplesAMArray) {
			for(int index = 0; index < numSamples; index++) {
				if(index >= samplesAMIn.length) continue;
				samplesAM[index] += samplesAMIn[index]; 
			}
		}
		ArrayList<double[]> samplesADDArray = new ArrayList<double[]>();
		for(Integer inputID: inputADD) {
			if(nativeOutput) break;
			Input input = (Input) parent.connectorIDToConnector.get(inputID);
			if(input.getConnection() == null) continue;
			waitingForModuleIDs.add(moduleID);
			Module.Output output = (Module.Output) parent.connectorIDToConnector.get(input.getConnection());
			samplesADDArray.add(output.getSamples(waitingForModuleIDs, controlIn));
			waitingForModuleIDs.remove(moduleID);
		}
		// no change if samplesADD.isEmpty()
		for(double[] samplesADDIn: samplesADDArray) {
			for(int index = 0; index < numSamples; index++) {
				if(index >= samplesADDIn.length) break;
				samplesADD[index] += samplesADDIn[index]; 
			}
		}
		double phase = 0;
		int startIndex = 0;
		if(nativeOutput) {
			returnVal[0] = 0.0;
			startIndex = 1;
		}
		for(double freq = minFreqInHz; freq <= maxFreqInHz; freq *= 2.0) {
			if(freq >= SynthTools.sampleRate / 2.0) continue;
			double deltaPhase = freq / SynthTools.sampleRate * Math.PI * 2.0;
			for(int index = startIndex; index < numSamples; index++) {
				if(samplesAM[index] == 0.0 || innerControl[index] < 0.0) {
					phase = 0.0;
					continue;
				}
				double inputPhase =  (phase + samplesFM[index]) * Math.PI;
				if(inputPhase > Math.PI) inputPhase -= 2.0 * Math.PI;
				returnVal[index] += Math.sin(inputPhase) * samplesAM[index] * amplitude + samplesADD[index];
				phase += deltaPhase * innerControl[index];
			}
		}
		return returnVal;
	}

	public void mousePressed(int x, int y) {
		int index = 0;
		for(Rectangle ampControl: ampControls) {
			if(ampControl.contains(x, y)) {
				Double inputAmplitude = getInput("Input Amplitude in dB", ModuleEditor.minAmplitudeIn_dB, ModuleEditor.maxAmplitudeIn_dB);
				if(inputAmplitude == null) return;
				amplitudes[index] = Math.pow(10.0, inputAmplitude / 20.0);
				parent.refreshView();
				return;
			}
			index++;
		}
		index = 0;
		for(Integer outputID: outputs) {
			Output output = (Output) parent.connectorIDToConnector.get(outputID);
			if(output.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(outputID);
				System.out.println("Sine Bank output: " + index);
			}
			index++;
		}
		index = 0;
		for(Integer inputID: inputADD) {
			Input input = (Input) parent.connectorIDToConnector.get(inputID);
			if(input.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(inputID);
				System.out.println("Sine Bank inputADD: " + index);
			}
			index++;
		}
		index = 0;
		for(Integer inputID: inputAM) {
			Input input = (Input) parent.connectorIDToConnector.get(inputID);
			if(input.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(inputID);
				System.out.println("Sine Bank inputAM: " + index);
			}
			index++;
		}
		index = 0;
		for(Integer inputID: inputFM) {
			Input input = (Input) parent.connectorIDToConnector.get(inputID);
			if(input.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(inputID);
				System.out.println("Sine Bank inputFM: " + index);
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
		if(g2 != null) g2.drawString("Sine Bank", currentX, currentY);
		if(g2 != null) g2.setColor(Color.GREEN);
		currentY += yStep;
		ampControls = new ArrayList<Rectangle>();
		for(double freq: frequencies) {
			double amplitude = Math.round(amplitudes[ampControls.size()] * 10000.0) / 10000.0;
			double logAmplitude = Math.round(Math.log(amplitudes[ampControls.size()])/Math.log(10.0) * 2000.0) / 100.0;
			if(freq >= SynthTools.sampleRate / 2.0) {
				if(g2 != null) g2.setColor(Color.GRAY);
			}
			if(g2 != null) g2.drawString(freq + ": " + amplitude + " (" + logAmplitude + "dB)", currentX, currentY);
			ampControls.add(new Rectangle(currentX, currentY - fontSize, width, fontSize));
			currentY += yStep;
		}
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
		if(g2 != null) g2.drawString("FM: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) inputFM.add(parent.addConnector(new Input(this, currentRect)));
		}
		currentY += yStep;
		if(g2 != null) g2.setColor(Color.BLUE);
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
		//if(g2 == null) height = currentY + 6 - y;
		//if(g2 == null) width = height;
		//System.out.println(width);
	}

	public void loadModuleInfo(BufferedReader in) {
		ArrayList<Double> amplitudesArray = new ArrayList<Double>();
		ArrayList<Double> frequenciesArray = new ArrayList<Double>();
		try { 
			String currentLine = in.readLine();
			int numAmplitudes = new Integer(currentLine);
			for(int index = 0; index < numAmplitudes; index++) {
				currentLine = in.readLine();
				amplitudesArray.add(new Double(currentLine));
			}
			for(int index = 0; index < numAmplitudes; index++) {
				currentLine = in.readLine();
				frequenciesArray.add(new Double(currentLine));
			}
		} catch (Exception e) {
			System.out.println("SineBank.loadModuleInfo: Error reading from file");
		}
		amplitudes = new double[amplitudesArray.size()];
		frequencies = new double[amplitudesArray.size()];
		for(int index = 0; index < amplitudes.length; index++) {
			amplitudes[index] = amplitudesArray.get(index);
			frequencies[index] = frequenciesArray.get(index);;
		}
	}

	public void saveModuleInfo(BufferedWriter out) {
		try { 
			out.write(new Integer(amplitudes.length).toString());
			out.newLine();
			for(double amplitude: amplitudes) {
				out.write(new Double(amplitude).toString());
				out.newLine();
			}
			for(double frequency: frequencies) {
				out.write(new Double(frequency).toString());
				out.newLine();
			}
		} catch (Exception e) {
			System.out.println("SineBank.Waveform.saveModuleInfo: Error saving to file");
		}
	}

	@Override
	public ModuleType getModuleType() {
		// TODO Auto-generated method stub
		return Module.ModuleType.SINEBANK;
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