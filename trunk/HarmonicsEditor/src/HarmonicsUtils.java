import java.awt.*;
import java.util.*;

public class HarmonicsUtils {
	
	public static int pixelsPerTime = HarmonicsView.pixelsPerTime;
	public static int pixelsPerNote = HarmonicsView.pixelsPerNote;
	public static TreeSet<Integer> currentNotes = null;
	public static TreeMap<Integer, Integer> noteToPixelYMap = null;
	
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
		if(noteToPixelYMap == null) compileNoteToPixelY();
		return noteToPixelY(note, true);
	}
	
	public static int noteToPixelY(int note, boolean lookup) {
		if(lookup) {
			if(noteToPixelYMap.containsKey(note)) {
				return noteToPixelYMap.get(note);
			}
			return -1;
		}
		int maxNote = HarmonicsEditor.maxNote - HarmonicsEditor.upperY;
		int pixelY = HarmonicsEditor.upperOffset;
		if(note > maxNote || note < HarmonicsEditor.minNote) return -1;
		for(int loopNote = maxNote; loopNote >= HarmonicsEditor.minNote; loopNote--) {
			if(loopNote == note) return pixelY;
			if(currentNotes.contains(loopNote)) pixelY += HarmonicsView.pixelsPerNote;
		}
		return -1;
	}
	
	public static void compileNoteToPixelY() {
		currentNotes = new TreeSet<Integer>();
		noteToPixelYMap = new TreeMap<Integer, Integer>();
		for(Harmonic harmonic: HarmonicsEditor.harmonicIDToHarmonic.values()) {
			currentNotes.add(harmonic.getAverageNote());
		}
		for(int note = FDData.getMinNote(); note <= FDData.getMaxNote(); note++) {
			noteToPixelYMap.put(note, noteToPixelY(note, false));
		}
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
	
	public static boolean timeToDrawSegment(int time) {
		return true;
	}
	
	public static int timeToPixelX(int time) {
		return (time - HarmonicsEditor.leftX) * pixelsPerTime + HarmonicsEditor.leftOffset;
	}
		
	public static int pixelXToTime(int pixelX) {
		pixelX -= HarmonicsEditor.leftOffset;
		return (pixelX / pixelsPerTime) + HarmonicsEditor.leftX;
	}
	
	public static void DrawAmplitudeVertical(Graphics g, Color b, int screenX, int screenY, int numDigits, double value) {
		double dIntVal = Math.floor(value);
		double dFractionVal = value - dIntVal;
		if(dIntVal > 9) dIntVal -= 10.0;
		int intVal = (int) Math.round(dIntVal);
		int fractionVal = (int) Math.round(dFractionVal * 10.0);
		if(numDigits == 1) {
			DrawSegmentData(g, b, screenX, screenY, intVal);
			g.fillRect(screenX, screenY, HarmonicsEditor.xStep, HarmonicsEditor.yStep);
			return;
		}
		if(numDigits == 2) {
			DrawSegmentData(g, b, screenX, screenY, intVal, fractionVal);
			return;
		}
		System.out.println("HarmonicsUtils.drawAmplitudeHorizontal: invalid number of digits");
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
		g.fillRect(screenX, screenY, HarmonicsEditor.xStep, HarmonicsEditor.yStep);
		SevenSegmentSmall(g, black, b, screenX, screenY, digitVal);
	}
	
	//This takes two vertical lines, digitVal is above fraction val 
	public static void DrawSegmentData(Graphics g, Color b, int screenX, int screenY, int digitVal, int fractionVal) {
		Color black = new Color(0.0f, 0.0f, 0.0f);
		int lowerScreenY = screenY + pixelsPerNote / 2;			
		g.setColor(b);
		g.fillRect(screenX, screenY, HarmonicsEditor.xStep, pixelsPerNote);
		SevenSegmentSmall(g, black, b, screenX, screenY, digitVal);
		SevenSegmentSmall(g, black, b, screenX, lowerScreenY, fractionVal);
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
