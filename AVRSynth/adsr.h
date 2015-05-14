
extern const unsigned char adsrDelta[];

// ADSR1
extern unsigned char noteOn;
extern unsigned char adsr1up;
extern unsigned char adsr1PrevValue;
extern unsigned char adsr1CurrentValue[3];
extern unsigned char adsr1DeltaValue[2];
extern unsigned int  adsr1AttackTime;
extern unsigned int  adsr1DecayTime;
extern unsigned char adsr1Sustain;
extern unsigned int  adsr1ReleaseTime;

unsigned char noteOn;
unsigned char adsr1up;
unsigned char adsr1PrevValue;
unsigned char adsr1CurrentValue[3];
unsigned char adsr1DeltaValue[2];
unsigned int  adsr1AttackTime;
unsigned int  adsr1DecayTime;
unsigned char adsr1Sustain;
unsigned int  adsr1ReleaseTime;

void updateEnvelope() {
	if(!noteOn) {
		adsr1up = 0;
		adsr1DeltaValue[0] = adsrDelta[adsr1ReleaseTime * 2];
		adsr1DeltaValue[1] = adsrDelta[adsr1ReleaseTime * 2 + 1];
		return;
	}
	if(adsr1up) {
		adsr1up = 0;
		adsr1DeltaValue[0] = adsrDelta[adsr1DecayTime * 2];
		adsr1DeltaValue[1] = adsrDelta[adsr1DecayTime * 2 + 1];
	}
	if(adsr1PrevValue == 0) {
		adsr1DeltaValue[0] = 0;
		adsr1DeltaValue[1] = 0;
		return;
	}
}

