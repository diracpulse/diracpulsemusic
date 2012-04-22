import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TreeMap;

import javax.swing.JOptionPane;

class SynthTools {
	
	static double sampleRate = 44100.0;
	static double twoPI = 2.0 * Math.PI;
	static double timeToSample = sampleRate * (FDData.timeStepInMillis * 1.0 / 1000.0);
	static double[] PCMData = null;
	static int slowSpeed = 2;
	static GraphEditor parent;

	static void createPCMWindowData(GraphEditor parent) {
		//PCMData = FastSynth.synthHarmonics(new ArrayList<Harmonic>(parent.harmonicIDToHarmonic.values()));
		if(GraphEditor.harmonicIDToHarmonic == null || GraphEditor.harmonicIDToHarmonic.isEmpty()) return;
		ArrayList<Harmonic> synthHarmonics = new ArrayList<Harmonic>();
		for(Harmonic harmonic: GraphEditor.harmonicIDToHarmonic.values()) {
			if(harmonic.getStartTime() > GraphEditor.maxViewTime) continue;
			if(harmonic.getEndTime() < GraphEditor.minViewTime) continue;
			if(harmonic.getMaxNote() < GraphEditor.minViewNote) continue;
			if(harmonic.getMinNote() > GraphEditor.maxViewNote) continue;
			synthHarmonics.add(new Harmonic(harmonic.getHarmonicID()));
			for(FDData data: harmonic.getTrimmedHarmonic(GraphEditor.minViewTime, GraphEditor.maxViewTime, slowSpeed)) {
				synthHarmonics.get(synthHarmonics.size() - 1).addData(data);
			}
		}
		PCMData = GraphFileOutput.SynthFDDataExternally(synthHarmonics);
	}
	
	static void createPCMSequencerData(GraphEditor parent) {
		//PCMData = FastSynth.synthHarmonics(new ArrayList<Harmonic>(parent.harmonicIDToHarmonic.values()));
		if(GraphEditor.selectedHarmonicIDs == null || GraphEditor.selectedHarmonicIDs.isEmpty()) return;
		ArrayList<Harmonic> synthHarmonics = new ArrayList<Harmonic>();
		for(long harmonicID: GraphEditor.selectedHarmonicIDs) {
			Harmonic harmonic = GraphEditor.harmonicIDToHarmonic.get(harmonicID);
			synthHarmonics.add(new Harmonic(harmonic.getHarmonicID()));
			for(FDData data: harmonic.getPureSineHarmonic(1.0)) {
				synthHarmonics.get(synthHarmonics.size() - 1).addData(data);
			}
		}
		PCMData = GraphFileOutput.SynthFDDataExternally(synthHarmonics);
	}
	
	static void createPCMControlPointData(GraphEditor parent) {
		//PCMData = FastSynth.synthHarmonics(new ArrayList<Harmonic>(parent.harmonicIDToHarmonic.values()));
		if(GraphEditor.harmonicIDToControlPointHarmonic.isEmpty()) return;
		ArrayList<Harmonic> synthHarmonics = new ArrayList<Harmonic>();
		for(long harmonicID: GraphEditor.harmonicIDToControlPointHarmonic.keySet()) {
			Harmonic harmonic = GraphEditor.harmonicIDToControlPointHarmonic.get(harmonicID);
			synthHarmonics.add(new Harmonic(harmonic.getHarmonicID()));
			for(FDData data: harmonic.getPureSineHarmonic(1.0)) {
				synthHarmonics.get(synthHarmonics.size() - 1).addData(data);
			}
		}
		PCMData = GraphFileOutput.SynthFDDataExternally(synthHarmonics);
	}

	public static void playWindow() {
		if(PCMData == null) return;
		AudioPlayer ap = new AudioPlayer(PCMData, 1.0);
		ap.start();
	}

}
