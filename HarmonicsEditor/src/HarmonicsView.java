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
		HARMONICS,
		AMPLITUDES;
	}
	
	public static DataView dataView = DataView.AMPLITUDES;
	
	private static final long serialVersionUID = 2964004162144513754L;
	
	private static BufferedImage bi;
	private static boolean useImage = true;
	private static boolean drawPlaying = false;
	private static int offsetInMillis;
	
	public static int timesPerPixel = 2;
	public static int minPixelsPerNote = 2;

	public void drawUpperTimes(Graphics g) {
		int intDigits = 3;
		int decimalStartY = intDigits * HarmonicsEditor.yStep;
		int endTime = HarmonicsUtils.pixelXToTimeRange(getWidth()).getLower();		
		Color white = new Color(0.0f, 0.0f, 0.0f);
		Color black = new Color(1.0f, 1.0f, 1.0f);
		for(int time = HarmonicsEditor.leftX; time <= endTime; time += timesPerPixel) {
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
		for(int note = startNote; note > endNote; note--) {
			if(!HarmonicsUtils.noteToDrawSegment(note)) continue;
			int screenY = HarmonicsUtils.noteToPixelY(note);
			int freqInHz = (int) Math.round(Math.pow(2.0, note / FDData.noteBase));
			HarmonicsUtils.DrawIntegerHorizontal(g, white, black, 0, screenY, 5, freqInHz);
			int screenX = 6 * HarmonicsEditor.xStep - 1;
			int displayNote = note % FDData.noteBase;
			HarmonicsUtils.DrawIntegerHorizontal(g, white, black, screenX, screenY, 2, displayNote);
		}
	}
	
	public void drawFileData(Graphics g) {
		drawLeftNotes(g);
		drawUpperTimes(g);
		int endTime = HarmonicsUtils.pixelXToTimeRange(getWidth()).getLower();
		int startNote = HarmonicsEditor.maxNote - HarmonicsEditor.upperY;
		int endNote = HarmonicsUtils.pixelYToNote(getHeight());
		if(endNote == -1) endNote = HarmonicsEditor.minNote;
		for(int time = HarmonicsEditor.leftX; time <= endTime; time += timesPerPixel) {
			for(int note = startNote; note > endNote; note--) {
        		FDData data = HarmonicsEditor.getSelected(time, note);
        		if(data == null) continue;
        		Color b = null;
        		if(dataView == DataView.AMPLITUDES) {
        			b = getColor(data.getLogAmplitude());
        		}
        		if(dataView == DataView.HARMONICS) {
        			b = getColor(data.getHarmonicID());
        		}
        		g.setColor(b);
        		int screenX = HarmonicsUtils.timeToPixelX(time);
        		int screenY = HarmonicsUtils.noteToPixelY(note);
        		//System.out.println(screenX + " " + screenY + " " + logAmplitude);
        		//drawAmplitude(g, screenX, screenY, logAmplitude, b);
        		g.fillRect(screenX, screenY, 1, HarmonicsEditor.yStep);
            }
		}	
	}

	private Color getColor(double logAmplitude) {
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
		return new Color(red, green, blue, 0.75f);
	}
	
	private Color getColor(long harmonicID) {
		int red = (int) harmonicID % 128;
		int green = (int) (harmonicID / 128) % 128;
		int blue = (int) (harmonicID / (128 * 128)) % 128;
		return new Color(red + 128, green + 128, blue + 128);
	}
	
	public void drawPlayTime(int offsetInMillis, int refreshInMillis) {
		drawPlaying = true;
		HarmonicsView.offsetInMillis = offsetInMillis;
	}
	
	public int getTimeAxisWidthInMillis() {
   		double millisPerPixel = (double) FDData.timeStepInMillis * timesPerPixel;
   		return (int) Math.round(getWidth() * millisPerPixel);
	}
	
	// See also HarmonicsEditor.getAmplitude()
	private boolean isYInBounds(int y) {
		if(y > HarmonicsEditor.maxNote - HarmonicsEditor.minNote) return false;
		return true;
	}
	
	// See also HarmonicsEditor.getAmplitude()
	private boolean isXInBounds(int x) {
		if(x > HarmonicsEditor.maxTime) return false;
		return true;
	}
		
    protected void paintComponent(Graphics g) {
    	if(drawPlaying) {
    		double millisPerPixel = (double) FDData.timeStepInMillis * timesPerPixel;
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
    		g.drawImage(bi, 0, 0, null);
    		return;
    	}
		super.paintComponent(g);
		drawFileData(g);
		return;
    	
    }
	
}