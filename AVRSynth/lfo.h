
// LFO1

extern unsigned char lfo1Up;
extern unsigned char lfo1CurrentValue[3];
extern unsigned char lfo1DeltaPhaseIndex[2];
extern unsigned char lfo1MasterData[63];
unsigned char lfo1Up;
unsigned char lfo1CurrentValue[3];
unsigned char lfo1DeltaPhaseIndex[2];
unsigned char lfo1MasterData[63];

extern void initLFO1() {
	asm("ldi r28, lo8(lfoDelta)\n");
	asm("ldi r29, hi8(lfoDelta)\n");
	asm("ld r23, Y+\n");
	asm("ld r24, Y\n");
	asm("sts lfo1CurrentValue, r23");
	asm("sts lfo1CurrentValue + 1, r24");
	asm("ldi r17, 1");
	asm("sts lfo1Up, r17");
}

extern void updateLFO1Val() {
	// val passed in r25
	asm("ldi r28, lo8(lfoDelta)\n");
	asm("ldi r29, hi8(lfoDelta)\n");
	asm("lsl r25");
	asm("clr r2");
	asm("add r26, r25");
	asm("adc r27, r2");
	asm("sts lfo1DeltaPhaseIndex, r26");
	asm("sts lfo1DeltaPhaseIndex + 1, r27");
}

extern void updateLFO1() {
		// START LFO1
		asm("clr r2");
		asm("lds r25, portBVal"); // portVal in r25
		asm("andi r25, 0b11111100"); // clear lfo bits 
		// lfo1DeltaPhaseIndex in r5:r4
		asm("lds r4, lfo1DeltaPhaseIndex");
		asm("lds r5, lfo1DeltaPhaseIndex + 1");
		//Y = lfoMasterData
		asm("ldi r28, lo8(lfo1MasterData)\n");
		asm("ldi r29, hi8(lfo1MasterData)\n");
		// put deltaPhase in Z
		asm("ldi r30, lo8(lfoDelta)\n");
		asm("ldi r31, hi8(lfoDelta)\n");
		// add offset from r5:r4
		asm("add r30, r4\n");
		asm("adc r31, r5\n");
		// finally load lfoPhase from Z
		asm("lpm r16, Z+\n");
		asm("lpm r17, Z+\n");
		// Load lfo1CurrentValue into r21:r20:r19
		asm("ld r19, Y+\n");
		asm("ld r20, Y+\n");
		asm("ld r21, Y+\n");
		// store current value in r24
		asm("mov r24, r21");
		asm("lds r23, lfo1Up");
		// check if lfoUp
		asm("cpi r23, 0");
		// if not go to subtract
		asm("breq lfo1Subtract");
		// Add lfoPhase to lfo1CurrentValue
		asm("add r19, r16\n");
		asm("adc r20, r17\n");
		asm("adc r21, r2\n");
		// store lfo1Currentvalue
		asm("st -Y, r21\n");
		asm("st -Y, r20\n");
		asm("st -Y, r19\n");
		// compare with previous output
		asm("cp r24, r21\n");
		asm("brsh lfo1SkipSetOutput\n");
		// if greater than prev value set output to add pin
		asm("ori r25, 0b00000010\n");
		asm("lfo1SkipSetOutput:");
		// test for max value
		asm("cpi r21, 127\n");
		asm("brlo lfo1Finished\n");
		// if maxValue set lfoUp to 0
		asm("sts lfo1Up, r2");
		asm("jmp lfo1Finished");
		asm("lfo1Subtract:");
		// Substract lfoPhase from lfo1CurrentValue
		asm("sub r19, r16\n");
		asm("sbc r20, r17\n");
		asm("sbc r21, r2\n");
		// store lfo1Currentvalue
		asm("st -Y, r21\n");
		asm("st -Y, r20\n");
		asm("st -Y, r19\n");
		// compare with previous output
		asm("cp r21, r24\n");
		asm("brsh lfo1Finished\n");
		// if less than prev value set output to subtract pin
		asm("ori r25, 0b00000001\n");
		// see if were at 0
		asm("cpi r21, 0\n");
		asm("brne lfo1Finished");
		// if so start going up again
		asm("ldi r16, 1");
		asm("sts lfo1Up, r16");
		asm("lfo1Finished:");
		// write r25 to portBVal
		asm("sts portBVal, r25");
}

extern const unsigned char lfoDelta[] PROGMEM = {
23, 2, 46, 2, 71, 2, 97, 2, 124, 2, 152, 2, 181, 2, 212, 2, 244, 2, 
22, 3, 57, 3, 93, 3, 131, 3, 171, 3, 213, 3, 0, 4, 45, 4, 
93, 4, 142, 4, 194, 4, 248, 4, 48, 5, 107, 5, 168, 5, 232, 5, 
43, 6, 113, 6, 186, 6, 6, 7, 86, 7, 169, 7, 0, 8, 91, 8, 
185, 8, 28, 9, 131, 9, 239, 9, 96, 10, 213, 10, 80, 11, 208, 11, 
86, 12, 226, 12, 116, 13, 13, 14, 172, 14, 82, 15, 0, 16, 181, 16, 
115, 17, 56, 18, 7, 19, 223, 19, 192, 20, 171, 21, 161, 22, 161, 23, 
173, 24, 196, 25, 233, 26, 25, 28, 88, 29, 165, 30, 0, 32, 107, 33, 
229, 34, 113, 36, 14, 38, 189, 39, 128, 41, 86, 43, 65, 45, 66, 47, 
90, 49, 137, 51, 209, 53, 51, 56, 176, 58, 73, 61, 0, 64, 213, 66, 
203, 69, 226, 72, 28, 76, 122, 79, 255, 82, 172, 86, 130, 90, 132, 94, 
179, 98, 18, 103, 162, 107, 102, 112, 96, 117, 146, 122, 0, 128, 170, 133, 
149, 139, 195, 145, 55, 152, 245, 158, 254, 165, 88, 173, 4, 181, 8, 189, 
102, 197, 36, 206, 68, 215, 204, 224, 192, 234, 37, 245, 255, 255, 84, 11, 
42, 23, 135, 35, 111, 48, 233, 61, 252, 75, 175, 90, 8, 106, 16, 122, 
205, 138, 71, 156, 136, 174, 152, 193, 128, 213, 73, 234, 254, 255};