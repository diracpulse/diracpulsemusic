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
import main.Filter.CriticalBand;
import main.Module;
import main.ModuleEditor;
import main.SynthTools;
import main.modules.SpectrumEQ.EQBand.FilterType;

public class SpectrumEQ implements Module {
	
	ModuleEditor parent = null;
	Integer moduleID = null;
	double amplitude = 1.0;
	double minFreqInHz = Filter.minFilterFrequency;
	double maxFreqInHz = Filter.maxFilterFrequency;
	double minFreqInHzLog2 = Math.log(minFreqInHz) / Math.log(2.0);
	double maxFreqInHzLog2 = Math.log(maxFreqInHz) / Math.log(2.0);
	ArrayList<EQBand> eqBands = new ArrayList<EQBand>();

	int cornerX;
	int cornerY;
	int width = 150; // should be >= value calculated by init
	int height = 150; // calculated by init
	
	ArrayList<Integer> outputs;
	ArrayList<Integer> inputs;
	
	public static class EQBand {
		
		public enum FilterType {
			BANDPASS,
			LOWPASS,
			HIGHPASS;
		}
		
		FilterType type = FilterType.BANDPASS;
		boolean controlled = false; // if true, centerFreq varies with control
		boolean subtractive = false; // if true signals blocked by filter are not in output
		double gain = 1.0;
		int order = 4;
		double lowerBound;
		double upperBound;
		double overshoot = 1.0;
		double filterQ = Math.sqrt(2.0) / 2.0; // Butterworth
		double maxFreq = Filter.maxFilterFrequency;
		double minFreq = Filter.minFilterFrequency;

		EQBand(double lowerBound, double upperBound) {
			type = FilterType.BANDPASS;
			this.upperBound = upperBound;
			this.lowerBound = lowerBound;
			if(upperBound > maxFreq) upperBound = maxFreq;
			if(lowerBound < minFreq) lowerBound = minFreq;
			if(upperBound < lowerBound) {
				upperBound = maxFreq;
				lowerBound = minFreq;
			}
		}
		
		EQBand(FilterType type, double centerFreq) {
			this.type = type;
			if(centerFreq > maxFreq) centerFreq = maxFreq;
			if(centerFreq < minFreq) centerFreq = minFreq;
			upperBound = centerFreq;
			lowerBound = centerFreq;
		}

		public void setOvershoot(double overshoot) {
			this.overshoot = overshoot;
		}
		
		public void setFilterQ(double filterQ) {
			this.filterQ = filterQ;
		}
		
		public void setCenterFreq(double centerFreq) {
			if(type == FilterType.BANDPASS) {
				double freqRatio = this.getCenterFreq() / centerFreq;
				upperBound /= freqRatio;
				lowerBound /= freqRatio;
				if(upperBound > maxFreq) upperBound = maxFreq;
				if(lowerBound < minFreq) lowerBound = minFreq;
			} else {
				if(centerFreq > maxFreq) centerFreq = maxFreq;
				if(centerFreq < minFreq) centerFreq = minFreq;
				lowerBound = centerFreq;
				upperBound = centerFreq;
			}
		}
		
		public double getOvershoot() {
			return overshoot;
		}
		
		// THIS CONTROLS FILTER SHAPE, NOT BANDWIDTH
		public double getFilterQ() {
			return filterQ;
		}
		
		public double getLowerBound() {
			double returnVal = lowerBound / overshoot;
			if(returnVal < minFreq) return minFreq;
			if(returnVal > upperBound * overshoot) return Math.sqrt(upperBound * lowerBound);
			return returnVal;
		}
		
		public double getUpperBound() {
			double returnVal = upperBound * overshoot;
			if(returnVal > maxFreq) return maxFreq;
			if(returnVal < lowerBound / overshoot) return Math.sqrt(upperBound * lowerBound);
			return returnVal;
		}
		
		public double getBandwidth() {
			return getUpperBound() - getLowerBound();
		}
		
		public double getCenterFreq() {
			return Math.sqrt(getUpperBound() * getLowerBound());
		}
		
