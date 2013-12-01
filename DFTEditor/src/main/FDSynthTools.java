package main;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TreeMap;

import javax.swing.JOptionPane;

class FDSynthTools {
	
	static double sampleRate = 44100.0;
	static double twoPI = 2.0 * Math.PI;
	static double timeToSample = sampleRate * (FDData.timeStepInMillis * 1.0 / 1000.0);
	static double[] PCMDataMono = null;
	static double[] PCMDataLeft = null;
	static double[] PCMDataRight = null;
	static double playSpeed = 1.0;
	static FDEditor parent;
	
	static void createPCMDataLinear() {
		//PCMData = FastSynth.synthHarmonics(new ArrayList<Harmonic>(parent.harmonicIDToHarmonic.values()));
		if(DFTEditor.getSelectedHarmonicIDs() == null || DFTEditor.getSelectedHarmonicIDs().isEmpty()) return;
		ArrayList<Harmonic> synthHarmonics = new ArrayList<Harmonic>();
		for(long harmonicID: DFTEditor.getSelectedHarmonicIDs()) {
			Harmonic harmonic = DFTEditor.harmonicIDToHarmonic.get(harmonicID);
			if(harmonic == null) continue;
			if(!harmonic.isSynthesized()) continue;
			synthHarmonics.add(harmonic);
		}
		PCMDataLeft = FastSynth.synthHarmonicsLinear(FDData.Channel.LEFT, synthHarmonics);
		PCMDataRight = FastSynth.synthHarmonicsLinear(FDData.Channel.RIGHT, synthHarmonics);
	}
	
	static void createPCMDataLinearCubicSpline() {
		//PCMData = FastSynth.synthHarmonics(new ArrayList<Harmonic>(parent.harmonicIDToHarmonic.values()));
		if(DFTEditor.getSelectedHarmonicIDs() == null || DFTEditor.getSelectedHarmonicIDs().isEmpty()) return;
		ArrayList<Harmonic> synthHarmonics = new ArrayList<Harmonic>();
		for(long harmonicID: DFTEditor.getSelectedHarmonicIDs()) {
			Harmonic harmonic = DFTEditor.harmonicIDToHarmonic.get(harmonicID);
			if(harmonic == null) continue;
			if(!harmonic.isSynthesized()) continue;
			synthHarmonics.add(harmonic);
		}
		PCMDataLeft = FastSynth.synthHarmonicsLinearCubicSpline(FDData.Channel.LEFT, synthHarmonics);
		PCMDataRight = FastSynth.synthHarmonicsLinearCubicSpline(FDData.Channel.RIGHT, synthHarmonics);
	}
	
	static void createPCMDataLinearNoise() {
		//PCMData = FastSynth.synthHarmonics(new ArrayList<Harmonic>(parent.harmonicIDToHarmonic.values()));
		if(DFTEditor.getSelectedHarmonicIDs() == null || DFTEditor.getSelectedHarmonicIDs().isEmpty()) return;
		ArrayList<Harmonic> synthHarmonics = new ArrayList<Harmonic>();
		for(long harmonicID: DFTEditor.getSelectedHarmonicIDs()) {
			Harmonic harmonic = DFTEditor.harmonicIDToHarmonic.get(harmonicID);
			if(harmonic == null) continue;
			if(!harmonic.isSynthesized()) continue;
			synthHarmonics.add(harmonic);
		}
		PCMDataLeft = FastSynth.synthHarmonicsLinearNoise(FDData.Channel.LEFT, synthHarmonics);
		PCMDataRight = FastSynth.synthHarmonicsLinearNoise(FDData.Channel.RIGHT, synthHarmonics);
	}
	
	static void createPCMDataPureSine() {
		//PCMData = FastSynth.synthHarmonics(new ArrayList<Harmonic>(parent.harmonicIDToHarmonic.values()));
		if(DFTEditor.getSelectedHarmonicIDs() == null || DFTEditor.getSelectedHarmonicIDs().isEmpty()) return;
		ArrayList<Harmonic> synthHarmonics = new ArrayList<Harmonic>();
		for(long harmonicID: DFTEditor.getSelectedHarmonicIDs()) {
			Harmonic harmonic = DFTEditor.harmonicIDToHarmonic.get(harmonicID);
			if(harmonic == null) continue;
			if(!harmonic.isSynthesized()) continue;
			synthHarmonics.add(new Harmonic(harmonic.getHarmonicID()));
			for(FDData data: harmonic.getPureSineHarmonic(1.0, playSpeed)) {
				synthHarmonics.get(synthHarmonics.size() - 1).addData(data);
			}
		}
		PCMDataLeft = FastSynth.synthHarmonicsLinear(FDData.Channel.LEFT, synthHarmonics);
		PCMDataRight = FastSynth.synthHarmonicsLinear(FDData.Channel.RIGHT, synthHarmonics);
	}
	

	public static void playPCMData() {
		AudioPlayer ap = new AudioPlayer(PCMDataLeft, PCMDataRight, 1.0);
		ap.start();
	}

}
