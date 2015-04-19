package main.playable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.Timer;

import main.AudioPlayer;
import main.Module.Connector;
import main.Module.ModuleType;
import main.PlayDataInWindow.SynthType;
import main.Module;
import main.ModuleEditor;
import main.ModuleFileTools;
import main.MultiWindow;
import main.Scale;
import main.SynthTools;
import main.modules.MasterInput;
import main.playable.ControlBank.Name;
import main.playable.ControlBank.Spec;

public class PlayableEditor extends JPanel implements ActionListener, AudioSource {

	private static final long serialVersionUID = 3138005743637187863L;
	
	private static AudioFetcher af;
	
	public TreeMap<String, PlayableModule> nameToModule;
	public static int currentX = 0;
	public static int currentY = 0;
	
	MultiWindow parent;
	PlayableSequencer sequencer;
	PlayableView view;
	PlayableController controller;
	JToolBar navigationBar = null;
	Timer timer = null;
	long timerCalls = 0;
	public final int framesPerSecond = 50;
	public final int frameLengthInMillis = 1000 / framesPerSecond;
	public final int frameLengthInSamples = (int) Math.round(SynthTools.sampleRate / framesPerSecond);
	double[] loopData = new double[frameLengthInSamples * framesPerSecond * 4];
	int loopPosition = 0;
	public static final int moduleSpacing = 10;
	public static final int moduleYPadding = 20;
	public volatile boolean noAudio = false;
	
	ControlBank osc1Controls = new ControlBank(this, "OSC1", currentX, currentY);
	ControlBank osc2Controls = new ControlBank(this, "OSC2", currentX, currentY);
	ControlBank osc3Controls = new ControlBank(this, "OSC3", currentX, currentY);
   	ControlBank osc1RingControls = new ControlBank(this, "RING1", currentX, currentY);
  	ControlBank osc2RingControls = new ControlBank(this, "RING2", currentX, currentY);
  	ControlBank osc3RingControls = new ControlBank(this, "RING3", currentX, currentY);
	
	public void addNavigationButton(String buttonText) {
		JButton button = new JButton(buttonText);
		button.addActionListener((ActionListener) controller);
		navigationBar.add(button);
	}
	
	public JToolBar createNavigationBar() {
		addNavigationButton("Open");
		addNavigationButton("Save");
		addNavigationButton("Play");
		addNavigationButton("Pause");
		addNavigationButton("Stop");
        addNavigationButton("Random Sequence");
        addNavigationButton("Random Patch");
        addNavigationButton("Bass");
        addNavigationButton("Treble");
        
    	return navigationBar;
	}
	
    public PlayableEditor(MultiWindow parent) {
		super(new BorderLayout());
		this.parent = parent;
        view = new PlayableView(this);
        view.setBackground(Color.black);
        controller = new PlayableController(this);
        navigationBar = new JToolBar();
        add(createNavigationBar(), BorderLayout.PAGE_START);
        view.addMouseListener(controller);
        view.addMouseMotionListener(controller);
        view.setPreferredSize(new Dimension(1600, 750));
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setSize(800, 600);
        add(scrollPane, BorderLayout.CENTER);
        createModules();
        sequencer = new PlayableSequencer(this, 10, 10 + 53 * 8 + 16 * 6);
        af = new AudioFetcher(this);
        af.start();
    }
    
    public synchronized void randomSequence() {
    	 noAudio = true;
    	 sequencer = new PlayableSequencer(this, 10, 10 + 53 * 8 + 16 * 6);
    	 sequencer.reset();
    	 noAudio = false;
    }
    
    public synchronized void pause() {
    	noAudio = true;
    }
    
    public synchronized void stop() {
    	sequencer.reset();
    	noAudio = true;
    }
    
    public synchronized void play() {
    	noAudio = false;
    }
    
