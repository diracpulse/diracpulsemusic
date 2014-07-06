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

const float octave[] = {63.05, 126.1, 252.2};
const float scale[] = {1.0, 17.0 / 16.0, 9.0 / 8.0, 6.0 / 5.0, 5.0 / 4.0, 4.0 / 3.0, 11.0 / 8.0, 3.0 / 2.0, 13.0 / 8.0, 27.0 / 16.0, 7.0 / 4.0, 15.0 / 8.0};
byte octaveIndex = 2;
byte pattern[] = {1, 3, 6, 6, 1, 8, 11, 1, 12};
byte accentArray[]  = {1, 0, 1, 0, 0, 1, 0,  0, 12};
byte slideArray[]   = {0, 0, 1, 0, 0, 1, 0,  1, 12};
byte patternIndex = 0;
const float slideTimeInMicros = 25.0 * 1000.0;
float accentApexInMicros = 50.0 * 1000.0;
float accentApexScalar = 1.5;
float beatLengthInMicros = 1000.0 * 1000.0;
volatile float microsElapsed = 0;
float startSlideTimeInMicros = 0;
float bpm = 180;
byte currentNote = 0;
byte nextNote = 0;
float saveCurrentMicrosPerPulse;
float currentMicrosPerPulse;
float nextMicrosPerPulse;
float deltaMicrosPerPulse;
bool pulsePhase = false;
bool silence = false;
bool slide = false;
bool startSlide = true;
bool firstBeat = true;
bool newFreq = false;
bool accent = true;
bool accentUpward = true;
bool accentDownward = true;
bool accentToggle = true;
byte nextBeat = 1;
byte nextPulse = 1;

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
		slide = slideArray[patternIndex];
		accent = accentArray[patternIndex];
		if(currentNote > 11) {
			patternIndex = 0;
			currentNote = pattern[(int) patternIndex];
			slide = slideArray[patternIndex];
			accent = accentArray[patternIndex];
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
		silence = false;
		accentUpward = true;
		accentDownward = true;
		accentToggle = true;
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
	if(silence) {
		digitalWrite(7, 0);
		return;
	}
	pulsePhase = !pulsePhase;
	if(pulsePhase) {
		digitalWrite(7, 1);
	} else {
		digitalWrite(7, 0);
	}
	if(nextBeat == 1) return; // variables in flux
	if(nextPulse > 0) {
		nextPulse--;
		return;
	}
	nextPulse = 16;
	microsElapsed += currentMicrosPerPulse;
	if(accent && accentToggle) {
		if(accentUpward) {
			saveCurrentMicrosPerPulse = currentMicrosPerPulse;
			float averageMicrosPerPulse = (currentMicrosPerPulse / accentApexScalar + currentMicrosPerPulse) / 2.0;
			float numPulsesInSlide = accentApexInMicros / averageMicrosPerPulse;
			deltaMicrosPerPulse = (currentMicrosPerPulse / accentApexScalar - currentMicrosPerPulse) / numPulsesInSlide;
			accentUpward = false;
		} else {
			if(microsElapsed >= accentApexInMicros && accentDownward) {
				float averageMicrosPerPulse = (saveCurrentMicrosPerPulse / accentApexScalar + saveCurrentMicrosPerPulse) / 2.0;
				float numPulsesInSlide = accentApexInMicros / averageMicrosPerPulse;
				deltaMicrosPerPulse = (saveCurrentMicrosPerPulse - saveCurrentMicrosPerPulse / accentApexScalar) / numPulsesInSlide;
				accentDownward = false;
			}
			if(microsElapsed >= accentApexInMicros * 2) {
				currentMicrosPerPulse = saveCurrentMicrosPerPulse;
				deltaMicrosPerPulse = 0;
				accentToggle = false;
			}
		}
		currentMicrosPerPulse += deltaMicrosPerPulse;
		Timer1.setPeriod(round(currentMicrosPerPulse));
	}
	if(microsElapsed >= startSlideTimeInMicros) {
		if(slide) {
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
		} else {
			silence = true;
		}
	}
	nextPulse = 0;
}

void timer2ISR()
{
	nextBeat = 1;
}
