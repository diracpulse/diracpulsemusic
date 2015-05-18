
extern unsigned char jumpTable[127];
extern unsigned char midiChannel;
unsigned char jumpTable[127];
unsigned char midiChannel;

extern void initJumpTable() {
	// Put pointer to jump table in X
	asm("ldi r26, lo8(jumpTable)");
	asm("ldi r27, hi8(jumpTable)");
	// 0 = OSC1 LFO Mod
	asm("ldi r16, lo8(setOSC1LFOMod)");
	asm("ldi r17, hi8(setOSC1LFOMod)");
	asm("st X+, r16");
	asm("st X+, r17");
	// 1 = OSC1 SH Mod
	asm("ldi r16, lo8(setOSC1SHMod)");
	asm("ldi r17, hi8(setOSC1SHMod)");
	asm("st X+, r16");
	asm("st X+, r17");
	// 2 = OSC1 ENV Mod
	asm("ldi r16, lo8(setOSC1ENVMod)");
	asm("ldi r17, hi8(setOSC1ENVMod)");
	asm("st X+, r16");
	asm("st X+, r17");
	// 4 = OSC2 LFO Mod
	asm("ldi r16, lo8(setOSC2LFOMod)");
	asm("ldi r17, hi8(setOSC2LFOMod)");
	asm("st X+, r16");
	asm("st X+, r17");
	// 5 = OSC2 SH Mod
	asm("ldi r16, lo8(setOSC2SHMod)");
	asm("ldi r17, hi8(setOSC2SHMod)");
	asm("st X+, r16");
	asm("st X+, r17");
	// 6 = OSC2 ENV Mod
	asm("ldi r16, lo8(setOSC2ENVMod)");
	asm("ldi r17, hi8(setOSC2ENVMod)");
	asm("st X+, r16");
	asm("st X+, r17");
}

//message in r25:r24:r23
extern void handleMIDIMessage() {
	// Check for note off
	asm("cpi r25, 0b10000000");
	asm("brne hmmTest1");
	noteTurnedOff();
	asm("ret");
	asm("hmmTest1:");
	// Check for note on
	asm("cpi r25, 0b10010000");
	asm("brne hmmTest2");
	noteTurnedOn();
	asm("ret");
	asm("hmmTest2:");
	// Check for pitch bend
	asm("cpi r25, 0b11100000");
	asm("brne hmmTest3");
	
	asm("ret");
	asm("hmmTest3:");
}
