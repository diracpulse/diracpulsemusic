import javax.swing.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

public class TrackView extends JComponent {

	private static BufferedImage bi;
	
	private static BufferedImage leftPanel;
	
	private static boolean useImage = true;
	private static boolean drawPlaying = false;
	private static int offsetInMillis = 0;
	public static int loopLength = 400;
	public static int leftPanelWidth = 100;
	public static int leftYStep = 31 + 2;
	// changing this will not change height in drawUpperPanel
	public static int upperPanelHeight = 31 * 4;
	// changing this will not change width in drawUpperPanel
	public static int upperPanelWidth = 800;
	// changing this will not change beat width in drawUpperPanel
	public static int upperPanelBeatWidth = 200;
	// changing this will not change height in drawLowerPanel
	public static int lowerRowHeight = 31 * 2;
	public static int numLoopsPerRow = 4;
	
	public void drawFileData(Graphics g) {
		g.drawImage(leftPanel, 0, 0, null);
		drawUpperPanel(g);
		drawLowerPanel(g);
	}
	
	public static void initLeftPanel() {
		int yStep = leftYStep;
		int xStep = loopLength / leftPanelWidth;
		int leftPanelHeight = TrackEditor.loopFiles.length * yStep;
		float minAmp = 16.0f;
		float maxAmp = 0.0f;
		float[][] leftPanelArray = new float[leftPanelWidth][leftPanelHeight];
		for(int x = 0; x < leftPanelWidth; x++) {
			for(int y = 0; y < leftPanelHeight; y++) {
				leftPanelArray[x][y] = 0.0f;
			}
		}
		leftPanel = new BufferedImage(leftPanelWidth, leftPanelHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = leftPanel.createGraphics();
		int upperY = 0;
		for(int index = 0; index < TrackEditor.loopFiles.length; index++) {
			for(Harmonic harmonic: TrackEditor.fileIndexToHarmonics.get(index)) {
				for(FDData data: new ArrayList<FDData>(harmonic.getAllDataInterpolated().values())) {
					int x = data.getTime() / xStep;
					if(x >= leftPanelWidth) continue;
					int y = upperY + data.getNote() % 31;
					if(data.getLogAmplitude() > maxAmp) maxAmp = (float) data.getLogAmplitude();
					if(data.getLogAmplitude() < minAmp) minAmp = (float) data.getLogAmplitude();
					if(data.getLogAmplitude() > leftPanelArray[x][y]) leftPanelArray[x][y] = (float) data.getLogAmplitude();
				}
			}
			upperY += yStep;
		}
		TrackEditor.maxLoopLogAmplitude = maxAmp;
		TrackEditor.minLoopLogAmplitude = minAmp;
		for(int x = 0; x < leftPanelWidth; x++) {
			for(int y = 0; y < leftPanelHeight; y++) {
				g2.setColor(getAmplitudeColor(leftPanelArray[x][y], 1.0f));
				g2.fillRect(x, y, 1, 1);
				if(y % 33 == 0 || y == 0) {
					g2.setColor(new Color(1.0f, 1.0f, 1.0f, 0.5f));
					g2.fillRect(x, y, leftPanelWidth, 1);
					g2.setColor(new Color(1.0f, 1.0f, 1.0f, 0.75f));
					g2.drawString("" + y / 33, 0, y + 10);
				}
			}
		}
		for(int beatIndex = 0;  beatIndex < 4; beatIndex++) {
			for(int fileIndex = 0; fileIndex < TrackEditor.loopFiles.length; fileIndex++) {
				if(TrackEditor.fileIndexToTaggedBeats.get(fileIndex).contains(beatIndex)) {
					g2.setColor(new Color(1.0f, 1.0f, 1.0f, 0.5f));
					g2.fillRect(beatIndex * 25, fileIndex * leftYStep, 25, leftYStep);
				}
			}
		}		
		g2.setColor(new Color(1.0f, 1.0f, 1.0f, 0.4f));
		g2.fillRect(0, TrackEditor.currentLoopFileIndex * leftYStep, 100, leftYStep);
	}
	
	private void drawUpperPanel(Graphics g) {
		float[][] upperPanelArray = new float[loopLength][31];
		for(int x = 0; x < loopLength; x++) {
			for(int y = 0; y < 31; y++) {
				upperPanelArray[x][y] = 0.0f;
			}
		}
		for(Harmonic harmonic: TrackEditor.loopHarmonicIDToHarmonic.values()) {
			harmonic.flattenHarmonic();
			for(FDData data: new ArrayList<FDData>(harmonic.getAllDataInterpolated().values())) {
				int x = data.getTime();
				if(x >= loopLength) continue;
				int y = data.getNote() % 31;
				if(data.getLogAmplitude() > upperPanelArray[x][y]) upperPanelArray[x][y] = (float) data.getLogAmplitude();
			}
		}
		for(int x = 0; x < loopLength; x++) {
			for(int y = 0; y < 31; y++) {
				g.setColor(getAmplitudeColor(upperPanelArray[x][y], 1.0f));
				g.fillRect(x * 2 + leftPanelWidth, y * 4, 2, 4);
				if((x * 2) % 200 == 0 && x != 0) {
					g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.75f));
					g.fillRect(x * 2 + leftPanelWidth, 0, 1, 31 * 4);
				}
			}
		}
	}
	
	private void drawLowerPanel(Graphics g) {
		int maxTime = 0;
		for(Harmonic harmonic: TrackEditor.trackHarmonicIDToHarmonic.values()) {
			for(FDData data: new ArrayList<FDData>(harmonic.getAllDataInterpolated().values())) {
				if(data.getTime() > maxTime) maxTime = data.getTime();
			}
		}
		int numRows = (int) Math.ceil((double) maxTime / (double) (loopLength * numLoopsPerRow));
		int y = upperPanelHeight;
		for(int row = 0; row < numRows; row++) {
			drawRow(g, y, row * loopLength * numLoopsPerRow, (row + 1) * loopLength * numLoopsPerRow);
			y += lowerRowHeight;
		}
	}
	
	private void drawRow(Graphics g, int upperY, int minTime, int maxTime) {
		int xStep = 2;
		int numXVals = loopLength * numLoopsPerRow / xStep;
		float[][] rowArray = new float[loopLength * numXVals][31];
		for(int x = 0; x < numXVals; x++) {
			for(int y = 0; y < 31; y++) {
				rowArray[x][y] = 0.0f;
			}
		}
		int arrayX = 0;
		for(Harmonic harmonic: TrackEditor.trackHarmonicIDToHarmonic.values()) {
			for(FDData data: new ArrayList<FDData>(harmonic.getAllDataInterpolated().values())) {
				int x = data.getTime();
				if(x < minTime || x > maxTime) continue;
				int y = data.getNote() % 31;
				arrayX = (x - minTime) / xStep;
				if(data.getLogAmplitude() > rowArray[arrayX][y]) rowArray[arrayX][y] = (float) data.getLogAmplitude();
			}
		}
		for(int x = 0; x < numXVals; x++) {
			for(int y = 0; y < 31; y++) {
				g.setColor(getAmplitudeColor(rowArray[x][y], 1.0f));
				g.fillRect(x + leftPanelWidth, upperY + y * 2, 1, 2);
				if((x * 2) % 100 == 0 && x != 0) {
					g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.75f));
					g.fillRect(x + leftPanelWidth, upperY, 1, 31 * 2);
				}
			}
		}
		g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.5f));
		g.fillRect(leftPanelWidth, upperY, numXVals, 1);
	}
	
	private static Color getAmplitudeColor(double amplitude, float alpha) {
		float ampRange = (float) (TrackEditor.maxLoopLogAmplitude - TrackEditor.minLoopLogAmplitude);
		float currentVal = (float) amplitude;
		currentVal -= TrackEditor.minLoopLogAmplitude;
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
		if(currentVal == 0.0f) return new Color(0.0f, 0.0f, 0.0f);
		return new Color(red, green, blue, alpha);
	}
	
	public void drawPlayTime(int offsetInMillis) {
		drawPlaying = true;
		TrackView.offsetInMillis = (int) Math.round(offsetInMillis / SynthTools.playSpeed);
	}
	
	public int getTimeAxisWidthInMillis() {
   		//return (int) Math.round((TrackEditor.maxViewTime - TrackEditor.minViewTime) * FDData.timeStepInMillis * SynthTools.playSpeed);
		return 2000;
	}
	
    protected void paintComponent(Graphics g) {
    	//System.out.println("Paint Component");
    	if(drawPlaying) {
    		double pixelsPerTime = 1.0; // (double) getWidth() / (double) (TrackEditor.maxViewTime - TrackEditor.minViewTime);
    		double millisPerPixel = (double) FDData.timeStepInMillis / pixelsPerTime;
    		int startX = (int) Math.round((double) offsetInMillis / millisPerPixel);
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
