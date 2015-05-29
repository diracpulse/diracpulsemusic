
// OSC1
extern unsigned char osc1PrevVal;
extern unsigned char osc1shAmt;
extern unsigned char osc1lfoAmt;
extern unsigned char osc1adsrAmt;
unsigned char osc1PrevVal;
unsigned char osc1shAmt;
unsigned char osc1lfoAmt;
unsigned char osc1adsrAmt;

// OSC2
extern unsigned char osc2PrevVal;
extern unsigned char osc2shAmt;
extern unsigned char osc2lfoAmt;
extern unsigned char osc2adsrAmt;
unsigned char osc2PrevVal;
unsigned char osc2shAmt;
unsigned char osc2lfoAmt;
unsigned char osc2adsrAmt;

// OSC Master Data
extern unsigned int oscDeltaPhaseIndex[2];
unsigned int oscDeltaPhaseIndex[2];
extern unsigned char oscMasterData[63];
unsigned char oscMasterData[63];
// 0 - 1 osc1LFOMod
// 2 - 3 osc1SHMod
// 4 - 5 osc1ENVMod
// 6 - 8 osc1DeltaPhase
// 9 - 10 osc2LFOMod
// 11 - 12 osc2SHMod
// 13 - 14 osc2ENVMod
// 15 - 17 osc2DeltaPhase
extern unsigned char pitchMod[2];
unsigned char pitchMod[2];
extern unsigned char maxMidiNote = 84;
extern unsigned char minMidiNote = 12;

void initOSC1() {
	asm("ldi r25, 36");
	asm("ldi r24, 0");
	asm("call setDeltaPhaseIndex");	
}

void initOSC2() {
	asm("ldi r25, 72");
	asm("ldi r24, 1");
	asm("call setDeltaPhaseIndex");
}

// note in r25, oscIndex in r24
void setDeltaPhaseIndex() {
	asm("lds r17, minMidiNote");
	asm("cp r25, r17");
	asm("brsh sdpiCheckHigh");
	asm("ret");
	asm("sdpiCheckHigh:");
	asm("lds r17, maxMidiNote");
	asm("inc r17");
	asm("cp r25, r17");
	asm("brlo sdpiSetNote");
	asm("ret");
	asm("sdpiSetNote:");
	// check which oscillator
	asm("cpi r24, 0");
	asm("breq sdpiSetOsc1");
	// update osc2
	asm("ldi r23, 3 * 64");
	asm("mul r25, r23");
	asm("sts oscDeltaPhaseIndex + 2, r0");
	asm("sts oscDeltaPhaseIndex + 3, r1");
	asm("ret");
	asm("sdpiSetOsc1:");
	asm("ldi r23, 3 * 64");
	asm("mul r25, r23");
	//X = &oscDeltaPhaseIndex;
	asm("ldi r26, lo8(oscDeltaPhaseIndex)\n");
	asm("ldi r27, hi8(oscDeltaPhaseIndex)\n");
	asm("sts oscDeltaPhaseIndex, r0");
	asm("sts oscDeltaPhaseIndex + 1, r1");
}

extern void setPitchBend() {
	asm("ldi r24, 64");
	asm("sub r24, r25");
	// 15 = a fifth (15 * 64)
	asm("ldi r24, 15");
	asm("muls r25, r24");
	asm("sts pitchMod, r0");
	asm("sts pitchMod + 1, r1");
}

extern void setOSC1LFOMod() {
	asm("ldi r24, 64");
	asm("sub r24, r25");
	// 6 = 2 semitones (6 * 64)
	asm("ldi r24, 15");
	asm("muls r25, r24");
	asm("sts osc1MasterData, r0");
	asm("sts osc1MasterData + 1, r1");
}

extern void setOSC1SHMod() {
	asm("ldi r24, 64");
	asm("sub r24, r25");
	// 6 = 2 semitones (6 * 64)
	asm("ldi r24, 15");
	asm("muls r25, r24");
	asm("sts osc1MasterData + 2, r0");
	asm("sts osc1MasterData + 3, r1");
}


extern void setOSC1ENVMod() {
	asm("ldi r24, 64");
	asm("sub r24, r25");
	// 6 = 2 semitones (6 * 64)
	asm("ldi r24, 15");
	asm("muls r25, r24");
	asm("sts osc1MasterData + 4, r0");
	asm("sts osc1MasterData + 5, r1");
}


