import javax.swing.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

public class GraphView extends JComponent {

	
	
	public enum ColorView {
		DATA,
		CHANNEL,
		HARMONICS;
	}
	
	public enum YView {
		AMPLITUDE,
		FREQUENCY;
	}
	
	public enum XView {
		TIME,
		FREQUENCY;
	}

	public static ColorView colorView = ColorView.DATA;
	public static YView yView = YView.AMPLITUDE;
	public static XView xView = XView.TIME;
	
	private static final long serialVersionUID = 2964004162144513754L;
	
	private static BufferedImage bi;
	private static boolean drawPlaying = false;
	private static boolean refresh = true;
	private static int offsetInMillis = 0;
	private int height = 0;
	private int width = 0;
	
	public boolean isHarmonicVisible(Harmonic harmonic) {
		if(harmonic.getChannel() == 0) {
			if(GraphEditor.currentChannel == GraphEditor.Channel.MONO) return true;
			return false;			
		}
		if(harmonic.getChannel() == 1) {
			if(GraphEditor.currentChannel == GraphEditor.Channel.LEFT) return true;
			if(GraphEditor.currentChannel == GraphEditor.Channel.STEREO) return true;
			return false;
		}
		if(harmonic.getChannel() == 2) {
			if(GraphEditor.currentChannel == GraphEditor.Channel.RIGHT) return true;
			if(GraphEditor.currentChannel == GraphEditor.Channel.STEREO) return true;
			return false;
		}
		System.out.println("DFTView.isHarmonicVisible: Unknown channel");
		return false;
	}
	
