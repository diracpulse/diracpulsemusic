package main;

import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;

public class Filter {
	
	public enum FilterType {
		LOWPASS,
		HIGHPASS,
		BANDPASS
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
				
		void setLPFilter(double[] filter) {
			this.lpFilter = filter;
		}
		
		void setHPFilter(double[] filter) {
			this.hpFilter = filter;
		}
		
		int getMaxFilterLength() {
			if(lpFilter == null && hpFilter == null) return 0;
			if(lpFilter == null) return hpFilter.length;
			if(hpFilter == null) return lpFilter.length;
			if(lpFilter.length > hpFilter.length) return lpFilter.length;
			return hpFilter.length;
		}
		
		void calculateRandomness(FDData.Channel channel) {
			TreeMap<Integer, TreeSet<Integer>> timeToFreqsAtMaxima = null;
			double[][] logAmplitudes = null;
			double[][] randomness = null;
			if(channel == FDData.Channel.LEFT) logAmplitudes = DFTEditor.amplitudesLeft;
			if(channel == FDData.Channel.RIGHT) logAmplitudes = DFTEditor.amplitudesRight;
			if(channel == FDData.Channel.LEFT) timeToFreqsAtMaxima = DFTEditor.timeToFreqsAtMaximaLeft;
			if(channel == FDData.Channel.RIGHT) timeToFreqsAtMaxima = DFTEditor.timeToFreqsAtMaximaRight;
			if(channel == FDData.Channel.LEFT) randomness = DFTEditor.randomnessLeft;
			if(channel == FDData.Channel.RIGHT) randomness = DFTEditor.randomnessRight;
			double absMaxDerivative = 0.0;
			int timeStep = 10;
			int startNote = DFT2.frequencyToNote(lowerBound);
			int endNote = DFT2.frequencyToNote(upperBound);
			int numNotes = endNote - startNote + 1;
			for(int outerTime = 0; outerTime < DFTEditor.maxTime; outerTime += timeStep) {
				double[] derivatives = new double[timeStep * numNotes];
				int dIndex = 0;
				for(int innerTime = outerTime; innerTime < outerTime + timeStep; innerTime++) {
					if(innerTime >= DFTEditor.maxTime) break;
					for(int note = startNote; note < endNote; note++) {
						if(DFTEditor.noteToFreq(note) == 0) {
							derivatives[dIndex] = logAmplitudes[innerTime][DFTEditor.noteToFreq(note)];
							continue;
						}
						derivatives[dIndex] = logAmplitudes[innerTime][DFTEditor.noteToFreq(note) - 1] - logAmplitudes[innerTime][DFTEditor.noteToFreq(note)];
						if(Math.abs(derivatives[dIndex]) > absMaxDerivative) absMaxDerivative = Math.abs(derivatives[dIndex]);
						dIndex++;
					}
				}
				if(absMaxDerivative == 0.0) absMaxDerivative = 1.0; // All zeros so just divide by one;
				double[] C = new double[timeStep * numNotes];
				for(int k = 0; k < C.length; k++) {
					C[k] = 0.0;
					if(derivatives[k] <= 0) derivatives[k] = -1;
					if(derivatives[k] > 0) derivatives[k] = 1;
				}
				for(int k = 0; k < C.length; k++) {
					for(int j = 0; j < C.length; j++) {
						C[k] += derivatives[j] * derivatives[(k + j) % C.length];
					}
					C[k] /= (C.length);
				}
				double r = 0;
				for(int k = 0; k < C.length; k++) {
					r += Math.abs(C[k]);
				}
				r = 1.0 - (1.0 / (C.length) * r);
				//System.out.println(r);
				r = Math.pow(r, 3.0);
				//r = 1 / (-1.0 * Math.log(r)/Math.log(2.0));
				for(int time = outerTime; time < outerTime + timeStep; time++) {
					if(time >= DFTEditor.maxTime) break;
					for(int note = startNote; note < endNote; note++) {
						randomness[time][DFTEditor.noteToFreq(note)] = r;
						//System.out.println(randomness[time][DFTEditor.noteToFreq(note)]);
					}
				}
			}
		}
		
