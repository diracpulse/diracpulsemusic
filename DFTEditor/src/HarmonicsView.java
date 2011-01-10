import javax.swing.*;
import java.awt.*;

public class HarmonicsView extends JComponent {
		
	/**
	 * 
	 */
	private static final long serialVersionUID = -7636633432315243789L;

	public enum View {
		Digits,
		Pixels1,
		Pixels2;
	}
	
	public static void setView(View v) {
		view = v;
	}
	
	private static View view = View.Pixels1; 
	
	public static int getXStep() {
    	switch(view) {
    	case Digits:
    		return HarmonicsEditor.xStep;
    	case Pixels1:
    		return 1;
    	case Pixels2:
    		return 2;
    	}
    	return 1;
	}
	
	public static int getYStep() {
    	switch(view) {
    	case Digits:
    		return HarmonicsEditor.yStep;
    	case Pixels1:
    		return 1;
    	case Pixels2:
    		return 2;
    	}
    	return 1;
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
		int xStep = HarmonicsEditor.xStep + HarmonicsEditor.xStep % getXStep();
		int timeStep = xStep / getXStep();
		int startTime = HarmonicsEditor.leftX;
		int endTime = HarmonicsEditor.leftX + (getWidth() - HarmonicsEditor.leftOffset) / xStep * timeStep;
		for(int time = startTime; time < endTime; time += timeStep) {
			iTime = time * HarmonicsEditor.timeStepInMillis;
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
				if(time >= HarmonicsEditor.maxTime) {
					f = blank;
					b = blank;
				}
				screenX = HarmonicsEditor.leftOffset + ((time - startTime) / timeStep) * xStep;
				screenY = yOffset * HarmonicsEditor.topYStep;
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
		// ADJUST FROM 2 DIGITS TO 1
		int mainYStep = HarmonicsEditor.yStep;
		if(view != View.Digits) mainYStep /= 2;
		int yStep = mainYStep + mainYStep % getYStep();
		int freqStep = yStep / getYStep();
		int startFreq = HarmonicsEditor.upperY;
		int freqRange = (getHeight() - HarmonicsEditor.upperOffset) / yStep * freqStep;
		int endFreq = startFreq + freqRange;
		for(int freq = startFreq; freq < endFreq; freq += freqStep) {
			iFreq = HarmonicsEditor.maxRealFreq - freq;
			int xOffset = 1;
			double freqsPerOctave = (double) HarmonicsEditor.freqsPerOctave;
			double dFreq = (double) iFreq;
			float freqInHz = (float) Math.pow(2.0, dFreq / freqsPerOctave);			
			for(digitPlace = 10000; digitPlace >= 1; digitPlace /= 10) {
				digitVal = (int) Math.floor(freqInHz / digitPlace);
				freqInHz -= digitVal * digitPlace;
				f = new Color(1.0f, 1.0f, 1.0f);
				b = new Color(0.0f, 0.0f, 0.0f);
				if(freq >= (HarmonicsEditor.maxRealFreq - HarmonicsEditor.minRealFreq)) {
					f = blank;
					b = blank;
				}
				int screenX = xOffset * HarmonicsEditor.xStep;
				int screenY = HarmonicsEditor.upperOffset + ((freq - startFreq) / freqStep) * yStep;
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
		g.fillRect(HarmonicsEditor.leftOffset, HarmonicsEditor.upperOffset, getWidth(), getHeight());
		DrawLeftFreqs(g);
		DrawUpperTimes(g);		
		if(view != View.Digits) {
			drawFileDataAsPixels(g);
			return;
		}
		//DrawMaxAmpAtFreq(g);
		int startTime = HarmonicsEditor.leftX;
		int endTime = startTime + ((getWidth() - HarmonicsEditor.leftOffset) / HarmonicsEditor.xStep);
		int startFreq = HarmonicsEditor.upperY;
		int endFreq = startFreq + ((getHeight() - HarmonicsEditor.upperOffset) / HarmonicsEditor.yStep);
		for(int time = startTime; time < endTime; time++) {
            if(!isXInBounds(time)) break;
            for(int freq = startFreq; freq < endFreq; freq++) {
                if(!isYInBounds(freq)) break;
        		int screenX = HarmonicsEditor.leftOffset + ((time - HarmonicsEditor.leftX) * HarmonicsEditor.xStep);
        		int screenY = HarmonicsEditor.upperOffset + ((freq - HarmonicsEditor.upperY) * HarmonicsEditor.yStep);
                float amp = HarmonicsEditor.getAmplitude(time, freq);
                if(amp > 0.0f) {
                	drawAmplitude(g, screenX, screenY, amp, HarmonicsEditor.minAmplitude, HarmonicsEditor.maxAmplitude, 2);
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
		System.out.println("HarmonicsView.drawAmplitude() invalid number of digits: " + digits);
	}
	
	public void drawFileDataAsPixels(Graphics g) {
		int pixelStepX = getXStep(); //(HarmonicsEditor.xStep / getTimeIncrement());
		int pixelStepY = getYStep(); //(HarmonicsEditor.yStep / getFreqIncrement() / 2);
		int startX = HarmonicsEditor.leftX;
		int endX = startX + ((getWidth() - HarmonicsEditor.leftOffset) / pixelStepX);
		int startY = HarmonicsEditor.upperY;
		int endY = startY + ((getHeight() - HarmonicsEditor.upperOffset) / pixelStepY);
		for(int x = startX; x < endX; x++) {
			if(!isXInBounds(x)) break;
            for(int y = startY; y < endY; y++) {
            	if(!isYInBounds(y)) break;
        		int screenX = HarmonicsEditor.leftOffset + ((x - HarmonicsEditor.leftX) * pixelStepX);
        		int screenY = HarmonicsEditor.upperOffset + ((y - HarmonicsEditor.upperY) * pixelStepY);
        		float amp = HarmonicsEditor.getAmplitude(x, y);
        		if(amp == 0.0f) continue;
                drawAmplitude(g, screenX, screenY, HarmonicsEditor.getAmplitude(x, y), HarmonicsEditor.minAmplitude, HarmonicsEditor.maxAmplitude, 0);
            }
		}
	}
	
	// See also HarmonicsEditor.getAmplitude()
	private boolean isYInBounds(int y) {
		if(y > HarmonicsEditor.maxScreenFreq) return false;
		return true;
	}
	
	// See also HarmonicsEditor.getAmplitude()
	private boolean isXInBounds(int x) {
		if(x > HarmonicsEditor.maxTime) return false;
		return true;
	}
		
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        DrawFileData(g, true);
    }
	
}
