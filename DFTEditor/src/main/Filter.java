
package main;

import java.util.ArrayList;
import java.util.TreeMap;

public class Filter {
	
	public enum FilterType {
		LOWPASS,
		HIGHPASS,
		BANDPASS;
	}
	
	public static class CriticalBand {
		
		double lowerBound;
		double upperBound;
		double lpFilter[] = null;
		double hpFilter[] = null;

		CriticalBand(double lowerBound, double upperBound) {
			this.upperBound = upperBound;
			this.lowerBound = lowerBound;
		}
	
		double getLowerBound() {
			return lowerBound;
		}
		
		double getUpperBound() {
			return upperBound;
		}
		
		double getCenterFreq() {
			//return Math.sqrt(upperBound * lowerBound);
			return (upperBound + lowerBound) / 2.0;
		}

	}
	
	static double[] filter = null;	
	final static double twoPI = 6.283185307179586476925286766559;
	final static double onePI = 3.1415926535897932384626433832795;
	final static double halfPI = 1.5707963267948966192313216916398;
	final static double samplingRate = 44100.0;
	final static double maxBinStep = 1.0;
	final static double optimalLPRejectRatio = 1.38;
	public static TreeMap<Float, Integer> passFreqToFilterLength = null;
	public static ArrayList<CriticalBand> criticalBands = null;
	public static ArrayList<CriticalBand> noiseCriticalBands = null;
	public static double criticalBandBarkStep = 0.5;
	public static double noiseCriticalBandBarkStep = 1.0;

	final static double alpha = 5.0;
	
	public static double BesselI0(double x) {
		   double denominator;
		   double numerator;
		   double z;

		   if (x == 0.0) {
		      return 1.0;
		   } else {
		      z = x * x;
		      numerator = (z* (z* (z* (z* (z* (z* (z* (z* (z* (z* (z* (z* (z* 
		                     (z* 0.210580722890567e-22  + 0.380715242345326e-19 ) +
		                         0.479440257548300e-16) + 0.435125971262668e-13 ) +
		                         0.300931127112960e-10) + 0.160224679395361e-7  ) +
		                         0.654858370096785e-5)  + 0.202591084143397e-2  ) +
		                         0.463076284721000e0)   + 0.754337328948189e2   ) +
		                         0.830792541809429e4)   + 0.571661130563785e6   ) +
		                         0.216415572361227e8)   + 0.356644482244025e9   ) +
		                         0.144048298227235e10);

		      denominator = (z*(z*(z-0.307646912682801e4)+
		                       0.347626332405882e7)-0.144048298227235e10);
		   }

		   return -numerator/denominator;
		}

		static void LPFilter(double freq, int order, double alpha) {
			double w = 2.0 * (freq / samplingRate);
			double w0 = 0.0;
			double w1 = w * onePI;
			CreateFilter(w0, w1, order, alpha);
		}
		
		public static double[] getLPFilter(double freq, int order, double alpha) {
			filter = new double[order + 1];
			LPFilter(freq, order, alpha);
			double[] returnVal = new double[filter.length];
			for(int index = 0; index < filter.length; index++) {
				returnVal[index] = filter[index];
			}
			return returnVal;
		}
		

		static void HPFilter(double freq, int order, double alpha) {
			double w = 2.0 * (freq / samplingRate);
			double w0 = onePI;
			double w1 = (1.0 - w) * onePI;
			CreateFilter(w0, w1, order, alpha);
		}
		
		public static double[] getHPFilter(double freq, int order, double alpha) {
			filter = new double[order + 1];
			HPFilter(freq, order, alpha);
			double[] returnVal = new double[filter.length];
			for(int index = 0; index < filter.length; index++) {
				returnVal[index] = filter[index];
			}
			return returnVal;
		}

		static void BPFilter(double freq, int order, double alpha) {
			double w = 2.0 * (freq / samplingRate);
			double w0 = w * onePI;
			double w1 = w / -4.0;
			CreateFilter(w0, w1, order, alpha);
		}
		
		public static double[] getBPFilter(double freq, int order, double alpha) {
			filter = new double[order + 1];
			BPFilter(freq, order, alpha);
			double[] returnVal = new double[filter.length];
			for(int index = 0; index < filter.length; index++) {
				returnVal[index] = filter[index];
			}
			return returnVal;
		}

		public static void CreateFilter(double w0, double w1, int order, double alpha) {
			//filter = new double[order];
			int m = order / 2;
			int n;
			double dn;
			double dm = (double) m;
			double I0alpha = BesselI0(alpha);
			
			filter[0] = w1 / onePI;
				
			for (n=1; n <= m; n++) {
				dn = (double) n;
				filter[n] = Math.sin(dn*w1)*Math.cos(dn*w0)/(dn*onePI);
				filter[n] *= BesselI0(alpha * Math.sqrt(1.0 - (dn/dm) * (dn/dm))) / I0alpha;
			}
			
				
			// shift impulse response to make filter causal:
			for (n=m+1; n<=order; n++) filter[n] = filter[n - m];
			for (n=0; n<=m-1; n++) filter[n] = filter[order - n];
			filter[m] = w1 / onePI;
			return;
		}

