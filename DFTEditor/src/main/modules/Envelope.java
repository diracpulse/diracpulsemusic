
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
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import main.FDData;
import main.Interpolate;
import main.Module;
import main.ModuleEditor;
import main.TestSignals.Generator;
import main.TestSignals.TAPair;
import main.modules.BasicWaveform.WaveformType;
import main.SynthTools;

public class Envelope implements Module {
	
	private enum InterpolationType {
		LINEAR,
		CUBICSPLINE,
		LOGLINEAR,
	}
	
	ModuleEditor parent = null;
	Integer moduleID = null;
	int width = 150;
	int height = 300; // calculated by init
	int cornerX;
	int cornerY;
	int numPoints = 3;
	int minPoints = 2;
	int maxPoints = 6;
	double defaultTimeInMillis = 20.0;
	double defaultAmplitude = 1.0;
	String name = "Envelope";

	ArrayList<Rectangle> timesControl;
	ArrayList<Rectangle> amplitudesControl;
	Rectangle interpolationControl;
	Rectangle numPointsControl;
	double[] timeValuesInMillis = null;
	double[] amplitudeValues = null;
	InterpolationType iType = InterpolationType.LINEAR;
	double minTimeInMillis = 0.0;
	double maxTimeInMillis = ModuleEditor.maxDuration * 1000.0;
	
	ArrayList<Integer> triggers = null;
	ArrayList<Integer> outputs = null;
	
	private class Input extends Module.Input {

		public Input(Module parent, Rectangle selectArea) {
			super(parent, selectArea);
			// TODO Auto-generated constructor stub
		}
		
	}

	private class Output extends Module.Output {

		private double[] calculatedSamples;
		
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
	
	public Envelope(ModuleEditor parent, int x, int y) {
		this.cornerX = x;
		this.cornerY = y;
		this.parent = parent;
		outputs = new ArrayList<Integer>();
		triggers = new ArrayList<Integer>();
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
	
	public double[] masterGetSamples(HashSet<Integer> waitingForModuleIDs, double[] control) {
		if(control == null) {
			double[] samples = new double[0];
			double currentTime = 0.0;
			ArrayList<Interpolate.TAPair> TAPairs = new ArrayList<Interpolate.TAPair>();
			TAPairs.add(new Interpolate.TAPair(0.0, 0.0));
			for(int index = 0; index < numPoints - 1; index++) {
				currentTime += timeValuesInMillis[index] / 1000.0;
				TAPairs.add(new Interpolate.TAPair(currentTime, amplitudeValues[index]));
			}
			currentTime += timeValuesInMillis[numPoints - 1] / 1000.0;
			TAPairs.add(new Interpolate.TAPair(currentTime, 0.0));
			if(iType == InterpolationType.LINEAR) samples = Interpolate.synthTAPairsLinear(TAPairs);
			if(iType == InterpolationType.LOGLINEAR) samples = Interpolate.synthTAPairsLog(TAPairs);
			if(iType == InterpolationType.CUBICSPLINE) samples = Interpolate.synthTAPairsCubicSpline(TAPairs);
			System.out.println("Envelope " + samples.length);
			return samples;
		} else {
			double[] returnVal = new double[control.length];
			for(int index = 0; index < returnVal.length; index++) {
				returnVal[index] = 0.0;
			}
			TreeMap<Integer, Integer> startToEnd = new TreeMap<Integer, Integer>();
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
				double[] envelope = synthSingleEnvelope(startToEnd.get(start) - start);
				if(envelope == null) continue;
				for(int index = 0; index < envelope.length; index++) {
					int returnIndex = start + index;
					if(returnIndex == returnVal.length) return returnVal;
					returnVal[returnIndex] += envelope[index];
				}
			}
			return returnVal;
		}
	}
	
