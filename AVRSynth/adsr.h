
extern const unsigned char adsrDelta[];

// ADSR1
extern unsigned char noteOn;
extern unsigned char adsr1Up;
extern unsigned char adsr1PrevVal;
extern unsigned char adsr1CurrentValue[3];
extern unsigned char adsr1DeltaValue[2];
extern unsigned char adsr1AttackDelta[2];
extern unsigned char adsr1DecayDelta[2];
extern unsigned char adsr1Sustain;
extern unsigned char adsr1ReleaseDelta[2];

unsigned char noteOn;
unsigned char adsr1Up;
unsigned char adsr1PrevVal;
unsigned char adsr1CurrentValue[3];
unsigned char adsr1DeltaValue[2];
unsigned char adsr1AttackDelta[2];
unsigned char adsr1DecayDelta[2];
unsigned char adsr1Sustain;
unsigned char adsr1ReleaseDelta[2];

extern void initADSR1() {
	asm("ldi r25, 127");
	asm("call setADSR1Attack");
	asm("ldi r25, 127");
	asm("call setADSR1Decay");
	asm("ldi r25, 127");
	asm("call setADSR1Release");
	asm("ldi r25, 64");
	asm("call setADSR1Sustain");
}

extern void noteTurnedOn() {
	asm("ldi r16, 1");
	asm("sts noteOn, r16");
	asm("sts adsr1Up, r16");
	asm("lds r16, adsr1AttackDelta");
	asm("lds r17, adsr1AttackDelta + 1");
	asm("sts adsrDeltaValue, r16");
	asm("sts adsrDeltaValue + 1, r17");
}

extern void noteTurnedOff() {
	asm("ldi r16, 0");
	asm("sts noteOn, r16");
	asm("sts adsr1Up, r16");
	asm("lds r16, adsr1ReleaseDelta");
	asm("lds r17, adsr1ReleaseDelta + 1");
	asm("sts adsr1DeltaValue, r16");
	asm("sts adsr1DeltaValue + 1, r17");
}

extern void setADSR1Attack() {
	// val passed in r25
	asm("ldi r26, lo8(adsrDeltaPhase)\n");
	asm("ldi r27, hi8(adsrDeltaPhase)\n");
	asm("lsl r25");
	asm("clr r2");
	asm("add r26, r25");
	asm("adc r27, r2");
	asm("ld r16, X+");
	asm("ld r17, X");
	asm("sts adsr1AttackDelta, r16");
	asm("sts adsr1AttackDelta + 1, r17");
}

extern void setADSR1Decay() {
	// val passed in r25
	asm("ldi r26, lo8(adsrDeltaPhase)\n");
	asm("ldi r27, hi8(adsrDeltaPhase)\n");
	asm("lsl r25");
	asm("clr r2");
	asm("add r26, r25");
	asm("adc r27, r2");
	asm("ld r16, X+");
	asm("ld r17, X");
	asm("sts adsr1DecayDelta, r16");
	asm("sts adsr1DecayDelta + 1, r17");
}

extern void setADSR1Sustain() {
	// val passed in r25
	asm("sts adsr1Sustain, r25");
}

extern void setADSR1Release() {
	// val passed in r25
	asm("ldi r26, lo8(adsrDeltaPhase)\n");
	asm("ldi r27, hi8(adsrDeltaPhase)\n");
	asm("lsl r25");
	asm("clr r2");
	asm("add r26, r25");
	asm("adc r27, r2");
	asm("ld r16, X+");
	asm("ld r17, X");
	asm("sts adsr1ReleaseDelta, r16");
	asm("sts adsr1ReleaseDelta + 1, r17");
}


