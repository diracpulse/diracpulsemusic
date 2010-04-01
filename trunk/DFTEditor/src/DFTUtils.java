import java.awt.*;
import java.util.*;

public class DFTUtils {

	// needed by TBSView.DrawAmpSums and TBSView.DrawMaxAmpAtFreq 
	public static float getMaxValue(TreeMap<Integer, Float> values, int startVal, int step) {
		float maxValue = 0.0f;
		int endVal = startVal + step;
		for(int val = startVal; val < endVal; val++) {
			if (values.containsKey(val)) {
				if(values.get(val) > maxValue) maxValue = values.get(val);
			}
		}
		return maxValue;
	}
	
	public static DFTModel.TFA getMaxValue(int time, int freq) {
		TreeMap<Integer, Float> freqToAmp;
		DFTModel.TFA returnVal = new DFTModel.TFA(0, 0, 0.0f);
		freq = DFTEditor.maxRealFreq - freq;
		int endTime = time + getTimeIncrement();
		int endFreq = freq + getFreqIncrement();
		for(int timeIndex = time; timeIndex < endTime; timeIndex++) {
			for(int freqIndex = freq; freqIndex < endFreq; freqIndex++) {
				if(DFTEditor.timeToFreqToAmp.containsKey(timeIndex)) {
					freqToAmp = DFTEditor.timeToFreqToAmp.get(timeIndex);
					if(freqToAmp.containsKey(freqIndex)) {
						float amplitude = freqToAmp.get(freqIndex);
						if(amplitude > returnVal.getAmplitude()) {
							returnVal = new DFTModel.TFA(timeIndex, freqIndex, amplitude);
						}
					}
				}
			}
		}
		return returnVal;
	}
	
	public static int getTimeIncrement() {
		return DFTEditor.timeCollapse;
	}
	
	public static int getFreqIncrement() {
		return DFTEditor.freqCollapse;
	}
	
	public static int getIncrement(TreeMap<Integer, Boolean> isCollapsed, int value, int step) {
		int key = value / step;
		if(isCollapsed == null) return 1;
		// key may be too large if bottom of screen extends beyond data
		if(!isCollapsed.containsKey(key)) {
			return 1;
		}
		if(isCollapsed.get(key).booleanValue() == true) {
			return step;
		}
		return 1;
	}
	
	public static void DrawSegmentData(Graphics g, Color b, int screenX, int screenY, int digitVal, int fractionVal) {
		Color black = new Color(0.0f, 0.0f, 0.0f);
		// int lowerScreenY = screenY + topYStep;				
		g.setColor(b);
		g.fillRect(screenX, screenY, DFTEditor.xStep, DFTEditor.yStep);;
		SevenSegmentSmall(g, black, b, screenX, screenY, digitVal);
		// SevenSegmentSmall(g, black, b, screenX, lowerScreenY, fractionVal);
		// g.setColor(black);
		// int bottomScreenY = screenY + yStep - 1;
		// g.drawLine(screenX, bottomScreenY, screenX + xStep, bottomScreenY);
	}
	
		// (x, y) = (left, top)
	// Segment numbering (0 for OFF, otherwise ON):
	//  0
	// 6 1
	//  2
	// 5 3
	//  4
	// Color f = forground (segment) color
	// Color g = background (segment block) color
	// int digitVal = digit to display (0 - 9)
	public static void SevenSegmentSmall(Graphics g, Color f, Color b, int x, int y, int digitVal) {
		int[] segments;
		int[][] digits = {	{1, 1, 0, 1, 1, 1, 1},
							{0, 1, 0, 1, 0, 0, 0},
							{1, 1, 1, 0, 1, 1, 0},
							{1, 1, 1, 1, 1, 0, 0},
							{0, 1, 1, 1, 0, 0, 1},
							{1, 0, 1, 1, 1, 0, 1},
							{1, 0, 1, 1, 1, 1, 1},
							{1, 1, 0, 1, 0, 0, 0},
							{1, 1, 1, 1, 1, 1, 1},
							{1, 1, 1, 1, 1, 0, 1}
						 };
		int[] errDigit = 	{1, 0, 1, 0, 1, 1, 1};
		if((digitVal >= 0) && (digitVal < 10)) {
			segments = digits[digitVal];
		} else {
			segments = errDigit;
		}
		// draw background
		// g.setColor(b);
		// g.fillRect(x, y, 6, 8);
		g.setColor(f);
		// top segment
		if(segments[0] != 0) g.drawLine((x + 2), (y + 1), (x + 4), (y + 1));
		// middle segment
		if(segments[2] != 0) g.drawLine((x + 2), (y + 4), (x + 4), (y + 4));
		// bottom segment
		if(segments[4] != 0) g.drawLine((x + 2), (y + 7), (x + 4), (y + 7));
		// upper right segment
		if(segments[1] != 0) g.drawLine((x + 5), (y + 2), (x + 5), (y + 3));
		// lower right segment
		if(segments[3] != 0) g.drawLine((x + 5), (y + 5), (x + 5), (y + 6));
		// lower left segment
		if(segments[5] != 0) g.drawLine((x + 1), (y + 5), (x + 1), (y + 6));
		// upper left segment
		if(segments[6] != 0) g.drawLine((x + 1), (y + 2), (x + 1), (y + 3));
	}
	
}
