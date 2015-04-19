
package main.playable;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.Random;

import main.SynthTools;
import main.modules.BasicWaveform;
import main.playable.ControlBank.Spec;

public class PlayableSequencer implements PlayableModule {
	
	private Random random;
	private Waveforms waveforms;
	private int screenX;
	private int screenY;
	private static final int numKeys = 24;
	private static final int keyHeight = 10;
	private static final int noteWidth = 30;
	private static final Color[] keyColors = {Color.WHITE, Color.BLACK, Color.WHITE, Color.BLACK, Color.WHITE, Color.WHITE, Color.BLACK, Color.WHITE, Color.BLACK, Color.WHITE, Color.BLACK, Color.WHITE};
	private static final int[] minorScale = {0, 2, 3, 5, 7, 8, 10, 12, 14, 15, 17, 19, 20, 22};
	PlayableEditor parent;
	long currentTimeInSamples = 0;
	public static double baseFreq = 256.0;
	int numNotes = 16;
	boolean[] tie;
	boolean[] accent;
	boolean accentOn = false;
	int[] notes;
	int bpm = 120;
	int noteLengthInSamples = (int) Math.round(44100.0 / bpm * 60); 
	int noteRestInSamples = (int) Math.round(noteLengthInSamples / 4.0);
	long noteStartInSamples = 0;
	long accentTimeInSamples = 882;
	double currentPhase;
	double[] osc = new double[4];
	public volatile boolean newSequence = false;
	double prevOsc2NoteFreq = baseFreq;
	double osc2NoteFreq = baseFreq;
	double glideVal = 0.0;
	double accentVal = 1.0;
	
	long ampLFOStartTime = 0;
	long filterLFOStartTime = 0;
	boolean ampLFO_On = false;
	boolean filterLFO_On = false;

	public PlayableSequencer(PlayableEditor parent, int screenX, int screenY) {
		waveforms = new Waveforms();
		random = new Random();
		//noise = new Noise();
		this.parent = parent;
		this.screenX = screenX;
		this.screenY = screenY;
		currentTimeInSamples = 0;
		notes = new int[numNotes];
		tie = new boolean[numNotes];
		accent = new boolean[numNotes];
		int currentNote = 7;
		for(int index = 0; index < notes.length; index++) {
			 accent[index] = false; // random.nextBoolean();
			 tie[index] = random.nextBoolean();
			 notes[index] = minorScale[currentNote];
			 int noteStep = random.nextInt(5) - 2;
			 currentNote += noteStep;
			 if(currentNote >= minorScale.length) currentNote -= noteStep * 2;
			 if(currentNote < 0) currentNote -= noteStep * 2;
		}
	}
	
	public synchronized void reset() {
    	PlayableFilter lpFilter = (PlayableFilter) parent.nameToModule.get("LP");
    	lpFilter.reset();
    	PlayableFilter hpFilter = (PlayableFilter) parent.nameToModule.get("HP");
    	hpFilter.reset();
		currentTimeInSamples = 0;
	}
	
	public synchronized void bass() {
		baseFreq = 64;
	}
	
	public synchronized void treble() {
		baseFreq = 256;
	}