extern void setOSC2LFOMod() {
	asm("ldi r24, 64");
	asm("sub r24, r25");
	// 6 = 2 semitones  (6 * 64)
	asm("ldi r24, 15");
	asm("muls r25, r24");
	asm("sts osc1MasterData + 9, r0");
	asm("sts osc1MasterData + 10, r1");
}


extern void setOSC2SHMod() {
	asm("ldi r24, 64");
	asm("sub r24, r25");
	// 6 = 2 semitones  (6 * 64)
	asm("ldi r24, 15");
	asm("muls r25, r24");
	asm("sts osc1MasterData + 11, r0");
	asm("sts osc1MasterData + 12, r1");
}


extern void setOSC2ENVMod() {
	asm("ldi r24, 64");
	asm("sub r24, r25");
	// 6 = 2 semitones  (6 * 64)
	asm("ldi r24, 15");
	asm("muls r25, r24");
	asm("sts osc1MasterData + 13, r0");
	asm("sts osc1MasterData + 14, r1");
}

extern void updateOscillators() {
	/*
	// START OSC1
	*/
	//serialWrite(portBVal);
	//cli();
	// osc1DeltaPhaseIndex in r5:r4
	asm("lds r4, oscDeltaPhaseIndex\n");
	asm("lds r5, oscDeltaPhaseIndex + 1\n");
	//Y = oscMasterData (starting at osc1LFOMod)
	asm("ldi r28, lo8(oscMasterData)\n");
	asm("ldi r29, hi8(oscMasterData)\n");
	//Y = osc1LFOMod
	asm("ld r3, Y+\n");
	asm("ld r2, Y+\n");
	asm("add r4, r3");
	asm("adc r5, r2");
	//Y = osc1SHMod
	asm("ld r3, Y+\n");
	asm("ld r2, Y+\n");
	asm("add r4, r3");
	asm("adc r5, r2");
	//Y = osc1ENVMod
	asm("ld r3, Y+\n");
	asm("ld r2, Y+\n");
	asm("add r4, r3");
	asm("adc r5, r2");
	// add pitchBend
	asm("lds r20, pitchMod");
	asm("lds r21, pitchMod + 1");
	asm("add r4, r20");
	asm("adc r5, r21");
	// put deltaPhase in Z
	asm("ldi r30, lo8(deltaPhase)\n");
	asm("ldi r31, hi8(deltaPhase)\n");
	// add offset from r5:r4
	asm("add r30, r4\n");
	asm("adc r31, r5\n");
	// finally load deltaPhase from Z
	asm("lpm r17, Z+\n");
	asm("lpm r18, Z+\n");
	asm("lpm r19, Z+\n");
	// load deltaPhase into bytes [6 - 8] of osc1DeltaPhase
	asm("cli");
	asm("st Y+, r17");
	asm("st Y+, r18");
	asm("st Y+, r19");
	asm("sei");
	/*
	// START OSC2
	*/
	//serialWrite(portBVal);
	//asm("cli");
	// osc1DeltaPhaseIndex in r5:r4
	asm("lds r4, oscDeltaPhaseIndex + 2\n");
	asm("lds r5, oscDeltaPhaseIndex + 3\n");
	//Y = oscMasterData (starting at osc2LFOMod) (in Y from OSC1)
	asm("ld r3, Y+\n");
	asm("ld r2, Y+\n");
	asm("add r4, r3");
	asm("adc r5, r2");
	//Y = osc1SHMod
	asm("ld r3, Y+\n");
	asm("ld r2, Y+\n");
	asm("add r4, r3");
	asm("adc r5, r2");
	//Y = osc1ENVMod
	asm("ld r3, Y+\n");
	asm("ld r2, Y+\n");
	asm("add r4, r3");
	asm("adc r5, r2");
	// add pitchBend (already in r21:r20)
	// asm("lds r20, pitchMod");
	// asm("lds r21, pitchMod + 1");
	asm("add r4, r20");
	asm("adc r5, r21");
	// put deltaPhase in Z
	asm("ldi r30, lo8(deltaPhase)\n");
	asm("ldi r31, hi8(deltaPhase)\n");
	// add offset from r5:r4
	asm("add r30, r4\n");
	asm("adc r31, r5\n");
	// finally load deltaPhase from Z
	asm("lpm r17, Z+\n");
	asm("lpm r18, Z+\n");
	asm("lpm r19, Z+\n");
	// load deltaPhase into bytes [15 - 16] of osc1CurrentValue
	asm("cli");
	asm("st Y+, r17");
	asm("st Y+, r18");
	asm("st Y+, r19");
	asm("sei");
}