	public double[] synthSingleEnvelope(int length) {
		double envelopeLengthWithoutDecay = length / SynthTools.sampleRate - timeValuesInMillis[numPoints - 1] / 1000.0;
		if(envelopeLengthWithoutDecay <= 0.0) return getDefaultEnvelope(length);
		double currentTimeOffset = 0.0;
		ArrayList<Interpolate.TAPair> TAPairs = new ArrayList<Interpolate.TAPair>();
		TAPairs.add(new Interpolate.TAPair(currentTimeOffset, 0.0));
		for(int index = 0; index < numPoints - 2; index++) {
			currentTimeOffset += timeValuesInMillis[index] / 1000.0;
			TAPairs.add(new Interpolate.TAPair(currentTimeOffset, amplitudeValues[index]));
		}
		currentTimeOffset = TAPairs.get(TAPairs.size() - 1).getTimeInSeconds();
		if(currentTimeOffset > envelopeLengthWithoutDecay) return getDefaultEnvelope(length);
		if(timeValuesInMillis[numPoints - 2] == 0.0) {
			// NO SUSTAIN
			TAPairs.add(new Interpolate.TAPair(currentTimeOffset + timeValuesInMillis[numPoints - 1] / 1000.0, 0.0));
		} else {
			// SUSTAIN
			TAPairs.add(new Interpolate.TAPair(envelopeLengthWithoutDecay, amplitudeValues[numPoints - 1]));
			TAPairs.add(new Interpolate.TAPair(envelopeLengthWithoutDecay + timeValuesInMillis[numPoints - 1] / 1000.0, 0.0));
		}
		if(iType == InterpolationType.LINEAR) return Interpolate.synthTAPairsLinear(TAPairs);
		if(iType == InterpolationType.LOGLINEAR) return Interpolate.synthTAPairsLog(TAPairs);
		if(iType == InterpolationType.CUBICSPLINE) return Interpolate.synthTAPairsCubicSpline(TAPairs);
		return null;
	}
	
	public double[] getDefaultEnvelope(int length) {
		double envelopeLength = length / SynthTools.sampleRate;
		ArrayList<Interpolate.TAPair> TAPairs = new ArrayList<Interpolate.TAPair>();
		TAPairs.add(new Interpolate.TAPair(0.0, 0.0));
		TAPairs.add(new Interpolate.TAPair(envelopeLength / 2.0, amplitudeValues[0]));
		TAPairs.add(new Interpolate.TAPair(envelopeLength, 0.0));
		return Interpolate.synthTAPairsLinear(TAPairs);
	}
	
