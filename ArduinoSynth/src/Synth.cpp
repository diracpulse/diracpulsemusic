
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
volatile bool timer2Loop = false;
volatile bool OSC1CountUpdated = true;
volatile bool OSC2CountUpdated = true;
volatile bool OSC3CountUpdated = true;
volatile bool OSC4CountUpdated = true;
volatile int OSC1CountOld = 0;
volatile int OSC2CountOld = 0;
volatile int OSC3CountOld = 0;
volatile int OSC4CountOld = 0;
volatile int OSC1Count = 0;
volatile int OSC2Count = 0;
volatile int OSC3Count = 0;
volatile int OSC4Count = 0;
volatile int OSC1CountNext = 0;
volatile int OSC2CountNext = 0;
volatile int OSC3CountNext = 0;
volatile int OSC4CountNext = 0;
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
void handleTimer2Interrupt();
void initTimer1(long microseconds);
void initTimer2();
void setFrequency1(float freq);
void setFrequency2(float freq);
void setFrequency3(float freq);
void setFrequency4(float freq);

void setup()
{
  pinMode(13, OUTPUT);
  DDRD = DDRD | B11111100;  // sets pins 2 to 7 as outputs
  initTimer1(8000000);
  initTimer2();
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
	}
}

// Timer 1 Code:

void initTimer1(long microseconds) {
	  TCCR1A = 0;                 // clear control register A
	  TCCR1B = _BV(WGM13);        // set mode 8: phase and frequency correct pwm, stop the timer
	  long cycles = 65535;                                // the counter runs backwards after TOP, interrupt is at BOTTOM so divide microseconds by 2
	  unsigned char oldSREG = SREG;
	  cli();							// Disable interrupts for 16 bit register access
	  ICR1 = cycles;                    // ICR1 is TOP in p & f correct pwm mode
	  SREG = oldSREG;
	  TIMSK1 = _BV(TOIE1);
	  TCCR1B &= ~(_BV(CS10) | _BV(CS11) | _BV(CS12));
	  TCCR1B |= _BV(CS11) | _BV(CS10);  // prescale by /64
	  sei();
}

ISR(TIMER1_OVF_vect)          // interrupt service routine that wraps a user defined function supplied by attachInterrupt
{
	nextBeat = true;
}

// Timer 2 Code:

void setFrequency1(float freqInHz) {
	long cycles = round((1.0 / freqInHz) * 16000000.0);
	cycles >>= 5;
	OSC1CountUpdated = false;
	OSC1CountNext = cycles;
	OSC1CountUpdated = true;
	OSC1CountOld = cycles;
	OSC1Active = true;
}

void setFrequency2(float freqInHz) {
	long cycles = round((1.0 / freqInHz) * 16000000.0);
	cycles >>= 5;
	OSC2CountUpdated = false;
	OSC2CountNext = cycles;
	OSC2CountUpdated = true;
	OSC2CountOld = cycles;
	OSC2Active = true;
}

void setFrequency3(float freqInHz) {
	long cycles = round((1.0 / freqInHz) * 16000000.0);
	cycles >>= 5;
	OSC3CountUpdated = false;
	OSC3CountNext = cycles;
	OSC3CountUpdated = true;
	OSC3CountOld = cycles;
	OSC3Active = true;
}

void setFrequency4(float freqInHz) {
	long cycles = round((1.0 / freqInHz) * 16000000.0);
	cycles >>= 5;
	OSC4CountUpdated = false;
	OSC4CountNext = cycles;
	OSC4CountUpdated = true;
	OSC4CountOld = cycles;
	OSC4Active = true;
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
	/* Now configure the prescaler to CPU clock divided by 64 */
	TCCR2B |= (1<<CS21) | (1<<CS20); // | (1<<CS20); // Set Bit
	TCCR2B &= ~(1<<CS22); // Clear Bit
	/* Finally load end enable the timer */
	TCNT2 = 0;
	TIMSK2 |= (1<<TOIE2);
}

// Called when timer 2 overflows
ISR(TIMER2_OVF_vect) {
	OSC1Count -= 256;
	OSC2Count -= 256;
	OSC3Count -= 256;
	OSC4Count -= 256;
	handleTimer2Interrupt();
}

void handleTimer2Interrupt() {
	unsigned char tcnt2 = TCNT2;
	unsigned char tcnt2plus1 = TCNT2;
	unsigned char oldTCNT2 = tcnt2;
	unsigned char unfinished = B00001111;
	while(tcnt2 <= 253) {
		unsigned char tcnt2plus1 = tcnt2 + 2;
		signed int subtractValue = 256 - tcnt2;
		if(OSC1Count <= tcnt2plus1) {
			OSC1Phase = !OSC1Phase;
			if(OSC1Phase) {
				bitSet(PORTD, OSC1PIN);
			} else {
				bitClear(PORTD, OSC1PIN);
			}
			if(OSC1CountUpdated) {
				OSC1Count = OSC1CountNext - subtractValue; // Account for current TCNT != 0
			} else {
				OSC1Count = OSC1CountOld - subtractValue; // Account for current TCNT != 0
			}
			unfinished &= B11111110;
		}
		if(OSC2Count <= tcnt2plus1) {
			OSC2Phase = !OSC2Phase;
			if(OSC2Phase) {
				bitSet(PORTD, OSC2PIN);
			} else {
				bitClear(PORTD, OSC2PIN);
			}
			if(OSC2CountUpdated) {
				OSC2Count = OSC2CountNext - subtractValue; // Account for current TCNT != 0
			} else {
				OSC2Count = OSC2CountOld - subtractValue; // Account for current TCNT != 0
			}
			unfinished &= B11111101;
		}
		if(OSC3Count <= tcnt2plus1) {
			OSC3Phase = !OSC3Phase;
			if(OSC3Phase) {
				bitSet(PORTD, OSC3PIN);
			} else {
				bitClear(PORTD, OSC3PIN);
			}
			if(OSC3CountUpdated) {
				OSC3Count = OSC3CountNext - subtractValue; // Account for current TCNT != 0
			} else {
				OSC3Count = OSC3CountOld - subtractValue; // Account for current TCNT != 0
			}
			unfinished &= B11111011;
		}
		if(OSC4Count <= tcnt2plus1) {
			OSC4Phase = !OSC4Phase;
			if(OSC4Phase) {
				bitSet(PORTD, OSC4PIN);
			} else {
				bitClear(PORTD, OSC4PIN);
			}
			if(OSC4CountUpdated) {
				OSC4Count = OSC4CountNext - subtractValue; // Account for current TCNT != 0
			} else {
				OSC4Count = OSC4CountOld - subtractValue; // Account for current TCNT != 0
			}
			unfinished &= B11110111;
		}
		if(unfinished == 0) return;
		oldTCNT2 = tcnt2;
		tcnt2 = TCNT2;
	}
}
	/*
	if(OSC1Count <= 255) {
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
	if(OSC2Count <= 255) {
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
	if(OSC3Count <= 255) {
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
	if(OSC4Count <= 255) {
		OSC4Phase = !OSC4Phase;
		if(OSC4Phase) {
			bitSet(PORTD, OSC4PIN);
		} else {
			bitClear(PORTD, OSC4PIN);
		}
		if(OSC3CountUpdated) {
			OSC4Count = OSC4CountNext; // Account for current TCNT != 0
		} else {
			OSC4Count = OSC4CountOld; // Account for current TCNT != 0
		}
	}
	*/