	public void drawFileData(Graphics g) {
		double pixelsPerValueY = 1;
		double pixelsPerValueX = 1;
		if(xView == XView.TIME) pixelsPerValueX = (double) getWidth() / (double) (GraphEditor.maxViewTime - GraphEditor.minViewTime);
		if(xView == XView.FREQUENCY) pixelsPerValueX = (double) getWidth() / (double) (GraphEditor.maxViewNote - GraphEditor.minViewNote);
		if(yView == YView.AMPLITUDE) pixelsPerValueY = (double) getHeight() / (double) (GraphEditor.maxViewLogAmplitude - GraphEditor.minViewLogAmplitude);
		if(yView == YView.FREQUENCY) pixelsPerValueY = (double) getHeight() / (double) (GraphEditor.maxViewNote - GraphEditor.minViewNote);
		ArrayList<Harmonic> allHarmonics = new ArrayList<Harmonic>(GraphEditor.harmonicIDToHarmonic.values());
		//allHarmonics.addAll(GraphEditor.harmonicIDToControlPointHarmonic.values());
		for(Harmonic harmonic: allHarmonics) {
			if(!isHarmonicVisible(harmonic)) continue;
			if(!harmonic.isSynthesized()) continue;
			if(harmonic.getLength() < GraphEditor.minHarmonicLength) continue;
			if(harmonic.getAverageNote() < GraphEditor.minViewNote || harmonic.getAverageNote() > GraphEditor.maxViewNote) continue;
			if(harmonic.getMaxLogAmplitude() < GraphEditor.minViewLogAmplitude) continue;
			if(harmonic.getMaxLogAmplitude() > GraphEditor.maxViewLogAmplitude) continue;
			ArrayList<FDData> hData = new ArrayList<FDData>(harmonic.getAllDataInterpolated().values());
			if(hData.size() < 2) continue;
			FDData start = hData.get(0);
			for(int index = 1; index < hData.size(); index++) {
				FDData end = hData.get(index);
				if(start.getTime() > GraphEditor.maxViewTime) {
					start = end;
					continue;
				}
				int startX = 0;
				int startY = 0;
				if(xView == XView.TIME) startX = (int) Math.round(pixelsPerValueX * (start.getTime() - GraphEditor.minViewTime));
				if(xView == XView.FREQUENCY) startX = (int) Math.round(pixelsPerValueX * (start.getNote() - GraphEditor.minViewNote));
				if(yView == YView.AMPLITUDE) startY = (int) Math.round(pixelsPerValueY * (start.getLogAmplitude() - GraphEditor.minViewLogAmplitude));
				if(yView == YView.FREQUENCY) startY = (int) Math.round(pixelsPerValueY * (start.getNote() - GraphEditor.minViewNote));
				startY = getHeight() - startY;
				if(end.getTime() < GraphEditor.minViewTime) {
					start = end;
					continue;					
				}
				int endX = 0;
				int endY = 0;
				if(xView == XView.TIME) endX = (int) Math.round(pixelsPerValueX * (end.getTime() - GraphEditor.minViewTime));
				if(xView == XView.FREQUENCY) endX = (int) Math.round(pixelsPerValueX * (end.getNote() - GraphEditor.minViewNote));		
				if(yView == YView.AMPLITUDE) endY = (int) Math.round(pixelsPerValueY * (end.getLogAmplitude() - GraphEditor.minViewLogAmplitude));
				if(yView == YView.FREQUENCY) endY = (int) Math.round(pixelsPerValueY * (end.getNote() - GraphEditor.minViewNote));		
				endY = getHeight() - endY;
				if(colorView == ColorView.DATA) {
					if(xView == XView.FREQUENCY) {
						g.setColor(getTimeColor((start.getTime() + end.getTime()) / 2));
					}				
					if(xView == XView.TIME && yView == YView.FREQUENCY) {
						g.setColor(getAmplitudeColor((start.getLogAmplitude() + end.getLogAmplitude()) / 2));
					}
					if(xView == XView.TIME && yView == YView.AMPLITUDE) {
						g.setColor(getFrequencyColor((start.getNote() + end.getNote()) / 2));
					}
				}
				if(colorView == ColorView.HARMONICS) {
					g.setColor(getHarmonicColor(harmonic.getHarmonicID()));
				}
				if(colorView == ColorView.CHANNEL) {
					if(start.getChannel() == end.getChannel()) {
						if(start.getChannel() == 0) g.setColor(Color.RED);
						if(start.getChannel() == 1) g.setColor(Color.GREEN);
						if(start.getChannel() == 2) g.setColor(Color.BLUE);
						if(start.getChannel() < 0 || start.getChannel() > 2) {
							System.out.println("GraphView.drawFileData: unknow channel");
							g.setColor(Color.WHITE);
						}
					} else {
						System.out.println("GraphView.drawFileData: different channels in same harmonic");
						g.setColor(Color.WHITE);
					}
				}
				/*
				if(GraphEditor.harmonicIDToControlPointHarmonic.containsKey(harmonic.getHarmonicID())) {
					if(harmonic.containsData(start.getTime(), start.getNote())) {
						g.fillRect(startX - 1, startY - 1, 3, 3);
						g.fillRect(endX - 1, endY - 1, 3, 3);
					}
				}
				*/
				if(GraphEditor.selectedHarmonicIDs.contains(harmonic.getHarmonicID())) {
					if(!GraphEditor.displaySelectedHarmonics) {
						g.setColor(new Color(0.5f, 0.5f, 0.5f, 0.75f));
					} else {
						g.setColor(new Color(1.0f, 1.0f, 1.0f, 1.0f));
					}
				} else {
					if(!GraphEditor.displayUnselectedHarmonics) {
						g.setColor(new Color(0.5f, 0.5f, 0.5f, 0.75f));
					}
				}
				g.fillRect(startX - 1, startY - 1, 3, 3);
				g.drawLine(startX, startY, endX, endY);
				start = end;
			}
		}
		drawViewInfo(g);
	}
	
	private Color getAmplitudeColor(double amplitude) {
		return getAmplitudeColor(amplitude, 1.0f);
	}

	private Color getAmplitudeColor(double amplitude, float alpha) {
		float noteRange = (float) (GraphEditor.maxViewLogAmplitude - GraphEditor.minViewLogAmplitude);
		float currentVal = (float) amplitude;
		currentVal -= GraphEditor.minViewLogAmplitude;
		currentVal /= noteRange;
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
		return new Color(red, green, blue, alpha);
	}
	
	private Color getFrequencyColor(double note) {
		return getFrequencyColor(note, 1.0f);
	}

	private Color getFrequencyColor(double note, float alpha) {
		float noteRange = (float) (GraphEditor.maxViewNote - GraphEditor.minViewNote);
		float currentVal = (float) note;
		currentVal -= GraphEditor.minNote;
		currentVal /= noteRange;
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
		return new Color(blue, green, red, alpha);
	}
	
	private Color getTimeColor(double time) {
		return getTimeColor(time, 1.0f);
	}

