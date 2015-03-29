package main.playable;

public class ArduinoLoop {
	
	int maxInt = 65535;
	float dMaxReading = 1024.0f;
	float samplesPerSecond = 44100.0f;
	
	class SquareWave {
	
		float dPulseWidth = 0.5f;
		float dPhase = 0.0f;
		float dDeltaPhase = 0.0f;
		int pulseWidth = maxInt / 2;
		int phase = 0;
		int deltaPhase = 1;
		
		public void begin() {
			dPhase = 0.0f;
			phase = 0;
		}
		
		public void setPulseWidth(float pulseWidthIn) {
			setPulseWidth(Math.round(pulseWidthIn * dMaxReading));
		}
		
		public void setPulseWidth(int pulseWidthIn) {
			dPulseWidth = pulseWidthIn / dMaxReading;
			pulseWidth = Math.round(dPulseWidth * maxInt);
		}
		
		public void setFrequency(float freq) {
			deltaPhase = Math.round(freq / samplesPerSecond * maxInt);
		}
		
		public float dGetNextSample() {
			if(dPhase >= maxInt) dPhase -= maxInt; // REMOVE FOR ARDUINO CODE
			if(dPhase < dPulseWidth) {
				dPhase += dDeltaPhase;
				return 1.0f;
			}
			dPhase += dDeltaPhase;
			return 0.0f;
		}
		
		public int getNextSample() {
			if(phase >= maxInt) phase -= maxInt; // REMOVE FOR ARDUINO CODE
			if(phase < pulseWidth) {
				phase += deltaPhase;
				return 1;
			}
			phase += deltaPhase;
			return 0;
		}
		
	}

}
