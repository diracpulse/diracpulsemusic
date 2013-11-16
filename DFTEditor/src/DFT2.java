import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.TreeMap;

public class DFT2 {
	
	private static int MAXDFTWINDOW = 44100;
	private static int MAXSAMPLES = 44100 * 10;
	private static int MAXWAVELETS = 62 * 12;
	private static float LeftRight[] = null;
	private static int stereo = 2;
	private static int headerLengthInBytes = 56; // changed from 44 to accomodate newer .wav files
	private static int sampleLengthInBytes = 4;
	private static int stepIndex = 0;
	private static int maxDFTLength = 0;
	private static int debug = 0;
	public static boolean applyMasking = true;
	public static double maskingFactor = Float.NEGATIVE_INFINITY;
	public boolean hasData = false;
	
	// Wavelet Variables
	private static final double onePI = 3.1415926535897932384626433832795;
	private static final double twoPI = 6.283185307179586476925286766559;
	private static double samplingRate = 44100.0;
	public static double maxBinStep = 2.0;
	//private static double maxFreqHz = samplingRate / 2.0;
	//private static double minFreqHz = 20.0;
	private static double maxWindowLength = 44100 / 5;
	private static double alpha = 5.0;
	public static double midFreq = 0.0;
	public static double bassFreq = 640.0;
	
	// Calculated Variables
	private static double maxCyclesPerWindow = 0.0;
	private static int numWavelets = 0;
	
	// Special Variables
	private static double roundingFactor = 1000.0;
	
	// Function helpers
	private static double KaiserWindow[] = new double[MAXDFTWINDOW];
	private static double initialTaper = 1.0; // used by InitWavelets
	
	private static class Wavelet {
		double radianFreq;
		double freqInHz;
		double gain;
		int length;
		double sinArray[];
		double cosArray[];
	} 
	
	public static void printDFTParameters() {
		int freq = 0;
		for(double freqInHz = samplingRate / 2048.0; freqInHz <= samplingRate / 2.0; freqInHz *= Math.pow(2.0, 1.0 / (double) FDData.noteBase)) {
			if(freq % FDData.noteBase == 0) {
				System.out.print(freq);
				//printWaveletInfo(waveletInfo);
			}
			//index++;
		}
		System.out.print(numWavelets);
		//printWaveletInfo(WaveletInfoArrayList.get(numWavelets - 1));
		System.out.println("maxBinStep: " + maxBinStep);
		System.out.println("midFreq: " + midFreq);
		System.out.println("bassFreq: " + bassFreq);
		System.out.println("noteBase: " + FDData.noteBase);
		System.out.println("timeStepInMillis: " + FDData.timeStepInMillis);
	}
	
	// returns max time of file
	private static int LoadSamplesFromFile(String fileName) {
		double samplesPerStep = SynthTools.sampleRate / (1000.0 / FDData.timeStepInMillis);
		DataInputStream in = null;
	    try {
	    	in = new DataInputStream(new
	                BufferedInputStream(new FileInputStream(new String(fileName))));
		} catch (FileNotFoundException nf) {
			System.out.println("DFTEditor: " + fileName + ".[suffix] not found");
			return 0;
		}
		ArrayList<Float> ArrayListLeftRight = new ArrayList<Float>();
		try {
			in.skip(headerLengthInBytes);
			System.out.println(in.available());
			while(true) {
				int sample = in.readShort();
				sample = (short) (((sample & 0xFF00) >> 8) | ((sample & 0x00FF) << 8));
				ArrayListLeftRight.add((float) sample);
			}
		} catch (IOException e) {
			if(e instanceof EOFException) {
				System.out.println("DFT.LoadSamplesFromFile finished reading");
			} else {
				System.out.println("DFT.LoadSamplesFromFile error reading");
			}
		}
	    try {
	    	in.close();
		} catch (IOException nf) {
			System.out.println("DFTEditor: " + fileName + ".[suffix] not found");
			return 0;
		}	
		int maxTime = ArrayListLeftRight.size() / 2;
		LeftRight = new float[maxTime * 2];
		SynthTools.WAVDataLeft = new float[maxTime];	
		SynthTools.WAVDataRight = new float[maxTime];
		for(int index = 0; index < maxTime; index++) {
			LeftRight[index * 2] = ArrayListLeftRight.get(index * 2);
			LeftRight[index * 2 + 1] = ArrayListLeftRight.get(index * 2 + 1);
			SynthTools.WAVDataLeft[index] = LeftRight[index * 2];
			SynthTools.WAVDataRight[index] = LeftRight[index * 2 + 1];
		}
		return maxTime;
	}

