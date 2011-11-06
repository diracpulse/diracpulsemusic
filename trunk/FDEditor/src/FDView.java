import javax.swing.*;
import java.awt.*;

public class FDView extends JComponent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2964004162144513754L;
	
	public enum View {
		Digits,
		Pixels1,
		Pixels2;
	}
	
	public static void setView(View v) {
		view = v;
	}
	
	public View getView() {
		return view;
	}
	
	private static View view = View.Pixels1; 
	
	public static int getXStep() {
    	switch(view) {
    	case Digits:
    		return FDEditor.segmentWidth;
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
    		return FDEditor.segmentHeight;
    	case Pixels1:
    		return 1;
    	case Pixels2:
    		return 2;
    	}
    	return 1;
	}	

	public void DrawUpperTimes(Graphics g) {
		Color f;
		Color b;
		int screenX = FDEditor.xDataStart;
		int maxScreenX = getWidth();
		int screenY = 0;
		int timeInMillis = FDEditor.startTimeIndex * FDEditor.timeStepInMillis;
		while(screenX < maxScreenX) {
			int seconds = timeInMillis / 1000;
		    int millis = timeInMillis % 1000;
			// seconds
		    screenY = 0;
			b = new Color(1.0f, 1.0f, 1.0f);
			f = new Color(0.0f, 0.0f, 0.0f);
			FDUtils.DrawIntegerVertical(g, b, f, screenX, screenY, 4, seconds);
			// millis
			screenY = 4 * FDEditor.segmentHeight;
			f = new Color(1.0f, 1.0f, 1.0f);
			b = new Color(0.0f, 0.0f, 0.0f);
			FDUtils.DrawIntegerVertical(g, b, f, screenX, screenY, 3, millis);
			screenX += FDEditor.segmentWidth;
			timeInMillis += FDEditor.timeStepInMillis;
		}
	}


	public void DrawLeftFreqs(Graphics g) {
		return;
	}
		
	public void DrawFileData(Graphics g, boolean scaleLines) {
		DrawLeftFreqs(g);
		DrawUpperTimes(g);		
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
			FDUtils.DrawSegmentData(g, b, screenX, screenY, digitVal);
			return;
		}
		if(digits == 2) {
			FDUtils.DrawSegmentData(g, b, screenX, screenY, digitVal, fractionVal);
			return;
		}
		System.out.println("DFTView.drawAmplitude() invalid number of digits: " + digits);
	}
	
	public void drawFileDataAsPixels(Graphics g) {
		return;
	}
	
	// See also FDEditor.getAmplitude()
	private boolean isYInBounds(int y) {
		//if(y > FDEditor.maxScreenFreq) return false;
		return true;
	}
	
	// See also FDEditor.getAmplitude()
	private boolean isXInBounds(int x) {
		//if(x > FDEditor.maxTime) return false;
		return true;
	}
		
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        DrawFileData(g, true);
    }
	
}
