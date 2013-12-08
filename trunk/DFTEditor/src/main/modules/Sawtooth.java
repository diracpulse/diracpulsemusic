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

public class Sawtooth implements Module {
	
	ModuleEditor parent = null;
	Long moduleID = null;
	double amplitude = 0.0;
	double freqInHz = 0.0;
	double duration = 0.0;
	int width = 200; // should be >= value calculated by init
	int height = 200; // calculated by init
	
	Rectangle freqControl = null;
	Rectangle ampControl = null;
	Rectangle durationControl = null;
	ArrayList<Output> outputs;
	ArrayList<Input> inputADD;
	ArrayList<Input> inputAM;
	ArrayList<Input> inputFM;
	HashMap<Integer, Long> outputToModuleID = null;
	HashMap<Integer, Long> inputAddToModuleID = null;
	
	private class Input extends Module.Input {

		public Input(Module parent, Rectangle selectArea, Long connectionID) {
			super(parent, selectArea, connectionID);
			// TODO Auto-generated constructor stub
		}
		
		public void inputSamples(double[] samples) {
		}
		
	}
	
	private class Output extends Module.Output {

		public Output(Module parent, Rectangle selectArea, Long connectionID) {
			super(parent, selectArea, connectionID);
			// TODO Auto-generated constructor stub
		}
		
		public double[] outputSamples() {
			return null;
		}
		
	}
	
