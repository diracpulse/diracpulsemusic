
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

public class MasterInput implements Module {
	
	ModuleEditor parent = null;
	Integer moduleID = null;
	int width = 1000;
	int height = 200; // calculated by init
	int cornerX;
	int cornerY;
	ArrayList<Integer> inputLeft;
	ArrayList<Integer> inputRight;
	
	private class Input extends Module.Input {

		public Input(Module parent, Rectangle selectArea) {
			super(parent, selectArea);
			// TODO Auto-generated constructor stub
		}
		
	}
	
	public MasterInput(ModuleEditor parent, int x, int y) {
		this.cornerX = x;
		this.cornerY = y;
		this.parent = parent;
		inputLeft = new ArrayList<Integer>();
		inputRight = new ArrayList<Integer>();
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
	
	public double[] getSamplesLeft() {
		int numSamples = 0;
		double[] leftOut = new double[0];
		ArrayList<double[]> samplesLeftArray = new ArrayList<double[]>();
		for(Integer inputID: inputLeft) {
			Input input = (Input) parent.connectorIDToConnector.get(inputID);
			if(input.getConnection() == null) continue;
			Output output = (Output) parent.connectorIDToConnector.get(input.getConnection());
			samplesLeftArray.add(output.getSamples(null));
		}
		if(samplesLeftArray.isEmpty()) return leftOut;
		for(double[] samplesLeftIn: samplesLeftArray) {
			if(samplesLeftIn.length > numSamples) numSamples = samplesLeftIn.length;
		}
		leftOut = new double[numSamples];
		for(int index = 0; index < leftOut.length; index++) leftOut[index] = 0.0;
		for(double[] samplesLeftIn: samplesLeftArray) {
			for(int index = 0; index < samplesLeftIn.length; index++) {
				leftOut[index] += samplesLeftIn[index];
			}
		}
		return leftOut;
	}
	
	public double[] getSamplesRight() {
		int numSamples = 0;
		double[] rightOut = new double[0];
		ArrayList<double[]> samplesRightArray = new ArrayList<double[]>();
		for(Integer inputID: inputRight) {
			Input input = (Input) parent.connectorIDToConnector.get(inputID);
			if(input.getConnection() == null) continue;
			Output output = (Output) parent.connectorIDToConnector.get(input.getConnection());
			samplesRightArray.add(output.getSamples(null));
		}
		if(samplesRightArray.isEmpty()) return rightOut;
		for(double[] samplesRightIn: samplesRightArray) {
			if(samplesRightIn.length > numSamples) numSamples = samplesRightIn.length;
		}
		rightOut = new double[numSamples];
		for(int index = 0; index < rightOut.length; index++) rightOut[index] = 0.0;
		for(double[] samplesRightIn: samplesRightArray) {
			for(int index = 0; index < samplesRightIn.length; index++) {
				rightOut[index] += samplesRightIn[index];
			}
		}
		return rightOut;
	}

	public void mousePressed(int x, int y) {
		int index = 0;
		for(Integer inputID: inputLeft) {
			Input input = (Input) parent.connectorIDToConnector.get(inputID);
			if(input.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(input.getConnectorID());
				System.out.println("Master Input: inputLeft: " + index);
			}
			index++;
		}
		index = 0;
		for(Integer inputID: inputRight) {
			Input input = (Input) parent.connectorIDToConnector.get(inputID);
			if(input.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(input.getConnectorID());
				System.out.println("Master Input: inputRight: " + index);
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
		int xStep = yStep;
		if(g2 != null) g2.setColor(Color.WHITE);
		Font font = new Font(Font.SANS_SERIF, Font.BOLD, fontSize);
		if(g2 != null) g2.setFont(font);
		currentX = cornerX + 4;
		currentY = cornerY + yStep;
		if(g2 != null) g2.drawString("Master Input", currentX, currentY);
		if(g2 != null) g2.setColor(Color.BLACK);
		currentY += yStep;
		if(g2 != null) g2.drawString("LEFT: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) inputLeft.add(parent.addConnector(new Input(this, currentRect)));
		}
		currentY += yStep;
		if(g2 != null) g2.setColor(Color.RED);		
		if(g2 != null) g2.drawString("RIGHT: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) inputRight.add(parent.addConnector(new Input(this, currentRect)));
		}
		if(g2 == null) height = currentY + 6 - cornerY;
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
		return Module.ModuleType.MASTERINPUT;
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
