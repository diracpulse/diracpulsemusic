import javax.swing.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

public class FDView extends JComponent {
	
	/**
	 * 
	 */

	public enum DataView {
		HARMONICS,
		AMPLITUDES,
		SELECTED,
		SELECTED_ONLY;
	}
	
	public static DataView dataView = DataView.AMPLITUDES;
	
	private static final long serialVersionUID = 2964004162144513754L;
	
	private static BufferedImage bi;
	private static boolean useImage = true;
	private static boolean drawPlaying = false;
	private static int offsetInMillis;
	
	public static int pixelsPerTime = 2;
	public static int minPixelsPerNote = 0; // used to create blank space between notes

	public void drawUpperTimes(Graphics g) {
		int intDigits = 3;
		int decimalStartY = intDigits * FDEditor.yStep;
		int endTime = FDUtils.pixelXToTime(getWidth());		
		Color white = new Color(0.0f, 0.0f, 0.0f);
		Color black = new Color(1.0f, 1.0f, 1.0f);
		for(int time = FDEditor.leftX; time <= endTime; time++) {
			if(time > FDEditor.maxTime) return;
			if(!FDUtils.timeToDrawSegment(time)) continue;
			int screenX = FDUtils.timeToPixelX(time);
            int millis = time * FDData.timeStepInMillis;
            int intVal = millis / 1000;
            int decimalVal = millis - intVal * 1000;
			FDUtils.DrawIntegerVertical(g, white, black, screenX, 0, 3, intVal);
			FDUtils.DrawIntegerVertical(g, black, white, screenX, decimalStartY, 3, decimalVal);
			//System.out.println("drawUpperTimes " + intVal + " " + decimalVal);
		}
	}

	public void drawLeftNotes(Graphics g) {
		Color white = new Color(0.0f, 0.0f, 0.0f);
		Color black = new Color(1.0f, 1.0f, 1.0f);
		int endNote = FDUtils.pixelYToNote(getHeight());
		if(endNote == -1) endNote = FDEditor.minNote;
		int startNote = FDEditor.maxNote - FDEditor.upperY;
		for(int note = startNote; note > endNote; note--) {
			if(!FDUtils.noteToDrawSegment(note)) continue;
			int screenY = FDUtils.noteToPixelY(note);
			int freqInHz = (int) Math.round(Math.pow(2.0, note / FDData.noteBase));
			FDUtils.DrawIntegerHorizontal(g, white, black, 0, screenY, 5, freqInHz);
			int screenX = 6 * FDEditor.xStep - 1;
			int displayNote = note % FDData.noteBase;
			FDUtils.DrawIntegerHorizontal(g, white, black, screenX, screenY, 2, displayNote);
		}
	}
	
	public void drawFileData(Graphics g) {
		drawLeftNotes(g);
		drawUpperTimes(g);
		int endTime = FDUtils.pixelXToTime(getWidth());
		int startNote = FDEditor.maxNote - FDEditor.upperY;
		int endNote = FDUtils.pixelYToNote(getHeight());
		if(endNote == -1) endNote = FDEditor.minNote;
		for(int time = FDEditor.leftX; time <= endTime; time++) {
			for(int note = startNote; note > endNote; note--) {
        		FDData data = FDEditor.getData(time, note);
        		if(data == null) continue;
        		Color b = null;
        		if(dataView == DataView.AMPLITUDES) {
        			b = getColor(data.getLogAmplitude(), 1.0);
        		}
        		if(dataView == DataView.HARMONICS) {
        			b = getColor(data.getHarmonicID());
        		}
        		if(dataView == DataView.SELECTED) {
        			if(FDEditor.selectedHarmonicIDs.contains(data.getHarmonicID())) {
        				b = getColor(data.getLogAmplitude(), 1.0);
        			} else {
        				b = getColor(data.getLogAmplitude(), 0.5);
        			}
        		}
        		if(dataView == DataView.SELECTED_ONLY) {
        			if(FDEditor.selectedHarmonicIDs.contains(data.getHarmonicID())) {
        				b = getColor(data.getLogAmplitude(), 1.0);
        			} else {
        				continue;
        			}        			
        		}
        		g.setColor(b);
        		int screenX = FDUtils.timeToPixelX(time);
        		int screenY = FDUtils.noteToPixelY(note);
        		//System.out.println(screenX + " " + screenY + " " + logAmplitude);
        		//drawAmplitude(g, screenX, screenY, logAmplitude, b);
        		g.fillRect(screenX, screenY, pixelsPerTime, FDEditor.yStep);
            }
		}	
	}
	
	private Color getColor(double logAmplitude, double alpha) {
		float ampRange = (float) (FDEditor.getMaxAmplitude() - FDEditor.getMinAmplitude());
		float currentVal = (float) logAmplitude;
		currentVal -= FDEditor.getMinAmplitude();
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
		return new Color(red, green, blue, (float) alpha);
	}
	
	private Color getColor(long harmonicID) {
		int red = (int) harmonicID % 128;
		int green = (int) (harmonicID / 128) % 128;
		int blue = (int) (harmonicID / (128 * 128)) % 128;
		return new Color(red + 128, green + 128, blue + 128);
	}
	
	public void drawPlayTime(int offsetInMillis, int refreshInMillis) {
		drawPlaying = true;
		FDView.offsetInMillis = offsetInMillis;
	}
	
	public int getTimeAxisWidthInMillis() {
   		double millisPerPixel = (double) FDData.timeStepInMillis / pixelsPerTime;
   		return (int) Math.round(getWidth() * millisPerPixel);
	}
	
	// See also FDEditor.getAmplitude()
	private boolean isYInBounds(int y) {
		if(y > FDEditor.maxNote - FDEditor.minNote) return false;
		return true;
	}
	
	// See also FDEditor.getAmplitude()
	private boolean isXInBounds(int x) {
		if(x > FDEditor.maxTime) return false;
		return true;
	}
		
    protected void paintComponent(Graphics g) {
    	if(drawPlaying) {
    		double millisPerPixel = (double) FDData.timeStepInMillis / pixelsPerTime;
    		int startX = (int) Math.round((double) FDView.offsetInMillis / millisPerPixel + FDEditor.leftOffset);
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
