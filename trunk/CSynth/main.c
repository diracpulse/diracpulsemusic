
#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#define MAXSAMPLES (44100 * 60 * 5)
#define MAXDATA (31 * 10 * 200 * 60)

int numFDData = 0;
int maxSample = 0;
int inputFileLength = 0;
double maxLogAmplitude = 0.0001;

double sampleArray[MAXSAMPLES];

const double onePI = 3.1415926535897932384626433832795;
const double twoPI = 6.283185307179586476925286766559;
const double sampleRate = 44100.0;
const double timeToSample = 220.5; // 5ms
const double notesPerOctave = 62.0;
const double log2val = 0.69314718055994530941723212145818;
const double max16BitAmplitude = 32767.0;

struct FDData {
	float time;
	float note;
	double logAmplitude;
	long harmonicID;
} FDDataArray[MAXDATA];

// important asm variables (not all in use)
char FPSave[256];
int asmNumLoops;
double asmdouble;
double asmInt;
double asmLogAmplitude;
double asmSlope;
double asmCurrentPhase;
double asmDeltaPhase;
double asmResult;
double stval;
double st1val;
double st2val;
double st3val;
double st4val;
double st5val;
double st6val;
double st7val;
int ediVal;
int ecxVal;
int asmPrintVal;
double *asmSamplePointer;
double junk[10];
double junk2[10];


// FDData variables
float timeIn;
float noteIn;
double logAmplitudeIn;
long harmonicIDIn;

// debug asm variables
double asm1;
double asm2;
double asm3;
double asm4;
double asm5;
double asm6;
double asm7;
double asm8;
double asm9;
double asm10;
double asm11;
double asm12;
double asm13;
double asm14;
double asm15;
double asm16;
int asmIndex = 0;


void fpRegPrint(int printVal) {
	asm("fstpl _stval")	;
	asm("fstpl _st1val");
	printf("pv:%d la:%f s:%f cp:%f dp:%f r:%f st0:%f st1:%f\n", printVal, asmLogAmplitude, asmSlope, asmCurrentPhase, asmDeltaPhase, asmResult, stval, st1val);
	asm("fldl _st1val");
	asm("fldl _stval");

}

void printFPData() {
	printf("FPDATA: la:%f s:%f cp:%f dp:%f r:%f\n",
	asmLogAmplitude,
	asmSlope,
	asmCurrentPhase,
	asmDeltaPhase,
	asmResult);
}


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
	fread((void *) &timeIn, sizeof(float), 1, stream);
	fread((void *) &noteIn, sizeof(float), 1, stream);
	fread((void *) &logAmplitudeIn, sizeof(double), 1, stream);
	fread((void *) &harmonicIDIn, sizeof(long), 1, stream);
	FDDataArray[index].time = timeIn;
	FDDataArray[index].note = noteIn;
	FDDataArray[index].logAmplitude = logAmplitudeIn;
	FDDataArray[index].harmonicID = harmonicIDIn;
	if(logAmplitudeIn > maxLogAmplitude) maxLogAmplitude = logAmplitudeIn;
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

void printFPHistory() {
	printf("1:%f 2:%f 3:%f 4:%f 5:%f 6:%f 7:%f 8:%f 9:%f 10:%f 11:%f 12:%f 13:%f 14:%f s:%f\n",
	asm1,
	asm2,
	asm3,
	asm4,
	asm5,
	asm6,
	asm7,
	asm8,
	asm9,
	asm10,
	asm11,
	asm12,
	asm13,
	asm14);
}

