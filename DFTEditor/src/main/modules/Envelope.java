
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
	private TreeMap<Double, EnvelopePoint> secondsToEnvelopePoint = null;
	private double startSustain = -1.0;
	
	public class EnvelopePoint {
		
		public double amplitude = 1.0;
		public InterpolationType type = InterpolationType.LINEAR;
		public double bits = 8;
		
		EnvelopePoint(double amplitude, InterpolationType type) {
			this.amplitude = amplitude;
			this.type = type;
		}
		
		EnvelopePoint(double amplitude, InterpolationType type, double bits) {
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
		secondsToEnvelopePoint = new TreeMap<Double, EnvelopePoint>();
		secondsToEnvelopePoint.put(0.00, new EnvelopePoint(0.0, InterpolationType.LOGLINEAR, 2));
		secondsToEnvelopePoint.put(0.10, new EnvelopePoint(1.0, InterpolationType.LOGLINEAR, 2));
		secondsToEnvelopePoint.put(0.2, new EnvelopePoint(0.5, InterpolationType.LOGLINEAR, 4));
		startSustain = 0.2;
		secondsToEnvelopePoint.put(0.5, new EnvelopePoint(0.4, InterpolationType.LOGLINEAR, 6));
		secondsToEnvelopePoint.put(0.6, new EnvelopePoint(0.0, InterpolationType.LOGLINEAR, 8));
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
		int numSamples = (int) Math.round(secondsToEnvelopePoint.lastKey() * SynthTools.sampleRate) + 1;
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
		secondsToEnvelopePoint = new TreeMap<Double, EnvelopePoint>();
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
				secondsToEnvelopePoint.put(seconds, new EnvelopePoint(amplitude, type));
			}
		} catch (Exception e) {
			System.out.println("Envelope.loadModuleInfo: Error reading from file");
		}
		
	}

	@Override
	public void saveModuleInfo(BufferedWriter out) {
		try {
			out.write(new Integer(secondsToEnvelopePoint.size()).toString());
			out.newLine();
			for(double seconds: secondsToEnvelopePoint.keySet()) {
				EnvelopePoint point = secondsToEnvelopePoint.get(seconds);
				out.write(new Double(seconds).toString());
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
