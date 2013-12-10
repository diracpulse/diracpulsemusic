
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

public class MasterInput implements Module {
	
	ModuleEditor parent = null;
	Long moduleID = null;
	int width = 1000;
	int height = 200; // calculated by init
	
	ArrayList<Input> inputLeft;
	ArrayList<Input> inputRight;
	
	private class Input extends Module.Input {

		public Input(Module parent, Rectangle selectArea, Long connectionID) {
			super(parent, selectArea, connectionID);
			// TODO Auto-generated constructor stub
		}
		
	}
	
	public MasterInput(ModuleEditor parent, int x, int y) {
		this.moduleID = ModuleEditor.randomGenerator.nextLong();
		this.parent = parent;
		inputLeft = new ArrayList<Input>();
		inputRight = new ArrayList<Input>();
		init(x, y);
		for(Input input: inputLeft) {
			parent.addInput(input);
		}
		for(Input input: inputRight) {
			parent.addInput(input);
		}
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public long getModuleId() {
		return moduleID;
	}
	
	public double[] getSamplesLeft() {
		int numSamples = 0;
		double[] leftOut = new double[0];
		ArrayList<double[]> samplesLeftArray = new ArrayList<double[]>();
		for(Input input: inputLeft) {
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
		for(Input input: inputRight) {
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
		for(Input inputLeftVal: inputLeft) {
			if(inputLeftVal.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(inputLeftVal.getConnectorID());
				System.out.println("Master Input: inputLeft: " + index);
			}
			index++;
		}
		index = 0;
		for(Input inputRightVal: inputRight) {
			if(inputRightVal.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(inputRightVal.getConnectorID());
				System.out.println("Master Input: inputRight: " + index);
			}
			index++;
		}
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
		if(g2 != null) g2.drawString("Master Input", currentX, currentY);
		if(g2 != null) g2.setColor(Color.BLACK);
		currentY += yStep;
		if(g2 != null) g2.drawString("LEFT: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) inputLeft.add(new Input(this, currentRect, ModuleEditor.randomGenerator.nextLong()));
		}
		currentY += yStep;
		if(g2 != null) g2.setColor(Color.RED);		
		if(g2 != null) g2.drawString("RIGHT: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) inputRight.add(new Input(this, currentRect, ModuleEditor.randomGenerator.nextLong()));
		}
		if(g2 == null) height = currentY + 6 - y;
	}
}
