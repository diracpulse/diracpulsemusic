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
	static double playSpeed = 1.0;
	static TrackEditor parent;
	
	static void createPCMLoopDataLinear() {
		PCMData = FastSynth.synthHarmonicsLinear(new ArrayList<Harmonic>(TrackEditor.loopHarmonicIDToHarmonic.values()));
	}
	
	static void createPCMTrackDataLinear() {
		PCMData = FastSynth.synthHarmonicsLinear(new ArrayList<Harmonic>(TrackEditor.trackHarmonicIDToHarmonic.values()));
	}	
	
	static void createPCMLoopDataLinearCubicSpline() {
		PCMData = FastSynth.synthHarmonicsLinearCubicSpline(new ArrayList<Harmonic>(TrackEditor.loopHarmonicIDToHarmonic.values()));
	}
	
	
	static void createPCMTrackDataLinearCubicSpline() {
		PCMData = FastSynth.synthHarmonicsLinearCubicSpline(new ArrayList<Harmonic>(TrackEditor.trackHarmonicIDToHarmonic.values()));
	}
	
	public static void playWindow() {
		if(PCMData == null) return;
		AudioPlayer ap = new AudioPlayer(PCMData, 1.0);
		ap.start();
	}

}
