import java.awt.*;
import java.util.*;

public class FDUtils {
	
	public static int pixelsPerTime = FDView.pixelsPerTime;
	public static int minPixelsPerNote = FDView.minPixelsPerNote;
	
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
		int maxNote = FDEditor.maxNote - FDEditor.upperY;
		int pixelY = FDEditor.upperOffset;
		if(note > maxNote || note < FDEditor.minNote) return -1;
		for(int loopNote = maxNote; loopNote >= FDEditor.minNote; loopNote--) {
			if(note == loopNote) return pixelY;
			if(FDEditor.averageNoteToHarmonicID.containsKey(loopNote)) {
				pixelY += FDEditor.yStep;
			} else {
				pixelY += minPixelsPerNote;
			}
		}
		return -1;
	}
	
	public static boolean noteToDrawSegment(int note) {
		if(FDView.dataView == FDView.DataView.SELECTED_ONLY) {
			if(FDEditor.selectedHarmonicIDs.contains(note)) return true;
		}
		if(FDEditor.averageNoteToHarmonicID.containsKey(note)) return true;
		return false;
	}
	
	public static int pixelYToNote(int pixelY) {
		if(pixelY < FDEditor.upperOffset) return -1;
		int testNote = FDEditor.maxNote - FDEditor.upperY;
		int testPixelY = noteToPixelY(testNote);
		while(testPixelY < pixelY) {
			testNote--;
			testPixelY = noteToPixelY(testNote);
			if(testPixelY == -1) return -1;
		}
		return testNote + 1;
	}
	
	public static int timeToPixelX(int time) {
		return (time - FDEditor.leftX) * pixelsPerTime + FDEditor.leftOffset;
	}
	
	public static boolean timeToDrawSegment(int time) {
		if((FDEditor.xStep % pixelsPerTime) != 0) {
			System.out.println("FDUtils.timeToDrawSegement: pixelsPerTime must be whole divisor of xStep");
		}
		if(time % (FDEditor.xStep / pixelsPerTime) == 0) return true;
		return false;
	}
	
	public static int pixelXToTime(int pixelX) {
		pixelX -= FDEditor.leftOffset;
		int time = (pixelX - FDEditor.leftX) / pixelsPerTime;
		return time;
	}

	public static FDData getMaxDataInTimeRange(int startTime, int endTime, int note) {
		FDData returnVal = FDEditor.getData(startTime, note);
		for(int time = startTime + 1; time < endTime; time++) {
			FDData currentVal =  FDEditor.getData(time, note);
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
			FDUtils.SevenSegmentSmall(g, f, b, screenX, 
			                           screenY, 
			                           digitVal);
			screenX += FDEditor.xStep;
		}
	}
	
	public static void DrawIntegerVertical(Graphics g, Color b, Color f, int screenX, int screenY, int numdigits, int value) {
		for(int digitPlace = (int) Math.round(Math.pow(10, numdigits - 1)); digitPlace >= 1; digitPlace /= 10) {
			int digitVal = (int) Math.floor(value / digitPlace);
			value -= digitVal * digitPlace;
			g.setColor(b);
			g.fillRect(screenX, screenY, 6, 8);
			FDUtils.SevenSegmentSmall(g, f, b, screenX, 
			                           screenY, 
			                           digitVal);
			screenY += FDEditor.yStep;
		}
	}
	
	public static void DrawSegmentData(Graphics g, Color b, int screenX, int screenY, int digitVal) {
		Color black = new Color(0.0f, 0.0f, 0.0f);
		// int lowerScreenY = screenY + topYStep;				
		g.setColor(b);
		g.fillRect(screenX, screenY, FDEditor.xStep, FDEditor.yStep);;
		SevenSegmentSmall(g, black, b, screenX, screenY, digitVal);
		// SevenSegmentSmall(g, black, b, screenX, lowerScreenY, fractionVal);
		// g.setColor(black);
		// int bottomScreenY = screenY + yStep - 1;
		// g.drawLine(screenX, bottomScreenY, screenX + xStep, bottomScreenY);
	}
	
	//This takes two vertical lines, digitVal is above fraction val 
	public static void DrawSegmentData(Graphics g, Color b, int screenX, int screenY, int digitVal, int fractionVal) {
		Color black = new Color(0.0f, 0.0f, 0.0f);
		int lowerScreenY = screenY + FDEditor.yStep / 2;			
		g.setColor(b);
		g.fillRect(screenX, screenY, FDEditor.xStep, FDEditor.yStep);;
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
