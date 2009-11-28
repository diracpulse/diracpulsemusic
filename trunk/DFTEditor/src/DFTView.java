import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.lang.*;
import java.io.*;

public class DFTView extends JComponent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2964004162144513754L;

	public void DrawAmpSums(Graphics g) {
		int startX = DFTEditor.leftX;
		int endX = DFTEditor.leftX + (getWidth() - DFTEditor.leftOffset) / DFTEditor.xStep;
		for(int time = startX; time < endX; time++) {
			int screenX = DFTEditor.leftOffset + ((time - startX) * DFTEditor.xStep);
			if(DFTEditor.timeToAmpSum.get(new Integer(time)) == null) continue;
			float ampSumVal = DFTEditor.timeToAmpSum.get(new Integer(time)).floatValue();
			drawAmplitude(g, screenX, 0, ampSumVal, 0.0f, DFTEditor.maxAmplitudeSum);
		}
	}
	public void DrawMaxAmpAtFreq(Graphics g) {
		int startY = DFTEditor.upperY;
		int endY = startY + getHeight() - DFTEditor.upperOffset / DFTEditor.yStep;
		int iFreq;
		int screenY;
		float amp;
		for(int freqIndex = startY; freqIndex < endY; freqIndex++) {
			if(freqIndex < DFTEditor.dataYDim) {			
				iFreq = DFTEditor.maxFreq - freqIndex;
			} else {
				iFreq = 1; // dummy value
			}
			if(DFTEditor.freqToMaxAmp.get(iFreq) == null) {
				amp = 0.0f;
			} else {
				amp = DFTEditor.freqToMaxAmp.get(iFreq).floatValue();
			}
			screenY = DFTEditor.upperOffset + ((freqIndex - startY) * DFTEditor.yStep);
			drawAmplitude(g, 0, screenY, amp, 0.0f, DFTEditor.maxAmplitude);
		}
	}

	public void DrawUpperTimes(Graphics g) {
		int timeIndex;
		int iTime;
		int digitPlace;
		int digitVal;
		boolean leading0;
		Color f;
		Color b;
		Color blank = new Color(0.0f, 0.0f, 0.0f);
		int screenX;
		int screenY;
		int startX = DFTEditor.leftX;
		int endX = DFTEditor.leftX + (getWidth() - DFTEditor.leftOffset) / DFTEditor.xStep;
		for(timeIndex = startX; timeIndex < endX; timeIndex++) {
			iTime = timeIndex;
			leading0 = true;
			int yOffset = 1;
			for(digitPlace = 1000; digitPlace >= 1; digitPlace /= 10) {
				digitVal = iTime / digitPlace;
				if((digitVal == 0) && leading0 && (digitPlace != 1)) {
					yOffset++;
					continue;
				}
				leading0 = false;
				iTime -= digitVal * digitPlace;
				if(digitPlace > 10) {
					b = new Color(1.0f, 1.0f, 1.0f);
					f = new Color(0.0f, 0.0f, 0.0f);
				} else {
					f = new Color(1.0f, 1.0f, 1.0f);
					b = new Color(0.0f, 0.0f, 0.0f);					
				}
				if(timeIndex >= DFTEditor.dataXDim) {
					f = blank;
					b = blank;
				}
				screenX = DFTEditor.leftOffset + ((timeIndex - startX) * DFTEditor.xStep);
				screenY = yOffset * DFTEditor.topYStep;
				g.setColor(b);
				g.fillRect(screenX, screenY, 6, 8);				
				DFTUtils.SevenSegmentSmall(g, f, b, screenX, 
				                           screenY, 
				                           digitVal);
				yOffset++;
			}
		}
	}


	public void DrawLeftFreqs(Graphics g) {
		int freqIndex;
		int iFreq;
		int digitPlace;
		int digitVal;
		Color f;
		Color b;
		Color blank = new Color(0.0f, 0.0f, 0.0f);
		int startY = DFTEditor.upperY;
		int endY = startY + getHeight() - DFTEditor.upperOffset / DFTEditor.yStep;
		int screenX;
		int screenY;
		for(freqIndex = startY; freqIndex < endY; freqIndex++) {
			if(freqIndex < DFTEditor.dataYDim) {			
				iFreq = DFTEditor.maxFreq - freqIndex;
			} else {
				iFreq = 1; // dummy value
			}
			int xOffset = 1;
			double freqsPerOctave = (double) DFTEditor.freqsPerOctave;
			double dFreq = (double) iFreq;
			float freqInHz = (float) Math.pow(2.0, dFreq / freqsPerOctave);			
			for(digitPlace = 10000; digitPlace >= 1; digitPlace /= 10) {
				digitVal = (int) Math.floor(freqInHz / digitPlace);
				freqInHz -= digitVal * digitPlace;
				f = new Color(1.0f, 1.0f, 1.0f);
				b = new Color(0.0f, 0.0f, 0.0f);
				if(freqIndex >= DFTEditor.dataYDim) {
					f = blank;
					b = blank;
				}
				screenX = xOffset * DFTEditor.xStep;
				screenY = DFTEditor.upperOffset + ((freqIndex - startY) * DFTEditor.yStep);
				g.setColor(b);
				g.fillRect(screenX, screenY, 6, 8);
				DFTUtils.SevenSegmentSmall(g, f, b, screenX, 
				                           screenY, 
				                           digitVal);
				xOffset++;
			}
		}
	}
			
	public void DrawFileData(boolean scaleLines) {
		DrawFileData(getGraphics(), scaleLines);
	}
		
	public void DrawFileData(Graphics g, boolean scaleLines) {
		DrawLeftFreqs(g);
		DrawUpperTimes(g);
		DrawAmpSums(g);
		DrawMaxAmpAtFreq(g);
		// clear old data
		g.setColor(new Color(0.0f, 0.0f, 0.0f));
		g.fillRect(DFTEditor.leftOffset, DFTEditor.upperOffset, getWidth(), getHeight());
		TreeMap<Integer, Float> currentMap;
		int startX = DFTEditor.leftX;
		int endX = startX + ((getWidth() - DFTEditor.leftOffset) / DFTEditor.xStep);
		int startY = DFTEditor.upperY;
		int endY = startY + ((getHeight() - DFTEditor.upperOffset) / DFTEditor.yStep);
		for(int x = startX; x < endX; x++) {
            if(x >= DFTEditor.dataXDim) continue;
            if(!DFTEditor.timeToFreqToAmp.containsKey(new Integer(x))) continue;
            currentMap = DFTEditor.timeToFreqToAmp.get(new Integer(x));
            for(int y = startY; y < endY; y++) {
                if(y >= DFTEditor.dataYDim) continue;
                if(!currentMap.containsKey(new Integer(DFTEditor.maxFreq - y))) continue;
        		int screenX = DFTEditor.leftOffset + ((x - DFTEditor.leftX) * DFTEditor.xStep);
        		int screenY = DFTEditor.upperOffset + ((y - DFTEditor.upperY) * DFTEditor.yStep);
                float currentVal = currentMap.get(new Integer(DFTEditor.maxFreq - y)).floatValue();
				drawAmplitude(g, screenX, screenY, currentVal, DFTEditor.minAmplitude, DFTEditor.maxAmplitude);
			}
		}
	}
	
	public void drawAmplitude(Graphics g, int screenX, int screenY, float currentVal, float minVal, float maxVal) {
		int digitVal;
		int fractionVal;
		float red;
		float green;
		float blue;
		float ampRange = maxVal - minVal;
		if(currentVal > 10.0f) {
			digitVal = (int) Math.floor(currentVal);
			digitVal -= 10;					
		} else {
			digitVal = (int) Math.floor(currentVal);
		}
		fractionVal = (int) Math.floor((currentVal - Math.floor(currentVal)) * 10.0f);
		currentVal -= minVal;
		currentVal /= ampRange;
		blue = 1.0f - currentVal;
		red = currentVal;
		if(red >= 0.5f) {
			green = (1.0f - red) * 2.0f;
		} else {
			green = red * 2.0f;
		}
		// g.setColor(new Color(red, green, blue));
		// g.fillRect(x * xStep, y * yStep, xStep, yStep);
		Color b = new Color(red, green, blue);
		DFTUtils.DrawSegmentData(g, b, screenX, screenY, digitVal, fractionVal); 
	}
	
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(DFTEditor.fileDataRead) DrawFileData(g, true);
    }
	
}
