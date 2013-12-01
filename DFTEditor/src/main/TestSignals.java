import java.util.ArrayList;


public class TestSignals {
	
	public static double sampleRate = SynthTools.sampleRate;
	
	public static class TAPair {
		
		public enum TimeFormat {
			FDDATA, SAMPLES, SECONDS, MILLISECONDS
		}
		
		public enum AmplitudeFormat {
			LOG, ABSOLUTE
		}
		
		double timeInSeconds = 0.0;
		double absoluteAmplitude = 0.0;
		
		public TAPair(TimeFormat TF, AmplitudeFormat AF, double time, double amplitude) {
			if(TF == TimeFormat.FDDATA) timeInSeconds = time * (FDData.timeStepInMillis / 1000.0);
			if(TF == TimeFormat.SAMPLES) timeInSeconds = time / sampleRate;
			if(TF == TimeFormat.SECONDS) timeInSeconds = time;
			if(TF == TimeFormat.MILLISECONDS) timeInSeconds = time / 1000.0;
			if(AF == AmplitudeFormat.LOG) absoluteAmplitude = Math.pow(FDData.logBase, amplitude);
			if(AF == AmplitudeFormat.ABSOLUTE) absoluteAmplitude = Math.abs(amplitude);
		}
		
		private double getTime(TimeFormat TF) {
			if(TF == TimeFormat.FDDATA) return timeInSeconds / (FDData.timeStepInMillis / 1000.0);
			if(TF == TimeFormat.SAMPLES) return timeInSeconds * sampleRate;
			if(TF == TimeFormat.MILLISECONDS) return timeInSeconds * 1000.0;
			return timeInSeconds;
		}
		
		private double getAmplitude(AmplitudeFormat AF) {
			if(AF == AmplitudeFormat.LOG) {
				if(absoluteAmplitude == 0.0) return FDData.minLogAmplitude;
				return Math.log(absoluteAmplitude) / Math.log(FDData.logBase);
			}
			return absoluteAmplitude;
		}
		
		public double getLogAmplitude() {
			return(getAmplitude(AmplitudeFormat.LOG));
		}
		
		public double getAbsoluteAmplitude() {
			return absoluteAmplitude;
		}
		
		public int getTimeInFDData() {
			return (int) Math.round(getTime(TimeFormat.FDDATA));
		}

		public int getTimeInSamples() {
			return (int) Math.round(getTime(TimeFormat.SAMPLES));
		}
		public double getTimeInSeconds() {
			return getTime(TimeFormat.SECONDS);
		}
	}		
		
	public static interface Generator {
		
		double[] getSamples();
		double[] addTo(double[] input);
		double[] modulateAM(double[] input);
		double[] modulateFM(double[] input);
		
	}
	
	
	public static class ADSR implements Generator {
		
		double[] samples = null;
		
		public enum Interpolation {
			LINEAR, LOG
		}
		
		
		public ADSR(ArrayList<TAPair> TAPairs, Interpolation interpolation) {
			if(interpolation == Interpolation.LINEAR) samples = Interpolate.synthTAPairsLinear(TAPairs);
			if(interpolation == Interpolation.LOG) samples = Interpolate.synthTAPairsLog(TAPairs);
		}
			
		public double[] getSamples() {
			return samples;
		}
		
		public double[] addTo(double[] input) {
			double[] returnVal = null;
			if(input.length > samples.length) {
				returnVal = new double[input.length];
			} else {
				returnVal = new double[samples.length];
			}
			for(int index = 0; index < returnVal.length; index++) {
				if(index >= samples.length || index >= input.length) {
					returnVal[index] = 0.0;
					continue;
				}
				returnVal[index] = samples[index] + input[index];
			}
			return returnVal;
		}
		
		public double[] modulateAM(double[] input) {
			double[] returnVal = null;
			if(input.length > samples.length) {
				returnVal = new double[input.length];
			} else {
				returnVal = new double[samples.length];
			}
			for(int index = 0; index < returnVal.length; index++) {
				if(index >= samples.length || index >= input.length) {
					returnVal[index] = 0.0;
					continue;
				}
				returnVal[index] = samples[index] * input[index];
			}
			return returnVal;
		}
		
		public double[] modulateFM(double[] input) {
			return null;
		}
		
	}
	
	public static class PureSine implements Generator {
		
		double[] samples = null;
		double amplitude = 0.0;
		double freqInHz = 0.0;
		
		public PureSine(double freqInHz, TAPair durationAndAmplitude) {
			this.freqInHz = freqInHz;
			samples = new double[durationAndAmplitude.getTimeInSamples()];
			double deltaPhase = freqInHz / sampleRate * Math.PI;
			double phase = 0;
			amplitude = durationAndAmplitude.getAbsoluteAmplitude();
			for(int index  = 0; index < samples.length; index++) {
				samples[index] = Math.sin(phase) * amplitude;
				phase += deltaPhase;
			}
		}
		
		public double[] getSamples() {
			return samples;
		}
		
