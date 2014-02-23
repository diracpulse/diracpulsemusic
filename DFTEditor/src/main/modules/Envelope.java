
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
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import main.FDData;
import main.Interpolate;
import main.Interpolate.TAPair;
import main.Module;
import main.ModuleEditor;
import main.MultiWindow;
import main.modules.BasicWaveform.WaveformType;
import main.SynthTools;

public class Envelope implements Module {
	
	private enum InterpolationType {
		LINEAR,
		CUBICSPLINE,
		LOGLINEAR,
	}
	
	private double millisPerPixel = 1.0;
	private double maxSeconds = ModuleEditor.maxDuration;
	private ArrayList<EnvelopePoint> envelopePoints = null;
	private double startSustain = -1.0;
	
	public static class EnvelopePoint {
		
		public double seconds = 0.0;
		public double amplitude = 1.0;
		public InterpolationType type = InterpolationType.LINEAR;
		public double bits = 8;
		
		EnvelopePoint(double seconds, double amplitude, InterpolationType type) {
			this.seconds = seconds;
			this.amplitude = amplitude;
			this.type = type;
		}
		
		EnvelopePoint(double seconds, double amplitude, InterpolationType type, double bits) {
			this.seconds = seconds;
			this.amplitude = amplitude;
			this.type = type;
			this.bits = bits;
		}
		
	}
	
	ModuleEditor parent = null;
	Integer moduleID = null;
	int width = 150;
	int height = 300; // calculated by init
	int cornerX;
	int cornerY;
	int numPoints = 3;

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
		envelopePoints = new ArrayList<EnvelopePoint>();
		envelopePoints.add(new EnvelopePoint(0.0, 0.0, InterpolationType.LOGLINEAR, 2));
		envelopePoints.add(new EnvelopePoint(0.1, 1.0, InterpolationType.LOGLINEAR, 2));
		envelopePoints.add(new EnvelopePoint(0.2, 0.5, InterpolationType.LOGLINEAR, 4));
		startSustain = 0.2;
		envelopePoints.add(new EnvelopePoint(0.5, 0.4, InterpolationType.LOGLINEAR, 6));
		envelopePoints.add(new EnvelopePoint(0.6, 0.0, InterpolationType.LOGLINEAR, 8));
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
	
	public TreeMap<Double, EnvelopePoint> getSecondsToEnvelopePoint() {
		TreeMap<Double, EnvelopePoint> returnVal = new TreeMap<Double, EnvelopePoint>();
		for(EnvelopePoint point: envelopePoints) {
			returnVal.put(point.seconds, point);
		}
		return returnVal;
	}
	
	public TreeSet<Double> getEnvelopeTimes() {
		TreeSet<Double> returnVal = new TreeSet<Double>();
		for(EnvelopePoint point: envelopePoints) {
			returnVal.add(point.seconds);
		}
		return returnVal;
	}
	
	public EnvelopePoint getEnvelopePoint(double time) {
		TreeMap<Double, EnvelopePoint> secondsToEnvelopePoint = getSecondsToEnvelopePoint();
		return secondsToEnvelopePoint.get(time);
	}
	
	public boolean replaceEnvelopePoint(EnvelopePoint oldPoint, EnvelopePoint newPoint) {
		TreeMap<Double, EnvelopePoint> secondsToEnvelopePoint = getSecondsToEnvelopePoint();
		secondsToEnvelopePoint.remove(oldPoint.seconds);
		envelopePoints = new ArrayList<EnvelopePoint>();
		envelopePoints.add(newPoint);
		envelopePoints.addAll(secondsToEnvelopePoint.values());
		return true;
	}

