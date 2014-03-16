package main;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import main.Module.Connector;
import main.Module.ConnectorType;
import main.Module.ModuleType;
import main.modules.BasicWaveform;
import main.modules.BasicWaveformEditor;
import main.modules.Envelope;
import main.modules.EnvelopeEditor;
import main.modules.FIRFilter;
import main.modules.IIRFilter;
import main.modules.KarplusStrong;
import main.modules.MasterInput;
import main.modules.SelfModulator;
import main.modules.SineBank;
import main.modules.StereoPan;
import main.modules.WhiteNoise;

public class ModuleEditor extends JPanel {

	private static final long serialVersionUID = -6364925274198951658L;
	public MultiWindow parent;
	public ModuleView view;
	public ModuleController controller;
	private int moduleEditorID;
	private Integer basicWaveformEditorFrameID = null;
	private TreeMap<Integer, Integer> envelopeIDToEditorFrameID = new TreeMap<Integer, Integer>();
	private int masterInputHeight;
	private MasterInput masterInput = null;
	public static final int columnWidth = 150;
	public static final int scrollableWidth = columnWidth * 16;
	public static final int scrollableHeight = columnWidth * 16;
	public TreeMap<Module.ModuleType, TreeSet<Integer>> moduleTypeToModuleIDs;
	public ArrayList<Module> modules = null;
	public ArrayList<Connector> connectors = null;
	public Integer selectedOutput = null;
	private JToolBar navigationBar = null;
	private static double[] left = null;
	private static double[] right = null;
	public final static double maxAmplitudeLog2 = 4;
	public final static double minAmplitudeLog2 = -12.0;
	public final static double defaultDuration = 0.5;
	public final static double minFrequency = 1.0 / 2.0;
	public final static double maxFrequency = SynthTools.sampleRate / 2.0;
	public final static double minFrequencyLog2 = Math.log(minFrequency) / Math.log(2.0);
	public final static double maxFrequencyLog2 = Math.log(maxFrequency) / Math.log(2.0);
	public final static double minOctave = 32.0;
	public final static double maxOctave = 8192.0;
	public final static double defaultOctave = 256.0;
	public final static int continuousBufferLength = 44100 / 2;
	private boolean playContinuous = false;
	
	private String getTitle() {
		return parent.moduleEditorInfo.get(moduleEditorID).getName();
	}
	
	public void addNavigationButton(String buttonText) {
		JButton button = new JButton(buttonText);
		button.addActionListener((ActionListener) controller);
		navigationBar.add(button);
	}
	
	public JToolBar createNavigationBar() {
		navigationBar = new JToolBar("Navigation Bar");
        // Create Navigation Buttons
        addNavigationButton("Play Once");
        addNavigationButton("Play Continuous");
        addNavigationButton("Stop");
        addNavigationButton("DFT");
        addNavigationButton("Spectrum (DFT)");
        addNavigationButton("Spectrum (FFT)");
        addNavigationButton("Load");
        addNavigationButton("Save");
        addNavigationButton("Waveforms");
    	return navigationBar;
	}
	
	public void initLeftRight(double[] control) {
		if(control == null) {
			control = new double[(int) Math.round(defaultDuration * SynthTools.sampleRate)];
			for(int index = 0; index < control.length; index++) {
				control[index] = 1.0;
			}
		}
		for(Connector connector : connectors) {
			if(connector instanceof Module.Output) {
				//System.out.println("Output");
				Module.Output output = (Module.Output) connector;
				output.clearSamples();
			}
		}
		left = masterInput.getSamplesLeft(control);
		right = masterInput.getSamplesRight(control);
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
			//System.out.println("ModuleEditor.dft(): no signal");
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
		playContinuous = false;
		initLeftRight(null);
		AudioPlayer.playAudio(left, right);
	}

	public void stop() {
		playContinuous = false;
		AudioPlayer.stopPlaying();
	}
	
