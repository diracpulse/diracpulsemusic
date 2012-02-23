
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
//#include "main.h"

#define MAXDFTWINDOW 44100
#define MAXWAVELETS 31 * 32

void CalculateWavelets();

// File Variables
short LeftRight[MAXDFTWINDOW * 2];
short Mono[MAXDFTWINDOW];
const int stereo = 2;
const int headerLengthInBytes = 56; // changed from 44 to accomodate newer .wav files
const int sampleLengthInBytes = 4;
int Header[16];
int stepIndex = 0;
int maxDFTLength = 0;
int inputFileLength = 0;
int maxCenterIndex = 0;
int debug = 0;
char str[80];

// Wavelet Variables
const double onePI = 3.1415926535897932384626433832795;
const double twoPI = 6.283185307179586476925286766559;
const double samplingRate = 44100.0;
const double samplesPerStep = 220.5; // 5ms
const double notesPerOctave = 31.0;
const double maxBinStep = 1.0;
const double alpha = 5.0;

// Calculated Variables
double maxCyclesPerWindow = 0.0;
int numWavelets = 0;

// Special Variables
double roundingFactor = 10.0;

double KaiserWindow[MAXDFTWINDOW];

struct WaveletInfo {
	double radianFreq;
	double gain;
	int length;
	int note;
	float *sinArray;//[MAXDFTWINDOW];
	float *cosArray;//[MAXDFTWINDOW];
} WaveletInfoArray[MAXWAVELETS];

double logAmps[MAXWAVELETS];

void LoadSamplesFromFile(FILE *stream, int centerIndex) {
	//if(debug) printf("LoadSamplesFromFile\n");
	if(centerIndex >= (inputFileLength - headerLengthInBytes) / sampleLengthInBytes) {
		// printf("End of file reached at sample %i\n", centerIndex);
		exit(0);
	}
	int fileReadStart = 0;
	int fileReadEnd = 0;
	int fileReadLength = 0;
	int startIndex = centerIndex - maxDFTLength / 2;
	int index = startIndex;
	int arrayIndex = 0;
	// printf("\nindex: %i arrayIndex: %i\n", index, arrayIndex);
	while(index < 0) {
		LeftRight[arrayIndex * 2] = (short) 0;
		LeftRight[arrayIndex * 2 + 1] = (short) 0;
		index++;
		arrayIndex++;
	}
	// printf("index: %i arrayIndex: %i\n", index, arrayIndex);
	fileReadStart = (index * 4) + headerLengthInBytes;
	fileReadLength = (maxDFTLength - arrayIndex) * 4;
	fileReadEnd = fileReadStart + fileReadLength;
	if(fileReadEnd > inputFileLength) {
		fileReadLength = inputFileLength - fileReadStart;
	}
	fseek(stream, (long) fileReadStart, 0);
	fread((void *) &LeftRight[arrayIndex * stereo], 1, fileReadLength, stream);
	/* printf("index: %i, arrayIndex: %i, fileReadStart: %i, fileReadLength: %i\n", 
			index, arrayIndex, fileReadStart, fileReadLength); */
	arrayIndex += (fileReadLength / 4);
	index += (fileReadLength / 4);
	// printf("index: %i arrayIndex: %i\n", index, arrayIndex);
	while(arrayIndex < maxDFTLength) {
		LeftRight[arrayIndex * 2] = (short) 0;
		LeftRight[arrayIndex * 2 + 1] = (short) 0;
		arrayIndex++;
	}
	// printf("index: %i arrayIndex: %i\n", index, arrayIndex);
}

