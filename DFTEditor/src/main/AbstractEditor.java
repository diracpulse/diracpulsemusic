package main;

import java.util.TreeMap;

public interface AbstractEditor {
	
	//public static TreeMap<Long, Harmonic> harmonicIDToHarmonic = null;
	abstract void drawPlayTime(int offsetInMillis);
	abstract int getMaxViewTimeInMillis();
	abstract void createPCMDataLinear();
	abstract void createPCMDataLinearCubicSpline();
	abstract void createPCMDataLinearNoise();
	abstract void playPCMData();
}