double SynthHarmonicASMInnerLoop(int lowerTime, int upperTime, double logAmplitude, double slope, double currentPhase, double deltaPhase) {
	int index;
	asmNumLoops = upperTime - lowerTime;
	asmLogAmplitude = logAmplitude;
	asmSlope = slope;
	asmCurrentPhase = currentPhase;
	asmDeltaPhase = deltaPhase;
	//for(index = lowerTime; index < upperTime; index++) {
	//	sampleArray[index] = (float) (index - lowerTime);
	//}
	asmSamplePointer = &sampleArray[lowerTime];
	//printFPData();
	double junk;
	double asmInt;
	if(lowerTime >= upperTime) return asmCurrentPhase;
	asm("movl _asmNumLoops, %ecx");
	//asm("movl %ecx, _ecxVal ");
	asm("movl _asmSamplePointer, %edi");
	//asm("movl %edi, _ediVal ");
	asm("loop1:");
	asm("fstpl _junk");
	asm("fldl _asmLogAmplitude");
		//asm("fstl _asm1"); // DEBUG
	asm("faddl _asmSlope");
		//asm("fstl _asm2"); // DEBUG
	asm("fstl _asmLogAmplitude");
		//asm("fstl _asm3"); // DEBUG
	asm("frndint");
		//asm("fstl _asm1"); // DEBUG
	asm("fstl _asmInt");
	//asm("fst %st(0)");
		//asm("fstl _asm5"); // DEBUG
	//asm("fstl _asmLogAmplitude");
		//asm("fstl _asm6"); // DEBUG
	asm("fsubrl _asmLogAmplitude");
		//asm("fstl _asm7"); // DEBUG
	asm("f2xm1");
		//asm("fstl _asm2"); // DEBUG
	asm("fld1");
		//asm("fstl _asm3"); // DEBUG
	asm("faddp");
		//asm("fstl _asm4"); // DEBUG
	asm("fldl _asmInt");
	asm("fxch");
	asm("fscale");
		//asm("fstl _asm5"); // DEBUG
	asm("fldl _asmCurrentPhase");
		//asm("fstl _asm6"); // DEBUG
	asm("fsin");
		//asm("fstl _asm7"); // DEBUG
	asm("fmulp");
		//asm("fstl _asm8"); // DEBUG
	asm("faddl (%edi)");
		//asm("fstl _asm9"); // DEBUG
	asm("fstpl (%edi)");
		//asm("fstl _asm10"); // DEBUG
	asm("fldl _asmCurrentPhase");
		//asm("fstl _asm11"); // DEBUG
	asm("faddl _asmDeltaPhase");
		//asm("fstl _asm12"); // DEBUG
	asm("fstpl _asmCurrentPhase");
		//asm("fstl _asm13"); // DEBUG
	//printFPHistory();
	//asm("movl _ediVal, %edi");
	asm("addl $8, %edi");
	//asm("movl %edi, _ediVal");
	//asm("movl _ecxVal, %ecx");
	asm("decl %ecx");
	//asm("movl %ecx, _ecxVal");
	asm("cmpl $0, %ecx");
	asm("je finished");
	asm("jmp loop1");
	asm("finished:");
	//for(index = lowerTime; index < upperTime; index++) {
		//printf("%f ", sampleArray[index]);
	//}
	//exit(0);
	return asmCurrentPhase;
}

void printMacro() {
	asm("fstpl _stval")	;
	asm("fstpl _st1val");
	asm("movl %edi, _ediVal ");
	asm("movl %ecx, _ecxVal ");
	asm("fnsave _FPSave");
	fpRegPrint(0);
	asm("frstor _FPSave");
	asm("movl _ediVal, %edi ");
	asm("movl _ecxVal, %ecx ");
	asm("fldl _st1val");
	asm("fldl _stval");
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
		double freqInHz = pow(2.0, averageNote / notesPerOctave);
		double deltaPhase = (freqInHz / sampleRate) * twoPI;
		int lowerTime = round(FDDataArray[arrayIndex].time * timeToSample);
		int upperTime = round(FDDataArray[arrayIndex + 1].time * timeToSample);
		if(upperTime > maxSample) maxSample = upperTime;
		double lowerLogAmplitude = FDDataArray[arrayIndex].logAmplitude;
		double upperLogAmplitude = FDDataArray[arrayIndex + 1].logAmplitude;
		double slope = (upperLogAmplitude - lowerLogAmplitude) / (upperTime - lowerTime);
		double logAmplitude = lowerLogAmplitude - slope;
		double amplitude = 0;
		//printf("lt: %d ut: %d s: %f fih: %f dp: %f\n", lowerTime, upperTime, slope, freqInHz, deltaPhase);
		currentPhase = SynthHarmonicASMInnerLoop(lowerTime, upperTime, logAmplitude, slope, currentPhase, deltaPhase);
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
	if (output == NULL) {
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
