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
	
	public void drawFileData(Graphics g) {
		g.drawImage(leftPanel, 0, 0, null);
	}
	
	public static void initLeftPanel() {
		int yStep = 31 + 2;
		int xStep = 4;
		int loopLength = 400;
		int leftPanelWidth = loopLength / xStep;
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
		for(File loopFile: TrackEditor.loopFiles) {
			TrackEditor.loopHarmonicIDToHarmonic = new HashMap<Long, Harmonic>();
			TrackFileInput.ReadBinaryFileData(loopFile.getAbsolutePath());
			for(Harmonic harmonic: TrackEditor.loopHarmonicIDToHarmonic.values()) {
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
				if(y % 33 == 0 && y != 0) {
					g2.setColor(new Color(1.0f, 1.0f, 1.0f));
					g2.fillRect(x, y, leftPanelWidth, 1);
				}
			}
		}
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
