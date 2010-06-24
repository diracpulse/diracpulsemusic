import javax.swing.*;
import java.awt.*;

public class DFTView extends JComponent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2964004162144513754L;
	
	public enum View {
		Digits,
		Pixels;
	}
	
	public static void setView(View v) {
		view = v;
	}
	
	private static View view = View.Pixels; 
	
	public static int getXStep() {
    	switch(view) {
    	case Digits:
    		return DFTEditor.xStep;
    	case Pixels:
    		return 1;
    	}
    	return 1;
	}
	
	public static int getYStep() {
    	switch(view) {
    	case Digits:
    		return DFTEditor.yStep;
    	case Pixels:
    		return 1;
    	}
    	return 1;
	}	
	
	public void DrawAmpSums(Graphics g) {
		int startTime = DFTEditor.leftX;
		int endTime = DFTEditor.leftX + (getWidth() - DFTEditor.leftOffset) / DFTEditor.xStep;
		for(int time = startTime; time < endTime; time++) {
			if(time >= DFTEditor.timeToAmpSum.size()) return;
			int screenX = DFTEditor.leftOffset + (time - startTime) * DFTEditor.xStep;
			float ampSumValue = DFTEditor.timeToAmpSum.get(time);
			// draw amp even if 0.0f, to overwrite previous value
			drawAmplitude(g, screenX, 0, ampSumValue, 0.0f, DFTEditor.maxAmplitudeSum, 2);
		}
	}
	
	public void DrawMinimaAmdMaximas(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		int startTime = DFTEditor.leftX;
		int endTime = DFTEditor.leftX + (getWidth() - DFTEditor.leftOffset) / DFTEditor.xStep;
		Color minimaColor = new Color(1.0f, 0.5f, 0.5f, 0.25f);
		Color maximaColor = new Color(0.5f, 0.5f, 1.0f, 0.25f);
        g2.setStroke(new BasicStroke(DFTEditor.xStep));
		for(int time = startTime; time < endTime; time++) {
			int screenX = DFTEditor.leftOffset + time * DFTEditor.xStep;
			int xVal = screenX + DFTEditor.xStep / 2; // center line
			if(DFTEditor.timeAtAmpMaximas.contains(time)) {
				g2.setColor(maximaColor);
				g2.drawLine(xVal, DFTEditor.upperOffset, xVal, getHeight());
			}
			if(DFTEditor.timeAtAmpMinimas.contains(time)) {
				g2.setColor(minimaColor);
				g2.drawLine(xVal, DFTEditor.upperOffset, xVal, getHeight());
			}	
		}
	}
	
	
	// NOT YET TESTED
	public void DrawMaxAmpAtFreq(Graphics g) {
		if(DFTEditor.freqToMaxAmp == null) return;
		int startY = DFTEditor.upperY;
		int endY = startY + getHeight() - DFTEditor.upperOffset / DFTEditor.yStep;
		int screenY;
		float amp;
		for(int y = startY; y < endY; y++) {
			int freq = DFTEditor.maxRealFreq - y;
			amp = DFTEditor.freqToMaxAmp.get(freq);
			screenY = DFTEditor.upperOffset + (y - startY) * DFTEditor.yStep;
			drawAmplitude(g, 0, screenY, amp, 0.0f, DFTEditor.maxAmplitude, 1);
		}
	}
	
	public void DrawUpperTimes(Graphics g) {
		int iTime;
		int digitPlace;
		int digitVal;
		boolean leading0;
		Color f;
		Color b;
		Color blank = new Color(0.0f, 0.0f, 0.0f);
		int screenX;
		int screenY;
		int startTime = DFTEditor.leftX;
		int endTime = DFTEditor.leftX + (getWidth() - DFTEditor.leftOffset) / DFTEditor.xStep;
		for(int time = startTime; time < endTime; time++) {
			iTime = time * DFTEditor.timeStepInMillis;;
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
				if(time >= DFTEditor.maxTime) {
					f = blank;
					b = blank;
				}
				screenX = DFTEditor.leftOffset + (time - startTime) * DFTEditor.xStep;
				screenY = yOffset * DFTEditor.topYStep;
				g.setColor(b);
				g.fillRect(screenX, screenY, 6, 8);				
				DFTUtils.SevenSegmentSmall(g, f, b, screenX, screenY, digitVal);
				yOffset++;
			}
		}
	}


	public void DrawLeftFreqs(Graphics g) {
		int iFreq;
		int digitPlace;
		int digitVal;
		Color f;
		Color b;
		Color blank = new Color(0.0f, 0.0f, 0.0f);
		int startFreq = DFTEditor.upperY;
		int endFreq = startFreq + (getHeight() - DFTEditor.upperOffset) / DFTEditor.yStep;
		for(int freq = startFreq; freq < endFreq; freq++) {
			iFreq = DFTEditor.maxRealFreq - freq;
			int xOffset = 1;
			double freqsPerOctave = (double) DFTEditor.freqsPerOctave;
			double dFreq = (double) iFreq;
			float freqInHz = (float) Math.pow(2.0, dFreq / freqsPerOctave);			
			for(digitPlace = 10000; digitPlace >= 1; digitPlace /= 10) {
				digitVal = (int) Math.floor(freqInHz / digitPlace);
				freqInHz -= digitVal * digitPlace;
				f = new Color(1.0f, 1.0f, 1.0f);
				b = new Color(0.0f, 0.0f, 0.0f);
				if(freq >= (DFTEditor.maxRealFreq - DFTEditor.minRealFreq)) {
					f = blank;
					b = blank;
				}
				int screenX = xOffset * DFTEditor.xStep;
				int screenY = DFTEditor.upperOffset + (freq - startFreq) * DFTEditor.yStep;
				g.setColor(b);
				g.fillRect(screenX, screenY, 6, 8);
				DFTUtils.SevenSegmentSmall(g, f, b, screenX, 
				                           screenY, 
				                           digitVal);
				xOffset++;
			}
		}
	}
		
	public void DrawFileData(Graphics g, boolean scaleLines) {
		// clear old data
		g.setColor(new Color(0.0f, 0.0f, 0.0f));
		g.fillRect(DFTEditor.leftOffset, DFTEditor.upperOffset, getWidth(), getHeight());
		if(view == View.Pixels) {
			drawFileDataAsPixels(g);
			return;
		}
		//DrawMaxAmpAtFreq(g);
		DrawLeftFreqs(g);
		DrawUpperTimes(g);
		DrawAmpSums(g);
		int startTime = DFTEditor.leftX;
		int endTime = startTime + ((getWidth() - DFTEditor.leftOffset) / DFTEditor.xStep);
		int startFreq = DFTEditor.upperY;
		int endFreq = startFreq + ((getHeight() - DFTEditor.upperOffset) / DFTEditor.yStep);
		for(int time = startTime; time < endTime; time++) {
            if(!isXInBounds(time)) break;
            for(int freq = startFreq; freq < endFreq; freq++) {
                if(!isYInBounds(freq)) break;
        		int screenX = DFTEditor.leftOffset + ((time - DFTEditor.leftX) * DFTEditor.xStep);
        		int screenY = DFTEditor.upperOffset + ((freq - DFTEditor.upperY) * DFTEditor.yStep);
                float amp = DFTEditor.getAmplitude(time, freq);
                if(amp > 0.0f) {
                	drawAmplitude(g, screenX, screenY, amp, DFTEditor.minAmplitude, DFTEditor.maxAmplitude, 2);
                }
			}
		}
        //g2.setColor(new Color(1.0f, 0.0f, 0.0f, 0.5f));
        //g2.setStroke(new BasicStroke(4));
        //g2.drawLine(100, 400, 1500, 400);
		//DrawMinimaAmdMaximas(g);
	}
	
	public void drawAmplitude(Graphics g, int screenX, int screenY, float currentVal, float minVal, float maxVal, int digits) {
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
		if(digits == 0) {
			g.setColor(b);
			int pixelStepX = getXStep();
			int pixelStepY = getYStep();
			g.fillRect(screenX, screenY, pixelStepX, pixelStepY);
			return;
		}
		if(digits == 1) {
			DFTUtils.DrawSegmentData(g, b, screenX, screenY, digitVal);
			return;
		}
		if(digits == 2) {
			DFTUtils.DrawSegmentData(g, b, screenX, screenY, digitVal, fractionVal);
			return;
		}
		System.out.println("DFTView.drawAmplitude() invalid number of digits: " + digits);
	}
	
	public void drawFileDataAsPixels(Graphics g) {
		int pixelStepX = 1; //(DFTEditor.xStep / getTimeIncrement());
		int pixelStepY = 1; //(DFTEditor.yStep / getFreqIncrement() / 2);
		int startX = DFTEditor.leftX;
		int endX = startX + ((getWidth() - DFTEditor.leftOffset) / pixelStepX);
		int startY = DFTEditor.upperY;
		int endY = startY + ((getHeight() - DFTEditor.upperOffset) / pixelStepY);
		for(int x = startX; x < endX; x++) {
			if(!isXInBounds(x)) break;
            for(int y = startY; y < endY; y++) {
            	if(!isYInBounds(y)) break;
        		int screenX = DFTEditor.leftOffset + ((x - DFTEditor.leftX) * pixelStepX);
        		int screenY = DFTEditor.upperOffset + ((y - DFTEditor.upperY) * pixelStepY);
        		float amp = DFTEditor.getAmplitude(x, y);
        		if(amp == 0.0f) continue;
                drawAmplitude(g, screenX, screenY, DFTEditor.getAmplitude(x, y), DFTEditor.minAmplitude, DFTEditor.maxAmplitude, 0);
            }
		}
	}
	
	// See also DFTEditor.getAmplitude()
	private boolean isYInBounds(int y) {
		if(y > DFTEditor.maxScreenFreq) return false;
		return true;
	}
	
	// See also DFTEditor.getAmplitude()
	private boolean isXInBounds(int x) {
		if(x > DFTEditor.maxTime) return false;
		return true;
	}
		
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        DrawFileData(g, true);
    }
	
}
