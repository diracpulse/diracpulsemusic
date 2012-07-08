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
	
	static void createPCMData(HarmonicsEditor parent, int startTime, int endTime) {
		int startSampleOffset = (int) Math.round(startTime * timeToSample);
		int endSampleOffset = (int) Math.round(endTime * timeToSample);
		int dataLength = endSampleOffset - startSampleOffset;
		for(Harmonic harmonic: HarmonicsEditor.harmonicIDToHarmonic.values()) {
			if(harmonic.getStartSampleOffset() >= endSampleOffset) continue;
			if(harmonic.getEndSampleOffset() <= startSampleOffset) continue;
			if(harmonic.getStartSampleOffset() < startSampleOffset) {
				startSampleOffset = harmonic.getStartSampleOffset();
			}
			if(harmonic.getEndSampleOffset() > endSampleOffset) {
				endSampleOffset = harmonic.getEndSampleOffset();
			}
			if(harmonic.getStartSampleOffset() < endSampleOffset) continue;
		}
		PCMData = new double[endSampleOffset + 1];
		System.out.println("End Sample Offset " + endSampleOffset);
		for(int i= 0; i <= endSampleOffset; i++) PCMData[i] = 0.0;
		for(Harmonic harmonic: HarmonicsEditor.harmonicIDToHarmonic.values()) {
			Double[] HarmonicPCMData = harmonic.getPCMData();
			int startSample = harmonic.getStartSampleOffset();
			int endSample = startSample + HarmonicPCMData.length - 1;
			//System.out.println("SynthTools.playFileData startSample = " + startSample);
			//System.out.println("SynthTools.playFileData endSample = " + endSample);
			//System.out.println("SynthTools.playFileData HarmonicPCMData.length = " + HarmonicPCMData.length);
			for(int currentSample = startSample; currentSample < endSample - 1; currentSample++) {
				if(currentSample >= PCMData.length) break;
				PCMData[currentSample] += HarmonicPCMData[currentSample - startSample];
				if(currentSample % 441 == 0) {
					//System.out.println(currentSample + ":" + PCMData[currentSample]);
				}
			}
		}
		for(int i= 0; i <= endSampleOffset; i += 100) {
			//System.out.println(i + "!" + PCMData[i]);
		}
		//JOptionPane.showMessageDialog(parent, "Ready To Play");
	}
	
	public static void playWindow() {
		if(PCMData == null) return;
		AudioPlayer ap = new AudioPlayer(parent, PCMData, 1.0);
		ap.start();
	}


}
