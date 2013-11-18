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


public class DFT {
	
private static int MAXDFTWINDOW = 44100;
private static int MAXWAVELETS = 62 * 12;
private static double LeftRight[] = null;
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

private static class WaveletInfo {
	double radianFreq;
	double gain;
	int length;
	int note;
	float sinArray[];//[MAXDFTWINDOW];
	float cosArray[];//[MAXDFTWINDOW];
} 

private static ArrayList<WaveletInfo> WaveletInfoArrayList = new ArrayList<WaveletInfo>();

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
	LeftRight = new double[maxTime * 2];
	SynthTools.WAVDataLeft = new double[maxTime];	
	SynthTools.WAVDataRight = new double[maxTime];
	for(int index = 0; index < maxTime; index++) {
		LeftRight[index * 2] = ArrayListLeftRight.get(index * 2);
		LeftRight[index * 2 + 1] = ArrayListLeftRight.get(index * 2 + 1);
		SynthTools.WAVDataLeft[index] = LeftRight[index * 2];
		SynthTools.WAVDataRight[index] = LeftRight[index * 2 + 1];
	}
	int maxNote = WaveletInfoArrayList.get(0).note;
	int minNote = WaveletInfoArrayList.get(WaveletInfoArrayList.size() - 1).note;
	if(minNote > maxNote) {
		System.out.println("DFT.LoadSamplesFromFile error minNote > maxNote");
	}
	int numNotes = maxNote - minNote;
	int numTimes = (int) Math.round(LeftRight.length / samplesPerStep / 2);
	DFTEditor.amplitudesLeft = new float[numTimes + 1][numNotes + 1];
	DFTEditor.amplitudesRight = new float[numTimes + 1][numNotes + 1];
	for(int time = 0; time <= numTimes; time++) {
		for(int freq = 0; freq <= numNotes; freq++) {
			DFTEditor.amplitudesLeft[time][freq] = -1.0f;
			DFTEditor.amplitudesRight[time][freq] = -1.0f;
		}	
	}
	DFTEditor.maxTime = numTimes;
	DFTEditor.maxScreenNote = maxNote;
	DFTEditor.minScreenNote = minNote;
	DFTEditor.maxScreenFreq = numNotes;
	return maxTime;
}

private static void SingleDFT(int waveletIndex, int centerIndex) {
	//if(debug) System.out.print("SingleDFT\n");
	if(waveletIndex == 0) return;
	int maxIndex = WaveletInfoArrayList.get(waveletIndex).length;
	int startIndex = -maxIndex / 2;
	double leftVal = 0.0;
	double rightVal = 0.0;
	double sinValLeft = 0.0;
	double cosValLeft = 0.0;
	double sinValRight = 0.0;
	double cosValRight = 0.0;
	for(int index = 0; index < maxIndex; index++) {
		int readIndexLeft = (startIndex + centerIndex + index) * 2;
		int readIndexRight = (startIndex + centerIndex + index) * 2 + 1;
		if(readIndexLeft < 0) continue;
		if(readIndexRight >= LeftRight.length) break;
		leftVal = (double) LeftRight[readIndexLeft];
		rightVal = (double) LeftRight[readIndexRight];
		sinValLeft += WaveletInfoArrayList.get(waveletIndex).sinArray[index] * leftVal;
		cosValLeft += WaveletInfoArrayList.get(waveletIndex).cosArray[index] * leftVal;
		sinValRight += WaveletInfoArrayList.get(waveletIndex).sinArray[index] * rightVal;
		cosValRight += WaveletInfoArrayList.get(waveletIndex).cosArray[index] * rightVal;
	}
	outputRoundedData(DFTEditor.amplitudesLeft, sinValLeft, cosValLeft, waveletIndex, centerIndex);
	outputRoundedData(DFTEditor.amplitudesRight, sinValRight, cosValRight, waveletIndex, centerIndex);
}

