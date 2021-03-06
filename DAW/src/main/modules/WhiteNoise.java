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

import main.Filter;
import main.Module;
import main.ModuleEditor;
import main.SynthTools;

public class WhiteNoise implements Module {

	ModuleEditor parent = null;
	Integer moduleID = null;
	int cornerX;
	int cornerY;
	int width = 150; // should be >= value calculated by init
	int height = 150; // calculated by init
	private double[] calculatedSamples = null;
	
	ArrayList<Integer> outputs;
	ArrayList<Integer> inputAM;

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

	public WhiteNoise(ModuleEditor parent, int x, int y) {
		this.cornerX = x;
		this.cornerY = y;
		this.parent = parent;
		outputs = new ArrayList<Integer>();
		inputAM = new ArrayList<Integer>();
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
		boolean nativeOutput = false;
		if(waitingForModuleIDs == null) waitingForModuleIDs = new HashSet<Integer>();
		if(waitingForModuleIDs.contains(moduleID)) {
			//JOptionPane.showMessageDialog(parent.getParentFrame(), "Loop");
			//return new double[0];
			nativeOutput = true;
		}
		double[] samplesAM = new double[control.length];
		for(int index = 0; index < samplesAM.length; index++) {
			if(control[index] > 0.0) {
				samplesAM[index] = 1.0;
			} else {
				samplesAM[index] = 0.0;
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
			for(int index = 0; index < samplesAM.length; index++) samplesAM[index] = 0.0;
		}
		for(double[] samplesAMIn: samplesAMArray) {
			for(int index = 0; index < samplesAM.length; index++) {
				if(index >= samplesAMIn.length) continue;
				samplesAM[index] += samplesAMIn[index]; 
			}
		}
		for(int index = 0; index < samplesAM.length; index++) {
			double random = (Math.random() - 0.5) * 2.0;
			samplesAM[index] *= Math.sin(index * Math.PI * random);
		}
		return samplesAM;
	}
	
	public void mousePressed(int x, int y) {
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
		for(Integer inputID: inputAM) {
			Input input = (Input) parent.connectors.get(inputID);
			if(input.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(inputID);
				System.out.println("SelfModulator inputAM: " + index);
			}
			index++;
		}
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
		if(g2 != null) g2.drawString("WhiteNoise", currentX, currentY);
		if(g2 != null) g2.setColor(Color.GREEN);
		currentY += yStep;
		if(g2 != null) g2.drawString("AM: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) inputAM.add(parent.addConnector(new Input(this, currentRect)));
		}
		currentY += yStep;
		if(g2 != null) g2.drawString("AM: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) inputAM.add(parent.addConnector(new Input(this, currentRect)));
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
	}

	public void loadModuleInfo(BufferedReader in) {
		try { 
		} catch (Exception e) {
			System.out.println("SelfModulator.loadModuleInfo: Error reading from file");
		}
		
	}

	public void saveModuleInfo(BufferedWriter out) {
		try { 
		} catch (Exception e) {
			System.out.println("SelfModulator.loadModuleInfo: Error reading from file");
		}
		
	}

	@Override
	public ModuleType getModuleType() {
		// TODO Auto-generated method stub
		return Module.ModuleType.WHITENOISE;
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
