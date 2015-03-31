
package main.playable;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Random;

import main.SynthTools;
import main.playable.PlayableLFO.WaveformType;

public class PlayableSequencer implements PlayableModule {
	
	private Waveforms waveforms;
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
	
	public PlayableSequencer(PlayableEditor parent, int screenX, int screenY) {
		waveforms = new Waveforms();
		Random random = new Random();
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

	public static double getFreqRatio() {
		return currentFreq / baseFreq;
	}
	
	public double[] masterGetSamples(int numSamples) {
    	PlayableControl mainOSC = (PlayableControl) parent.nameToModule.get("MAIN OSC");
    	PlayableControl sqrPWM = (PlayableControl) parent.nameToModule.get("SQR PWM");
    	PlayableControl ampOSC = (PlayableControl) parent.nameToModule.get("AMP OSC");
    	PlayableLFO ampLFO = (PlayableLFO) parent.nameToModule.get("AMP LFO");
    	PlayableEnvelope ampENV = (PlayableEnvelope) parent.nameToModule.get("AMP ENV");
    	PlayableControl filterOSC = (PlayableControl) parent.nameToModule.get("FILTER OSC");
    	PlayableLFO filterLFO = (PlayableLFO) parent.nameToModule.get("FILTER LFO");
    	PlayableEnvelope filterENV = (PlayableEnvelope) parent.nameToModule.get("FILTER ENV");
    	PlayableControl ringOSC = (PlayableControl) parent.nameToModule.get("RING OSC");
    	PlayableControl ringFREQ = (PlayableControl) parent.nameToModule.get("RING FREQ");
    	PlayableEnvelope ringENV =  (PlayableEnvelope) parent.nameToModule.get("RING ENV");
    	PlayableControl mixer = (PlayableControl) parent.nameToModule.get("MIXER");
    	PlayableFilter lpFilter = (PlayableFilter) parent.nameToModule.get("LP FILTER");
		double[] returnVal = new double[numSamples];
		for(int index = 0; index < numSamples; index++) {
			if(currentTimeInSamples % noteLengthInSamples == 0) {
				currentPhase = 0.0;
				currentRingPhase = 0.0;
				ampENV.noteOn(currentTimeInSamples);
				filterENV.noteOn(currentTimeInSamples);
				ringENV.noteOn(currentTimeInSamples);
				ampLFO.reset();
				filterLFO.reset();
			}
			if(currentTimeInSamples % noteLengthInSamples == noteLengthInSamples - noteRestInSamples) {
				ampENV.noteOff(currentTimeInSamples);
				filterENV.noteOff(currentTimeInSamples);
				ringENV.noteOff(currentTimeInSamples);
			}
			long currentTime = currentTimeInSamples / noteLengthInSamples;
			int noteIndex = (int) currentTime % notes.length;
			currentFreq = Math.pow(2.0, notes[noteIndex] / 12.0) * baseFreq;
			double freqRatio = currentFreq / baseFreq;
			double sawtoothLevel = mainOSC.getSample();
			double pwm = sqrPWM.getSample();
			double main = waveforms.sawtooth(currentPhase) * sawtoothLevel + waveforms.squarewave(currentPhase, pwm) * (1.0 - sawtoothLevel);
			double ringSineLevel = ringOSC.getSample();
			double ring = Math.sin(currentRingPhase) * ringSineLevel + waveforms.triangle(currentRingPhase) * (1.0 - ringSineLevel);
			ring *= ringENV.getSample(currentTimeInSamples);
			ring *= main;
			double ampLFOSineLevel = ampOSC.getSample();
			double ampLFOVal = ampLFO.sine() * ampLFOSineLevel + ampLFO.triangle() * (1.0 - ampLFOSineLevel);
			ampLFO.newSample(freqRatio);
			main *= (ampENV.getSample(currentTimeInSamples) + ampLFOVal) / 2.0;
			double mixerVal = mixer.getSample();
			returnVal[index] = ring * mixerVal + main * (1.0 - mixerVal);
			double filterLFOSineLevel = filterOSC.getSample();
			double filterLFOVal = filterLFO.sineFilter() * filterLFOSineLevel + filterLFO.triangleFilter() * (1.0 - filterLFOSineLevel);
			filterLFO.newSampleFilter(freqRatio);
			double filterInput = (filterENV.getFilterSample(currentTimeInSamples) + filterLFOVal) / 2.0;
			returnVal[index] = lpFilter.getSample(returnVal[index], freqRatio, filterInput);
			currentPhase += currentFreq / AudioFetcher.sampleRate * Math.PI * 2.0;
			currentRingPhase += freqRatio * ringFREQ.getSample() / AudioFetcher.sampleRate * Math.PI * 2.0;
			currentTimeInSamples++;
		}
		return returnVal;
	}
	
	public double sawtooth(double phase) {
		phase -= Math.floor(phase / (Math.PI * 2.0)) * Math.PI * 2.0;
		if(phase < Math.PI) return phase / Math.PI;
		if(phase > Math.PI) return -1.0 + (phase - Math.PI) / Math.PI;
		return Math.random() * 2.0 - 1.0; // phase == Math.PI
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