    public void randomPatch() {
    	osc1Controls.setRandomValues();
    	osc2Controls.setRandomValues();
    	osc3Controls.setRandomValues();
       	osc1RingControls.setRandomValues();
      	osc2RingControls.setRandomValues();
      	osc3RingControls.setRandomValues();
      	view.repaint();
    }

    public void createModules() {
    	currentY = 10;
    	currentX = 10;
    	nameToModule = new TreeMap<String, PlayableModule>();
    	addControl("BPM", 30.0, 300.0, 120.0);
    	osc1Controls = new ControlBank(this, "OSC1", currentX, currentY);
    	osc1Controls.add(osc1Controls.new Spec(ControlBank.Name.OSC1Shape, Slider.Type.LINEAR, 0.0, 1.0, 0.5));
    	osc1Controls.add(osc1Controls.new Spec(ControlBank.Name.OSC1PWM, Slider.Type.LINEAR, 0.1, 0.9, 0.5));
    	addControlBank(osc1Controls);
    	osc2Controls = new ControlBank(this, "OSC2", currentX, currentY);
    	osc2Controls.add(osc2Controls.new Spec(ControlBank.Name.OSC2Shape, Slider.Type.LINEAR, 0.0, 1.0, 1.0));
    	osc2Controls.add(osc2Controls.new Spec(ControlBank.Name.OSC2PWM, Slider.Type.LINEAR, 0.1, 0.9, 0.5));
    	osc2Controls.add(osc2Controls.new Spec(ControlBank.Name.OSC2FREQ, Slider.Type.LOGDIV2, 1.0 / 4.0, 4.0, 1.0));
       	osc2Controls.add(osc2Controls.new Spec(ControlBank.Name.OSC2DETUNE, Slider.Type.LOGARITHMIC, 1.0, 1.5, 1.0));
    	addControlBank(osc2Controls);
    	osc3Controls = new ControlBank(this, "OSC3", currentX, currentY);
    	osc3Controls.add(osc3Controls.new Spec(ControlBank.Name.OSC3Shape, Slider.Type.LINEAR, 0.0, 1.0, 1.0));
    	osc3Controls.add(osc3Controls.new Spec(ControlBank.Name.OSC3PWM, Slider.Type.LINEAR, 0.1, 0.9, 0.5));
    	osc3Controls.add(osc3Controls.new Spec(ControlBank.Name.OSC3FREQ, Slider.Type.LOGDIV2, 1.0 / 4.0, 4.0, 1.0));
       	osc3Controls.add(osc3Controls.new Spec(ControlBank.Name.OSC3DETUNE, Slider.Type.LOGARITHMIC, 1.0, 1.5, 1.0));
    	addControlBank(osc3Controls);
    	osc1RingControls = new ControlBank(this, "RING1", currentX, currentY);
    	osc1RingControls.add(osc1RingControls.new Spec(ControlBank.Name.RING1Shape, Slider.Type.LINEAR, 0.0, 1.0, 1.0));
    	osc1RingControls.add(osc1RingControls.new Spec(ControlBank.Name.RING1PWM, Slider.Type.LINEAR, 0.1, 0.9, 0.5));
    	osc1RingControls.add(osc1RingControls.new Spec(ControlBank.Name.RING1FREQ, Slider.Type.LOGDIV2, 1.0 / 4.0, 4.0, 1.0));
    	osc1RingControls.add(osc1RingControls.new Spec(ControlBank.Name.RING1AMT, Slider.Type.LINEAR, 0.0, 1.0, 1.0));
    	addControlBank(osc1RingControls);
      	osc2RingControls = new ControlBank(this, "RING2", currentX, currentY);
    	osc2RingControls.add(osc2RingControls.new Spec(ControlBank.Name.RING2Shape, Slider.Type.LINEAR, 0.0, 1.0, 1.0));
    	osc2RingControls.add(osc2RingControls.new Spec(ControlBank.Name.RING2PWM, Slider.Type.LINEAR, 0.1, 0.9, 0.5));
    	osc2RingControls.add(osc2RingControls.new Spec(ControlBank.Name.RING2FREQ, Slider.Type.LOGDIV2, 1.0 / 4.0, 4.0, 1.0));
    	osc2RingControls.add(osc2RingControls.new Spec(ControlBank.Name.RING2AMT, Slider.Type.LINEAR, 0.0, 1.0, 1.0));
    	addControlBank(osc2RingControls);
      	osc3RingControls = new ControlBank(this, "RING3", currentX, currentY);
    	osc3RingControls.add(osc3RingControls.new Spec(ControlBank.Name.RING3Shape, Slider.Type.LINEAR, 0.0, 1.0, 1.0));
    	osc3RingControls.add(osc3RingControls.new Spec(ControlBank.Name.RING3PWM, Slider.Type.LINEAR, 0.1, 0.9, 0.5));
    	osc3RingControls.add(osc3RingControls.new Spec(ControlBank.Name.RING3FREQ, Slider.Type.LOGDIV2, 1.0 / 4.0, 4.0, 1.0));
    	osc3RingControls.add(osc3RingControls.new Spec(ControlBank.Name.RING3AMT, Slider.Type.LINEAR, 0.0, 1.0, 1.0));
    	addControlBank(osc3RingControls);
    	addEnvelope("R1AMP", PlayableEnvelope.EnvelopeType.R0);
    	addEnvelope("R2AMP", PlayableEnvelope.EnvelopeType.R0);
    	addEnvelope("R3AMP", PlayableEnvelope.EnvelopeType.R0);
    	addEnvelope("AMP ADSR", PlayableEnvelope.EnvelopeType.ADSR);
    	addModule("AMP OSC", PlayableModule.Type.CONTROL, new String[]{"SAW", "SIN"});
    	addSpecifiedLFO("AMP LFO", 0.5, 32, false);
    	addEnvelope("FLTR ADSR", PlayableEnvelope.EnvelopeType.ADSR);
    	addModule("FLTR OSC", PlayableModule.Type.CONTROL, new String[]{"SAW", "SIN"});
    	addSpecifiedLFO("FLTR LFO", 0.5, 32, false);
    	addFilterModule("LP", PlayableFilter.FilterType.LOWPASS);
    	addFilterModule("HP", PlayableFilter.FilterType.HIGHPASS);
       	ControlBank mixerControls = new ControlBank(this, "MIXER", currentX, currentY);
    	mixerControls.add(mixerControls.new Spec(ControlBank.Name.OSC1LEVEL, Slider.Type.LINEAR, 0.0, 1.0, 1.0));
    	mixerControls.add(mixerControls.new Spec(ControlBank.Name.OSC2LEVEL, Slider.Type.LINEAR, 0.0, 1.0, 0.5));
    	mixerControls.add(mixerControls.new Spec(ControlBank.Name.OSC3LEVEL, Slider.Type.LINEAR, 0.0, 1.0, 0.5));
    	mixerControls.add(mixerControls.new Spec(ControlBank.Name.RINGALL, Slider.Type.LINEAR, 0.0, 1.0, 0.5));
    	addControlBank(mixerControls);
    }
    
