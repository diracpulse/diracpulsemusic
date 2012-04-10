
#include <stdio.h>
#include <stdlib.h>
#include <math.h> 

#define CHARS (4410 * 4)
#define STEREO (4410 * 2)
#define MONO 4410
void *stereoShorts;
void *leftFloats;
void *rightFloats;
void *monoFloats;
short int number_of_channels = 2;
int sampling_rate = 44100;
short int bits_per_channel = 16;
int input_data_length;
FILE *input;
FILE *mono;
FILE *stereo;


void WriteHeader(FILE *fp, int data_length) {
	
	int length_of_format_chunk=0x10;
	short int hex_0x01=0x01;
	
	// Based on wav file properties
	short int bytes_per_sample = (bits_per_channel / 8) *number_of_channels;
	int bytes_per_second = bytes_per_sample*sampling_rate;
	int DATA_data_length = data_length;
	int RIFF_data_length = DATA_data_length+30;

	// Wave files are arganized as follows:
	// RIFF Chunk:
	// 0-3				"RIFF" (ASCII Characters)
	fwrite("RIFF", sizeof (char), 4, fp);

	// 4-7				Total length of pakage to follow" (Binary,little endian)
	fwrite (&RIFF_data_length, sizeof (int), 1 , fp);
	
	// 8-11				"WAVE" (ASCII Characters)
	fwrite ("WAVE", sizeof (char), 4, fp);
	
	// FORMAT Chunk
	// 0-3				"fmt\x20" ASCII charcters
	fwrite ("fmt\x20", sizeof (char), 4, fp);
	
	// 4-7				Length of format chunk (always 0x10)
	fwrite (&length_of_format_chunk, sizeof(int), 1, fp);
	
	// 8-9				(always 0x01)
	fwrite (&hex_0x01, sizeof (short int), 1, fp);
	
	// 10-11			Number of channels
	fwrite (&number_of_channels, sizeof (short int), 1, fp);

	// 12-15            Sampling Rate (binary in Hz)
	fwrite (&sampling_rate, sizeof (int), 1, fp);

	// 16-19			Bytes per second
	fwrite (&bytes_per_second, sizeof (int), 1, fp);

	// 20-21            Bytes per sample
	fwrite (&bytes_per_sample, sizeof (short int), 1, fp);

	// 22-23            Bits per channel

	fwrite (&bits_per_channel, sizeof (short int), 1, fp);
	
	// DATA CHUNK
	// 0-3

	fwrite("data", sizeof (char), 4, fp);
	// 4-7				Length of sample data to follow

	fwrite (&DATA_data_length, sizeof (int), 1 , fp);

	return;
}
	
void ReadHeader(FILE *fp) {
	
	char ASCII_4[5];
	int temp;
	short int shorttemp;

	// Constant
	int length_of_format_chunk=0x10;
	short int hex_0x01=0x01;
	
	int index;

	for (index =0; index < 5; index++) ASCII_4[index] = 0;

	// Wave files are arganized as follows:
	// RIFF Chunk:
	// 0-3				"RIFF" (ASCII Characters)
	fread(&ASCII_4, sizeof (char), 4, fp);
	printf("RIFF: %s\n", ASCII_4);

	// 4-7				Total length of pakage to follow" (Binary,little endian)
	fread (&temp, sizeof (int), 1 , fp);
	printf("Total length of package to follow: %d\n", temp);
	
	// 8-11				"WAVE" (ASCII Characters)
	fread (&ASCII_4, sizeof (char), 4, fp);
	printf("WAVE: %s\n", ASCII_4);
	
	// FORMAT Chunk
	// 0-3				"_fmt" ASCII charcters
	fread (&ASCII_4, sizeof (char), 4, fp);
	printf("_fmt: %s\n", ASCII_4);
	
	// 4-7				Length of format chunk (always 0x10)
	fread (&temp, sizeof(int), 1, fp);
	printf("Length of format chunk (always 0x10): %x\n", temp);
	
	// 8-9				(always 0x01)
	fread (&shorttemp, sizeof (short int), 1, fp);
	printf("0x01: %x\n", shorttemp);
	
	// 10-11			Number of channels
	fread (&number_of_channels, sizeof (short int), 1, fp);
	printf("Number of channels: %d\n", number_of_channels);

	// 12-15            Sampling Rate (binary in Hz)
	fread (&sampling_rate, sizeof (int), 1, fp);
	printf("Sampling rate: %d\n", sampling_rate);
	
	// 16-19			Bytes per second
	fread (&temp, sizeof (int), 1, fp);
	printf("Bytes per second: %d\n", temp);
	
	// 20-21            Bytes per sample
	fread (&shorttemp, sizeof (short int), 1, fp);
	printf("Bytes per sample: %d\n", shorttemp);
	
	// 22-23            Bits per channel
	fread (&bits_per_channel, sizeof (short int), 1, fp);
	printf("Bytes per sample: %d\n", bits_per_channel);

	// DATA CHUNK
	// 0-3				"data" (ASCII Characters)
	fread(&ASCII_4, sizeof (char), 4, fp);
	printf("data: %s\n", ASCII_4);
	
	// 4-7				Length of sample data to follow
	fread (&input_data_length, sizeof (int), 1 , fp);
	printf("Length of sample data to follow: %d\n", input_data_length);

	if (bits_per_channel != 16) {
		printf("Input file bits per channel != 16\n");
		printf("Exiting program\n");
		exit(0);
	}
	
	if (number_of_channels != 2) {
		printf("Input file number of channels != 2\n");
		printf("Exiting program\n");
		exit(0);
	}
	
	if (sampling_rate != 44100) {
		printf("Input file sampling_rate != 44100\n");
		printf("Exiting program\n");
		exit(0);
	}
	
	return;
}

