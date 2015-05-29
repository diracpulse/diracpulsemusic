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
#include "sh.h"
#include "jumpTable.h"

//#include "eeprom.h"

#include "io.h"
#include "interrupt.h"

volatile char nextSample = 1;

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

void serialWriteWithWait(unsigned char DataOut)
{
	while(!serialWriteReady());
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
	/* Now configure the prescaler to CPU clock divided by 64 */
	TCCR2B |= (1<<CS22); // Set Bits
	TCCR2B &=  ~((1<<CS21) | (1<<CS20)); // Clear Bits
	/* Finally load end enable the timer */
	//TCNT2 = 255;
	TCNT2 = 255;
	TIMSK2 |= (1<<TOIE2);
}

int blink = 0;
// next sample when timerCount = 4
char timerCount = 0;

extern char portBVal;
char portBVal;
extern char portDVal;
char portDVal;

// Called when timer 2 overflows
ISR(TIMER2_OVF_vect) {
	// ***********************
	// Start OSC1
	// ***********************
	// load portBVal into r23
	// osc1 currentValue in r15:r14:r13
	asm("lds r16, portBVal");
	asm("andi r16, 0b11000011"); // clear oscillator bits
	//X = oscMasterData
	asm("ldi r26, lo8(oscMasterData + 6)");
	asm("ldi r27, hi8(oscMasterData + 6)");
	// Load osc1DeltaPhase
	asm("ld r7, X+\n");
	asm("ld r8, X+\n");
	asm("ld r9, X+\n");
	// save OSC1 currentValue in r6
	asm("mov r6, r15");
	// Load osc1CurrentValue
	// add to osc1 currentValue in r15:r14:r13
	asm("add r13, r7\n");
	asm("adc r14, r8\n");
	asm("adc r15, r9\n");
	// check for overflow
	asm("brvc skipReset1TM2\n");
	// if maxValue set integrator reset pin high
	asm("ori r16, 0b00100000");
	asm("jmp startOSC2TM2");
	// compare to prev value to currentValue
	asm("skipReset1TM2:");
	asm("cp r6, r15\n");
	asm("brsh skipSetOutput1TM2\n");
	asm("ori r16, 0b00010000\n");
	asm("skipSetOutput1TM2:\n");
	// ***********************
	// Start OSC2
	// ***********************
	// osc1 currentValue in r12:r11:r10
	asm("startOSC2TM2:");
	asm("ldi r26, lo8(oscMasterData + 15)");
	asm("ldi r27, hi8(oscMasterData + 15)");
	// Load osc2DeltaPhase
	asm("ld r7, X+\n");
	asm("ld r8, X+\n");
	asm("ld r9, X+\n");
	// store current value in r6
	asm("mov r6, r12");
	// Add deltaPhase to osc1CurrentValue
	asm("add r10, r7\n");
	asm("adc r11, r8\n");
	asm("adc r12, r9\n");
	// check for overflow
	asm("brvc skipReset2TM2\n");
	// if maxValue set integrator reset pin high
	asm("ori r16, 0b00001000");
	asm("jmp finishedTM2");
	asm("skipReset2TM2:\n");
	// compare to prev value to currentValue
	asm("cp r6, r12\n");
	asm("brsh finishedTM2\n");
	asm("ori r16, 0b00000100\n");
	// store port value and send new value out
	asm("finishedTM2:");
	asm("sts portBVal, r16");
	asm("out 0x5, r16");
	//asm("sts portDVal, r16");
	//asm("out 0x0B, r16");
	//nextSample = 1;
	TCNT2 = 255;
}

int main() {
	
	// Setup starts here
	DDRD = DDRD | B11111110;  // sets pins 2 to 7 as outputs
	DDRB = DDRB | B00111111;  // sets pins 8 to 13 as outputs
	//TIMSK0 &= ~_BV(TOIE0); // disable timer0 overflow interrupt
	initSerial();
	// Setup ends here
	sei();
	initOSC1();
	initOSC2();
	initLFO1();
	initADSR1();
	//initSH1();
	updateOscillators();
	initTimer2();
	asm("infinite:");
			asm("lds r16, nextSample\n");
			asm("cpi r16, 0\n");
			asm("breq infinite\n");

				//updateOscillators();
				//updateLFO1();
			//sei();
		asm("ldi r16, 0\n");
		asm("sts nextSample, r16\n");
		/*
		while(serialHasData()) {
			if(serialReadIntoBuffer() >= 2) {
				command = serialReadOutOfBuffer();
				if(!(command && 0b10010000)) continue;
				//serialWrite(command);
				data1 = serialReadOutOfBuffer();
				//data2 = serialReadOutOfBuffer();
				//if(command == 0b10010000) {
					//serialWrite(data1);
					//setDeltaPhaseIndex(data1, 0);
				//}
				//serialDataReady = 3;
				break;
			}
		}
		//serialWriteWithWait(128 + 0);
		//serialWriteWithWait(osc2PrevVal);
		//serialWriteWithWait(128 + 1);
		//serialWriteWithWait(osc2PrevVal);
		//serialWriteWithWait(128 + 2);
		//serialWriteWithWait(lfo1PrevVal);
		*/
	asm("jmp infinite\n");
	return 0;
}
