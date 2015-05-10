/*
 * AVRSynth.c
 *
 * Created: 5/4/2015 4:46:10 AM
 *  Author: Glenn
 */ 

/*
 * ArduinoSynth.cpp
 *
 * Created: 5/2/2015 12:01:13 AM
 *  Author: Glenn
 */ 

#include "pgmspace.h"
#include "binary.h"
#include "frequencies.h"
#include "oscillator.h"
#include "adsr.h"
#include "lfo.h"

/*
#include <binary.h>
#include "Client.h"
#include "HardwareSerial.h"
#include "IPAddress.h"
#include "new.h"
#include "pins_arduino.h"
#include "Platform.h"
#include "Print.h"
#include "Printable.h"
#include "Server.h"
#include "Stream.h"
#include "Udp.h"
#include "USBAPI.h"
#include "USBCore.h"
#include "USBDesc.h"
#include "WCharacter.h"
#include "wiring_private.h"
#include "WString.h"
*/
// INO Starts HERE

//#include "eeprom.h"

#include "io.h"
#include "interrupt.h"

#define minMidiNote 12
#define maxMidiNote 84
#define setOsc1ResetOn B00000100
#define setOsc1ResetOff B11111011
#define setOsc1On B00001000
#define setOsc1Off B11110111
volatile char nextSample = 1;
char noteOn = 1;
char loopEnvelope = 0;
unsigned char sampleCount = 0;
long currentTimeInMillis = 0;

extern void *sampleGenerator;

unsigned char adcRead() {
	/*
	asm("ldi r19,0\n"); // Select ADC0
	asm("out ADMUX, r19\n"); //  Enable ADC, Single Mode conversion
	asm("ldi r19, 1000000b\n"); //  ADC Interrupt disable, Prescaler division factor = 2
	asm("out ADCSR,r19\n"); // this gives an ADC clock frequency of 16MHz/2 = 8MHz.
	asm("sbi ADCSR,ADSC\n"); //  Start conversion
	asm("Wait\n");
	asm("sbis ADCSR,ADIF\n");
	asm("rjmp Wait:\n");
	asm("in r19,ADCH\n");
	*/
	return 1;
}

unsigned char serialBuffer[] = {0, 0, 0, 0, 0};
int serialBufferIndex = 0;
int serialDataReady = 0;
unsigned char command;
unsigned char data1;
unsigned char data2;

#define F_CPU 16000000	// 16 MHz oscillator.
#define BaudRate 31500
#define MYUBRR (F_CPU / 16 / BaudRate ) - 1

void initSerial() {
	//Serial Initialization
	/*Set baud rate */
	UBRR0H = (unsigned char)((MYUBRR)>>8);
	UBRR0L = (unsigned char) MYUBRR;
	/* Enable receiver and transmitter   */
	UCSR0C = _BV(UCSZ01) | _BV(UCSZ00); /* 8-bit data */
	UCSR0B = _BV(RXEN0) | _BV(TXEN0);   /* Enable RX and TX */
}

unsigned char serialHasData() {
	return( UCSR0A & _BV(RXC0)) ;		// nonzero if serial data is available to read.
}

unsigned char serialGetData() {
	return UDR0;
}

int serialReadIntoBuffer() {
	serialBuffer[serialBufferIndex] = UDR0;
	return serialBufferIndex++;
}

unsigned char serialReadOutOfBuffer() {
	serialBufferIndex--;
	return serialBuffer[serialBufferIndex];
}


unsigned char serialWriteReady() {
	return( UCSR0A & _BV(UDRE0) ) ;		// nonzero if transmit register is ready to receive new data.
}

void serialWrite(unsigned char DataOut)
{
	UDR0 = DataOut;
}

void initTimer2() {
	/* First disable the timer overflow interrupt while we're configuring */
	TIMSK2 &= ~(1<<TOIE2);
	/* Configure timer2 in normal mode (pure counting, no PWM etc.) */
	TCCR2A &= ~((1<<WGM21) | (1<<WGM20));
	TCCR2B &= ~(1<<WGM22);
	/* Select clock source: internal I/O clock */
	ASSR &= ~(1<<AS2);
	/* Disable Compare Match A interrupt enable (only want overflow) */
	TIMSK2 &= ~(1<<OCIE2A);
	/* Now configure the prescaler to CPU clock divided by 128 */
	TCCR2B |= (1<<CS22) | (1<<CS20); // Set Bits
	TCCR2B &= ~(1<<CS21); // Clear Bits
	/* Finally load end enable the timer */
	//TCNT2 = 255;
	TCNT2 = 0;
	TIMSK2 |= (1<<TOIE2);
}

