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

//#include "eeprom.h"

#include "io.h"
#include "interrupt.h"

#define minMidiNote 12
#define maxMidiNote 84
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
	/* Now configure the prescaler to CPU clock divided by 128 */
	TCCR2B |= (1<<CS20) | (1<<CS21); // Set Bits
	TCCR2B &= ~(1<<CS22); // Clear Bits
	/* Finally load end enable the timer */
	//TCNT2 = 255;
	TCNT2 = 0;
	TIMSK2 |= (1<<TOIE2);
}

int setDeltaPhaseIndex(int note, int oscIndex) {
	if(note >= minMidiNote && note <= maxMidiNote) {
		if(oscIndex == 0) {
			oscDeltaPhaseIndex[0] = note * 64 * 3 - 9;
		}
		if(oscIndex == 1) {
			oscDeltaPhaseIndex[1] = note * 64 * 3 - 9;
		}
	}
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
	nextSample = 1;
}

void initOscMasterData() {
	// 0 osc1LFOMod
	oscMasterData[0] = 64 * 3;
	// 2 osc1SHMod
	oscMasterData[2] = 64 * 3;
	// 4 osc1ENVMod
	oscMasterData[4] = 64 * 3;
	// 12 osc1LFOMod
	oscMasterData[12] = 64 * 3;
	// 14 osc1SHMod
	oscMasterData[14] = 64 * 3;
	// 16 osc1ENVMod
	oscMasterData[16] = 64 * 3;
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
	setDeltaPhaseIndex(60, 1);
	updateLFOVal(900);
	lfo1Up = 1;
	updateEnvelope();
	sh1NextValue = 64;
	sh1CurrentValue = 64;
	sh1Rate = 10000;
	sh1CurrentCount = 0;
	initOscMasterData();
	asm("infinite:");
			asm("lds r16, nextSample\n");
			asm("cpi r16, 0\n");
			asm("breq infinite\n");
				// ***********************
				// Start OSC1
				// ***********************
				// load portBVal into r23
				asm("lds r23, portBVal");
				asm("andi r23, 0b11000011"); // clear oscillator bits
				//Y = oscMasterData
				asm("ldi r28, lo8(oscMasterData + 6)");
				asm("ldi r29, hi8(oscMasterData + 6)");
				// Load osc1DeltaPhase
				asm("ld r3, Y+\n");
				asm("ld r4, Y+\n");
				asm("ld r5, Y+\n");
				// Load osc1CurrentValue
				asm("ld r6, Y+\n");
				asm("ld r7, Y+\n");
				asm("ld r22, Y+\n");
				// store current value in r9
				asm("mov r9, r22");
				// DEBUGGING
				asm("sts osc1PrevVal, r22");
				// Add deltaPhase to osc1CurrentValue
				asm("add r6, r3\n");
				asm("adc r7, r4\n");
				asm("adc r22, r5\n");
				// test for max value
				asm("cpi r22, 128\n");
				asm("brlo skipReset1\n");
				// if maxValue reset and set integrator reset pin high
				asm("clr r6\n");
				asm("clr r7\n");
				asm("clr r22\n");
				asm("ori r23, 0b00100000");
				// compare to prev value to currentValue
				// NOTE use of BRSH (Branch if same or higher)
				// prevVal will be greater than current val if 128 is reached
				// Update osc1 current value
				asm("skipReset1:\n");
				asm("cp r9, r22\n");
				asm("brsh skipSetOutput1\n");
				asm("ori r23, 0b00010000\n");
				asm("skipSetOutput1:\n");
				// Update osc1 current value
				asm("st -Y, r22\n");
				asm("st -Y, r7\n");
				asm("st -Y, r6\n");
				// ***********************
				// Start OSC2
				// ***********************
				// load portBVal into r23
				// asm("lds r23, portBVal")
				//Y = oscMasterData
				asm("ldi r28, lo8(oscMasterData + 18)\n");
				asm("ldi r29, hi8(oscMasterData + 18)\n");
				// Load osc2DeltaPhase
				asm("ld r3, Y+\n");
				asm("ld r4, Y+\n");
				asm("ld r5, Y+\n");
				// Load osc2CurrentValue
				asm("ld r6, Y+\n");
				asm("ld r7, Y+\n");
				asm("ld r22, Y+\n");
				// store current value in r9
				asm("mov r9, r22");
				// DEBUGGING
				asm("sts osc2PrevVal, r22");
				// Add deltaPhase to osc1CurrentValue
				asm("add r6, r3\n");
				asm("adc r7, r4\n");
				asm("adc r22, r5\n");
				// test for max value
				asm("cpi r22, 128\n");
				asm("brlo skipReset2\n");
				// if maxValue reset and set integrator reset pin high
				asm("clr r6\n");
				asm("clr r7\n");
				asm("clr r22\n");
				asm("ori r23, 0b00001000");
				// compare to prev value to currentValue
				// NOTE use of BRSH (Branch if same or higher)
				// prevVal will be greater than current val if 128 is reached
				// Update osc1 current value
				asm("skipReset2:\n");
				asm("cp r9, r22\n");
				asm("brsh skipSetOutput2\n");
				asm("ori r23, 0b00000100\n");
				asm("skipSetOutput2:\n");
				// Update osc1 current value
				asm("st -Y, r22\n");
				asm("st -Y, r7\n");
				asm("st -Y, r6\n");
				// store port value and send new value out
				asm("sts portBVal, r23");
				asm("out 0x5, r23");
				updateOscillators();
				updateLFO1();
			//sei();
		asm("ldi r16, 0\n");
		asm("sts nextSample, r16\n");
		while(serialHasData()) {
			if(serialReadIntoBuffer() >= 2) {
				command = serialReadOutOfBuffer();
				if(!(command && 0b10010000)) continue;
				//serialWrite(command);
				data1 = serialReadOutOfBuffer();
				//data2 = serialReadOutOfBuffer();
				//if(command == 0b10010000) {
					//serialWrite(data1);
					setDeltaPhaseIndex(data1, 0);
				//}
				serialDataReady = 3;
				break;
			}
		}
		//serialWriteWithWait(128 + 0);
		serialWriteWithWait(osc2PrevVal);
		//serialWriteWithWait(128 + 1);
		//serialWriteWithWait(osc2PrevVal);
		//serialWriteWithWait(128 + 2);
		//serialWriteWithWait(lfo1PrevVal);
	asm("jmp infinite\n");
	return 0;
}
