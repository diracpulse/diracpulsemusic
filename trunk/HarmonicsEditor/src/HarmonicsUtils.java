import java.awt.*;
import java.util.*;

public class HarmonicsUtils {
	
	public static int timesPerPixel = HarmonicsView.timesPerPixel;
	public static int minPixelsPerNote = HarmonicsView.minPixelsPerNote;
	
	public static class Range {
		
		int lower;
		int upper;
		
		public Range(int val1, int val2) {
			if(val1 < val2) {
				lower = val1;
				upper = val2;
			} else {
				lower = val2;
				upper = val1;				
			}
		}
		
		public int getLower() {
			return upper;
		}
		
		public int getUpper() {
			return upper;
		}
	}

	public static int noteToPixelY(int note) {
		int maxNote = HarmonicsEditor.maxNote - HarmonicsEditor.upperY;
		int pixelY = HarmonicsEditor.upperOffset;
		if(note > maxNote || note < HarmonicsEditor.minNote) return -1;
		for(int loopNote = maxNote; loopNote >= HarmonicsEditor.minNote; loopNote--) {
			if(note == loopNote) return pixelY;
			for(Harmonic harmonic: HarmonicsEditor.harmonicIDToHarmonic.values()) {
				if(harmonic.getAverageNote() == loopNote) pixelY += HarmonicsEditor.yStep;
			}
		}
		return -1;
	}
	
	public static boolean noteToDrawSegment(int note) {
		for(Harmonic harmonic: HarmonicsEditor.harmonicIDToHarmonic.values()) {
			if(harmonic.getAverageNote() == note) return true;
		}
		return false;
	}
	
	public static int pixelYToNote(int pixelY) {
		if(pixelY < HarmonicsEditor.upperOffset) return -1;
		int testNote = HarmonicsEditor.maxNote - HarmonicsEditor.upperY;
		int testPixelY = noteToPixelY(testNote);
		while(testPixelY < pixelY) {
			testNote--;
			testPixelY = noteToPixelY(testNote);
			if(testPixelY == -1) return -1;
		}
		return testNote + 1;
	}
	
	public static int timeToPixelX(int time) {
		return (time - HarmonicsEditor.leftX) / timesPerPixel + HarmonicsEditor.leftOffset;
	}
	
	public static boolean timeToDrawSegment(int time) {
		if(time % (timesPerPixel * HarmonicsEditor.xStep) == 0) return true;
		return false;
	}
	
	public static HarmonicsUtils.Range pixelXToTimeRange(int pixelX) {
		pixelX -= HarmonicsEditor.leftOffset;
		int startTime = (pixelX - HarmonicsEditor.leftX) * timesPerPixel;
		int endTime = startTime + HarmonicsEditor.xStep * timesPerPixel - 1;
		return new HarmonicsUtils.Range(startTime, endTime);
	}

	public static FDData getMaxDataInTimeRange(int startTime, int endTime, int note) {
		FDData returnVal = HarmonicsEditor.getSelected(startTime, note);
		for(int time = startTime + 1; time < endTime; time++) {
			FDData currentVal =  HarmonicsEditor.getSelected(time, note);
			if(currentVal == null) continue;
			if(returnVal == null) {
				returnVal = currentVal;
				continue;
			}
			if(currentVal.getLogAmplitude() > returnVal.getLogAmplitude()) {
				returnVal = currentVal;
			}
		}
		return returnVal;
	}

	public static void DrawIntegerHorizontal(Graphics g, Color b, Color f, int screenX, int screenY, int numdigits, int value) {
		for(int digitPlace = (int) Math.round(Math.pow(10, numdigits - 1)); digitPlace >= 1; digitPlace /= 10) {
			int digitVal = (int) Math.floor(value / digitPlace);
			value -= digitVal * digitPlace;
			g.setColor(b);
			g.fillRect(screenX, screenY, 6, 8);
			HarmonicsUtils.SevenSegmentSmall(g, f, b, screenX, 
			                           screenY, 
			                           digitVal);
			screenX += HarmonicsEditor.xStep;
		}
	}
	
	public static void DrawIntegerVertical(Graphics g, Color b, Color f, int screenX, int screenY, int numdigits, int value) {
		for(int digitPlace = (int) Math.round(Math.pow(10, numdigits - 1)); digitPlace >= 1; digitPlace /= 10) {
			int digitVal = (int) Math.floor(value / digitPlace);
			value -= digitVal * digitPlace;
			g.setColor(b);
			g.fillRect(screenX, screenY, 6, 8);
			HarmonicsUtils.SevenSegmentSmall(g, f, b, screenX, 
			                           screenY, 
			                           digitVal);
			screenY += HarmonicsEditor.yStep;
		}
	}
	
	public static void DrawSegmentData(Graphics g, Color b, int screenX, int screenY, int digitVal) {
		Color black = new Color(0.0f, 0.0f, 0.0f);
		// int lowerScreenY = screenY + topYStep;				
		g.setColor(b);
		g.fillRect(screenX, screenY, HarmonicsEditor.xStep, HarmonicsEditor.yStep);;
		SevenSegmentSmall(g, black, b, screenX, screenY, digitVal);
		// SevenSegmentSmall(g, black, b, screenX, lowerScreenY, fractionVal);
		// g.setColor(black);
		// int bottomScreenY = screenY + yStep - 1;
		// g.drawLine(screenX, bottomScreenY, screenX + xStep, bottomScreenY);
	}
	
	//This takes two vertical lines, digitVal is above fraction val 
	public static void DrawSegmentData(Graphics g, Color b, int screenX, int screenY, int digitVal, int fractionVal) {
		Color black = new Color(0.0f, 0.0f, 0.0f);
		int lowerScreenY = screenY + HarmonicsEditor.yStep / 2;			
		g.setColor(b);
		g.fillRect(screenX, screenY, HarmonicsEditor.xStep, HarmonicsEditor.yStep);;
		SevenSegmentSmall(g, black, b, screenX, screenY, digitVal);
		SevenSegmentSmall(g, black, b, screenX, lowerScreenY, fractionVal);
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
