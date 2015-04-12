package main.playable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
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
import main.Module.ModuleType;
import main.PlayDataInWindow.SynthType;
import main.ModuleEditor;
import main.MultiWindow;
import main.Scale;
import main.SynthTools;
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
	
	public void addNavigationButton(String buttonText) {
		JButton button = new JButton(buttonText);
		button.addActionListener((ActionListener) controller);
		navigationBar.add(button);
	}
	
	public JToolBar createNavigationBar() {
        addNavigationButton("New Sequence");
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
        view.setPreferredSize(new Dimension(5000, 900));
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setSize(800, 600);
        add(scrollPane, BorderLayout.CENTER);
        createModules();
        sequencer = new PlayableSequencer(this, 10, 500);
        af = new AudioFetcher(this);
        af.start();
    }
    
    public synchronized void newSequence() {
    	 noAudio = true;
    	 sequencer = new PlayableSequencer(this, 10, 500);
    	 sequencer.newSequence();
    	 noAudio = false;
    }
    
    public void createModules() {
    	currentY = 10;
    	currentX = 10;
    	nameToModule = new TreeMap<String, PlayableModule>();
       	ArrayList<ControlBank.Spec> osc1Controls = new ArrayList<ControlBank.Spec>();
    	osc1Controls.add(new ControlBank.Spec(ControlBank.Name.OSC1Shape, Slider.Type.LINEAR, 0.0, 1.0, 0.5));
    	osc1Controls.add(new ControlBank.Spec(ControlBank.Name.OSC1PWM, Slider.Type.LINEAR, 0.1, 0.9, 0.5));
    	osc1Controls.add(new ControlBank.Spec(ControlBank.Name.SUBOSCLevel, Slider.Type.LINEAR, 0.0, 0.5, 0.0));
       	osc1Controls.add(new ControlBank.Spec(ControlBank.Name.NOISE_COLOR, Slider.Type.LINEAR, 0.0, 1.0, 0.5));
    	osc1Controls.add(new ControlBank.Spec(ControlBank.Name.NOISE_LEVEL, Slider.Type.LINEAR, 0.0, 1.0, 0.0));
    	addControlBank("OSCILLATOR_1", osc1Controls);
       	ArrayList<ControlBank.Spec> osc2Controls = new ArrayList<ControlBank.Spec>();
    	osc2Controls.add(new ControlBank.Spec(ControlBank.Name.OSC2Shape, Slider.Type.LINEAR, 0.0, 1.0, 0.5));
    	osc2Controls.add(new ControlBank.Spec(ControlBank.Name.OSC2PWM, Slider.Type.LINEAR, 0.1, 0.9, 0.5));
    	osc2Controls.add(new ControlBank.Spec(ControlBank.Name.OSC2FREQ, Slider.Type.LOGARITHMIC, 1.0 / 16.0, 16.0, 1.0));
       	osc2Controls.add(new ControlBank.Spec(ControlBank.Name.OSC2DETUNE, Slider.Type.LOGARITHMIC, 1.0, 1.5, 1.0));
    	osc2Controls.add(new ControlBank.Spec(ControlBank.Name.OSC2RING, Slider.Type.LINEAR, 0.0, 1.0, 0.0));
    	addControlBank("OSCILLATOR_2", osc2Controls);
    	addEnvelope("FM ADSR", PlayableEnvelope.EnvelopeType.ADSR);
       	addEnvelope("FM AR1", PlayableEnvelope.EnvelopeType.AR);
       	addEnvelope("FM AR2", PlayableEnvelope.EnvelopeType.AR);
    	addSpecifiedLFO("FM LFO", 0.5, 16.0, false);
    	ArrayList<ControlBank.Spec> fmControls = new ArrayList<ControlBank.Spec>();
    	fmControls.add(new ControlBank.Spec(ControlBank.Name.FM1Mod, Slider.Type.LOGARITHMIC, 1.0 / 256.0, 16, 1.0 / 256.0));
    	fmControls.add(new ControlBank.Spec(ControlBank.Name.FM1Ratio, Slider.Type.LOGARITHMIC, 0.25, 16.0, 1.0));
    	fmControls.add(new ControlBank.Spec(ControlBank.Name.FM2Mod, Slider.Type.LOGARITHMIC, 1.0 / 256.0, 16, 1.0 / 256.0));
    	fmControls.add(new ControlBank.Spec(ControlBank.Name.FM2Ratio, Slider.Type.LOGARITHMIC, 0.25, 16.0, 1.0));
    	fmControls.add(new ControlBank.Spec(ControlBank.Name.FM3Mod, Slider.Type.LOGARITHMIC, 1.0 / 256.0, 16, 1.0 / 256.0));
    	fmControls.add(new ControlBank.Spec(ControlBank.Name.FM3Ratio, Slider.Type.LOGARITHMIC, 0.25, 16.0, 1.0));
    	fmControls.add(new ControlBank.Spec(ControlBank.Name.FM4Mod, Slider.Type.LOGARITHMIC, 1.0 / 256.0, 16, 1.0 / 256.0));
    	fmControls.add(new ControlBank.Spec(ControlBank.Name.FM4Ratio, Slider.Type.LOGARITHMIC, 0.25, 16.0, 1.0));
    	fmControls.add(new ControlBank.Spec(ControlBank.Name.FM5Mod, Slider.Type.LOGARITHMIC, 1.0 / 256.0, 16, 1.0 / 256.0));
    	fmControls.add(new ControlBank.Spec(ControlBank.Name.FM5Ratio, Slider.Type.LOGARITHMIC, 0.25, 16.0, 1.0));
    	fmControls.add(new ControlBank.Spec(ControlBank.Name.FM6Mod, Slider.Type.LOGARITHMIC, 1.0 / 256.0, 16, 1.0 / 256.0));
    	fmControls.add(new ControlBank.Spec(ControlBank.Name.FM6Ratio, Slider.Type.LOGARITHMIC, 0.25, 16.0, 1.0));
    	fmControls.add(new ControlBank.Spec(ControlBank.Name.FM7Mod, Slider.Type.LOGARITHMIC, 1.0 / 256.0, 16, 1.0 / 256.0));
    	fmControls.add(new ControlBank.Spec(ControlBank.Name.FM7Ratio, Slider.Type.LOGARITHMIC, 0.25, 16.0, 1.0));
    	addControlBank("FM_OPERATOR_VALUES", fmControls);
    	addEnvelope("PWM ADSR", PlayableEnvelope.EnvelopeType.ADSR);
    	addModule("PWM OSC", PlayableModule.Type.CONTROL, new String[]{"SQR", "TRI"});
    	addSpecifiedLFO("PWM LFO", 0.5, 16.0, false);
    	addSpecifiedLFO("PWM FAST LFO", 16.0, 1024.0, false);
    	addEnvelope("AMP ADSR", PlayableEnvelope.EnvelopeType.ASR);
    	addModule("AMP OSC", PlayableModule.Type.CONTROL, new String[]{"SQR", "TRI"});
    	addSpecifiedLFO("AMP LFO", 0.5, 16.0, false);
    	addSpecifiedLFO("AMP FAST LFO", 16.0, 1024.0, false);
    	addEnvelope("FILTER ADSR", PlayableEnvelope.EnvelopeType.AR);
    	addModule("FILTER OSC", PlayableModule.Type.CONTROL, new String[]{"SQR", "TRI"});
    	addSpecifiedLFO("FILTER LFO", 0.5, 64.0, false);
    	addSpecifiedLFO("FILTER FAST LFO", 0.5, 64.0, false);
    	addFilterModule("LP FILTER", PlayableFilter.FilterType.LOWPASS);
    	addFilterModule("HP FILTER", PlayableFilter.FilterType.HIGHPASS);
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
    
    public void addControlBank(String moduleName, ArrayList<ControlBank.Spec> specs) {
 		nameToModule.put(moduleName, new ControlBank(this, moduleName, currentX, currentY, specs));
 		currentX = nameToModule.get(moduleName).getMaxScreenX();
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

}