		public double[] addTo(double[] input) {
			double[] returnVal = null;
			if(input.length > samples.length) {
				returnVal = new double[input.length];
			} else {
				returnVal = new double[samples.length];
			}
			for(int index = 0; index < returnVal.length; index++) {
				if(index >= samples.length || index >= input.length) {
					returnVal[index] = 0.0;
					continue;
				}
				returnVal[index] = samples[index] + input[index];
			}
			return returnVal;
		}
		
		public double[] modulateAM(double[] input) {
			double[] returnVal = null;
			if(input.length > samples.length) {
				returnVal = new double[input.length];
			} else {
				returnVal = new double[samples.length];
			}
			for(int index = 0; index < returnVal.length; index++) {
				if(index >= samples.length || index >= input.length) {
					returnVal[index] = 0.0;
					continue;
				}
				returnVal[index] = samples[index] * input[index];
			}
			return returnVal;
		}
		
		public double[] modulateFM(double[] input) {
			double[] returnVal = null;
			if(input.length > samples.length) {
				returnVal = new double[input.length];
			} else {
				returnVal = new double[samples.length];
			}
			double deltaPhase = freqInHz / sampleRate * Math.PI;
			double phase = 0;
			for(int index  = 0; index < returnVal.length; index++) {
				returnVal[index] = Math.sin(phase + input[index] * Math.PI * 2.0) * amplitude;
				phase += deltaPhase;
			}
			return returnVal;
		}
	}
	
	public static class KarplusStrong implements Generator {
		
		double[] samples = new double[44100];
		
		public KarplusStrong(int p) {
			for(int index = 0; index <= p; index++) {
				samples[index] = Math.random() - 0.5;
			}
			for(int index = p + 1; index < samples.length; index++) {
				samples[index] = 0.0;
			}
			for(int index = p + 1; index < samples.length; index++) {
				samples[index] += 0.5 * (samples[index - p] + samples[index - p - 1]);
			}
			for(int index = p; index < samples.length; index++) {
				samples[index] *= 32000.0;
			}
		}
		
		public double[] getSamples() {
			return samples;
		}
		
		public double[] addTo(double[] input) {
			double[] returnVal = null;
			if(input.length > samples.length) {
				returnVal = new double[input.length];
			} else {
				returnVal = new double[samples.length];
			}
			for(int index = 0; index < returnVal.length; index++) {
				if(index >= samples.length || index >= input.length) {
					returnVal[index] = 0.0;
					continue;
				}
				returnVal[index] = samples[index] + input[index];
			}
			return returnVal;
		}
		
		public double[] modulateAM(double[] input) {
			double[] returnVal = null;
			if(input.length > samples.length) {
				returnVal = new double[input.length];
			} else {
				returnVal = new double[samples.length];
			}
			for(int index = 0; index < returnVal.length; index++) {
				if(index >= samples.length || index >= input.length) {
					returnVal[index] = 0.0;
					continue;
				}
				returnVal[index] = samples[index] * input[index];
			}
			return returnVal;
		}
		
		public double[] modulateFM(double[] input) {
			return null;
		}
	}
	
	public static ADSR getEnvelope() {
		ArrayList<TAPair> values = new ArrayList<TAPair>();
		values.add(new TAPair(TAPair.TimeFormat.SECONDS, TAPair.AmplitudeFormat.LOG, 0.0, -2.0));
		values.add(new TAPair(TAPair.TimeFormat.MILLISECONDS, TAPair.AmplitudeFormat.LOG, 20.0, 15.0));
		values.add(new TAPair(TAPair.TimeFormat.MILLISECONDS, TAPair.AmplitudeFormat.LOG, 40.0, 14.0));
		values.add(new TAPair(TAPair.TimeFormat.SECONDS, TAPair.AmplitudeFormat.LOG, 3.5, 14.0));
		values.add(new TAPair(TAPair.TimeFormat.SECONDS, TAPair.AmplitudeFormat.LOG, 4.0, -2.0));
		return new ADSR(values, ADSR.Interpolation.LOG);
		
	}
	
	public static double[] getTestSignal() {
		TAPair sin0Pair =  new TAPair(TAPair.TimeFormat.SECONDS, TAPair.AmplitudeFormat.ABSOLUTE, 4.0, 1.0);
		TAPair sin1Pair =  new TAPair(TAPair.TimeFormat.SECONDS, TAPair.AmplitudeFormat.ABSOLUTE, 4.0, 1.0);
		PureSine sin0 = new PureSine(2000.0, sin0Pair);
		PureSine sin1 = new PureSine(10, sin1Pair);
		double[] returnVal = sin0.modulateFM(sin1.getSamples());
		return getEnvelope().modulateAM(returnVal);
	}
	
	public static double[] getTestSignal1() {
		KarplusStrong ks = new KarplusStrong(441);
		KarplusStrong ks2 = new KarplusStrong(441 * 2);
		return ks.addTo(ks2.getSamples());
	}
	
}