	public void playContinuous() {
		playContinuous = true;
		initLeftRight(null);
		AudioPlayer.playAudioLoop(left, right);
	}
	
	public ArrayList<double[]> getSamples(double[] controlIn) {
		ArrayList<double[]> returnVal = new ArrayList<double[]>();
		initLeftRight(controlIn);
		returnVal = new ArrayList<double[]>();
		returnVal.add(left);
		returnVal.add(right);
		return returnVal;
	}
	
	public void refreshData() {
		refreshView();
		if(playContinuous) {
			playContinuous();
			return;
		} else {
			play();
			return;
		}
	}
	
	public void dft() {
		initLeftRight(null);
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
	
	public ModuleEditor(MultiWindow parent, int moduleEditorID) {
		super(new BorderLayout());
		this.parent = parent;
		this.moduleEditorID = moduleEditorID;
        initModules();
        view = new ModuleView(this);
        view.setBackground(Color.black);
        controller = new ModuleController(this);
        add(createNavigationBar(), BorderLayout.PAGE_START);
        view.addMouseListener(controller);
        view.addMouseMotionListener(controller);
        view.setPreferredSize(new Dimension(scrollableWidth, scrollableHeight));
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setSize(800, 600);
        add(scrollPane, BorderLayout.CENTER);
        //this.setTitle("ModuleEditor: [no project selected]");
	}
	
	public void initModules() {
		modules = new ArrayList<Module>();
		connectors = new ArrayList<Connector>();
		moduleTypeToModuleIDs = new TreeMap<Module.ModuleType, TreeSet<Integer>>();
		modules.add(new MasterInput(this, 0, 0));
		masterInput = (MasterInput) modules.get(0);
		masterInputHeight = masterInput.getHeight();
		int col = 0;
		addModuleToColumn(col, Module.ModuleType.SINEBANK);
		addModuleToColumn(col, Module.ModuleType.SINEBANK);
		col++;
		addModuleToColumn(col, Module.ModuleType.ENVELOPE);
		addModuleToColumn(col, Module.ModuleType.ENVELOPE);
		addModuleToColumn(col, Module.ModuleType.ENVELOPE);
		addModuleToColumn(col, Module.ModuleType.ENVELOPE);
		col++;
		addModuleToColumn(col, Module.ModuleType.BASICWAVEFORM);
		addModuleToColumn(col, Module.ModuleType.BASICWAVEFORM);
		addModuleToColumn(col, Module.ModuleType.BASICWAVEFORM);
		addModuleToColumn(col, Module.ModuleType.BASICWAVEFORM);
		col++;
		addModuleToColumn(col, Module.ModuleType.BASICWAVEFORM);
		addModuleToColumn(col, Module.ModuleType.BASICWAVEFORM);
		addModuleToColumn(col, Module.ModuleType.BASICWAVEFORM);
		addModuleToColumn(col, Module.ModuleType.BASICWAVEFORM);
		col++;
		addModuleToColumn(col, Module.ModuleType.BASICWAVEFORM);
		addModuleToColumn(col, Module.ModuleType.BASICWAVEFORM);
		addModuleToColumn(col, Module.ModuleType.BASICWAVEFORM);
		addModuleToColumn(col, Module.ModuleType.BASICWAVEFORM);
		col++;
		addModuleToColumn(col, Module.ModuleType.SELFMODULATOR);
		addModuleToColumn(col, Module.ModuleType.SELFMODULATOR);
		addModuleToColumn(col, Module.ModuleType.SELFMODULATOR);
		addModuleToColumn(col, Module.ModuleType.SELFMODULATOR);
		col++;
		addModuleToColumn(col, Module.ModuleType.WHITENOISE);
		addModuleToColumn(col, Module.ModuleType.WHITENOISE);
		addModuleToColumn(col, Module.ModuleType.WHITENOISE);
		addModuleToColumn(col, Module.ModuleType.WHITENOISE);
		col++;
		addModuleToColumn(col, Module.ModuleType.IIRFILTER);
		addModuleToColumn(col, Module.ModuleType.IIRFILTER);
		addModuleToColumn(col, Module.ModuleType.IIRFILTER);
		addModuleToColumn(col, Module.ModuleType.IIRFILTER);
		col++;
		addModuleToColumn(col, Module.ModuleType.KARPLUSSTRONG);
		addModuleToColumn(col, Module.ModuleType.KARPLUSSTRONG);
		addModuleToColumn(col, Module.ModuleType.KARPLUSSTRONG);
		addModuleToColumn(col, Module.ModuleType.KARPLUSSTRONG);
		col++;
		addModuleToColumn(col, Module.ModuleType.STEREOPAN);
		addModuleToColumn(col, Module.ModuleType.STEREOPAN);	
		addModuleToColumn(col, Module.ModuleType.STEREOPAN);
		addModuleToColumn(col, Module.ModuleType.STEREOPAN);	
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
		case SELFMODULATOR:
			module = new SelfModulator(this, currentX, currentY);
			addModule(module);
			break;
		case FIRFILTER:
			module = new FIRFilter(this, currentX, currentY);
			addModule(module);
			break;
		case IIRFILTER:
			module = new IIRFilter(this, currentX, currentY);
			addModule(module);
			break;
		case WHITENOISE:
			module = new WhiteNoise(this, currentX, currentY);
			addModule(module);
			break;
		case SINEBANK:
			module = new SineBank(this, currentX, currentY);
			addModule(module);
			break;
		case KARPLUSSTRONG:
			module = new KarplusStrong(this, currentX, currentY);
			addModule(module);
			break;
		default:
			System.out.println("ModuleEditor.addModuleType: unknown module type");
		}
	}
	
