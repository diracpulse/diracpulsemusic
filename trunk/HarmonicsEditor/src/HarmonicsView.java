import javax.swing.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

public class HarmonicsView extends JComponent {
	
	/**
	 * 
	 */

	public enum DataView {
		PIXELS,
		DIGITS;
	}
	
	public static DataView dataView = DataView.PIXELS;
	
	private static final long serialVersionUID = 2964004162144513754L;
	
	private static BufferedImage bi;
	private static boolean useImage = true;
	private static boolean drawPlaying = false;
	private static int offsetInMillis;
	
	public static int getNumDigits() {
		if(dataView == DataView.DIGITS) return 2;
		return 1; // as pixels, must be one digit high for note display
	}
	
	public static int getPixelsPerTime() {
		if(dataView == DataView.DIGITS) return HarmonicsEditor.xStep;
		return 1;
	}
	
	public static int getPixelsPerNote() {
		return HarmonicsEditor.yStep * getNumDigits();
	}
	
	public void drawUpperTimes(Graphics g) {
		int timeStep = 1;
		if(dataView == DataView.PIXELS) timeStep = HarmonicsEditor.xStep;
		int intDigits = 3;
		int decimalStartY = intDigits * HarmonicsEditor.yStep;
		int endTime = HarmonicsUtils.pixelXToTime(getWidth());	
		Color white = new Color(0.0f, 0.0f, 0.0f);
		Color black = new Color(1.0f, 1.0f, 1.0f);
		for(int time = HarmonicsEditor.leftX; time <= endTime; time += timeStep) {
			if(time > HarmonicsEditor.maxTime) return;
			if(!HarmonicsUtils.timeToDrawSegment(time)) continue;
			int screenX = HarmonicsUtils.timeToPixelX(time);
            int millis = time * FDData.timeStepInMillis;
            int intVal = millis / 1000;
            int decimalVal = millis - intVal * 1000;
			HarmonicsUtils.DrawIntegerVertical(g, white, black, screenX, 0, 3, intVal);
			HarmonicsUtils.DrawIntegerVertical(g, black, white, screenX, decimalStartY, 3, decimalVal);
			//System.out.println("drawUpperTimes " + intVal + " " + decimalVal);
		}
	}

	public void drawLeftNotes(Graphics g) {
		Color white = new Color(0.0f, 0.0f, 0.0f);
		Color black = new Color(1.0f, 1.0f, 1.0f);
		int endNote = HarmonicsUtils.pixelYToNote(getHeight());
		if(endNote == -1) endNote = HarmonicsEditor.minNote;
		int startNote = HarmonicsEditor.maxNote - HarmonicsEditor.upperY;
		for(int note = startNote; note >= endNote; note--) {
			if(!HarmonicsUtils.noteToDrawSegment(note)) continue;
			int screenY = HarmonicsUtils.noteToPixelY(note);
			int screenX = HarmonicsEditor.controlPanelSegments * HarmonicsEditor.xStep;
			int freqInHz = (int) Math.round(Math.pow(2.0, note / FDData.noteBase));
			HarmonicsUtils.DrawIntegerHorizontal(g, white, black, screenX, screenY, 5, freqInHz);
			screenX += 6 * HarmonicsEditor.xStep - 1;
			int displayNote = note % FDData.noteBase;
			HarmonicsUtils.DrawIntegerHorizontal(g, white, black, screenX, screenY, 2, displayNote);
		}
	}
	
	public void drawBeatInfo(Graphics g) {
		if(HarmonicsEditor.randomLoop == null) return;
		int fontHeight = g.getFontMetrics().getHeight();
		int beatIndex = 0;
		int currentTime = 0;
		Color black = new Color(0.0f, 0.0f, 0.0f, 0.75f);
		Color grey = new Color(1.0f, 1.0f, 1.0f, 0.75f);
		for(Beat beat: HarmonicsEditor.randomLoop.beatArray) {
			int x = HarmonicsUtils.timeToPixelX(currentTime);
			// START: draw base note info
			Color b = black;
			Color f = grey;
			String currentLine = "B: " + beat.getBaseNote() % FDData.noteBase;
			int lineWidth = (int) Math.round(g.getFontMetrics().getStringBounds(currentLine, g).getWidth());
			if(!beat.modifyBaseNote) {
				b = grey;
				f = black;
			}
			int y = HarmonicsEditor.upperOffset;
			g.setColor(b);
			g.fillRect(x, y, lineWidth, fontHeight);
			g.setColor(f);
			g.drawString(currentLine, x, y + fontHeight - 2);
			// START: draw base note info
			// START: draw chords info
			b = black;
			f = grey;
			currentLine = "C: ";
			for(int chord: beat.getChords()) {
				currentLine += chord + " ";
			}
			lineWidth = (int) Math.round(g.getFontMetrics().getStringBounds(currentLine, g).getWidth());
			if(!beat.modifyChords) {
				b = grey;
				f = black;
			}
			y = HarmonicsEditor.upperOffset + fontHeight * 1;
			g.setColor(b);
			g.fillRect(x, y, lineWidth, fontHeight);
			g.setColor(f);
			g.drawString(currentLine, x, y + fontHeight - 2);
			// END: draw chords info
			// START: draw duration info
			b = black;
			f = grey;
			currentLine = "D: " + beat.getDuration();
			lineWidth = (int) Math.round(g.getFontMetrics().getStringBounds(currentLine, g).getWidth());
			if(!beat.modifyDuration) {
				b = grey;
				f = black;
			}
			y = HarmonicsEditor.upperOffset + fontHeight * 2;
			g.setColor(b);
			g.fillRect(x, y, lineWidth, fontHeight);
			g.setColor(f);
			g.drawString(currentLine, x, y + fontHeight - 2);
			// END: draw duration info
			beatIndex++;
			currentTime += beat.getDuration();
		}
	}
	