	public double[] masterGetSamples(int numSamples) {
		PlayableControl BPM = (PlayableControl) parent.nameToModule.get("BPM");
		ControlBank osc1CONTROLS = (ControlBank) parent.nameToModule.get("OSC1");
		ControlBank osc2CONTROLS = (ControlBank) parent.nameToModule.get("OSC2");
		ControlBank osc3CONTROLS = (ControlBank) parent.nameToModule.get("OSC3");
		ControlBank ringOsc1CONTROLS = (ControlBank) parent.nameToModule.get("RING1");
		ControlBank ringOsc2CONTROLS = (ControlBank) parent.nameToModule.get("RING2");
		ControlBank ringOsc3CONTROLS = (ControlBank) parent.nameToModule.get("RING3");
		PlayableEnvelope ringAmp1ENV = (PlayableEnvelope) parent.nameToModule.get("R1AMP");
		PlayableEnvelope ringAmp2ENV = (PlayableEnvelope) parent.nameToModule.get("R2AMP");
		PlayableEnvelope ringAmp3ENV = (PlayableEnvelope) parent.nameToModule.get("R3AMP");
    	PlayableEnvelope ampENV = (PlayableEnvelope) parent.nameToModule.get("AMP ADSR");
    	PlayableControl ampOSC = (PlayableControl) parent.nameToModule.get("AMP OSC");
    	PlayableLFO ampLFO = (PlayableLFO) parent.nameToModule.get("AMP LFO");
    	PlayableEnvelope filterENV = (PlayableEnvelope) parent.nameToModule.get("FLTR ADSR");
    	PlayableControl filterOSC = (PlayableControl) parent.nameToModule.get("FLTR OSC");
    	PlayableLFO filterLFO = (PlayableLFO) parent.nameToModule.get("FLTR LFO");
    	ControlBank MIXER = (ControlBank) parent.nameToModule.get("MIXER");
    	PlayableFilter lpFilter = (PlayableFilter) parent.nameToModule.get("LP");
    	PlayableFilter hpFilter = (PlayableFilter) parent.nameToModule.get("HP");
    	int currentBPM = (int) Math.round(BPM.getSample());
    	if(currentBPM != bpm) {
    		bpm = currentBPM;
    		long currentTime = currentTimeInSamples / noteLengthInSamples;
			int noteIndex = (int) currentTime % notes.length;
    		noteLengthInSamples = (int) Math.round(44100.0 / bpm * 60); 
    		noteRestInSamples = (int) Math.round(noteLengthInSamples / 4.0);
    		currentTimeInSamples = noteIndex * noteLengthInSamples;
    	}
		double[] returnVal = new double[numSamples];
		for(int index = 0; index < numSamples; index++) {
			returnVal[index] = 0.0;
		}
		for(int index = 0; index < numSamples; index++) {
			if(currentTimeInSamples % noteLengthInSamples == 0) {
				noteStartInSamples = currentTimeInSamples;
				lpFilter.reset();
				hpFilter.reset();
				currentPhase = 0.0;
				ampENV.noteOn(currentTimeInSamples);
				ampLFO.reset();
				filterENV.noteOn(currentTimeInSamples);
				filterLFO.reset();
				ringAmp1ENV.noteOn(currentTimeInSamples);
				ringAmp2ENV.noteOn(currentTimeInSamples);
				ringAmp3ENV.noteOn(currentTimeInSamples);
			}
			if(currentTimeInSamples % noteLengthInSamples == noteLengthInSamples - noteRestInSamples) {
				ampENV.noteOff(currentTimeInSamples);
				filterENV.noteOff(currentTimeInSamples);
				ringAmp1ENV.noteOff(currentTimeInSamples);
				ringAmp2ENV.noteOff(currentTimeInSamples);
				ringAmp3ENV.noteOff(currentTimeInSamples);
			}
			long currentTime = currentTimeInSamples / noteLengthInSamples;
			int noteIndex = (int) currentTime % notes.length;
			double noteFreq = Math.pow(2.0, notes[noteIndex] / 12.0) * baseFreq;
			double freqRatio = noteFreq / baseFreq;
			double[] pwm = new double[6];
			pwm[0] = osc1CONTROLS.getValue(ControlBank.Name.OSC1PWM);
			pwm[1] = osc2CONTROLS.getValue(ControlBank.Name.OSC2PWM);
			pwm[2] = osc3CONTROLS.getValue(ControlBank.Name.OSC3PWM);
			pwm[3] = ringOsc1CONTROLS.getValue(ControlBank.Name.RING1PWM);
			pwm[4] = ringOsc2CONTROLS.getValue(ControlBank.Name.RING2PWM);
			pwm[5] = ringOsc3CONTROLS.getValue(ControlBank.Name.RING3PWM);
			double[] saw = new double[6];
			saw[0] = osc1CONTROLS.getValue(ControlBank.Name.OSC1Shape);
			saw[1] = osc2CONTROLS.getValue(ControlBank.Name.OSC2Shape);
			saw[2] = osc3CONTROLS.getValue(ControlBank.Name.OSC3Shape);
			saw[3] = ringOsc1CONTROLS.getValue(ControlBank.Name.RING1Shape);
			saw[4] = ringOsc2CONTROLS.getValue(ControlBank.Name.RING2Shape);
			saw[5] = ringOsc3CONTROLS.getValue(ControlBank.Name.RING3Shape);
			double[] ampMod = new double[6];
			ampMod[0] = 1;
			ampMod[1] = 1;
			ampMod[2] = 1;
			ampMod[3] = ringAmp1ENV.getSample(currentTimeInSamples, true);
			ampMod[4] = ringAmp2ENV.getSample(currentTimeInSamples, true);
			ampMod[5] = ringAmp3ENV.getSample(currentTimeInSamples, true);
			double[] freqMod = new double[6];
			freqMod[0] = 1;
			freqMod[1] = osc2CONTROLS.getValue(ControlBank.Name.OSC2FREQ) * osc2CONTROLS.getValue(ControlBank.Name.OSC2DETUNE);
			freqMod[2] = osc3CONTROLS.getValue(ControlBank.Name.OSC3FREQ) * osc3CONTROLS.getValue(ControlBank.Name.OSC3DETUNE);
			freqMod[3] = ringOsc1CONTROLS.getValue(ControlBank.Name.RING1FREQ);
			freqMod[4] = ringOsc2CONTROLS.getValue(ControlBank.Name.RING2FREQ) * freqMod[1];
			freqMod[5] = ringOsc3CONTROLS.getValue(ControlBank.Name.RING3FREQ) * freqMod[2];
			double[] depth = new double[6];
			depth[0] = 1;
			depth[1] = 1;
			depth[2] = 1;
			depth[3] = ringOsc1CONTROLS.getValue(ControlBank.Name.RING1AMT);
			depth[4] = ringOsc2CONTROLS.getValue(ControlBank.Name.RING2AMT);
			depth[5] = ringOsc3CONTROLS.getValue(ControlBank.Name.RING3AMT);
			double[] osc = new double[6];
			for(int i = 0; i < osc.length; i++) {
				osc[i] = waveforms.allSigned(currentPhase * freqMod[i], saw[i], pwm[i]) * depth[i] + (1.0 - depth[i]);
				//osc[i] = waveforms.sawtooth(currentPhase * freqMod[i]) * saw[i] * depth[i] + (1.0 - depth[i]);
				//osc[i] += waveforms.squarewave(currentPhase * freqMod[i], pwm[i]) * (1.0 - saw[i]) * depth[i] + (1.0 - depth[i]);
				osc[i] *= ampMod[i] * depth[i] + (1.0 - depth[i]);
			}
			double[] ring = new double[3];
			for(int i = 3; i < 6; i++) {
				ring[i - 3] = waveforms.sawtooth(currentPhase * freqMod[i]) * saw[i];
				ring[i - 3] += waveforms.squarewave(currentPhase * freqMod[i], pwm[i]) * (1.0 - saw[i]);
			}
	    	double osc1Level = MIXER.getValue(ControlBank.Name.OSC1LEVEL);
	    	double osc2Level = MIXER.getValue(ControlBank.Name.OSC2LEVEL);
	    	double osc3Level = MIXER.getValue(ControlBank.Name.OSC3LEVEL);
	    	double ringAllLevel = MIXER.getValue(ControlBank.Name.RINGALL);
	    	osc[0] = osc[0] * osc[3];
	    	osc[1] = osc[1] * osc[4];
	    	osc[2] = osc[2] * osc[5];
	    	double ringAll = (osc[0] * osc[1] + osc[1] * osc[2] + osc[1] * osc[3]) / 3.0;
			double ampEnv = ampENV.getSample(currentTimeInSamples, false);
			double ampOscVal = ampOSC.getSample();
			for(int i = 0; i < 3; i++) {
				osc[i] *= ampLFO.all(ampOscVal, 1.0);
				osc[i] *= ampEnv;
			}
			ringAll *= ampEnv;
			ampLFO.newSample();
	    	returnVal[index] = osc[0] * osc1Level + osc[1] * osc2Level + osc[2] * osc3Level + ringAll * ringAllLevel;
	    	returnVal[index] /= 4.0;
	    	double filterOscVal = filterOSC.getSample();
			double filter = filterENV.getSample(currentTimeInSamples, true) + filterLFO.allFilter(filterOscVal, 1.0);
			filterLFO.newSample();
			returnVal[index] = lpFilter.getSample(returnVal[index], freqRatio, filter);
			returnVal[index] = hpFilter.getSample(returnVal[index], 1.0, 1.0);
			currentPhase += (2.0 * Math.PI * noteFreq) / AudioFetcher.sampleRate;
			currentTimeInSamples++;
		}
		return returnVal;
	}
	
