/*
 * ArduinoSynth.cpp
 *
 * Created: 5/2/2015 12:01:13 AM
 *  Author: Glenn
 */ 

#include "binary.h"
#include "waveforms.h"
#include "frequencies.h"
#include "serial.h"
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

typedef struct lfo {
	unsigned char currentValue;
	unsigned long currentPhase;
	unsigned long deltaPhase;
};

typedef struct adsr {
	unsigned char currentValue;
	unsigned int attackTime;
	unsigned int decayTime;
	unsigned char sustain;
	unsigned int releaseTime;
};

typedef struct s_and_h {
	unsigned char currentValue;
	unsigned int rate;
	unsigned int currentCount;
};

typedef struct osc {
   unsigned char currentValue;
   unsigned long currentPhase;
   unsigned long deltaPhase;
   unsigned char shAmt;
   unsigned char lfoAmt;
   unsigned char adsrAmt;
   struct s_and_h shMod;
   struct lfo lfoMod;
   struct adsr adsrMod;
};



struct osc osc1;
struct lfo lfo1;
struct adsr adsr1;
struct s_and_h sh1;
unsigned long phaseEqual1 = 16777216; // pow(2, 24);
#define setOsc1ResetOn B00000100
#define setOsc1ResetOff B11111011
#define setOsc1On B00001000
#define setOsc1Off B11110111
bool nextSample = true;

void setup()
{
  DDRD = DDRD | B11111100;  // sets pins 2 to 7 as outputs
  DDRB = DDRB | B00111111;  // sets pins 8 to 13 as outputs
  //TIMSK0 &= ~_BV(TOIE0); // disable timer0 overflow interrupt
  osc1.lfoMod = lfo1;
  osc1.adsrMod = adsr1;
  osc1.shMod = sh1;
}

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

int blink = 0;
// Called when timer 2 overflows
ISR(TIMER2_OVF_vect) {
	//nextSample = true;
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
}

int main() {
	
	// Setup starts here
	DDRD = DDRD | B11111110;  // sets pins 2 to 7 as outputs
	DDRB = DDRB | B00111111;  // sets pins 8 to 13 as outputs
	//TIMSK0 &= ~_BV(TOIE0); // disable timer0 overflow interrupt
	osc1.lfoMod = lfo1;
	osc1.adsrMod = adsr1;
	osc1.shMod = sh1;
	initSerial();
	initTimer2();
	// Setup ends here
	sei();
	while(1) {
		if(nextSample) {
			register unsigned long deltaPhaseMod = osc1.deltaPhase;
			register unsigned int lfoVal = osc1.lfoAmt * osc1.lfoMod.currentValue;
			register unsigned int adsrVal = osc1.adsrAmt * osc1.adsrMod.currentValue;
			register unsigned int shVal = osc1.shAmt * osc1.shMod.currentValue;
			deltaPhaseMod = deltaPhaseMod * ((lfoVal + adsrVal + shVal) >> 8);
			deltaPhaseMod >>= 8;
			osc1.currentPhase += osc1.deltaPhase + deltaPhaseMod;
			if(osc1.currentPhase >= phaseEqual1) {
				osc1.currentPhase -= phaseEqual1;
				PORTD |= setOsc1ResetOn;
			}
			register unsigned char osc1NewValue = osc1.currentPhase >> 17;
			if(osc1NewValue > osc1.currentValue) {
				PORTD |= setOsc1On;
				} else {
				PORTD &= setOsc1Off;
			}
			osc1.currentValue = osc1NewValue;
			nextSample = false;
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
		while(serialWriteReady()) {
			if(serialDataReady == 3) {
				serialWrite(command);
				serialDataReady--;
				continue;
			}
			if(serialDataReady == 2) {
				serialWrite(data1);
				serialDataReady--;
				continue;
			}
			if(serialDataReady == 1) {
				serialWrite(data2);
				serialDataReady--;
				continue;
			}
			break;
		}
	}
}
