
package main.modules;

import main.ModuleEditor;
import main.TestSignals.TAPair;

public class TriangleWave extends BasicWaveform {

	public TriangleWave(ModuleEditor parent, int x, int y, double freqInHz,
			TAPair durationAndAmplitude) {
		super(parent, x, y, freqInHz, durationAndAmplitude);
		this.name = "Triangle Wave";
	}

	@Override
	public double generator(double phase) {
		phase -= Math.floor(phase / (Math.PI * 2.0)) * Math.PI * 2.0;
		if(phase < Math.PI / 2.0) return phase / (Math.PI / 2.0);
		if(phase < Math.PI * 1.5) return 1.0 - (phase - Math.PI / 2.0) / (Math.PI / 2.0);
		return -1.0 + (phase - Math.PI * 1.5) / (Math.PI / 2.0);
	}

}
