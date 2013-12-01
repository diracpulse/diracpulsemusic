package main;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.TreeMap;

public class DFTView extends JComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2964004162144513754L;

	private static BufferedImage bi;
	private static boolean refresh = true;
	private static boolean drawPlaying = false;
	private static int offsetInMillis;
	public boolean dftInProgress = false;
	private int height = 0;
	private int width = 0;

	public enum View {
		Digits1, Digits2, Pixels1, Pixels2, Pixels3;
	}

	public static void setView(View v) {
		view = v;
	}

	public static View getView() {
		return view;
	}

	public enum DataView {
		DATA, DATA_ONLY, HARMONICS, HARMONIC_ID, DERIVATIVES, CHANNEL_DATA, CHANNEL_HARMONICS;
	}

	public static void setDataView(DataView v) {
		dataView = v;
	}

	public static DataView getDataView() {
		return dataView;
	}

	private static View view = View.Pixels1;
	private static DataView dataView = DataView.DATA;

	public static double getXStep() {
		switch (view) {
		case Digits1:
			return DFTEditor.digitWidth;
		case Digits2:
			return DFTEditor.digitWidth;
		case Pixels1:
			return 1;
		case Pixels2:
			return 2;
		case Pixels3:
			return 3;
		}
		return 1;
	}

	public static double getYStep() {
		switch (view) {
		case Digits1:
			return DFTEditor.digitHeight;
		case Digits2:
			return DFTEditor.digitHeight * 2;
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
		int intDigits = 3;
		int decimalStartY = intDigits * DFTEditor.digitHeight;
		int endTime = (int) Math.round(DFTEditor.minViewTime + getWidth()
				/ getXStep());
		double timeStep = (double) DFTEditor.digitWidth / (double) getXStep();
		int screenX = DFTEditor.leftOffset;
		Color white = new Color(0.0f, 0.0f, 0.0f);
		Color black = new Color(1.0f, 1.0f, 1.0f);
		for (double dTime = DFTEditor.minViewTime; dTime <= endTime; dTime += timeStep) {
			int time = (int) Math.round(dTime);
			if (time >= DFTEditor.maxTime)
				return;
			int millis = time * FDData.timeStepInMillis;
			int intVal = millis / 1000;
			int decimalVal = millis - intVal * 1000;
			DFTUtils
					.DrawIntegerVertical(g, white, black, screenX, 0, 2, intVal);
			DFTUtils.DrawIntegerVertical(g, black, white, screenX,
					decimalStartY, 2, decimalVal);
			screenX += DFTEditor.digitWidth;
			// System.out.println("drawUpperTimes " + intVal + " " +
			// decimalVal);
		}
	}

	public void drawLeftFreqs(Graphics g) {
		double dFreqStep = (double) DFTEditor.digitHeight / (double) getYStep();
		int deltaScreenY = DFTEditor.digitHeight;
		int screenY = DFTEditor.upperOffset;
		int endFreq = (int) Math.round(DFTEditor.minViewFreq + getHeight()
				/ getYStep());
		// handle digits > 1
		if (DFTEditor.digitHeight < getYStep()) {
			dFreqStep = 1.0;
			deltaScreenY = (int) getYStep();
		}
		Color white = new Color(0.0f, 0.0f, 0.0f);
		Color black = new Color(1.0f, 1.0f, 1.0f);
		for (double dFreq = DFTEditor.minViewFreq; dFreq < endFreq; dFreq += dFreqStep) {
			int freq = (int) Math.round(dFreq);
			if (freq >= DFTEditor.maxScreenFreq)
				return;
			double note = (double) DFTEditor.freqToNote(freq);
			int freqInHz = (int) Math.round(Math.pow(2.0, note
					/ FDData.noteBase));
			DFTUtils.DrawIntegerHorizontal(g, white, black, 0, screenY, 4,
					freqInHz);
			screenY += deltaScreenY;
			// System.out.println("drawUpperTimes " + intVal + " " +
			// decimalVal);
		}
	}

	public void drawFileData(Graphics g, boolean scaleLines) {
		// clear old data
		g.setColor(new Color(0.0f, 0.0f, 0.0f));
		g.fillRect(DFTEditor.leftOffset, DFTEditor.upperOffset, getWidth(), getHeight());
		drawLeftFreqs(g);
		drawUpperTimes(g);
		if ((dataView == DataView.HARMONICS || dataView == DataView.HARMONIC_ID || dataView == DataView.CHANNEL_HARMONICS)) {
			drawFileDataAsHarmonics(g);
			return;
		}
		if ((dataView == DataView.DERIVATIVES)) {
			drawFileDataAsDerivatives(g);
			return;
		}
		drawFileDataAsPixelsOrDigits(g);
		drawControllerStateData(g);
	}
	
	public void drawControllerStateData(Graphics g) {
		g.setColor(Color.YELLOW);
		if(DFTController.currentAction == DFTController.ControllerAction.RANGE_SELECT) {
			for(int index = 0; index < DFTController.selectionIndex; index++) {
				int x = DFTUtils.timeToScreenX(DFTController.selectedTimes[index]);
				int y = DFTUtils.freqToScreenY(DFTController.selectedFreqs[index]);
				g.drawLine(x, 0, x, getHeight());
				g.drawLine(0, y, getWidth(), y);
			}
		}
	}

	public void drawFileDataAsHarmonics(Graphics g) {
		for(Harmonic harmonic: DFTEditor.harmonicIDToHarmonic.values()) {
			if(!DFTUtils.isChannelVisible(harmonic)) continue;
			if(!harmonic.isSynthesized()) continue;
			if(harmonic.getAllDataRaw().size() == 1) {
				FDData data = harmonic.getAllDataRaw().get(0);
				Color b = getColor(data.getLogAmplitude());
				if(dataView == DataView.HARMONIC_ID) b = getColor(data.getHarmonicID());
				if(dataView == DataView.CHANNEL_HARMONICS) b = getColor(harmonic.getChannel(), data.getLogAmplitude());
				g.setColor(b);
				int x = DFTUtils.timeToScreenX(data.getTime());
				int y = DFTUtils.freqToScreenY(DFTEditor.noteToFreq(data.getNote()));
				g.fillRect(x, y, 1, 1);
				continue;
			}
			FDData firstData = null;
			for(FDData data: harmonic.getAllDataRaw()) {
				if(firstData == null) {
					firstData = data;
					continue;
				}
				Color b = getColor(firstData.getLogAmplitude());
				if(dataView == DataView.HARMONIC_ID) b = getColor(firstData.getHarmonicID());
				if(dataView == DataView.CHANNEL_HARMONICS) b = getColor(harmonic.getChannel(), firstData.getLogAmplitude());
				g.setColor(b);
				int startScreenX = DFTUtils.timeToScreenX(firstData.getTime());
				int startScreenY = DFTUtils.freqToScreenY(DFTEditor.noteToFreq(firstData.getNote()));
				int endScreenX = DFTUtils.timeToScreenX(data.getTime());
				int endScreenY = DFTUtils.freqToScreenY(DFTEditor.noteToFreq(data.getNote()));
				g.drawLine(startScreenX, startScreenY, endScreenX, endScreenY);
				firstData = data;
			}
		}
	}

	public void drawAmplitude(Graphics g, int screenX, int screenY,
			float currentVal, Color b) {
		int digitVal;
		int fractionVal;
		// need to use >= to avoid "E" segment at currentVal = 10.0f
		if (currentVal >= 10.0f) {
			digitVal = (int) Math.floor(currentVal);
			digitVal -= 10;
		} else {
			digitVal = (int) Math.floor(currentVal);
		}
		fractionVal = (int) Math
				.floor((currentVal - Math.floor(currentVal)) * 10.0f);
		if (view == View.Digits1) {
			DFTUtils.DrawSegmentData(g, b, screenX, screenY, digitVal);
			return;
		}
		if (view == View.Digits2) {
			DFTUtils.DrawSegmentData(g, b, screenX, screenY, digitVal,
					fractionVal);
			return;
		}
		// Assume we're drawing pixels
		g.setColor(b);
		int pixelStepX = (int) getXStep();
		int pixelStepY = (int) getYStep();
		g.fillRect(screenX, screenY, pixelStepX, pixelStepY);
	}

	public void drawFileDataAsPixelsOrDigits(Graphics g) {
		int pixelStepX = (int) getXStep(); // (DFTEditor.xStep /
											// getTimeIncrement());
		int pixelStepY = (int) getYStep(); // (DFTEditor.yStep /
											// getFreqIncrement() / 2);
		int startX = DFTEditor.minViewTime;
		int endX = startX + ((getWidth() - DFTEditor.leftOffset) / pixelStepX);
		int startY = DFTEditor.minViewFreq;
		int endY = startY
				+ ((getHeight() - DFTEditor.upperOffset) / pixelStepY);
		for (int x = startX; x < endX; x++) {
			if (!isXInBounds(x)) break;
			for (int y = startY; y < endY; y++) {
				if (!isYInBounds(y))
					break;
				int screenX = DFTEditor.leftOffset
						+ ((x - DFTEditor.minViewTime) * pixelStepX);
				int screenY = DFTEditor.upperOffset
						+ ((y - DFTEditor.minViewFreq) * pixelStepY);
				float amp = (float) DFTEditor.getAmplitude(x, y);
				if (amp == 0.0f) continue;
				if(dataView == DataView.CHANNEL_DATA) {
					Color b = getColor((float)DFTEditor.amplitudesLeft[x][y], (float)DFTEditor.amplitudesRight[x][y]);
					drawAmplitude(g, screenX, screenY, amp, b);
				} else {
					Color b = getColor(x,y);
					drawAmplitude(g, screenX, screenY, amp, b);
				}
			}
		}
	}
	
	public void drawFileDataAsDerivatives(Graphics g) {
		float ampRange = DFTEditor.maxAmplitude - DFTEditor.minAmplitude;
		int startX = DFTEditor.minViewTime;
		int endX = startX + (int) Math.round((getWidth() - DFTEditor.leftOffset) / getXStep());
		int startY = DFTEditor.minViewFreq;
		int endY = startY + (int) Math.round((getHeight() - DFTEditor.upperOffset) / getYStep());
		for (int x = startX; x < endX; x++) {
			if (!isXInBounds(x)) break;
			for (int y = startY; y < endY - 1; y++) {
				if (!isYInBounds(y)) break;
				int screenX = DFTEditor.leftOffset + (int) Math.round((x - DFTEditor.minViewTime) * getXStep());
				int screenY = DFTEditor.upperOffset + (int) Math.round((y - DFTEditor.minViewFreq) * getYStep());
				float amp = (float) (DFTEditor.getAmplitude(x, y) - DFTEditor.getAmplitude(x, y + 1));
				Color b = null;
				float red = 0;
				float green = 0;
				float blue = 0;
				if (Math.abs(amp) <= 0.5f) {
					red = 0.5f + amp;
					blue = 0.5f - amp;
					green = 0.5f;
				} else {
					if (amp < 0.5f) {
						red = 0.0f;
						blue = 0.25f + -0.75f * (amp) / (ampRange);
						green = 0.0f;
					} else {
						red = 0.25f + 0.75f * (amp) / (ampRange);
						blue = 0.0f;
						green = 0.0f;						
					}
				}
				b = new Color(red, green, blue);
				drawAmplitude(g, screenX, screenY, amp, b);
			}
		}
	}
	
	private Color getColor(float left, float right) {
		if(left < 0) left = 0.0f;
		if(right < 0) right = 0.0f;
		float ampRange = DFTEditor.maxAmplitude - DFTEditor.minAmplitude;
		left -= DFTEditor.minAmplitude;
		right -= DFTEditor.minAmplitude;		
		left /= ampRange;
		right /= ampRange;
		//System.out.println(mono + " " + left + " " + right);
		return new Color(left, 0.0f, right);
	}
	
	private Color getColor(FDData.Channel channel, double logAmplitude) {
		if(logAmplitude < 0) return new Color(1.0f, 1.0f, 1.0f);
		float ampRange = DFTEditor.maxAmplitude - DFTEditor.minAmplitude;
		float currentVal = (float) logAmplitude;
		currentVal -= DFTEditor.minAmplitude;
		currentVal /= ampRange;
		if(channel == FDData.Channel.LEFT) return new Color(0.0f, currentVal * 0.5f + 0.5f, 0.0f, 0.33f);
		if(channel == FDData.Channel.RIGHT) return new Color(0.0f, 0.0f, currentVal * 0.5f + 0.5f, 0.33f);
		//System.out.println("DFTView.getColor: unknown channel: " + channel);
		return new Color(0.0f, 0.0f, 0.0f);
	}

	private Color getColor(double logAmplitude) {
		if(logAmplitude < 0) return new Color(1.0f, 1.0f, 1.0f);
		float ampRange = DFTEditor.maxAmplitude - DFTEditor.minAmplitude;
		float currentVal = (float) logAmplitude;
		currentVal -= DFTEditor.minAmplitude;
		currentVal /= ampRange;
		if (currentVal < 0.0f)
			currentVal = 0.0f;
		if (currentVal > 1.0f)
			currentVal = 1.0f;
		float red = currentVal;
		float green = 0.0f;
		float blue = 1.0f - currentVal;
		if (red >= 0.5f) {
			green = (1.0f - red) * 2.0f;
		} else {
			green = red * 2.0f;
		}
		// return new Color(1.0f, 1.0f, 1.0f, 0.75f);
		return new Color(red, green, blue, 0.75f);
	}

	private Color getColor(int time, int freq) {
		float ampRange = DFTEditor.maxAmplitude - DFTEditor.minAmplitude;
		float currentVal = (float) DFTEditor.getAmplitude(time, freq);
		if(currentVal < 0) return new Color(1.0f, 1.0f, 1.0f);
		currentVal -= DFTEditor.minAmplitude;
		currentVal /= ampRange;
		if (currentVal < 0.0f)
			currentVal = 0.0f;
		if (currentVal > 1.0f)
			currentVal = 1.0f;
		float red = currentVal;
		float green = 0.0f;
		float blue = 1.0f - currentVal;
		if (red >= 0.5f) {
			green = (1.0f - red) * 2.0f;
		} else {
			green = red * 2.0f;
		}
		if(DFTEditor.isMaxima(time, freq)) {
			if(dataView != DataView.DATA_ONLY) {
				red = red / 2.0f + 0.5f;
				green = 0.0f;
				blue = blue / 2.0f + 0.5f;
				return new Color(red, green, blue);
			}
		}
		return new Color(red, green, blue);
	}

	private Color getColor(long harmonicID) {
		int red = (int) harmonicID % 128;
		int green = (int) (harmonicID / 128) % 128;
		int blue = (int) (harmonicID / (128 * 128)) % 128;
		return new Color(red + 128, green + 128, blue + 128);
	}

	// See also DFTEditor.getAmplitude()
	private boolean isYInBounds(int y) {
		if (y > DFTEditor.maxScreenFreq)
			return false;
		return true;
	}

	// See also DFTEditor.getAmplitude()
	private boolean isXInBounds(int x) {
		if (x > DFTEditor.maxTime)
			return false;
		return true;
	}
	
	public void drawPlayTime(int offsetInMillis) {
		drawPlaying = true;
		DFTView.offsetInMillis = offsetInMillis;
	}
	
	public int getTimeAxisWidth() {
		return (int) Math.round(getWidth() / getXStep());
	}

	public int getTimeAxisWidthInMillis() {
		double millisPerPixel = (double) FDData.timeStepInMillis
				/ (double) getXStep();
		return (int) Math.round(getWidth() * millisPerPixel);
	}

	public void refresh() {
		refresh = true;
		paintImmediately(0, 0, getWidth(), getHeight());
	}
	
	protected void paintComponent(Graphics g) {
    	if(drawPlaying) {
    		double millisPerPixel = (double) (FDData.timeStepInMillis / getXStep());
    		int startX = DFTEditor.leftOffset + (int) Math.round((offsetInMillis - DFTEditor.getMinViewTimeInMillis()) / millisPerPixel);
    		g.drawImage(bi, 0, 0, null);
       		g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.75f));
    		g.fillRect(startX, 0, 2, getHeight());    		
    		drawPlaying = false;
    		return;
    	}
    	if(refresh || height != getHeight() || width != getWidth()) {
    		bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
    		Graphics2D g2 = bi.createGraphics();
    		super.paintComponent(g);
    		drawFileData(g2, false);
    		g.drawImage(bi, 0, 0, null);
    		refresh = false;
    		width = getWidth();
    		height = getHeight();
    		return;
    	}
		super.paintComponent(g);
		g.drawImage(bi, 0, 0, null);
		return;
	}

}
