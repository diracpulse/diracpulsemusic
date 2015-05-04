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
char nextSample = 1;
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
#define BaudRate 9600
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
	TCNT2 = 255;
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

void updateLFO(int lfoVal) {
	cli();
	lfo1Delta[0] = lfo1Delta[lfoVal * 2];
	lfo1Delta[1] = lfo1Delta[lfoVal * 2 + 1];
	sei();
}

int blink = 0;

// Called when timer 2 overflows
ISR(TIMER2_OVF_vect) {
	//nextSample = 1;
	//TCNT2 = 255;
	//serialWrite(127);
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
	setDeltaPhaseIndex(60, 0);
	updateLFO(128);
	updateEnvelope();
	sh1NextValue = 64;
	sh1CurrentValue = 64;
	sh1rate = 10000;
	sh1currentCount = 0;
	while(1) {
		if(nextSample) {
			// Load &osc1CDeltaPhaseIndex into Z
			asm("ldi r30, lo8(osc1DeltaPhaseIndex)\n");
			asm("ldi r31, hi8(osc1DeltaPhaseIndex)\n");
			/*
			// START add vibrato from lf01
			*/
			// Load &lfo1Value into Y
			asm("ldi r28, lo8(lfo1CurrentValue)\n");
			asm("ldi r29, hi8(lfo1CurrentValue)\n");
			// Load current lfo1 value into r16
			asm("ldd r16, Y+2\n");
			/// Load &osc1lfoAmt into into Y
			asm("ldi r28, lo8(osc1lfoAmt)\n");
			asm("ldi r29, lo8(osc1lfoAmt)\n");
			// Load osc1lfoAmt into r17
			asm("ld r17, Y\n");
			// calculate currentlfoVal * osc1lfoAmt
			asm("mul r16, r17\n");
			// mov high byte into r2
			asm("mov r2, r1\n");
			// multiply by 3 since there are 3 bytes per deltaPhase value
			asm("ldi r16, 3\n");
			asm("mul r2, r16\n");
			// finally add to deltaPhaseIndex (Z)
			asm("add r30, r0\n");
			asm("adc r31, r1\n");
			// now add the static amount
			asm("ldi r16, 0x40\n");
			/// Load &osc1lfoAmt from Y (Y should be unchanged)
			asm("ld r17, Y\n");
			// get the inverse of osc1lfoAmt
			asm("ldi r18, 0xff\n");
			asm("sub r18, r17\n");
			// calculate 64 * (1 - osc1lfoAmt)
			asm("mul r16, r18\n");
			// mov high byte into r2
			asm("mov r2, r1\n");
			// multiply by 3 since there are 3 bytes per deltaPhase value
			asm("ldi r16, 3\n");
			asm("mul r2, r16\n");
			// finally add to deltaPhaseIndex (Z)
			asm("add r30, r0\n");
			asm("adc r31, r1\n");
			/*
			// END add add vibrato from lf01
			*/
			// Load deltaPhase from Z
			asm("lpm r16, Z+\n");
			asm("lpm r17, Z+\n");
			asm("lpm r18, Z+\n");
			// Load &osc1CurrentValue into Y
			asm("ldi r28, lo8(osc1CurrentValue)\n");
			asm("ldi r29, hi8(osc1CurrentValue)\n");
			// Load osc1CurrentValue into r21:r20:r19
			asm("ld r19, Y+\n");
			asm("ld r20, Y+\n");
			asm("ld r21, Y+\n");
			// Add deltaPhase to osc1CurrentValue
			asm("add r16, r19\n");
			asm("adc r17, r20\n");
			asm("adc r18, r21\n");
			// Update osc1 current value
			asm("st -Y, r18\n");
			asm("st -Y, r17\n");
			asm("st -Y, r16\n");
			// Load &osc1PrevValue into Y
			asm("ldi r28, lo8(osc1PrevValue)\n");
			asm("ldi r29, hi8(osc1PrevValue)\n");
			// stor currentValue into prevValue
			asm("st Y, r18\n"); 
		}
		while(serialHasData()) {
			if(serialReadIntoBuffer() == 3) {
				command = serialReadOutOfBuffer();
				data1 = serialReadOutOfBuffer();
				data2 = serialReadOutOfBuffer();
				serialDataReady = 3;
				break;
			}
		}
		while(!serialWriteReady()){};
		serialWrite(osc1PrevValue);
	}
}