	public void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		for(int keyIndex = 0; keyIndex < numKeys; keyIndex++) {
			int lowerY = (numKeys - keyIndex) * keyHeight + screenY;
			g2.setColor(keyColors[keyIndex % 12]);
			g2.fillRect(screenX, lowerY - keyHeight, noteWidth, keyHeight);
			g2.setColor(Color.GRAY);
			g2.drawRect(screenX, lowerY - keyHeight, noteWidth, keyHeight);
			for(int noteIndex = 0; noteIndex < notes.length; noteIndex++) {
				g2.setColor(Color.BLACK);
				if(notes[noteIndex] == keyIndex) g2.setColor(Color.RED);
				g2.fillRect(screenX + (noteIndex + 1) * noteWidth, lowerY - keyHeight, noteWidth, keyHeight);
				g2.setColor(Color.GRAY);
				g2.drawRect(screenX + (noteIndex + 1) * noteWidth, lowerY - keyHeight, noteWidth, keyHeight);
			}
		}
	}

	public void pointSelected(int x, int y) {
		for(int keyIndex = 0; keyIndex < numKeys; keyIndex++) {
			int lowerY = (numKeys - keyIndex) * keyHeight + screenY;
			for(int noteIndex = 0; noteIndex < notes.length; noteIndex++) {
				Rectangle rect = new Rectangle(screenX + (noteIndex + 1) * noteWidth, lowerY - keyHeight, noteWidth, keyHeight);
				if(rect.contains(x, y)) {
					notes[noteIndex] = keyIndex;
				}
			}
		}
		parent.view.repaint();
	}

	@Override
	public int getMaxScreenX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void loadModuleInfo(BufferedReader in) {
		try {
			notes = new int[numNotes];
			for(int noteIndex = 0; noteIndex < numNotes; noteIndex++) {
				notes[noteIndex] = new Integer(in.readLine()).intValue();
				tie[noteIndex] = new Boolean(in.readLine()).booleanValue();
				accent[noteIndex] = new Boolean(in.readLine()).booleanValue();
			}
		} catch (Exception e) {
			System.out.println("PlayableSequencer.loadModuleInfo: Error reading from file");
		}
	}

	@Override
	public void saveModuleInfo(BufferedWriter out) {
		try {
			for(int noteIndex = 0; noteIndex < numNotes; noteIndex++) {
				out.write(new Integer(notes[noteIndex]).toString());
				out.newLine();
				out.write(new Boolean(tie[noteIndex]).toString());
				out.newLine();
				out.write(new Boolean(accent[noteIndex]).toString());
				out.newLine();
			}
		} catch (Exception e) {
			System.out.println("PlayableSequencer.saveModuleInfo: Error reading from file");
		}
		
	}
	
}
