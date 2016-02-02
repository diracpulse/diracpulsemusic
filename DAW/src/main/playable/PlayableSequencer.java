
package main.playable;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
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
	private static final int keyHeight = 12;
	private static final int noteWidth = 30;
	private static final Color[] keyColors = {Color.WHITE, Color.BLACK, Color.WHITE, Color.BLACK, Color.WHITE, Color.WHITE, Color.BLACK, Color.WHITE, Color.BLACK, Color.WHITE, Color.BLACK, Color.WHITE};
	private static final int[] minorScale = {0, 2, 3, 5, 7, 8, 10, 12, 14, 15, 17, 19, 20, 22};
	PlayableEditor parent;
	long currentTimeInSamples = 0;
	public static double baseFreq = 256.0;
	int maxNotes = 16;
	int sequenceLength = 16;
	boolean[] tie;
	boolean[] accent;
	boolean accentOn = false;
	int[] notes;
	int bpm = 120;
	int noteLengthInSamples = (int) Math.round(44100.0 / bpm * 60); 
	int noteRestInSamples = (int) Math.round(noteLengthInSamples / 4.0);
	//long noteStartInSamples = 0;
	long accentTimeInSamples = 882;
	double currentPhase;
	double[] osc = new double[4];
	public volatile boolean newSequence = false;
	double prevOsc2NoteFreq = baseFreq;
	double osc2NoteFreq = baseFreq;
	double glideVal = 0.0;
	double deltaGlideVal = 0.0;
	double noteFreq = baseFreq;
	double restVal = 0.0;
	
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
		notes = new int[maxNotes];
		tie = new boolean[maxNotes];
		accent = new boolean[maxNotes];
		int currentNote = 7;
		for(int index = 0; index < notes.length; index++) {
			 accent[index] = false; // random.nextBoolean();
			 tie[index] = false;
			 notes[index] = minorScale[currentNote];
			 int noteStep = random.nextInt(5) - 2;
			 currentNote += noteStep;
			 if(currentNote >= minorScale.length) currentNote -= noteStep * 2;
			 if(currentNote < 0) currentNote -= noteStep * 2;
		}
	}
	
	public synchronized void reset() {
		/*
    	PlayableFilter lpFilter = (PlayableFilter) parent.nameToModule.get("LP");
    	lpFilter.reset();
    	PlayableFilter hpFilter = (PlayableFilter) parent.nameToModule.get("HP");
    	hpFilter.reset();
		*/
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
		ControlBank osc4CONTROLS = (ControlBank) parent.nameToModule.get("OSC4");
		PlayableLFO pwmLFO1 = (PlayableLFO) parent.nameToModule.get("PWM LFO 1");
		PlayableLFO pwmLFO2 = (PlayableLFO) parent.nameToModule.get("PWM LFO 2");
		PlayableLFO pwmLFO3 = (PlayableLFO) parent.nameToModule.get("PWM LFO 3");
		PlayableLFO pwmLFO4 = (PlayableLFO) parent.nameToModule.get("PWM LFO 4");
    	PlayableEnvelope ampENV = (PlayableEnvelope) parent.nameToModule.get("AMP AHDSR");
    	PlayableEnvelope filterENV = (PlayableEnvelope) parent.nameToModule.get("FLTR AHDSR");
    	PlayableEnvelope resENV = (PlayableEnvelope) parent.nameToModule.get("RES AHDSR");
    	PlayableFilter lpFilter1 = (PlayableFilter) parent.nameToModule.get("LP1");
    	PlayableFilter lpFilter2 = (PlayableFilter) parent.nameToModule.get("LP2");
    	PlayableFilter lpFilter3 = (PlayableFilter) parent.nameToModule.get("LP3");
    	PlayableFilter lpFilter4 = (PlayableFilter) parent.nameToModule.get("LP4");
    	PlayableFilter hpFilter = (PlayableFilter) parent.nameToModule.get("HP");
    	//PlayableEnvelope accentENV = new PlayableEnvelope(PlayableEnvelope.EnvelopeType.INVISIBLE);
    	int currentBPM = (int) Math.round(BPM.getSample());
    	if(currentBPM != bpm) {
    		bpm = currentBPM;
    		long currentTime = currentTimeInSamples / noteLengthInSamples;
			int noteIndex = (int) currentTime % sequenceLength;
    		noteLengthInSamples = (int) Math.round(44100.0 / bpm * 60); 
    		noteRestInSamples = (int) Math.round(noteLengthInSamples / 4.0);
    		currentTimeInSamples = noteIndex * noteLengthInSamples;
    	}
		double[] returnVal = new double[numSamples];
		for(int index = 0; index < numSamples; index++) {
			returnVal[index] = 0.0;
		}
		for(int index = 0; index < numSamples; index++) {
			long currentTime = currentTimeInSamples / noteLengthInSamples;
			int noteIndex = (int) currentTime % sequenceLength;
			if(currentTimeInSamples % noteLengthInSamples == 0) {
				if(notes[noteIndex] == -1) {
					restVal = 0.0;
				} else {
					restVal = 1.0;
				}
				glideVal = 1.0;
				deltaGlideVal = 0.0;
				if(notes[noteIndex] != -1) {
					noteFreq = Math.pow(2.0, notes[noteIndex] / 12.0) * baseFreq;
					int prevNoteIndex = (noteIndex - 1) % sequenceLength;
					if(prevNoteIndex < 0) prevNoteIndex += sequenceLength;
					int midiNoteFreq = 60;
					midiNoteFreq += notes[(noteIndex + 1) % sequenceLength];
					if(baseFreq == 64.0) {
						midiNoteFreq = 36;
						midiNoteFreq += notes[(noteIndex + 1) % sequenceLength];
					}
					ArrayList<Integer> data = new ArrayList<Integer>();
					data.add(0b10010000);
					data.add(midiNoteFreq + 3);
					parent.sendSerialPortData(data);
					if(notes[prevNoteIndex] == -1 || (notes[prevNoteIndex] != notes[noteIndex]) || !tie[prevNoteIndex]) {
						//lpFilter1.reset();
						//lpFilter2.reset();
						//lpFilter3.reset();
						//lpFilter4.reset();
						//hpFilter.reset();
						currentPhase /= Math.PI * 2.0;
						currentPhase -= Math.floor(currentPhase);
						currentPhase *= Math.PI * 2.0;
						ampENV.noteOn(currentTimeInSamples);
						filterENV.noteOn(currentTimeInSamples);
						resENV.noteOn(currentTimeInSamples);
						pwmLFO1.reset();
						pwmLFO2.reset();
						pwmLFO3.reset();
						pwmLFO4.reset();
					}
				}
			}
			if(currentTimeInSamples % noteLengthInSamples == noteLengthInSamples - noteRestInSamples) {
				if(notes[noteIndex] != -1) {
					if(!tie[noteIndex]) {
						ampENV.noteOff(currentTimeInSamples);
						filterENV.noteOff(currentTimeInSamples);
						resENV.noteOff(currentTimeInSamples);
					}
					if(tie[noteIndex] && !(notes[(noteIndex + 1) % sequenceLength] == -1)) {
						glideVal = 1.0;
						double startFreq = Math.pow(2.0, notes[noteIndex] / 12.0);
						double endFreq = Math.pow(2.0, notes[(noteIndex + 1) % sequenceLength] / 12.0); 
						deltaGlideVal = (Math.pow(2.0, endFreq / startFreq) - 2.0) / noteRestInSamples;
					}
				}
			}
			double currentNoteFreq = noteFreq * glideVal;
			glideVal += deltaGlideVal;
			double freqRatio = currentNoteFreq / baseFreq;
			double[] pwm = new double[4];
			pwm[0] = osc1CONTROLS.getValue(ControlBank.Name.OSC1PWMFixed) + pwmLFO1.sineFilter();
			pwm[1] = osc2CONTROLS.getValue(ControlBank.Name.OSC2PWMFixed) + pwmLFO2.sineFilter();
			pwm[2] = osc3CONTROLS.getValue(ControlBank.Name.OSC3PWMFixed) + pwmLFO3.sineFilter();
			pwm[3] = osc4CONTROLS.getValue(ControlBank.Name.OSC4PWMFixed) + pwmLFO4.sineFilter();
			pwmLFO1.newSample();
			pwmLFO2.newSample();
			pwmLFO3.newSample();
			pwmLFO4.newSample();
			double[] ampMod = new double[6];
			ampMod[0] = 1;
			ampMod[1] = 1;
			ampMod[2] = 1;
			ampMod[3] = 1;
			double[] freqMod = new double[4];
			freqMod[0] = osc1CONTROLS.getValue(ControlBank.Name.OSC1Detune);
			freqMod[1] = osc2CONTROLS.getValue(ControlBank.Name.OSC2Detune);
			freqMod[2] = osc3CONTROLS.getValue(ControlBank.Name.OSC3Detune);
			freqMod[3] = osc4CONTROLS.getValue(ControlBank.Name.OSC4Detune);
			double ampEnv = ampENV.getSample(currentTimeInSamples, false);
			double filterEnv = filterENV.getSample(currentTimeInSamples, false);
			double resEnv = resENV.getSample(currentTimeInSamples, false);
			double[] osc = new double[4];
			osc[0] = waveforms.sawtooth(currentPhase * freqMod[0]) * restVal * ampEnv;
			osc[0] = lpFilter1.getSample(osc[0], freqRatio * filterEnv, resEnv); 
			osc[1] = waveforms.sawtooth(currentPhase * freqMod[1] * 0.5) * restVal * ampEnv;
			osc[1] = lpFilter2.getSample(osc[1] - 0.5, freqRatio * filterEnv, resEnv); 
			osc[2] = waveforms.sawtooth(currentPhase * freqMod[2] * 0.25) * restVal * ampEnv;
			osc[2] = lpFilter3.getSample(osc[2] - 0.5, freqRatio * filterEnv, resEnv); 
			osc[3] = waveforms.sawtooth(currentPhase * freqMod[3] * 0.125) * restVal * ampEnv;
			osc[3] = lpFilter4.getSample(osc[3], freqRatio * filterEnv, resEnv); 
	    	returnVal[index] = osc[0] + osc[1] + osc[2] + osc[3];
	    	returnVal[index] /= 4.0;
			returnVal[index] = hpFilter.getSample(returnVal[index], 1.0, 1.0);
			currentPhase += (2.0 * Math.PI * currentNoteFreq) / AudioFetcher.sampleRate;
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
				if(noteIndex < sequenceLength) {
					g2.setColor(Color.BLACK);
				} else {
					g2.setColor(Color.GRAY);
				}
				if(notes[noteIndex] == keyIndex) {
					if(tie[noteIndex] && accent[noteIndex]) g2.setColor(new Color(1.0f, 0.0f, 1.0f));
					if(!tie[noteIndex] && accent[noteIndex]) g2.setColor(Color.BLUE);
					if(tie[noteIndex] && !accent[noteIndex]) g2.setColor(Color.RED);
					if(!tie[noteIndex] && !accent[noteIndex]) g2.setColor(Color.WHITE);
				}
				g2.fillRect(screenX + (noteIndex + 1) * noteWidth, lowerY - keyHeight, noteWidth, keyHeight);
				g2.setColor(Color.GREEN);
				g2.drawRect(screenX + (noteIndex + 1) * noteWidth, lowerY - keyHeight, noteWidth, keyHeight);
				if((currentTimeInSamples / noteLengthInSamples) % sequenceLength == noteIndex) {
					g2.setColor(new Color(0.0f, 0.5f, 0.0f, 0.5f));
					g2.fillRect(screenX + (noteIndex + 1) * noteWidth, lowerY - keyHeight, noteWidth, keyHeight);
				}
			}
		}
	}

	public void pointSelected(int x, int y, PlayableController.ClickInfo info) {
		for(int keyIndex = 0; keyIndex < numKeys; keyIndex++) {
			int lowerY = (numKeys - keyIndex) * keyHeight + screenY;
			for(int noteIndex = 0; noteIndex < notes.length; noteIndex++) {
				Rectangle rect = new Rectangle(screenX + (noteIndex + 1) * noteWidth, lowerY - keyHeight, noteWidth, keyHeight);
				if(rect.contains(x, y)) {
					if(info == PlayableController.ClickInfo.SHIFT_DOWN) {
						sequenceLength = noteIndex + 1;
						reset();
						parent.view.repaint();
						return;
					}
					if(info == PlayableController.ClickInfo.CTRL_DOWN) {
						tie[noteIndex] = !tie[noteIndex];
						parent.view.repaint();
						return;
					}
					if(info == PlayableController.ClickInfo.ALT_DOWN) {
						accent[noteIndex] = !accent[noteIndex];
						parent.view.repaint();
						return;
					}
					if(notes[noteIndex] == keyIndex) {
						if(info == PlayableController.ClickInfo.NONE) { 
							notes[noteIndex] = -1;
							parent.view.repaint();
							return;
						}
					} else {
						notes[noteIndex] = keyIndex;
						parent.view.repaint();
						return;
					}
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
			baseFreq = new Double(in.readLine());
			sequenceLength = new Integer(in.readLine());
			notes = new int[maxNotes];
			for(int noteIndex = 0; noteIndex < maxNotes; noteIndex++) {
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
			out.write(new Double(baseFreq).toString());
			out.newLine();
			out.write(new Integer(sequenceLength).toString());
			out.newLine();
			for(int noteIndex = 0; noteIndex < maxNotes; noteIndex++) {
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
