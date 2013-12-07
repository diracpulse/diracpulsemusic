package main.generators;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.HashMap;

import main.FDData;
import main.Module;
import main.TestSignals.Generator;
import main.TestSignals.TAPair;
import main.SynthTools;

public class Sawtooth implements Generator, Module {
	
	double[] samples = null;
	double amplitude = 0.0;
	double freqInHz = 0.0;
	double duration = 0.0;
	int width = 200; // should be >= value calculated by init
	int height = 200; // calculated by init
	
	Rectangle freqControl = null;
	Rectangle ampControl = null;
	Rectangle durationControl = null;
	ArrayList<Rectangle> outputs;
	ArrayList<Rectangle> inputADD;
	ArrayList<Rectangle> inputAM;
	ArrayList<Rectangle> inputFM;
	HashMap<Integer, Long> outputToModuleID = null;
	HashMap<Integer, Long> inputAddToModuleID = null;
	
	
	public Sawtooth(int x, int y, double freqInHz, TAPair durationAndAmplitude) {
		this.freqInHz = freqInHz;
		amplitude = durationAndAmplitude.getAbsoluteAmplitude();
		duration = durationAndAmplitude.getTimeInSeconds();
		outputs = new ArrayList<Rectangle>();
		inputADD = new ArrayList<Rectangle>();
		inputAM = new ArrayList<Rectangle>();
		inputFM = new ArrayList<Rectangle>();
		init(x, y);
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
		
	
	
	public double[] getSamples() {
		samples = new double[(int) Math.round(duration * SynthTools.sampleRate)];
		double deltaPhase = freqInHz / SynthTools.sampleRate * Math.PI;
		double phase = 0;
		for(int index  = 0; index < samples.length; index++) {
			samples[index] = Math.sin(phase) * amplitude;
			phase += deltaPhase;
		}
		return samples;
	}
	
	public double[] addTo(double[] input) {
		double[] returnVal = null;
		if(input.length > samples.length) {
			returnVal = new double[input.length];
		} else {
			returnVal = new double[samples.length];
		}
		for(int index = 0; index < returnVal.length; index++) {
			if(index >= samples.length || index >= input.length) {
				returnVal[index] = 0.0;
				continue;
			}
			returnVal[index] = samples[index] + input[index];
		}
		return returnVal;
	}
	
	public double[] modulateAM(double[] input) {
		double[] returnVal = null;
		if(input.length > samples.length) {
			returnVal = new double[input.length];
		} else {
			returnVal = new double[samples.length];
		}
		for(int index = 0; index < returnVal.length; index++) {
			if(index >= samples.length || index >= input.length) {
				returnVal[index] = 0.0;
				continue;
			}
			returnVal[index] = samples[index] * input[index];
		}
		return returnVal;
	}
	
	public double[] modulateFM(double[] input) {
		double[] returnVal = null;
		if(input.length > samples.length) {
			returnVal = new double[input.length];
		} else {
			returnVal = new double[samples.length];
		}
		double deltaPhase = freqInHz / SynthTools.sampleRate * Math.PI;
		double phase = 0;
		for(int index  = 0; index < returnVal.length; index++) {
			returnVal[index] = Math.sin(phase + input[index]) * amplitude;
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
		for(Rectangle output: outputs) {
			if(output.contains(x, y)) {
				System.out.println("Sawtooth: output: " + index);
			}
			index++;
		}
		index = 0;
		for(Rectangle inputADDval: inputADD) {
			if(inputADDval.contains(x, y)) {
				System.out.println("Sawtooth: inputADD: " + index);
			}
			index++;
		}
		index = 0;
		for(Rectangle inputAMval: inputAM) {
			if(inputAMval.contains(x, y)) {
				System.out.println("Sawtooth: inputAM: " + index);
			}
			index++;
		}
		index = 0;
		for(Rectangle inputFMval: inputFM) {
			if(inputFMval.contains(x, y)) {
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
			if(g2 == null) inputADD.add(currentRect);
		}
		currentY += yStep;
		if(g2 != null) g2.drawString("AM: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) inputAM.add(currentRect);
		}
		currentY += yStep;
		if(g2 != null) g2.drawString("FM: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) inputFM.add(currentRect);
		}
		if(g2 != null) g2.setColor(Color.BLUE);
		currentY += yStep;
		if(g2 != null) g2.drawString("OUT: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) outputs.add(currentRect);
		}
		if(g2 == null) height = currentY + 6 - y;
		if(g2 == null) width = height;
	}
}