		// THIS IS RELATED TO BANDWIDTH
		public double getQ() {
			if(getBandwidth() == 0.0) return Float.MAX_VALUE;
			return getCenterFreq() / getBandwidth();
		}
		
		public int getOrder() {
			return order;
		}
		
		public void setOrder(int order) {
			this.order = order;
		}
		
		public double getGain() {
			return gain;
		}
		
		public void setGain(double gain) {
			this.gain = gain;
		}
		
		public FilterType getType() {
			return type;
		}
		
		public void setType(FilterType type) {
			double currentFreq = getCenterFreq();
			if(type == FilterType.LOWPASS || type == FilterType.HIGHPASS) {
				upperBound = currentFreq;
				lowerBound = currentFreq;
			} else {
				if(upperBound == lowerBound) {
					upperBound = currentFreq * Math.sqrt(2.0);
					lowerBound = currentFreq / Math.sqrt(2.0);
				}
			}
			this.type = type;
		}
		
		public boolean isSubtractive() {
			return subtractive;
		}
		
		public boolean isControlled() {
			return controlled;
		}
		
		public void toggleSubtractive() {
			subtractive = !subtractive;
		}
		
		public void toggleControlled() {
			controlled = !controlled;
		}
		
		public double[] getAudioData(double[] samples) {
			return getAudioData(samples, 1.0);
		}
		
		public double[] getAudioData(double[] samples, double controlValue) {
			double[] returnVal = null;
			double saveCenterFreq = getCenterFreq();
			if(controlled) setCenterFreq(getCenterFreq() * controlValue);
			switch (type) {
			case BANDPASS: 
				returnVal = Filter.variableQBandpass4(samples, getCenterFreq(), getBandwidth() , filterQ);
				for(int apply = 4; apply < order; apply += 4) {
					returnVal = Filter.variableQBandpass4(returnVal, getCenterFreq(), getBandwidth() , filterQ);
				}
				setCenterFreq(saveCenterFreq);
				return returnVal;
			case LOWPASS:
				returnVal = Filter.variableQLowpass2(samples, getCenterFreq(), filterQ);
				for(int apply = 2; apply < order; apply += 2) {
					returnVal = Filter.variableQLowpass2(returnVal, getCenterFreq(), filterQ);
				}
				setCenterFreq(saveCenterFreq);
				return returnVal;
			case HIGHPASS:
				returnVal = Filter.variableQHighpass2(samples, getCenterFreq(), filterQ);
				for(int apply = 2; apply < order; apply += 2) {
					returnVal = Filter.variableQHighpass2(returnVal, getCenterFreq(), filterQ);
				}
				setCenterFreq(saveCenterFreq);
				return returnVal;
			}
			return null;
		}
	}
	
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
	
	public SpectrumEQ(ModuleEditor parent, int x, int y) {
		this.cornerX = x;
		this.cornerY = y;
		this.parent = parent;
		outputs = new ArrayList<Integer>();
		inputs = new ArrayList<Integer>();
		init();
	}
	
	public void initCriticalBands() {
		eqBands = new ArrayList<EQBand>();
		for(CriticalBand criticalBand: Filter.calculateCriticalBands(minFreqInHz, maxFreqInHz, 2.0)) {
			eqBands.add(new EQBand(criticalBand.getLowerBound(), criticalBand.getUpperBound()));
		}
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
	
	public ModuleEditor getParent() {
		return parent;
	}
	
	public double[] getSamplesLeft(HashSet<Integer> waitingForModuleIDs) {
		return null;
	}
	
	public double[] getSamplesRight(HashSet<Integer> waitingForModuleIDs) {
		return null;
	}

	public double[] masterGetSamples(HashSet<Integer> waitingForModuleIDs, double[] control) {
		return masterGetSamples(waitingForModuleIDs, control, false);
	}
	
	public double[] masterGetSamples(HashSet<Integer> waitingForModuleIDs, double[] control, boolean returnInput) {
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
		if(returnInput) return inputSamples;
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
			double controlValue = control[start];
			double[] samples = getFilteredSamples(input, controlValue);
			for(int index = 0; index < samples.length; index++) {
				int returnIndex = start + index;
				if(returnIndex == returnVal.length) return returnVal;
				returnVal[returnIndex] += samples[index];
			}
		}
		return returnVal;
	}
	
