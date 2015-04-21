
How to use the synthesizer:

BPM: Beats per minute (30 - 300)
OSC1: (Main oscilator)
	SHP (Wave shape):
		0.0 = Sine
		.33 = Triangle
		.66 = Square
		1.0 = Sawtooth
		Values in between are mixtures of the waveforms above and below
	PWM: Pulse width modulation of square wave 
	(only has an effect for SHP values >.33 and <1.0)
OSC2: (Oscillator 2):
	FREQ: (OSC2 Frequency) / (OSC1 Frequency)
OSC3: (Oscillator 3):
	FREQ: (OSC3 Frequency) / (OSC1 Frequency)
RING1 (Ring oscillator 1):
	FREQ: (RING1 Frequency) / (OSC1 Frequency)
	AMT: OSC1 Output = OSC1 * RING1 * AMT + OSC1 * (1.0 - AMT)
RING2: (Ring oscillator 2):
	FREQ: (RING2 Frequency) / (OSC2 Frequency)
RING3: (Ring oscillator 3):
	FREQ: (RING3 Frequency) / (OSC3 Frequency)
RE1: (Ring Envelope 1)
	The amount of time in seconds it takes RING1 to decay to e^-2.0 ( about 14%), 
	it starts at 1.0
RE2: (Ring Envelope 2)
RE3: (Ring Envelope 3)
AMP OSC: Amplitude Oscillator Shape:
	see OSC1 Shape
AMP LFO: Amplitude Low Frequency Oscillator (Common to all oscillators):
	RATE: Number of cycles per second
	AMT: OSC Output = OSC * AMP LFO * AMT + OSC1 * (1.0 - AMT)
AMP ADSR: Amplitude Envelope (Common to all oscillators):
	A (Attack): The amount of time in seconds it takes to go from 0 to 1.0
	D (Decay): The amount of time in seconds to decay to 14%.  
	Decay stops when sustain is reached.
	S (Sustain) Amplitude until note off occurs
	R (Release) Amount of time in seconds to decay from sustain level
	to 14% of sustain level after note off
FLTR ADSR: Filter Envelope (Common to all oscillators):
	Same as AMP ADSR except it modulates filter cutoff frequency.
 	Modifies cuttoff freq so that: LP FREQ += Filter ADSR * 8.0
	i.e. when FILTER ADSR is at maximum cutoff freq is 3 octaves 
	above value indicated by LP FREQ (see also FILTER LFO)
FLTR OSC: Filter Oscillator Shape:
	see OSC1 Shape
FLTR LFO: Filter Low Frequency Oscillator:
	RATE: Number of cycles per second
	AMT: LP FREQ += 8.0 * LFO Waveform
LP: Low pass filter:
	FREQ: Frequency at which output is 24dB below max level, 
	for each octave above another 24dB of attenuation occurs
	unless resonance is set above .707
	RES: Amplifies tones around the cutoff frequency (if above .707)
HP: High pass filter:
	FREQ: Frequency at which output is 12dB below max level, 
	for each octave below another 24 dB of attenuation occurs
	Mostly there just to remove subsonic frequencies.
MIXER:
	OSC(X): OSC(X) output volume

Sequencer (Green below all the sliders):
	To create a rest click on a note:
	To shorten the sequence: Hold down SHIFT and click on the last note to play
	To toggle glide: Hold down CTRL and click the note
	To toggle accent: Hold down ALT and click the note
Colors:
	WHITE: Plain note
	RED: Glide
	BLUE: Accent
	PURPLE: Glide and Accent
	GRAY: Grayed out notes are skipped over
