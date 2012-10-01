import java.sql.Time;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TreeMap;

import javax.swing.JOptionPane;

class SynthTools {
	
	static double sampleRate = 44100.0;
	static double twoPI = 2.0 * Math.PI;
	static double timeToSample = sampleRate * (FDData.timeStepInMillis * 1.0 / 1000.0);
	static double[] PCMDataMono = null;
	static double[] PCMDataLeft = null;
	static double[] PCMDataRight = null;
	static HarmonicsEditor parent;

	static void createPCMData(HarmonicsEditor parent) {
		if(HarmonicsEditor.harmonicIDToHarmonicMono == null || HarmonicsEditor.harmonicIDToHarmonicMono.isEmpty()) return;
		PCMDataMono = FastSynth.synthHarmonicsLinear(new ArrayList<Harmonic>(HarmonicsEditor.harmonicIDToHarmonicMono.values()));
		PCMDataLeft = FastSynth.synthHarmonicsLinear(new ArrayList<Harmonic>(HarmonicsEditor.harmonicIDToHarmonicLeft.values()));
		PCMDataRight = FastSynth.synthHarmonicsLinear(new ArrayList<Harmonic>(HarmonicsEditor.harmonicIDToHarmonicRight.values()));
	}
	
	static void createPCMDataNoise(HarmonicsEditor parent) {
		if(HarmonicsEditor.harmonicIDToHarmonicMono == null || HarmonicsEditor.harmonicIDToHarmonicMono.isEmpty()) return;
		FastSynth.initSharedPCMData(new ArrayList<Harmonic>(HarmonicsEditor.harmonicIDToHarmonicMono.values()));
		for(Harmonic harmonic: HarmonicsEditor.harmonicIDToHarmonicMono.values()) {
			int duration = harmonic.getLength();
			int note = harmonic.getAverageNote();
			if(note < 8 * FDData.noteBase) {
				//FastSynth.synthHarmonicLinear(harmonic);
				//continue;
			}
			int startTime = harmonic.getStartSampleOffset();
			double amplitude = Math.pow(2.0, harmonic.getMaxLogAmplitude());
			double[] audio = Filter.getFilteredNoise(duration, note, amplitude);
			for(int time = startTime; time < startTime + duration; time++) {
				FastSynth.sharedPCMData[time] += audio[time - startTime];
			}
			System.out.println(note + startTime + "complete");
		}
		PCMDataMono = FastSynth.sharedPCMData;
	}
	
	static void createPCMDataFiltered(HarmonicsEditor parent) {
		if(HarmonicsEditor.harmonicIDToHarmonicMono == null || HarmonicsEditor.harmonicIDToHarmonicMono.isEmpty()) return;
		FastSynth.initSharedPCMData(new ArrayList<Harmonic>(HarmonicsEditor.harmonicIDToHarmonicMono.values()));
		for(Harmonic harmonic: HarmonicsEditor.harmonicIDToHarmonicMono.values()) {
			int duration = harmonic.getLength();
			int note = harmonic.getAverageNote();
			if(note < 8 * FDData.noteBase) {
				//FastSynth.synthHarmonicLinear(harmonic);
				//continue;
			}
			int startTime = harmonic.getStartSampleOffset();
			double amplitude = Math.pow(2.0, harmonic.getMaxLogAmplitude());
			double[] audio = Filter.getFilteredSawtooth(duration, note, amplitude);
			for(int time = startTime; time < startTime + duration; time++) {
				FastSynth.sharedPCMData[time] += audio[time - startTime];
			}
			System.out.println(note + startTime + "complete");
		}
		PCMDataMono = FastSynth.sharedPCMData;
	}

	public static void playWindow() {
		AudioPlayer ap = null;
		if(PCMDataMono == null) return;
		if(HarmonicsEditor.currentChannel == HarmonicsEditor.Channel.STEREO) {
			ap = new AudioPlayer(parent, PCMDataLeft, PCMDataRight, 1.0);
		}
		ap.start();
	}


}