	public double[] masterGetSamples(HashSet<Integer> waitingForModuleIDs, double[] control) {
		if(control == null) {
			return synthSingleEnvelope((int) Math.round(ModuleEditor.maxDuration * SynthTools.sampleRate));
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
		TreeMap<Double, EnvelopePoint> secondsToEnvelopePoint = getSecondsToEnvelopePoint();
		int numSamples = (int) Math.round(secondsToEnvelopePoint.lastKey() * SynthTools.sampleRate) + 1;
		if(numSamples > length) return getDefaultEnvelope(length);
		double[] returnVal = new double[numSamples];
		TreeMap<InterpolationType, double[]> interpolationToSamples = new TreeMap<InterpolationType, double[]>();		
		ArrayList<TAPair> TAPairs = new ArrayList<TAPair>();
		for(double seconds: secondsToEnvelopePoint.keySet()) {
			TAPairs.add(new Interpolate.TAPair(seconds, secondsToEnvelopePoint.get(seconds).amplitude, secondsToEnvelopePoint.get(seconds).bits));
		}
		for(InterpolationType type: InterpolationType.values()) {
			if(type == InterpolationType.LINEAR) interpolationToSamples.put(type, Interpolate.synthTAPairsLinear(TAPairs));
			if(type == InterpolationType.LOGLINEAR) interpolationToSamples.put(type, Interpolate.synthTAPairsLog(TAPairs));
			if(type == InterpolationType.CUBICSPLINE) interpolationToSamples.put(type, Interpolate.synthTAPairsCubicSpline(TAPairs));
		}
		int startSample = 0;
		for(double seconds: secondsToEnvelopePoint.keySet()) {
			int endSample = (int) Math.round(seconds * SynthTools.sampleRate);
			InterpolationType type = secondsToEnvelopePoint.get(seconds).type;
			double[] samples = interpolationToSamples.get(type);
			for(int index = startSample; index < endSample; index++) {
				returnVal[index] = samples[index];
			}
			startSample = endSample;
		}
		return returnVal;
	}
	
	public double[] getDefaultEnvelope(int length) {
		double envelopeLength = length / SynthTools.sampleRate;
		ArrayList<Interpolate.TAPair> TAPairs = new ArrayList<Interpolate.TAPair>();
		TAPairs.add(new Interpolate.TAPair(0.0, 0.0));
		TAPairs.add(new Interpolate.TAPair(envelopeLength / 2.0, 1.0));
		TAPairs.add(new Interpolate.TAPair(envelopeLength, 0.0));
		return Interpolate.synthTAPairsLinear(TAPairs);
	}
	
	public void mousePressed(int x, int y) {
		int index = 0;
		for(Integer outputID: outputs) {
			Output output = (Output) parent.connectorIDToConnector.get(outputID);
			if(output.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(outputID);
				System.out.println("Envelope " + " " + "output: " + index);
				return;
			}
			index++;
		}
		EnvelopeEditor editor = new EnvelopeEditor(this);
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
		if(g2 != null) g2.drawString("Envelope", currentX, currentY);
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

	@Override
	public void loadModuleInfo(BufferedReader in) {
		envelopePoints = new ArrayList<EnvelopePoint>();
		try {
			String currentLine = in.readLine();
			int numPoints = new Integer(currentLine);
			for(int index = 0; index < numPoints; index++) {
				currentLine = in.readLine();
				double seconds = new Double(currentLine);
				currentLine = in.readLine();
				double amplitude = new Double(currentLine);
				currentLine = in.readLine();
				InterpolationType type = InterpolationType.valueOf(currentLine);
				envelopePoints.add(new EnvelopePoint(seconds, amplitude, type));
			}
		} catch (Exception e) {
			System.out.println("Envelope.loadModuleInfo: Error reading from file");
		}
		
	}

	@Override
	public void saveModuleInfo(BufferedWriter out) {
		try {
			out.write(new Integer(envelopePoints.size()).toString());
			out.newLine();
			for(EnvelopePoint point: envelopePoints) {
				out.write(new Double(point.seconds).toString());
				out.newLine();
				out.write(new Double(point.amplitude).toString());
				out.newLine();
				out.write(point.type.toString());
				out.newLine();
			}
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
	
	public MultiWindow getMultiWindow() {
		return parent.parent;
	}
	
}
