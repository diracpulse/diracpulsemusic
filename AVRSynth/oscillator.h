
// Oscillator 1 Variables

// LFO1
extern unsigned char lfo1up;
extern unsigned char lfo1PrevValue;
extern unsigned char lfo1CurrentValue[3];
extern unsigned char lfo1Delta[2];

// ADSR1
extern unsigned char adsr1up;
extern unsigned char adsr1PrevValue;
extern unsigned char adsr1CurrentValue[3];
extern unsigned char adsr1DeltaValue[2];
extern unsigned int  adsr1AttackTime;
extern unsigned int  adsr1DecayTime;
extern unsigned char adsr1Sustain;
extern unsigned int  adsr1ReleaseTime;

// Sample and Hold 1
extern unsigned char sh1CurrentValue;
extern unsigned int sh1rate;
extern unsigned int sh1currentCount;

// OSC1
extern unsigned int osc1DeltaPhaseIndex;
extern volatile unsigned char portBVal;
extern unsigned char osc1PrevValue;
extern unsigned char osc1CurrentValue[3];
extern unsigned char osc1shAmt;
extern unsigned char osc1lfoAmt;
extern unsigned char osc1adsrAmt;

// Oscillator 1 Variables
// LFO1

unsigned char lfo1up;
unsigned char lfo1PrevValue;
unsigned char lfo1CurrentValue[3];
unsigned char lfo1Delta[2];

// ADSR1
unsigned char adsr1up;
unsigned char adsr1PrevValue;
unsigned char adsr1CurrentValue[3];
unsigned char adsr1DeltaValue[2];
unsigned int  adsr1AttackTime;
unsigned int  adsr1DecayTime;
unsigned char adsr1Sustain;
unsigned int  adsr1ReleaseTime;

// Sample and Hold 1
unsigned char sh1CurrentValue;
unsigned char sh1NextValue;
unsigned int sh1rate;
unsigned int sh1currentCount;

// OSC1
unsigned int osc1DeltaPhaseIndex;
unsigned volatile char portBVal;
unsigned char osc1PrevValue;
unsigned char osc1CurrentValue[3];
unsigned char osc1shAmt;
unsigned char osc1lfoAmt;
unsigned char osc1adsrAmt;

// OSC Master Data
extern unsigned char oscMasterData[63];
unsigned char oscMasterData[63];
// 0 osc1LFOMod
// 1 osc1SHMod
// 2 osc1ENVMod
// 3 - 5 osc1CurrentValue
// 6 osc1PrevValue
