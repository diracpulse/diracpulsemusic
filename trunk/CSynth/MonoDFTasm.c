
#include <stdio.h>
#include <stdlib.h>
#include <math.h> 

char  FPSave[256];
short Left[2700];
short Right[2700];
double WindowedBlock[2700];
int n;
int BlockLength;
int StepLength;
int samplesLeft;
int blocknum;
int octave;
int note;
int EAX;
int EBX;
int ECX;
int EDX;
long currentOffset;
long startOffset;
long endOffset;
double dVal;
double dn;
double dN;
double dNminus1;
double arg;
double sinsum;
double cossum;
double phase;
double deltaPhase;
double freq;
double oldFreq;
double freqStep;
double binVal;
double oldBinVal;
double deltaBinVal;
double binFreq;
double binStep;
double sumVal;
double oldSumVal;
double asmDFTBinReturn;
double twoPI = 6.28318530717958647692;
double BHWindowTerm1 = 0.35875;
double BHWindowTerm2 = -0.48829;
double BHWindowTerm3 = 0.14128;
double BHWindowTerm4 = -.01168;
double asm_dN;
double asm_deltaPhase;
double asm_sinsum ;
double asm_cossum;
double asm_phase;
double stval;
double st1val;
double st2val;
double st3val;
double st4val;
double st5val;
double st6val;
double st7val;

short readSample(FILE *stream) {
	short returnVal;
	int upperSample;
	int lowerSample;
	upperSample = fgetc(stream);
	lowerSample = fgetc(stream);
	if ((upperSample == EOF) || (lowerSample == EOF)) {
		fclose(stream);
		exit(0);
	}
	returnVal = (short) ((upperSample << 8) + lowerSample);
	return returnVal;
}

void WindowBuffer() {
	dN = (double) BlockLength;
	arg = 2.0 * 3.1415926536 / (dN - 1.0);
	for (n = 0; n < BlockLength; n++) {
		dn = (double) n;
		WindowedBlock[n] = (double) Left[n];
		WindowedBlock[n] *= 0.35875 - 0.48829*cos(arg*dn) + 0.14128*cos(2.0*arg*dn)
				- .01168*cos(3.0*arg*dn);
	}
	return;
}

void asmWindowBuffer() {
	asm("fnsave _FPSave");
	asm("push %eax");
	asm("push %ebx");
	asm("push %ecx");
	asm("push %edx");
	asm("fld1");
	asm("fildl _BlockLength"); /* dN */
	asm("fsub %st, %st(1)"); /* dN - 1 */
	asm("fdivrl _twoPI"); /* st(0) = twoPI / st(0); */
	asm("fxch"); /* exchange st0, st1 */
	asm("fstp %st"); /* pop off 1 */
	asm("fstpl _deltaPhase");
	//asm("f
	asm("xorl %ecx, %ecx"); /* use %ecx as loop index */
	asm("movl $(_Left), %ebx"); /* use ebx for pointer to Left */
	asm("asmWindowBufferLoop:"); /* loop starts here */
    // this was missing in original
	asm("pop %edx");
	asm("pop %ecx");
	asm("pop %ebx");
	asm("pop %eax");
	asm("frstor _FPSave");
}


void fpRegPrint() {
	asm("push %eax");
	asm("push %ebx");
	asm("push %ecx");
	asm("push %edx");
	asm("fstpl _stval");
	asm("fstpl _st1val");
	asm("fstpl _st2val");
	asm("fstpl _st3val");
	asm("fstpl _st4val");
	asm("fstpl _st5val");
	asm("fstpl _st6val");
	asm("fstpl _st7val");
	asm("fnsave _FPSave");
	printf("%f %f %f %f %f %f %f %f\n", stval, st1val, st2val, st3val, st4val, st5val, st6val, st7val);
	asm("frstor _FPSave");
	asm("fldl _st7val");
	asm("fldl _st6val");
	asm("fldl _st5val");
	asm("fldl _st4val");
	asm("fldl _st3val");
	asm("fldl _st2val");
	asm("fldl _st1val");
	asm("fldl _stval");
	asm("pop %edx");
	asm("pop %ecx");
	asm("pop %ebx");
	asm("pop %eax");
} 
	
void fpPrint() {
	asm("push %eax");
	asm("push %ebx");
	asm("push %ecx");
	asm("push %edx");
	asm("fnsave _FPSave");
	printf("sinsum: %f, cossum: %f, phase: %f deltaPhase: %f dVal: %f EDX: %d  WindowedBlock: %p\n", 
			sinsum,     cossum,     phase,    deltaPhase,    dVal,    EDX,   WindowedBlock);
	asm("frstor _FPSave");
	asm("pop %edx");
	asm("pop %ecx");
	asm("pop %ebx");
	asm("pop %eax");
}

