
#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#define MAXDFTWINDOW 44100
#define MAXFREQUENCIES 1000

double WindowLengths[MAXFREQUENCIES];
double RadianFreqs[MAXFREQUENCIES];
short LeftRight[MAXDFTWINDOW * 2];
short Mono[MAXDFTWINDOW];
int numFrequencies = 0;
int stereo = 2;
int headerLengthInBytes = 56; // changed from 44 to accomodate newer .wav files
int sampleLengthInBytes = 4; 
int Header[16];

const double gaussianConstant = 8.0;
const double onePI = 3.1415926535897932384626433832795;
const double twoPI = 6.283185307179586476925286766559;

double samplingRate = 44100.0;
double cyclesPerBin = 45.22540955090449;
double samplesPerStep = 220.5; // 5ms
double notesPerOctave = 31.0;
int maxDFTLength = 0;
int inputFileLength = 0; 
int maxCenterIndex = 0;
int calcDFT = 0;
int stepIndex = 0;

int globalNote;
double globalLogAmp;

void LoadSamplesFromFile(FILE *stream, int centerIndex) {
	if(centerIndex >= (inputFileLength - headerLengthInBytes) / sampleLengthInBytes) {
		// printf("End of file reached at sample %i\n", centerIndex);
		exit(0);
	}
	int fileReadStart = 0;
	int fileReadEnd = 0;
	int fileReadLength = 0;
	int startIndex = centerIndex - maxDFTLength / 2;
	int endIndex = centerIndex + (maxDFTLength / 2) + (maxDFTLength % 2);
	int index = startIndex;
	int arrayIndex = 0;
	int readLength = 0;
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

void SingleDFT(double radianFreq, int startIndex, int endIndex) {
	// int printInteger;
	// int printDecimal;
	int index;
	double dIndex;
	int lowerIndex;
	int upperIndex;
	double dLowerIndex;
	double dUpperIndex;
	double lowerLeftVal;
	double lowerRightVal;
	double upperLeftVal;
	double upperRightVal;
	double lowerMonoVal;
	double upperMonoVal;
	double sinVal = 0.0;
	double cosVal = 0.0;
	double ampVal = 0.0;
	double freqInHz = samplingRate * (radianFreq / twoPI);
	double logAmp = 0.0;
	double logFreq = 0.0;
	int fullWindow = endIndex - startIndex;
	int halfWindow = fullWindow / 2;
	double dHalfWindow = (double) halfWindow;
	double dFullWindow = (double) fullWindow;
	double z;
	double FullDFTWindowGain = 0.0;
	double windowVal;
	double windowRatio;
	double a0 = 0.35875;
	double a1 = -0.48829;
	double a2 = 0.14128;
	double a3 = -0.01168;
	for(index = 0; index < halfWindow; index++) {
		dIndex = (double) index;
		/* START: Gaussian Window   
		z = (0.5 + dHalfWindow - 1.0 - dIndex) * (onePI / dFullWindow);
		windowVal = exp(-1.0 * gaussianConstant * z * z);
		   END: Gaussian Window */
		windowRatio = dIndex / dFullWindow;
		windowVal = 0.0;
		windowVal += a0;
		windowVal += a1 * cos(2.0 * onePI * windowRatio);
		windowVal += a2 * cos(4.0 * onePI * windowRatio);
		windowVal += a3 * cos(6.0 * onePI * windowRatio);
		FullDFTWindowGain += 2.0 * windowVal;
		lowerIndex = index;
		upperIndex = (fullWindow - 1 - index);
		dLowerIndex = (double) lowerIndex;
		dUpperIndex = (double) upperIndex;
		lowerLeftVal = (double) LeftRight[(startIndex + lowerIndex) * 2];
		lowerRightVal = (double) LeftRight[(startIndex + lowerIndex) * 2 + 1];
		upperLeftVal = (double) LeftRight[(startIndex + upperIndex) * 2];
		upperRightVal = (double) LeftRight[(startIndex + upperIndex) * 2 + 1];
		// lowerMonoVal = Mono[startIndex + lowerIndex]; // lowerLeftVal + lowerRightVal;
		// upperMonoVal = Mono[startIndex + upperIndex]; // upperLeftVal + upperRightVal;
		lowerMonoVal = lowerLeftVal + lowerRightVal;
		upperMonoVal = upperLeftVal + upperRightVal;
		sinVal += sin(radianFreq * dLowerIndex) * lowerMonoVal * windowVal;
		sinVal += sin(radianFreq * dUpperIndex) * upperMonoVal * windowVal;
		cosVal += cos(radianFreq * dLowerIndex) * lowerMonoVal * windowVal;
		cosVal += cos(radianFreq * dUpperIndex) * upperMonoVal * windowVal;

		/* printf("fullWindow: %i lower: %i upper: %i windowVal: %f windowGain: %f\n", 
				fullWindow, lowerIndex, upperIndex, windowVal, FullDFTWindowGain); */
	}
	if((upperIndex - lowerIndex) == 2) {
		lowerIndex++;
		dLowerIndex = (double) lowerIndex;
		windowVal = 1.0;
		FullDFTWindowGain += windowVal;
		lowerLeftVal = (double) LeftRight[(startIndex + lowerIndex) * 2];
		lowerRightVal = (double) LeftRight[(startIndex + lowerIndex) * 2 + 1];
		// lowerMonoVal = Mono[startIndex + lowerIndex]; // lowerLeftVal + lowerRightVal;
		lowerMonoVal = lowerLeftVal + lowerRightVal;
		sinVal += sin(radianFreq * dLowerIndex) * lowerMonoVal * windowVal;
		cosVal += cos(radianFreq * dLowerIndex) * lowerMonoVal * windowVal;
		/* printf("fullWindow: %i center: %i windowVal: %f windowGain: %f\n", 
				fullWindow, lowerIndex, windowVal, FullDFTWindowGain); */
	}
	ampVal = sinVal * sinVal;
	ampVal += cosVal * cosVal;
	ampVal = sqrt(ampVal) / FullDFTWindowGain;
	ampVal *= 2.0; // integral of sin, cos over time approaches 0.5
	if(ampVal < 4.0) {
		logAmp = 0.0;
	} else {
		logAmp = log(ampVal) / log(2.0);
	}
	logFreq = log(freqInHz) / log(2.0);
	globalNote = (int) round(logFreq * notesPerOctave);
	globalLogAmp = logAmp;
	// printf("%i %f %f\n", stepIndex, logFreq, logAmp); // full printout
	// printf("%f\n", logFreq);
	// printInteger = (int) floor(logAmp);
	// printDecimal = (int) 100.0 * (logAmp - floor(logAmp));
	// printf("%d.%d\n", printInteger, printDecimal);
	// printf("%f\n", logAmp);
}

void SampleArrayIndex(double radianFreq, double dDFTLength) {
	if (maxDFTLength == 0) return;
	int iDFTLength = (int) dDFTLength;
	int startIndex;
	int centerIndex;
	int endIndex;
	int index;
	int length;
	if(iDFTLength > MAXDFTWINDOW) {
		printf("Error: SampleArrayIndex: iDFTLength %i > MAXDFTWINDOW: %i\n", iDFTLength, MAXDFTWINDOW);
		exit(0);
	}
	if(iDFTLength > maxDFTLength) {
		printf("Error: SampleArrayIndex: iDFTLength %i > maxDFTLength: %i\n", iDFTLength, MAXDFTWINDOW);
		exit(0);
	}
	if(iDFTLength == maxDFTLength) {
		centerIndex = maxDFTLength / 2;
		startIndex = 0;
		endIndex = maxDFTLength;
	} else {
		centerIndex = maxDFTLength / 2;
		startIndex = centerIndex - (iDFTLength / 2);
		endIndex = centerIndex + (iDFTLength / 2);
	}
	endIndex += iDFTLength % 2; 
	length = 0;
	for(index = startIndex; index < endIndex; index++) {
		length++;
	}
    // printf("start: %i center: %i end: %i length: %i inputLength: %i computedLength: %i\n", 
	//		startIndex, centerIndex, endIndex, length, iDFTLength, (endIndex - startIndex));
	SingleDFT(radianFreq, startIndex, endIndex);
}


void FreqDFT() {
	int freqIndex;
	double radianFreq;
	double windowLength;
	double upperLogAmp;
	int upperNote;
	double middleLogAmp;
	int middleNote;
	double lowerLogAmp;
	int lowerNote;
	upperLogAmp = 0.0;
	// test highest freq
	SampleArrayIndex(RadianFreqs[0], WindowLengths[0]);
	middleLogAmp = globalLogAmp;
	middleNote = globalNote;
	for(freqIndex = 1; freqIndex < (numFrequencies - 1); freqIndex++) {
		radianFreq = RadianFreqs[freqIndex];
		windowLength = WindowLengths[freqIndex];
		SampleArrayIndex(radianFreq, windowLength);
		lowerLogAmp = globalLogAmp;
		lowerNote = globalNote;
		if((upperLogAmp < middleLogAmp) && (lowerLogAmp < middleLogAmp)) {
			//printf("%i %i %f\n", stepIndex, middleNote, middleLogAmp); // full printout
		}
		upperLogAmp = middleLogAmp;
		upperNote = middleNote;
		middleLogAmp = lowerLogAmp;
		middleNote = lowerNote;
	}
	// test lowest freq
	if(upperLogAmp < middleLogAmp) {
		//printf("%i %i %f\n", stepIndex, middleNote, middleLogAmp); // full printout
	}
}

int InitFrequencies(double upperFreq, double centerFreq, double lowerFreq) {
	int freqIndex = 0;
	int freqIndexIncrement = 2;
	double freq;
	double logFreq;
	double radianFreq;
	double samplesPerCycle;
	double windowLength;
	double tempCyclesPerBin;
	double cyclesPerWindow;
	double ratio = (cyclesPerBin - 1.0) / cyclesPerBin;
	if((ratio >= 1.0) || (ratio <= 0.0)) {
		printf("Error: InitFrequencies: invalid ratio: %f\n", ratio);
		return;
	}
	for(freq = upperFreq; freq > centerFreq; freq *= ratio) {
		samplesPerCycle = samplingRate / freq;
		windowLength = samplesPerCycle * cyclesPerBin;
		cyclesPerWindow = windowLength / samplesPerCycle;
		radianFreq = twoPI / samplesPerCycle;
		RadianFreqs[freqIndex] = radianFreq;
		WindowLengths[freqIndex] = windowLength;
		freqIndex += freqIndexIncrement;
		if(freqIndex >= MAXFREQUENCIES) {
			printf("Error: InitFrequencies: MAXFREQUENCIES exceeded, freq: %f , index %i\n", freq, freqIndex);
			exit(0);
		}
	}
	maxDFTLength = (int) windowLength;
	if(maxDFTLength > MAXDFTWINDOW) {
		printf("Error: InitFrequencies: maxDFTLength %i > MAXDFTWINDOW %i\n", maxDFTLength, MAXDFTWINDOW);
		exit(0);
	}	
	for(tempCyclesPerBin = (cyclesPerBin - 1.0); tempCyclesPerBin > 0.0; tempCyclesPerBin -= 1.0) {
		samplesPerCycle = windowLength / tempCyclesPerBin;
		radianFreq = twoPI / samplesPerCycle;
		freq = samplingRate / samplesPerCycle;
		if(freq < lowerFreq) break;
		RadianFreqs[freqIndex] = radianFreq;
		WindowLengths[freqIndex] = windowLength;
		freqIndex += freqIndexIncrement;
		if(freqIndex >= MAXFREQUENCIES) {
			printf("Error: InitFrequencies: MAXFREQUENCIES exceeded, freq: %f , index %i\n", freq, freqIndex);
			exit(0);
		}
	}
	// Perform Interpolation
	numFrequencies = freqIndex - freqIndexIncrement + 1;
	for(freqIndex = 1; freqIndex < numFrequencies; freqIndex += freqIndexIncrement) {
		RadianFreqs[freqIndex] = (RadianFreqs[freqIndex - 1] + RadianFreqs[freqIndex + 1]) / 2.0;
		WindowLengths[freqIndex] = (WindowLengths[freqIndex - 1] + WindowLengths[freqIndex + 1]) / 2.0;
	}
	/* Commented out for TreeMap, uncomment for grid view */
	/*
	printf("FREQS:");
	for(freqIndex = 0; freqIndex < numFrequencies; freqIndex++) {
		freq = (RadianFreqs[freqIndex] / twoPI) * samplingRate;
		windowLength = WindowLengths[freqIndex];
		logFreq = log(freq) / log(2.0);
		printf(" %f", logFreq);
	}
	printf("\n");
	*/
}

void LRSynth() {
	int index;
	double dindex;
	double sampleVal;
	double loopFreq;
	double arg = twoPI / samplingRate;
	double freqStart = 18000.0;
	double freqEnd = 18.0;
	double freqStep = (cyclesPerBin - 1.0) / cyclesPerBin;
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

void FileDFT(FILE *stream, int startCenterIndex, int maxCenterIndex, 
			 double upperFreq, double centerFreq, double lowerFreq) {
	int centerIndex;
	double dStepIndex;
	stepIndex = 0;
	dStepIndex = (double) stepIndex;
	InitFileRead(stream);
	InitFrequencies(upperFreq, centerFreq, lowerFreq);
	// LRSynth();
	// FreqDFT(upperFreq, centerFreq);
	// return;
	for(centerIndex = startCenterIndex; centerIndex < maxCenterIndex; centerIndex = (int) round(samplesPerStep * dStepIndex)) {
		LoadSamplesFromFile(stream, centerIndex);
		FreqDFT();
		stepIndex++;
		dStepIndex = (double) stepIndex;
	}
}

int main(int argc, char *argv[])
{
	FILE *input = fopen("input.wav", "rb");
	if (input == NULL) {
		printf("Unable to open input file\n");
		return 0;
	}
	FileDFT(input, 0, 44100 * 600, 18322.012048779428, 1000.0, 20.0);
	fclose(input);
	return 0;
}