		void filterMaximasByAmplitude(FDData.Channel channel, 
						   TreeMap<Integer, TreeMap<Integer, FDData>> timeToFreqToData,
						   TreeMap<Double, SynthTools.IntPair> amplitudeToTimeAndFreq) {
			TreeMap<Integer, TreeSet<Integer>> timeToFreqsAtMaxima = null;
			double[][] logAmplitudes = null;
			if(channel == FDData.Channel.LEFT) timeToFreqsAtMaxima = DFTEditor.timeToFreqsAtMaximaLeft;
			if(channel == FDData.Channel.RIGHT) timeToFreqsAtMaxima = DFTEditor.timeToFreqsAtMaximaRight;
			if(channel == FDData.Channel.LEFT) logAmplitudes = DFTEditor.amplitudesLeft;
			if(channel == FDData.Channel.RIGHT) logAmplitudes = DFTEditor.amplitudesRight;
			for(int time = 0; time < timeToFreqToData.lastKey(); time++) {
				if(!timeToFreqToData.containsKey(time)) continue;
				TreeMap<Double, Integer> logAmplitudeToNote = new TreeMap<Double, Integer>();
				for(int note = DFT2.frequencyToNote(lowerBound); note < DFT2.frequencyToNote(upperBound); note++) {
					if(timeToFreqToData.get(time).containsKey(DFTEditor.noteToFreq(note))) {
						double logAmplitude = timeToFreqToData.get(time).get(DFTEditor.noteToFreq(note)).getLogAmplitude();
						while(logAmplitudeToNote.containsKey(logAmplitude)) logAmplitude += 0.0000001; 
						logAmplitudeToNote.put(logAmplitude, note);
					}
				}
				if(logAmplitudeToNote.isEmpty()) continue;
				logAmplitudeToNote.remove(logAmplitudeToNote.lastKey());
				for(int note: logAmplitudeToNote.values()) {
					timeToFreqToData.get(time).remove(DFTEditor.noteToFreq(note));
					timeToFreqsAtMaxima.get(time).remove(DFTEditor.noteToFreq(note));
					amplitudeToTimeAndFreq.remove(logAmplitudes[time][DFTEditor.noteToFreq(note)]);
				}
			}
		}
		
		void filterMaximasByHarmonicMaxima(FDData.Channel channel, 
				   TreeMap<Integer, TreeMap<Integer, FDData>> timeToFreqToData,
				   TreeMap<Double, SynthTools.IntPair> amplitudeToTimeAndFreq) {
			TreeMap<Integer, TreeSet<Integer>> timeToFreqsAtMaxima = null;
			double[][] logAmplitudes = null;
			if(channel == FDData.Channel.LEFT) timeToFreqsAtMaxima = DFTEditor.timeToFreqsAtMaximaLeft;
			if(channel == FDData.Channel.RIGHT) timeToFreqsAtMaxima = DFTEditor.timeToFreqsAtMaximaRight;
			if(channel == FDData.Channel.LEFT) logAmplitudes = DFTEditor.amplitudesLeft;
			if(channel == FDData.Channel.RIGHT) logAmplitudes = DFTEditor.amplitudesRight;
			for(int time = 0; time < timeToFreqToData.lastKey(); time++) {
				if(!timeToFreqToData.containsKey(time)) continue;
				TreeMap<Double, TreeSet<Integer>> harmonicMaximaToNotes = new TreeMap<Double, TreeSet<Integer>>();
				for(int note = DFT2.frequencyToNote(lowerBound); note < DFT2.frequencyToNote(upperBound); note++) {
					if(timeToFreqToData.get(time).containsKey(DFTEditor.noteToFreq(note))) {
						long harmonicID = timeToFreqToData.get(time).get(DFTEditor.noteToFreq(note)).getHarmonicID();
						if(!DFTEditor.harmonicIDToHarmonic.containsKey(harmonicID)) {
							 timeToFreqToData.get(time).remove(DFTEditor.noteToFreq(note));
							 timeToFreqsAtMaxima.get(time).remove(DFTEditor.noteToFreq(note));
							 continue;
						}
						double harmonicMaxima = DFTEditor.harmonicIDToHarmonic.get(harmonicID).getMaxLogAmplitude();
						if(harmonicMaximaToNotes.containsKey(harmonicMaxima)) {
							harmonicMaximaToNotes.get(harmonicMaxima).add(note);
						} else {
							harmonicMaximaToNotes.put(harmonicMaxima, new TreeSet<Integer>());
							harmonicMaximaToNotes.get(harmonicMaxima).add(note);
						}
					}
				}
				if(harmonicMaximaToNotes.isEmpty()) continue;
				harmonicMaximaToNotes.remove(harmonicMaximaToNotes.lastKey());
				for(double harmonicMaxima: harmonicMaximaToNotes.keySet()) {
					for(int note: harmonicMaximaToNotes.get(harmonicMaxima)) {
						timeToFreqToData.get(time).remove(DFTEditor.noteToFreq(note));
						timeToFreqsAtMaxima.get(time).remove(DFTEditor.noteToFreq(note));
						amplitudeToTimeAndFreq.remove(logAmplitudes[time][DFTEditor.noteToFreq(note)]);
					}
				}
			}
		}

