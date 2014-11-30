
#include "Arduino.h"
/*
#include "binary.h"
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

const float octave[] = {63.05, 126.1, 252.2, 504.4};
const float scale[] = {1.0, 17.0 / 16.0, 9.0 / 8.0, 6.0 / 5.0, 5.0 / 4.0, 4.0 / 3.0, 11.0 / 8.0, 3.0 / 2.0, 13.0 / 8.0, 27.0 / 16.0, 7.0 / 4.0, 15.0 / 8.0};
int scaleIndex = 0;
// Arrays not used for faster speed
// Set to false when OSCxFullCount is being updated in case of interrupt
volatile unsigned int timer2CountStart = 16;
volatile unsigned int timer2Count = 0;
volatile unsigned char numTimer2Overflows = 0;
volatile bool OSC1CountUpdated = true;
volatile bool OSC2CountUpdated = true;
volatile bool OSC3CountUpdated = true;
volatile bool OSC4CountUpdated = true;
volatile unsigned int OSC1CountOld = 0;
volatile unsigned int OSC2CountOld = 0;
volatile unsigned int OSC3CountOld = 0;
volatile unsigned int OSC4CountOld = 0;
volatile unsigned int OSC1Count = 0;
volatile unsigned int OSC2Count = 0;
volatile unsigned int OSC3Count = 0;
volatile unsigned int OSC4Count = 0;
volatile unsigned int OSC1CountNext = 0;
volatile unsigned int OSC2CountNext = 0;
volatile unsigned int OSC3CountNext = 0;
volatile unsigned int OSC4CountNext = 0;
volatile bool OSC1Phase = false;
volatile bool OSC2Phase = false;
volatile bool OSC3Phase = false;
volatile bool OSC4Phase = false;
bool OSC1Active = false;
bool OSC2Active = false;
bool OSC3Active = false;
bool OSC4Active = false;
#define OSC1PIN 2
#define OSC2PIN 3
#define OSC3PIN 4
#define OSC4PIN 5
volatile bool nextBeat = false;
volatile bool timer2InterruptInProgress = false;
void initTimer1(long microseconds);
void initTimer2(long cycles);
void setFrequency1(float freq);
void setFrequency2(float freq);
void setFrequency3(float freq);
void setFrequency4(float freq);

void setup()
{
  pinMode(13, OUTPUT);
  DDRD = DDRD | B11111100;  // sets pins 2 to 7 as outputs
  setFrequency1(octave[0] * scale[scaleIndex]);
  setFrequency2(octave[1] * scale[scaleIndex]);
  setFrequency3(octave[2] * scale[scaleIndex]);
  setFrequency4(octave[3] * scale[scaleIndex]);
  initTimer1(256);
  initTimer2(256);
  TIMSK0 &= ~_BV(TOIE0); // disable timer0 overflow interrupt
  scaleIndex = 0;
}

void loop() {
	if(nextBeat) {
		nextBeat = false;
		setFrequency1(octave[0] * scale[scaleIndex]);
		setFrequency2(octave[1] * scale[scaleIndex]);
		setFrequency3(octave[2] * scale[scaleIndex]);
		setFrequency4(octave[3] * scale[scaleIndex]);
		scaleIndex = (scaleIndex + 1) % 12;
		//delay(1000);
	}
}

// Timer 1 Code:

void initTimer1(long cycles) {
	  TCCR1A = 0;                 // clear control register A
	  TCCR1B = _BV(WGM13);        // set mode 8: phase and frequency correct pwm, stop the timer
	                          // the counter runs backwards after TOP, interrupt is at BOTTOM so divide microseconds by 2
	  unsigned char oldSREG = SREG;
	  cli();							// Disable interrupts for 16 bit register access
	  ICR1 = cycles;                    // ICR1 is TOP in p & f correct pwm mode
	  SREG = oldSREG;
	  TIMSK1 = _BV(TOIE1);
	  TCCR1B &= ~(_BV(CS10) | _BV(CS11) | _BV(CS12));
	  TCCR1B |= _BV(CS12);
}


void updateTimer1(long cycles) {
	  unsigned char oldSREG = SREG;
	  cli();							// Disable interrupts for 16 bit register access
	  ICR1 = cycles;                    // ICR1 is TOP in p & f correct pwm mode
	  SREG = oldSREG;
}

ISR(TIMER2_OVF_vect)          // interrupt service routine that wraps a user defined function supplied by attachInterrupt
{
	timer2Count--;
	if(timer2Count == 0) {
		nextBeat = true;
		timer2Count = 16;
	}
}

// Timer 2 Code:

void setFrequency1(float freqInHz) {
	long cycles = round((1.0 / freqInHz) * 16000000.0);
	cycles >>= 6;
	OSC1CountUpdated = false;
	OSC1CountNext = cycles;
	OSC1CountUpdated = true;
	OSC1CountOld = cycles;
	OSC1Active = true;
}

void setFrequency2(float freqInHz) {
	long cycles = round((1.0 / freqInHz) * 16000000.0);
	cycles >>= 6;
	OSC2CountUpdated = false;
	OSC2CountNext = cycles;
	OSC2CountUpdated = true;
	OSC2CountOld = cycles;
	OSC2Active = true;
}

void setFrequency3(float freqInHz) {
	long cycles = round((1.0 / freqInHz) * 16000000.0);
	cycles >>= 6;
	OSC3CountUpdated = false;
	OSC3CountNext = cycles;
	OSC3CountUpdated = true;
	OSC3CountOld = cycles;
	OSC3Active = true;
}

void setFrequency4(float freqInHz) {
	long cycles = round((1.0 / freqInHz) * 16000000.0);
	cycles >>= 6;
	OSC4CountUpdated = false;
	OSC4CountNext = cycles;
	OSC4CountUpdated = true;
	OSC4CountOld = cycles;
	OSC4Active = true;
}

void initTimer2(long cycles) {

	/* First disable the timer overflow interrupt while we're configuring */
	TIMSK2 &= ~(1<<TOIE2);
	/* Configure timer2 in normal mode (pure counting, no PWM etc.) */
	TCCR2A &= ~((1<<WGM21) | (1<<WGM20));
	TCCR2B &= ~(1<<WGM22);
	/* Select clock source: internal I/O clock */
	ASSR &= ~(1<<AS2);
	/* Disable Compare Match A interrupt enable (only want overflow) */
	TIMSK2 &= ~(1<<OCIE2A);
	/* Now configure the prescaler to CPU clock divided by 64 */
	TCCR2B |= (1<<CS21) | (1<<CS20)| (1<<CS22); // | (1<<CS20); // Set Bit
	//TCCR2B &= ~(1<<CS22); // Clear Bit
	/* Finally load end enable the timer */
	TCNT2 = 0;
	TIMSK2 |= (1<<TOIE2);
	timer2Count = 16;
}

