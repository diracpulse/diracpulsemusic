
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
//#include "main.h"

#define MAXDFTWINDOW 44100
#define MAXWAVELETS 31 * 16
#define MAXWAVELETDATA 31 * 16 * 10000

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

// Wavelet Variables
const double onePI = 3.1415926535897932384626433832795;
const double twoPI = 6.283185307179586476925286766559;
const double samplingRate = 44100.0;
const double maxCyclesPerWindow = 45.22540955090449;
const double samplesPerStep = 220.5; // 5ms
const double notesPerOctave = 31.0;
const double upperFreq = 20000.0;
const double centerFreq = 1000.0;
const double lowerFreq = 20.0;
const double taperPerOctave = 1.4142135623730950488016887242097; // sqrt(2.0)
const double alpha = 6.5;
int numWavelets = 0;
double KaiserWindow[MAXDFTWINDOW];

struct WaveletInfo {
	double radianFreq;
	double gain;
	int length;
	int note;
	int startIndex; // index into WaveletData
	float *sinArray;
	float *cosArray;
} WaveletInfoArray[MAXWAVELETS];

double logAmps[MAXWAVELETS];

void LoadSamplesFromFile(FILE *stream, int centerIndex) {
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
	//printf("%f ", ampVal);
}

void SampleArrayIndex(int waveletIndex) {
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
	int waveletIndex = 0;
	double upperAmp = 0.0;
	double centerAmp = 0.0;
	double lowerAmp = 0.0;
	for(waveletIndex = 0; waveletIndex < numWavelets; waveletIndex++) {
		SampleArrayIndex(waveletIndex);
	}
	upperAmp = logAmps[0];
	centerAmp = logAmps[1];
	for(waveletIndex = 2; waveletIndex < numWavelets; waveletIndex++) {
		lowerAmp = logAmps[waveletIndex];
		if((centerAmp >= upperAmp) && (centerAmp >= lowerAmp)) {
			if(centerAmp != 0.0) printf("%d %d %f\n", stepIndex, WaveletInfoArray[waveletIndex - 1].note, centerAmp);
		}
		upperAmp = centerAmp;
		centerAmp = lowerAmp;
	}
}

void InitWavelets() {
	double freqInHz = 0.0;
    double startLogFreq = 0.0;
    double currentLogFreq = 0.0;
    int note = 0;
    int index = 0;
    int windowLength = 0;
    double dWindowLength = 0.0;
    int windowStartIndex = 0;
	int maxNote = (int) round(log(upperFreq) / log(2.0) * notesPerOctave);
	int centerNote = (int) round(log(centerFreq) / log(2.0) * notesPerOctave);
	int minNote = (int) round(log(lowerFreq) / log(2.0) * notesPerOctave);
    double cyclesPerWindow = 1.0;
    double taperValue = 1.0;
    double ratio = (maxCyclesPerWindow - 1.0) / maxCyclesPerWindow;
    double samplesPerCycle = 0.0;
    double radianFreq = 0.0;
    if((ratio >= 1.0) || (ratio <= 0.0)) {
    	printf("Error: InitFrequencies: invalid ratio: %f", ratio);
    	return;
    }
    for(note = maxNote; note > centerNote; note--) {
    	freqInHz = pow(2.0, note / notesPerOctave);
    	samplesPerCycle = samplingRate / freqInHz;
    	radianFreq = twoPI / samplesPerCycle;
    	dWindowLength = samplingRate / freqInHz;
    	windowLength = (int) round(maxCyclesPerWindow * dWindowLength);
    	WaveletInfoArray[index].radianFreq = radianFreq;
    	WaveletInfoArray[index].length = windowLength;
    	WaveletInfoArray[index].startIndex = windowStartIndex;
    	WaveletInfoArray[index].note = note;
    	windowStartIndex += windowLength;
    	index++;
    }
    // start tapering window length
    startLogFreq = log(freqInHz) / log(2.0);
    for(note = centerNote; note >= minNote; note--) {
    	freqInHz = pow(2.0, note / notesPerOctave);
    	samplesPerCycle = samplingRate / freqInHz;
    	radianFreq = twoPI / samplesPerCycle;
    	currentLogFreq =  log(freqInHz) / log(2.0);
    	taperValue = pow(taperPerOctave, startLogFreq - currentLogFreq);
    	cyclesPerWindow = maxCyclesPerWindow / taperValue;
    	dWindowLength = samplingRate / freqInHz;
    	windowLength = (int) round(maxCyclesPerWindow * dWindowLength);
    	WaveletInfoArray[index].radianFreq = radianFreq;
    	WaveletInfoArray[index].length = windowLength;
    	WaveletInfoArray[index].startIndex = windowStartIndex;
    	WaveletInfoArray[index].note = note;
    	windowStartIndex += windowLength;
    	index++;
    }
    numWavelets = index;
    maxDFTLength = windowLength;
    //printf("%d\n", numWavelets, maxDFTLength);
    CalculateWavelets();
}

void CalculateWavelets() {
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
		WaveletInfoArray[waveletIndex].sinArray = (float *) malloc(length * sizeof(float));
		WaveletInfoArray[waveletIndex].cosArray = (float *) malloc(length * sizeof(float));
		CreateWindow(KaiserWindow, length, alpha);
		for(index = 0; index < length; index++) {
			dIndex = (double) index;
			gain += KaiserWindow[index];
			WaveletInfoArray[waveletIndex].sinArray[index] = sin(dIndex * radianFreq) * KaiserWindow[index];
			WaveletInfoArray[waveletIndex].cosArray[index] = sin(dIndex * radianFreq) * KaiserWindow[index];
		}
		WaveletInfoArray[waveletIndex].gain = gain;
	}
	for(index = 0; index < numWavelets; index++) {
		radianFreq = WaveletInfoArray[index].radianFreq;
		length = WaveletInfoArray[index].length;
		startIndex = WaveletInfoArray[index].startIndex;
		note = WaveletInfoArray[index].note;
		gain = WaveletInfoArray[index].gain;
		//printf("Wavelet: %d: %f %d %d %d %f\n", index, radianFreq, length, startIndex, note, gain);
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
	fread((void *) Header, 1, headerLengthInBytes, stream);
	fseek(stream, 0, SEEK_END);
	inputFileLength = (int) ftell(stream);
	// printf("file length: %i\n", inputFileLength);
	return 0;
}

void FileDFT(FILE *stream, int startCenterIndex, int maxCenterIndex) {
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
	FILE *input = fopen("input.wav", "rb");
	if (input == NULL) {
		printf("Unable to open input file\n");
		return 0;
	}
	FileDFT(input, 0, 44100 * 600);
	fclose(input);
	return 0;
}
