
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
import main.Module;
import main.ModuleEditor;

public class StereoPan implements Module {
	
	ModuleEditor parent = null;
	Integer moduleID = null;
	int width = 150;
	int height = 150; // calculated by init
	int cornerX;
	int cornerY;
	String name = "Stereo Pan";
	
	ArrayList<Integer> inputs;
	ArrayList<Integer> controls;
	ArrayList<Integer> outputsLeft;
	ArrayList<Integer> outputsRight;
	
	private class Input extends Module.Input {

		public Input(Module parent, Rectangle selectArea) {
			super(parent, selectArea);
			// TODO Auto-generated constructor stub
		}
		
	}
	
	private class OutputLeft extends Module.Output {

		private double[] calculatedSamples = null;
		
		public OutputLeft(Module parent, Rectangle selectArea) {
			super(parent, selectArea);
			// TODO Auto-generated constructor stub
		}

		@Override
		public double[] getSamples(HashSet<Integer> waitingForModuleIDs, double[] control) {
			if(calculatedSamples != null) return calculatedSamples;
			calculatedSamples = getSamplesLeft(waitingForModuleIDs, control);
			return calculatedSamples;
		}
		
		public void clearSamples() {
			calculatedSamples = null;
		}
	}
	
	private class OutputRight extends Module.Output {
		
		private double[] calculatedSamples = null;


		public OutputRight(Module parent, Rectangle selectArea) {
			super(parent, selectArea);
			// TODO Auto-generated constructor stub
		}

		@Override
		public double[] getSamples(HashSet<Integer> waitingForModuleIDs, double[] control) {
			if(calculatedSamples != null) return calculatedSamples;
			calculatedSamples = getSamplesRight(waitingForModuleIDs, control);
			return calculatedSamples;
		}
		
		public void clearSamples() {
			calculatedSamples = null;
		}
	}
	
	public StereoPan(ModuleEditor parent, int x, int y) {
		this.cornerX = x;
		this.cornerY = y;
		this.parent = parent;
		inputs = new ArrayList<Integer>();
		controls = new ArrayList<Integer>();
		outputsLeft = new ArrayList<Integer>();
		outputsRight = new ArrayList<Integer>();
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
	
	public double[] getSamplesLeft(HashSet<Integer> waitingForModuleIDs, double[] control) {
		double[] inputArray = getInputSum(inputs, waitingForModuleIDs, control);
		double[] controlsArray = getInputSum(controls, waitingForModuleIDs, control);
		double[] returnVal = new double[inputArray.length];
		for(int index = 0; index < inputArray.length; index++) {
			if(index >= controlsArray.length) {
				returnVal[index] = inputArray[index];
				continue;
			}
			if(controlsArray[index] <= 0.0) {
				returnVal[index] = inputArray[index];
				continue;
			}
			if(controlsArray[index] >= 1.0) {
				returnVal[index] = 0.0;
				continue;
			}
			returnVal[index] = (1.0 - controlsArray[index]) * inputArray[index];
		}
		return returnVal;
	}
	
	public double[] getSamplesRight(HashSet<Integer> waitingForModuleIDs, double[] control) {
		double[] inputArray = getInputSum(inputs, waitingForModuleIDs, control);
		double[] controlsArray = getInputSum(controls, waitingForModuleIDs, control);
		double[] returnVal = new double[inputArray.length];
		for(int index = 0; index < inputArray.length; index++) {
			if(index >= controlsArray.length) {
				returnVal[index] = inputArray[index];
				continue;
			}
			if(controlsArray[index] >= 0.0) {
				returnVal[index] = inputArray[index];
				continue;
			}
			if(controlsArray[index] <= -1.0) {
				returnVal[index] = 0.0;
				continue;
			}
			returnVal[index] = (controlsArray[index] + 1.0) * inputArray[index];
		}
		return returnVal;
	}
	
	public double[] getInputSum(ArrayList<Integer> inputs, HashSet<Integer> waitingForModuleIDs, double[] control) {
		if(waitingForModuleIDs == null) waitingForModuleIDs = new HashSet<Integer>();
		if(waitingForModuleIDs.contains(moduleID)) {
			//JOptionPane.showMessageDialog(parent.getParentFrame(), "Loop in StereoPan");
			return new double[0];
		}
		int numSamples = 0;
		double[] returnVal = new double[0];
		ArrayList<double[]> inputsArray = new ArrayList<double[]>();
		for(Integer inputID: inputs) {
			Input input = (Input) parent.connectorIDToConnector.get(inputID);
			if(input.getConnection() == null) continue;
			waitingForModuleIDs.add(moduleID);
			Output output = (Output) parent.connectorIDToConnector.get(input.getConnection());
			inputsArray.add(output.getSamples(waitingForModuleIDs, control));
		}
		if(inputsArray.isEmpty()) return returnVal;
		for(double[] inputsLeftIn: inputsArray) {
			if(inputsLeftIn.length > numSamples) numSamples = inputsLeftIn.length;
		}
		returnVal = new double[numSamples];
		for(int index = 0; index < returnVal.length; index++) returnVal[index] = 0.0;
		for(double[] inputsIn: inputsArray) {
			for(int index = 0; index < inputsIn.length; index++) {
				returnVal[index] += inputsIn[index];
			}
		}
		return returnVal;
	}
	
	public void mousePressed(int x, int y) {
		int index = 0;
		for(Integer outputID: outputsLeft) {
			Output output = (Output) parent.connectorIDToConnector.get(outputID);
			if(output.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(output.getConnectorID());
				System.out.println(name + " " + "outputsLeft: " + index);
			}
			index++;
		}
		index = 0;
		for(Integer outputID: outputsRight) {
			Output output = (Output) parent.connectorIDToConnector.get(outputID);
			if(output.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(output.getConnectorID());
				System.out.println(name + " outputsRight: " + index);
			}
			index++;
		}
		index = 0;
		for(Integer inputID: inputs) {
			Input input = (Input) parent.connectorIDToConnector.get(inputID);
			if(input.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(input.getConnectorID());
				System.out.println(name + " inputs: " + index);
			}
			index++;
		}
		index = 0;
		for(Integer controlID: controls) {
			Input control = (Input) parent.connectorIDToConnector.get(controlID);
			if(control.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(control.getConnectorID());
				System.out.println(name + " controls: " + index);
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
		if(g2 != null) g2.drawString(name, currentX, currentY);
		if(g2 != null) g2.setColor(Color.BLACK);
		currentY += yStep;
		if(g2 != null) g2.drawString("LEFT: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) outputsLeft.add(parent.addConnector(new OutputLeft(this, currentRect)));
		}
		currentY += yStep;
		if(g2 != null) g2.setColor(Color.RED);		
		if(g2 != null) g2.drawString("RIGHT: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) outputsRight.add(parent.addConnector(new OutputRight(this, currentRect)));
		}
		currentY += yStep;
		if(g2 != null) g2.setColor(Color.GREEN);		
		if(g2 != null) g2.drawString("CNTRL: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) controls.add(parent.addConnector(new Input(this, currentRect)));
		}
		currentY += yStep;
		if(g2 != null) g2.setColor(Color.YELLOW);		
		if(g2 != null) g2.drawString("INPUT: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) inputs.add(parent.addConnector(new Input(this, currentRect)));
		}
		//if(g2 == null) height = currentY + 6 - y;
	}

	@Override
	public void loadModuleInfo(BufferedReader in) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveModuleInfo(BufferedWriter out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ModuleType getModuleType() {
		return Module.ModuleType.STEREOPAN;
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
