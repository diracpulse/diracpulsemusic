/*
Blink
  Turns on an LED on for one second, then off for one second, repeatedly.

  This example code is in the public domain.
 */

// Pin 13 has an LED connected on most Arduino boards.
// give it a name:


#include "Arduino.h"
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
#include <avr/pgmspace.h>
#include <avr/io.h>
#include <avr/interrupt.h>


// INO Starts HERE

#include "eeprom.h"
#include <TimerOne.h>
#include <MsTimer2.h>

void timer1ISR();
void timer2ISR();

const float octave[] = {63.05, 126.1, 252.2, 504.4};
const float scale[] = {1.0, 17.0 / 16.0, 9.0 / 8.0, 6.0 / 5.0, 5.0 / 4.0, 4.0 / 3.0, 11.0 / 8.0, 3.0 / 2.0, 13.0 / 8.0, 27.0 / 16.0, 7.0 / 4.0, 15.0 / 8.0};
byte octaveIndex = 2;
byte pattern[] = {1, 5, 6, 6, 13};
byte patternIndex = 0;
const float slideTimeInMicros = 500.0 * 1000.0;
float beatLengthInMicros = 1000.0 * 1000.0;
volatile float microsElapsed = 0;
float startSlideTimeInMicros = 0;
float bpm = 30;
byte currentNote = 0;
byte nextNote = 0;
float currentMicrosPerPulse;
float nextMicrosPerPulse;
float deltaMicrosPerPulse;
long pulsePhase = 0;
bool slide = true;
bool startSlide = true;
bool firstBeat = true;
bool newFreq = false;
byte nextBeat = 1;

void setup()
{
  pinMode(13, OUTPUT);
  DDRD = DDRD | B11111100;  // sets pins 2 to 7 as outputs
  beatLengthInMicros = 60.0 / bpm * 1000000.0;
  int beatLengthInMillis = round(beatLengthInMicros / 1000.0);
  MsTimer2::set(beatLengthInMillis, timer2ISR); // 500ms period
  MsTimer2::start();
}

void loop() {
	if(nextBeat == 1) {
		currentNote = pattern[(int) patternIndex];
		if(currentNote > 11) {
			patternIndex = 0;
			currentNote = pattern[(int) patternIndex];
		}
		patternIndex++;
		nextNote = pattern[(int) patternIndex];
		if(nextNote > 11) {
			nextNote = pattern[0];
		}
		float freqInHz = octave[octaveIndex] * scale[currentNote];
		float newCurrentMicrosPerPulse = 500000.0 / freqInHz;
		freqInHz = octave[octaveIndex] * scale[nextNote];
		float newNextMicrosPerPulse = 500000.0 / freqInHz;
		microsElapsed = 0.0;
		startSlideTimeInMicros = beatLengthInMicros - slideTimeInMicros;
		startSlide = true;
		currentMicrosPerPulse = newCurrentMicrosPerPulse;
		nextMicrosPerPulse = newNextMicrosPerPulse;
		if(firstBeat) {
			Timer1.initialize(round(currentMicrosPerPulse));
			Timer1.attachInterrupt(timer1ISR); // attach the service routine here
			firstBeat = false;
		} else {
			Timer1.setPeriod(round(currentMicrosPerPulse));
		}
		nextBeat = 0;
	}
}

void timer1ISR()
{
	pulsePhase++;
	if(pulsePhase % 2 == 0) {
		digitalWrite(7, 1);
	} else {
		digitalWrite(7, 0);
	}
	microsElapsed += currentMicrosPerPulse;
	if(microsElapsed >= startSlideTimeInMicros) {
		if(startSlide) {
			float averageMicrosPerPulse = (currentMicrosPerPulse + nextMicrosPerPulse) / 2.0;
			float numPulsesInSlide = slideTimeInMicros / averageMicrosPerPulse;
			deltaMicrosPerPulse = (nextMicrosPerPulse - currentMicrosPerPulse) / numPulsesInSlide;
			Timer1.setPeriod(round(currentMicrosPerPulse));
			startSlide = false;
		} else {
			currentMicrosPerPulse += deltaMicrosPerPulse;
			Timer1.setPeriod(round(currentMicrosPerPulse));
		}
	}
}

void timer2ISR()
{
	nextBeat = 1;
}