		// Returns average noise
		void synthNoise(FDData.Channel channel, double[] sharedPCMData, double[] noise) {
			if(lpFilter == null && hpFilter == null) return;
			double[][] amplitudes = null;
			if(channel == FDData.Channel.LEFT) amplitudes = DFTEditor.amplitudesLeft;
			if(channel == FDData.Channel.RIGHT) amplitudes = DFTEditor.amplitudesRight;
			int numNotes = DFT2.frequencyToNote(upperBound) - DFT2.frequencyToNote(lowerBound);
			double bandwidth = upperBound - lowerBound;
			double[] timeToNoiseAmplitude = new double[DFTEditor.maxTime];
			for(int time = 0; time < DFTEditor.maxTime; time++) {
				timeToNoiseAmplitude[time] = 0.0;
				for(int note = DFT2.frequencyToNote(lowerBound); note < DFT2.frequencyToNote(upperBound); note++) {
					timeToNoiseAmplitude[time] += Math.pow(FDData.logBase, amplitudes[time][DFTEditor.noteToFreq(note)]);
				}
				timeToNoiseAmplitude[time] /= numNotes;
			}
			double[] lpFilteredNoise = new double[sharedPCMData.length];
			double[] hpFilteredNoise = new double[sharedPCMData.length];
			double maxFilteredSample = 0.0;
			if(lpFilter != null) {
				for(int time = 0; time < sharedPCMData.length; time++) {
					double filteredSample = 0.0;
					for(int filterIndex = 0; filterIndex < lpFilter.length; filterIndex++) {
						int innerIndex = time + filterIndex - lpFilter.length / 2;
						if(innerIndex < 0) continue;
						if(innerIndex >= sharedPCMData.length) break;
						filteredSample += noise[innerIndex] * lpFilter[filterIndex];
					}
					lpFilteredNoise[time] = filteredSample;
				}
			} else {
				for(int time = 0; time < sharedPCMData.length; time++) {
					lpFilteredNoise[time] = noise[time];
				}
			}
			if(hpFilter != null) {
				for(int time = 0; time < hpFilteredNoise.length; time++) {
					double filteredSample = 0.0;
					for(int filterIndex = 0; filterIndex < hpFilter.length; filterIndex++) {
						int innerIndex = time + filterIndex - hpFilter.length / 2;
						if(innerIndex < 0) continue;
						if(innerIndex >= sharedPCMData.length) break;
						filteredSample += lpFilteredNoise[innerIndex] * hpFilter[filterIndex];
					}
					if(filteredSample > maxFilteredSample) maxFilteredSample = filteredSample;
					hpFilteredNoise[time] = filteredSample;
				}
			} else {
				for(int time = 0; time < sharedPCMData.length; time++) {
					hpFilteredNoise[time] = lpFilteredNoise[time];
					if(hpFilteredNoise[time] > maxFilteredSample) maxFilteredSample = hpFilteredNoise[time];
				}
			}
			double timeToSample = SynthTools.sampleRate * (FDData.timeStepInMillis / 1000.0);
			for(int time = 0; time < DFTEditor.maxTime - 1; time++) {
				int lowerTime = (int) Math.round(time * timeToSample);
				int upperTime = (int) Math.round((time + 1) * timeToSample);
				double lowerAmplitude = timeToNoiseAmplitude[time];
				double upperAmplitude = timeToNoiseAmplitude[time + 1];
				double ampSlope = (upperAmplitude - lowerAmplitude) / (upperTime - lowerTime);
				for(int timeIndex = lowerTime; timeIndex < upperTime; timeIndex++) {
					if(timeIndex >= sharedPCMData.length) break;
					double amplitude = lowerAmplitude + (timeIndex - lowerTime) * ampSlope;
					sharedPCMData[timeIndex] += (amplitude * hpFilteredNoise[timeIndex]) / maxFilteredSample;
				}	
			}
		}
	}
	