// Called when timer 1 overflows
ISR(TIMER1_OVF_vect) {
	unsigned int minCycles = OSC1Count;
	if(OSC2Count < minCycles) minCycles = OSC2Count;
	if(OSC3Count < minCycles) minCycles = OSC3Count;
	if(OSC4Count < minCycles) minCycles = OSC4Count;
	if(minCycles > 0) {
		OSC1Count -= minCycles;
		OSC2Count -= minCycles;
		OSC3Count -= minCycles;
		OSC4Count -= minCycles;
		//TIMSK2 &= ~(1<<TOIE2);
		ICR1 = minCycles;
		TCNT1 = minCycles;
		//TIMSK2 &= (1<<TOIE2);
		return;
	} else {
		if(OSC1Count == 0) {
			OSC1Phase = !OSC1Phase;
			if(OSC1Phase) {
				bitSet(PORTD, OSC1PIN);
			} else {
				bitClear(PORTD, OSC1PIN);
			}
			if(OSC1CountUpdated) {
				OSC1Count = OSC1CountNext; // Account for current TCNT != 0
			} else {
				OSC1Count = OSC1CountOld; // Account for current TCNT != 0
			}
		}
		if(OSC2Count == 0) {
			OSC2Phase = !OSC2Phase;
			if(OSC2Phase) {
				bitSet(PORTD, OSC2PIN);
			} else {
				bitClear(PORTD, OSC2PIN);
			}
			if(OSC2CountUpdated) {
				OSC2Count = OSC2CountNext; // Account for current TCNT != 0
			} else {
				OSC2Count = OSC2CountOld; // Account for current TCNT != 0
			}
		}
		if(OSC3Count == 0) {
			OSC3Phase = !OSC3Phase;
			if(OSC3Phase) {
				bitSet(PORTD, OSC3PIN);
			} else {
				bitClear(PORTD, OSC3PIN);
			}
			if(OSC3CountUpdated) {
				OSC3Count = OSC3CountNext; // Account for current TCNT != 0
			} else {
				OSC3Count = OSC3CountOld; // Account for current TCNT != 0
			}
		}
		if(OSC4Count == 0) {
			OSC4Phase = !OSC4Phase;
			if(OSC4Phase) {
				bitSet(PORTD, OSC4PIN);
			} else {
				bitClear(PORTD, OSC4PIN);
			}
			if(OSC4CountUpdated) {
				OSC4Count = OSC4CountNext; // Account for current TCNT != 0
			} else {
				OSC4Count = OSC4CountOld; // Account for current TCNT != 0
			}
		}
		minCycles = OSC1Count;
		if(OSC2Count < minCycles) minCycles = OSC2Count;
		if(OSC3Count < minCycles) minCycles = OSC3Count;
		if(OSC4Count < minCycles) minCycles = OSC4Count;
		OSC1Count -= minCycles;
		OSC2Count -= minCycles;
		OSC3Count -= minCycles;
		OSC4Count -= minCycles;
		//TIMSK2 &= ~(1<<TOIE2);
		ICR1 = minCycles;
		TCNT1 = minCycles;
		//TIMSK2 &= (1<<TOIE2);
	}
}