private static void outputRoundedData(float[][] matrix, double sinVal, double cosVal, int waveletIndex, int centerIndex) {
	double samplesPerStep = SynthTools.sampleRate / (1000.0 / FDData.timeStepInMillis);
	int currentTime = 	(int) Math.round(centerIndex / samplesPerStep);
	int currentFreq = WaveletInfoArrayList.get(0).note - WaveletInfoArrayList.get(waveletIndex).note;
	double logAmp = 0.0;
	//double roundedLogAmp = 0.0;
	double ampVal = sinVal * sinVal;
	ampVal += cosVal * cosVal;
	ampVal = Math.sqrt(ampVal) / WaveletInfoArrayList.get(waveletIndex).gain;
	ampVal *= 2.0; // integral of sin, cos over time approaches 0.5
	if(ampVal > 2.0) {
		logAmp = Math.log(ampVal) / Math.log(2.0);
	} else {
		logAmp = 0.0;
	}
	/*
	double length = WaveletInfoArrayList.get(waveletIndex).length / samplesPerStep;
	int startTime = (int) Math.round(currentTime - length / 2.0);
	int endTime = (int) Math.round(currentTime + length / 2.0);
	if(startTime < 0) return;
	if(endTime >= matrix.length) endTime = matrix.length - 1;
	if(matrix[startTime][currentFreq] != -1.0f) return;
	for(int time = startTime; time <= endTime; time++) {
		matrix[time][currentFreq] = (float) logAmp;
	}
	*/
	matrix[currentTime][currentFreq] = (float) logAmp;
}

private static double noteToFrequency(int note) {
	return Math.pow(2.0, note / (double) FDData.noteBase);
}

private static int frequencyToNote(double frequency) {
	return (int) Math.round(Math.log(frequency) / Math.log(2.0) * (double) FDData.noteBase);
}

public static void InitWavelets() {
	numWavelets = 0;
	maxCyclesPerWindow = 0;
	initialTaper = 1.0;
	WaveletInfoArrayList = new ArrayList<WaveletInfo>();
	int index = 0;
	// UNRESOLVED ISSUE: bin step = 2.0 and maxFreqHz = 20000.0, causes noise at 20000.0
	// does not occur at frequency above, or two below
	double maxFreqHz = 19160.0;
	double minFreqHz = 20.0;
	//if(debug) System.out.print("InitWavelets\n");
	maxDFTLength = 0;
	maxCyclesPerWindow = maxBinStep / (Math.pow(2.0, 1.0 / (double) FDData.noteBase) - 1.0);
	if(bassFreq < midFreq) {
		index = InitWaveletsHelper(maxFreqHz, midFreq, index, 1.0);
		index = InitWaveletsHelper(midFreq, bassFreq, index, Math.sqrt(2.0));
	} else {
		index = InitWaveletsHelper(maxFreqHz, bassFreq, index, 1.0);
	}
	index = InitWaveletsHelper(bassFreq, minFreqHz, index, 2.0);
	//index = InitWaveletsHelper(centerFreq, 20.0, index, 2.0);
    numWavelets = index;
    // MATRIX OUTPUT
    int minNote = frequencyToNote(maxFreqHz) - 1; // #HACK skip first wavelet
    int maxNote = frequencyToNote(minFreqHz) + 1; // stops before last note
    //System.out.print("%d\n", numWavelets, maxDFTLength);
    CalculateWavelets();
}