	public Sawtooth(ModuleEditor parent, int x, int y, double freqInHz, TAPair durationAndAmplitude) {
		this.moduleID = ModuleEditor.randomGenerator.nextLong();
		this.parent = parent;
		this.freqInHz = freqInHz;
		amplitude = durationAndAmplitude.getAbsoluteAmplitude();
		duration = durationAndAmplitude.getTimeInSeconds();
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
	
	public long getModuleId() {
		return moduleID;
	}
	
	public double[] getSamplesLeft(HashSet<Long> moduleIDsWaiting) {
		return null;
	}
	
	public double[] getSamplesRight(HashSet<Long> moduleIDsWaiting) {
		return null;
	}

	public double[] getSamples(HashSet<Long> moduleIDsWaiting) {
		if(moduleIDsWaiting == null) moduleIDsWaiting = new HashSet<Long>();
		if(moduleIDsWaiting.contains(moduleID)) {
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
			samplesAM[index] = 1.0; // 1.0 if no AM data
			samplesADD[index] = 0.0;
		}
		for(Input input: inputFM) {
			ArrayList<double[]> samplesFMArray = new ArrayList<double[]>();
			if(input.getConnection() != null) {
				moduleIDsWaiting.add(moduleID);
				samplesFMArray.add(parent.connectorIDToConnector.get(input.getConnection()).getParent().getSamples(moduleIDsWaiting));
				moduleIDsWaiting.remove(moduleID);
			}
			for(double[] samplesFMIn: samplesFMArray) {
				for(int index = 0; index < numSamples; index++) {
					if(index >= samplesFMIn.length) break;
					samplesFM[index] += samplesFMIn[index]; 
				}
			}
		}
		for(Input input: inputAM) {
			ArrayList<double[]> samplesAMArray = new ArrayList<double[]>();
			if(input.getConnection() != null) {
				moduleIDsWaiting.add(moduleID);
				samplesAMArray.add(parent.connectorIDToConnector.get(input.getConnection()).getParent().getSamples(moduleIDsWaiting));
				moduleIDsWaiting.remove(moduleID);
			}
			for(double[] samplesAMIn: samplesAMArray) {
				for(int index = 0; index < numSamples; index++) {
					// need to change if there is AM data present
					samplesAM[index] = 0.0;
					if(index >= samplesAMIn.length) continue;
					samplesAM[index] += samplesAMIn[index]; 
				}
			}
		}
		for(Input input: inputADD) {
			ArrayList<double[]> samplesADDArray = new ArrayList<double[]>();
			if(input.getConnection() != null) {
				moduleIDsWaiting.add(moduleID);
				samplesADDArray.add(parent.connectorIDToConnector.get(input.getConnection()).getParent().getSamples(moduleIDsWaiting));
				moduleIDsWaiting.remove(moduleID);
			}
			for(double[] samplesADDIn: samplesADDArray) {
				for(int index = 0; index < numSamples; index++) {
					if(index >= samplesADDIn.length) break;
					samplesADD[index] += samplesADDIn[index]; 
				}
			}
		}
		double deltaPhase = freqInHz / SynthTools.sampleRate * Math.PI * 2.0;
		double phase = 0;
		for(int index = 0; index < numSamples; index++) {
			returnVal[index] = Math.sin(phase + samplesFM[index]) * samplesAM[index] * amplitude + samplesADD[index];
			phase += deltaPhase;
		}
		return returnVal;
	}
	
	public void mousePressed(int x, int y) {
		if(freqControl.contains(x, y)) {
			System.out.println("Sawtooth: Freq Control");
		}
		if(ampControl.contains(x, y)) {
			System.out.println("Sawtooth: Amp Control");
		}
		if(durationControl.contains(x, y)) {
			System.out.println("Sawtooth: Duration Control");
		}	
		int index = 0;
		for(Output output: outputs) {
			if(output.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(output.getConnectorID());
				System.out.println("Sawtooth: output: " + index);
			}
			index++;
		}
		index = 0;
		for(Input inputADDval: inputADD) {
			if(inputADDval.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(inputADDval.getConnectorID());
				System.out.println("Sawtooth: inputADD: " + index);
			}
			index++;
		}
		index = 0;
		for(Input inputAMval: inputAM) {
			if(inputAMval.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(inputAMval.getConnectorID());
				System.out.println("Sawtooth: inputAM: " + index);
			}
			index++;
		}
		index = 0;
		for(Input inputFMval: inputFM) {
			if(inputFMval.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(inputFMval.getConnectorID());
				System.out.println("Sawtooth: inputFM: " + index);
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
		if(g2 != null) g2.drawString("SAWTOOTH", currentX, currentY);
		if(g2 != null) g2.setColor(Color.GREEN);
		currentY += yStep;
		if(g2 != null) g2.drawString("Frequency: " + freqInHz, currentX, currentY);
		if(g2 == null) freqControl = new Rectangle(currentX, currentY - fontSize, width, fontSize);
		currentY += yStep;
		if(g2 != null) g2.drawString("Amplitude: " + amplitude, currentX, currentY);
		if(g2 == null) ampControl = new Rectangle(currentX, currentY - fontSize, width, fontSize);
		currentY += yStep;
		if(g2 != null) g2.drawString("Duration: " + duration, currentX, currentY);
		if(g2 == null) durationControl = new Rectangle(currentX, currentY - fontSize, width, fontSize);
		if(g2 != null) g2.setColor(Color.RED);
		currentY += yStep;
		if(g2 != null) g2.drawString("ADD: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) inputADD.add(new Input(this, currentRect, ModuleEditor.randomGenerator.nextLong()));
		}
		currentY += yStep;
		if(g2 != null) g2.drawString("AM: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) inputAM.add(new Input(this, currentRect, ModuleEditor.randomGenerator.nextLong()));
		}
		currentY += yStep;
		if(g2 != null) g2.drawString("FM: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) inputFM.add(new Input(this, currentRect, ModuleEditor.randomGenerator.nextLong()));
		}
		if(g2 != null) g2.setColor(Color.BLUE);
		currentY += yStep;
		if(g2 != null) g2.drawString("OUT: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) outputs.add(new Output(this, currentRect, ModuleEditor.randomGenerator.nextLong()));
		}
		if(g2 == null) height = currentY + 6 - y;
		if(g2 == null) width = height;
	}
}
