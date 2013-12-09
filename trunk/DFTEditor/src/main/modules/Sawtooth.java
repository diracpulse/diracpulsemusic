
package main.modules;

import main.ModuleEditor;
import main.TestSignals.TAPair;

public class Sawtooth extends BasicWaveform {

	public Sawtooth(ModuleEditor parent, int x, int y, double freqInHz,
			TAPair durationAndAmplitude) {
		super(parent, x, y, freqInHz, durationAndAmplitude);
		this.name = "Sawtooth";
	}

	@Override
	public double generator(double phase) {
		phase -= Math.floor(phase / (Math.PI * 2.0)) * Math.PI * 2.0;
		if(phase < Math.PI) return phase / Math.PI;
		if(phase > Math.PI) return -1.0 + (phase - Math.PI) / Math.PI;
		return Math.random() * 2.0 - 1.0; // phase == Math.PI
	}
	

}