// Creates Wavelets starting at upperNote and ending at (lowerNote - 1)
// Returns index for NEXT wavelet
private static int InitWaveletsHelper(double upperFreqHz, double stopFreqHz, int index, double taperPerOctave) {
	int note = 0;
	int upperNote = frequencyToNote(upperFreqHz);
	int stopNote = frequencyToNote(stopFreqHz);
    double startLogFreq = Math.log(noteToFrequency(upperNote)) / Math.log(2.0);
    double taperValue = initialTaper;
    for(note = upperNote; note > stopNote; note--) {
    	WaveletInfoArrayList.add(new WaveletInfo());
    	double freqInHz = noteToFrequency(note);
    	double samplesPerCycle = samplingRate / freqInHz;
    	double radianFreq = twoPI / samplesPerCycle;
    	double currentLogFreq =  Math.log(freqInHz) / Math.log(2.0);
    	taperValue = initialTaper * Math.pow(taperPerOctave, startLogFreq - currentLogFreq);
    	double cyclesPerWindow = maxCyclesPerWindow / taperValue;
    	int windowLength = (int) Math.round(cyclesPerWindow * samplesPerCycle);
    	//IMPORTANT: the next 3 lines set cycles per window to an integer (doesn't improve anything)
    	//samplesPerCycle = round(windowLength / cyclesPerWindow);
    	//windowLength = cyclesPerWindow * samplesPerCycle;
    	//radianFreq = twoPI / samplesPerCycle;
    	WaveletInfoArrayList.get(index).radianFreq = radianFreq;
    	WaveletInfoArrayList.get(index).length = windowLength;
    	WaveletInfoArrayList.get(index).note = note;
    	index++;
    }
    initialTaper = taperValue;
    //System.out.print("taperValue: %f ", taperValue);
    return index;
}

private static int InitWaveletsHelperConstant(double upperFreqHz, double stopFreqHz, int index) {
	int note = frequencyToNote(upperFreqHz);
	double freqInHz = noteToFrequency(note);
	double samplesPerCycle = samplingRate / freqInHz;
	int windowLength = (int) Math.round(maxCyclesPerWindow * samplesPerCycle);
	int stopNote = frequencyToNote(stopFreqHz);
	double cycles =  maxCyclesPerWindow;
    while(true) {
    	samplesPerCycle = windowLength / cycles;
    	double radianFreq = twoPI / samplesPerCycle;
    	note = frequencyToNote(samplingRate / samplesPerCycle);
    	if(note < stopNote) break;
    	WaveletInfoArrayList.add(new WaveletInfo());
    	WaveletInfoArrayList.get(index).radianFreq = radianFreq;
    	WaveletInfoArrayList.get(index).length = windowLength;
    	WaveletInfoArrayList.get(index).note = note;
    	cycles -= maxBinStep;
    	index++;
    }
    //System.out.print("taperValue: %f ", taperValue);
    return index;
}

