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
		Music, Digits1, Digits2, Pixels1, Pixels2, Pixels3;
	}

	public static void setView(View v) {
		view = v;
	}

	public View getView() {
		return view;
	}

	public enum DataView {
		DATA, HARMONICS, DERIVATIVES;
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
		case Music:
			return 0.5;
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

	public static double getYStep() {
		switch (view) {
		case Music:
			return 2;
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
		int intDigits = 3;
		int decimalStartY = intDigits * DFTEditor.yStep;
		int endTime = (int) Math.round(DFTEditor.leftX + getWidth()
				/ getXStep());
		double timeStep = (double) DFTEditor.xStep / (double) getXStep();
		int screenX = DFTEditor.leftOffset;
		Color white = new Color(0.0f, 0.0f, 0.0f);
		Color black = new Color(1.0f, 1.0f, 1.0f);
		for (double dTime = DFTEditor.leftX; dTime <= endTime; dTime += timeStep) {
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
			screenX += DFTEditor.xStep;
			// System.out.println("drawUpperTimes " + intVal + " " +
			// decimalVal);
		}
	}

	public void drawLeftFreqs(Graphics g) {
		double dFreqStep = (double) DFTEditor.yStep / (double) getYStep();
		int deltaScreenY = DFTEditor.yStep;
		int screenY = DFTEditor.upperOffset;
		int endFreq = (int) Math.round(DFTEditor.upperY + getHeight()
				/ getYStep());
		// handle digits > 1
		if (DFTEditor.yStep < getYStep()) {
			dFreqStep = 1.0;
			deltaScreenY = (int) getYStep();
		}
		Color white = new Color(0.0f, 0.0f, 0.0f);
		Color black = new Color(1.0f, 1.0f, 1.0f);
		for (double dFreq = DFTEditor.upperY; dFreq < endFreq; dFreq += dFreqStep) {
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
		if (view == View.Music) {
			drawFileDataAsMusic(g);
			System.out.println("DFTView.drawFileData Music Finished");
			return;
		}
		if ((dataView == DataView.HARMONICS)) {
			drawFileDataAsHarmonics(g);
			return;
		}
		if ((dataView == DataView.DERIVATIVES)) {
			drawFileDataAsDerivatives(g);
			return;
		}
		drawFileDataAsPixelsOrDigits(g);
	}

	public void drawFileDataAsMusic(Graphics g) {
		int timeStep = (int) Math.round(1.0 / getXStep());
		int startTime = DFTEditor.leftX;
		int endTime = (int) Math.round(startTime
				+ ((getWidth() - DFTEditor.leftOffset) * timeStep));
		int startFreq = DFTEditor.upperY;
		int endFreq = (int) Math.round(startFreq
				+ ((getHeight() - DFTEditor.upperOffset) / getYStep()));
		for (int time = startTime; time < endTime; time += timeStep) {
			if (!isXInBounds(time))
				break;
			for (int freq = startFreq; freq < endFreq; freq++) {
				if (!isYInBounds(freq))
					break;
				FDData data = DFTUtils.getMaxDataInTimeRange(time, time
						+ timeStep, freq);
				if (data == null)
					continue;
				float logAmplitude = (float) data.getLogAmplitude();
				Color b = getColor(logAmplitude);
				g.setColor(b);
				int screenX = DFTEditor.leftOffset + (time - startTime)
						/ timeStep;
				int screenY = (int) Math.round(DFTEditor.upperOffset
						+ (freq - startFreq) * getYStep());
				// System.out.println(screenX + " " + screenY + " " +
				// logAmplitude);
				// drawAmplitude(g, screenX, screenY, logAmplitude, b);
				g.drawRect(screenX, screenY, 2, 2);
			}
		}
	}

	public void drawFileDataAsHarmonics(Graphics g) {
		for(Harmonic harmonic: DFTEditor.harmonicIDToHarmonicMono.values()) {
			if(!harmonic.isSynthesized()) continue;
			FDData firstData = null;
			for(FDData data: harmonic.getAllData()) {
				if(firstData == null) {
					firstData = data;
					continue;
				}
				Color b = getColor(firstData.getLogAmplitude());
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
		int startX = DFTEditor.leftX;
		int endX = startX + ((getWidth() - DFTEditor.leftOffset) / pixelStepX);
		int startY = DFTEditor.upperY;
		int endY = startY
				+ ((getHeight() - DFTEditor.upperOffset) / pixelStepY);
		for (int x = startX; x < endX; x++) {
			if (!isXInBounds(x))
				break;
			for (int y = startY; y < endY; y++) {
				if (!isYInBounds(y))
					break;
				int screenX = DFTEditor.leftOffset
						+ ((x - DFTEditor.leftX) * pixelStepX);
				int screenY = DFTEditor.upperOffset
						+ ((y - DFTEditor.upperY) * pixelStepY);
				float amp = DFTEditor.getAmplitude(x, y);
				if (amp == 0.0f)
					continue;
				Color b = getColor(x, y);
				if (DFTEditor.isSelected(x, y)) {
					float logAmplitude = (float) DFTEditor.getSelected(x, y)
							.getLogAmplitude();
					drawAmplitude(g, screenX, screenY, logAmplitude, b);
				} else {
					drawAmplitude(g, screenX, screenY, DFTEditor.getAmplitude(
							x, y), b);
				}
			}
		}
	}
	
	public void drawFileDataAsDerivatives(Graphics g) {
		float ampRange = DFTEditor.maxAmplitude - DFTEditor.minAmplitude;
		int startX = DFTEditor.leftX;
		int endX = startX + (int) Math.round((getWidth() - DFTEditor.leftOffset) / getXStep());
		int startY = DFTEditor.upperY;
		int endY = startY + (int) Math.round((getHeight() - DFTEditor.upperOffset) / getYStep());
		for (int x = startX; x < endX; x++) {
			if (!isXInBounds(x)) break;
			for (int y = startY; y < endY - 1; y++) {
				if (!isYInBounds(y)) break;
				int screenX = DFTEditor.leftOffset + (int) Math.round((x - DFTEditor.leftX) * getXStep());
				int screenY = DFTEditor.upperOffset + (int) Math.round((y - DFTEditor.upperY) * getYStep());
				float amp = DFTEditor.getAmplitude(x, y) - DFTEditor.getAmplitude(x, y + 1);
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

	private Color getColor(double logAmplitude) {
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
		float currentVal = DFTEditor.getAmplitude(time, freq);
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
		if (DFTEditor.isSelected(time, freq)) {
			red = red / 2.0f + 0.5f;
			green = 0.0f;
			blue = blue / 2.0f + 0.5f;
			return new Color(red, green, blue);
		}
		return new Color(red, green, blue);
	}

	private Color getColor(long harmonicID) {
		int red = (int) harmonicID % 128;
		int green = (int) (harmonicID / 128) % 128;
		int blue = (int) (harmonicID / (128 * 128)) % 128;
		return new Color(red + 128, green + 128, blue + 128);
	}

	private void drawHarmonicsBase31(Graphics g) {
		if (DFTEditor.drawHarmonicsBaseFreq == -1)
			return;
		g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.25f));
		int currentFreq = DFTEditor.drawHarmonicsBaseFreq;
		int startX = DFTEditor.leftOffset;
		int width = getWidth() - DFTEditor.leftOffset;
		int height = 1; // getYStep();
		int index = 1;
		int outputFreq = currentFreq;
		while (DFTUtils.freqToScreenY(outputFreq) != -1) {
			g.fillRect(startX, DFTUtils.freqToScreenY(outputFreq), width,
					height);
			// REMEMBER: "freq" decreases as freqInHz increases (see comment
			// near top of DFTEditor)
			outputFreq = currentFreq
					- DFTUtils.getConsonantOvertonesBase31(index);
			index++;
		}
	}

	private void drawVerticesSelectedArea(Graphics g) {
		g.setColor(new Color(1.0f, 1.0f, 0.0f, 0.5f));
		for (FDData data : DFTEditor.getCurrentSelection().getInputData()) {
			int x = DFTUtils.timeToScreenX(data.getTime());
			int y = DFTUtils
					.freqToScreenY(DFTEditor.noteToFreq(data.getNote()));
			g.drawLine(DFTEditor.leftOffset, y, getWidth(), y);
			g.drawLine(x, DFTEditor.upperOffset, x, getHeight());
		}
	}

	public void drawPlayTime(int offsetInMillis, int refreshInMillis) {
		drawPlaying = true;
		DFTView.offsetInMillis = offsetInMillis;
	}

	public int getTimeAxisWidthInMillis() {
		double millisPerPixel = (double) FDData.timeStepInMillis
				/ (double) getXStep();
		return (int) Math.round(getWidth() * millisPerPixel);
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

	protected void paintComponent(Graphics g) {
		if (drawPlaying) {
			double millisPerPixel = (double) FDData.timeStepInMillis
					/ (double) getXStep();
			int startX = (int) Math.round((double) DFTView.offsetInMillis
					/ millisPerPixel + DFTEditor.leftOffset);
			g.drawImage(bi, 0, 0, null);
			g.setColor(new Color(0.5f, 0.5f, 0.5f, 0.75f));
			g.fillRect(startX, 0, 1, getHeight());
			drawPlaying = false;
			return;
		}
		if (useImage == true) {
			bi = new BufferedImage(getWidth(), getHeight(),
					BufferedImage.TYPE_INT_RGB);
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
