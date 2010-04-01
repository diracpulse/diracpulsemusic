import javax.swing.*;
import java.awt.*;

public class DFTView extends JComponent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2964004162144513754L;

	public void DrawAmpSums(Graphics g) {
		int startX = DFTEditor.leftX;
		int endX = DFTEditor.leftX + (getWidth() - DFTEditor.leftOffset) / DFTEditor.xStep;
		int screenIndex = startX;
		int screenEnd = endX;
		for(int time = startX; screenIndex < screenEnd; time += DFTUtils.getTimeIncrement()) {
			int screenX = DFTEditor.leftOffset + ((screenIndex - startX) * DFTEditor.xStep);
			float ampSumVal = DFTUtils.getMaxValue(DFTEditor.timeToAmpSum, time, DFTUtils.getTimeIncrement());
			// draw amp even if 0.0f, to overwrite previous value
			drawAmplitude(g, screenX, 0, ampSumVal, 0.0f, DFTEditor.maxAmplitudeSum);
			screenIndex++;
		}
	}
	
	public void DrawMaxAmpAtFreq(Graphics g) {
		int startY = DFTEditor.upperY;
		int endY = startY + getHeight() - DFTEditor.upperOffset / DFTEditor.yStep;
		int iFreq;
		int screenY;
		float amp;
		int screenIndex = startY;
		int screenEnd = endY;
		for(int freqIndex = startY; screenIndex < screenEnd; freqIndex += DFTUtils.getFreqIncrement()) {
			if(freqIndex < (DFTEditor.maxRealFreq - DFTEditor.minRealFreq)) {			
				iFreq = DFTEditor.maxRealFreq - freqIndex;
			} else {
				iFreq = 1; // dummy value
			}
			amp = DFTUtils.getMaxValue(DFTEditor.freqToMaxAmp, iFreq, DFTUtils.getFreqIncrement());
			// draw amp even if 0.0f, to overwrite previous value
			screenY = DFTEditor.upperOffset + ((screenIndex - startY) * DFTEditor.yStep);
			drawAmplitude(g, 0, screenY, amp, 0.0f, DFTEditor.maxAmplitude);
			screenIndex++;
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
		int screenIndex = startX; // controls actual position on the screen
		for(timeIndex = startX; screenIndex < endX; timeIndex += DFTUtils.getTimeIncrement()) {
			iTime = timeIndex * DFTEditor.timeStepInMillis;;
			leading0 = true;
			int yOffset = 1;
			for(digitPlace = 1000000; digitPlace >= 1; digitPlace /= 10) {
				digitVal = iTime / digitPlace;
				if((digitVal == 0) && leading0 && (digitPlace != 1)) {
					yOffset++;
					continue;
				}
				leading0 = false;
				iTime -= digitVal * digitPlace;
				if(digitPlace >= 1000) {
					b = new Color(1.0f, 1.0f, 1.0f);
					f = new Color(0.0f, 0.0f, 0.0f);
				} else {
					f = new Color(1.0f, 1.0f, 1.0f);
					b = new Color(0.0f, 0.0f, 0.0f);					
				}
				if(timeIndex >= DFTEditor.maxTime) {
					f = blank;
					b = blank;
				}
				screenX = DFTEditor.leftOffset + ((screenIndex - startX) * DFTEditor.xStep);
				screenY = yOffset * DFTEditor.topYStep;
				g.setColor(b);
				g.fillRect(screenX, screenY, 6, 8);				
				DFTUtils.SevenSegmentSmall(g, f, b, screenX, 
				                           screenY, 
				                           digitVal);
				yOffset++;
			}
			screenIndex++;
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
		int screenIndex = startY; // controls actual position on the screen
		for(freqIndex = startY; screenIndex < endY; freqIndex += DFTUtils.getFreqIncrement()) {
			if(freqIndex < (DFTEditor.maxRealFreq - DFTEditor.minRealFreq)) {			
				iFreq = DFTEditor.maxRealFreq - freqIndex;
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
				if(freqIndex >= (DFTEditor.maxRealFreq - DFTEditor.minRealFreq)) {
					f = blank;
					b = blank;
				}
				screenX = xOffset * DFTEditor.xStep;
				screenY = DFTEditor.upperOffset + ((screenIndex - startY) * DFTEditor.yStep);
				g.setColor(b);
				g.fillRect(screenX, screenY, 6, 8);
				DFTUtils.SevenSegmentSmall(g, f, b, screenX, 
				                           screenY, 
				                           digitVal);
				xOffset++;
			}
			screenIndex++;
		}
	}
		
	public void DrawFileData(Graphics g, boolean scaleLines) {
		DrawLeftFreqs(g);
		DrawUpperTimes(g);
		DrawAmpSums(g);
		DrawMaxAmpAtFreq(g);
		// clear old data
		g.setColor(new Color(0.0f, 0.0f, 0.0f));
		g.fillRect(DFTEditor.leftOffset, DFTEditor.upperOffset, getWidth(), getHeight());
		int startX = DFTEditor.leftX;
		int endX = startX + ((getWidth() - DFTEditor.leftOffset) / DFTEditor.xStep);
		int startY = DFTEditor.upperY;
		int endY = startY + ((getHeight() - DFTEditor.upperOffset) / DFTEditor.yStep);
		int screenXIndex = startX;
		for(int x = startX; screenXIndex < endX; x += DFTUtils.getTimeIncrement()) {
            if(x >= DFTEditor.maxTime) break;
    		int screenYIndex = startY;
            for(int y = startY; screenYIndex < endY; y += DFTUtils.getFreqIncrement()) {
                if(y >= (DFTEditor.maxRealFreq - DFTEditor.minRealFreq)) break;
        		int screenX = DFTEditor.leftOffset + ((screenXIndex - DFTEditor.leftX) * DFTEditor.xStep);
        		int screenY = DFTEditor.upperOffset + ((screenYIndex - DFTEditor.upperY) * DFTEditor.yStep);
                float currentVal = DFTUtils.getMaxValue(x, y).getAmplitude();
                if(currentVal > 0.0f) {
                	drawAmplitude(g, screenX, screenY, currentVal, DFTEditor.minAmplitude, DFTEditor.maxAmplitude);
                }
				screenYIndex++;
			}
            screenXIndex++;
		}
		Graphics2D g2 = (Graphics2D) g;
        g2.setColor(new Color(1.0f, 0.0f, 0.0f, 0.5f));
        g2.setStroke(new BasicStroke(4));
        g2.drawLine(100, 400, 1500, 400);
		
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
		if(currentVal < 0.0f) currentVal = 0.0f;
		if(currentVal > 1.0f) currentVal = 1.0f;
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
