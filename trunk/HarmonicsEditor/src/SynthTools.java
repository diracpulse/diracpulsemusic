import java.sql.Time;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TreeMap;

import javax.swing.JOptionPane;

class SynthTools {
	
	static double sampleRate = 44100.0;
	static double twoPI = 2.0 * Math.PI;
	static double timeToSample = sampleRate * (FDData.timeStepInMillis * 1.0 / 1000.0);
	static double[] PCMData = null;
	static HarmonicsEditor parent;

	static void createPCMData(HarmonicsEditor parent) {
		if(HarmonicsEditor.harmonicIDToHarmonic == null || HarmonicsEditor.harmonicIDToHarmonic.isEmpty()) return;
		PCMData = FastSynth.synthHarmonicsLinear(new ArrayList<Harmonic>(HarmonicsEditor.harmonicIDToHarmonic.values()));
	}
	
	static void createPCMDataNoise(HarmonicsEditor parent) {
		if(HarmonicsEditor.harmonicIDToHarmonic == null || HarmonicsEditor.harmonicIDToHarmonic.isEmpty()) return;
		FastSynth.initSharedPCMData(new ArrayList<Harmonic>(HarmonicsEditor.harmonicIDToHarmonic.values()));
		for(Harmonic harmonic: HarmonicsEditor.harmonicIDToHarmonic.values()) {
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
		PCMData = FastSynth.sharedPCMData;
	}
	
	static void createPCMDataFiltered(HarmonicsEditor parent) {
		if(HarmonicsEditor.harmonicIDToHarmonic == null || HarmonicsEditor.harmonicIDToHarmonic.isEmpty()) return;
		FastSynth.initSharedPCMData(new ArrayList<Harmonic>(HarmonicsEditor.harmonicIDToHarmonic.values()));
		for(Harmonic harmonic: HarmonicsEditor.harmonicIDToHarmonic.values()) {
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
		PCMData = FastSynth.sharedPCMData;
	}

	public static void playWindow() {
		if(PCMData == null) return;
		AudioPlayer ap = new AudioPlayer(parent, PCMData, 1.0);
		ap.start();
	}


}