	public void addModule(Module module) {
		module.setModuleId(modules.size());
		modules.add(module);
	}
	
	public Integer getNumberOfModuleType(ModuleType type) {
		int returnVal = 0;
		for(Module module: modules) {
			if(module.getModuleType() == type) returnVal++;
		}
		return returnVal;
	}
	
	public int getIDFromModuleID(int moduleID, ModuleType type) {
		int moduleIndex = 0;
		int id = 0;
		for(Module module: modules) {
			if(moduleID == moduleIndex) return id;
			if(module.getModuleType() == type) id++;
			moduleIndex++;
		}
		return -1;
	}
	
	public Module getModuleFromTypeID(int typeID, ModuleType type) {
		int idIndex = 0;
		for(Module module: modules) {
			if(module.getModuleType() == type) {
				if(idIndex == typeID) {
					return module; 
				} else {
					idIndex++;
				}
			}
		}
		return null;
	}
	
	public int addConnector(Connector connector) {
		int newID = connectors.size();
		connector.setConnectorID(newID);
		connectors.add(connector);
		return newID;
	}

	public void handleConnectorSelect(Integer connectorID) {
		if(selectedOutput == null) {
			if(connectors.get(connectorID).getConnectorType() == ConnectorType.OUTPUT) {
				Integer connectedTo = connectors.get(connectorID).getConnection();
				if(connectedTo != null) {
					connectors.get(connectorID).removeConnection();
					connectors.get(connectedTo).removeConnection();
					refreshView();
				} else {
					selectedOutput = connectorID;
					return;
				}
			} else {
				System.out.println("ModuleEditor.handleConnectorSelect: please select output first");
			}
		} else {
			if(connectors.get(connectorID).getConnectorType() == ConnectorType.INPUT) {
				Integer connectedFrom = connectors.get(connectorID).getConnection();
				if(connectedFrom != null) {
					System.out.println("ModuleEditor.handleConnectorSelect: input already connected");
					return;
				}
				connectors.get(selectedOutput).setConnection(connectorID);
				connectors.get(connectorID).setConnection(selectedOutput);
				selectedOutput = null;
			} else {
				System.out.println("ModuleEditor.handleConnectorSelect: please select input");
				return;
			}
		}
		refreshData();
	}
	