void SingleDFT(int waveletIndex, int startIndex, int endIndex) {
	//if(debug) printf("SingleDFT\n");
	int index = 0;
	int maxIndex = endIndex - startIndex;
	int maxIndexWavelet = WaveletInfoArray[waveletIndex].length;
	if(maxIndex != maxIndexWavelet) {
		//printf("SingleDFT: maxIndex: %d maxIndexWavelet %d\n", maxIndex, maxIndexWavelet);
		if(maxIndexWavelet < maxIndex) {
			maxIndex = maxIndexWavelet;
		}
	}
	double leftVal = 0.0;
	double rightVal = 0.0;
	double monoVal = 0.0;
	double sinVal = 0.0;
	double cosVal = 0.0;
	double ampVal = 0.0;
	double logAmp = 0.0;
	double roundedLogAmp = 0.0;
	for(index = 0; index < maxIndex; index++) {
		leftVal = (double) LeftRight[(startIndex + index) * 2];
		rightVal = (double) LeftRight[(startIndex + index) * 2 + 1];
		monoVal = leftVal + rightVal;
		sinVal += WaveletInfoArray[waveletIndex].sinArray[index] * monoVal;
		cosVal += WaveletInfoArray[waveletIndex].cosArray[index] * monoVal;
	}
	ampVal = sinVal * sinVal;
	ampVal += cosVal * cosVal;
	ampVal = sqrt(ampVal) / WaveletInfoArray[waveletIndex].gain;
	ampVal *= 2.0; // integral of sin, cos over time approaches 0.5
	if(ampVal > 4.0) {
		logAmp = log(ampVal) / log(2.0);
	} else {
		logAmp = 0.0;
	}
	logAmps[waveletIndex] = logAmp;
	// MATRIX OUTPUT
	roundedLogAmp = round(logAmp * roundingFactor) / roundingFactor;
	printf("%f\n", roundedLogAmp);
}

void SampleArrayIndex(int waveletIndex) {
	//if(debug) printf("SampleArrayIndex\n");
	if (maxDFTLength == 0) return;
	int DFTLength = WaveletInfoArray[waveletIndex].length;
	int startIndex;
	int centerIndex;
	int endIndex;
	int index;
	int length;
	if(DFTLength > MAXDFTWINDOW) {
		printf("Error: SampleArrayIndex: DFTLength %i > MAXDFTWINDOW: %i\n", DFTLength, MAXDFTWINDOW);
		exit(0);
	}
	if(DFTLength > maxDFTLength) {
		printf("Error: SampleArrayIndex: DFTLength %i > maxDFTLength: %i\n", DFTLength, MAXDFTWINDOW);
		exit(0);
	}
	if(DFTLength == maxDFTLength) {
		centerIndex = maxDFTLength / 2;
		startIndex = 0;
		endIndex = maxDFTLength;
	} else {
		centerIndex = maxDFTLength / 2;
		startIndex = centerIndex - (DFTLength / 2);
		endIndex = centerIndex + (DFTLength / 2);
	}
	endIndex += DFTLength % 2;
	length = 0;
	for(index = startIndex; index < endIndex; index++) {
		length++;
	}
    // printf("start: %i center: %i end: %i length: %i inputLength: %i computedLength: %i\n", 
	//		startIndex, centerIndex, endIndex, length, DFTLength, (endIndex - startIndex));
	SingleDFT(waveletIndex, startIndex, endIndex);
}


void FreqDFT() {
	//if(debug) printf("FreqDFT\n");
	int waveletIndex = 0;
	//double upperAmp = 0.0;
	//double centerAmp = 0.0;
	//double lowerAmp = 0.0;
	for(waveletIndex = 0; waveletIndex < numWavelets; waveletIndex++) {
		SampleArrayIndex(waveletIndex);
	}
	//upperAmp = logAmps[0];
	//centerAmp = logAmps[1];
	/*
	for(waveletIndex = 2; waveletIndex < numWavelets; waveletIndex++) {
		lowerAmp = logAmps[waveletIndex];
		if((centerAmp >= upperAmp) && (centerAmp >= lowerAmp)) {
			if(centerAmp != 0.0) printf("%d %d %f\n", stepIndex, WaveletInfoArray[waveletIndex - 1].note, centerAmp);
		}
		upperAmp = centerAmp;
		centerAmp = lowerAmp;
	}
	*/
}


double noteToFrequency(int note) {
	return pow(2.0, note / notesPerOctave);
}