	private static void SingleDFT(Wavelet wavelet, int centerIndex, int note, double[] left, double[] right, double samplesPerStep) {
		int maxIndex = wavelet.length;
		int startIndex = -maxIndex / 2;
		double leftVal = 0.0;
		double rightVal = 0.0;
		double sinValLeft = 0.0;
		double cosValLeft = 0.0;
		double sinValRight = 0.0;
		double cosValRight = 0.0;
		for(int index = 0; index < maxIndex; index++) {
			int readIndex = (startIndex + centerIndex + index);
			if(readIndex < 0) continue;
			if(readIndex >= left.length) break;
			leftVal = left[readIndex];
			rightVal = right[readIndex];
			sinValLeft += wavelet.sinArray[index] * leftVal;
			cosValLeft += wavelet.cosArray[index] * leftVal;
			sinValRight += wavelet.sinArray[index] * rightVal;
			cosValRight += wavelet.cosArray[index] * rightVal;
		}
		outputRoundedData(DFTEditor.amplitudesLeft, sinValLeft, cosValLeft, wavelet, note, centerIndex, samplesPerStep);
		outputRoundedData(DFTEditor.amplitudesRight, sinValRight, cosValRight, wavelet, note, centerIndex, samplesPerStep);
	}
	
	private static void outputRoundedData(float[][] matrix, double sinVal, double cosVal, Wavelet wavelet, int note, int centerIndex, double samplesPerStep) {
		int currentTime = (int) Math.round(centerIndex / samplesPerStep);
		int currentFreq = frequencyToNote(samplingRate / 2.0) - note;
		double logAmp = 0.0;
		//double roundedLogAmp = 0.0;
		double ampVal = sinVal * sinVal;
		ampVal += cosVal * cosVal;
		ampVal = Math.sqrt(ampVal) / wavelet.gain;
		ampVal *= 2.0; // integral of sin, cos over time approaches 0.5
		if(ampVal > 2.0) {
			logAmp = Math.log(ampVal) / Math.log(2.0);
		} else {
			logAmp = 0.0;
		}
		matrix[currentTime][currentFreq] = (float) logAmp;
	}
	
	private static double noteToFrequency(int note) {
		return Math.pow(2.0, note / (double) FDData.noteBase);
	}
	
	private static int frequencyToNote(double frequency) {
		return (int) Math.round(Math.log(frequency) / Math.log(2.0) * (double) FDData.noteBase);
	}
	
