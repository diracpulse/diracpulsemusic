package main;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
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
	public TreeMap<Integer, Module> moduleIDToModule = null;
	public TreeMap<Integer, Connector> connectorIDToConnector = null;
	public Integer selectedOutput = null;
	private JToolBar navigationBar = null;
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
	
	public void save() {
		String filename = ModuleFileTools.PromptForFileSave(view);
		if(filename == null) return;
		saveToFile(filename);
	}
	
	public void open() {
		String filename = ModuleFileTools.PromptForFileOpen(view);
		if(filename == null) return;
		loadFromFile(filename);
		view.repaint();
	}
	
	public ModuleEditor(MultiWindow parent) {
		this.parent = parent;
    	connectorIDToConnector = new TreeMap<Integer, Connector>();
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
		moduleIDToModule = new TreeMap<Integer, Module>();
		connectorIDToConnector = new TreeMap<Integer, Connector>();
		moduleIDToModule.put(0, new MasterInput(this, 0, 0));
		masterInput = (MasterInput) moduleIDToModule.get(0);
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
		for(Module loopModule: moduleIDToModule.values()) {
			if(loopModule.getX() == currentX) {
				if(loopModule.getY() + loopModule.getWidth() > currentY) {
					currentY = loopModule.getY() + loopModule.getHeight();
				}
			}
		}
		addModuleAbsolute(currentX, currentY, moduleType);
	}
	
	public void addModuleAbsolute(int currentX, int currentY, Module.ModuleType moduleType) {
		Module module = null;
		switch(moduleType) {
		case BASICWAVEFORM:
			module = new BasicWaveform(this, currentX, currentY);
			addModule(module);
			break;
		case ENVELOPE:
			module = new Envelope(this, currentX, currentY);
			addModule(module);
			break;
		case MASTERINPUT:
			System.out.println("ModuleEditor.addModuleType: duplicate master input");
			break;
		case STEREOPAN:
			module = new StereoPan(this, currentX, currentY);
			addModule(module);
			break;			
		}
		System.out.println("ModuleEditor.addModuleType: unknown module type");
	}
	
	public Integer addModule(Module module) {
		int nextKey = moduleIDToModule.lastKey() + 1;
		module.setModuleId(nextKey);
		moduleIDToModule.put(nextKey, module);
		return nextKey;
	}
	
	public Integer addConnector(Connector connector) {
		int nextKey = 0;
		if(!connectorIDToConnector.isEmpty()) nextKey = connectorIDToConnector.lastKey() + 1;
		connector.setConnectorID(nextKey);
		connectorIDToConnector.put(nextKey, connector);
		return nextKey;
	}

	public void handleConnectorSelect(Integer connectorID) {
		if(selectedOutput == null) {
			if(connectorIDToConnector.get(connectorID).getConnectorType() == ConnectorType.OUTPUT) {
				Integer connectedTo = connectorIDToConnector.get(connectorID).getConnection();
				if(connectedTo != null) {
					connectorIDToConnector.get(connectorID).removeConnection();
					connectorIDToConnector.get(connectedTo).removeConnection();
					view.repaint();
				} else {
					selectedOutput = connectorID;
				}
			} else {
				System.out.println("ModuleEditor.handleConnectorSelect: please select output first");
			}
		} else {
			if(connectorIDToConnector.get(connectorID).getConnectorType() == ConnectorType.INPUT) {
				Integer connectedFrom = connectorIDToConnector.get(connectorID).getConnection();
				if(connectedFrom != null) {
					System.out.println("ModuleEditor.handleConnectorSelect: input already connected");
					return;
				}
				connectorIDToConnector.get(selectedOutput).setConnection(connectorID);
				connectorIDToConnector.get(connectorID).setConnection(selectedOutput);
				selectedOutput = null;
			} else {
				System.out.println("ModuleEditor.handleConnectorSelect: please select input");
			}
		}
		view.repaint();
	}
	
	public void refreshView() {
		view.repaint();
	}

	public ArrayList<Module> getModules() {
		return new ArrayList<Module>(moduleIDToModule.values());
	}
	
	public void saveToFile(String filename) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
			for(Module module: getModules()) {
				if(module instanceof MasterInput) continue;
				out.write(module.getModuleType().toString());
				out.newLine();
				out.write(module.getX() + "");
				out.newLine();
				out.write(module.getY() + "");
				out.newLine();
				module.saveModuleInfo(out);
			}
			out.write("END MODULES");
			out.newLine();
			for(Connector connector: connectorIDToConnector.values()) {
				if(connector.getConnection() == null) continue;
				out.write(connector.getConnectorID() + "");
				out.newLine();
				out.write(connector.getConnection() + "");
				out.newLine();
			}
			out.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "There was a problem saving the file");
			return;
		}
		JOptionPane.showMessageDialog(this, "Finished Saving File");
		this.setTitle(filename);
	}
	
	public void loadFromFile(String filename) {
		moduleIDToModule = new TreeMap<Integer, Module>();
		connectorIDToConnector = new TreeMap<Integer, Connector>();
		moduleIDToModule.put(0, new MasterInput(this, 0, 0));
		masterInput = (MasterInput) moduleIDToModule.get(0);
		masterInputHeight = masterInput.getHeight();
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String currentLine = in.readLine();
			while(currentLine != "END MODULES") {
				Module.ModuleType type = Module.ModuleType.valueOf(currentLine);
				int x = new Integer(in.readLine());
				int y = new Integer(in.readLine());
				addModuleAbsolute(x, y, type);
				moduleIDToModule.get(moduleIDToModule.lastKey()).loadModuleInfo(in);
				currentLine = in.readLine();
			}
			while(true) {
				currentLine = in.readLine();
				if(currentLine.isEmpty()) break;
				int connectorID = new Integer(currentLine);
				int connectedID = new Integer(in.readLine());
				connectorIDToConnector.get(connectorID).setConnection(connectedID);
				System.out.println(connectorID + " " + connectedID);
			}
		} catch (Exception e) {
			if(e instanceof EOFException) {
				JOptionPane.showMessageDialog(this, "Reached End Of File");
				this.setTitle(filename);
			} else {
				JOptionPane.showMessageDialog(this, "Error Reading File");
			}
		}
		JOptionPane.showMessageDialog(this, "Finished Loading File");
	}
	
}