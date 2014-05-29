
const int maxInt = 65535;
const int maxLong = 4294967296;
const byte microsPerSample = 24;
const float sampleRate = 1000.0 * 1000.0 / (float) microsPerSample;

const float middleC = 252.2;
const float[] completeScale = {1.0, 17.0 / 16.0, 9.0 / 8.0, 6.0 / 5.0, 5.0 / 4.0, 4.0 / 3.0, 11.0 / 8.0, 3.0 / 2.0, 13.0 / 8.0, 27.0 / 16.0, 7.0 / 4.0, 15.0 / 8.0};
const float[] majorScale = {1.0, 9.0 / 8.0, 5.0 / 4.0, 4.0 / 3.0, 3.0 / 2.0, 5.0 / 3.0, 15.0 / 8.0};
const float[] minorScale = {1.0, 9.0 / 8.0, 6.0 / 5.0, 4.0 / 3.0, 3.0 / 2.0, 8.0 / 5.0, 9.0 / 5.0};
const float majorChord = 4.0 / 3.0;
const float minorChord = 6.0 / 5.0;

const boolean major = 0;
const boolean minor = 1;

boolean intonation = minor;
int key = 0;

class PulseGen {
  
  int currentPhase = 0;
  int deltaPhase = 0;
  int pulseWidth = maxInt / 2;
  
  PulseGenerator(float freq, float pulseWidth) {
    currentPhase = 0;
    deltaPhase = (int) round(freq / sampleRate * (float) maxLong);
    pulseWidth = 
  }
}

const byte leadPin =  B10000000;
const byte fifthPin = B01000000;
const byte chordPin = B00100000;
const byte bassPin =  B00010000;

PulseGen lead = new PulseGen(middleC, 0.5);
PulseGen fifth = new PulseGen(middleC * 1.5, 0.5);
PulseGen chord = new PulseGen(middleC * minorChord, 0.5);
PulseGen bass = new PulseGen(middleC * 0.5, 0.5);

long time = 0;
byte output = 1;

void setup()
{
  DDRD = DDRD | B11111100;  // sets pins 2 to 7 as outputs without changing the value of pins 0 & 1, which are RX & TX 
}

void loop() {
  while(byte(micros()) % microsPerSamples != 0);
  output = 0;
  if(lead.currentPhase < lead.pulseWidth) output || leadPin;
  if(fifth.currentPhase < fifth.pulseWidth) output || fifthPin;
  if(chord.currentPhase < chord.pulseWidth) output || chordPin;
  if(bass.currentPhase < bass.pulseWidth) output || bassPin;
  PORTD = output;
  lead.currentPhase += lead.deltaPhase;
  fifth.currentPhase += fifth.deltaPhase;
  chord.currentPhase += chord.deltaPhase;
  bass.currentPhase += bass.deltaPhase;
}

