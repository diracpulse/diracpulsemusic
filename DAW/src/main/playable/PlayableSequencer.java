
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
    	PlayableEnvelope fmADSR = (PlayableEnvelope) parent.nameToModule.get("FM ADSR");
    	PlayableEnvelope fmAR1 = (PlayableEnvelope) parent.nameToModule.get("FM AR1");
    	PlayableLFO fmLFO = (PlayableLFO) parent.nameToModule.get("FM LFO");
    	PlayableControl fmOSC = (PlayableControl) parent.nameToModule.get("FM OSC");
    	ControlBank fmCONTROLS = (ControlBank) parent.nameToModule.get("FM_OPERATOR_VALUES");
    	PlayableEnvelope pwmENV = (PlayableEnvelope) parent.nameToModule.get("PWM ADSR");
    	PlayableControl pwmOSC = (PlayableControl) parent.nameToModule.get("PWM OSC");
    	PlayableLFO pwmLFO = (PlayableLFO) parent.nameToModule.get("PWM LFO");
    	PlayableLFO pwmFastLFO = (PlayableLFO) parent.nameToModule.get("PWM FAST LFO");
    	PlayableEnvelope ampENV = (PlayableEnvelope) parent.nameToModule.get("AMP ADSR");
    	PlayableControl ampOSC = (PlayableControl) parent.nameToModule.get("AMP OSC");
    	PlayableLFO ampLFO = (PlayableLFO) parent.nameToModule.get("AMP LFO");
    	PlayableLFO ampFastLFO = (PlayableLFO) parent.nameToModule.get("AMP FAST LFO");
    	PlayableControl filterOSC = (PlayableControl) parent.nameToModule.get("FILTER OSC");
    	PlayableEnvelope filterENV = (PlayableEnvelope) parent.nameToModule.get("FILTER ADSR");
    	PlayableLFO filterLFO = (PlayableLFO) parent.nameToModule.get("FILTER LFO");
    	PlayableLFO filterFastLFO = (PlayableLFO) parent.nameToModule.get("FILTER FAST LFO");
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
				fmADSR.noteOn(currentTimeInSamples);
				ampENV.noteOn(currentTimeInSamples);
				filterENV.noteOn(currentTimeInSamples);
				pwmENV.noteOn(currentTimeInSamples);
				fmAR1.noteOn(currentTimeInSamples);
				ampLFO.reset();
				ampFastLFO.reset();
				filterLFO.reset();
				filterFastLFO.reset();
				pwmLFO.reset();
				pwmFastLFO.reset();
				fmLFO.reset();
			}
			if(currentTimeInSamples % noteLengthInSamples == noteLengthInSamples - noteRestInSamples) {
				fmADSR.noteOff(currentTimeInSamples);
				ampENV.noteOff(currentTimeInSamples);
				filterENV.noteOff(currentTimeInSamples);
				pwmENV.noteOff(currentTimeInSamples);
				fmAR1.noteOn(currentTimeInSamples);
			}
			long currentTime = currentTimeInSamples / noteLengthInSamples;
			int noteIndex = (int) currentTime % notes.length;
			double noteFreq = Math.pow(2.0, notes[noteIndex] / 12.0) * baseFreq;
			noteFreq *= fmAR1.getSample(currentTimeInSamples) + 1.0;
			double freqRatio = noteFreq / baseFreq;
			double innerFMVal = currentTimeInSamples / 44100.0 * Math.PI * 2.0 * noteFreq;
			double fmSin = fmCONTROLS.getValue(ControlBank.Name.FM3Mod) * Math.sin(innerFMVal * fmCONTROLS.getValue(ControlBank.Name.FM3Ratio));
			fmSin += fmCONTROLS.getValue(ControlBank.Name.FM2Mod) * Math.sin(innerFMVal * fmCONTROLS.getValue(ControlBank.Name.FM2Ratio));
			fmSin += fmCONTROLS.getValue(ControlBank.Name.FM1Mod) * Math.sin(innerFMVal * fmCONTROLS.getValue(ControlBank.Name.FM1Ratio));
			double fmTri = fmCONTROLS.getValue(ControlBank.Name.FM3Mod) * waveforms.triangle(innerFMVal * fmCONTROLS.getValue(ControlBank.Name.FM3Ratio));
			fmTri += fmCONTROLS.getValue(ControlBank.Name.FM2Mod) * waveforms.triangle(innerFMVal * fmCONTROLS.getValue(ControlBank.Name.FM2Ratio));
			fmTri += fmCONTROLS.getValue(ControlBank.Name.FM1Mod) * waveforms.triangle(innerFMVal * fmCONTROLS.getValue(ControlBank.Name.FM1Ratio));
			fmTri *= fmADSR.getSample(currentTimeInSamples);
			double fm = (fmTri * fmOSC.getSample() + fmSin *  (1.0 - fmOSC.getSample())) * fmLFO.sine();
			fmLFO.newSample();
			fm *= fmADSR.getSample(currentTimeInSamples) / 3.0;
			double pwmSquareVal = pwmOSC.getSample();
			double pwmLFOVal = pwmLFO.squarewaveSigned() * pwmSquareVal + pwmLFO.triangleSigned() * (1.0 - pwmSquareVal); 
			double pwmFastLFOVal = pwmFastLFO.squarewaveSigned() * pwmSquareVal + pwmFastLFO.triangleSigned() * (1.0 - pwmSquareVal); 
			double osc1pwm = osc1CONTROLS.getValue(ControlBank.Name.OSC1PWM);
			double osc2pwm = osc2CONTROLS.getValue(ControlBank.Name.OSC2PWM);
			double pwm = (pwmLFOVal + pwmFastLFOVal) / 2.0;
			pwm *= pwmENV.getSample(currentTimeInSamples);
			pwmLFO.newSample();
			pwmFastLFO.newSample(freqRatio);
			double osc1fm = osc1CONTROLS.getValue(ControlBank.Name.OSC1FM) * fm;
			double osc2fm = osc2CONTROLS.getValue(ControlBank.Name.OSC2FM) * fm;
			double osc1Sawtooth = osc1CONTROLS.getValue(ControlBank.Name.OSC1Shape);
			double osc1 = waveforms.sawtooth4bit(currentPhaseOsc1 + osc1fm, pwm + osc1pwm) * osc1Sawtooth + waveforms.squarewave(currentPhaseOsc1 + osc1fm, pwm + osc1pwm) * (1.0 - osc1Sawtooth);
			double osc2Sawtooth = osc2CONTROLS.getValue(ControlBank.Name.OSC2Shape);
			double osc2 = waveforms.sawtooth4bit(currentPhaseOsc2 + osc2fm, pwm + osc2pwm) * osc2Sawtooth + waveforms.squarewave(currentPhaseOsc2 + osc2fm, pwm + osc2pwm) * (1.0 - osc2Sawtooth);
			double ampSquareVal = ampOSC.getSample();
			double ampLFOVal = ampLFO.squarewave() * ampSquareVal + ampLFO.triangle() * (1.0 - ampSquareVal); 
			double ampFastLFOVal = ampFastLFO.squarewave() * ampSquareVal + ampFastLFO.triangle() * (1.0 - ampSquareVal); 
			double amp = (ampLFOVal + ampFastLFOVal) / 2.0;
			amp *= ampENV.getSample(currentTimeInSamples);
			ampLFO.newSample();
			ampFastLFO.newSample(freqRatio);
			osc1 *= amp;
			double osc2Ring = osc2;
			osc2 *= amp;
			double filterSquareVal = filterOSC.getSample();
			double filterLFOVal = filterLFO.squarewaveFilter() * filterSquareVal + filterLFO.triangleFilter() * (1.0 - filterSquareVal); 
			double filterFastLFOVal = filterFastLFO.squarewaveFilter() * filterSquareVal + filterFastLFO.triangleFilter() * (1.0 - filterSquareVal); 
			double filterEnv = filterENV.getSample(currentTimeInSamples);
			double filter = (filterLFOVal + filterFastLFOVal) * filterEnv + filterEnv;
			filterLFO.newSample();
			filterFastLFO.newSample();
			double noiseVal = osc1CONTROLS.getValue(ControlBank.Name.NOISE_LEVEL);
			double noiseColor = osc1CONTROLS.getValue(ControlBank.Name.NOISE_COLOR);
			double subOscVal = osc1CONTROLS.getValue(ControlBank.Name.SUBOSCLevel);
			double ringVal = osc2CONTROLS.getValue(ControlBank.Name.OSC2RING);
			double osc2Val = osc2CONTROLS.getValue(ControlBank.Name.OSC2LEVEL);
			double osc1Val = osc1CONTROLS.getValue(ControlBank.Name.OSC1LEVEL);
			returnVal[index] = (noise.getSample(noiseColor) * noiseVal + osc1 * osc1Val + osc2 * osc2Val + osc1 * osc2Ring * ringVal + waveforms.squarewave(currentPhaseOsc1 / 2.0) * subOscVal) / 5.0;
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