		public static void CreateWindow(double[] window, int size, double alpha) {
			   double sumvalue = 0.0;
			   int i;
			   
			   for (i=0; i<size/2; i++) {
			      sumvalue += BesselI0(onePI * alpha * Math.sqrt(1.0 - Math.pow(4.0*i/size - 1.0, 2)));
			      window[i] = sumvalue;
			   }

			   // need to add one more value to the nomalization factor at size/2:
			   sumvalue += BesselI0(onePI * alpha * Math.sqrt(1.0 - Math.pow(4.0*(size/2)/size-1.0, 2)));

			   // normalize the window and fill in the righthand side of the window:
			   for (i=0; i<size/2; i++) {
			      window[i] = Math.sqrt(window[i]/sumvalue);
			      window[size-1-i] = window[i];
			   }
			   
			   for(i = 0; i < size; i++) {
				   //printf("%f %d %f\n", alpha, i, window[i]);
			   }
		}
		
		public static double[] decimate(double[] samples) {
			int filterLength = 42;
			filter = new double[filterLength + 1];
			LPFilter(7612.5, filterLength, alpha);
			double[] filteredSamples = new double[samples.length + 1];
			for(int index = 0; index < samples.length; index++) {
				filteredSamples[index] = 0.0;
				for(int filterIndex = 0; filterIndex < filter.length; filterIndex++) {
					int innerIndex = index + filterIndex - filterLength / 2;
					if(innerIndex < 0) continue;
					if(innerIndex == samples.length) break;
					filteredSamples[index] += samples[innerIndex] * filter[filterIndex];
				}
			}
			int outputLength = 0;
			for(int index = 0; index < samples.length; index += 2) outputLength++;
			double[] output = new double[outputLength];
			for(int index = 0; index < samples.length; index += 2) {
				output[index / 2] = filteredSamples[index];
			}
			return output;
		}
		
		public static double[] applyFilter(double minus3dBFreq, double filterBins, double[] samples, FilterType type) {
			int filterLength = (int) Math.round((samplingRate / minus3dBFreq) * filterBins);
			filterLength += filterLength % 1;
			filter = new double[filterLength + 1];
			if(type == FilterType.HIGHPASS) HPFilter(minus3dBFreq, filterLength, alpha);
			if(type == FilterType.LOWPASS) LPFilter(minus3dBFreq, filterLength, alpha);
			if(type == FilterType.BANDPASS) BPFilter(minus3dBFreq, filterLength, alpha);
			double[] filteredSamples = new double[samples.length + 1];
			for(int index = 0; index < samples.length; index++) {
				filteredSamples[index] = 0.0;
				for(int filterIndex = 0; filterIndex < filter.length; filterIndex++) {
					int innerIndex = index + filterIndex - filter.length / 2;
					if(innerIndex < 0) continue;
					if(innerIndex == samples.length) break;
					filteredSamples[index] += samples[innerIndex] * filter[filterIndex];
				}
			}
			return filteredSamples;
		}
		
		public static ArrayList<CriticalBand> calculateCriticalBands(double barkStep) {
			ArrayList<CriticalBand> criticalBands = new ArrayList<CriticalBand>();
			double startFreqInHz = FDData.minFrequencyInHz;
			double startBark = 13.0 * Math.atan(0.00076 * startFreqInHz) + 3.5 * Math.atan((startFreqInHz / 7500.0) * (startFreqInHz / 7500.0));
			double endBark = 0.0;
			double endFreqInHz = startFreqInHz;
			double minRatio = Math.pow(2.0, 1 / 1000.0);
			while(endFreqInHz < FDData.maxFrequencyInHz) {
				endFreqInHz *= minRatio;
				endBark = 13.0 * Math.atan(0.00076 * endFreqInHz) + 3.5 * Math.atan((endFreqInHz / 7500.0) * (endFreqInHz / 7500.0));
				if(endBark - startBark > barkStep) {
					criticalBands.add(new CriticalBand(startFreqInHz, endFreqInHz));
					startBark = endBark;
					startFreqInHz = endFreqInHz;
				}
			}
			criticalBands.add(new CriticalBand(startFreqInHz, FDData.maxFrequencyInHz));
			for(CriticalBand bounds: criticalBands) {
				System.out.println("Critical Band: " + bounds.getLowerBound() + " " + bounds.getUpperBound());
			}
			return criticalBands;
			
		}
		
		private static boolean finished = true;
		private static double b0 = 0.0;
		private static double b1 = 0.0;
		private static double b2 = 0.0;
		private static double a0 = 0.0;
		private static double a1 = 0.0;
		private static int IIRIndex = 0;
		
		public static double[] applyIIRFilterOrder1(double[] input, double freq, FilterType type) {
			double gamma = Math.tan((Math.PI * freq) / SynthTools.sampleRate);
			double D = gamma * gamma + Math.sqrt(2.0) * gamma + 1.0;
			b0 = (gamma * gamma) / D;
			b1 = 2.0 * (gamma * gamma) / D;
			b2 = b0;
			a0 = 2.0 * (gamma * gamma - 1.0) / D;
			a1 = (gamma * gamma - Math.sqrt(2.0) * gamma + 1.0) / D;
			System.out.println(b0 + " " + b1 + " " + b2 + " " + a0 + " " + a1);
			double[] y = new double[input.length];
			for(int index = 0; index < input.length; index++) {
				y[index] = 0.0;
			}
			//y[0] = b0 * input[0];
			//y[1] = b0 * input[1] + b1 * input[0] + a0 * y[0];
			for(int n = 2; n < input.length; n++) {
				y[n] = b0 * input[n] + b1 * input[n - 1] + b2 * input[n - 2] - a0 * y[n - 1] - a1 * y[n - 2];
				//y[n] = 1.1429 * y[n - 1] - 0.4712 * y[n - 2] + 0.067 * input[n] + 0.135 * input[n -1] + 0.067 * input[n - 2];
				
			}
			return y;
		}

}