void asmDFTBin() {
	asm("fnsave _FPSave");
	asm("push %eax");
	asm("push %ebx");
	asm("push %ecx");
	asm("push %edx");
	asm("xorl %ecx, %ecx"); /* use %ecx as loop index */
	asm("movl $(_WindowedBlock), %ebx"); /* use ebx for pointer to WindowedBlock, init to 0 */
	asm("fldz"); /* load 0.0 for init sinsum and cossum */
	asm("fstl _sinsum"); /* set cossum to 0.0 */
	asm("fstl _cossum"); /* set sinsum to 0.0 */
	asm("fstl _phase"); /* set phase to 0.0 */
	asm("asmDFTBinLoop:"); /* loop starts here */
	asm("fldl _phase"); /* sinsum += *(WindowedBlock + $8(%edx)) * sin(phase) */
	asm("fsin");
	asm("fmull (%ebx)"); /*WINDOWBUFFER */
	asm("faddl _sinsum");
	asm("fstpl _sinsum");
	asm("fldl _phase"); /* sinsum += *(WindowedBlock + $8(%edx)) * sin(phase) */
	asm("fcos");
	asm("fmull (%ebx)");
	asm("faddl _cossum");
	asm("fstpl _cossum");
	asm("addl $8, %ebx"); /* get pointer to next double in WindowBuffer */
	asm("incl %ecx"); /* increment loop index */
	asm("fldl _phase"); /* phase += deltaphase */
	asm("faddl _deltaPhase");
	asm("fstpl _phase");
	asm("cmpl _BlockLength, %ecx");
	asm("jb asmDFTBinLoop"); /* if (index < BlockLength) dloop: */
	asm("fldl _sinsum"); /* sinsum = sinsum * sinsum */
	asm("fmull _sinsum");
	asm("fstpl _sinsum");
	asm("fldl _cossum"); /* cossum = cossum * cossum */
	asm("fmull _cossum");
	asm("fstpl _cossum");
	asm("fldl _sinsum"); /* sinsum^2 + cossum^2 */
	asm("faddl _cossum");
	asm("fsqrt");
	asm("fstpl _asmDFTBinReturn");
	asm("pop %edx");
	asm("pop %ecx");
	asm("pop %ebx");
	asm("pop %eax");
	asm("frstor _FPSave");
}

void DFTBin() {
	sinsum = 0.0;
	cossum = 0.0;
	phase = 0.0;
	for (n = 0; n < BlockLength; n++) {
		dVal = WindowedBlock[n];
		sinsum += dVal * sin(phase);
		cossum += dVal * cos(phase);
		phase += deltaPhase;
	}
	dVal = (sinsum * sinsum) + (cossum * cossum);
	dVal = sqrt(dVal);
	asmDFTBinReturn = dVal;
}

void BlockDFT() {
	WindowBuffer();
	freq = 40.0;
	sumVal = 0.0;
	deltaPhase = (freq / 44100.0) * 2.0 * 3.1415926536;
	asmDFTBin();
	binVal = asmDFTBinReturn;
	freqStep = pow(2.0, (1.0 / 31.0));
	while(freq < 20000.0) {
		oldFreq = freq;
		freq = freq * freqStep;
		binStep = (freq / binFreq) - (oldFreq / binFreq);
		oldBinVal = binVal;
		deltaPhase = (freq / 44100.0) * 2.0 * 3.1415926536;
		asmDFTBin();
		binVal = asmDFTBinReturn;
		deltaBinVal = (binVal - oldBinVal) * binStep;
		sumVal += deltaBinVal;
		/* printf("%d %f %f\n", blocknum, binStep, sumVal); */
	}
	return;
}


void InitDFT(FILE *stream, int start, int end) {

	BlockLength = 2700;
	StepLength = 900;
	binFreq = 44100.0 / 2700.0;
	startOffset = (long) (start + 44);
	endOffset = (long) (end + 44);
	
	fseek(stream, startOffset, 0);

	for(n = 0; n < 1350; n++) {
		Left[n] = 0;
		Right[n] = 0;
	}
	for(n = 1350; n < BlockLength; n++) {
		Left[n] = readSample(stream);
		Right[n] = readSample(stream);
	}
	
	/* run DFT */
	blocknum = 0;
	asmWindowBuffer();
	return;
	BlockDFT();
	/* BlockDFT(); */

	
	/* initial check of steps left for while loop */
	currentOffset = ftell(stream);
	if (currentOffset == -1) return;
	samplesLeft = (int) (endOffset - currentOffset);
	
	/* loop while steps left */
	while(samplesLeft > StepLength) {
		for(n = 0; n < (BlockLength - StepLength); n++) {
			Left[n] = Left[n + StepLength];
			Right[n] = Right[n + StepLength];
		}
		
		for(n = (BlockLength - StepLength); n < BlockLength; n++) {
			Left[n] = readSample(stream);
			Right[n] = readSample(stream);
		}
		
		/* run DFT */
		blocknum++;
		BlockDFT();
		/* BlockDFT(); */
		
		/* check samples left for while loop */
		currentOffset = ftell(stream);
		if (currentOffset == -1) return;
		samplesLeft = (int) (endOffset - currentOffset);
	}
	
	printf("%f\n", asm_dN);
	
}

int main(int argc, char** argv) {
	FILE *input = fopen("GetTheBalanceRight.wav", "rb");
	if (input == NULL) {
		printf("Unable to open input file\n");
		return 0;
	}
	InitDFT(input, 44100 * 10, 44100 * 20);
	fclose(input);
	return 0;
}
