
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
//#include "main.h"

/* used by DFTBinStep */
#define MAXORDER 64
#define WINDOWSIZE 88200

double Filter[MAXORDER + 1];
double KaiserWindow[WINDOWSIZE];

const double TwoPI = 6.283185307179586476925286766559;
const double OnePI = 3.1415926535897932384626433832795;
const double HalfPI = 1.5707963267948966192313216916398;
const double SamplingRate = 44100.0;

int FilterOrder = MAXORDER;
double Frequency;
double BinFrequency;
double Alpha;

double BesselI0(double x);
void CreateWindow(double* window, int size, double alpha);
void CreateFilter(double w0, double w1, int order, double alpha);
void LPFilter(double freq, int order, double alpha);
void HPFilter(double freq, int order, double alpha);
void BPFilter(double freq, int order, double alpha);
double DFTFreq(double freq);
void BlockDFT(double LowTest, double HighTest, const char *TestName);

double BesselI0(double x) {
   double denominator;
   double numerator;
   double z;

   if (x == 0.0) {
      return 1.0;
   } else {
      z = x * x;
      numerator = (z* (z* (z* (z* (z* (z* (z* (z* (z* (z* (z* (z* (z* 
                     (z* 0.210580722890567e-22  + 0.380715242345326e-19 ) +
                         0.479440257548300e-16) + 0.435125971262668e-13 ) +
                         0.300931127112960e-10) + 0.160224679395361e-7  ) +
                         0.654858370096785e-5)  + 0.202591084143397e-2  ) +
                         0.463076284721000e0)   + 0.754337328948189e2   ) +
                         0.830792541809429e4)   + 0.571661130563785e6   ) +
                         0.216415572361227e8)   + 0.356644482244025e9   ) +
                         0.144048298227235e10);

      denominator = (z*(z*(z-0.307646912682801e4)+
                       0.347626332405882e7)-0.144048298227235e10);
   }

   return -numerator/denominator;
}

double DFTFreq(double freq) {
	int n;
	double dn;
	double amplitude;
	double sampleVal;
	double length = (double) FilterOrder;
	double sinsum = 0.0;
	double cossum = 0.0;
	double arg = TwoPI * (freq / SamplingRate);
	for(n = 0; n < FilterOrder; n++) {
		dn = (double) n;
		sampleVal = Filter[n];
		sinsum += sin(arg * dn) * sampleVal;
		cossum += cos(arg * dn) * sampleVal;
	}
	amplitude = (sinsum * sinsum) + (cossum * cossum);
	amplitude = sqrt(amplitude);
	return amplitude;
}

void LPFilter(double freq, int order, double alpha) {
	Alpha = alpha;
	FilterOrder = order;
	Frequency = freq;
	double w = 2.0 * (freq / SamplingRate);
	double w0 = 0.0;
	double w1 = w * OnePI;
	CreateFilter(w0, w1, order, alpha);
}

void HPFilter(double freq, int order, double alpha) {
	Alpha = alpha;
	FilterOrder = order;
	Frequency = freq;
	double w = 2.0 * (freq / SamplingRate);
	double w0 = OnePI;
	double w1 = (1.0 - w) * OnePI;
	CreateFilter(w0, w1, order, alpha);
}

void BPFilter(double freq, int order, double alpha) {
	Alpha = alpha;
	FilterOrder = order;
	Frequency = freq;
	double w = 2.0 * (freq / SamplingRate);
	double w0 = w * OnePI;
	double w1 = w / -4.0;
	CreateFilter(w0, w1, order, alpha);
}

void CreateWindow(double* window, int size, double alpha) {
   double sumvalue = 0.0;
   int i;
   
   for (i=0; i<size/2; i++) {
      sumvalue += BesselI0(OnePI * alpha * sqrt(1.0 - pow(4.0*i/size - 1.0, 2)));
      window[i] = sumvalue;
   }

   // need to add one more value to the nomalization factor at size/2:
   sumvalue += BesselI0(OnePI * alpha * sqrt(1.0 - pow(4.0*(size/2)/size-1.0, 2)));

   // normalize the window and fill in the righthand side of the window:
   for (i=0; i<size/2; i++) {
      window[i] = sqrt(window[i]/sumvalue);
      window[size-1-i] = window[i];
   }
   
   for(i = 0; i < size; i++) {
	   //printf("%f %d %f\n", alpha, i, window[i]);
   }
}

void CreateFilter(double w0, double w1, int order, double alpha) {
	int m = order / 2;
	int n;
	double dn;
	double dm = (double) m;
	double dorder = (double) order;
	double arg = TwoPI / (dorder - 1.0);
	double I0alpha;
	double FilterGain;
	double average;

	I0alpha = BesselI0(alpha);
	
	Filter[0] = w1 / OnePI;
		
	for (n=1; n <= m; n++) {
		dn = (double) n;
		Filter[n] = sin(dn*w1)*cos(dn*w0)/(dn*OnePI);
		Filter[n] *= BesselI0(alpha * sqrt(1.0 - (dn/dm) * (dn/dm))) / I0alpha;
	}
		
	// shift impulse response to make filter causal:
	for (n=m+1; n<=order; n++) Filter[n] = Filter[n - m];
	for (n=0; n<=m-1; n++) Filter[n] = Filter[order - n];
	Filter[m] = w1 / OnePI;
	return;
}

void BlockDFT(double LowTest, double HighTest, const char *TestName) {
	double freq;
	double binFreq;
	double amp;
	double ampLow;
	double ampHigh;
	int HighTrue = 0;
	int LowTrue = 0;
	ampLow = DFTFreq(LowTest);
	ampHigh = DFTFreq(HighTest);
	if((ampHigh > 0.95) && (ampLow < .01)) {
		printf("%s %f %d %f %f %f %f %f\n", TestName, Alpha, FilterOrder, Frequency, LowTest, ampLow, HighTest, ampHigh);
	} else {
		printf("X");
	}
	return;
}

int testFilter() {
	int order;
	double dorder;
	double freq;
	double binFreq;
	double binStep;
	double minFreq;
	double maxFreq;
	double startFreq;
	double endFreq;
	double high = 16384.0;
	double middle = 11025.0;
	double low = 8192.0;
	double halfmiddle = middle / 2.0;
	double alpha = 6.5;
	printf("\nUpperHP\n");
	for(freq = low; freq < high; freq += 16.0) {
			HPFilter(freq, 64, alpha);
			BlockDFT(low, middle, "UpperHP");
	}
	printf("\nLowerHP\n");
	for(freq = halfmiddle; freq < low; freq += 1.0) {
			HPFilter(freq, 64, alpha);
			BlockDFT(halfmiddle, low, "LowerHP");
	}
	printf("\nLowerLP\n");
	for(freq = middle; freq < high; freq += 16.0) {
			LPFilter(freq, 48, alpha);
			BlockDFT(high, middle, "LowerLP");
	}
	printf("\nDecimate\n");
	for(freq = low; freq < middle; freq += 16.0) {
			LPFilter(freq, 64, alpha);
			BlockDFT(middle, low, "Decimate");
	}
	for(alpha = 5.0; alpha < 10.0; alpha += 0.5) {
		CreateWindow(KaiserWindow, WINDOWSIZE, alpha);
	}
	return 0;
}