extern const unsigned char adsrDelta[] PROGMEM = {255, 255, 69, 254, 142, 252, 218, 250, 40, 249, 122, 247, 207, 245, 38, 244, 129, 242,
	222, 240, 62, 239, 161, 237, 6, 236, 111, 234, 218, 232, 72, 231, 184, 229,
	43, 228, 161, 226, 26, 225, 149, 223, 19, 222, 148, 220, 23, 219, 156, 217,
	36, 216, 175, 214, 60, 213, 204, 211, 94, 210, 243, 208, 138, 207, 36, 206,
	192, 204, 94, 203, 255, 201, 162, 200, 72, 199, 239, 197, 154, 196, 70, 195,
	245, 193, 166, 192, 89, 191, 15, 190, 198, 188, 128, 187, 61, 186, 251, 184,
	187, 183, 126, 182, 67, 181, 10, 180, 211, 178, 158, 177, 107, 176, 59, 175,
	12, 174, 224, 172, 181, 171, 140, 170, 102, 169, 65, 168, 31, 167, 254, 165,
	224, 164, 195, 163, 168, 162, 143, 161, 120, 160, 99, 159, 80, 158, 62, 157,
	47, 156, 33, 155, 21, 154, 11, 153, 3, 152, 252, 150, 247, 149, 244, 148,
	243, 147, 244, 146, 246, 145, 250, 144, 255, 143, 7, 143, 16, 142, 26, 141,
	39, 140, 53, 139, 68, 138, 85, 137, 104, 136, 125, 135, 147, 134, 170, 133,
	195, 132, 222, 131, 250, 130, 24, 130, 56, 129, 88, 128, 123, 127, 159, 126,
	196, 125, 235, 124, 19, 124, 61, 123, 104, 122, 148, 121, 194, 120, 242, 119,
	35, 119, 85, 118, 137, 117, 190, 116, 244, 115, 44, 115, 101, 114, 159, 113,
	219, 112, 24, 112, 87, 111, 150, 110, 215, 109, 26, 109, 93, 108, 162, 107,
	232, 106, 48, 106, 120, 105, 194, 104, 13, 104, 89, 103, 167, 102, 246, 101,
	70, 101, 151, 100, 233, 99, 60, 99, 145, 98, 231, 97, 62, 97, 150, 96,
	239, 95, 73, 95, 165, 94, 1, 94, 95, 93, 190, 92, 30, 92, 126, 91,
	224, 90, 67, 90, 168, 89, 13, 89, 115, 88, 218, 87, 66, 87, 172, 86,
	22, 86, 129, 85, 238, 84, 91, 84, 201, 83, 57, 83, 169, 82, 26, 82,
	140, 81, 0, 81, 116, 80, 233, 79, 95, 79, 214, 78, 78, 78, 198, 77,
	64, 77, 187, 76, 54, 76, 178, 75, 48, 75, 174, 74, 45, 74, 173, 73,
	46, 73, 175, 72, 50, 72, 181, 71, 57, 71, 190, 70, 68, 70, 203, 69,
	82, 69, 218, 68, 99, 68, 237, 67, 120, 67, 4, 67, 144, 66, 29, 66,
	171, 65, 57, 65, 201, 64, 89, 64, 234, 63, 123, 63, 14, 63, 161, 62,
	53, 62, 201, 61, 94, 61, 244, 60, 139, 60, 35, 60, 187, 59, 84, 59,
	237, 58, 135, 58, 34, 58, 190, 57, 90, 57, 247, 56, 149, 56, 51, 56,
	210, 55, 114, 55, 18, 55, 179, 54, 84, 54, 246, 53, 153, 53, 61, 53,
	225, 52, 133, 52, 43, 52, 209, 51, 119, 51, 30, 51, 198, 50, 110, 50,
	23, 50, 193, 49, 107, 49, 21, 49, 193, 48, 108, 48, 25, 48, 198, 47,
	115, 47, 33, 47, 208, 46, 127, 46, 47, 46, 223, 45, 144, 45, 65, 45,
	243, 44, 165, 44, 88, 44, 12, 44, 192, 43, 116, 43, 41, 43, 222, 42,
	148, 42, 75, 42, 2, 42, 185, 41, 113, 41, 42, 41, 227, 40, 156, 40,
	86, 40, 16, 40, 203, 39, 134, 39, 66, 39, 254, 38, 187, 38, 120, 38,
	53, 38, 243, 37, 178, 37, 113, 37, 48, 37, 240, 36, 176, 36, 113, 36,
	50, 36, 243, 35, 181, 35, 120, 35, 58, 35, 254, 34, 193, 34, 133, 34,
	73, 34, 14, 34, 211, 33, 153, 33, 95, 33, 37, 33, 236, 32, 179, 32,
	123, 32, 67, 32, 11, 32, 212, 31, 157, 31, 102, 31, 48, 31, 250, 30,
	196, 30, 143, 30, 91, 30, 38, 30, 242, 29, 190, 29, 139, 29, 88, 29,
	37, 29, 243, 28, 193, 28, 143, 28, 94, 28, 45, 28, 252, 27, 204, 27,
	156, 27, 108, 27, 61, 27, 14, 27, 223, 26, 177, 26, 131, 26, 85, 26,
	39, 26, 250, 25, 205, 25, 161, 25, 117, 25, 73, 25, 29, 25, 242, 24,
	199, 24, 156, 24, 113, 24, 71, 24, 29, 24, 243, 23, 202, 23, 161, 23,
	120, 23, 80, 23, 39, 23, 255, 22, 216, 22, 176, 22, 137, 22, 98, 22,
	59, 22, 21, 22, 239, 21, 201, 21, 163, 21, 126, 21, 89, 21, 52, 21,
	15, 21, 235, 20, 199, 20, 163, 20, 127, 20, 92, 20, 57, 20, 22, 20,
	243, 19, 209, 19, 175, 19, 141, 19, 107, 19, 73, 19, 40, 19, 7, 19,
	230, 18, 197, 18, 165, 18, 133, 18, 101, 18, 69, 18, 38, 18, 6, 18,
	231, 17, 200, 17, 169, 17, 139, 17, 109, 17, 79, 17, 49, 17, 19, 17,
	245, 16, 216, 16, 187, 16, 158, 16, 129, 16, 101, 16, 73, 16, 45, 16,
	17, 16, 245, 15, 217, 15, 190, 15, 163, 15, 136, 15, 109, 15, 82, 15,
	56, 15, 30, 15, 3, 15, 234, 14, 208, 14, 182, 14, 157, 14, 132, 14,
	106, 14, 82, 14, 57, 14, 32, 14, 8, 14, 240, 13, 216, 13, 192, 13,
	168, 13, 144, 13, 121, 13, 98, 13, 75, 13, 52, 13, 29, 13, 6, 13,
	240, 12, 217, 12, 195, 12, 173, 12, 151, 12, 129, 12, 108, 12, 86, 12,
	65, 12, 44, 12, 23, 12, 2, 12, 237, 11, 217, 11, 196, 11, 176, 11,
	156, 11, 136, 11, 116, 11, 96, 11, 76, 11, 57, 11, 37, 11, 18, 11,
	255, 10, 236, 10, 217, 10, 198, 10, 180, 10, 161, 10, 143, 10, 125, 10,
	107, 10, 89, 10, 71, 10, 53, 10, 35, 10, 18, 10, 1, 10, 239, 9,
	222, 9, 205, 9, 188, 9, 171, 9, 155, 9, 138, 9, 122, 9, 105, 9,
	89, 9, 73, 9, 57, 9, 41, 9, 25, 9, 9, 9, 250, 8, 234, 8,
	219, 8, 204, 8, 188, 8, 173, 8, 158, 8, 143, 8, 129, 8, 114, 8,
	99, 8, 85, 8, 70, 8, 56, 8, 42, 8, 28, 8, 14, 8, 0, 8,
	242, 7, 228, 7, 215, 7, 201, 7, 188, 7, 174, 7, 161, 7, 148, 7,
	135, 7, 122, 7, 109, 7, 96, 7, 83, 7, 71, 7, 58, 7, 46, 7,
	33, 7, 21, 7, 9, 7, 253, 6, 241, 6, 229, 6, 217, 6, 205, 6,
	193, 6, 181, 6, 170, 6, 158, 6, 147, 6, 136, 6, 124, 6, 113, 6,
	102, 6, 91, 6, 80, 6, 69, 6, 58, 6, 47, 6, 37, 6, 26, 6,
	16, 6, 5, 6, 251, 5, 240, 5, 230, 5, 220, 5, 210, 5, 200, 5,
	190, 5, 180, 5, 170, 5, 160, 5, 151, 5, 141, 5, 131, 5, 122, 5,
	112, 5, 103, 5, 94, 5, 84, 5, 75, 5, 66, 5, 57, 5, 48, 5,
	39, 5, 30, 5, 21, 5, 12, 5, 4, 5, 251, 4, 242, 4, 234, 4,
	225, 4, 217, 4, 209, 4, 200, 4, 192, 4, 184, 4, 176, 4, 168, 4,
	160, 4, 152, 4, 144, 4, 136, 4, 128, 4, 120, 4, 112, 4, 105, 4,
	97, 4, 90, 4, 82, 4, 75, 4, 67, 4, 60, 4, 53, 4, 45, 4,
	38, 4, 31, 4, 24, 4, 17, 4, 10, 4, 3, 4, 252, 3, 245, 3,
	238, 3, 231, 3, 225, 3, 218, 3, 211, 3, 205, 3, 198, 3, 192, 3,
	185, 3, 179, 3, 172, 3, 166, 3, 160, 3, 153, 3, 147, 3, 141, 3,
	135, 3, 129, 3, 123, 3, 117, 3, 111, 3, 105, 3, 99, 3, 93, 3,
	87, 3, 81, 3, 76, 3, 70, 3, 64, 3, 59, 3, 53, 3, 48, 3,
	42, 3, 37, 3, 31, 3, 26, 3, 21, 3, 15, 3, 10, 3, 5, 3,
	255, 2, 250, 2, 245, 2, 240, 2, 235, 2, 230, 2, 225, 2, 220, 2,
	215, 2, 210, 2, 205, 2, 200, 2, 196, 2, 191, 2, 186, 2, 181, 2,
	177, 2, 172, 2, 167, 2, 163, 2, 158, 2, 154, 2, 149, 2, 145, 2,
	140, 2, 136, 2, 132, 2, 127, 2, 123, 2, 119, 2, 114, 2, 110, 2,
	106, 2, 102, 2, 98, 2, 94, 2, 89, 2, 85, 2, 81, 2, 77, 2,
	73, 2, 69, 2, 66, 2, 62, 2, 58, 2, 54, 2, 50, 2, 46, 2,
	43, 2, 39, 2, 35, 2, 31, 2, 28, 2, 24, 2, 20, 2, 17, 2,
	13, 2, 10, 2, 6, 2, 3, 2, 255, 1, 252, 1, 248, 1, 245, 1,
	242, 1, 238, 1, 235, 1, 232, 1, 228, 1, 225, 1, 222, 1, 219, 1,
	215, 1, 212, 1, 209, 1, 206, 1, 203, 1, 200, 1, 197, 1, 194, 1,
	191, 1, 188, 1, 185, 1, 182, 1, 179, 1, 176, 1, 173, 1, 170, 1,
	167, 1, 164, 1, 161, 1, 159, 1, 156, 1, 153, 1, 150, 1, 147, 1,
	145, 1, 142, 1, 139, 1, 137, 1, 134, 1, 131, 1, 129, 1, 126, 1,
	124, 1, 121, 1, 118, 1, 116, 1, 113, 1, 111, 1, 108, 1, 106, 1,
	104, 1, 101, 1, 99, 1, 96, 1, 94, 1, 92, 1, 89, 1, 87, 1,
	85, 1, 82, 1, 80, 1, 78, 1, 76, 1, 73, 1, 71, 1, 69, 1,
	67, 1, 65, 1, 62, 1, 60, 1, 58, 1, 56, 1, 54, 1, 52, 1,
	50, 1, 48, 1, 46, 1, 44, 1, 42, 1, 39, 1, 38, 1, 36, 1,
	34, 1, 32, 1, 30, 1, 28, 1, 26, 1, 24, 1, 22, 1, 20, 1,
	18, 1, 16, 1, 15, 1, 13, 1, 11, 1, 9, 1, 7, 1, 6, 1,
	4, 1, 2, 1, 0, 1, 255, 0, 253, 0, 251, 0, 249, 0, 248, 0,
	246, 0, 244, 0, 243, 0, 241, 0, 240, 0, 238, 0, 236, 0, 235, 0,
	233, 0, 232, 0, 230, 0, 228, 0, 227, 0, 225, 0, 224, 0, 222, 0,
	221, 0, 219, 0, 218, 0, 216, 0, 215, 0, 214, 0, 212, 0, 211, 0,
	209, 0, 208, 0, 206, 0, 205, 0, 204, 0, 202, 0, 201, 0, 200, 0,
	198, 0, 197, 0, 196, 0, 194, 0, 193, 0, 192, 0, 190, 0, 189, 0,
	188, 0, 186, 0, 185, 0, 184, 0, 183, 0, 182, 0, 180, 0, 179, 0,
	178, 0, 177, 0, 175, 0, 174, 0, 173, 0, 172, 0, 171, 0, 170, 0,
	168, 0, 167, 0, 166, 0, 165, 0, 164, 0, 163, 0, 162, 0, 161, 0,
	160, 0, 159, 0, 157, 0, 156, 0, 155, 0, 154, 0, 153, 0, 152, 0,
	151, 0, 150, 0, 149, 0, 148, 0, 147, 0, 146, 0, 145, 0, 144, 0,
	143, 0, 142, 0, 141, 0, 140, 0, 139, 0, 138, 0, 138, 0, 137, 0,
	136, 0, 135, 0, 134, 0, 133, 0, 132, 0, 131, 0, 130, 0, 129, 0,
	129, 0, 128, 0, 127, 0, 126, 0, 125, 0, 124, 0, 123, 0, 123, 0,
	122, 0, 121, 0, 120, 0, 119, 0, 118, 0, 118, 0, 117, 0, 116, 0,
	115, 0, 115, 0, 114, 0, 113, 0, 112, 0, 111, 0, 111, 0, 110, 0,
	109, 0, 109, 0, 108, 0, 107, 0, 106, 0, 106, 0, 105, 0, 104, 0,
	103, 0, 103, 0, 102, 0, 101, 0, 101, 0, 100, 0, 99, 0, 99, 0,
	98, 0, 97, 0, 97, 0, 96, 0, 95, 0, 95, 0, 94, 0, 93, 0,
	93, 0, 92, 0, 92, 0, 91, 0, 90, 0, 90, 0, 89, 0, 89, 0,
	88, 0, 87, 0, 87, 0, 86, 0, 86, 0, 85, 0, 84, 0, 84, 0,
	83, 0, 83, 0, 82, 0, 82, 0, 81, 0, 81, 0, 80, 0, 79, 0,
	79, 0, 78, 0, 78, 0, 77, 0, 77, 0, 76, 0, 76, 0, 75, 0,
	75, 0, 74, 0, 74, 0, 73, 0, 73, 0, 72, 0, 72, 0, 71, 0,
	71, 0, 70, 0, 70, 0, 69, 0, 69, 0, 68, 0, 68, 0, 68, 0,
	67, 0, 67, 0, 66, 0, 66, 0, 65, 0, 65, 0, 64, 0};