void updateADSR1() {
	// load portDVal in r25
	asm("lds r25, portDVal");
	// clear adsr bits
	asm("andi r25, 0b00111111");
	asm("lds r16, noteOn");
	asm("cpi r16, 0");
	asm("brne noteIsOn");
	/////////////////////////////////////
	// RELEASE
	/////////////////////////////////////
	asm("lds r16, adsr1CurrentValue + 2");
	asm("cpi r16, 0");
	asm("breq releaseFinished");
	//Y = oscMasterData
	asm("ldi r28, lo8(adsr1CurrentValue)\n");
	asm("ldi r29, hi8(adsr1CurrentValue)\n");
	// Load adsr current value into r18:r17:r16
	asm("ld r16, Y+");
	asm("ld r17, Y+");
	asm("ld r18, Y+");
	// store current value in r22
	asm("mov r22, r18");
	// Load deltaValue into r21:r20:r19
	asm("lds r19, adsr1DeltaValue");
	asm("lds r20, adsr1DeltaValue + 1");
	asm("clr r21");
	// subtract
	asm("sub r16, r19");
	asm("sbc r17, r20");
	asm("sbc r18, r21");
	// store new value
	asm("st -Y, r18");
	asm("st -Y, r17");
	asm("st -Y, r16");
	// FOR DEBUGGING
	asm("sts adsr1PrevVal, r18");
	// compare prev value to new value
	asm("cp r22, r21");
	asm("breq releaseFinished");
	// set subtract bit high
	asm("ori r25, 0b01000000");
	asm("releaseFinished:");
	// update portBVal
	asm("sts portBVal, r25");
	// release finished
	asm("jmp updateEnvelopeFinished");
	asm("noteIsOn:");
	asm("lds r16, adsr1Up");
	asm("cpi r16, 0");
	asm("brne envelopeDecreasing");
	////////////////////////////////////////
	// ATTACK
	////////////////////////////////////////
	//Y = oscMasterData
	asm("ldi r28, lo8(adsr1CurrentValue)\n");
	asm("ldi r29, hi8(adsr1CurrentValue)\n");
	// Load adsr current value into r18:r17:r16
	asm("ld r16, Y+");
	asm("ld r17, Y+");
	asm("ld r18, Y+");
	// store current value in r22
	asm("mov r22, r18");
	// Load deltaValue into r21:r20:r19
	asm("lds r19, adsr1DeltaValue");
	asm("lds r20, adsr1DeltaValue + 1");
	asm("clr r21");
	// add
	asm("add r16, r19");
	asm("adc r17, r20");
	asm("adc r18, r21");
	// store new value
	asm("st -Y, r18");
	asm("st -Y, r17");
	asm("st -Y, r16");
	// FOR DEBUGGING
	asm("sts adsr1PrevVal, r18");
	// compare prev value to new value
	asm("cp r22, r21");
	asm("breq checkMaxAttack");
	// set add bit high
	asm("ori r25, 0b10000000");
	asm("checkMaxAttack:");
	//see if were at max value
	asm("cpi r18, 127");
	asm("brlo attackFinished");
	// if so set adsr1Up to 0
	asm("clr r2");
	asm("sts adsr1Up, r2");
	// load decay delta into envelope delta
	asm("lds r16, adsr1DecayDelta");
	asm("lds r17, adsr1DecayDelta + 1");
	asm("sts adsr1DeltaValue, r16");
	asm("sts adsr1DeltaValue + 1, r17");
	asm("attackFinished:");
	// update portBVal
	asm("sts portBVal, r25");
	// release finished
	asm("jmp updateEnvelopeFinished");
	asm("envelopeDecreasing:");
	///////////////////////////////////////
	// SUSTAIN
	///////////////////////////////////////
	asm("lds r16, adsr1CurrentValue + 2");
	asm("lds r17, adsr1Sustain");
	// NOTE: we are testing if sustain value is lower than current value
	asm("cp r17, r16");
	asm("brlo updateEnvelopeDecay");
	// update portBVal
	asm("sts portBVal, r25");
	// sustain finished
	asm("jmp updateEnvelopeFinished");
	///////////////////////////////////////
	// DECAY
	///////////////////////////////////////
	asm("updateEnvelopeDecay:");
	//Y = oscMasterData
	asm("ldi r28, lo8(adsr1CurrentValue)\n");
	asm("ldi r29, hi8(adsr1CurrentValue)\n");
	// Load adsr current value into r18:r17:r16
	asm("ld r16, Y+");
	asm("ld r17, Y+");
	asm("ld r18, Y+");
	// store current value in r22
	asm("mov r22, r18");
	// Load deltaValue into r21:r20:r19
	asm("lds r19, adsr1DeltaValue");
	asm("lds r20, adsr1DeltaValue + 1");
	asm("clr r21");
	// subtract
	asm("sub r16, r19");
	asm("sbc r17, r20");
	asm("sbc r18, r21");
	// store new value
	asm("st -Y, r18");
	asm("st -Y, r17");
	asm("st -Y, r16");
	// FOR DEBUGGING
	asm("sts adsr1PrevVal, r18");
	// update portBVal
	asm("sts portBVal, r25");
	asm("updateEnvelopeFinished:");
}
const unsigned long adsrDeltaPhase[] PROGMEM = 
{68, 0, 71, 0, 75, 0, 79, 0, 84, 0, 89, 0, 93, 0, 99, 0, 104, 0, 
110, 0, 116, 0, 123, 0, 129, 0, 137, 0, 144, 0, 152, 0, 161, 0, 
170, 0, 179, 0, 189, 0, 200, 0, 211, 0, 222, 0, 235, 0, 248, 0, 
6, 1, 20, 1, 36, 1, 52, 1, 69, 1, 87, 1, 106, 1, 126, 1, 
147, 1, 170, 1, 194, 1, 219, 1, 245, 1, 17, 2, 46, 2, 77, 2, 
110, 2, 145, 2, 181, 2, 220, 2, 5, 3, 48, 3, 93, 3, 141, 3, 
192, 3, 245, 3, 45, 4, 105, 4, 168, 4, 234, 4, 48, 5, 122, 5, 
200, 5, 26, 6, 113, 6, 205, 6, 46, 7, 148, 7, 0, 8, 114, 8, 
234, 8, 105, 9, 239, 9, 125, 10, 18, 11, 176, 11, 86, 12, 6, 13, 
192, 13, 132, 14, 82, 15, 45, 16, 19, 17, 6, 18, 7, 19, 22, 20, 
52, 21, 98, 22, 161, 23, 242, 24, 85, 26, 204, 27, 88, 29, 250, 30, 
179, 32, 133, 34, 113, 36, 120, 38, 156, 40, 222, 42, 65, 45, 198, 47, 
110, 50, 61, 53, 51, 56, 84, 59, 161, 62, 29, 66, 203, 69, 173, 73, 
198, 77, 26, 82, 172, 86, 126, 91, 150, 96, 246, 101, 162, 107, 159, 113, 
242, 119, 159, 126, 170, 133, 26, 141, 244, 148, 62, 157, 254, 165, 59, 175, 
251, 184, 70, 195, 36, 206, 156, 217, 184, 229, 129, 242, 255, 255};