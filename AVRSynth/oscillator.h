
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
// 9 - 11 osc1CurrentValue
// 12 - 13 osc2LFOMod
// 14 - 15 osc2SHMod
// 16 - 17 osc2ENVMod
// 18 - 20 osc2DeltaPhase

extern void updateOscillators() {
	/*
	// START OSC1
	*/
	//serialWrite(portBVal);
	//cli();
	//X = &oscDeltaPhaseIndex;
	asm("ldi r26, lo8(oscDeltaPhaseIndex)\n");
	asm("ldi r27, hi8(oscDeltaPhaseIndex)\n");
	// osc1DeltaPhaseIndex in r5:r4
	asm("ld r4, X+\n");
	asm("ld r5, X+\n");
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
	// put deltaPhase in Z
	asm("ldi r30, lo8(deltaPhase)\n");
	asm("ldi r31, hi8(deltaPhase)\n");
	// add offset from r5:r4
	asm("add r30, r4\n");
	asm("adc r31, r5\n");
	// finally load deltaPhase from Z
	asm("lpm r16, Z+\n");
	asm("lpm r17, Z+\n");
	asm("lpm r18, Z+\n");
	// load deltaPhase into bytes [6 - 8] of osc1DeltaPhase
	asm("st Y+, r16");
	asm("st Y+, r17");
	asm("st Y+, r18");
	// skip over osc1CurrentValue
	asm("adiw r28, 3");
	/*
	// START OSC2
	*/
	//serialWrite(portBVal);
	//cli();
	//X = &oscDeltaPhaseIndex;
	// osc1DeltaPhaseIndex in r5:r4 (in X from OSC1)
	asm("ld r4, X+\n");
	asm("ld r5, X+\n");
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
	// put deltaPhase in Z (can't reuse Z from OSC1, since it's off by 3)
	asm("ldi r30, lo8(deltaPhase)\n");
	asm("ldi r31, hi8(deltaPhase)\n");
	// add offset from r5:r4
	asm("add r30, r4\n");
	asm("adc r31, r5\n");
	// finally load deltaPhase from Z
	asm("lpm r16, Z+\n");
	asm("lpm r17, Z+\n");
	asm("lpm r18, Z+\n");
	// load deltaPhase into bytes [15 - 16] of osc1CurrentValue
	asm("st Y+, r16");
	asm("st Y+, r17");
	asm("st Y+, r18");
}