import java.awt.Color;
import java.awt.Graphics;


public class TrackUtils {
	
	public static int screenXToTime(double x) {
		//double timeSpan = TrackEditor.maxViewTime - TrackEditor.minViewTime;
		//double timesPerPixel = timeSpan / (double) TrackEditor.view.getWidth();
		//int returnVal = (int) Math.round(x * timesPerPixel) + TrackEditor.minViewTime;
		//System.out.println("screenXToTime " + returnVal);
		return 0; //returnVal;
	}
	
	public static double screenYToValue(double y) {
		if(y < 0) y = 0;
		if(y > TrackEditor.view.getHeight()) y = TrackEditor.view.getHeight();
		//double pixelsPerValue = 0.0;
		//if(TrackView.yView == TrackView.YView.AMPLITUDE) pixelsPerValue = (double) TrackEditor.view.getHeight() / (TrackEditor.maxViewLogAmplitude - TrackEditor.minViewLogAmplitude);
		//if(TrackView.yView == TrackView.YView.FREQUENCY) pixelsPerValue = (double) TrackEditor.view.getHeight() / (TrackEditor.maxViewNote - TrackEditor.minViewNote);
		//if(TrackView.yView == TrackView.YView.AMPLITUDE) return TrackEditor.maxViewLogAmplitude - (y / pixelsPerValue);
		//if(TrackView.yView == TrackView.YView.FREQUENCY) return TrackEditor.maxViewNote - (y / pixelsPerValue);
		return -1.0;
	}
	
	// b = background color, f = foreground color. numdigits needed for space ahead of value 
	public static void DrawIntegerHorizontal(Graphics g, Color b, Color f, int screenX, int screenY, int numdigits, int value) {
		for(int digitPlace = (int) Math.round(Math.pow(10, numdigits)); digitPlace >= 1; digitPlace /= 10) {
			int digitVal = (int) Math.floor(value / digitPlace);
			value -= digitVal * digitPlace;
			g.setColor(b);
			g.fillRect(screenX, screenY, 6, 8);
			TrackUtils.SevenSegmentSmall(g, f, screenX, 
			                           screenY, 
			                           digitVal);
			screenX += TrackEditor.xStep;
		}
	}
	
	public static void DrawIntegerVertical(Graphics g, Color b, Color f, int screenX, int screenY, int numdigits, int value) {
		for(int digitPlace = (int) Math.round(Math.pow(10, numdigits)); digitPlace >= 1; digitPlace /= 10) {
			int digitVal = (int) Math.floor(value / digitPlace);
			value -= digitVal * digitPlace;
			g.setColor(b);
			g.fillRect(screenX, screenY, 6, 8);
			TrackUtils.SevenSegmentSmall(g, f, screenX, 
			                           screenY, 
			                           digitVal);
			screenY += TrackEditor.yStep;
		}
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
	public static void SevenSegmentSmall(Graphics g, Color f, int x, int y, int digitVal) {
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
