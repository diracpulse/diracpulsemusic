import java.awt.*;
import java.util.*;

public class FDUtils {

	public static void DrawIntegerHorizonal(Graphics g, Color b, int screenX, int screenY, int numdigits, int value) {
		for(int digitPlace = (int) Math.round(Math.pow(10, numdigits)); digitPlace >= 1; digitPlace /= 10) {
			int digitVal = (int) Math.floor(value / digitPlace);
			value -= digitVal * digitPlace;
			Color f = new Color(1.0f, 1.0f, 1.0f);
			//Color b = new Color(0.0f, 0.0f, 0.0f);
			g.setColor(b);
			g.fillRect(screenX, screenY, 6, 8);
			FDUtils.SevenSegmentSmall(g, f, b, screenX, 
			                           screenY, 
			                           digitVal);
			screenX += FDEditor.segmentWidth;
		}
	}
	
	public static void DrawSegmentData(Graphics g, Color b, int screenX, int screenY, int digitVal) {
		Color black = new Color(0.0f, 0.0f, 0.0f);
		// int lowerScreenY = screenY + topYStep;				
		g.setColor(b);
		g.fillRect(screenX, screenY, FDEditor.segmentWidth, FDEditor.segmentHeight);;
		SevenSegmentSmall(g, black, b, screenX, screenY, digitVal);
		// SevenSegmentSmall(g, black, b, screenX, lowerScreenY, fractionVal);
		// g.setColor(black);
		// int bottomScreenY = screenY + yStep - 1;
		// g.drawLine(screenX, bottomScreenY, screenX + xStep, bottomScreenY);
	}
	
	//This takes two vertical lines, digitVal is above fraction val 
	public static void DrawSegmentData(Graphics g, Color b, int screenX, int screenY, int digitVal, int fractionVal) {
		Color black = new Color(0.0f, 0.0f, 0.0f);
		int lowerScreenY = screenY + FDEditor.segmentHeight / 2;			
		g.setColor(b);
		g.fillRect(screenX, screenY, FDEditor.segmentWidth, FDEditor.segmentHeight);;
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