	public static void createBackgroundNoise(FDData.Channel channel, double[] sharedPCMData, double logNoiseVolume) {
		createCriticalBands();
		if(noiseCriticalBands.get(2).lpFilter == null) {
			createCriticalBandFilters();
		}
		double[] outputPCM = new double[sharedPCMData.length];
		for(int index = 0; index < outputPCM.length; index++) {
			outputPCM[index] = 0.0;
		}
		Random random = new Random();
		double[] noise = new double[sharedPCMData.length];
		for(int sample = 0; sample < noise.length; sample++) {
			noise[sample] = random.nextDouble() - 0.5;
		}
		for(CriticalBand criticalBand: noiseCriticalBands) {
			criticalBand.synthNoise(channel, outputPCM, noise);
		}
		double maxAmplitude = 0.0;
		for(int index = 0; index < outputPCM.length; index++) {
			if(outputPCM[index] > Math.abs(maxAmplitude)) maxAmplitude = Math.abs(outputPCM[index]);
		}
		double noiseVolume = Math.pow(FDData.logBase, logNoiseVolume);
		for(int index = 0; index < outputPCM.length; index++) {
			sharedPCMData[index] += (outputPCM[index] / maxAmplitude) * noiseVolume;
		}
	}
	
	public static void applyCriticalBandFiltering(FDData.Channel channel, 
												  TreeMap<Integer, TreeMap<Integer, FDData>> timeToFreqToData,
												  TreeMap<Double, SynthTools.IntPair> amplitudeToTimeAndFreq) {
		createCriticalBands();
		for(CriticalBand criticalBand: criticalBands) criticalBand.filterMaximasByHarmonicMaxima(channel, timeToFreqToData, amplitudeToTimeAndFreq);
	}
	
	public static void calculateRandomness(FDData.Channel channel) { 
		createCriticalBands();
		for(CriticalBand criticalBand: criticalBands) criticalBand.calculateRandomness(channel);
	}
	
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
	
