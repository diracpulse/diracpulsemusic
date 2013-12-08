package main;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JToolBar;

import main.Module.Connector;
import main.Module.ConnectorType;
import main.TestSignals.TAPair;
import main.TestSignals.TAPair.AmplitudeFormat;
import main.TestSignals.TAPair.TimeFormat;
import main.modules.MasterInput;
import main.modules.Sawtooth;


public class ModuleEditor extends JFrame {

	private static final long serialVersionUID = -6364925274198951658L;
	public MultiWindow parent;
	public ModuleView view;
	public ModuleController controller;
	private TreeMap<Integer, TreeMap<Integer, Module>> xToYToModule = null;
	public static Random randomGenerator = new Random();
	public HashMap<Long, Long> outputToInput = null;
	public HashMap<Long, Long> inputToOutput = null;
	public HashSet<Long> inputs = null;
	public HashSet<Long> outputs = null;
	public HashMap<Long, Connector> connectorIDToConnector = null;
	public Long selectedOutput = null;
	private JToolBar navigationBar = null;
	
	public class ModuleScreenInfo {
		Rectangle dimensions;
		Module module;
		public ModuleScreenInfo(Rectangle dimensions, Module module) {
			this.dimensions = dimensions;
			this.module = module;
		}
		public boolean pointIsInside(int x, int y) {
			return dimensions.contains(x, y);
		}
	}
	
	public void addNavigationButton(String buttonText) {
		JButton button = new JButton(buttonText);
		button.addActionListener((ActionListener) controller);
		navigationBar.add(button);
	}
	
	public JToolBar createNavigationBar() {
		navigationBar = new JToolBar("Navigation Bar");
        // Create Navigation Buttons
        addNavigationButton("Play");
        addNavigationButton("DFT");
    	return navigationBar;
	}
	
	public void play() {
		double[] left = xToYToModule.get(0).get(0).getSamplesLeft(null);
		double[] right = xToYToModule.get(0).get(0).getSamplesRight(null);
		AudioPlayer ap = new AudioPlayer(left, right, 1.0);
		ap.run();
	}
	
	public void dft() {
		double[] left = xToYToModule.get(0).get(0).getSamplesLeft(null);
		double[] right = xToYToModule.get(0).get(0).getSamplesRight(null);
		double[] paddedLeft;
		double[] paddedRight;
		if(left.length > right.length) {
			paddedRight = new double[left.length];
			for(int index = 0; index < right.length; index++) {
				paddedRight[index] = right[index];
			}
			for(int index = right.length; index < left.length; index++) {
				paddedRight[index] = 0.0;
			}
			right = paddedRight;
		}
		if(right.length > left.length) {
			paddedLeft = new double[right.length];
			for(int index = 0; index < left.length; index++) {
				paddedLeft[index] = left[index];
			}
			for(int index = left.length; index < right.length; index++) {
				paddedLeft[index] = 0.0;
			}
			left = paddedLeft;
		}
		if(left.length == 0) {
			System.out.println("ModuleEditor.dft(): no signal");
			return;
		}
		double maxAmplitude = 0.0;
		for(int index = 0; index < left.length; index++) {
			if(Math.abs(left[index]) > maxAmplitude) maxAmplitude = Math.abs(left[index]);
			if(Math.abs(right[index]) > maxAmplitude) maxAmplitude = Math.abs(right[index]);
		}
		if(maxAmplitude == 0.0) return;
		double scale = (Short.MAX_VALUE - 1) / maxAmplitude;
		for(int index = 0; index < left.length; index++) {
			left[index] *= scale;
			right[index] *= scale;
		}
		DFT2.SynthDFTMatrix(left, right);
	}
	
	public ModuleEditor() {
    	outputToInput = new HashMap <Long, Long>();
    	inputToOutput = new HashMap <Long, Long>();
    	inputs = new HashSet<Long>();
    	outputs = new HashSet<Long>();
    	connectorIDToConnector = new HashMap<Long, Connector>();
        initModules();
        view = new ModuleView(this);
        view.setBackground(Color.black);
        controller = new ModuleController(this);
        add(createNavigationBar(), BorderLayout.PAGE_START);
        view.addMouseListener(controller);
        view.addMouseMotionListener(controller);
        add(view);
        setSize(1500, 800);
        this.setTitle("ModuleEditor: [no project selected]");
	}
	