	private static void printWaveletInfo(Wavelet wavelet) {
		double radFreq = wavelet.radianFreq;
		//System.out.print("radFreq: " + radFreq + " ");
		double freqInHz = samplingRate / twoPI * radFreq;
		double length = wavelet.length;
		double samplesPerCycle = samplingRate / freqInHz;
		double bins = length / samplesPerCycle;
		System.out.print(" | HZ: " + (float) freqInHz);
		System.out.print(" | BINS: " + (float) bins);
		//System.out.print("gain: " + wavelet.gain + " ");
		System.out.print(" | LENGTH: " + (float) wavelet.length); // pass as int
		//System.out.print("*sin[]: %x ", wavelet.sinArray);
		//System.out.print("*cos[]: %x ", wavelet.cosArray);
		System.out.print("\n");
	}
	
	
	public static Wavelet createWavelet(double freqInHz, double bins) {
		Wavelet wavelet = new Wavelet();
		wavelet.freqInHz = freqInHz;
	   	double samplesPerCycle = samplingRate / freqInHz;
		wavelet.radianFreq = twoPI / samplesPerCycle;
		wavelet.gain = 0.0;
		wavelet.length = (int) Math.round(bins * samplesPerCycle);
		if(wavelet.length > maxWindowLength) wavelet.length = (int) Math.round(maxWindowLength);
		wavelet.sinArray = new double[wavelet.length];
		wavelet.cosArray = new double[wavelet.length];
		double KaiserWindow[] = new double[wavelet.length];
		Filter.CreateWindow(KaiserWindow, wavelet.length, alpha);
		for(int index = 0; index < wavelet.length; index++) {
			wavelet.gain += KaiserWindow[index];
			wavelet.sinArray[index] = Math.sin(index * wavelet.radianFreq) * KaiserWindow[index];
			wavelet.cosArray[index] = Math.cos(index * wavelet.radianFreq) * KaiserWindow[index];
		}
		return wavelet;
	}
	
	static void DFTWithDecimate(int maxCenterIndex) {
		double left[] = new double[LeftRight.length / 2];
		double right[] = new double[LeftRight.length / 2];
		for(int index = 0; index < LeftRight.length / 2; index++) {
			left[index] = (double) LeftRight[index * 2];
			right[index] = (double) LeftRight[index * 2 + 1];
		}
		double noteRatio = Math.pow(2.0, -1.0 / (double) FDData.noteBase);
		double samplesPerStep = SynthTools.sampleRate / (1000.0 / FDData.timeStepInMillis);
		double bins = maxBinStep / (Math.pow(2.0, 1.0 / (double) FDData.noteBase) - 1.0);
		// 22050 -> 5512.5
		int note = frequencyToNote(samplingRate / 2.0);
		for(double freqInHz = samplingRate / 2.0; Math.round(freqInHz * 10000.0) / 10000.0 > samplingRate / 4.0; freqInHz *= noteRatio) {
			Wavelet currentWavelet = createWavelet(freqInHz, bins);
			for(double centerIndex = 0; centerIndex < maxCenterIndex; centerIndex += samplesPerStep) {
				if(note == frequencyToNote(samplingRate / 2048.0)) return;
				SingleDFT(currentWavelet, (int) Math.round(centerIndex), note, left, right, samplesPerStep);
			}
			note--;
		}
		for(double freqInHz = samplingRate / 4.0; Math.round(freqInHz * 10000.0) / 10000.0 > samplingRate / 8.0; freqInHz *= noteRatio) {
			Wavelet currentWavelet = createWavelet(freqInHz * 2, bins);
			double[] tempLeft = Filter.filterAndMultiply(freqInHz, left);
			double[] tempRight = Filter.filterAndMultiply(freqInHz, right);
			for(double centerIndex = 0; centerIndex < maxCenterIndex; centerIndex += samplesPerStep) {
				if(note == frequencyToNote(samplingRate / 2048.0)) return;
				SingleDFT(currentWavelet, (int) Math.round(centerIndex), note, tempLeft, tempRight, samplesPerStep);
			}
			note--;
		}
		for(double freqInHz = samplingRate / 8.0; Math.round(freqInHz * 10000.0) / 10000.0 > samplingRate / 16.0; freqInHz *= noteRatio) {
			Wavelet currentWavelet = createWavelet(freqInHz * 4, bins);
			double[] tempLeft = Filter.filterAndMultiply(freqInHz, left);
			double[] tempRight = Filter.filterAndMultiply(freqInHz, right);
			tempLeft = Filter.filterAndMultiply(freqInHz * 2, tempLeft);
			tempRight = Filter.filterAndMultiply(freqInHz * 2, tempRight);
			for(double centerIndex = 0; centerIndex < maxCenterIndex; centerIndex += samplesPerStep) {
				if(note == frequencyToNote(samplingRate / 2048.0)) return;
				SingleDFT(currentWavelet, (int) Math.round(centerIndex), note, tempLeft, tempRight, samplesPerStep);
			}
			note--;
		}
		while(true) {
			for(double freqInHz = samplingRate / 16.0; Math.round(freqInHz * 10000.0) / 10000.0 > samplingRate / 32.0; freqInHz *= noteRatio) {
				Wavelet currentWavelet = createWavelet(freqInHz * 8, bins);
				double[] tempLeft = Filter.filterAndMultiply(freqInHz, left);
				double[] tempRight = Filter.filterAndMultiply(freqInHz, right);
				tempLeft = Filter.filterAndMultiply(freqInHz * 2, tempLeft);
				tempRight = Filter.filterAndMultiply(freqInHz * 2, tempRight);
				tempLeft = Filter.filterAndMultiply(freqInHz * 4, tempLeft);
				tempRight = Filter.filterAndMultiply(freqInHz * 4, tempRight);
				for(double centerIndex = 0; centerIndex < maxCenterIndex; centerIndex += samplesPerStep) {
					if(note == frequencyToNote(samplingRate / 2048.0)) return;
					SingleDFT(currentWavelet, (int) Math.round(centerIndex), note, tempLeft, tempRight, samplesPerStep);
				}
				note--;
			}
			left = Filter.decimate(left);
			right = Filter.decimate(right);
			maxCenterIndex /= 2;
			samplesPerStep /= 2;
			System.out.println("decimate");
		}
	}
	
