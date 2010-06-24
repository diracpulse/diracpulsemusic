
public class DFTModel {
		
	public static class FAPair {
		int freq;
		float amplitude;
		
		public FAPair(int freq, float amplitude) {
			this.freq = freq;
			this.amplitude = amplitude;
		}
		
		int getFreq() {
			return freq;
		}
		
		double getRadianFreq() {
			return (Math.PI * 2.0d) / getFreqInHz();
		}
		
		double getFreqInHz() {
			double dBase = (double) DFTEditor.freqsPerOctave;
			double dFreq = (double) freq;
			double freqInHz = Math.pow(2.0, dFreq / dBase);
			return freqInHz;
		}
		
		
		
		float getAmplitude() {
			return amplitude;
		}
		
		double getRealAmplitude() {
			return Math.pow(2.0, amplitude);
		}
		
		public String toString() {
			return new String("DFTModel.FAPair: " + getFreqInHz() + " " + amplitude);
		}
	}
	
	public static class TFA {
		
		int freq;
		int time;
		float amplitude;
		
		public TFA(int time, int freq, float amplitude) {
			this.time = time;
			this.freq = freq;
			this.amplitude = amplitude;
		}
		
		int getTime() {
			return time;
		}
				
		int getTimeInMillis() {
			int timeInMillis = time * DFTEditor.timeStepInMillis;
			return timeInMillis;
		}
		
		double getTimeInSeconds() {
			double timeInMillis = (double) getTimeInMillis();
			return timeInMillis / 1000.0d;
		}
		
		int getFreq() {
			return freq;
		}
		
		double getFreqInHz() {
			double dBase = (double) DFTEditor.freqsPerOctave;
			double dFreq = (double) freq;
			return Math.pow(2.0, dFreq / dBase);
		}
		
		double getRadianFreq() {
			return (Math.PI * 2.0d) / getFreqInHz();
		}
		
		float getAmplitude() {
			return amplitude;
		}
		
		double getRealAmplitude() {
			return Math.pow(2.0, amplitude);
		}
		
		public String toString() {
			return new String("DFTModel.TFA: " + getTimeInMillis() + " " + getFreqInHz() + " " + amplitude);
		}
	}
}
