
public class DFTModel {
	
	public static class TFA {
		
		int maxFreq = 31 * 18;
		int freq;
		int time;
		float amplitude;
		
		public TFA(int time, int freq, float amplitude) {
			this.time = time;
			this.freq = freq;
			this.amplitude = amplitude;
			if(freq > maxFreq) {
				System.out.println("TFA: Freq out of bounds");
			}
		}
		
		int getTime() {
			return time;
		}
		int getTimeInMillis() {
			int timeInMillis = time * DFTEditor.timeStepInMillis;
			return timeInMillis;
		}
		
		int getFreq() {
			return freq;
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
		
		public String toString() {
			return new String("DFTModel.TFA: " + getTimeInMillis() + " " + getFreqInHz() + " " + amplitude);
		}
	}
}
