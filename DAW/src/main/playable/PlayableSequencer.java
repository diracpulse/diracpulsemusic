
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
	public static final double baseFreq = 256.0;
	int numNotes = 16;
	boolean[] tie;
	boolean[] accent;
	int[] notes;
	int bpm = 120;
	int noteLengthInSamples = (int) Math.round(44100.0 / bpm * 60); 
	int noteRestInSamples = (int) Math.round(noteLengthInSamples / 4.0); 
	double currentPhaseOsc1 = 0.0;
	double currentPhaseOsc2 = 0.0;
	public volatile boolean newSequence = false;
	double prevOsc2NoteFreq = baseFreq;
	double osc2NoteFreq = baseFreq;
	double glideVal = 0.0;
	
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
			 notes[index] = minorScale[currentNote];
			 tie[index] = false;
			 accent[index] = false;
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

	public double[] masterGetSamples(int numSamples) {
		PlayableControl BPM = (PlayableControl) parent.nameToModule.get("BPM");
		ControlBank osc1CONTROLS = (ControlBank) parent.nameToModule.get("OSC 1");
		ControlBank osc2CONTROLS = (ControlBank) parent.nameToModule.get("OSC 2");
    	PlayableEnvelope fmENV = (PlayableEnvelope) parent.nameToModule.get("FM ADSR");
    	PlayableControl fmOSC = (PlayableControl) parent.nameToModule.get("FM OSC");
    	PlayableLFO fmLFO = (PlayableLFO) parent.nameToModule.get("FM LFO");
    	PlayableEnvelope pwmENV = (PlayableEnvelope) parent.nameToModule.get("PWM ADSR");
    	PlayableControl pwmOSC = (PlayableControl) parent.nameToModule.get("PWM OSC");
    	PlayableLFO pwmLFO = (PlayableLFO) parent.nameToModule.get("PWM LFO");
    	PlayableEnvelope ampENV = (PlayableEnvelope) parent.nameToModule.get("AMP ADSR");
    	PlayableControl ampOSC = (PlayableControl) parent.nameToModule.get("AMP OSC");
    	PlayableLFO ampLFO = (PlayableLFO) parent.nameToModule.get("AMP LFO");
    	PlayableEnvelope filterENV = (PlayableEnvelope) parent.nameToModule.get("FILTER ADSR");
    	PlayableControl filterOSC = (PlayableControl) parent.nameToModule.get("FILTER OSC");
    	PlayableLFO filterLFO = (PlayableLFO) parent.nameToModule.get("FILTER LFO");
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
				lpFilter.reset();
				hpFilter.reset();
				currentPhaseOsc1 = 0.0;
				currentPhaseOsc2 = 0.0;
				ampENV.noteOn(currentTimeInSamples);
				ampLFO.reset();
				filterENV.noteOn(currentTimeInSamples);
				filterLFO.reset();
				pwmENV.noteOn(currentTimeInSamples);
				pwmLFO.reset();
				fmENV.noteOn(currentTimeInSamples);
				fmLFO.reset();
			}
			if(currentTimeInSamples % noteLengthInSamples == noteLengthInSamples - noteRestInSamples) {
				ampENV.noteOff(currentTimeInSamples);
				filterENV.noteOff(currentTimeInSamples);
				pwmENV.noteOff(currentTimeInSamples);
				fmENV.noteOff(currentTimeInSamples);
			}
			long currentTime = currentTimeInSamples / noteLengthInSamples;
			int noteIndex = (int) currentTime % notes.length;
			double noteFreq = Math.pow(2.0, notes[noteIndex] / 12.0) * baseFreq;
			double freqRatio = noteFreq / baseFreq;
			double osc2Ratio = osc2CONTROLS.getValue(ControlBank.Name.OSC2FREQ) * osc2CONTROLS.getValue(ControlBank.Name.OSC2DETUNE) / noteFreq;
			double fmOscVal = fmOSC.getSample();
			double fmVal1 = fmLFO.allSigned(fmOscVal, 1.0) * fmENV.getSample(currentTimeInSamples, true);
			double fmVal2 = fmLFO.allSigned(fmOscVal, osc2Ratio) * fmENV.getSample(currentTimeInSamples, true);
			fmVal2 *= osc2CONTROLS.getValue(ControlBank.Name.OSC2FMAmt);
			if(fmLFO.getFreq() < 16.0) {
				fmLFO.newSample();
			} else {
				fmLFO.newSample(freqRatio);
			}
			double pwmOscVal = pwmOSC.getSample();
			double pwm1 = pwmLFO.allSigned(pwmOscVal, 1.0) * pwmENV.getSample(currentTimeInSamples, false);
			double pwm2 = pwmLFO.allSigned(pwmOscVal, osc2Ratio) * pwmENV.getSample(currentTimeInSamples, false);
			pwm2 *= osc2CONTROLS.getValue(ControlBank.Name.OSC2PWMAmt);
			if(pwmLFO.getFreq() < 16.0) {
				pwmLFO.newSample();
			} else {
				pwmLFO.newSample(freqRatio);
			}
			double osc1pwm = osc1CONTROLS.getValue(ControlBank.Name.OSC1PWM);
			double osc2pwm = osc2CONTROLS.getValue(ControlBank.Name.OSC2PWM);
			double osc1saw = osc1CONTROLS.getValue(ControlBank.Name.OSC1Shape);
			double osc2saw = osc2CONTROLS.getValue(ControlBank.Name.OSC2Shape);
			double osc2 = waveforms.sawtooth(currentPhaseOsc2 + fmVal2) * osc2saw;
			osc2 += waveforms.squarewave(currentPhaseOsc2 + fmVal2, osc2pwm + pwm2) * (1.0 - osc2saw);
			double osc1 = waveforms.sawtooth(currentPhaseOsc1 + fmVal1) * osc1saw;
			osc1 += waveforms.squarewave(currentPhaseOsc1 + fmVal1, osc1pwm + pwm1) * (1.0 - osc1saw);
			double ampEnv = ampENV.getSample(currentTimeInSamples, false);
			double ampOscVal = ampOSC.getSample();
			double amp1 = ampLFO.all(ampOscVal, 1.0);
			double amp2 = ampLFO.all(ampOscVal, osc2Ratio);
			amp2 *= osc2CONTROLS.getValue(ControlBank.Name.OSC2AMPAmt);
			if(ampLFO.getFreq() < 16.0) {
				ampLFO.newSample();
			} else {
				ampLFO.newSample(freqRatio);
			}
			double ring = osc1 * osc2 * ampEnv;
			osc1 *= amp1 * ampEnv;
			osc2 *= amp2 * ampEnv;
			double filterOscVal = filterOSC.getSample();
			double filter = filterENV.getSample(currentTimeInSamples, false) + filterLFO.allFilter(filterOscVal, 1.0);
			if(filterLFO.getFreq() < 16.0) {
				filterLFO.newSample();
			} else {
				filterLFO.newSample(freqRatio);
			}
			double ringVal = osc2CONTROLS.getValue(ControlBank.Name.OSC2RING);
			double osc2Val = osc2CONTROLS.getValue(ControlBank.Name.OSC2LEVEL);
			returnVal[index] = (osc1 + osc2 * osc2Val + ring * ringVal) / 3.0;
			returnVal[index] = lpFilter.getSample(returnVal[index], freqRatio, filter);
			returnVal[index] = hpFilter.getSample(returnVal[index], 1.0, 1.0);
			currentPhaseOsc1 += (2.0 * Math.PI * noteFreq) / AudioFetcher.sampleRate;
			currentPhaseOsc2 += (2.0 * Math.PI * noteFreq * osc2CONTROLS.getValue(ControlBank.Name.OSC2FREQ) * osc2CONTROLS.getValue(ControlBank.Name.OSC2DETUNE)) / AudioFetcher.sampleRate;
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
