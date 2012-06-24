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
	public static int numBeatsPerLoop = 4;
	public static int leftYStep = FDData.noteBase + 2;
	public static int upperPanelHeight = FDData.noteBase * 4;
	public static int upperPanelWidth = 800;
	public static int upperPanelBeatWidth = 200;
	public static int lowerRowHeight = FDData.noteBase * 2;
	public static int numLoopsPerRow = 4;
	public static int lowerPanelMillisPerPixel = 25;
	public static int lowerPanelMaxNote = FDData.noteBase * 9 + 15;
	public static int lowerPanelMinNote = FDData.noteBase * 7 + 15;
	
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
					int y = upperY + data.getNote() % FDData.noteBase;
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
				if(y % leftYStep == 0 || y == 0) {
					g2.setColor(new Color(1.0f, 1.0f, 1.0f, 0.5f));
					g2.fillRect(x, y, leftPanelWidth, 1);
					g2.setColor(new Color(1.0f, 1.0f, 1.0f, 0.75f));
					g2.drawString("" + y / leftYStep, 0, y + 10);
				}
			}
		}
		int beatWidth = leftPanelWidth / numBeatsPerLoop;
		for(int beatIndex = 0;  beatIndex < numBeatsPerLoop; beatIndex++) {
			for(int fileIndex = 0; fileIndex < TrackEditor.loopFiles.length; fileIndex++) {
				if(TrackEditor.fileIndexToTaggedBeats.get(fileIndex).contains(beatIndex)) {
					g2.setColor(new Color(1.0f, 1.0f, 1.0f, 0.5f));
					g2.fillRect(beatIndex * beatWidth, fileIndex * leftYStep, beatWidth, leftYStep);
				}
			}
		}		
		g2.setColor(new Color(1.0f, 1.0f, 1.0f, 0.4f));
		g2.fillRect(0, TrackEditor.currentLoopFileIndex * leftYStep, leftPanelWidth, leftYStep);
	}
	
	private void drawUpperPanel(Graphics g) {
		TreeMap<Integer, TreeMap<Integer, Integer>> beatToNoteToNumNotes = new TreeMap<Integer, TreeMap<Integer, Integer>>();
		float[][] upperPanelArray = new float[loopLength][FDData.noteBase];
		for(int x = 0; x < loopLength; x++) {
			for(int y = 0; y < FDData.noteBase; y++) {
				upperPanelArray[x][y] = 0.0f;
			}
		}
		for(Harmonic harmonic: TrackEditor.loopHarmonicIDToHarmonic.values()) {
			harmonic.flattenHarmonic();
			for(FDData data: new ArrayList<FDData>(harmonic.getAllDataInterpolated().values())) {
				int x = data.getTime();
				int beat = x / 100;
				if(x >= loopLength) continue;
				int y = data.getNote() % FDData.noteBase;
				if(!beatToNoteToNumNotes.containsKey(beat)) beatToNoteToNumNotes.put(beat, new TreeMap<Integer, Integer>());
				if(!beatToNoteToNumNotes.get(beat).containsKey(y)) beatToNoteToNumNotes.get(beat).put(y, 0);
				int numNotes = beatToNoteToNumNotes.get(beat).get(y).intValue() + 1;
				beatToNoteToNumNotes.get(beat).put(y, numNotes);
				if(data.getLogAmplitude() > upperPanelArray[x][y]) upperPanelArray[x][y] = (float) data.getLogAmplitude();
			}
		}
		// draw amplitude for each note
		for(int x = 0; x < loopLength; x++) {
			for(int y = 0; y < FDData.noteBase; y++) {
				g.setColor(getAmplitudeColor(upperPanelArray[x][y], 1.0f));
				g.fillRect(x * 2 + leftPanelWidth, y * 4, 2, 4);
				if((x * 2) % 200 == 0 && x != 0) {
					g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.75f));
					g.fillRect(x * 2 + leftPanelWidth, 0, 1, FDData.noteBase * 4);
				}
			}
		}	
		g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.75f));
		int x = leftPanelWidth;
		// display number of sum of data points for each note
		for(int beat = 0; beat < 4; beat++) {
			TreeMap<Integer, TreeSet<Integer>> numNotesToNote = new TreeMap<Integer, TreeSet<Integer>>();
			for(int note = 0; note < FDData.noteBase; note++) {
				if(!beatToNoteToNumNotes.containsKey(beat)) continue;
				if(!beatToNoteToNumNotes.get(beat).containsKey(note)) continue;
				int numNotes = beatToNoteToNumNotes.get(beat).get(note);
				if(!numNotesToNote.containsKey(numNotes)) numNotesToNote.put(numNotes, new TreeSet<Integer>());
				numNotesToNote.get(numNotes).add(note);
			}
			int y = 0;
			for(int numNotes: numNotesToNote.keySet()) {
				for(int note: numNotesToNote.get(numNotes)) {
					g.drawString(note + " | " + numNotes, x, y + 10);
					y += 10;
				}
			}
			x += upperPanelBeatWidth;
		}
	}
	
	private void drawLowerPanel(Graphics g) {
		Color white = new Color(0.0f, 0.0f, 0.0f);
		Color black = new Color(1.0f, 1.0f, 1.0f);
		int maxTime = 0;
		for(Harmonic harmonic: TrackEditor.trackHarmonicIDToHarmonic.values()) {
			for(FDData data: new ArrayList<FDData>(harmonic.getAllDataInterpolated().values())) {
				if(data.getTime() > maxTime) maxTime = data.getTime();
			}
		}
		int screenX = 0;
		int screenY = upperPanelHeight;
		int leftX = leftPanelWidth;
		for(int note = lowerPanelMaxNote; note >= lowerPanelMinNote; note--) {
			int freqInHz = (int) Math.round(Math.pow(2.0, note / FDData.noteBase));
			screenX = leftX;
			TrackUtils.DrawIntegerHorizontal(g, white, black, screenX, screenY, 4, freqInHz);
			screenX += 6 * TrackEditor.xStep;
			int displayNote = note % FDData.noteBase;
			TrackUtils.DrawIntegerHorizontal(g, white, black, screenX, screenY, 2, displayNote);
			screenY += TrackEditor.yStep;
		}
		for(Harmonic harmonic: TrackEditor.trackHarmonicIDToHarmonic.values()) {
			for(FDData data: new ArrayList<FDData>(harmonic.getAllDataInterpolated().values())) {
				if(data.getTime() > maxTime) maxTime = data.getTime();
			}
		}
		int timesPerPixel = lowerPanelMillisPerPixel / FDData.timeStepInMillis;
		int arrayXDim = maxTime / timesPerPixel + 1;
		int arrayYDim = lowerPanelMaxNote - lowerPanelMinNote + 1;
		float[][] lowerPanelArray = new float[arrayXDim][arrayYDim];
		for(int x = 0; x < arrayXDim; x++) {
			for(int y = 0; y < arrayYDim; y++) {
				lowerPanelArray[x][y] = 0.0f;
			}
		}
		for(Harmonic harmonic: TrackEditor.trackHarmonicIDToHarmonic.values()) {
			harmonic.flattenHarmonic();
			for(FDData data: new ArrayList<FDData>(harmonic.getAllDataInterpolated().values())) {
				int x = data.getTime() / timesPerPixel;
				int y = lowerPanelMaxNote - data.getNote();
				if(y >= arrayYDim || y < 0) continue;
				if(data.getLogAmplitude() > lowerPanelArray[x][y]) lowerPanelArray[x][y] = (float) data.getLogAmplitude();
			}
		}
		for(int x = 0; x < arrayXDim; x++) {
			for(int y = 0; y < arrayYDim; y++) {
				screenX = leftPanelWidth + TrackEditor.xStep * 10 + x;
				screenY = upperPanelHeight + y * TrackEditor.yStep;
				g.setColor(getAmplitudeColor(lowerPanelArray[x][y], 1.0f));
				g.fillRect(screenX, screenY, 1, TrackEditor.yStep);
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