	private double[] getFilteredSamples(double[] input, double controlValue) {
		boolean additive = false;
		double[] returnVal = new double[input.length];
		double[] filteredInput = null;
		for(EQBand eqBand: eqBands) {
			if(!eqBand.isSubtractive()) continue;
			if(filteredInput == null) {
				filteredInput = eqBand.getAudioData(input, controlValue);
			} else {
				filteredInput = eqBand.getAudioData(filteredInput, controlValue);
			}
		}
		if(filteredInput == null) filteredInput = input;
		for(int index = 0; index < input.length; index++) returnVal[index] = 0.0;
		for(EQBand eqBand: eqBands) {
			if(eqBand.isSubtractive()) continue;
			additive = true;
			double[] filtered = eqBand.getAudioData(filteredInput, controlValue);
			for(int index = 0; index < input.length; index++) {
				returnVal[index] += filtered[index] * eqBand.gain;
			}
		}
		if(additive) return returnVal;
		return filteredInput;
	}
	
	public void mousePressed(int x, int y) {
		int index = 0;
		for(Integer outputID: outputs) {
			Output output = (Output) parent.connectors.get(outputID);
			if(output.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(outputID);
				System.out.println("SpectrumEQ output: " + index);
				return;
			}
			index++;
		}
		index = 0;
		for(Integer inputID: inputs) {
			Input input = (Input) parent.connectors.get(inputID);
			if(input.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(inputID);
				System.out.println("SpectrumEQ input: " + index);
				return;
			}
			index++;
		}
		parent.viewSpectrumEQEditor(this);
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
		if(g2 != null) g2.drawString("SpectrumEQ", currentX, currentY);
		currentY += yStep;
		if(g2 != null) g2.setColor(Color.GREEN);
		if(g2 != null) g2.drawString("IN: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) inputs.add(parent.addConnector(new Input(this, currentRect)));
		}
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
			eqBands = new ArrayList<EQBand>();
			int numBands = new Integer(in.readLine());
			for(int index = 0; index < numBands; index++) {
				FilterType type = FilterType.valueOf(in.readLine());
				double lowerBound = new Double(in.readLine());
				double upperBound = new Double(in.readLine());
				EQBand eqBand = new EQBand(type, lowerBound);
				if(type.equals(FilterType.BANDPASS)) eqBand = new EQBand(lowerBound, upperBound);
				eqBand.setOrder(new Integer(in.readLine()));
				eqBand.setGain(new Double(in.readLine()));
				eqBand.setFilterQ(new Double(in.readLine()));
				eqBand.controlled = new Boolean(in.readLine());
				eqBand.subtractive = new Boolean(in.readLine());
				eqBands.add(eqBand);
			}
		} catch (Exception e) {
			System.out.println("SpectrumEQ: loadModuleInfo: Error reading from file");
		}
		
	}

	public void saveModuleInfo(BufferedWriter out) {
		try { 
			out.write(new Integer(eqBands.size()).toString());
			out.newLine();
			for(EQBand eqBand: eqBands) {
				out.write(eqBand.type.toString());
				out.newLine();
				out.write(new Double(eqBand.getLowerBound()).toString());
				out.newLine();		
				out.write(new Double(eqBand.getUpperBound()).toString());
				out.newLine();
				out.write(new Integer(eqBand.getOrder()).toString());
				out.newLine();
				out.write(new Double(eqBand.getGain()).toString());
				out.newLine();
				out.write(new Double(eqBand.getFilterQ()).toString());
				out.newLine();
				out.write(new Boolean(eqBand.isControlled()).toString());
				out.newLine();
				out.write(new Boolean(eqBand.isSubtractive()).toString());
				out.newLine();
			}
		} catch (Exception e) {
			System.out.println("SpectrumEQ.saveModuleInfo: Error saving to file");
		}
		
	}

	public ModuleType getModuleType() {
		// TODO Auto-generated method stub
		return Module.ModuleType.SPECTRUM_EQ;
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
