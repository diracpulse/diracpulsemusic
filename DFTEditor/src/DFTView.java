import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.lang.*;
import java.io.*;

public class DFTView extends JComponent {

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
			int yOffset = 0;
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
		int x;
		int y;
		int freqIndex;
		int iFreq;
		int digitPlace;
		int digitVal;
		Color f;
		Color b;
		Color blank = new Color(0.0f, 0.0f, 0.0f);
		int startX = 0;
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
			int xOffset = 0;
			for(digitPlace = 10000; digitPlace >= 1; digitPlace /= 10) {
				digitVal = iFreq / digitPlace;
				iFreq -= digitVal * digitPlace;
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
		// clear old data
		g.setColor(new Color(0.0f, 0.0f, 0.0f));
		g.fillRect(DFTEditor.leftOffset, DFTEditor.upperOffset, getWidth(), getHeight());
		int startX = DFTEditor.leftX;
		int endX = startX + ((getWidth() - DFTEditor.leftOffset) / DFTEditor.xStep);
		int startY = DFTEditor.upperY;
		int endY = startY + ((getHeight() - DFTEditor.upperOffset) / DFTEditor.yStep);
		float red;
		float green;
		float blue;
		TreeMap<Integer, Float> currentMap;
		float currentVal;
		Float FVal;
		float ampRange = DFTEditor.maxAmplitude - DFTEditor.minAmplitude;
		Color b; // background
		Color f = new Color(0.0f, 0.0f, 0.0f); // forground
		Color black = new Color(0.0f, 0.0f, 0.0f);
		Color white = new Color(1.0f, 1.0f, 1.0f);
		Color gray = new Color(0.5f, 0.5f, 0.5f);
		int digitVal;
		float fDigitVal;
		int fractionVal;
		int screenX; // for whole part
		int screenY;
		int lowerScreenY; // for decimal part
		int bottomScreenY;
		// for(int x = 0; x < dataXDim; x++) {
		//	for(int y = 0; y < dataYDim; y++) {
		for(int x = startX; x < endX; x++) {
			if(x >= DFTEditor.dataXDim) continue;
			if(!DFTEditor.timeToFreqToAmp.containsKey(new Integer(x))) continue;
			currentMap = DFTEditor.timeToFreqToAmp.get(new Integer(x));
			for(int y = startY; y < endY; y++) {
				if(y >= DFTEditor.dataYDim) continue;
				if(!currentMap.containsKey(new Integer(DFTEditor.maxFreq - y))) continue;
				currentVal = currentMap.get(new Integer(DFTEditor.maxFreq - y)).floatValue();
				if(currentVal > 10.0f) {
					digitVal = (int) Math.floor(currentVal);
					digitVal -= 10;					
				} else {
					digitVal = (int) Math.floor(currentVal);
				}
				fractionVal = (int) Math.floor((currentVal - Math.floor(currentVal)) * 10.0f);
				currentVal -= DFTEditor.minAmplitude;
				blue = 1.0f - currentVal / ampRange;	
				red = currentVal / ampRange;
				if(red >= 0.5f) {
					green = (1.0f - red) * 2.0f;
				} else {
					green = red * 2.0f;
				}
				// g.setColor(new Color(red, green, blue));
				// g.fillRect(x * xStep, y * yStep, xStep, yStep);
				b = new Color(red, green, blue);
				screenX = DFTEditor.leftOffset + ((x - DFTEditor.leftX) * DFTEditor.xStep);
				screenY = DFTEditor.upperOffset + ((y - DFTEditor.upperY) * DFTEditor.yStep);
				DFTUtils.DrawSegmentData(g, b, screenX, screenY, digitVal, fractionVal); 
			}
		}
	}
	
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(DFTEditor.fileDataRead) DrawFileData(g, true);
    }
	
}
