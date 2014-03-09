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

import main.Filter;
import main.Module;
import main.ModuleEditor;

public class IIRFilter implements Module {
	
	ModuleEditor parent = null;
	Integer moduleID = null;
	double amplitude = 1.0;
	double freqInHz = ModuleEditor.defaultOctave;

	int cornerX;
	int cornerY;
	int width = 150; // should be >= value calculated by init
	int height = 150; // calculated by init
	Filter.Type type = Filter.Type.LOWPASS;
	int filterOrder = 1;
	int minOrder = 1;
	int maxOrder = 24;
	double filterQ = 0.5;
	
	Rectangle typeControl = null;
	Rectangle freqControl = null;
	Rectangle ampControl = null;
	Rectangle orderControl = null;
	Rectangle qControl = null;
	ArrayList<Integer> outputs;
	ArrayList<Integer> inputs;
	
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
	
	public IIRFilter(ModuleEditor parent, int x, int y) {
		this.cornerX = x;
		this.cornerY = y;
		this.parent = parent;
		outputs = new ArrayList<Integer>();
		inputs = new ArrayList<Integer>();
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
			//JOptionPane.showMessageDialog(parent.getParentFrame(), "Loop in FIRFilter");
			return new double[0];
		}
		double[] inputSamples = new double[control.length];
		for(int index = 0; index < inputSamples.length; index++) inputSamples[index] = 0.0;
		ArrayList<double[]> inputArray = new ArrayList<double[]>();
		for(Integer inputID: inputs) {
			Input input = (Input) parent.connectors.get(inputID);
			if(input.getConnection() == null) continue;
			waitingForModuleIDs.add(moduleID);
			Module.Output output = (Module.Output) parent.connectors.get(input.getConnection());
			inputArray.add(output.getSamples(waitingForModuleIDs, control));
			waitingForModuleIDs.remove(moduleID);
		}
		for(double[] samplesIn: inputArray) {
			for(int index = 0; index < inputSamples.length; index++) {
				if(index >= samplesIn.length) break;
				inputSamples[index] += samplesIn[index]; 
			}
		}
		// chop up input for filtering
		TreeMap<Integer, Integer> startToEnd = new TreeMap<Integer, Integer>();
		double[] returnVal = new double[control.length];
		for(int index = 0; index < returnVal.length; index++) {
			returnVal[index] = 0.0;
		}
		int controlIndex = 0;
		while(true) {
			while(control[controlIndex] < 0.0) {
				controlIndex++;
				if(controlIndex == control.length) break;
			}
			if(controlIndex == control.length) break;
			int start = controlIndex;
			double freqRatio = control[controlIndex];
			while(control[controlIndex] == freqRatio) {
				controlIndex++;
				if(controlIndex == control.length) break;
			}
			if(controlIndex == control.length) {
				startToEnd.put(start, controlIndex - 1);
				break;
			}
			startToEnd.put(start, controlIndex);
		}
		if(startToEnd.isEmpty()) return returnVal;
		for(int start: startToEnd.keySet()) {
			double[] input = new double[startToEnd.get(start) - start];
			for(int index = 0; index < input.length; index++) input[index] = inputSamples[index + start];
			double[] samples = getFilteredSamples(input, freqInHz * control[start]);
			for(int index = 0; index < samples.length; index++) {
				int returnIndex = start + index;
				if(returnIndex == returnVal.length) return returnVal;
				returnVal[returnIndex] += samples[index];
			}
		}
		return returnVal;
	}
	
	private double[] getFilteredSamples(double[] input, double freqInHz) {
		if(type == Filter.Type.LOWPASS) return Filter.butterworthLowpass(input, freqInHz, filterOrder);
		if(type == Filter.Type.HIGHPASS) return Filter.butterworthHighpass(input, freqInHz, filterOrder);
		if(type == Filter.Type.BANDPASS) return Filter.butterworthBandpass(input, freqInHz, filterQ, filterOrder);
		return null;
	}
	
	public void mousePressed(int x, int y) {
		if(typeControl.contains(x, y)) {
			Filter.Type inputType = (Filter.Type) JOptionPane.showInputDialog(null, "Choose a type", "Type Select", JOptionPane.INFORMATION_MESSAGE, null, Filter.Type.values(),  Filter.Type.LOWPASS);
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
		if(qControl.contains(x, y)) {
			Double inputQ = getInput("Input Q", Double.MIN_VALUE, Double.MAX_VALUE);
			if(inputQ == null) return;
			filterQ = inputQ;
			parent.refreshData();
			return;
		}
		if(orderControl.contains(x, y)) {
			Integer orderInput = getInput("Input Filter Order", minOrder, maxOrder);
			if(orderInput == null) return;
			filterOrder = orderInput;
			parent.refreshData();
			return;
		}
		int index = 0;
		for(Integer outputID: outputs) {
			Output output = (Output) parent.connectors.get(outputID);
			if(output.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(outputID);
				System.out.println(type + " " + "output: " + index);
			}
			index++;
		}
		index = 0;
		for(Integer inputID: inputs) {
			Input input = (Input) parent.connectors.get(inputID);
			if(input.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(inputID);
				System.out.println(type + " inputADD: " + index);
			}
			index++;
		}
	}
	
	public Integer getInput(String prompt, int minBound, int maxBound) {
		Integer returnVal = null;
		String inputValue = JOptionPane.showInputDialog(prompt);
		if(inputValue == null) return null;
		try {
			returnVal = new Integer(inputValue);
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
		if(g2 != null) g2.drawString("IIR Filter", currentX, currentY);
		currentY += yStep;
		if(g2 != null) g2.setColor(Color.GREEN);
		if(g2 != null) g2.drawString(type.toString(), currentX, currentY);
		if(g2 == null) typeControl = new Rectangle(currentX, currentY - fontSize, width, fontSize);
		currentY += yStep;
		if(g2 != null) g2.drawString("Frequency: " + freqInHz, currentX, currentY);
		if(g2 == null) freqControl = new Rectangle(currentX, currentY - fontSize, width, fontSize);
		currentY += yStep;
		if(g2 != null) g2.drawString("Amp: " + Math.round(amplitude * 100000.0) / 100000.0 + " (" + Math.round(Math.log(amplitude)/Math.log(10.0) * 2000.0) / 100.0 + "dB)", currentX, currentY);
		if(g2 == null) ampControl = new Rectangle(currentX, currentY - fontSize, width, fontSize);
		currentY += yStep;
		if(g2 != null) g2.drawString("Q: " + filterQ, currentX, currentY);
		if(g2 == null) qControl = new Rectangle(currentX, currentY - fontSize, width, fontSize);
		currentY += yStep;
		if(g2 != null) g2.drawString("Order: " + filterOrder, currentX, currentY);
		if(g2 == null) orderControl = new Rectangle(currentX, currentY - fontSize, width, fontSize);
		currentY += yStep;
		if(g2 != null) g2.setColor(Color.GREEN);
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
		//if(g2 == null) height = currentY + 6 - y;
		//if(g2 == null) width = height;
		//System.out.println(width);
	}

	public void loadModuleInfo(BufferedReader in) {
		try { 
			String currentLine = in.readLine();
			this.type = Filter.Type.valueOf(currentLine);
			currentLine = in.readLine();
			this.freqInHz = new Double(currentLine);
			currentLine = in.readLine();
			this.amplitude = new Double(currentLine);
			currentLine = in.readLine();
			this.filterOrder = new Integer(currentLine);
		} catch (Exception e) {
			System.out.println("IIRFilter.loadModuleInfo: Error reading from file");
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
			out.write(new Integer(filterOrder).toString());
			out.newLine();	
		} catch (Exception e) {
			System.out.println("IIRFilter.saveModuleInfo: Error saving to file");
		}
		
	}

	public ModuleType getModuleType() {
		// TODO Auto-generated method stub
		return Module.ModuleType.FIRFILTER;
	}

	public int getX() {
		return cornerX;
	}

	public int getY() {
		return cornerY;
	}
	
	public boolean pointIsInside(int x, int y) {
		Rectangle moduleBounds = new Rectangle(this.cornerX, this.cornerY, width, height);
		return moduleBounds.contains(x, y);
	}
	
	
}