	public JFrame getParentFrame() {
		return parent.sequencerFrame;
	}
	
	public void refreshView() {
		view.repaint();
	}

	public ArrayList<Module> getModules() {
		return modules;
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
			for(Connector connector: connectors) {
				if(connector.getConnection() == null) continue;
				out.newLine();
				out.write(connector.getConnectorID() + "");
				out.newLine();
				out.write(connector.getConnection() + "");
			}
			out.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "There was a problem saving the file");
			return;
		}
		JOptionPane.showMessageDialog(this, "Finished Saving File");
		//this.setTitle(filename);
	}
	
	public void loadFromFile(String filename) {
		modules = new ArrayList<Module>();
		connectors = new ArrayList<Connector>();
		modules.add(new MasterInput(this, 0, 0));
		masterInput = (MasterInput) modules.get(0);
		masterInputHeight = masterInput.getHeight();
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String currentLine = in.readLine();
			while(!currentLine.contains("END MODULES")) {
				Module.ModuleType type = Module.ModuleType.valueOf(currentLine);
				int x = new Integer(in.readLine());
				int y = new Integer(in.readLine());
				addModuleAbsolute(x, y, type);
				modules.get(modules.size() - 1).loadModuleInfo(in);
				currentLine = in.readLine();
			}
			while(true) {
				currentLine = in.readLine();
				if(currentLine == null) break;
				int connectorID = new Integer(currentLine);
				int connectedID = new Integer(in.readLine());
				connectors.get(connectorID).setConnection(connectedID);
				System.out.println(connectorID + " " + connectedID);
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.toString());
			return;
		}
		//this.setTitle(filename);
		JOptionPane.showMessageDialog(this, "Finished Loading File");
	}
	
	public void viewEnvelopeEditor(EnvelopeEditor envelopeEditor) {
		Envelope envelope = envelopeEditor.getEnvelope();
		if(envelopeIDToEditorFrameID.containsKey(envelope.getTypeID())) {  
			parent.requestFocus(envelopeIDToEditorFrameID.get(envelope.getTypeID()));
		} else {
			envelopeIDToEditorFrameID.put(envelope.getTypeID(), parent.newFrame(envelopeEditor, getTitle() + ": Envelope: " + envelope.getTypeID()));
			parent.addWindowListener(envelopeIDToEditorFrameID.get(envelope.getTypeID()), envelopeEditor);
		}
	}
	
	public void viewBasicWaveformEditor() {
		if(basicWaveformEditorFrameID == null) {  
			BasicWaveformEditor bwe = new BasicWaveformEditor(this);
			basicWaveformEditorFrameID = parent.newFrame(bwe, getTitle() + ": Basic Waveform Editor");
			parent.addWindowListener(basicWaveformEditorFrameID, bwe);
		} else {
			parent.requestFocus(basicWaveformEditorFrameID);
		}
	}
	
	public void closeEnvelopeEditor(Envelope envelope) {
		parent.dispose(envelopeIDToEditorFrameID.get(envelope.getTypeID()));
		envelopeIDToEditorFrameID.remove(envelope.getTypeID());
	}

	public void closeBasicWaveformEditor() {
		parent.dispose(basicWaveformEditorFrameID);
		basicWaveformEditorFrameID = null;
	}

	public void viewSpectrumDFT() {
		initLeftRight(null);
		Spectrum spectrum = new Spectrum(parent);
		parent.newFrame(spectrum, "Spectrum");
		spectrum.initDFTData(left, right);
	}
	
	public void viewSpectrumFFT() {
		initLeftRight(null);
		Spectrum spectrum = new Spectrum(parent);
		parent.newFrame(spectrum, "Spectrum");
		spectrum.initFFTData(left, right);
	}

}