int setDeltaPhaseIndex(int note, int oscIndex) {
	if(note >= minMidiNote && note <= maxMidiNote) {
		if(oscIndex == 0) {
			osc1DeltaPhaseIndex = note * 64 * 3 - 9;
		}
	}
}

void updateEnvelope() {
	if(!noteOn) {
		cli();
		adsr1up = 0;
		adsr1DeltaValue[0] = adsrDelta[adsr1ReleaseTime * 2];
		adsr1DeltaValue[1] = adsrDelta[adsr1ReleaseTime * 2 + 1];
		sei();
		return;
	}
	if(currentTimeInMillis == 0) {
		cli();
		adsr1up = 1;
		adsr1DeltaValue[0] = adsrDelta[adsr1AttackTime * 2];
		adsr1DeltaValue[1] = adsrDelta[adsr1AttackTime * 2 + 1];
		adsr1CurrentValue[0] = 0;
		adsr1CurrentValue[1] = 0;
		adsr1CurrentValue[2] = 0;
		sei();
		return;
	}
	if(adsr1up) {
		cli();
		adsr1up = 0;
		adsr1DeltaValue[0] = adsrDelta[adsr1DecayTime * 2];
		adsr1DeltaValue[1] = adsrDelta[adsr1DecayTime * 2 + 1];
		sei();
	}
	if(adsr1PrevValue == 0) {
		if(loopEnvelope) {
			cli();
			adsr1up = 1;
			adsr1DeltaValue[0] = adsrDelta[adsr1AttackTime * 2];
			adsr1DeltaValue[1] = adsrDelta[adsr1AttackTime * 2 + 1];
			adsr1CurrentValue[0] = 0;
			adsr1CurrentValue[1] = 0;
			adsr1CurrentValue[2] = 0;
			sei();
			return;
		} else {
			cli();
			adsr1DeltaValue[0] = 0;
			adsr1DeltaValue[1] = 0;
			sei();
			return;
		}
	}
	if(adsr1PrevValue <= adsr1Sustain) {
		if(loopEnvelope) {
			cli();
			adsr1up = 0;
			adsr1DeltaValue[0] = adsrDelta[adsr1ReleaseTime * 2];
			adsr1DeltaValue[1] = adsrDelta[adsr1ReleaseTime * 2 + 1];
			sei();
		}
	}
}

void updateLFO() {
	/*
	X = lfoDeltaValue;
	Y = lfo1currentValue;
	Z = lfoup;
	if(lfo1up) {
		(add) lfo1CurrentValue[0] += lfoDeltaValue[0];
		(adc) lfo1CurrentValue[1] += lfoDeltaValue[1];
		(adc) lfo1CurrentValue[2] += r2(0)
		if(LFOCurrentValue >= 128) {
			lfoUp = 0;
		}
	} else {
		(sub) lfo1CurrentValue[0] -= lfoDeltaValue[0];
		(adc) lfo1CurrentValue[1] -= lfoDeltaValue[1];
		(adc) lfo1CurrentValue[2] -= r2(0)
		if(LFOCurrentValue == 0) {
			lfoUp = 1;
		}
	}
	if(LFOCurrentValue[2] == LFOPrevValue) {
		PORTB |= B0000000;
		return;
	}
	*/
}


void updateLFOVal(int lfoVal) {
	cli();
	lfo1Delta[0] = lfo1Delta[lfoVal * 2];
	lfo1Delta[1] = lfo1Delta[lfoVal * 2 + 1];
	sei();
}

int blink = 0;
//int testData = 0;
// This is needed to fool the optimizer
//int garbage = 0;

// Called when timer 2 overflows
ISR(TIMER2_OVF_vect) {
	//nextSample = 1;
	//TCNT2 = 255;
	//serialWrite(127);
	/*
	if(blink < 100) {
		PORTB = B00111111;
		blink++;
	} else {
		PORTB = B00000000;
		blink++;
		if(blink > 200) blink = 0;
	}
	nextSample = 1;
	sampleCount++;
	if(sampleCount == 128) {
		currentTimeInMillis++;
		sampleCount = 0;
	}
	while(!serialWriteReady()){};
	serialWrite(portBVal);
	//while(!serialWriteReady()){};
	//serialWrite(oscMasterData[7]);
	//testData++;
	// This is needed to fool the optimizer
	//garbage += deltaPhase[testData];
	*/
	asm("ldi r16, 1\n");
	asm("sts nextSample, r16\n");
	TCNT2 = 255;
	//while(!serialWriteReady()){};
	//serialWrite(portBVal);
	//TCNT2 = 255;
}

