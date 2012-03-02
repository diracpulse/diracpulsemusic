import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class DFTView extends JComponent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2964004162144513754L;
	
	private static BufferedImage bi;
	private static boolean useImage = true;
	private static boolean drawPlaying = false;
	private static int offsetInMillis;
	
	public enum View {
		Digits1,
		Digits2,
		Pixels1,
		Pixels2,
		Pixels3;
	}
	
	public static void setView(View v) {
		view = v;
	}
	
	public View getView() {
		return view;
	}
	
	public enum DataView {
		DATA_ONLY,
		MAXIMAS_ONLY,
		DATA_AND_MAXIMAS;
	}
	
	public static void setDataView(DataView v) {
		dataView = v;
	}
	
	public static DataView getDataView() {
		return dataView;
	}
	
	private static View view = View.Pixels3; 
	private static DataView dataView = DataView.DATA_ONLY; 
	
	public static int getXStep() {
    	switch(view) {
    	case Digits1:
    		return DFTEditor.xStep;
    	case Digits2:
    		return DFTEditor.xStep;
    	case Pixels1:
    		return 1;
    	case Pixels2:
    		return 2;
    	case Pixels3:
    		return 3;
    	}
    	return 1;
	}
	
	public static int getYStep() {
    	switch(view) {
    	case Digits1:
    		return DFTEditor.yStep;
    	case Digits2:
    		return DFTEditor.yStep * 2;
    	case Pixels1:
    		return 1;
    	case Pixels2:
    		return 2;
		case Pixels3:
			return 3;
		}
    	return 1;
	}	
	
	public void drawUpperTimes(Graphics g) {
		int iTime;
		int digitPlace;
		int digitVal;
		boolean leading0;
		Color f;
		Color b;
		Color blank = new Color(0.0f, 0.0f, 0.0f);
		int screenX;
		int screenY;
		int xStep = DFTEditor.xStep + DFTEditor.xStep % getXStep();
		int timeStep = xStep / getXStep();
		int startTime = DFTEditor.leftX;
		int endTime = DFTEditor.leftX + (getWidth() - DFTEditor.leftOffset) / xStep * timeStep;
		for(int time = startTime; time < endTime; time += timeStep) {
			iTime = time * DFTEditor.timeStepInMillis;
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
				screenX = DFTEditor.leftOffset + ((time - startTime) / timeStep) * xStep;
				screenY = yOffset * DFTEditor.topYStep;
				g.setColor(b);
				g.fillRect(screenX, screenY, 6, 8);				
				DFTUtils.SevenSegmentSmall(g, f, b, screenX, screenY, digitVal);
				yOffset++;
			}
		}
	}


	public void drawLeftFreqs(Graphics g) {
		int note;
		int digitPlace;
		int digitVal;
		Color f;
		Color b;
		Color blank = new Color(0.0f, 0.0f, 0.0f);
		// ADJUST FROM 2 DIGITS TO 1
		int mainYStep = DFTEditor.yStep;
		if(view == View.Digits2) mainYStep *= 2;
		int yStep = mainYStep + mainYStep % getYStep();
		int freqStep = yStep / getYStep();
		int startFreq = DFTEditor.upperY;
		int freqRange = (getHeight() - DFTEditor.upperOffset) / yStep * freqStep;
		int endFreq = startFreq + freqRange;
		for(int freq = startFreq; freq < endFreq; freq += freqStep) {
			note = DFTEditor.freqToNote(freq);
			int xOffset = 1;
			double freqsPerOctave = (double) DFTEditor.freqsPerOctave;
			double dFreq = (double) note;
			float freqInHz = (float) Math.pow(2.0, dFreq / freqsPerOctave);			
			for(digitPlace = 10000; digitPlace >= 1; digitPlace /= 10) {
				digitVal = (int) Math.floor(freqInHz / digitPlace);
				freqInHz -= digitVal * digitPlace;
				f = new Color(1.0f, 1.0f, 1.0f);
				b = new Color(0.0f, 0.0f, 0.0f);
				if(freq >= (DFTEditor.maxScreenFreq)) {
					f = blank;
					b = blank;
				}
				int screenX = xOffset * DFTEditor.xStep;
				int screenY = DFTEditor.upperOffset + ((freq - startFreq) / freqStep) * yStep;
				g.setColor(b);
				g.fillRect(screenX, screenY, 6, 8);
				DFTUtils.SevenSegmentSmall(g, f, b, screenX, 
				                           screenY, 
				                           digitVal);
				xOffset++;
			}
		}
	}
		
	public void drawFileData(Graphics g, boolean scaleLines) {
		// clear old data
		g.setColor(new Color(0.0f, 0.0f, 0.0f));
		g.fillRect(DFTEditor.leftOffset, DFTEditor.upperOffset, getWidth(), getHeight());
		drawLeftFreqs(g);
		drawUpperTimes(g);		
		if((view != View.Digits1) && (view != View.Digits2)) {
			drawFileDataAsPixels(g);
			return;
		}
		int startTime = DFTEditor.leftX;
		int endTime = startTime + ((getWidth() - DFTEditor.leftOffset) / getXStep());
		int startFreq = DFTEditor.upperY;
		int endFreq = startFreq + ((getHeight() - DFTEditor.upperOffset) / getYStep());
		for(int time = startTime; time < endTime; time++) {
            if(!isXInBounds(time)) break;
            for(int freq = startFreq; freq < endFreq; freq++) {
                if(!isYInBounds(freq)) break;
        		int screenX = DFTEditor.leftOffset + ((time - DFTEditor.leftX) * getXStep());
        		int screenY = DFTEditor.upperOffset + ((freq - DFTEditor.upperY) * getYStep());
        		if(DFTEditor.isSelected(time, freq)) {
        			float amp = (float) DFTEditor.getSelected(time, freq).getLogAmplitude();
        			if(amp > 0.0f) {
        				Color b = getColor(time, freq);
        				drawAmplitude(g, screenX, screenY, amp, b);
        			}
        		} else {
        			float amp = DFTEditor.getAmplitude(time, freq);
        			if(amp > 0.0f) {
        				Color b = getColor(time, freq);
        				drawAmplitude(g, screenX, screenY, amp, b);
        			}
                }
			}
		}
        //g2.setColor(new Color(1.0f, 0.0f, 0.0f, 0.5f));
        //g2.setStroke(new BasicStroke(4));
        //g2.drawLine(100, 400, 1500, 400);
		//DrawMinimaAmdMaximas(g);
	}
	
	public void drawAmplitude(Graphics g, int screenX, int screenY, float currentVal, Color b) {
		int digitVal;
		int fractionVal;
		// need to use >= to avoid "E" segment at currentVal = 10.0f
		if(currentVal >= 10.0f) {
			digitVal = (int) Math.floor(currentVal);
			digitVal -= 10;					
		} else {
			digitVal = (int) Math.floor(currentVal);
		}
		fractionVal = (int) Math.floor((currentVal - Math.floor(currentVal)) * 10.0f);
		if(view == View.Digits1) {
			DFTUtils.DrawSegmentData(g, b, screenX, screenY, digitVal);
			return;
		}
		if(view == View.Digits2) {
			DFTUtils.DrawSegmentData(g, b, screenX, screenY, digitVal, fractionVal);
			return;
		}
		// Assume we're drawing pixels
		g.setColor(b);
		int pixelStepX = getXStep();
		int pixelStepY = getYStep();
		g.fillRect(screenX, screenY, pixelStepX, pixelStepY);
	}
	
	public void drawFileDataAsPixels(Graphics g) {
		int pixelStepX = getXStep(); //(DFTEditor.xStep / getTimeIncrement());
		int pixelStepY = getYStep(); //(DFTEditor.yStep / getFreqIncrement() / 2);
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
        		Color b = getColor(x, y);
        		if(DFTEditor.isSelected(x, y)) {
        			float logAmplitude = (float) DFTEditor.getSelected(x, y).getLogAmplitude();
        			drawAmplitude(g, screenX, screenY, logAmplitude, b);
        		} else {
        			drawAmplitude(g, screenX, screenY, DFTEditor.getAmplitude(x, y), b);
        		}
            }
		}
	}
	
	private Color getColor(int time, int freq) {
		float ampRange = DFTEditor.maxAmplitude - DFTEditor.minAmplitude;
		float currentVal = DFTEditor.getAmplitude(time, freq);
		currentVal -= DFTEditor.minAmplitude;
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
		if(DFTEditor.isSelected(time, freq)) {
			red = red / 2.0f + 0.5f;
			green = 0.0f;
			blue = blue / 2.0f + 0.5f;
			return new Color(red, green, blue);
		}		
		switch(dataView) {
			case DATA_ONLY:
				return new Color(red, green, blue);
			case MAXIMAS_ONLY:
				if(DFTEditor.isMaxima(time, freq)) return new Color(red, green, blue);
				return new Color(0.0f, 0.0f, 0.0f);
			case DATA_AND_MAXIMAS:
				if(DFTEditor.isMaxima(time, freq)) {
					return new Color(red, green, blue);
				}
				return new Color(red * 0.5f, green * 0.5f, blue * 0.5f);
		}
		// if we're here there's an error
		return new Color(-1.0f, -1.0f, -1.0f);
	}
	
	private void drawHarmonicsBase31(Graphics g) {
		if(DFTEditor.drawHarmonicsBaseFreq == -1) return;
		g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.25f));
		int currentFreq = DFTEditor.drawHarmonicsBaseFreq;
		int startX = DFTEditor.leftOffset;
		int width = getWidth() - DFTEditor.leftOffset;
		int height = 1; // getYStep();
		int index = 1;
		int outputFreq = currentFreq;
		while(DFTUtils.freqToScreenY(outputFreq) != -1) {
			g.fillRect(startX, DFTUtils.freqToScreenY(outputFreq), width, height);
			// REMEMBER: "freq" decreases as freqInHz increases (see comment near top of DFTEditor)
			outputFreq = currentFreq - DFTUtils.getConsonantOvertonesBase31(index);
			index++;
		}
	}
	
	private void drawVerticesSelectedArea(Graphics g) {
		g.setColor(new Color(1.0f, 1.0f, 0.0f, 0.5f));
		for(FDData data: DFTEditor.getCurrentSelection().getInputData()) {
			int x = DFTUtils.timeToScreenX(data.getTime());
			int y = DFTUtils.freqToScreenY(DFTEditor.noteToFreq(data.getNote()));
			g.drawLine(DFTEditor.leftOffset, y, getWidth(), y);
			g.drawLine(x, DFTEditor.upperOffset, x, getHeight());
		}
	}
	
	public void drawPlayTime(int offsetInMillis, int refreshInMillis) {
		drawPlaying = true;
		DFTView.offsetInMillis = offsetInMillis;
	}
	
	public int getTimeAxisWidthInMillis() {
   		double millisPerPixel = (double) FDData.timeStepInMillis / (double) getXStep();
   		return (int) Math.round(getWidth() * millisPerPixel);
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
    	if(drawPlaying) {
    		double millisPerPixel = (double) FDData.timeStepInMillis / (double) getXStep();
    		int startX = (int) Math.round((double) DFTView.offsetInMillis / millisPerPixel + DFTEditor.leftOffset);
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
    		drawFileData(g2, true);
    		drawHarmonicsBase31(g2);
    		drawVerticesSelectedArea(g2);
    		g.drawImage(bi, 0, 0, null);
    		return;
    	}
		super.paintComponent(g);
		drawFileData(g, true);
		drawHarmonicsBase31(g);
		drawVerticesSelectedArea(g);
		return;
    	
    }
	
}