private static void printWaveletInfo(WaveletInfo wavelet) {
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

private static void CalculateWavelets() {
	for(int waveletIndex = 0; waveletIndex < numWavelets; waveletIndex++) {
		double gain = 0.0;
		int length = WaveletInfoArrayList.get(waveletIndex).length;
		double radianFreq = WaveletInfoArrayList.get(waveletIndex).radianFreq;
		//System.out.print("malloc %d %d %d\n", waveletIndex, numWavelets, length);
		WaveletInfoArrayList.get(waveletIndex).sinArray = new float[length];
		WaveletInfoArrayList.get(waveletIndex).cosArray = new float[length];
		Filter.CreateWindow(KaiserWindow, length, alpha);
		for(int index = 0; index < length; index++) {
			double dIndex = (double) index;
			gain += KaiserWindow[index];
			WaveletInfoArrayList.get(waveletIndex).sinArray[index] = (float) (Math.sin(dIndex * radianFreq) * KaiserWindow[index]);
			WaveletInfoArrayList.get(waveletIndex).cosArray[index] = (float) (Math.cos(dIndex * radianFreq) * KaiserWindow[index]);
		}
		WaveletInfoArrayList.get(waveletIndex).gain = gain;
		//printWaveletInfo(WaveletInfoArrayList.get(waveletIndex), waveletIndex);
	}
}

static void fillMatrix() {
	int numTimes = DFTEditor.amplitudesLeft.length;
	int numFreqs = DFTEditor.amplitudesLeft[0].length;
	for(int time = 0; time < numTimes; time++) {
		for(int freq = 1; freq < numFreqs - 1; freq++) {
			if(DFTEditor.amplitudesLeft[time][freq] != -1.0f) continue;
			int startFreq = freq - 1;
			int endFreq = freq;
			for(endFreq = freq; endFreq < numFreqs; endFreq++) {
				if(DFTEditor.amplitudesLeft[time][endFreq] != -1.0f) break;
			}
			if(endFreq == numFreqs) continue;
			float startAmp = DFTEditor.amplitudesLeft[time][startFreq];
			float endAmp = DFTEditor.amplitudesLeft[time][endFreq];
			double slope = (startAmp - endAmp) / (startFreq - endFreq);
			if(slope == 0.0f) {
				if(Math.random() < 0.5) {
					slope = -0.00001f;
				} else {
					slope = 0.00001f;
				}
			}
			for(int innerFreq = startFreq + 1; innerFreq < endFreq; innerFreq++) {
				float ampValue = (float) (startAmp + (innerFreq - startFreq) * slope);
				DFTEditor.amplitudesLeft[time][innerFreq] = ampValue;
			}
		}
	}
	numTimes = DFTEditor.amplitudesRight.length;
	numFreqs = DFTEditor.amplitudesRight[0].length;
	for(int time = 0; time < numTimes; time++) {
		for(int freq = 1; freq < numFreqs - 1; freq++) {
			if(DFTEditor.amplitudesRight[time][freq] != -1.0f) continue;
			int startFreq = freq - 1;
			int endFreq = freq;
			for(endFreq = freq; endFreq < numFreqs; endFreq++) {
				if(DFTEditor.amplitudesRight[time][endFreq] != -1.0f) break;
			}
			if(endFreq == numFreqs) continue;
			float startAmp = DFTEditor.amplitudesRight[time][startFreq];
			float endAmp = DFTEditor.amplitudesRight[time][endFreq];
			double slope = (startAmp - endAmp) / (startFreq - endFreq);
			if(slope == 0.0f) {
				if(Math.random() < 0.5) {
					slope = -0.00001f;
				} else {
					slope = 0.00001f;
				}
			}
			for(int innerFreq = startFreq + 1; innerFreq < endFreq; innerFreq++) {
				float ampValue = (float) (startAmp + (innerFreq - startFreq) * slope);
				DFTEditor.amplitudesRight[time][innerFreq] = ampValue;
			}
		}
	}
}

public static void applyMasking() {
	if(DFTEditor.amplitudesLeft == null) return;
	if(DFTEditor.amplitudesRight == null) return;
	int bins = FDData.noteBase / 3;
	int numTimes = DFTEditor.amplitudesLeft.length;
	int numFreqs = DFTEditor.amplitudesLeft[0].length;
	for(int time = 0; time < numTimes; time++) {
		for(int freq = 0; freq < numFreqs; freq++) {
			float amplitude = DFTEditor.amplitudesLeft[time][freq];
			for(int innerFreq = freq - bins; innerFreq <= freq + bins; innerFreq++) {
				float maskingVal = (float) amplitude + Math.abs((freq - innerFreq) / (float) bins) * (float) maskingFactor - 1.0f;
				if(maskingVal < 0) continue;
				if(innerFreq < 0 || innerFreq >= numFreqs || innerFreq == freq) continue;
				if(DFTEditor.amplitudesLeft[time][innerFreq] < maskingVal) DFTEditor.amplitudesLeft[time][innerFreq] = 0.0f;
			}
		}
	}
	numTimes = DFTEditor.amplitudesRight.length;
	numFreqs = DFTEditor.amplitudesRight[0].length;
	for(int time = 0; time < numTimes; time++) {
		for(int freq = 0; freq < numFreqs; freq++) {
			float amplitude = DFTEditor.amplitudesRight[time][freq];
			for(int innerFreq = freq - bins; innerFreq <= freq + bins; innerFreq++) {
				float maskingVal = (float) amplitude + Math.abs((freq - innerFreq) / (float) bins) * (float) maskingFactor - 1.0f;
				if(maskingVal < 0) continue;
				if(innerFreq < 0 || innerFreq >= numFreqs || innerFreq == freq) continue;
				if(DFTEditor.amplitudesRight[time][innerFreq] < maskingVal) DFTEditor.amplitudesRight[time][innerFreq] = 0.0f;
			}
		}
	}
}

static void filterMatrix() {
	int numTimes = DFTEditor.amplitudesLeft.length;
	int numFreqs = DFTEditor.amplitudesLeft[0].length;
	for(int freq = 0; freq < numFreqs; freq++) {
		double freqInHz = Math.pow(2.0, DFTEditor.freqToNote(freq) / (double) FDData.noteBase);
		if(freqInHz < midFreq) continue;
		double logAdjust = Math.log(freqInHz / midFreq) / Math.log(2.0) / 2.0;
		for(int time = 0; time < numTimes; time++) {
			DFTEditor.amplitudesLeft[time][freq] += logAdjust;
			if(DFTEditor.amplitudesLeft[time][freq] < 0.0) DFTEditor.amplitudesLeft[time][freq] = 0.0f;
		}
	}
	numTimes = DFTEditor.amplitudesRight.length;
	numFreqs = DFTEditor.amplitudesRight[0].length;
	for(int freq = 0; freq < numFreqs; freq++) {
		double freqInHz = Math.pow(2.0, DFTEditor.freqToNote(freq) / (double) FDData.noteBase);
		if(freqInHz < midFreq) continue;
		double logAdjust = Math.log(freqInHz / midFreq) / Math.log(2.0) / 2.0;
		for(int time = 0; time < numTimes; time++) {
			DFTEditor.amplitudesRight[time][freq] += logAdjust;
			if(DFTEditor.amplitudesRight[time][freq] < 0.0) DFTEditor.amplitudesRight[time][freq] = 0.0f;
		}
	}	
}

public static void printDFTParameters() {
	InitWavelets();
	int index = 0;
	int numWavelets = WaveletInfoArrayList.size();
	for(WaveletInfo waveletInfo: WaveletInfoArrayList) {
		if(index % FDData.noteBase == 0) {
			System.out.print(index);
			printWaveletInfo(waveletInfo);
		}
		index++;
	}
	System.out.print(numWavelets);
	printWaveletInfo(WaveletInfoArrayList.get(numWavelets - 1));
	System.out.println("maxBinStep: " + maxBinStep);
	System.out.println("midFreq: " + midFreq);
	System.out.println("bassFreq: " + bassFreq);
	System.out.println("noteBase: " + FDData.noteBase);
	System.out.println("timeStepInMillis: " + FDData.timeStepInMillis);
}

static void FileDFTMatrix(String fileName) {
	maskingFactor = Float.NEGATIVE_INFINITY;
	double samplesPerStep = SynthTools.sampleRate / (1000.0 / FDData.timeStepInMillis);
	InitWavelets();
	int maxCenterIndex = LoadSamplesFromFile(fileName);
	int maxTime = (int) Math.floor(maxCenterIndex / samplesPerStep);
	for(int centerIndex = 0; centerIndex < maxCenterIndex; centerIndex += samplesPerStep) {
		int waveletIndex = 0;
		for(waveletIndex = 0; waveletIndex < numWavelets; waveletIndex++) {
			SingleDFT(waveletIndex, (int) Math.round(centerIndex));
		}
		int currentTime = (int) Math.round(centerIndex / samplesPerStep);
		if(currentTime % 100 == 0) {
			System.out.println("FileDFTMatrix: " + (centerIndex / samplesPerStep) + " of " + (maxCenterIndex / samplesPerStep));
		}
	}
	//fillMatrix();
	//filterMatrix();
	
}
}
