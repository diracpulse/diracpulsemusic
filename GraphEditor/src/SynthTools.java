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
	static GraphEditor parent;

	static void createPCMData(GraphEditor parent) {
		//PCMData = FastSynth.synthHarmonics(new ArrayList<Harmonic>(parent.harmonicIDToHarmonic.values()));
		if(GraphEditor.harmonicIDToHarmonic == null || GraphEditor.harmonicIDToHarmonic.isEmpty()) return;
		ArrayList<Harmonic> synthHarmonics = new ArrayList<Harmonic>();
		ArrayList<Harmonic> synthHarmonics2 = new ArrayList<Harmonic>();
		for(Harmonic harmonic: GraphEditor.harmonicIDToHarmonic.values()) {
			if(harmonic.getStartTime() > GraphEditor.maxViewTime) continue;
			if(harmonic.getEndTime() < GraphEditor.minViewTime) continue;
			if(harmonic.getMaxNote() < GraphEditor.minViewNote) continue;
			if(harmonic.getMinNote() > GraphEditor.maxViewNote) continue;
			synthHarmonics.add(new Harmonic(harmonic.getHarmonicID()));
			synthHarmonics2.add(harmonic);
			for(FDData data: harmonic.getTrimmedHarmonic(GraphEditor.minViewTime, GraphEditor.maxViewTime)) {
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