	private Color getTimeColor(double time, float alpha) {
		float timeRange = (float) (GraphEditor.maxViewTime - GraphEditor.minViewTime);
		float currentVal = (float) time;
		currentVal -= GraphEditor.minViewTime;
		currentVal /= timeRange;
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
		return new Color(blue, green, red, alpha);
	}
	
	private Color getHarmonicColor(long harmonicID) {
		int red = (int) harmonicID % 64;
		int green = (int) (harmonicID / 64) % 64;
		int blue = (int) (harmonicID / (64 * 64)) % 64;
		return new Color(red + 192, green + 192, blue + 192);
	}
	
	public void drawViewInfo(Graphics g) {
		double tVMin = GraphEditor.minViewTime / (1000.0 / FDData.timeStepInMillis);
		double tVMax = GraphEditor.maxViewTime / (1000.0 / FDData.timeStepInMillis);
		double fVMin = GraphEditor.noteToFrequencyInHz(GraphEditor.minViewNote);
		double fVMax = GraphEditor.noteToFrequencyInHz(GraphEditor.maxViewNote);
		double lAVMin = Math.round(GraphEditor.minViewLogAmplitude * 100.0) / 100.0;
		double lAVMax = Math.round(GraphEditor.maxViewLogAmplitude * 100.0) / 100.0;
		double tMin = GraphEditor.minTime / (1000.0 / FDData.timeStepInMillis);
		double tMax = GraphEditor.maxTime / (1000.0 / FDData.timeStepInMillis);
		double fMin = GraphEditor.noteToFrequencyInHz(GraphEditor.minNote);
		double fMax = GraphEditor.noteToFrequencyInHz(GraphEditor.maxNote);
		double lAMin = Math.round(GraphEditor.minLogAmplitude * 100.0) / 100.0;
		double lAMax = Math.round(GraphEditor.maxLogAmplitude * 100.0) / 100.0;
		int fontHeight = g.getFontMetrics().getHeight();
		int maxWidth = (int) Math.round(g.getFontMetrics().getStringBounds("XXXX: XXXXX.XE | XXXXX.XE", g).getWidth());
		g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.75f));
		g.fillRect(0, 0, maxWidth, fontHeight * 7);
		g.setColor(new Color(0.0f, 0.0f, 0.0f, 0.75f));
		g.drawString(new String("Tmin: " + tVMin + " | " + tMin), 0, fontHeight);
		g.drawString(new String("Tmax: " + tVMax + " | " + tMax), 0, fontHeight * 2);
		g.drawString(new String("Fmin: " + fVMin + " | " + fMin), 0, fontHeight * 3);
		g.drawString(new String("Fmax: " + fVMax + " | " + fMax), 0, fontHeight * 4);
		g.drawString(new String("LAmin: " + lAVMin + " | " + lAMin), 0, fontHeight * 5);
		g.drawString(new String("LAmax: " + lAVMax + " | " + lAMax), 0, fontHeight * 6);
	}
	
	public void drawPlayTime(int offsetInMillis) {
		drawPlaying = true;
		GraphView.offsetInMillis = (int) Math.round(offsetInMillis);
	}
	
	public void refresh() {
		refresh = true;
		paintImmediately(0, 0, getWidth(), getHeight());
	}
	
	public int getTimeAxisWidthInMillis() {
   		return (int) Math.round((GraphEditor.maxViewTime - GraphEditor.minViewTime) * FDData.timeStepInMillis);
	}
	
    protected void paintComponent(Graphics g) {
    	//System.out.println("Paint Component");
    	if(drawPlaying) {
    		double pixelsPerTime = (double) getWidth() / (double) (GraphEditor.maxViewTime - GraphEditor.minViewTime);
    		double millisPerPixel = (double) FDData.timeStepInMillis / pixelsPerTime;
    		int startX = (int) Math.round((double) offsetInMillis / millisPerPixel);
    		g.drawImage(bi, 0, 0, null);
       		g.setColor(new Color(0.5f, 0.5f, 0.5f, 0.75f));
    		g.fillRect(startX, 0, 1, getHeight());    		
    		drawPlaying = false;
    		return;
    	}
    	if(refresh || height != getHeight() || width != getWidth()) {
    		bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
    		Graphics2D g2 = bi.createGraphics();
    		super.paintComponent(g);
    		drawFileData(g2);
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