	public void drawFileData(Graphics g) {
		if(HarmonicsEditor.harmonicIDToHarmonic == null) return;
		HarmonicsUtils.compileNoteToPixelY();
		//System.out.println("Finished Compiling");
		drawLeftNotes(g);
		drawUpperTimes(g);
		int startTime = HarmonicsUtils.pixelXToTime(HarmonicsEditor.leftOffset);
		int endTime = HarmonicsUtils.pixelXToTime(getWidth());
		int startNote = HarmonicsUtils.pixelYToNote(HarmonicsEditor.upperOffset);
		int endNote = HarmonicsUtils.pixelYToNote(getHeight());
		if(endNote == -1) endNote = HarmonicsEditor.minNote;
		for(Harmonic harmonic: HarmonicsEditor.harmonicIDToHarmonic.values()) {
			//System.out.println(harmonic.getAverageNote());
			FDData start = harmonic.getStart();
			FDData end = harmonic.getEnd();
			if(start.getTime() > endTime || end.getTime() < startTime) continue;
			int note = harmonic.getAverageNote();
			//if(end.getTime() < startTime) continue;
			//if(start.getTime() > endTime) continue;
			//if(note < startNote || note > endNote) continue;
			if(note > startNote) continue;
			if(note < endNote) continue;
			int screenY =  HarmonicsUtils.noteToPixelY(note);
			//System.out.println(harmonic);
			for(FDData data: harmonic.getAllDataInterpolated().values()) {
				double logAmplitude = data.getLogAmplitude();
				int screenX = HarmonicsUtils.timeToPixelX(data.getTime());
				if(screenX < HarmonicsEditor.leftOffset) continue;
				if(screenX > getWidth()) continue;
				Color b = getColor(data.getLogAmplitude(), 0.5f);
				if(!harmonic.containsData(data)) {
					// data is interpolated
					b = getColor(data.getLogAmplitude(), 0.5f);
				}
				//g.setColor(b);
				//g.fillRect(screenX, screenY, HarmonicsEditor.xStep, HarmonicsEditor.yStep);
				if(dataView == DataView.DIGITS) {
					HarmonicsUtils.DrawAmplitudeVertical(g, b, screenX, screenY, 2, logAmplitude);
				}
				if(dataView == DataView.PIXELS) {
					g.setColor(b);
					g.fillRect(screenX, screenY, 1, HarmonicsEditor.yStep);
				}
			}
		}
		drawBeatInfo(g);
	}
	
	private Color getColor(double logAmplitude) {
		return getColor(logAmplitude, 1.0f);
	}

	private Color getColor(double logAmplitude, float alpha) {
		float ampRange = (float) (HarmonicsEditor.getMaxAmplitude() - HarmonicsEditor.getMinAmplitude());
		float currentVal = (float) logAmplitude;
		currentVal -= HarmonicsEditor.getMinAmplitude();
		currentVal /= ampRange;
		if(currentVal < 0.0f) currentVal = 0.0f;
		if(currentVal > 1.0f) currentVal = 1.0f;
		float red = currentVal;
		float green = 0.0f;
		float blue = 1.0f - currentVal;
		if(red >= 0.5f) {
			green = (1.0f - red) * 2.0f;
		} else {
			green = red * 2.0f;
		}
		//return new Color(1.0f, 1.0f, 1.0f, 0.75f);
		return new Color(red, green, blue, alpha);
	}
	
	private Color getColor(long harmonicID) {
		int red = (int) harmonicID % 128;
		int green = (int) (harmonicID / 128) % 128;
		int blue = (int) (harmonicID / (128 * 128)) % 128;
		return new Color(red + 128, green + 128, blue + 128);
	}
	
	public void drawPlayTime(int offsetInMillis) {
		drawPlaying = true;
		HarmonicsView.offsetInMillis = offsetInMillis;
	}
	
	public int getTimeAxisWidthInMillis() {
   		double millisPerPixel = (double) FDData.timeStepInMillis / (double) getPixelsPerTime();
   		return (int) Math.round(getWidth() * millisPerPixel);
	}
	
    protected void paintComponent(Graphics g) {
    	//System.out.println("Paint Component");
    	if(drawPlaying) {
    		double millisPerPixel = (double) FDData.timeStepInMillis / getPixelsPerTime();
    		int startX = (int) Math.round((double) HarmonicsView.offsetInMillis / millisPerPixel + HarmonicsEditor.leftOffset);
    		g.drawImage(bi, 0, 0, null);
       		g.setColor(new Color(0.5f, 0.5f, 0.5f, 0.75f));
    		g.fillRect(startX, 0, 1, getHeight());    		
    		drawPlaying = false;
    		return;
    	}
    	if(useImage == true) {
    		bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
    		Graphics2D g2 = bi.createGraphics();
    		super.paintComponent(g);
    		drawFileData(g2);
    		HarmonicsEditor.controlPanel.paintComponent(g2);
    		g.drawImage(bi, 0, 0, null);
    		return;
    	}
		super.paintComponent(g);
		drawFileData(g);
		HarmonicsEditor.controlPanel.paintComponent(g);
		return;
    	
    }
	
}