    public void addModule(String moduleName, PlayableModule.Type type) {
    	addModule(moduleName, type, null);
    }
    
    public void addModule(String moduleName, PlayableModule.Type type, String[] controlValues) {
    	switch(type) {
    	case LFO:
    		nameToModule.put(moduleName, new PlayableLFO(this, currentX, currentY, moduleName, PlayableLFO.WaveType.VARIABLE));
    		currentX = nameToModule.get(moduleName).getMaxScreenX();
    		return;
    	case ENVELOPE:
    		System.out.println("PlayableEditor.Module.addModule: Envelope not suppported");
    		return;
       	case CONTROL:
    		nameToModule.put(moduleName, new PlayableControl(this, currentX, currentY, new String[] {moduleName, controlValues[0], controlValues[1]}));
    		currentX = nameToModule.get(moduleName).getMaxScreenX();
    		return;	
       	case CONTROLBANK:
    		nameToModule.put(moduleName, new PlayableControl(this, currentX, currentY, new String[] {moduleName, controlValues[0], controlValues[1]}));
    		currentX = nameToModule.get(moduleName).getMaxScreenX();
    		return;	
       	case FILTER:
       		System.out.println("PlayableEditor.Module.addModule: Filter not suppported");
    	}
    }
    
    public void addControl(String moduleName, double minValue, double maxValue, double initialValue) {
    	nameToModule.put(moduleName, new PlayableControl(this, currentX, currentY, minValue, maxValue, initialValue, moduleName));
    	currentX = nameToModule.get(moduleName).getMaxScreenX();
    }
    
