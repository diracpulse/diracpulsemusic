package main;

import java.lang.*;
import java.util.*;
import java.io.*;

class CubicSpline {
	
	double[] x;
	double[] y;
	double[] y2;
	int n;
	
	CubicSpline(double[] xInput, double[] yInput) {
		int index;
		int i, k;
		double p, qn, sig, un;
		double[] u;
		if (xInput.length != yInput.length) {
			System.out.print("CubicSpline (constructor) x and y arrays not of same length\n");
			System.exit(0);
		}
		n = xInput.length;
		u = new double[n];
		x = new double[n + 1];
		y = new double[n + 1];
		y2 = new double[n + 1];
		x[0] = 0;
		y[0] = 0;
		y2[0] = 0;
		for(index = 1; index <= n; index++) {
			x[index] = xInput[index - 1];
			y[index] = yInput[index - 1];
		}
		y2[1] = u[1] = 0.0; // set lower boundary to natural
		for(i = 2; i <= n - 1; i++) {
			sig = (x[i] - x[i - 1]) / (x[i + 1] - x[i - 1]);
			p = sig * y2[i - 1] + 2.0;
			y2[i] = (sig - 1.0) / p;
			u[i] = (y[i + 1] - y[i]) / (x[i + 1] - x[i])
			     - (y[i] - y[i - 1]) / (x[i] - x[i - 1]);
			u[i] = (6.0 * u[i] / (x[i + 1] - x[i - 1]) - sig * u[i - 1]) / p;
		}
		qn = un = 0.0;
		y2[n] = (un - qn * u[n - 1]) / (qn * y2[n - 1] + 1.0);
		for (k = n - 1; k >= 1; k--) {
			y2[k] = y2[k] * y2[k + 1] + u[k];
		}
	}
	
	double interpolate(double xVal) {
		int klo, khi, k;
		double h, b, a;
		double yVal;
		klo = 1;
		khi = n;
		while((khi - klo) > 1) {
			k = (khi + klo) >> 1;
			if (x[k] > xVal) {
				khi = k;
			} else {
				klo = k;
			}
		}
		h = x[khi] - x[klo];
		if (h == 0.0) {
			System.out.print("Bad x array input to CubicSpline");
			System.exit(0);
		}
		a = (x[khi] - xVal) / h;
		b = (xVal - x[klo]) / h;
		yVal = a * y[klo] + b * y[khi] + 
		       ((a * a * a - a) * y2[klo] + (b * b * b - b) * y2[khi]) * (h * h) / 6.0;
		return yVal;
	}

}
