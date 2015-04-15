
package main.playable;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Random;

import main.SynthTools;

public class PlayableSequencer implements PlayableModule {
	
	private Random random;
	private Waveforms waveforms;
	private Noise noise;
	private FrequencyToAmplitude fToA1;
	private FrequencyToAmplitude fToA2;
	private FrequencyToAmplitude fToA3;
	private FrequencyToAmplitude fToA4;
	private int screenX;
	private int screenY;
	private static final int numKeys = 24;
	private static final int keyHeight = 12;
	private static final int noteWidth = 30;
	private static final Color[] keyColors = {Color.WHITE, Color.BLACK, Color.WHITE, Color.BLACK, Color.WHITE, Color.WHITE, Color.BLACK, Color.WHITE, Color.BLACK, Color.WHITE, Color.BLACK, Color.WHITE};
	private static final int[] minorScale = {0, 2, 3, 5, 7, 8, 10, 12, 14, 15, 17, 19, 20, 22};
	PlayableEditor parent;
	long currentTimeInSamples = 0;
	public static final double baseFreq = 64.0;
	int[] notes;
	int noteRestInSamples = 44100 / 4;
	int noteLengthInSamples = 44100; 
	double currentPhaseOsc1 = 0.0;
	double currentPhaseOsc2 = 0.0;
	double currentSawtoothVal1 = 0.0;
	double currentSawtoothVal1B = 0.0;
	double currentSawtoothVal2 = 0.0;
	double currentSawtoothVal2B = 0.0;
	double fm6 = 0.0;
	double fm7 = 0.0;
	public volatile boolean newSequence = false;
	double prevOsc2NoteFreq = baseFreq;
	double osc2NoteFreq = baseFreq;
	double glideVal = 0.0;
	
	long ampLFOStartTime = 0;
	long filterLFOStartTime = 0;
	boolean ampLFO_On = false;
	boolean filterLFO_On = false;
	
	double saw1Gain = 0.0;
	double saw2Gain = 0.0;
	double prevOsc1 = 0.0;
	double prevOsc2 = 0.0;
	double prevSaw1 = 0.0;
	double prevSaw2 = 0.0;
	
	// Arduino simulation
	double timePerCycle = 1.0 / AudioFetcher.sampleRate;
	double currentTimeElapsed = 0.0;
	double deltaSawtooth = baseFreq * timePerCycle;
	double sawtoothVal = -1.0;
	double sawtoothClippingVal = 1.0;
	double maxSawtoothClippingVal = 1.0;
	double saveCurrentFreq = baseFreq;
	double freqUpdateTime = 0;
	
	public PlayableSequencer(PlayableEditor parent, int screenX, int screenY) {
		waveforms = new Waveforms();
		random = new Random();
		noise = new Noise();
		fToA1 = new FrequencyToAmplitude();
		fToA2 = new FrequencyToAmplitude();
		fToA3 = new FrequencyToAmplitude();
		fToA4 = new FrequencyToAmplitude();
		this.parent = parent;
		this.screenX = screenX;
		this.screenY = screenY;
		currentTimeInSamples = 0;
		notes = new int[16];
		int currentNote = 7;
		for(int index = 0; index < notes.length; index++) {
			 notes[index] = minorScale[currentNote];
			 int noteStep = random.nextInt(5) - 2;
			 currentNote += noteStep;
			 if(currentNote >= minorScale.length) currentNote -= noteStep * 2;
			 if(currentNote < 0) currentNote -= noteStep * 2;
		}
	}
	
	public synchronized void newSequence() {
    	PlayableFilter lpFilter = (PlayableFilter) parent.nameToModule.get("LP FILTER");
    	lpFilter.reset();
    	PlayableFilter hpFilter = (PlayableFilter) parent.nameToModule.get("HP FILTER");
    	hpFilter.reset();
		currentTimeInSamples = 0;
	}

