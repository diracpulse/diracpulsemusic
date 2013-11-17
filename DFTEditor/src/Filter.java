import java.util.TreeMap;


public class Filter {

	static double[] filter = null;

	final static double twoPI = 6.283185307179586476925286766559;
	final static double onePI = 3.1415926535897932384626433832795;
	final static double halfPI = 1.5707963267948966192313216916398;
	final static double samplingRate = 44100.0;
	final static double maxBinStep = 1.0;
	final static double optimalLPRejectRatio = 1.38;
	public static TreeMap<Float, Integer> passFreqToFilterLength = null;

	final static double alpha = 10.0;
	
	static double BesselI0(double x) {
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
	
	static void LPFilterPrintInfo(double freq, int order, double alpha) {
		double w = 2.0 * (freq / samplingRate);
		double w0 = 0.0;
		double w1 = w * onePI;
		CreateFilterPrintInfo(w0, w1, order, alpha);
	}


	static void HPFilter(double freq, int order, double alpha) {
		double w = 2.0 * (freq / samplingRate);
		double w0 = onePI;
		double w1 = (1.0 - w) * onePI;
		CreateFilter(w0, w1, order, alpha);
	}

	static void BPFilter(double freq, int order, double alpha) {
		double w = 2.0 * (freq / samplingRate);
		double w0 = w * onePI;
		double w1 = w / -4.0;
		CreateFilter(w0, w1, order, alpha);
	}

	static void CreateFilter(double w0, double w1, int order, double alpha) {
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
	
	static void CreateFilterPrintInfo(double w0, double w1, int order, double alpha) {
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
		
		for (n=1; n <= order; n++) {
			System.out.println("BEFORE" + n + " " + filter[n]);
		}
		
			
		// shift impulse response to make filter causal:
		for (n=m+1; n<=order; n++) filter[n] = filter[n - m];
		for (n=0; n<=m-1; n++) filter[n] = filter[order - n];
		filter[m] = w1 / onePI;
		
		for (n=1; n <= order; n++) {
			System.out.println("AFTER" + n + " " + filter[n]);
		}
		
		return;
	}
	
	static void CreateWindow(double[] window, int size, double alpha) {
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

	
	public static double[] getFilteredNoise(int duration, int note, double amplitude) {
		double[] returnVal = new double[duration];
		double freq = Math.pow(2.0, (double) note / (double) FDData.noteBase);
		double cyclesPerWindow = maxBinStep / (Math.pow(2.0, 1.0 / FDData.noteBase) - 1.0);
		double samplesPerCycle = samplingRate / freq;
		int windowLength = (int) Math.round(cyclesPerWindow * samplesPerCycle);
		double[] noise = new double[duration + windowLength + 1];
		filter = new double[windowLength * 2];
		BPFilter(freq, windowLength, alpha);
		double[] bpFilter = filter;
		double currentPhase = 0.0;
		double deltaPhase = (freq / SynthTools.sampleRate) * SynthTools.twoPI;
		for(int index = 0; index < duration + windowLength; index += 1) {
			//double x1 = Math.random();
			//double x2 = Math.random();
		    //double y1 = Math.sqrt(-2 * Math.log(x1)) * Math.cos(2 * Math.PI * x2 );
		    //double y2 = Math.sqrt(-2 * Math.log(x1)) * Math.sin(2 * Math.PI * x2 );
		    //noise[index] = y1;
		    //noise[index + 1] = y2;
			noise[index] = Math.random();
			currentPhase += deltaPhase;
		}
		for(int returnIndex = 0; returnIndex < duration; returnIndex++) {
			double value = 0.0;
			for(int filterIndex = 0; filterIndex < windowLength; filterIndex++) {
				value += noise[returnIndex + filterIndex] * bpFilter[filterIndex];
			}
			returnVal[returnIndex] = value;
		}
		double maxAmplitude = 0.0;
		for(int returnIndex = 0; returnIndex < duration; returnIndex++) {
			if(returnVal[returnIndex] > maxAmplitude) maxAmplitude = returnVal[returnIndex];
		}
		for(int returnIndex = 0; returnIndex < duration; returnIndex++) {
			returnVal[returnIndex] *= amplitude / maxAmplitude;
		}
		return returnVal;	
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
	
	// IMPORTANT: (testFreq < samplingRate / 8.0) to avoid aliasing
	public static double[] filterAndMultiply(double testFreq, double[] samples) {
		//if(passFreqToFilterLength == null) FilterConstants.initPassFreqToFilterLength();
		int filterLength = (int) Math.round((samplingRate / testFreq) * 6.0);
		filterLength = filterLength + filterLength % 2;
		//if(passFreqToFilterLength.containsKey((float)testFreq)) filterLength = passFreqToFilterLength.get((float)testFreq);
		filter = new double[filterLength + 1];
		LPFilter(testFreq * optimalLPRejectRatio, filterLength, alpha);
		double filterGain = 0.0;
		double[] filteredSamples = new double[samples.length + 1];
		double phase = 0.0;
		double deltaPhase = (testFreq / SynthTools.sampleRate) * SynthTools.twoPI;
		/*
		for(int index = 0; index < filterLength; index++) {
			filterGain += Math.abs(filter[index]);
			phase += deltaPhase;
		}
		System.out.println(filterGain);
		phase = 0.0;
		*/
		for(int index = 0; index < samples.length; index++) {
			filteredSamples[index] = 0.0;
			for(int filterIndex = 0; filterIndex < filter.length; filterIndex++) {
				int innerIndex = index + filterIndex - filterLength / 2;
				if(innerIndex < 0) continue;
				if(innerIndex == samples.length) break;
				filteredSamples[index] += samples[innerIndex] * filter[filterIndex];
			}
		}
		for(int index = 0; index < samples.length; index++) {
			filteredSamples[index] *= Math.sin(phase) * 2.0;
			phase += deltaPhase;
		}
		return filteredSamples;
	}
	
	public static void testBPFilter(int filterLength, double passFreq) {
		double samplesPerCycle = samplingRate / passFreq;
		int signalLength = (int) Math.ceil(samplesPerCycle * 100) + filterLength;
		double[] testFreqs = {passFreq * .5, passFreq * .75, passFreq, passFreq * 1.5, passFreq * 2.0};
		double[][] testSignals = new double[testFreqs.length][signalLength];
 		double[] maxTestValue = new double[testFreqs.length];
 		for(int index = 0; index < testFreqs.length; index++) maxTestValue[index] = 0.0;
		filter = new double[filterLength + 1];
		BPFilter(passFreq, filterLength, alpha);
		double[] lpFilter = filter;
		double deltaPhase[] = new double[testFreqs.length];
		for(int index = 0; index < testFreqs.length; index++) deltaPhase[index] = (testFreqs[index] / SynthTools.sampleRate) * SynthTools.twoPI;
		for(int freqIndex = 0; freqIndex < testFreqs.length; freqIndex++) {
			double phase = 0.0;
			for(int index = 0; index < signalLength; index++) {
				double sinWindow = Math.sin((double) index / signalLength * Math.PI);
				testSignals[freqIndex][index] = Math.sin(phase) * sinWindow;
				phase += deltaPhase[freqIndex];
			}
		}
		for(int freqIndex = 0; freqIndex < testFreqs.length; freqIndex++) {
			for(int index = 0; index < signalLength; index++) {
				double testValue = 0.0;
				for(int filterIndex = 0; filterIndex < filter.length; filterIndex++) {
					int innerIndex = index + filterIndex;
					if(innerIndex < 0) continue;
					if(innerIndex >= signalLength) break;
					testValue += testSignals[freqIndex][innerIndex] * lpFilter[filterIndex];
				}
				if(Math.abs(testValue) > maxTestValue[freqIndex]) maxTestValue[freqIndex] = testValue;
			}
		}
		System.out.print(filterLength + " :");
		for(int freqIndex = 0; freqIndex < testFreqs.length; freqIndex++) {	
			System.out.print((float) testFreqs[freqIndex] / passFreq + " = " + (float) Math.round(maxTestValue[freqIndex] * 1000.0) / 1000.0 + " | ");
		}
		System.out.println();
		return;
	}
	
	public static void testFilters() {
		filter = new double[43];
		LPFilterPrintInfo(7612.5, 42, alpha);
		double minFreq = samplingRate / 8.0;
		// double rejectFreq = 2770; // OPTIMAL for 2000, 4000
		double maxFreq = samplingRate / 4.0;
		double samplesPerCycle = samplingRate / minFreq;
		int minFilterLength = (int) Math.ceil(samplesPerCycle * 5);
		int maxFilterLength = (int) Math.ceil(samplesPerCycle * 20);
		minFilterLength = minFilterLength - minFilterLength % 2;
		for(double testFreq = minFreq; testFreq < maxFreq; testFreq += 10000) {
			for(int filterLength = minFilterLength; filterLength < maxFilterLength; filterLength += 2) {
				testBPFilter(filterLength, testFreq);
			}
		}
	}
	
	public static void calculatePassFreqToMinFilterLength() {
		passFreqToFilterLength = new TreeMap<Float, Integer>();
		double noteRatio = Math.pow(2.0, -1.0 / FDData.noteBase);
		for(double passFreq = samplingRate / 4.0; passFreq >= samplingRate / 32.0; passFreq *= noteRatio) {
			passFreqToFilterLength.put((float) passFreq, Integer.MAX_VALUE);
			findMinFilterLength(passFreq);
			System.out.println(passFreq);
		}
		for(float passFreq: passFreqToFilterLength.keySet()) {
			System.out.println("Filter.passFreqToFilterLength.put(" + passFreq + ", " + passFreqToFilterLength.get(passFreq) + ");");
		}
	}
	
	public static void findMinFilterLength(double passFreq) {
		// double rejectFreq = 2770; // OPTIMAL for 2000, 4000
		double cutoffFreq = passFreq * 2.0;
		double maxRejectFreq = passFreq * 2.0;
		double minRejectFreq = passFreq;
		double samplesPerCycle = samplingRate / passFreq;
		int minFilterLength = (int) Math.ceil(samplesPerCycle);
		int maxFilterLength = (int) Math.ceil(samplesPerCycle * 32);
		minFilterLength = minFilterLength - minFilterLength % 2;
		double rejectFreqStep = (maxRejectFreq - minRejectFreq) / 100.0;
		for(double rejectFreq = minRejectFreq; rejectFreq <= maxRejectFreq; rejectFreq += rejectFreqStep) {
			for(int filterLength = minFilterLength; filterLength < maxFilterLength; filterLength += 2) {
				double bins = (double) filterLength / samplesPerCycle;
				if(bins > 6.0) continue;
				/*if(testLPFilter(filterLength, passFreq, rejectFreq, cutoffFreq)) {
					if(passFreqToFilterLength.get((float) passFreq) > filterLength) {
						passFreqToFilterLength.put((float) passFreq, filterLength);
					}
				}
				*/
			}
		}
	}

}
