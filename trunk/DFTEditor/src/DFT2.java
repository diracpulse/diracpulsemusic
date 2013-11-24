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
import java.util.TreeSet;

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
	public static double maxBinStep = 1.0;
	//private static double maxFreqHz = samplingRate / 2.0;
	//private static double minFreqHz = 20.0;
	private static double maxWindowLength = 44100 / 5;
	private static double alpha = 1.0;
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
	    // Test white noise
	    /*
	    ArrayListLeftRight = new ArrayList<Float>();
	    for(int time = 0; time < 44100 * 2; time++) {
	    	ArrayListLeftRight.add((float) ((Math.pow(Math.random(), 1.0 / 1.0) - 0.5) * 65534.0));
	    	ArrayListLeftRight.add((float) ((Math.pow(Math.random(), 1.0 / 1.0) - 0.5) * 65534.0));
	    }
	    */
		int maxTime = ArrayListLeftRight.size() / 2;
		LeftRight = new float[maxTime * 2];
		SynthTools.WAVDataLeft = new double[maxTime];	
		SynthTools.WAVDataRight = new double[maxTime];
		for(int index = 0; index < maxTime; index++) {
			LeftRight[index * 2] = ArrayListLeftRight.get(index * 2);
			LeftRight[index * 2 + 1] = ArrayListLeftRight.get(index * 2 + 1);
			SynthTools.WAVDataLeft[index] = LeftRight[index * 2];
			SynthTools.WAVDataRight[index] = LeftRight[index * 2 + 1];
		}
		return maxTime;
	}
	
	// Returns amplitude NOT log(amplitude)
	private static double singleDFTTest(Wavelet wavelet, double[] mono) {
		double sinVal = 0.0;
		double cosVal = 0.0;
		for(int index = 0; index < wavelet.length; index++) {
			double val = mono[index];
			sinVal += wavelet.sinArray[index] * val;
			cosVal += wavelet.cosArray[index] * val;
		}
		double ampVal = sinVal * sinVal;
		ampVal += cosVal * cosVal;
		ampVal = Math.sqrt(ampVal) / wavelet.gain;
		ampVal *= 2.0; // integral of sin, cos over time approaches 0.5
		return ampVal;
	}

	static void disentangleWithMatrix() {
		// create responseOfWaveletToNote
		int xDimension = FDData.noteBase + 2;
		int yDimension = FDData.noteBase + 1;
		double[][] responseOfWaveletToNote = new double[xDimension][yDimension];
		double noteRatio = Math.pow(2.0, -1.0 / (double) FDData.noteBase);
		double maxFreqHz = samplingRate / 16.0;
		double minFreqHz = (samplingRate / 32.0);
		for(double freqInHz = maxFreqHz ; Math.round(freqInHz * 10000.0) / 10000.0 >= minFreqHz; freqInHz *= noteRatio) {
			double samplesPerCycle = samplingRate / freqInHz;
			double noteBase = FDData.noteBase; Math.sqrt(freqInHz / (samplingRate / 8.0));
			if(noteBase > FDData.noteBase) noteBase = FDData.noteBase;
			double bins = maxBinStep / (Math.pow(2.0, 1.0 / (double) noteBase) - 1.0);
			double currentPhase = 0.0;
			double deltaPhase = (freqInHz / SynthTools.sampleRate) * SynthTools.twoPI;
			double[] mono = new double[(int) Math.ceil(bins * samplesPerCycle * 2.0)]; // MAXWINDOWLENGTH eventually
			for(int index = 0; index < mono.length; index++) {
				mono[index] = Math.sin(currentPhase);
				currentPhase += deltaPhase;
			}
			for(double innerFreqInHz = maxFreqHz; Math.round(innerFreqInHz * 10000.0) / 10000.0 >= minFreqHz; innerFreqInHz *= noteRatio) {
				double innerNoteBase = FDData.noteBase * Math.sqrt(innerFreqInHz / (samplingRate / 8.0));
				if(innerNoteBase > FDData.noteBase) innerNoteBase = FDData.noteBase;
				bins = maxBinStep / (Math.pow(2.0, 1.0 / (double) innerNoteBase) - 1.0);
				Wavelet currentWavelet = createWavelet(innerFreqInHz, bins);
				int xIndex = frequencyToNote(maxFreqHz) - frequencyToNote(innerFreqInHz);
				int yIndex = frequencyToNote(maxFreqHz) - frequencyToNote(freqInHz);
				responseOfWaveletToNote[xIndex][yIndex] = singleDFTTest(currentWavelet, mono);
			}
		}
		// create test signal
		double maxSamplesPerCycle = samplingRate / minFreqHz;
		double bins = maxBinStep / (Math.pow(2.0, 1.0 / (double) FDData.noteBase) - 1.0);
		double[] mono = new double[(int) Math.ceil(bins * maxSamplesPerCycle * 2.0)]; // MAXWINDOWLENGTH eventually
		double[] testAmplitudes = new double[FDData.noteBase + 1];
		for(int index = 0; index < testAmplitudes.length; index++) testAmplitudes[index] = Math.random();
		for(double freqInHz = maxFreqHz ; Math.round(freqInHz * 10000.0) / 10000.0 >= minFreqHz; freqInHz *= noteRatio) {
			double currentPhase = Math.random();
			double deltaPhase = (freqInHz / SynthTools.sampleRate) * SynthTools.twoPI;
			int noteIndex = frequencyToNote(maxFreqHz) - frequencyToNote(freqInHz);
			if(noteIndex % 1 != 0) {
				testAmplitudes[noteIndex] = 0.0;
				continue;
			}
			for(int index = 0; index < mono.length; index++) {
				mono[index] += Math.sin(currentPhase) * testAmplitudes[noteIndex];
				currentPhase += deltaPhase;
			}
		}
		// analyse test signal
		for(double freqInHz = maxFreqHz ; Math.round(freqInHz * 10000.0) / 10000.0 >= minFreqHz; freqInHz *= noteRatio) {
			double innerNoteBase = FDData.noteBase * Math.sqrt(freqInHz / (samplingRate / 8.0));
			if(innerNoteBase > FDData.noteBase) innerNoteBase = FDData.noteBase;
			bins = maxBinStep / (Math.pow(2.0, 1.0 / (double) innerNoteBase) - 1.0);
			Wavelet currentWavelet = createWavelet(freqInHz, bins);
			int index = frequencyToNote(maxFreqHz) - frequencyToNote(freqInHz);
			responseOfWaveletToNote[xDimension - 1][index] = singleDFTTest(currentWavelet, mono);
		}
		// print system to solve
		for(double freqInHz = maxFreqHz ; Math.round(freqInHz * 10000.0) / 10000.0 >= minFreqHz; freqInHz *= noteRatio) {
			System.out.print("[");
			int yIndex = frequencyToNote(maxFreqHz) - frequencyToNote(freqInHz);
			for(double innerFreqInHz = maxFreqHz; Math.round(innerFreqInHz * 10000.0) / 10000.0 >= minFreqHz; innerFreqInHz *= noteRatio) {
				int xIndex = frequencyToNote(maxFreqHz) - frequencyToNote(innerFreqInHz);
				System.out.printf("%.3f", responseOfWaveletToNote[xIndex][yIndex]);
				System.out.print(",");
			}
			System.out.printf("%.6f] = ", responseOfWaveletToNote[xDimension - 1][yIndex]);
			System.out.printf("%.6f", testAmplitudes[yIndex]);
		}
		double[][] A = responseOfWaveletToNote;
		for(int x = 0; x < yDimension; x++) {
			double divisor = A[x][x];
			for(int x2 = x; x2 < xDimension; x2++) A[x2][x] /= divisor;
			for(int y = 0; y < yDimension; y++) {
				if(y == x) continue;
				double multiplier = A[x][y];
				for(int x2 = x; x2 < xDimension; x2++) {
					A[x2][y] -= A[x2][x] * multiplier;
				}
			}
			System.out.println(x);
		}
		responseOfWaveletToNote = A;
		for(double freqInHz = maxFreqHz ; Math.round(freqInHz * 10000.0) / 10000.0 >= minFreqHz; freqInHz *= noteRatio) {
			System.out.print("[");
			int yIndex = frequencyToNote(maxFreqHz) - frequencyToNote(freqInHz);
			for(double innerFreqInHz = maxFreqHz; Math.round(innerFreqInHz * 10000.0) / 10000.0 >= minFreqHz; innerFreqInHz *= noteRatio) {
				int xIndex = frequencyToNote(maxFreqHz) - frequencyToNote(innerFreqInHz);
				System.out.printf("%.3f", responseOfWaveletToNote[xIndex][yIndex]);
				System.out.print(",");
			}
			System.out.printf("%.10f] = ", responseOfWaveletToNote[xDimension - 1][yIndex]);
			System.out.printf("%.10f", testAmplitudes[yIndex]);
		}
		System.out.println();
		System.out.println();
	}

	private static void SingleDFT(Wavelet wavelet, int centerIndex, int note, double[] left, double[] right, double samplesPerStep, double gain) {
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
		outputRoundedData(DFTEditor.amplitudesLeft, sinValLeft, cosValLeft, wavelet, note, centerIndex, samplesPerStep, gain);
		outputRoundedData(DFTEditor.amplitudesRight, sinValRight, cosValRight, wavelet, note, centerIndex, samplesPerStep, gain);
	}
	
	private static void outputRoundedData(float[][] matrix, double sinVal, double cosVal, Wavelet wavelet, int note, int centerIndex, double samplesPerStep, double gain) {
		int currentTime = (int) Math.round(centerIndex / samplesPerStep);
		int currentFreq = frequencyToNote(samplingRate / 2.0) - note;
		double logAmp = 0.0;
		//double roundedLogAmp = 0.0;
		double ampVal = sinVal * sinVal;
		ampVal += cosVal * cosVal;
		ampVal = Math.sqrt(ampVal) / wavelet.gain;
		ampVal *= 2.0 * gain; // integral of sin, cos over time approaches 0.5
		if(ampVal > 2.0) {
			logAmp = Math.log(ampVal) / Math.log(2.0);
		} else {
			logAmp = 0.0;
		}
		matrix[currentTime][currentFreq] = (float) logAmp;
	}
	
	public static double noteToFrequency(int note) {
		return Math.pow(2.0, note / (double) FDData.noteBase);
	}
	
	public static int frequencyToNote(double frequency) {
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
		double maxSamplesPerStep = samplesPerStep;
		double bins = maxBinStep / (Math.pow(2.0, 1.0 / (double) FDData.noteBase) - 1.0);
		// 22050 -> 5512.5
		double upperClipRatio = 1.0;
		double gain = 1.0;
		int note = frequencyToNote(samplingRate / 2.0 * upperClipRatio);
		for(double freqInHz = samplingRate / 2.0 * upperClipRatio; Math.round(freqInHz * 10000.0) / 10000.0 > samplingRate / 8.0 * upperClipRatio; freqInHz *= noteRatio) {
			Wavelet currentWavelet = createWavelet(freqInHz, bins);
			for(double centerIndex = 0; centerIndex < maxCenterIndex; centerIndex += samplesPerStep) {
				if(note == frequencyToNote(samplingRate / 2048.0)) return;
				SingleDFT(currentWavelet, (int) Math.round(centerIndex), note, left, right, samplesPerStep, gain);
			}
			note--;
		}
		while(true) {
			for(double freqInHz = samplingRate / 8.0  * upperClipRatio; Math.round(freqInHz * 10000.0) / 10000.0 > samplingRate / 16.0  * upperClipRatio; freqInHz *= noteRatio) {
				if(note < frequencyToNote(samplingRate / 8.0)) {
					double innerNoteBase = FDData.noteBase * Math.sqrt(noteToFrequency(note) / (samplingRate / 8.0));
					bins = maxBinStep / (Math.pow(2.0, 1.0 / (double) innerNoteBase) - 1.0);
					//System.out.println(noteToFrequency(note) + " " + innerNoteBase);
				}
				Wavelet currentWavelet = createWavelet(freqInHz, bins);
				for(double centerIndex = 0; centerIndex < maxCenterIndex; centerIndex += samplesPerStep) {
					if(note == frequencyToNote(samplingRate / 2048.0)) return;
					SingleDFT(currentWavelet, (int) Math.round(centerIndex), note, left, right, samplesPerStep, gain);
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
	
	static void DFTWithDecimateAndMultiply(int maxCenterIndex) {
		double left[] = new double[LeftRight.length / 2];
		double right[] = new double[LeftRight.length / 2];
		for(int index = 0; index < LeftRight.length / 2; index++) {
			left[index] = (double) LeftRight[index * 2];
			right[index] = (double) LeftRight[index * 2 + 1];
		}
		double noteRatio = Math.pow(2.0, -1.0 / (double) FDData.noteBase);
		double samplesPerStep = SynthTools.sampleRate / (1000.0 / FDData.timeStepInMillis);
		double bins = maxBinStep / (Math.pow(2.0, 1.0 / (double) FDData.noteBase) - 1.0);
		double upperClipRatio = 1.0;
		double gain = 1.0;
		int note = frequencyToNote(samplingRate / 2.0 * upperClipRatio);
		for(double freqInHz = samplingRate / 2.0 * upperClipRatio; Math.round(freqInHz * 10000.0) / 10000.0 > samplingRate / 8.0 * upperClipRatio; freqInHz *= noteRatio) {
			Wavelet currentWavelet = createWavelet(freqInHz, bins);
			for(double centerIndex = 0; centerIndex < maxCenterIndex; centerIndex += samplesPerStep) {
				if(note == frequencyToNote(samplingRate / 2048.0)) return;
				SingleDFT(currentWavelet, (int) Math.round(centerIndex), note, left, right, samplesPerStep, gain);
			}
			note--;
		}
		for(double freqInHz = samplingRate / 8.0 * upperClipRatio; Math.round(freqInHz * 10000.0) / 10000.0 > samplingRate / 16.0 * upperClipRatio; freqInHz *= noteRatio) {
			Wavelet currentWavelet = createWavelet(freqInHz * 2, bins);
			double[] tempLeft = Filter.filterAndMultiply(freqInHz, left);
			double[] tempRight = Filter.filterAndMultiply(freqInHz, right);			
			for(double centerIndex = 0; centerIndex < maxCenterIndex; centerIndex += samplesPerStep) {
				if(note == frequencyToNote(samplingRate / 2048.0)) return;
				SingleDFT(currentWavelet, (int) Math.round(centerIndex), note, tempLeft, tempRight, samplesPerStep, gain);
			}
			note--;
			//gain = freqInHz / (samplingRate / 4.0);
		}
		//gain = 1.0 / 4.0;
		for(double freqInHz = samplingRate / 16.0  * upperClipRatio; Math.round(freqInHz * 10000.0) / 10000.0 > samplingRate / 32.0  * upperClipRatio; freqInHz *= noteRatio) {
			Wavelet currentWavelet = createWavelet(freqInHz * 4, bins);
			double[] tempLeft = Filter.filterAndMultiply(freqInHz, left);
			double[] tempRight = Filter.filterAndMultiply(freqInHz, right);
			tempLeft = Filter.filterAndMultiply(freqInHz * 2, tempLeft);
			tempRight = Filter.filterAndMultiply(freqInHz * 2, tempRight);
			for(double centerIndex = 0; centerIndex < maxCenterIndex; centerIndex += samplesPerStep) {
				if(note == frequencyToNote(samplingRate / 2048.0)) return;
				SingleDFT(currentWavelet, (int) Math.round(centerIndex), note, tempLeft, tempRight, samplesPerStep, gain);
			}
			note--;
			//gain = freqInHz / (samplingRate / 16.0);
		}
		//gain = 1.0 / 8.0;
		while(true) {
			for(double freqInHz = samplingRate / 32.0  * upperClipRatio; Math.round(freqInHz * 10000.0) / 10000.0 > samplingRate / 64.0  * upperClipRatio; freqInHz *= noteRatio) {
				Wavelet currentWavelet = createWavelet(freqInHz * 8, bins);
				double[] tempLeft = Filter.filterAndMultiply(freqInHz, left);
				double[] tempRight = Filter.filterAndMultiply(freqInHz, right);
				tempLeft = Filter.filterAndMultiply(freqInHz * 2, tempLeft);
				tempRight = Filter.filterAndMultiply(freqInHz * 2, tempRight);
				tempLeft = Filter.filterAndMultiply(freqInHz * 4, tempLeft);
				tempRight = Filter.filterAndMultiply(freqInHz * 4, tempRight);
				for(double centerIndex = 0; centerIndex < maxCenterIndex; centerIndex += samplesPerStep) {
					if(note == frequencyToNote(samplingRate / 2048.0)) return;
					SingleDFT(currentWavelet, (int) Math.round(centerIndex), note, tempLeft, tempRight, samplesPerStep, gain);
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
		//disentangleWithMatrix();
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
