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
import main.MultiWindow;
import main.SynthTools;

public class BasicWaveform implements Module {
	
	public enum WaveformType {
		SINE,
		TRIANGLE,
		SQUAREWAVE,
		SAWTOOTH,
	}

	private ModuleEditor parent = null;
	private Integer moduleID = null;
	private double amplitude = 1.0;
	private double freqInHz = ModuleEditor.defaultOctave;
	private int cornerX;
	private int cornerY;
	private int width = 150; // should be >= value calculated by init
	private int height = 150; // calculated by init
	private WaveformType type = WaveformType.SINE;
	private Rectangle typeControl = null;
	private Rectangle freqControl = null;
	private Rectangle ampControl = null;
	private ArrayList<Integer> outputs;
	private ArrayList<Integer> inputAM;
	private ArrayList<Integer> inputVCO;
	private ArrayList<Integer> inputFM;

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
	
	public BasicWaveform(ModuleEditor parent, int x, int y) {
		this.cornerX = x;
		this.cornerY = y;
		this.parent = parent;
		outputs = new ArrayList<Integer>();
		inputAM = new ArrayList<Integer>();
		inputVCO = new ArrayList<Integer>();
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
	
	public int getTypeID() {
		return parent.getIDFromModuleID(moduleID, ModuleType.BASICWAVEFORM);
	}

	public Integer getModuleId() {
		return moduleID;
	}
	
	public double getAmplitude() {
		return amplitude;
	}
	
	// Returns true if amplitude set to new value
	public boolean setAmplitude(double amplitude) {
		double amplitudeLog2 = Math.log(amplitude) / Math.log(2.0);
		if(amplitudeLog2 < ModuleEditor.minAmplitudeLog2 || amplitudeLog2 > ModuleEditor.maxAmplitudeLog2) return false;
		this.amplitude = amplitude;
		return true;
	}
	
	public double getFreqInHz() {
		return freqInHz;
	}
	
	// Returns true if freqInHz set to new value
	public boolean setFreqInHz(double freqInHz) {
		if(freqInHz < ModuleEditor.minFrequency || freqInHz > ModuleEditor.maxFrequency) return false;
		this.freqInHz = freqInHz;
		return true;
	}

	public double[] getSamplesLeft(HashSet<Integer> waitingForModuleIDs) {
		return null;
	}
	
	public double[] getSamplesRight(HashSet<Integer> waitingForModuleIDs) {
		return null;
	}

	public double[] masterGetSamples(HashSet<Integer> waitingForModuleIDs, double[] control) {
		boolean nativeOutput = false;
		if(waitingForModuleIDs == null) waitingForModuleIDs = new HashSet<Integer>();
		if(waitingForModuleIDs.contains(moduleID)) {
			//JOptionPane.showMessageDialog(parent.getParentFrame(), "Infinite Loop");
			//return new double[0];
			nativeOutput = true;
		}
		int numSamples = control.length;
		double[] returnVal = new double[numSamples];
		double[] samplesFM = new double[numSamples];
		double[] samplesVCO = new double[numSamples];
		double[] samplesAM = new double[numSamples];
		for(int index = 0; index < numSamples; index++) {
			returnVal[index] = 0.0;
			samplesFM[index] = 0.0;
			samplesVCO[index] = 0.0;
			samplesAM[index] = 1.0;
		}
		ArrayList<double[]> samplesVCOArray = new ArrayList<double[]>();
		for(Integer inputID: inputVCO) {
			if(nativeOutput) break;
			Input input = (Input) parent.connectors.get(inputID);
			if(input.getConnection() == null) continue;
			waitingForModuleIDs.add(moduleID);
			Module.Output output = (Module.Output) parent.connectors.get(input.getConnection());
			samplesVCOArray.add(output.getSamples(waitingForModuleIDs, control));
			waitingForModuleIDs.remove(moduleID);
		}
		// no change if samplesVCO.isEmpty()
		for(double[] samplesVCOIn: samplesVCOArray) {
			for(int index = 0; index < numSamples; index++) {
				if(index >= samplesVCOIn.length) break;
				samplesVCO[index] += samplesVCOIn[index]; 
			}
		}
		ArrayList<double[]> samplesFMArray = new ArrayList<double[]>();
		for(Integer inputID: inputFM) {
			if(nativeOutput) break;
			Input input = (Input) parent.connectors.get(inputID);
			if(input.getConnection() == null) continue;
			waitingForModuleIDs.add(moduleID);
			Module.Output output = (Module.Output) parent.connectors.get(input.getConnection());
			samplesFMArray.add(output.getSamples(waitingForModuleIDs, control));
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
		for(Integer inputID: inputAM) {
			if(nativeOutput) break;
			Input input = (Input) parent.connectors.get(inputID);
			if(input.getConnection() == null) continue;
			waitingForModuleIDs.add(moduleID);
			Module.Output output = (Module.Output) parent.connectors.get(input.getConnection());
			samplesAMArray.add(output.getSamples(waitingForModuleIDs, control));
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
		double deltaPhase = freqInHz / SynthTools.sampleRate * Math.PI * 2.0;
		double phase = 0;
		int startIndex = 0;
		if(nativeOutput) {
			returnVal[0] = 0.0;
			startIndex = 1;
		}
		switch(type) {
			case SINE:
				for(int index = startIndex; index < numSamples; index++) {
					if(samplesAM[index] == 0.0 || control[index] < 0.0) {
						phase = 0.0;
						continue;
					}
					returnVal[index] = Math.sin(phase + samplesFM[index]) * samplesAM[index] * amplitude;
					phase += (deltaPhase * Math.pow(2.0, samplesVCO[index])) * control[index];
				}
				break;
			case SQUAREWAVE:
				for(int index = startIndex; index < numSamples; index++) {
					if(samplesAM[index] == 0.0 || control[index] < 0.0) {
						phase = 0.0;
						continue;
					}
					returnVal[index] = squarewave(phase + samplesFM[index]) * samplesAM[index] * amplitude;
					phase += (deltaPhase * Math.pow(2.0, samplesVCO[index])) * control[index];
				}
				break;
			case TRIANGLE:
				for(int index = startIndex; index < numSamples; index++) {
					if(samplesAM[index] == 0.0 || control[index] < 0.0) {
						phase = 0.0;
						continue;
					}
					returnVal[index] = triangle(phase + samplesFM[index]) * samplesAM[index] * amplitude;
					phase += (deltaPhase * Math.pow(2.0, samplesVCO[index])) * control[index];
				}
				break;
			case SAWTOOTH:
				for(int index = startIndex; index < numSamples; index++) {
					if(samplesAM[index] == 0.0 || control[index] < 0.0) {
						phase = 0.0;
						continue;
					}
					returnVal[index] = sawtooth(phase + samplesFM[index]) * samplesAM[index] * amplitude;
					phase += (deltaPhase * Math.pow(2.0, samplesVCO[index])) * control[index];
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
			parent.refreshData();
			return;
		}
		if(freqControl.contains(x, y)) {
			Double inputFreqInHz = getInput("Input Frequency In Hz", ModuleEditor.minFrequency, ModuleEditor.maxFrequency);
			if(inputFreqInHz == null) return;
			freqInHz = inputFreqInHz;
			parent.refreshData();
			return;
		}
		if(ampControl.contains(x, y)) {
			Double inputAmplitude = getInput("Input Amplitude Log(2.0)", ModuleEditor.minAmplitudeLog2, ModuleEditor.maxAmplitudeLog2);
			if(inputAmplitude == null) return;
			amplitude = Math.pow(2.0, inputAmplitude);
			parent.refreshData();
			return;
		}
		int index = 0;
		for(Integer outputID: outputs) {
			Output output = (Output) parent.connectors.get(outputID);
			if(output.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(outputID);
				System.out.println(type + " " + "output: " + index);
				return;
			}
			index++;
		}
		index = 0;
		for(Integer inputID: inputAM) {
			Input input = (Input) parent.connectors.get(inputID);
			if(input.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(inputID);
				System.out.println(type + " inputAM: " + index);
				return;
			}
			index++;
		}
		index = 0;
		for(Integer inputID: inputVCO) {
			Input input = (Input) parent.connectors.get(inputID);
			if(input.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(inputID);
				System.out.println(type + " inputVCO: " + index);
				return;
			}
			index++;
		}
		index = 0;
		for(Integer inputID: inputFM) {
			Input input = (Input) parent.connectors.get(inputID);
			if(input.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(inputID);
				System.out.println(type + " inputFM: " + index);
				return;
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
		if(g2 != null) g2.drawString("Basic Waveform " + parent.getIDFromModuleID(this.moduleID, ModuleType.BASICWAVEFORM), currentX, currentY);
		currentY += yStep;
		if(g2 != null) g2.drawString(type.toString(), currentX, currentY);
		if(g2 == null) typeControl = new Rectangle(currentX, currentY - fontSize, width, fontSize);
		if(g2 != null) g2.setColor(Color.GREEN);
		currentY += yStep;
		if(g2 != null) g2.drawString("Frequency: " + freqInHz, currentX, currentY);
		if(g2 == null) freqControl = new Rectangle(currentX, currentY - fontSize, width, fontSize);
		currentY += yStep;
		if(g2 != null) g2.drawString("Amp: " + Math.round(amplitude * 10000.0) / 10000.0 + " (" + Math.round(Math.log(amplitude)/Math.log(2.0) * 100.0) / 100.0 + " Log2)", currentX, currentY);
		if(g2 == null) ampControl = new Rectangle(currentX, currentY - fontSize, width, fontSize);
		currentY += yStep;
		if(g2 != null) g2.drawString("AM: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) inputAM.add(parent.addConnector(new Input(this, currentRect)));
		}
		currentY += yStep;
		if(g2 != null) g2.drawString("VCO: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) inputVCO.add(parent.addConnector(new Input(this, currentRect)));
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
		//if(g2 == null) height = currentY + 6 - y;
		//if(g2 == null) width = height;
		//System.out.println(width);
	}

	public void loadModuleInfo(BufferedReader in) {
		try { 
			String currentLine = in.readLine();
			this.type = BasicWaveform.WaveformType.valueOf(currentLine);
			currentLine = in.readLine();
			this.freqInHz = new Double(currentLine);
			currentLine = in.readLine();
			this.amplitude = new Double(currentLine);
		} catch (Exception e) {
			System.out.println("BasicWaveform.loadModuleInfo: Error reading from file");
		}
		
	}

	public void saveModuleInfo(BufferedWriter out) {
		try { 
			out.write(this.type.toString());
			out.newLine();
			out.write(new Double(freqInHz).toString());
			out.newLine();		
			out.write(new Double(amplitude).toString());
			out.newLine();
		} catch (Exception e) {
			System.out.println("BasicWaveform.saveModuleInfo: Error saving to file");
		}
		
	}

	@Override
	public ModuleType getModuleType() {
		// TODO Auto-generated method stub
		return Module.ModuleType.BASICWAVEFORM;
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
	
	public MultiWindow getMultiWindow() {
		return parent.parent;
	}
	
	public ModuleEditor getModuleEditor() {
		return parent;
	}
}
