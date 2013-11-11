
public class Filter {

	static double[] filter = null;

	final static double twoPI = 6.283185307179586476925286766559;
	final static double onePI = 3.1415926535897932384626433832795;
	final static double halfPI = 1.5707963267948966192313216916398;
	final static double samplingRate = 44100.0;
	final static double maxBinStep = 1.0;

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
	/*
	public static double[] applyLPFilter(double[] samples, double passFreq) {
		double samplesPerCycle = samplingRate / passFreq;
		int filterLength = (int) Math.round(samplesPerCycle * 4.0);
		filter = new double[filterLength + 1];
		LPFilter(passFreq, filterLength, alpha);
		double[] lpFilter = filter;
		/
		for(int index = 0; index < samples.length - filterLength; index++) {
			for(int filterIndex = 0; filterIndex < filter.length; filterIndex++) {
				passValue += pass[index + filterIndex] * lpFilter[filterIndex];
				rejectValue += reject[index + filterIndex] * lpFilter[filterIndex];
				passValueUnfiltered += pass[index + filterIndex];
				rejectValueUnfiltered += reject[index + filterIndex];
			}
			if(Math.abs(passValue) > maxFilteredValue) maxFilteredValue = passValue;
			passSum += Math.abs(passValue);
			rejectSum += Math.abs(rejectValue);
			passSumUnfiltered += Math.abs(passValueUnfiltered);
			rejectSumUnfiltered += Math.abs(rejectValueUnfiltered);
			
		}
	}
	*/
	public static boolean testLPFilter(int filterLength, double passFreq, double rejectFreq, double cutoffFreq) {
		double upperPassFreq = passFreq * Math.pow(2.0, 1.0 / 4.0);
		double samplesPerCycle = samplingRate / passFreq;
		double bins = (double) filterLength / samplesPerCycle;
		int signalLength = (int) Math.ceil(samplesPerCycle * 100);
		double[] pass = new double[signalLength + filterLength];
		double[] upperPass = new double[signalLength + filterLength];
		double[] reject = new double[signalLength + filterLength];
		double[] cutoff = new double[signalLength + filterLength];
		double maxPassValue = 0.0;
		double maxUpperPassValue = 0.0;
		double maxRejectValue = 0.0;
		double maxCutoffValue = 0.0;
		filter = new double[filterLength + 1];
		LPFilter(rejectFreq, filterLength, alpha);
		double[] lpFilter = filter;
		double phasePass = 0.0;
		double phaseUpperPass = 0.0;
		double phaseReject = 0.0;
		double phaseCutoff = 0.0;
		double deltaPhasePass = (passFreq / SynthTools.sampleRate) * SynthTools.twoPI;
		double deltaUpperPhasePass = (upperPassFreq / SynthTools.sampleRate) * SynthTools.twoPI;
		double deltaPhaseReject = (rejectFreq / SynthTools.sampleRate) * SynthTools.twoPI;
		double deltaPhaseCutoff = (cutoffFreq / SynthTools.sampleRate) * SynthTools.twoPI;
		for(int index = 0; index < pass.length; index += 1) {
			double sinWindow = Math.sin((double) index / signalLength * Math.PI);
			pass[index] = Math.sin(phasePass) * sinWindow;
			upperPass[index] = Math.sin(phaseUpperPass) * sinWindow;
			reject[index] = Math.sin(phaseReject) * sinWindow;
			cutoff[index] = Math.sin(phaseCutoff) * sinWindow;
			phasePass += deltaPhasePass;
			phaseUpperPass += deltaUpperPhasePass;
			phaseReject += deltaPhaseReject;
			phaseCutoff += deltaPhaseCutoff;
		}
		for(int index = 0; index < signalLength - filterLength; index++) {
			double passValue = 0.0;
			double upperPassValue = 0.0;
			double rejectValue = 0.0;
			double cutoffValue = 0.0;
			for(int filterIndex = 0; filterIndex < filter.length; filterIndex++) {
				passValue += pass[index + filterIndex] * lpFilter[filterIndex];
				upperPassValue += upperPass[index + filterIndex] * lpFilter[filterIndex];
				rejectValue += reject[index + filterIndex] * lpFilter[filterIndex];
				cutoffValue += cutoff[index + filterIndex] * lpFilter[filterIndex];
			}
			if(Math.abs(passValue) > maxPassValue) maxPassValue = passValue;
			if(Math.abs(upperPassValue) > maxUpperPassValue) maxUpperPassValue = upperPassValue;
			if(Math.abs(rejectValue) > maxRejectValue) maxRejectValue = rejectValue;
			if(Math.abs(cutoffValue) > maxCutoffValue) maxCutoffValue = cutoffValue;
		}
		if(maxPassValue < Math.pow(0.5, 1.0 / 8.0)) return false;
		// Reject Value < -45db (-90dB);
		if(Math.log(maxCutoffValue) / Math.log(10.0) > -4.5) return false;
		System.out.println(rejectFreq + " " + (float) filterLength + " " + (float) bins + " " + (float) maxPassValue + " " + (float) maxUpperPassValue + " " + (float) maxRejectValue + " " + (float) maxCutoffValue);
		return true;
	}
	
	public static void findMinFilterLength() {
		double passFreq = 2000;
		// double rejectFreq = 2770; // OPTIMAL for 2000, 4000
		double cutoffFreq = 4000;
		double samplesPerCycle = samplingRate / passFreq;
		int minFilterLength = (int) Math.ceil(samplesPerCycle);
		int maxFilterLength = (int) Math.ceil(samplesPerCycle * 10);
		minFilterLength = minFilterLength - minFilterLength % 2;
		for(double rejectFreq = passFreq; rejectFreq < cutoffFreq; rejectFreq += 10) {
			for(int filterLength = minFilterLength; filterLength < maxFilterLength; filterLength += 2) {
				if(testLPFilter(filterLength, passFreq, rejectFreq, cutoffFreq)) break;
			}
		}
	}
}