	public void initModules() {
		int currentY = 0;
		int currentX = 0;
		int masterInputHeight = 0;
		xToYToModule = new TreeMap<Integer, TreeMap<Integer, Module>>();
		xToYToModule.put(currentX, new TreeMap<Integer, Module>());
		xToYToModule.get(currentX).put(currentY, new MasterInput(this, currentX, currentY));
		masterInputHeight = xToYToModule.get(0).get(0).getHeight();
		currentY = masterInputHeight;
		xToYToModule.get(currentX).put(currentY, new Sawtooth(this, currentX, currentY, 256.0, new TAPair(TimeFormat.SECONDS, AmplitudeFormat.ABSOLUTE, 1.0, 1.0)));
		currentY += xToYToModule.get(0).get(currentY).getHeight();
		xToYToModule.get(currentX).put(currentY, new Sawtooth(this, currentX, currentY, 128.0, new TAPair(TimeFormat.SECONDS, AmplitudeFormat.ABSOLUTE, 2.0, 0.5)));
		currentX = xToYToModule.get(0).get(currentY).getWidth();
		currentY = masterInputHeight;
		xToYToModule.put(currentX, new TreeMap<Integer, Module>());
		xToYToModule.get(currentX).put(currentY, new Sawtooth(this, currentX, currentY, 64.0, new TAPair(TimeFormat.SECONDS, AmplitudeFormat.ABSOLUTE, 1.5, 0.75)));
		currentY += xToYToModule.get(currentX).get(currentY).getHeight();
		xToYToModule.get(currentX).put(currentY, new Sawtooth(this, currentX, currentY, 512.0, new TAPair(TimeFormat.SECONDS, AmplitudeFormat.ABSOLUTE, 2.5, 0.25)));
	}
	
	public ArrayList<ModuleScreenInfo> getModulesScreenInfo() {
		ArrayList<ModuleScreenInfo> returnVal = new ArrayList<ModuleScreenInfo>();
		for(Integer x: xToYToModule.keySet()) {
			for(Integer y: xToYToModule.get(x).keySet()) {
				Module currentModule = xToYToModule.get(x).get(y);
				returnVal.add(new ModuleScreenInfo(new Rectangle(x, y, currentModule.getWidth(), currentModule.getHeight()),xToYToModule.get(x).get(y)));
			}
		}
		return returnVal;
	}
	
	public void addInput(Connector connector) {
		if(connector.getConnectorType() != Module.ConnectorType.INPUT) {
			System.out.println("ModuleEditor: Error: addInput invalid connector type");
		} else {
			inputs.add(connector.getConnectorID());
			connectorIDToConnector.put(connector.getConnectorID(), connector);
		}
	}
	
	public void addOutput(Connector connector) {
		if(connector.getConnectorType() != Module.ConnectorType.OUTPUT) {
			System.out.println("ModuleEditor: Error: addInput invalid connector type");
		} else {
			outputs.add(connector.getConnectorID());
			connectorIDToConnector.put(connector.getConnectorID(), connector);
		}
	}
	
	public void handleConnectorSelect(Long connectorID) {
		if(selectedOutput == null) {
			if(connectorIDToConnector.get(connectorID).getConnectorType() == ConnectorType.OUTPUT) {
				if(outputToInput.containsKey(connectorID)) {
					outputToInput.remove(connectorID);
					view.repaint();
					return;
				}
				selectedOutput = connectorID;
			} else {
				System.out.println("Module Editor: please select output first");
			}
		} else {
			if(connectorIDToConnector.get(connectorID).getConnectorType() == ConnectorType.INPUT) {
				outputToInput.put(selectedOutput, connectorID);
				connectorIDToConnector.get(selectedOutput).setConnection(connectorID);
				connectorIDToConnector.get(connectorID).setConnection(selectedOutput);
				selectedOutput = null;
			} else {
				System.out.println("Module Editor: please select input");
			}
		}
		view.repaint();
	}
	
}
