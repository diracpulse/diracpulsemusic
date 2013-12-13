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
import main.Module.ModuleType;
import main.modules.BasicWaveform;
import main.modules.Envelope;
import main.modules.MasterInput;
import main.modules.StereoPan;

public class ModuleEditor extends JFrame {

	private static final long serialVersionUID = -6364925274198951658L;
	public MultiWindow parent;
	public ModuleView view;
	public ModuleController controller;
	private int masterInputHeight;
	private MasterInput masterInput = null;
	public static final int columnWidth = 150;
	private ArrayList<Module> modules;
	public HashMap<Integer, Integer> outputToInput = null;
	public HashSet<Integer> inputs = null;
	public HashSet<Integer> outputs = null;
	public HashMap<Integer, Connector> connectorIDToConnector = null;
	public Integer selectedOutput = null;
	private JToolBar navigationBar = null;
	private static int nextConnectorID = 0;
	private static int nextModuleID = 0;
	private static double[] left = null;
	private static double[] right = null;
	public final static double maxAmplitudeIn_dB = 20.0;
	public final static double minAmplitudeIn_dB = -144.5; // 24 bit data
	public final static double maxDuration = 5.0;
	public final static double minDuration = FDData.timeStepInMillis / 1000.0;
	public final static double minFrequency = 0.001;
	public final static double maxFrequency = SynthTools.sampleRate / 2.0;
	
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
        addNavigationButton("Load");
        addNavigationButton("Save");
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
    	outputToInput = new HashMap <Integer, Integer>();
    	inputs = new HashSet<Integer>();
    	outputs = new HashSet<Integer>();
    	connectorIDToConnector = new HashMap<Integer, Connector>();
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
		modules = new ArrayList<Module>();
		masterInput = new MasterInput(this, 0, 0);
		modules.add(masterInput);
		masterInputHeight = masterInput.getHeight();
		addModuleToColumn(0, Module.ModuleType.ENVELOPE);
		addModuleToColumn(0, Module.ModuleType.ENVELOPE);
		addModuleToColumn(1, Module.ModuleType.BASICWAVEFORM);
		addModuleToColumn(1, Module.ModuleType.BASICWAVEFORM);
		addModuleToColumn(1, Module.ModuleType.BASICWAVEFORM);
		addModuleToColumn(1, Module.ModuleType.BASICWAVEFORM);
		addModuleToColumn(2, Module.ModuleType.BASICWAVEFORM);
		addModuleToColumn(2, Module.ModuleType.BASICWAVEFORM);
		addModuleToColumn(2, Module.ModuleType.BASICWAVEFORM);
		addModuleToColumn(2, Module.ModuleType.BASICWAVEFORM);
		addModuleToColumn(3, Module.ModuleType.STEREOPAN);
		addModuleToColumn(3, Module.ModuleType.STEREOPAN);	
		
	}
	
	public void addModuleToColumn(int col, Module.ModuleType moduleType) {
		int currentX = col * columnWidth;
		int currentY = masterInputHeight;
		for(Module loopModule: modules) {
			if(loopModule.getX() == currentX) {
				if(loopModule.getY() + loopModule.getWidth() > currentY) {
					currentY = loopModule.getY() + loopModule.getHeight();
				}
			}
		}
		switch(moduleType) {
		case BASICWAVEFORM:
			modules.add(new BasicWaveform(this, currentX, currentY));
			break;
		case ENVELOPE:
			modules.add(new Envelope(this, currentX, currentY));
			break;
		case MASTERINPUT:
			//modules.add(new MasterInput(this, currentX, currentY));
			break;
		case STEREOPAN:
			modules.add(new StereoPan(this, currentX, currentY));
			break;			
		}
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
	
	public void handleConnectorSelect(Integer connectorID) {
		if(selectedOutput == null) {
			if(connectorIDToConnector.get(connectorID).getConnectorType() == ConnectorType.OUTPUT) {
				if(outputToInput.containsKey(connectorID)) {
					outputToInput.remove(connectorID);
					Integer connectedTo = connectorIDToConnector.get(connectorID).getConnection();
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
	
	public static int getNextConnectorID() {
		int currentConnectorID = nextConnectorID;
		nextConnectorID++;
		return currentConnectorID;
	}
	
	public static int getNextModuleID() {
		int currentModuleID = nextModuleID;
		nextModuleID++;
		return currentModuleID;
	}
	
	public ArrayList<Module> getModules() {
		return modules;
	}
	
}