int frequencyToNote(double frequency) {
	return (int) round(log(frequency) / log(2.0) * notesPerOctave);
}

void InitWavelets() {
	int index = 0;
	// UNRESOLVED ISSUE: bin step = 2.0 and maxFreqHz = 20000.0, causes noise at 20000.0
	// does not occur at frequency above, or two below
	double maxFreqHz = 19160.0;
	double minFreqHz = 20.0;
	//if(debug) printf("InitWavelets\n");
	maxDFTLength = 0;
	maxCyclesPerWindow = maxBinStep / (pow(2.0, 1.0 / notesPerOctave) - 1.0);
	index = InitWaveletsHelper(maxFreqHz, 240.0, index, 1.0, 1.0);
	index = InitWaveletsHelper(240.0, 80.0, index, 1.0, 1.55); // bins = 20 @ freq = 80Hz
	index = InitWaveletsHelper(80.0, 20.0, index, 4.0, sqrt(2.0)); // bins = 10 @ freq = 20Hz
    numWavelets = index;
    // MATRIX OUTPUT
    printf("#_MAXNOTE_\n%d\n", frequencyToNote(maxFreqHz));
    printf("#_MINNOTE_\n%d\n", frequencyToNote(minFreqHz) + 1); // stops before last note
    //printf("%d\n", numWavelets, maxDFTLength);
    CalculateWavelets();
}

// Creates Wavelets starting at upperNote and ending at (lowerNote - 1)
// Returns index for NEXT wavelet
int InitWaveletsHelper(double upperFreqHz, double stopFreqHz, int index, double initialTaper, double taperPerOctave) {
	int note = 0;
	int upperNote = frequencyToNote(upperFreqHz);
	int stopNote = frequencyToNote(stopFreqHz);
    double startLogFreq = log(noteToFrequency(upperNote)) / log(2.0);
    for(note = upperNote; note > stopNote; note--) {
    	double freqInHz = noteToFrequency(note);
    	double samplesPerCycle = samplingRate / freqInHz;
    	double radianFreq = twoPI / samplesPerCycle;
    	double currentLogFreq =  log(freqInHz) / log(2.0);
    	double taperValue = initialTaper * pow(taperPerOctave, startLogFreq - currentLogFreq);
    	double cyclesPerWindow = maxCyclesPerWindow / taperValue;
    	int windowLength = (int) round(cyclesPerWindow * samplesPerCycle);
    	//IMPORTANT: the next 3 lines set cycles per window to an integer (doesn't improve anything)
    	//samplesPerCycle = round(windowLength / cyclesPerWindow);
    	//windowLength = cyclesPerWindow * samplesPerCycle;
    	//radianFreq = twoPI / samplesPerCycle;
    	if(windowLength > maxDFTLength) maxDFTLength = windowLength;
    	if(windowLength > MAXDFTWINDOW) {
    		printf("InitWavelets: Max DFT window length exceeded\n");
    		exit(0);
    	}
    	if(index > (MAXWAVELETS - 1)) {
    		printf("InitWavelets: Max number of wavelets exceeded\n");
    		exit(0);
    	}
    	WaveletInfoArray[index].radianFreq = radianFreq;
    	WaveletInfoArray[index].length = windowLength;
    	WaveletInfoArray[index].note = note;
    	index++;
    }
    return index;
}

void printWaveletInfo(struct WaveletInfo wavelet, int index) {
	printf("# Wavelet:");
	printf("index: %d ", index);
	double radFreq = wavelet.radianFreq;
	printf("radFreq: %f ", radFreq);
	double freqInHz = samplingRate / twoPI * radFreq;
	double length = wavelet.length;
	double samplesPerCycle = samplingRate / freqInHz;
	double bins = length / samplesPerCycle;
	printf("freqInHz: %f ", freqInHz);
	printf("bins: %f ", bins);
	printf("gain: %f ", wavelet.gain);
	printf("length: %d ", wavelet.length); // pass as int
	//printf("*sin[]: %x ", wavelet.sinArray);
	//printf("*cos[]: %x ", wavelet.cosArray);
	printf("\n");
}

