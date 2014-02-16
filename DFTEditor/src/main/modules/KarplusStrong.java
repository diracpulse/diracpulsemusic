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
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JOptionPane;

import main.Module;
import main.ModuleEditor;
import main.SynthTools;
import main.Module.ModuleType;
import main.modules.BasicWaveform.WaveformType;

public class KarplusStrong implements Module {

	public enum AlgorithmType {
		STRING,
		A,
		B;
	}
	
	ModuleEditor parent = null;
	Integer moduleID = null;
	double duration = ModuleEditor.maxDuration;
	int cornerX;
	int cornerY;
	int width = 150; // should be >= value calculated by init
	int height = 150; // calculated by init
	int p = 100;
	double a = 0.5;
	double b = 0.5;
	AlgorithmType type = AlgorithmType.STRING;
	Rectangle typeControl = null;
	Rectangle pControl = null;
	Rectangle aControl = null;
	Rectangle bControl = null;
	ArrayList<Integer> outputs;
	
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

	public KarplusStrong(ModuleEditor parent, int x, int y) {
		this.cornerX = x;
		this.cornerY = y;
		this.parent = parent;
		outputs = new ArrayList<Integer>();
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
			System.out.println("Karplus-Strong has no inputs");
		}
		double[] control = new double[(int) Math.round(duration * SynthTools.sampleRate)];
		if(controlIn != null) {
			control = controlIn;
		} else {
			for(int index = 0; index < control.length; index++) {
				control[index] = 1.0;
			}
		}
		double[] samples = new double[control.length];
		for(int index = 0; index < samples.length; index++) {
			samples[index] = 0.0;
		}
		boolean stop = false;
		int controlIndex = 0;
		while(controlIndex < control.length) {
			if(control[controlIndex] < 0.0) {
				controlIndex++;
				continue;
			}
			stop = false;
			int endInitial = controlIndex + p + 1;
			while(controlIndex < endInitial) {
				if(control[controlIndex] < 0.0) {
					stop = true;
					break;
				}
				samples[controlIndex] = Math.random() - 0.5;
				controlIndex++;
			}
			if(stop) continue;
			while(controlIndex < control.length) {
				if(control[controlIndex] < 0.0) break;
				samples[controlIndex] += 0.5 * (samples[controlIndex - p] + samples[controlIndex - p - 1]);
				controlIndex++;
			}
		}
		return samples;
	}
	
	public void mousePressed(int x, int y) {
		if(typeControl.contains(x, y)) {
			AlgorithmType inputType = (AlgorithmType) JOptionPane.showInputDialog(null, "Choose a type", "Type Select", JOptionPane.INFORMATION_MESSAGE, null, AlgorithmType.values(),  AlgorithmType.STRING);
			if(inputType == null) return;
			type = inputType;
			parent.refreshView();
			return;
		}
		if(pControl.contains(x, y)) {
			Double pInput = getInput("Input delay in samples", 0, ModuleEditor.maxDuration * SynthTools.sampleRate);
			if(pInput == null) return;
			p = (int) Math.round(pInput);
			parent.refreshView();
			return;
		}
		if(aControl.contains(x, y)) {
			Double aInput = getInput("Input a probability between 0.0 and 1.0", 0.0, 1.0);
			if(aInput == null) return;
			a = aInput;
			parent.refreshView();
			return;
		}
		if(bControl.contains(x, y)) {
			Double bInput = getInput("Input a probability between 0.0 and 1.0", 0.0, 1.0);
			if(bInput == null) return;
			b = bInput;
			parent.refreshView();
			return;
		}
		int index = 0;
		for(Integer outputID: outputs) {
			Output output = (Output) parent.connectorIDToConnector.get(outputID);
			if(output.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(outputID);
				System.out.println("Karplus-Strong " + "output: " + index);
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
		if(g2 != null) g2.drawString("Karplus-Strong", currentX, currentY);
		currentY += yStep;
		if(g2 != null) g2.setColor(Color.GREEN);
		if(g2 != null) g2.drawString(type.toString(), currentX, currentY);
		if(g2 == null) typeControl = new Rectangle(currentX, currentY - fontSize, width, fontSize);
		if(g2 != null) g2.setColor(Color.GREEN);
		currentY += yStep;
		currentY += yStep;
		if(g2 != null) g2.drawString("p: " + p, currentX, currentY);
		if(g2 == null) pControl = new Rectangle(currentX, currentY - fontSize, width, fontSize);
		currentY += yStep;
		if(g2 != null) g2.drawString("a: " + a, currentX, currentY);
		if(g2 == null) aControl = new Rectangle(currentX, currentY - fontSize, width, fontSize);
		currentY += yStep;
		if(g2 != null) g2.drawString("b: " + b, currentX, currentY);
		if(g2 == null) bControl = new Rectangle(currentX, currentY - fontSize, width, fontSize);
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
	}

	public void loadModuleInfo(BufferedReader in) {
		try { 
		} catch (Exception e) {
			System.out.println("KarplusStrong.loadModuleInfo: Error reading from file");
		}
		
	}

	public void saveModuleInfo(BufferedWriter out) {
		try { 
		} catch (Exception e) {
			System.out.println("KarplusStrong.loadModuleInfo: Error reading from file");
		}
		
	}

	@Override
	public ModuleType getModuleType() {
		// TODO Auto-generated method stub
		return Module.ModuleType.KARPLUSSTRONG;
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