    public void addControlBank(ControlBank controlBank) {
 		nameToModule.put(controlBank.moduleName, controlBank);
 		currentX = nameToModule.get(controlBank.moduleName).getMaxScreenX();
     }
    
    public void addEnvelope(String moduleName, PlayableEnvelope.EnvelopeType type) {
		nameToModule.put(moduleName, new PlayableEnvelope(this, type, currentX, currentY, moduleName));
		currentX = nameToModule.get(moduleName).getMaxScreenX();
    }
    
    public void addSpecifiedLFO(String moduleName, double minFreq, double maxFreq, boolean fineFreq) {
   		nameToModule.put(moduleName, new PlayableLFO(this, currentX, currentY, minFreq, maxFreq, moduleName, fineFreq));
		currentX = nameToModule.get(moduleName).getMaxScreenX();
    }
    
    public void addFilterModule(String moduleName, PlayableFilter.FilterType filterType) {
   		nameToModule.put(moduleName, new PlayableFilter(this, filterType, currentX, currentY, moduleName));
		currentX = nameToModule.get(moduleName).getMaxScreenX();
    }
    
    public void addFreqModule(String moduleName, double minVal, double maxVal, double initial) {
    	nameToModule.put(moduleName, new PlayableControl(this, currentX, currentY, minVal, maxVal, initial, moduleName));
    	currentX = nameToModule.get(moduleName).getMaxScreenX();
    }
    
    public void mousePressed(int x, int y) {
    	for(PlayableModule module: nameToModule.values()) {
    		module.pointSelected(x, y);
    	}
    	sequencer.pointSelected(x, y);
    }
    
    public void mouseReleased(int x, int y) {
    }
    
    public void mouseDragged(int x, int y) {
      	for(PlayableModule module: nameToModule.values()) {
    		module.pointSelected(x, y);
    	}
    }

	@Override
	public synchronized double[] getNextSamples(int numSamples) {
		if(!noAudio) return sequencer.masterGetSamples(numSamples);
		double[] returnVal = new double[numSamples];
		for(int index = 0; index < numSamples; index++) {
			returnVal[index] = 0.0;
		}
		return returnVal;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public void save() {
		String filename = FileUtils.PromptForFileSave(view);
		if(filename == null) return;
		saveToFile(filename);
	}
	
	public void open() {
		String filename = FileUtils.PromptForFileOpen(view);
		if(filename == null) return;
		loadFromFile(filename);
		view.repaint();
	}
	
	public void saveToFile(String filename) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
			sequencer.saveModuleInfo(out);
			for(PlayableModule module: nameToModule.values()) module.saveModuleInfo(out);
			out.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "There was a problem saving the file");
			return;
		}
		JOptionPane.showMessageDialog(this, "Finished Saving File");
	}
	
	public void loadFromFile(String filename) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			sequencer.loadModuleInfo(in);
			for(PlayableModule module: nameToModule.values()) module.loadModuleInfo(in);
			in.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.toString());
			return;
		}
		JOptionPane.showMessageDialog(this, "Finished Loading File");
	}
	
	

}
