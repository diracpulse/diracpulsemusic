
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
	private static double currentFreq = 64.0;
	int[] notes;
	int noteRestInSamples = 44100 / 2;
	int noteLengthInSamples = 44100; 
	double currentPhase = 0.0;
	double currentRingPhase = 0.0;
	public volatile boolean newSequence = false;
	
	public PlayableSequencer(PlayableEditor parent, int screenX, int screenY) {
		waveforms = new Waveforms();
		random = new Random();
		noise = new Noise();
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

	public static double getFreqRatio() {
		return currentFreq / baseFreq;
	}
	
	public double[] masterGetSamples(int numSamples) {
    	PlayableControl mainOSC = (PlayableControl) parent.nameToModule.get("MAIN OSC");
    	PlayableControl noiseColor = (PlayableControl) parent.nameToModule.get("NOISE COLOR");
    	PlayableControl noiseLevel = (PlayableControl) parent.nameToModule.get("NOISE LEVEL");
    	PlayableControl sqrPWM = (PlayableControl) parent.nameToModule.get("SQR PWM");
    	PlayableEnvelope pwmENV1 = (PlayableEnvelope) parent.nameToModule.get("PWM AR");
    	PlayableEnvelope pwmENV2 = (PlayableEnvelope) parent.nameToModule.get("PWM ASR");
    	PlayableControl pwmOSC = (PlayableControl) parent.nameToModule.get("PWM OSC");
    	PlayableLFO pwmLFO = (PlayableLFO) parent.nameToModule.get("PWM LFO");
    	PlayableLFO pwmFastLFO = (PlayableLFO) parent.nameToModule.get("PWM FAST LFO");
    	PlayableEnvelope ampENV1 = (PlayableEnvelope) parent.nameToModule.get("AMP AR");
    	PlayableEnvelope ampENV2 = (PlayableEnvelope) parent.nameToModule.get("AMP ASR");
    	PlayableControl ampOSC = (PlayableControl) parent.nameToModule.get("AMP OSC");
    	PlayableLFO ampLFO = (PlayableLFO) parent.nameToModule.get("AMP LFO");
    	PlayableLFO ampFastLFO = (PlayableLFO) parent.nameToModule.get("AMP FAST LFO");
    	PlayableControl filterOSC = (PlayableControl) parent.nameToModule.get("FILTER OSC");
    	PlayableEnvelope filterENV1 = (PlayableEnvelope) parent.nameToModule.get("FILTER AR");
    	PlayableEnvelope filterENV2 = (PlayableEnvelope) parent.nameToModule.get("FILTER ASR");
    	PlayableLFO filterLFO = (PlayableLFO) parent.nameToModule.get("FILTER LFO");
    	PlayableLFO filterFastLFO = (PlayableLFO) parent.nameToModule.get("FILTER FAST LFO");
    	PlayableControl ringOSC = (PlayableControl) parent.nameToModule.get("RING OSC");
    	PlayableControl ringPWM = (PlayableControl) parent.nameToModule.get("RING PWM");
    	PlayableControl ringFREQ = (PlayableControl) parent.nameToModule.get("RING FREQ");
    	PlayableControl ringLevel = (PlayableControl) parent.nameToModule.get("RING LEVEL");
    	PlayableControl subOSC = (PlayableControl) parent.nameToModule.get("SUB OSC");
    	PlayableFilter lpFilter = (PlayableFilter) parent.nameToModule.get("LP FILTER");
    	PlayableFilter hpFilter = (PlayableFilter) parent.nameToModule.get("HP FILTER");
		double[] returnVal = new double[numSamples];
		for(int index = 0; index < numSamples; index++) {
			returnVal[index] = 0.0;
		}
		for(int index = 0; index < numSamples; index++) {
			if(currentTimeInSamples % noteLengthInSamples == 0) {
				currentPhase = 0.0;
				currentRingPhase = 0.0;
				pwmENV1.noteOn(currentTimeInSamples);
				pwmENV2.noteOn(currentTimeInSamples);
				ampENV1.noteOn(currentTimeInSamples);
				ampENV2.noteOn(currentTimeInSamples);
				filterENV1.noteOn(currentTimeInSamples);
				filterENV2.noteOn(currentTimeInSamples);
				ampLFO.reset();
				filterLFO.reset();
				pwmLFO.reset();
				ampFastLFO.reset();
				filterFastLFO.reset();
				pwmFastLFO.reset();
			}
			if(currentTimeInSamples % noteLengthInSamples == noteLengthInSamples - noteRestInSamples) {
				pwmENV1.noteOff(currentTimeInSamples);
				pwmENV2.noteOff(currentTimeInSamples);
				ampENV1.noteOff(currentTimeInSamples);
				ampENV2.noteOff(currentTimeInSamples);
				filterENV1.noteOff(currentTimeInSamples);
				filterENV2.noteOff(currentTimeInSamples);
			}
			long currentTime = currentTimeInSamples / noteLengthInSamples;
			int noteIndex = (int) currentTime % notes.length;
			currentFreq = Math.pow(2.0, notes[noteIndex] / 12.0) * baseFreq;
			double freqRatio = currentFreq / baseFreq;
			double pwmSquareVal = pwmOSC.getSample();
			double pwmLFOVal = pwmLFO.squarewaveFilter() * pwmSquareVal + pwmLFO.triangleFilter() * (1.0 - pwmSquareVal); 
			double pwmFastLFOVal = pwmFastLFO.sineFilter();
			//double pwm = (pwmENV1.getSample(currentTimeInSamples) + pwmENV2.getSample(currentTimeInSamples)) / 2.0;
			double pwm = (pwmLFOVal + pwmFastLFOVal + pwmENV1.getSample(currentTimeInSamples) + pwmENV2.getSample(currentTimeInSamples)) / 4.0;
			pwm += sqrPWM.getSample();
			pwmLFO.newSample();
			pwmFastLFO.newSample(freqRatio);
			double sawtoothLevel = mainOSC.getSample();
			double main = waveforms.sawtooth(currentPhase) * sawtoothLevel + waveforms.squarewave(currentPhase, pwm) * (1.0 - sawtoothLevel);
			//returnVal[index] = main;
			double noiseVal = noiseLevel.getSample();
			double noiseType = noiseColor.getSample();
			main = noise.getSample(noiseType) * noiseVal + main * (1.0 - noiseVal);
			double ampSquareVal = ampOSC.getSample();
			double ampLFOVal = ampLFO.squarewave() * ampSquareVal + ampLFO.triangle() * (1.0 - ampSquareVal); 
			double ampFastLFOVal = ampFastLFO.squarewave() * ampSquareVal + ampFastLFO.triangle() * (1.0 - ampSquareVal); 
			double amp = (ampLFOVal * ampFastLFOVal);
			ampLFO.newSample();
			ampFastLFO.newSample(freqRatio);
			main *= amp;
			double ringpwm = ringPWM.getSample();
			double ringSawValue = ringOSC.getSample();
			double ring = waveforms.sawtooth(currentRingPhase) * ringSawValue + waveforms.squarewave(currentRingPhase, ringpwm) * (1.0 - ringSawValue);
			ring *= main;
			double ringLevelVal = ringLevel.getSample();
			returnVal[index] = (ring * ringLevelVal + main * (1.0 - ringLevelVal)) * (ampENV1.getSample(currentTimeInSamples) + ampENV2.getSample(currentTimeInSamples)) / 2.0;		
			double filterSquareVal = filterOSC.getSample();
			double filterLFOVal = filterLFO.squarewaveFilter() * filterSquareVal + filterLFO.triangleFilter() * (1.0 - filterSquareVal); 
			double filterFastLFOVal = filterFastLFO.squarewaveFilter() * filterSquareVal + filterFastLFO.triangleFilter() * (1.0 - filterSquareVal); 
			double filter = (filterENV1.getSample(currentTimeInSamples) + filterENV2.getSample(currentTimeInSamples));
			filter += filterLFOVal + filterFastLFOVal;
			filterLFO.newSample();
			filterFastLFO.newSample(freqRatio);
			returnVal[index] = lpFilter.getSample(returnVal[index], freqRatio, filter);
			returnVal[index] = hpFilter.getSample(returnVal[index], 1.0, 1.0);
			currentPhase += currentFreq / AudioFetcher.sampleRate * Math.PI * 2.0;
			currentRingPhase += freqRatio * ringFREQ.getSample() / AudioFetcher.sampleRate * Math.PI * 2.0;
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
		parent.view.repaint();
	}

	@Override
	public int getMaxScreenX() {
		// TODO Auto-generated method stub
		return 0;
	}
	
}