/*
void WindowBuffer() {
	float Block[N];
	float dn;
	int n;
	for (int n=0; n<N; n++) {
		dn = (float) n;
		Block[n] *= 0.35875 - 0.48829*cos(arg*dn) + 0.14128*cos(2.0*arg*dn)
					- .01168*cos(3.0*arg*dn);
	}
}
*/

void writeMono() {
	int index;
	float fmono;
	float *pmono = monoFloats;
	short *pstereo = stereoShorts;
	for(index = 0; index < STEREO; index += 2) {
		fmono = *(pmono + (index / 2));
		*(pstereo + index) = (short) fmono;
		*(pstereo + index + 1) = (short) fmono;
	}
	fwrite(stereoShorts, 1, CHARS, mono);
	return;
}

void getMono() {
	int index;
	float fleft;
	float fright;
	float *pmono = monoFloats;
	short *pstereo = stereoShorts;
	for(index = 0; index < STEREO; index += 2) {
		fleft = (float) *(pstereo + index);
		fright = (float) *(pstereo + index + 1);
		*(pmono + (index / 2)) = (fleft + fright) / 2.0;
	}
	return;
}

void writeStereo() {
	int index;
	float *pleft = leftFloats;
	float *pright = rightFloats;
	short *pstereo = stereoShorts;
	for(index = 0; index < STEREO; index += 2) {
		*(pstereo + index) = (short) *(pleft + (index / 2));
		*(pstereo + index + 1) = (short) *(pright + (index / 2));
	}
	fwrite(stereoShorts, 1, CHARS, stereo);
	return;
}

void getStereo() {
	int index;
	float *pleft = leftFloats;
	float *pright = rightFloats;
	short *pstereo = stereoShorts;
	for(index = 0; index < STEREO; index += 2) {
		*(pleft + (index / 2)) = (float) *(pstereo + index);
		*(pright + (index / 2)) = (float) *(pstereo + index + 1);
	}
	return;
}

void readBlock() {
	int index;
	int *p = stereoShorts;
	fread(stereoShorts, 1, CHARS, input);
}

void copy(int seconds) {
	int index;
	int data_length = (seconds * 44100 * 4);
	int numblocks = data_length / CHARS;
	data_length = numblocks * CHARS;
	ReadHeader(input);
	WriteHeader(stereo, data_length);
	WriteHeader(mono, data_length);
	for (index = 0; index < numblocks; index++) {
		readBlock();
		getMono();
		getStereo();
		writeStereo();
		writeMono();
	}
}

void WAVFileMain(int argc, char** argv) {
	stereoShorts = malloc(CHARS);
	leftFloats = malloc(CHARS);
	rightFloats = malloc(CHARS);
	monoFloats = malloc(CHARS);
	input = fopen("input.wav", "rb");
	stereo = fopen("stereo.wav", "wb");
	mono = fopen("mono.wav", "wb");
	if (input == NULL) {
		printf("Unable to open input file\n");
		return;
	}
	if (stereo == NULL) {
		printf("Unable to open stereo output file\n");
		return;
	}
	if (mono == NULL) {
		printf("Unable to open mono output file");
		return;
	}
	copy(20);
	free(stereoShorts);
	free(leftFloats);
	free(rightFloats);
	free(monoFloats);
	fclose(input);
	fclose(stereo);
	fclose(mono);
}
