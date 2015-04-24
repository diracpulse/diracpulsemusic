
#include "Arduino.h"
#include "waveforms.h"
#include "frequencies.h";
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

byte commandByte;
byte noteByte;
byte velocityByte;

char osc1;
int osc1Phase = 0;

const float octave[] = {63.05, 126.1, 252.2, 504.4};
const float scale[] = {1.0, 17.0 / 16.0, 9.0 / 8.0, 6.0 / 5.0, 5.0 / 4.0, 4.0 / 3.0, 11.0 / 8.0, 3.0 / 2.0, 13.0 / 8.0, 27.0 / 16.0, 7.0 / 4.0, 15.0 / 8.0};
const float sampleRate = 32000.0;
char nextSample = 1;

void setup()
{
  pinMode(13, OUTPUT);
  DDRD = DDRD | B11111100;  // sets pins 2 to 7 as outputs
  DDRB = DDRB | B00111111;  // sets pins 8 to 13 as outputs
  //TIMSK0 &= ~_BV(TOIE0); // disable timer0 overflow interrupt
  Serial.begin(31250);
}

void loop() {
	if(nextSample) {
	}
}

void checkMIDI(){
  do{
    if (Serial.available() > 2){
      commandByte = Serial.read();//read first byte
      noteByte = Serial.read();//read next byte
      velocityByte = Serial.read();//read final byte
    }
  }
  while (Serial.available() > 2);//when at least three bytes available
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
	/* Now configure the prescaler to CPU clock divided by 8 */
	TCCR2B |= (1<<CS21); // Set Bit
	TCCR2B &= ~((1<<CS22) | (1<<CS20)); // Clear Bits
	/* Finally load end enable the timer */
	TCNT2 = 0;
	TIMSK2 |= (1<<TOIE2);
}

// Called when timer 2 overflows
ISR(TIMER2_OVF_vect) {
	TCNT2 = 255;
}
