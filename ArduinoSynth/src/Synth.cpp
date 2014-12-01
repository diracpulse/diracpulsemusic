
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

const float octave[] = {44.0, 126.1, 252.2, 4400.0};
const float scale[] = {1.0, 9.0 / 8.0, 5.0 / 4.0, 4.0 / 3.0, 3.0 / 2.0, 5.0 / 3.0, 15.0 / 8.0, 2.0};
int scaleIndex = 0;
// Arrays not used for faster speed
// Set to false when OSCxFullCount is being updated in case of interrupt
volatile unsigned int timer2CountStart = 16;
volatile unsigned int timer2Count = 0;
volatile unsigned char numTimer2Overflows = 0;
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
  initTimer1(500000);
  initTimer2(256);
  //TIMSK0 &= ~_BV(TOIE0); // disable timer0 overflow interrupt
  scaleIndex = 0;
}

void loop() {
	if(nextBeat) {
		nextBeat = false;
		setFrequency1(octave[0] * scale[scaleIndex]);
		setFrequency2(octave[1] * scale[scaleIndex]);
		setFrequency3(octave[2] * scale[scaleIndex]);
		setFrequency4(octave[3] * scale[scaleIndex]);
		scaleIndex = (scaleIndex + 1) % 8;
		//delay(1000);
	}
}

// Timer 1 Code:

void initTimer1(long microseconds) {
	  TCCR1A = 0;                 // clear control register A
	  TCCR1B = _BV(WGM13);        // set mode 8: phase and frequency correct pwm, stop the timer
	  long cycles = microseconds >> 6; //int the counter runs backwards after TOP, interrupt is at BOTTOM so divide microseconds by 2
	  unsigned char oldSREG = SREG;
	  cli();							// Disable interrupts for 16 bit register access
	  ICR1 = cycles;                    // ICR1 is TOP in p & f correct pwm mode
	  SREG = oldSREG;
	  TIMSK1 = _BV(TOIE1);
	  TCCR1B &= ~(_BV(CS10) | _BV(CS11) | _BV(CS12));
	  TCCR1B |= _BV(CS10) | _BV(CS12);
}

void updateTimer1(long cycles) {
	  unsigned char oldSREG = SREG;
	  cli();							// Disable interrupts for 16 bit register access
	  ICR1 = cycles;                    // ICR1 is TOP in p & f correct pwm mode
	  SREG = oldSREG;
}

ISR(TIMER1_OVF_vect)          // interrupt service routine that wraps a user defined function supplied by attachInterrupt
{
	nextBeat = true;
}

// Timer 2 Code:

void setFrequency1(float freqInHz) {
	long cycles = round((1.0 / freqInHz) * 16000000.0);
	cycles >>= 3;
	cli();							// Disable interrupts for 16 bit write
	OSC1CountNext = cycles;
	sei();
	OSC1Active = true;
}

void setFrequency2(float freqInHz) {
	long cycles = round((1.0 / freqInHz) * 16000000.0);
	cycles >>= 3;
	cli();							// Disable interrupts for 16 bit write
	OSC2CountNext = cycles;
	sei();
	OSC2Active = true;
}

void setFrequency3(float freqInHz) {
	long cycles = round((1.0 / freqInHz) * 16000000.0);
	cycles >>= 3; // Timer2
	cli();							// Disable interrupts for 16 bit write
	OSC3CountNext = cycles;
	sei();
	OSC3Active = true;
}

void setFrequency4(float freqInHz) {
	long cycles = round((1.0 / freqInHz) * 16000000.0);
	cycles >>= 3;
	cli();							// Disable interrupts for 16 bit write
	OSC4CountNext = cycles;
	sei();
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
	/* Now configure the prescaler to CPU clock divided by 8 */
	TCCR2B |= (1<<CS21); // Set Bit
	TCCR2B &= ~((1<<CS22) | (1<<CS20)); // Clear Bits
	/* Finally load end enable the timer */
	TCNT2 = 0;
	TIMSK2 |= (1<<TOIE2);
}

// Called when timer 2 overflows
ISR(TIMER2_OVF_vect) {
	unsigned int minCycles = OSC1Count;
	if(OSC2Count < minCycles) minCycles = OSC2Count;
	if(OSC3Count < minCycles) minCycles = OSC3Count;
	if(OSC4Count < minCycles) minCycles = OSC4Count;
	if(minCycles > 255) {
		OSC1Count -= 256;
		OSC2Count -= 256;
		OSC3Count -= 256;
		OSC4Count -= 256;
		TCNT2 = 0;
		return;
	}
	if(minCycles > 0) {
		OSC1Count -= minCycles;
		OSC2Count -= minCycles;
		OSC3Count -= minCycles;
		OSC4Count -= minCycles;
		TCNT2 = 256 - minCycles;
		return;
	} else {
		if(OSC1Count == 0) {
			OSC1Phase = !OSC1Phase;
			if(OSC1Phase) {
				bitSet(PORTD, OSC1PIN);
			} else {
				bitClear(PORTD, OSC1PIN);
			}
			OSC1Count = OSC1CountNext;
		}
		if(OSC2Count == 0) {
			OSC2Phase = !OSC2Phase;
			if(OSC2Phase) {
				bitSet(PORTD, OSC2PIN);
			} else {
				bitClear(PORTD, OSC2PIN);
			}
			OSC2Count = OSC2CountNext;
		}
		if(OSC3Count == 0) {
			OSC3Phase = !OSC3Phase;
			if(OSC3Phase) {
				bitSet(PORTD, OSC3PIN);
			} else {
				bitClear(PORTD, OSC3PIN);

			}
			OSC3Count = OSC3CountNext;
		}
		if(OSC4Count == 0) {
			OSC4Phase = !OSC4Phase;
			if(OSC4Phase) {
				bitSet(PORTD, OSC4PIN);
			} else {
				bitClear(PORTD, OSC4PIN);
			}
			OSC4Count = OSC4CountNext;
		}
		minCycles = OSC1Count;
		if(OSC2Count < minCycles) minCycles = OSC2Count;
		if(OSC3Count < minCycles) minCycles = OSC3Count;
		if(OSC4Count < minCycles) minCycles = OSC4Count;
		if(minCycles > 255) {
			OSC1Count -= 256;
			OSC2Count -= 256;
			OSC3Count -= 256;
			OSC4Count -= 256;
			TCNT2 = 0;
			return;
		} else {
			OSC1Count -= minCycles;
			OSC2Count -= minCycles;
			OSC3Count -= minCycles;
			OSC4Count -= minCycles;
			TCNT2 = 256 - minCycles;
		}
	}
}