void CalculateWavelets() {
	//if(debug) printf("CalculateWavelets\n");
	int waveletIndex = 0;
	int index = 0;
	double dIndex = 0.0;
	double radianFreq = 0.0;
	int length = 0;
	int startIndex = 0;
	int note = 0;
	double gain = 0.0;
	for(waveletIndex = 0; waveletIndex < numWavelets; waveletIndex++) {
		gain = 0.0;
		int length = WaveletInfoArray[waveletIndex].length;
		radianFreq = WaveletInfoArray[waveletIndex].radianFreq;
		//printf("malloc %d %d %d\n", waveletIndex, numWavelets, length);
		WaveletInfoArray[waveletIndex].sinArray = (float *) malloc(length * sizeof(float));
		WaveletInfoArray[waveletIndex].cosArray = (float *) malloc(length * sizeof(float));
		CreateWindow(KaiserWindow, length, alpha);
		for(index = 0; index < length; index++) {
			dIndex = (double) index;
			gain += KaiserWindow[index];
			WaveletInfoArray[waveletIndex].sinArray[index] = sin(dIndex * radianFreq) * KaiserWindow[index];
			WaveletInfoArray[waveletIndex].cosArray[index] = cos(dIndex * radianFreq) * KaiserWindow[index];
		}
		WaveletInfoArray[waveletIndex].gain = gain;
		printWaveletInfo(WaveletInfoArray[waveletIndex], waveletIndex);
	}
}

void LRSynth() {
	int index;
	double dindex;
	double sampleVal;
	double loopFreq;
	double arg = twoPI / samplingRate;
	double freqStart = 18000.0;
	double freqEnd = 18.0;
	double freqStep = (maxCyclesPerWindow - 1.0) / maxCyclesPerWindow;
	freqStep = pow(freqStep, 6.0);
	int printed = 0;
	for(index = 0; index < maxDFTLength; index++) {
		dindex = (double) index;
		sampleVal = 0.0;
		for(loopFreq = freqStart; loopFreq > freqEnd; loopFreq *= freqStep) {
			if(!printed) printf("Synth %f\n", loopFreq);
			sampleVal += sin(arg * dindex * loopFreq + loopFreq) * 500.0;
		}
		printed = 1;
		Mono[index] = sampleVal;
		LeftRight[(index * 2)] = (short) sampleVal;
		LeftRight[(index * 2 + 1)] = (short) sampleVal;
	}
}

int InitFileRead(FILE *stream) {
	//if(debug) printf("InitFileRead\n");
	fread((void *) Header, 1, headerLengthInBytes, stream);
	fseek(stream, 0, SEEK_END);
	inputFileLength = (int) ftell(stream);
	// printf("file length: %i\n", inputFileLength);
	return 0;
}

void FileDFT(FILE *stream, int startCenterIndex, int maxCenterIndex) {
	//if(debug) printf("FileDFT\n");
	int centerIndex;
	double dStepIndex;
	stepIndex = 0;
	dStepIndex = (double) stepIndex;
	InitFileRead(stream);
	InitWavelets();
	// LRSynth();
	// FreqDFT(upperFreq, centerFreq);
	// return;
	for(centerIndex = startCenterIndex; centerIndex < maxCenterIndex; centerIndex = (int) round(samplesPerStep * dStepIndex)) {
		LoadSamplesFromFile(stream, centerIndex);
		FreqDFT();
		stepIndex++;
		dStepIndex = (double) stepIndex;
		//if((stepIndex % 200) == 0) printf("%d\n", stepIndex);
	}
}

int main(int argc, char *argv[])
{
	//if(debug) printf("main\n");
	FILE *input = fopen("input.wav", "rb");
	//if(debug) printf("fopen\n");
	if (input == NULL) {
		printf("Unable to open input file\n");
		return 0;
	}
	FileDFT(input, 0, 44100 * 60 * 10);
	fclose(input);
	return 0;
}