void initOscMasterData() {
	// 0 osc1LFOMod
	oscMasterData[0] = 64 * 3;
	// 1 osc1SHMod
	oscMasterData[1] = 64 * 3;
	// 2 osc1ENVMod
	oscMasterData[2] = 64 * 3;
	// 3 - 5 osc1CurrentValue
	oscMasterData[3] = 0;
	oscMasterData[4] = 0;
	oscMasterData[5] = 0;
	// 6 osc1PrevValue
	oscMasterData[6] = 128;
	oscMasterData[7] = 64;
	oscMasterData[8] = 32;
}

int main() {
	
	// Setup starts here
	DDRD = DDRD | B11111110;  // sets pins 2 to 7 as outputs
	DDRB = DDRB | B00111111;  // sets pins 8 to 13 as outputs
	//TIMSK0 &= ~_BV(TOIE0); // disable timer0 overflow interrupt
	initSerial();
	initTimer2();
	// Setup ends here
	sei();
	adsr1AttackTime = 10;
	adsr1DecayTime = 10;
	adsr1Sustain = 64;
	adsr1ReleaseTime = 256;
	setDeltaPhaseIndex(72, 0);
	updateLFOVal(128);
	updateEnvelope();
	sh1NextValue = 64;
	sh1CurrentValue = 64;
	sh1rate = 10000;
	sh1currentCount = 0;
	initOscMasterData();
	asm("infinite:");
			asm("lds r16, nextSample\n");
			asm("cpi r16, 1\n");
			asm("brne infinite\n");
			/*
			// START OSC1
			*/
			//serialWrite(portBVal);
			cli();
			asm("clr r25\n");
			//X = &osc1DeltaPhaseIndex;
			asm("ldi r26, lo8(osc1DeltaPhaseIndex)\n");
			asm("ldi r27, hi8(osc1DeltaPhaseIndex)\n");
			// osc1DeltaPhaseIndex in r5:r4
			asm("ld r4, X+\n");
			asm("ld r5, X+\n");
			//Y = oscMasterData (starting at osc1LFOMod)
			asm("ldi r28, lo8(oscMasterData)\n");
			asm("ldi r29, hi8(oscMasterData)\n");
			// Put 0 in r2
			asm("clr r2\n");
			//Y = osc1LFOMod
			asm("ld r3, Y+\n");
			//asm("add r4, r3");
			//asm("adc r5, r2");
			//Y = osc1SHMod
			asm("ld r3, Y+\n");
			//asm("add r4, r3");
			//asm("adc r5, r2");
			//Y = osc1ENVMod
			asm("ld r3, Y+\n");
			//asm("add r4, r3");
			//asm("adc r5, r2");
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
			// Load osc1CurrentValue into r21:r20:r19
			asm("ld r19, Y+\n");
			asm("ld r20, Y+\n");
			asm("ld r21, Y+\n");
			// store current value in r25
			asm("mov r24, r21");
			// Add deltaPhase to osc1CurrentValue
			asm("add r19, r16\n");
			asm("adc r20, r17\n");
			asm("adc r21, r18\n");
			// test for max value
			asm("cpi r21, 128\n");
			asm("brlo skipReset\n");
			// if maxValue reset and set integerator reset pin high
			asm("clr r19\n");
			asm("clr r20\n");
			asm("clr r21\n");
			asm("ori r25, 0b00100000\n");
			// Update osc1 current value
			asm("skipReset:\n");
			asm("st -Y, r21\n");			
			asm("st -Y, r20\n");
			asm("st -Y, r19\n");
			// compare to prev value to currentValue
			// NOTE use of BRSH (Branch if same or higher)
			// prevVal will be greater than current val if 128 is reached
			asm("cp r24, r21\n");
			asm("brsh skipSetOutput\n");
			asm("ori r25, 0b00010000\n");
			asm("skipSetOutput:\n");
			// store port b in portB
			asm("ldi r26, lo8(portBVal)\n");
			asm("ldi r27, hi8(portBVal)\n");
			asm("st X, r25");
			//PORTB = r26;
			// output to port B
			PORTB = portBVal;
			sei();
		asm("ldi r16, 0\n");
		asm("sts nextSample, r16\n");
		while(serialHasData()) {
			if(serialReadIntoBuffer() == 3) {
				command = serialReadOutOfBuffer();
				data1 = serialReadOutOfBuffer();
				data2 = serialReadOutOfBuffer();
				serialDataReady = 3;
				break;
			}
		}
	asm("jmp infinite\n");
	return 0;
}
