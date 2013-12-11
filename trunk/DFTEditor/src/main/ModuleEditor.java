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
import main.modules.BasicWaveform;
import main.modules.Envelope;
import main.modules.MasterInput;
import main.modules.StereoPan;

public class ModuleEditor extends JFrame {

	private static final long serialVersionUID = -6364925274198951658L;
	public MultiWindow parent;
	public ModuleView view;
	public ModuleController controller;
	private TreeMap<Integer, TreeMap<Integer, Module>> xToYToModule = null;
	public static Random randomGenerator = new Random();
	public HashMap<Long, Long> outputToInput = null;
	public HashSet<Long> inputs = null;
	public HashSet<Long> outputs = null;
	public HashMap<Long, Connector> connectorIDToConnector = null;
	public Long selectedOutput = null;
	private JToolBar navigationBar = null;
	private static double[] left = null;
	private static double[] right = null;
	public final static double maxAmplitudeIn_dB = 20.0;
	public final static double minAmplitudeIn_dB = -144.5; // 24 bit data
	public final static double maxDuration = 5.0;
	public final static double minDuration = FDData.timeStepInMillis / 1000.0;
	public final static double minFrequency = 0.001;
	public final static double maxFrequency = SynthTools.sampleRate / 2.0;
	
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
	
	public void initLeftRight() {
		for(Connector connector : connectorIDToConnector.values()) {
			if(connector instanceof Module.Output) {
				System.out.println("Output");
				Module.Output output = (Module.Output) connector;
				output.clearSamples();
			}
		}
		MasterInput masterInput = (MasterInput) xToYToModule.get(0).get(0);
		left = masterInput.getSamplesLeft();
		right = masterInput.getSamplesRight();
		if(left == null) left = new double[0];
		if(right == null) right = new double[0];
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
	}
	
	public void play() {
		initLeftRight();
		AudioPlayer ap = new AudioPlayer(left, right, 1.0);
		ap.start();
	}
	
	public void dft() {
		initLeftRight();
		parent.dftEditorFrame.ModuleDFT(left, right);
	}
	
	public ModuleEditor(MultiWindow parent) {
		this.parent = parent;
    	outputToInput = new HashMap <Long, Long>();
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
		xToYToModule.get(currentX).put(currentY, new BasicWaveform(this, currentX, currentY));
		currentY += xToYToModule.get(0).get(currentY).getHeight();
		xToYToModule.get(currentX).put(currentY, new BasicWaveform(this, currentX, currentY));
		currentX = xToYToModule.get(0).get(currentY).getWidth();
		currentY = masterInputHeight;
		xToYToModule.put(currentX, new TreeMap<Integer, Module>());
		xToYToModule.get(currentX).put(currentY, new BasicWaveform(this, currentX, currentY));
		currentY += xToYToModule.get(currentX).get(currentY).getHeight();
		xToYToModule.get(currentX).put(currentY, new BasicWaveform(this, currentX, currentY));
		currentX += xToYToModule.get(currentX).get(currentY).getWidth();
		currentY = masterInputHeight;
		xToYToModule.put(currentX, new TreeMap<Integer, Module>());
		xToYToModule.get(currentX).put(currentY, new BasicWaveform(this, currentX, currentY));
		currentY += xToYToModule.get(currentX).get(currentY).getHeight();
		xToYToModule.get(currentX).put(currentY, new BasicWaveform(this, currentX, currentY));
		currentX += xToYToModule.get(currentX).get(currentY).getWidth();
		currentY = masterInputHeight;
		xToYToModule.put(currentX, new TreeMap<Integer, Module>());
		xToYToModule.get(currentX).put(currentY, new Envelope(this, currentX, currentY));
		currentY += xToYToModule.get(currentX).get(currentY).getHeight();
		xToYToModule.get(currentX).put(currentY, new Envelope(this, currentX, currentY));
		currentX += xToYToModule.get(currentX).get(currentY).getWidth();
		currentY = masterInputHeight;
		xToYToModule.put(currentX, new TreeMap<Integer, Module>());
		xToYToModule.get(currentX).put(currentY, new StereoPan(this, currentX, currentY));
		currentY += xToYToModule.get(currentX).get(currentY).getHeight();
		xToYToModule.get(currentX).put(currentY, new StereoPan(this, currentX, currentY));
		
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
					long connectedTo = connectorIDToConnector.get(connectorID).getConnection();
					connectorIDToConnector.get(connectorID).removeConnection();
					connectorIDToConnector.get(connectedTo).removeConnection();
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
	
	public void refreshView() {
		view.repaint();
	}
	
}