	static void FileDFTMatrix(String fileName) {
		Filter.testFilters();
		double samplesPerStep = SynthTools.sampleRate / (1000.0 / FDData.timeStepInMillis);
		int maxCenterIndex = LoadSamplesFromFile(fileName);
		int maxTime = (int) Math.floor(maxCenterIndex / samplesPerStep);
		if(maxCenterIndex > MAXSAMPLES) {
			System.out.println("File Too Large: Truncating");
			maxCenterIndex = MAXSAMPLES;
		}
		int numFreqs = frequencyToNote(samplingRate / 2.0) - frequencyToNote(samplingRate / 2048.0) + 1;
		DFTEditor.amplitudesLeft = new float[maxTime + 1][numFreqs + 1];
		DFTEditor.amplitudesRight = new float[maxTime + 1][numFreqs + 1];
		for(int time = 0; time <= maxTime; time++) {
			for(int freq = 0; freq <= numFreqs; freq++) {
				DFTEditor.amplitudesLeft[time][freq] = 0.0f;
				DFTEditor.amplitudesRight[time][freq] = 0.0f;
			}	
		}
		DFTWithDecimate(maxCenterIndex);
		/*
		for(double freqInHz = minFreqHz; freqInHz <= maxFreqHz; freqInHz *= Math.pow(2.0, 1.0 / (double) FDData.noteBase)) {
			double samplesPerCycle = samplingRate / freqInHz;
			double bins = maxWaveletLength / samplesPerCycle;
			if(bins > maxBins) bins = maxBins;
			Wavelet currentWavelet = createWavelet(freqInHz, bins);
			for(double centerIndex = 0; centerIndex < maxCenterIndex; centerIndex += samplesPerStep) {
				int waveletIndex = 0;
				SingleDFT(currentWavelet, (int) Math.round(centerIndex));
			}
			finalFreqHz = freqInHz;
		}
		*/
		DFTEditor.maxTime = maxTime;
		DFTEditor.maxScreenNote = frequencyToNote(samplingRate / 2.0);
		DFTEditor.minScreenNote = frequencyToNote(samplingRate / 2048.0);
		DFTEditor.maxScreenFreq = numFreqs;
	}
	
}