	public void mousePressed(int x, int y) {
		int index = 0;
		for(Integer outputID: outputs) {
			Output output = (Output) parent.connectorIDToConnector.get(outputID);
			if(output.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(output.getConnectorID());
				System.out.println(name + " " + "outputs: " + index);
			}
			index++;
		}
		index = 0;
		for(Rectangle time: timesControl) {
			if(time.contains(x, y)) {
				System.out.println(name + " Time Control " + index);
				Double timeVal = getInput("Enter time in milliseconds", minTimeInMillis, maxTimeInMillis);
				if(timeVal == null) return;
				timeValuesInMillis[index] = timeVal;
				parent.refreshView();
				return;
			}
			index++;
		}
		index = 0;
		for(Rectangle amplitude: amplitudesControl) {
			if(amplitude.contains(x, y)) {
				System.out.println(name + " Amplitude Control " + index);
				Double amplitudeVal = getInput("Enter amplitude in dB", ModuleEditor.minAmplitudeIn_dB, ModuleEditor.maxAmplitudeIn_dB);
				if(amplitudeVal == null) return;
				amplitudeValues[index] = Math.pow(10.0, amplitudeVal / 20.0);
				parent.refreshView();
				return;
			}
			index++;
		}
		if(interpolationControl.contains(x, y)) {
			InterpolationType inputType = (InterpolationType) JOptionPane.showInputDialog(null, "Choose a type", "Type Select", JOptionPane.INFORMATION_MESSAGE, null, InterpolationType.values(),  InterpolationType.LINEAR);
			if(inputType == null) return;
			iType = inputType;
			parent.refreshView();
			return;
		}
		if(numPointsControl.contains(x, y)) {
			Integer numPointsVal = getInput("Enter number of points to use", minPoints, maxPoints);
			if(numPointsVal == null) return;
			numPoints = numPointsVal;
			parent.refreshView();
			return;
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
		if(g2 != null) g2.drawString(name, currentX, currentY);
		if(g2 != null) g2.setColor(Color.GREEN);
		currentY += yStep;
		if(g2 != null) g2.drawString("Points: " + numPoints, currentX, currentY);
		if(g2 == null) numPointsControl = new Rectangle(currentX, currentY - fontSize, width, fontSize);
		currentY += yStep;
		if(g2 == null) {
			timesControl = new ArrayList<Rectangle>();
			timeValuesInMillis = new double[maxPoints];
		}
		for(int index = 0; index < maxPoints; index++) {
			if(g2 != null) {
				g2.setColor(Color.GREEN);
				if(index >= numPoints) g2.setColor(Color.GRAY);
				g2.drawString("Time[" + index + "] = " + timeValuesInMillis[index] + " ms", currentX, currentY);
			}
			if(g2 == null) {
				timesControl.add(new Rectangle(currentX, currentY - fontSize, width, fontSize));
				timeValuesInMillis[index] = defaultTimeInMillis;
			}
			currentY += yStep;
		}
		if(g2 == null) {
			amplitudesControl = new ArrayList<Rectangle>();
			amplitudeValues = new double[maxPoints - 1];
		}
		for(int index = 0; index < maxPoints - 1; index++) {
			if(g2 != null) {
				g2.setColor(Color.GREEN);
				if(index >= numPoints - 1) g2.setColor(Color.GRAY);
				g2.drawString("Amplitude[" + index + "] = " + Math.round(Math.log(amplitudeValues[index])/Math.log(10.0) * 20.0 * 1000.0) / 1000.0 + " dB", currentX, currentY);
			}
			if(g2 == null) {
				amplitudesControl.add(new Rectangle(currentX, currentY - fontSize, width, fontSize));
				amplitudeValues[index] = defaultAmplitude;
			}
			currentY += yStep;
		}
		if(g2 != null) g2.setColor(Color.GREEN);
		if(g2 != null) g2.drawString("Interpolation: " + iType, currentX, currentY);
		if(g2 == null) interpolationControl = new Rectangle(currentX, currentY - fontSize, width, fontSize);
		if(g2 != null) g2.setColor(Color.YELLOW);
		currentY += yStep;
		if(g2 != null) g2.setColor(Color.BLUE);		
		if(g2 != null) g2.drawString("OUT: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) outputs.add(parent.addConnector(new Output(this, currentRect)));
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
		//System.out.println("env" + currentY);
	}

	@Override
	public void loadModuleInfo(BufferedReader in) {
		try { 
			numPoints = new Integer(in.readLine());
			for(int index = 0; index < maxPoints; index++) {
				Double timeValueInMillisIn = new Double(in.readLine());
				timeValuesInMillis[index] = timeValueInMillisIn.doubleValue();
			}
			for(int index = 0; index < maxPoints - 1; index++) {
				Double amplitudeIn = new Double(in.readLine());
				amplitudeValues[index] = amplitudeIn.doubleValue();
			}
			String currentLine = in.readLine();
			this.iType = InterpolationType.valueOf(currentLine);
		} catch (Exception e) {
			System.out.println("Envelope.loadModuleInfo: Error reading from file");
		}
	}

	@Override
	public void saveModuleInfo(BufferedWriter out) {
		try { 
			out.write(new Integer(numPoints).toString());
			out.newLine();
			for(int index = 0; index < maxPoints; index++) {
				out.write(new Double(timeValuesInMillis[index]).toString());
				out.newLine();
			}
			for(int index = 0; index < maxPoints - 1; index++) {
				out.write(new Double(amplitudeValues[index]).toString());
				out.newLine();
			}
			out.write(iType.toString());
			out.newLine();
		} catch (Exception e) {
			System.out.println("Envelope.saveModuleInfo: Error saving to file");
		}
	}

	@Override
	public ModuleType getModuleType() {
		// TODO Auto-generated method stub
		return ModuleType.ENVELOPE;
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
