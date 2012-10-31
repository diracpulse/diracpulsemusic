import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TreeMap;

import javax.swing.JOptionPane;

class GraphSynthTools {
	
	static double sampleRate = 44100.0;
	static double twoPI = 2.0 * Math.PI;
	static double timeToSample = sampleRate * (FDData.timeStepInMillis * 1.0 / 1000.0);
	static float[] PCMDataMono = null;
	static float[] PCMDataLeft = null;
	static float[] PCMDataRight = null;
	static double playSpeed = 1.0;
	static GraphEditor parent;

	static void createPCMDataLinear() {
		//PCMData = FastSynth.synthHarmonics(new ArrayList<Harmonic>(parent.harmonicIDToHarmonic.values()));
		if(GraphEditor.harmonicIDToHarmonic == null || GraphEditor.harmonicIDToHarmonic.isEmpty()) return;
		ArrayList<Harmonic> synthHarmonics = new ArrayList<Harmonic>();
		for(Harmonic harmonic: GraphEditor.harmonicIDToHarmonic.values()) {
			if(!harmonic.isSynthesized()) continue;
			if(harmonic.getStartTime() > GraphEditor.maxViewTime) continue;
			if(harmonic.getEndTime() < GraphEditor.minViewTime) continue;
			if(harmonic.getMaxNote() < GraphEditor.minViewNote) continue;
			if(harmonic.getMinNote() > GraphEditor.maxViewNote) continue;
			if(harmonic.getMaxLogAmplitude() < GraphEditor.minViewLogAmplitude) continue;
			synthHarmonics.add(new Harmonic(harmonic.getHarmonicID()));
			for(FDData data: harmonic.getTrimmedHarmonic(GraphEditor.minViewTime, GraphEditor.maxViewTime, playSpeed)) {
				synthHarmonics.get(synthHarmonics.size() - 1).addData(data);
			}
		}
		PCMDataMono = FastSynth.synthHarmonicsLinear((byte) 0, synthHarmonics);
		PCMDataLeft = FastSynth.synthHarmonicsLinear((byte) 1, synthHarmonics);
		PCMDataRight = FastSynth.synthHarmonicsLinear((byte) 2, synthHarmonics);
	}
	
	static void createPCMDataLinearCubicSpline() {
		//PCMData = FastSynth.synthHarmonics(new ArrayList<Harmonic>(parent.harmonicIDToHarmonic.values()));
		if(GraphEditor.harmonicIDToHarmonic == null || GraphEditor.harmonicIDToHarmonic.isEmpty()) return;
		ArrayList<Harmonic> synthHarmonics = new ArrayList<Harmonic>();
		for(Harmonic harmonic: GraphEditor.harmonicIDToHarmonic.values()) {
			if(!harmonic.isSynthesized()) continue;
			if(harmonic.getStartTime() > GraphEditor.maxViewTime) continue;
			if(harmonic.getEndTime() < GraphEditor.minViewTime) continue;
			if(harmonic.getMaxNote() < GraphEditor.minViewNote) continue;
			if(harmonic.getMinNote() > GraphEditor.maxViewNote) continue;
			if(harmonic.getMaxLogAmplitude() < GraphEditor.minViewLogAmplitude) continue;
			synthHarmonics.add(new Harmonic(harmonic.getHarmonicID()));
			for(FDData data: harmonic.getTrimmedHarmonic(GraphEditor.minViewTime, GraphEditor.maxViewTime, playSpeed)) {
				synthHarmonics.get(synthHarmonics.size() - 1).addData(data);
			}
		}
		PCMDataMono = FastSynth.synthHarmonicsLinearCubicSpline((byte) 0, synthHarmonics);
		PCMDataLeft = FastSynth.synthHarmonicsLinearCubicSpline((byte) 1, synthHarmonics);
		PCMDataRight = FastSynth.synthHarmonicsLinearCubicSpline((byte) 2, synthHarmonics);
	}
	
	static void createPCMDataLinearNoise() {
		//PCMData = FastSynth.synthHarmonics(new ArrayList<Harmonic>(parent.harmonicIDToHarmonic.values()));
		if(GraphEditor.harmonicIDToHarmonic == null || GraphEditor.harmonicIDToHarmonic.isEmpty()) return;
		ArrayList<Harmonic> synthHarmonics = new ArrayList<Harmonic>();
		for(Harmonic harmonic: GraphEditor.harmonicIDToHarmonic.values()) {
			if(!harmonic.isSynthesized()) continue;
			if(harmonic.getStartTime() > GraphEditor.maxViewTime) continue;
			if(harmonic.getEndTime() < GraphEditor.minViewTime) continue;
			if(harmonic.getMaxNote() < GraphEditor.minViewNote) continue;
			if(harmonic.getMinNote() > GraphEditor.maxViewNote) continue;
			if(harmonic.getMaxLogAmplitude() < GraphEditor.minViewLogAmplitude) continue;
			synthHarmonics.add(new Harmonic(harmonic.getHarmonicID()));
			for(FDData data: harmonic.getTrimmedHarmonic(GraphEditor.minViewTime, GraphEditor.maxViewTime, playSpeed)) {
				synthHarmonics.get(synthHarmonics.size() - 1).addData(data);
			}
		}
		PCMDataMono = FastSynth.synthHarmonicsLinearNoise((byte) 0, synthHarmonics);
		PCMDataLeft = FastSynth.synthHarmonicsLinearNoise((byte) 1, synthHarmonics);
		PCMDataRight = FastSynth.synthHarmonicsLinearNoise((byte) 2, synthHarmonics);
	}
	
	static void createPCMDataLinearNoiseSelected() {
		//PCMData = FastSynth.synthHarmonics(new ArrayList<Harmonic>(parent.harmonicIDToHarmonic.values()));
		if(DFTEditor.getSelectedHarmonicIDs() == null || DFTEditor.getSelectedHarmonicIDs().isEmpty()) return;
		ArrayList<Harmonic> synthHarmonics = new ArrayList<Harmonic>();
		for(long harmonicID: DFTEditor.getSelectedHarmonicIDs()) {
			Harmonic harmonic = GraphEditor.harmonicIDToHarmonic.get(harmonicID);
			if(!harmonic.isSynthesized()) continue;
			synthHarmonics.add(new Harmonic(harmonic.getHarmonicID()));
			for(FDData data: harmonic.getPureSineHarmonic(1.0, playSpeed)) {
				synthHarmonics.get(synthHarmonics.size() - 1).addData(data);
			}
		}
		PCMDataMono = FastSynth.synthHarmonicsLinearNoise((byte) 0, synthHarmonics);
		PCMDataLeft = FastSynth.synthHarmonicsLinearNoise((byte) 1, synthHarmonics);
		PCMDataRight = FastSynth.synthHarmonicsLinearNoise((byte) 2, synthHarmonics);
	}

	public static void playPCMData() {
		AudioPlayer ap = new AudioPlayer(PCMDataLeft, PCMDataRight, 1.0);
		ap.start();
	}

}