	public double[] masterGetSamples(int numSamples) {
		ControlBank osc1CONTROLS = (ControlBank) parent.nameToModule.get("OSCILLATOR_1");
		ControlBank osc2CONTROLS = (ControlBank) parent.nameToModule.get("OSCILLATOR_2");
    	PlayableEnvelope fmAR1 = (PlayableEnvelope) parent.nameToModule.get("FM AR1");
    	PlayableEnvelope pwmENV = (PlayableEnvelope) parent.nameToModule.get("PWM ADSR");
    	PlayableLFO pwmLFO = (PlayableLFO) parent.nameToModule.get("PWM LFO");
    	PlayableEnvelope ampAR = (PlayableEnvelope) parent.nameToModule.get("AMP AR");
    	PlayableEnvelope ampASR = (PlayableEnvelope) parent.nameToModule.get("AMP ASR");
    	PlayableControl ampRate = (PlayableControl)  parent.nameToModule.get("AMP LFO RATE");
    	PlayableEnvelope ampLFO = (PlayableEnvelope) parent.nameToModule.get("AMP LFO");
    	PlayableEnvelope filterAR = (PlayableEnvelope) parent.nameToModule.get("FILTER AR");
    	PlayableEnvelope filterASR = (PlayableEnvelope) parent.nameToModule.get("FILTER ASR");
    	PlayableControl filterRate = (PlayableControl)  parent.nameToModule.get("FILTER LFO RATE");
    	PlayableEnvelope filterLFO = (PlayableEnvelope) parent.nameToModule.get("FILTER LFO");
    	PlayableFilter lpFilter = (PlayableFilter) parent.nameToModule.get("LP FILTER");
    	PlayableFilter hpFilter = (PlayableFilter) parent.nameToModule.get("HP FILTER");
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
				ampAR.noteOn(currentTimeInSamples);
				ampASR.noteOn(currentTimeInSamples);
				filterAR.noteOn(currentTimeInSamples);
				filterASR.noteOn(currentTimeInSamples);
				pwmENV.noteOn(currentTimeInSamples);
				fmAR1.noteOn(currentTimeInSamples);
				pwmLFO.reset();
			}
			if(currentTimeInSamples % noteLengthInSamples == noteLengthInSamples - noteRestInSamples) {
				ampAR.noteOff(currentTimeInSamples);
				ampASR.noteOff(currentTimeInSamples);
				filterAR.noteOff(currentTimeInSamples);
				filterASR.noteOff(currentTimeInSamples);
				pwmENV.noteOff(currentTimeInSamples);
				fmAR1.noteOff(currentTimeInSamples);
			}
			long currentTime = currentTimeInSamples / noteLengthInSamples;
			int noteIndex = (int) currentTime % notes.length;
			double noteFreq = Math.pow(2.0, notes[noteIndex] / 12.0) * baseFreq;
			noteFreq *= fmAR1.getSample(currentTimeInSamples) + 1.0;
			double freqRatio = noteFreq / baseFreq;
			double pwmLFOVal = pwmLFO.squarewaveSigned(); 
			double osc1pwm = osc1CONTROLS.getValue(ControlBank.Name.OSC1PWM);
			double osc2pwm = osc2CONTROLS.getValue(ControlBank.Name.OSC2PWM);
			double pwm = pwmLFOVal;
			pwm *= pwmENV.getSample(currentTimeInSamples);
			pwmLFO.newSample();
			double osc1 = waveforms.squarewave(currentPhaseOsc1, pwm + osc1pwm) * 4.0 / 6.0;
			osc1 += osc1CONTROLS.getValue(ControlBank.Name.OSC1F2) * waveforms.squarewave(currentPhaseOsc1 * 2.0) * 2.0 / 6.0;
			osc1 += osc1CONTROLS.getValue(ControlBank.Name.OSC1F4) * waveforms.squarewave(currentPhaseOsc1 * 4.0) * 1.0 / 6.0;
			double osc2 = waveforms.squarewave(currentPhaseOsc2, pwm + osc2pwm) * 0.5;
			osc2 += osc2CONTROLS.getValue(ControlBank.Name.OSC2F2) * waveforms.squarewave(currentPhaseOsc2 * 2.0) * 2.0 / 6.0;
			osc2 += osc2CONTROLS.getValue(ControlBank.Name.OSC2F4) * waveforms.squarewave(currentPhaseOsc2 * 4.0) * 1.0 / 6.0;
			int ampLFORate = 0;
			if(ampRate.getSample() > 0.5) {
				ampLFORate = (int) Math.round(AudioFetcher.sampleRate / (Math.pow(256.0, ampRate.getSample()) * freqRatio)) / 2;
			} else {
				ampLFORate = (int) Math.round(AudioFetcher.sampleRate / (Math.pow(256.0, ampRate.getSample()))) / 2;
			}
			if(ampLFOStartTime >= ampLFORate) {
				if(ampLFO_On) {
					ampLFOStartTime = 0;
					ampLFO.noteOff(currentTimeInSamples);
					ampLFO_On = false;
				} else {
					ampLFOStartTime = 0;
					ampLFO.noteOn(currentTimeInSamples);
					ampLFO_On = true;
				}
			}
			ampLFOStartTime++;
			double amp = (ampAR.getSample(currentTimeInSamples) + ampASR.getSample(currentTimeInSamples)) / 2.0;
			amp *= ampLFO.getSample(currentTimeInSamples);
			osc1 *= amp;
			double osc2Ring = osc2;
			osc2 *= amp;
			int filterLFORate = 0;
			if(filterRate.getSample() > 0.5) {
				filterLFORate = (int) Math.round(AudioFetcher.sampleRate / (Math.pow(256.0, filterRate.getSample()) * freqRatio)) / 2;
			} else {
				filterLFORate = (int) Math.round(AudioFetcher.sampleRate / (Math.pow(256.0, filterRate.getSample()))) / 2;
			}
			if(filterLFOStartTime >= filterLFORate) {
				if(filterLFO_On) {
					filterLFOStartTime = 0;
					filterLFO.noteOff(currentTimeInSamples);
					filterLFO_On = false;
				} else {
					filterLFOStartTime = 0;
					filterLFO.noteOn(currentTimeInSamples);
					filterLFO_On = true;
				}
			}
			filterLFOStartTime++;
			double filter = filterLFO.getSample(currentTimeInSamples);
			filter *= (filterAR.getSample(currentTimeInSamples) + filterASR.getSample(currentTimeInSamples)) / 2.0;
			double subOscVal = osc1CONTROLS.getValue(ControlBank.Name.SUBOSCLevel);
			double ringVal = osc2CONTROLS.getValue(ControlBank.Name.OSC2RING);
			double osc2Val = osc2CONTROLS.getValue(ControlBank.Name.OSC2LEVEL);
			returnVal[index] = (osc1 + osc2 * osc2Val + osc1 * osc2Ring * ringVal + waveforms.squarewave(currentPhaseOsc1 / 2.0) * subOscVal) / 4.0;
			returnVal[index] = lpFilter.getSample(returnVal[index], freqRatio, filter);
			returnVal[index] = hpFilter.getSample(returnVal[index], 1.0, 1.0);
			currentPhaseOsc1 += (2.0 * Math.PI * noteFreq) / AudioFetcher.sampleRate;
			currentPhaseOsc2 += (2.0 * Math.PI * noteFreq * osc2CONTROLS.getValue(ControlBank.Name.OSC2FREQ) * osc2CONTROLS.getValue(ControlBank.Name.OSC2DETUNE)) / AudioFetcher.sampleRate;
			currentTimeInSamples++;
		}
		if(sawtoothClippingVal > maxSawtoothClippingVal) {
			maxSawtoothClippingVal = sawtoothClippingVal;
			System.out.println("Sawtooth Clipping " + maxSawtoothClippingVal);
		}
		return returnVal;
	}
	
	public double arduinoSawtooth(double currentFreqIn) {
		if(Math.round(freqUpdateTime * 1000000.0) % 1000 == 0) {
			saveCurrentFreq = currentFreqIn;
		}
		freqUpdateTime += 1.0 / AudioFetcher.sampleRate;
		if(Math.floor(currentTimeElapsed * 500000) / 500000 > 1.0 / saveCurrentFreq) {
			currentTimeElapsed = 0;
			deltaSawtooth = 2.0 * saveCurrentFreq / AudioFetcher.sampleRate;
			sawtoothVal = -1.0;
			return sawtoothVal;
		}
		currentTimeElapsed += 1.0 / AudioFetcher.sampleRate;
		sawtoothVal += deltaSawtooth;
		if(sawtoothVal > sawtoothClippingVal) {
			sawtoothClippingVal = sawtoothVal;
		}
		if(sawtoothVal > 1.0) {
			return 1.0;
		}
		return sawtoothVal;
	}
	
	public double arduinoSquarewave(double sawoothVal, double pwmVal) {
		pwmVal -= 0.5;
		if(sawtoothVal < pwmVal) return -1.0;
		return 1.0;
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
		parent.view.repaint();
	}

	@Override
	public int getMaxScreenX() {
		// TODO Auto-generated method stub
		return 0;
	}
	
}