
package main.modules;

import main.ModuleEditor;
import main.TestSignals.TAPair;

public class Sine extends BasicWaveform {

	public Sine(ModuleEditor parent, int x, int y, double freqInHz,
			TAPair durationAndAmplitude) {
		super(parent, x, y, freqInHz, durationAndAmplitude);
		this.name = "Sine";
	}

	@Override
	public double generator(double phase) {
		return Math.sin(phase);
	}
	
	
}
