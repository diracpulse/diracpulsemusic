package main.playable;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import main.SynthTools;

public class PlayableSnare implements Playable {
	
	PlayableWaveformEditor parent;
	double minLogFreq = 5.0;
	double maxLogFreq = 14.0;
	double minLogAmp = 0.0;
	double maxLogAmp = Math.log(Short.MAX_VALUE) / Math.log(2.0);
	int maxDurationInSamples = (int) Math.round(1.0 * SynthTools.sampleRate);
	double freq = 128.0;
	double amp = 1.0;
	double b = 0.5;
	private Slider freqControl;
	private Slider ampControl;
	private Slider bControl;
	private Button button;
	
	public enum WaveformType {
		SINE,
		TRIANGLE,
		SQUAREWAVE,
		SAWTOOTH,
	}

	public PlayableSnare(PlayableWaveformEditor parent) {
		this.parent = parent;
		freqControl = new Slider(minLogFreq, maxLogFreq, new Rectangle(4, 4, 16, 700));
		ampControl = new Slider(minLogAmp, maxLogAmp, new Rectangle(24, 4, 16, 700));
		bControl = new Slider(0.0, 1.0, new Rectangle(44, 4, 16, 700));
		button = new Button(parent, this, new Rectangle(4, 704, 40, 20));
	}

	public void triggered(double[] loopData, int loopIndex) {
		int p = (int) Math.round(SynthTools.sampleRate / (freq - 0.5));
		double[] snareOnly = new double[maxDurationInSamples];
		for(int index = 0; index < p; index++) {
			snareOnly[index] = Math.random() - 0.5;
		}
		int snareIndex = p + 1;
		while(snareIndex < snareOnly.length) {
			if(Math.random() < b) {
				snareOnly[snareIndex] += 0.5 * (snareOnly[snareIndex - p] + snareOnly[snareIndex - p - 1]);
			} else {
				snareOnly[snareIndex] -= 0.5 * (snareOnly[snareIndex - p] + snareOnly[snareIndex - p - 1]);
			}
			snareIndex++;
		}
		for(int index = loopIndex; index < loopIndex + snareOnly.length; index++) {
			if(index >= loopData.length) return;
			loopData[index] += snareOnly[index - loopIndex];
		}
	}

	public void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g; 
		freqControl.draw(g2);
		ampControl.draw(g2);
		bControl.draw(g2);
		button.draw(g2);
	}

	public void pointSelected(int x, int y) {
		freqControl.pointSelected(x, y);
		ampControl.pointSelected(x, y);
		bControl.pointSelected(x, y);
		button.pointSelected(x, y);
		parent.view.repaint();
	}
}
