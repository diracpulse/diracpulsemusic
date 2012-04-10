
#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#define MAXSAMPLES (44100 * 60 * 5)
#define MAXDATA (31 * 10 * 200 * 60)

int numFDData = 0;
int maxSample = 0;
int inputFileLength = 0;
double dAverageNote = 0.0;

double sampleArray[MAXSAMPLES];

const double onePI = 3.1415926535897932384626433832795;
const double twoPI = 6.283185307179586476925286766559;
const double sampleRate = 44100.0;
const double timeToSample = 220.5; // 5ms
const double notesPerOctave = 31.0;
const double log2val = 0.69314718055994530941723212145818;
const double max16BitAmplitude = 32767.0;


struct FDData {
	int time;
	int note;
	double logAmplitude;
	long harmonicID;
} FDDataArray[MAXDATA];

int timeIn;
int noteIn;
double logAmplitudeIn;
long harmonicIDIn;


void printFDData(struct FDData data, int index) {
	printf("FDData: ");
	printf("index: %d ", index);
	int time = data.time;
	int note = data.note;
	double logAmplitude = data.logAmplitude;
	long harmonicID = data.harmonicID;
	printf("t: %d ", time);
	printf("n: %d ", note);
	printf("la: %e ", logAmplitude);
	printf("hID: %li ", harmonicID);
	printf("\n");
}

void LoadDataFromFile(FILE *stream, int offset, int index) {
	//printf("LoadDataFromFile");
	fseek(stream, offset, SEEK_SET);
	fread((void *) &timeIn, sizeof(int), 1, stream);
	fread((void *) &noteIn, sizeof(int), 1, stream);
	fread((void *) &logAmplitudeIn, sizeof(double), 1, stream);
	fread((void *) &harmonicIDIn, sizeof(long), 1, stream);
	FDDataArray[index].time = timeIn;
	FDDataArray[index].note = noteIn;
	FDDataArray[index].logAmplitude = logAmplitudeIn;
	FDDataArray[index].harmonicID = harmonicIDIn;
	//printFDData(FDDataArray[index], index);
}

void WriteDataToFile(FILE *stream) {
	fwrite((void *) &sampleArray, sizeof(double), maxSample, stream);
}

int ReadAllData(FILE *stream) {
	//printf("ReadAllData\n");
	fseek(stream, 0, SEEK_END);
	inputFileLength = (int) ftell(stream);
	//printf("inputFileLength %d\n", inputFileLength);
	int offset;
	int index = 0;
	fseek(stream, 0, SEEK_SET);
	for(offset = 0; offset < inputFileLength; offset += (4 + 4 + 8 + 8)) {
		LoadDataFromFile(stream, offset, index);
		index++;
		//printf("index %d\n", index);
	}
	numFDData = index;
	return 0;
}

void Normalize() {
	double maxAmplitude = 0.0;
	int arrayIndex;
	for(arrayIndex = 0; arrayIndex < maxSample; arrayIndex++) {
		if(sampleArray[arrayIndex] > maxAmplitude) maxAmplitude = sampleArray[arrayIndex];
	}
	if(maxAmplitude == 0.0) {
		printf("No Sample Data\n");
		return;
	}
	for(arrayIndex = 0; arrayIndex < maxSample; arrayIndex++) {
		sampleArray[arrayIndex] = (sampleArray[arrayIndex] / maxAmplitude) *  max16BitAmplitude;
	}
}

void SynthHarmonicFlat(int startIndex, int endIndex) {
	//printf("startIndex: %d endIndex: %d\n", startIndex, endIndex);
	int arrayIndex;
	int timeIndex;
	int maxArrayIndex = numFDData;
	double currentPhase = 0.0;
	for(arrayIndex = startIndex; arrayIndex < endIndex; arrayIndex++) {
		double averageNote = (double) FDDataArray[arrayIndex].note + (double) FDDataArray[arrayIndex + 1].note;
		averageNote /= 2.0;
		double freqInHz = pow(2.0, (double) averageNote / (double) notesPerOctave);
		double deltaPhase = (freqInHz / sampleRate) * twoPI;
		int lowerTime = round(FDDataArray[arrayIndex].time * timeToSample);
		int upperTime = round(FDDataArray[arrayIndex + 1].time * timeToSample);
		if(upperTime > maxSample) maxSample = upperTime;
		double lowerLogAmplitude = FDDataArray[arrayIndex].logAmplitude;
		double upperLogAmplitude = FDDataArray[arrayIndex + 1].logAmplitude;
		double slope = (upperLogAmplitude - lowerLogAmplitude) / (upperTime - lowerTime);
		//printf("lt: %d ut: %d s: %f fih: %f dp: %f\n", lowerTime, upperTime, slope, freqInHz, deltaPhase);
		for(timeIndex = lowerTime; timeIndex < upperTime; timeIndex++) {
			double logAmplitude = lowerLogAmplitude + ((double) timeIndex - (double) lowerTime) * slope;
			double amplitude = exp(logAmplitude * log2val);
			sampleArray[timeIndex] += sin(currentPhase) * amplitude;
			currentPhase += deltaPhase;
		}
	}
}


void SynthAllData() {
	int arrayIndex;
	int endIndexArg;
	for(arrayIndex = 0; arrayIndex < MAXSAMPLES; arrayIndex++) sampleArray[arrayIndex] = 0.0;
	int startIndex = 0;
	int endIndex = 0;
	while(endIndex < numFDData) {
		long harmonicID = FDDataArray[startIndex].harmonicID;
		while(FDDataArray[endIndex].harmonicID == harmonicID) endIndex++;
		endIndexArg = endIndex - 1;
		SynthHarmonicFlat(startIndex, endIndexArg);
		startIndex = endIndex;
	}
}


void speedTest() {
	register double result = 0.0;
	register int value;
	register double logVal;
	register double fValue;
	for(value = 1; value < 10000000; value++){
		fValue = value;
		asm ("fsin" : "=t" (logVal) : "0" (fValue));
		result += logVal;
	}
	printf("%f\n", result);
}

int main(int argc, char *argv[])
{
	FILE *input = fopen("synthInput.dat", "rb");
	if (input == NULL) {
		printf("Unable to open input file\n");
		return 1;
	}
	FILE *output = fopen("synthOutput.dat", "wb");
	if (input == NULL) {
		printf("Unable to open output file\n");
		return 1;
	}
	ReadAllData(input);
	SynthAllData();
	//Normalize();
	WriteDataToFile(output);
	fclose(input);
	fclose(output);
	return 0;
}
