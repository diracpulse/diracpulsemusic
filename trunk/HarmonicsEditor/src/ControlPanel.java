import java.awt.Color;
import java.awt.Graphics;
import java.util.TreeSet;


public class ControlPanel {
	
	private int segmentWidth;
	private int segmentHeight;
	private int[] durations = {1, 2, 3, 4, 6, 8, 16};
	
	private int octaveSelected = -1;
	private int noteSelected = -1;
	private TreeSet<Integer> overtonesSelected = new TreeSet<Integer>();
	private int durationSelected = -1;
	
	public ControlPanel(int segmentWidth, int segmentHeight) {
		this.segmentWidth = segmentWidth;
		this.segmentHeight = segmentHeight;
	}

	public void paintComponent(Graphics g) {
		drawOctaves(g);
		drawNotes(g);
		drawOvertones(g);
		drawDurations(g);
	}
	
	public void drawOctaves(Graphics g) {
		Color white = new Color(0.0f, 0.0f, 0.0f);
		Color black = new Color(1.0f, 1.0f, 1.0f);
		int screenX = 0;
		int screenY = 0;
		for(int octave = 16384; octave >= 16; octave /= 2) {
			if(octave == octaveSelected) {
				HarmonicsUtils.DrawIntegerHorizontal(g, black, white, screenX, screenY, 5, octave);
			} else {
				HarmonicsUtils.DrawIntegerHorizontal(g, white, black, screenX, screenY, 5, octave);
			}
			screenY += segmentHeight * 2;
		}
	}
	
	public void selectOctave(int y) {
		int index = y / (segmentHeight * 2);
		int octave = (int) Math.pow(2.0, (14 - index));
		if(octave < 16) {
			System.out.println("Octave too low");
		} else {
			octaveSelected = octave;
			System.out.println("Octave selected: " + octave);
		}
	}
	
	public void drawNotes(Graphics g) {
		Color white = new Color(0.0f, 0.0f, 0.0f);
		Color black = new Color(1.0f, 1.0f, 1.0f);
		int screenX = 6 * segmentWidth;
		int screenY = 0;
		for(int note = 30; note >= 0; note--) {
			if(note == noteSelected) {
				HarmonicsUtils.DrawIntegerHorizontal(g, black, white, screenX, screenY, 2, note);
			} else {
				HarmonicsUtils.DrawIntegerHorizontal(g, white, black, screenX, screenY, 2, note);
			}
			screenY += segmentHeight * 2;
		}
	}
	
	public void selectNote(int y) {
		int index = y / (segmentHeight * 2);
		int note = 30 - index;
		if(note < 0) {
			System.out.println("Note too low");
		} else {
			noteSelected = note;
			System.out.println("Note selected: " + note);
		}
	}
	
	public void drawOvertones(Graphics g) {
		Color white = new Color(0.0f, 0.0f, 0.0f);
		Color black = new Color(1.0f, 1.0f, 1.0f);
		int screenX = 9 * segmentWidth;
		int screenY = 0;
		for(int overtone = 7; overtone >= 2 ; overtone--) {
			if(overtonesSelected.contains(overtone)) {
				HarmonicsUtils.DrawIntegerHorizontal(g, black, white, screenX, screenY, 1, overtone);
			} else {
				HarmonicsUtils.DrawIntegerHorizontal(g, white, black, screenX, screenY, 1, overtone);
			}
			screenY += segmentHeight * 2;
		}
	}
	
	public void selectOvertone(int y) {
		int index = y / (segmentHeight * 2);
		int overtone = 7 - index;
		if(overtone < 2) {
			System.out.println("Overtone too low");
		} else {
			overtonesSelected.add(overtone);
			System.out.println("Overtone selected: " + overtone);
		}
	}
	
	public void drawDurations(Graphics g) {	
		Color white = new Color(0.0f, 0.0f, 0.0f);
		Color black = new Color(1.0f, 1.0f, 1.0f);
		int screenX = 11 * segmentWidth;
		int screenY = 0;
		for(int index = 0; index < durations.length; index++) {
			if(durations[index] == durationSelected) {
				HarmonicsUtils.DrawIntegerHorizontal(g, black, white, screenX, screenY, 2, durations[index]);
			} else {
				HarmonicsUtils.DrawIntegerHorizontal(g, white, black, screenX, screenY, 2, durations[index]);
			}
			screenY += segmentHeight * 2;
		}		
	}
	
	public void selectDuration(int y) {
		int index = y  / (segmentHeight * 2);
		if(index >= durations.length) {
			System.out.println("Invalid duration");
			return;
		}
		int duration = durations[index];
		durationSelected = duration;
		HarmonicsEditor.addBeat(octaveSelected, noteSelected, overtonesSelected, durationSelected);
		System.out.println("Duration selected: " + duration);
	}	
	
	public void handleMouseClick(int x, int y) {
		if(x < 6 * segmentWidth) {
			selectOctave(y);
			return;
		}
		if(x < 9 * segmentWidth) {
			selectNote(y);
			return;
		}
		if(x < 11 * segmentWidth) {
			selectOvertone(y);
			return;
		}
		if(x < 14 * segmentWidth) {
			selectDuration(y);
			return;
		}
		System.out.println("ControlPanel.handleMouseClick: Outside control panel");
	}
	
}