	public static double[] getLPFilter(double freq, int order, double alpha) {
		filter = new double[order + 1];
		LPFilter(freq, order, alpha);
		double[] returnVal = new double[filter.length];
		for(int index = 0; index < filter.length; index++) {
			returnVal[index] = filter[index];
		}
		return returnVal;
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
	
	public static double[] filterAndMultiply(double testFreq, double[] samples) {
		//int filterLength = (int) Math.round((samplingRate / testFreq) * 6.0);
		int filterLength = (int) Math.round((samplingRate / testFreq) * 10.0);
		filterLength = filterLength + filterLength % 2;
		//filter = new double[filterLength + 1];
		double[] lpFilter = getLPFilter(testFreq * optimalLPRejectRatio, filterLength, alpha);
		double[] hpFilter = getHPFilter(testFreq / optimalLPRejectRatio, filterLength, alpha);
		double[] lpFilteredSamples = new double[samples.length + 1];
		double[] hpFilteredSamples = new double[samples.length + 1];
		for(int index = 0; index < samples.length; index++) {
			lpFilteredSamples[index] = 0.0;
			for(int filterIndex = 0; filterIndex < lpFilter.length; filterIndex++) {
				int innerIndex = index + filterIndex - filterLength / 2;
				if(innerIndex < 0) continue;
				if(innerIndex == samples.length) break;
				lpFilteredSamples[index] += samples[innerIndex] * lpFilter[filterIndex];
			}
		}
		for(int index = 0; index < samples.length; index++) {
			hpFilteredSamples[index] = 0.0;
			for(int filterIndex = 0; filterIndex < hpFilter.length; filterIndex++) {
				int innerIndex = index + filterIndex - filterLength / 2;
				if(innerIndex < 0) continue;
				if(innerIndex == samples.length) break;
				hpFilteredSamples[index] += lpFilteredSamples[innerIndex] * hpFilter[filterIndex];
			}
		}
		double phase = 0.0;
		double deltaPhase = (testFreq / SynthTools.sampleRate) * SynthTools.twoPI;
		for(int index = 0; index < samples.length; index++) {
			hpFilteredSamples[index] *= Math.sin(phase) * 2.0;
			phase += deltaPhase;
		}
		return hpFilteredSamples;
	}
	
	
	// Filters out all frequencies > testFreq * 2
	public static double[] LPOctaveFilter(double testFreq, double[] samples) {
		int filterLength = (int) Math.round((samplingRate / testFreq) * 6.0);
		filterLength = filterLength + filterLength % 2;
		filter = new double[filterLength + 1];
		LPFilter(testFreq * optimalLPRejectRatio, filterLength, alpha);
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
		return filteredSamples;
	}
	
	public static double[] getLPFilter(int criticalBandIndex, double maxPassFreq, double minRejectFreq) {
		if(maxPassFreq == FDData.maxFrequencyInHz) return null;
		double maxFilterBins = 10.0;
		double minFilterBins = maxFilterBins;
		double optimumMinus3dBPoint = maxPassFreq; // (minRejectFreq - maxPassFreq) * 0.25 + maxPassFreq;
		for(double filterBins = 1.0; filterBins < minFilterBins; filterBins += 0.5) {
			int filterLength = (int) Math.round((samplingRate / minRejectFreq) * filterBins);
			filterLength += filterLength % 2;
			int signalLength = filterLength * 4;
			double maxPassValue = 0.0;
			double minRejectValue = 0.0;
			double[] maxPassSignal = new double[signalLength];
			double[] minRejectSignal = new double[signalLength];
			double maxPassPhase = 0.0;
			double minRejectPhase = 0.0;
			double maxPassDeltaPhase = maxPassFreq / SynthTools.sampleRate * Math.PI * 2.0;
			double minRejectDeltaPhase = minRejectFreq / SynthTools.sampleRate * Math.PI * 2.0;			
			for(int index = 0; index < signalLength; index++) {
				double sinWindow = Math.sin((double) index / signalLength * Math.PI);
				maxPassSignal[index] = Math.sin(maxPassPhase) * sinWindow;
				minRejectSignal[index] = Math.sin(minRejectPhase) * sinWindow;
				maxPassPhase += maxPassDeltaPhase;
				minRejectPhase += minRejectDeltaPhase;
			}
			filter = new double[filterLength + 1];
			LPFilter(optimumMinus3dBPoint, filterLength, alpha);
			for(int index = 0; index < signalLength; index++) {
				double maxPassTestValue = 0.0;
				double minRejectTestValue = 0.0;
				for(int filterIndex = 0; filterIndex < filter.length; filterIndex++) {
					int innerIndex = index + filterIndex;
					if(innerIndex < 0) continue;
					if(innerIndex >= signalLength) break;
					maxPassTestValue += maxPassSignal[innerIndex] * filter[filterIndex];
					minRejectTestValue += minRejectSignal[innerIndex] * filter[filterIndex];
				}
				if(Math.abs(maxPassTestValue) > maxPassValue) maxPassValue = Math.abs(maxPassTestValue);
				if(Math.abs(minRejectTestValue) > minRejectValue) minRejectValue = Math.abs(minRejectTestValue);
			}
			if(maxPassValue / minRejectValue > 10.0) {
				if(filterBins < minFilterBins) {
					minFilterBins = filterBins;
					break;
				}
			}
		}
		int filterLength = (int) Math.round((samplingRate / minRejectFreq) * minFilterBins);
		filterLength += filterLength % 2;
		filter = new double[filterLength + 1];
		LPFilter(optimumMinus3dBPoint, filterLength, alpha);
		System.out.println("noiseCriticalBands.get(" + criticalBandIndex + ").setLPFilter(Filter.getLPFilter(" + optimumMinus3dBPoint + " , " + filterLength + ", alpha)); " + "// " + (optimumMinus3dBPoint - maxPassFreq) / (minRejectFreq - maxPassFreq) + " " + maxPassFreq + " " + minRejectFreq + " " + minFilterBins);
		double[] returnVal = new double[filterLength];
		for(int index = 0; index < returnVal.length; index++) {
			returnVal[index] = filter[index];
		}
		return returnVal;
	}
	
	public static double[] getHPFilter(int criticalBandIndex, double minPassFreq, double maxRejectFreq) {
		if(minPassFreq == FDData.minFrequencyInHz) return null;
		double maxFilterBins = 10.0;
		double minFilterBins = maxFilterBins;
		double optimumMinus3dBPoint = minPassFreq; // (maxRejectFreq - minPassFreq) * 0.25 + minPassFreq;
		for(double filterBins = 1.0; filterBins < minFilterBins; filterBins += 0.5) {
			int filterLength = (int) Math.round((samplingRate / maxRejectFreq) * filterBins);
			filterLength += filterLength % 2;
			int signalLength = filterLength * 4;
			double minPassValue = 0.0;
			double maxRejectValue = 0.0;
			double[] minPassSignal = new double[signalLength];
			double[] maxRejectSignal = new double[signalLength];
			double minPassPhase = 0.0;
			double maxRejectPhase = 0.0;
			double minPassDeltaPhase = minPassFreq / SynthTools.sampleRate * Math.PI * 2.0;
			double maxRejectDeltaPhase = maxRejectFreq / SynthTools.sampleRate * Math.PI * 2.0;			
			for(int index = 0; index < signalLength; index++) {
				double sinWindow = Math.sin((double) index / signalLength * Math.PI);
				minPassSignal[index] = Math.sin(minPassPhase) * sinWindow;
				maxRejectSignal[index] = Math.sin(maxRejectPhase) * sinWindow;
				minPassPhase += minPassDeltaPhase;
				maxRejectPhase += maxRejectDeltaPhase;
			}
			filter = new double[filterLength + 1];
			HPFilter(optimumMinus3dBPoint, filterLength, alpha);
			for(int index = 0; index < signalLength; index++) {
				double minPassTestValue = 0.0;
				double maxRejectTestValue = 0.0;
				for(int filterIndex = 0; filterIndex < filter.length; filterIndex++) {
					int innerIndex = index + filterIndex;
					if(innerIndex < 0) continue;
					if(innerIndex >= signalLength) break;
					minPassTestValue += minPassSignal[innerIndex] * filter[filterIndex];
					maxRejectTestValue += maxRejectSignal[innerIndex] * filter[filterIndex];
				}
				if(Math.abs(minPassTestValue) > minPassValue) minPassValue = Math.abs(minPassTestValue);
				if(Math.abs(maxRejectTestValue) > maxRejectValue) maxRejectValue = Math.abs(maxRejectTestValue);
			}
			if(minPassValue / maxRejectValue > 10.0) {
				if(filterBins < minFilterBins) {
					minFilterBins = filterBins;
					break;
				}
			}
		}
		int filterLength = (int) Math.round((samplingRate / maxRejectFreq) * minFilterBins);
		filterLength += filterLength % 2;
		filter = new double[filterLength + 1];
		HPFilter(optimumMinus3dBPoint, filterLength, alpha);
		System.out.println("noiseCriticalBands.get(" + criticalBandIndex + ").setHPFilter(Filter.getHPFilter(" + optimumMinus3dBPoint + " , " + filterLength + ", alpha)); " + "// " + (optimumMinus3dBPoint - minPassFreq) / (maxRejectFreq - minPassFreq) + " " + minPassFreq + " " + maxRejectFreq + " " + minFilterBins);
		double[] returnVal = new double[filterLength];
		for(int index = 0; index < returnVal.length; index++) {
			returnVal[index] = filter[index];
		}
		return returnVal;
	}

	// returns true when optimum filter is found, start with filterBins low and run until true
	public static boolean testFilter(CriticalBand criticalBand, double filterBins, FilterType type) {
		double passFreq = criticalBand.getCenterFreq();
		int filterLength = (int) Math.round((samplingRate / passFreq) * filterBins);
		filterLength = filterLength + filterLength % 2;
		double samplesPerCycle = samplingRate / passFreq;
		int signalLength = (int) filterLength * 4;
		double[] testFreqs = {criticalBand.getLowerBound(), criticalBand.getCenterFreq(), criticalBand.getUpperBound()};
		double[][] testSignals = new double[testFreqs.length][signalLength];
 		double[] maxTestValue = new double[testFreqs.length];
 		for(int index = 0; index < testFreqs.length; index++) maxTestValue[index] = 0.0;
		filter = new double[filterLength + 1];
		if(type == FilterType.BANDPASS) BPFilter(testFreqs[1], filterLength, alpha);
		if(type == FilterType.LOWPASS) LPFilter(testFreqs[2], filterLength, alpha);
		if(type == FilterType.HIGHPASS) HPFilter(testFreqs[0], filterLength, alpha);
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
					testValue += testSignals[freqIndex][innerIndex] * filter[filterIndex];
				}
				if(Math.abs(testValue) > maxTestValue[freqIndex]) maxTestValue[freqIndex] = testValue;
			}
		}
		double valueAtLowerBound = maxTestValue[0];
		double valueAtCenterFreq = maxTestValue[1];
		double valueAtUpperBound = maxTestValue[2];
		if(type == FilterType.BANDPASS) {
			if(maxTestValue[0] / maxTestValue[1] < 0.6) {
				if(maxTestValue[2] / maxTestValue[1] < 0.6) {
					return true;
				}
			}
			
		}
		if(type == FilterType.LOWPASS) {
			if(maxTestValue[0] / maxTestValue[1] > 1.98) {
				if(maxTestValue[2] / maxTestValue[1] < 0.51) {
					return true;
				}
			}
			
		}
		if(type == FilterType.HIGHPASS) {
			if(maxTestValue[0] / maxTestValue[1] < 0.51) {
				if(maxTestValue[2] / maxTestValue[1] > 1.98) {
					return true;
				}
			}
			
		}
		return false;
	}

	public static void createCriticalBands() {
		if(criticalBands != null) return;
		criticalBands = calculateCriticalBands(criticalBandBarkStep);
		noiseCriticalBands = calculateCriticalBands(noiseCriticalBandBarkStep);
	}
	
	public static void createCriticalBandFilters() {
		FilterConstants.initNoiseCriticalBandsWithSoftFilters(noiseCriticalBands);
		/*
		if(noiseCriticalBands.get(1).getMaxFilterLength() == 0) {
			//FilterConstants.initNoiseCriticalBandsWithHardFilters(noiseCriticalBands);
			for(int index = 1; index < noiseCriticalBands.size() -1 ; index++) {
				noiseCriticalBands.get(index).setLPFilter(getLPFilter(index, noiseCriticalBands.get(index).getUpperBound(), noiseCriticalBands.get(index + 1).getUpperBound()));
				noiseCriticalBands.get(index).setHPFilter(getHPFilter(index, noiseCriticalBands.get(index).getLowerBound(), noiseCriticalBands.get(index - 1).getLowerBound()));
			}
			int maxIndex = noiseCriticalBands.size() - 1;
			noiseCriticalBands.get(maxIndex).setHPFilter(getHPFilter(maxIndex, noiseCriticalBands.get(maxIndex).getLowerBound(), noiseCriticalBands.get(maxIndex - 1).getLowerBound()));
			noiseCriticalBands.get(0).setLPFilter(getLPFilter(0, noiseCriticalBands.get(0).getUpperBound(), noiseCriticalBands.get(1).getUpperBound()));
		}
		*/
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

}
