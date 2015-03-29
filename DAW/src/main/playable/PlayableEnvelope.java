
package main.playable;

import java.awt.Graphics;
import java.awt.Graphics2D;

public class PlayableEnvelope implements PlayableModule {

	PlayableEditor parent;
	Slider attack;
	Slider decay;
	Slider sustain;
	Slider release;
	private int maxScreenX;
	private long startTimeInSamples = 0;
	private long stopTimeInSamples = 0;
	private boolean off = false;
	int attackInSamples;
	int decayInSamples;
	double sustainValue;
	int releaseInSamples;
	double tau = 2.0;
	double currentValue = 0.0;
	double saveAttackValue;
	double saveDecayValue;
	
	public PlayableEnvelope(PlayableEditor parent, int screenX, int screenY) {
		this.parent = parent;
		int x = screenX;
		int y = screenY;
		attack = new Slider(Slider.Type.LOGARITHMIC, x, y, 400, 0.00005, 0.1, .01, "Attack");
		x = attack.getMaxX();
		decay = new Slider(Slider.Type.LOGARITHMIC, x, y, 400, 0.00005, 0.1, .01, "Decay");
		x = decay.getMaxX();
		sustain = new Slider(Slider.Type.LOGARITHMIC, x, y, 400, Math.exp(-1.0 * tau), 1.0, .5, "Sustain");
		x = sustain.getMaxX();
		release = new Slider(Slider.Type.LOGARITHMIC, x, y, 400, 0.00005, 0.1, .1, "Release");
		maxScreenX = release.getMaxX();
	}
	
	public int getMaxScreenX() {
		return maxScreenX;
	}
	
	public synchronized void noteOn(long currentTimeInSamples) {
		off = false;
		startTimeInSamples = currentTimeInSamples;
		attackInSamples = (int) Math.round(attack.getCurrentValue() * AudioFetcher.sampleRate);
		decayInSamples = (int) Math.round(decay.getCurrentValue() * AudioFetcher.sampleRate);
		sustainValue = sustain.getCurrentValue();
		releaseInSamples = (int) Math.round(release.getCurrentValue() * AudioFetcher.sampleRate);
	}
	
	public synchronized void noteOff(long currentTimeInSamples) {
		off = true;
		stopTimeInSamples = currentTimeInSamples;
	}
	
	public double getSample(long absoluteTimeInSamples) {
		long currentTimeInSamples = absoluteTimeInSamples - startTimeInSamples;
		if(!off) {
			if(currentTimeInSamples <= attackInSamples) {
				return 1.0 - Math.exp(-1.0 * currentTimeInSamples * tau / attackInSamples);
			}
			if(currentTimeInSamples < attackInSamples + decayInSamples) {
				double decayVal = Math.exp(-1.0 * tau * (currentTimeInSamples - attackInSamples) / (double) decayInSamples);
				saveDecayValue = (1.0 - sustainValue) * decayVal + sustainValue;
				return saveDecayValue;
			}
			return saveDecayValue;
		}
		return Math.exp(-1.0 * tau * (absoluteTimeInSamples - stopTimeInSamples) / (double) releaseInSamples) * saveDecayValue;
	} 
	
	public double getSampleLinear(long absoluteTimeInSamples) {
		long currentTimeInSamples = absoluteTimeInSamples - startTimeInSamples;
		if(!off) {
			if(currentTimeInSamples < attackInSamples) {
				return currentTimeInSamples / attackInSamples;
			}
			if(currentTimeInSamples < attackInSamples + decayInSamples) {
				double decayVal = (currentTimeInSamples - attackInSamples) / (double) decayInSamples;
				return ((1.0 - decayVal) * (1.0 - sustainValue)) + sustainValue;
			}
			return sustainValue;
		}
		double releaseVal =  1.0 - (absoluteTimeInSamples - stopTimeInSamples) / releaseInSamples;
		if(releaseVal > 0.0) return sustainValue * releaseVal;
		return 0.0;
	} 
	
	public void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		attack.draw(g2);
		decay.draw(g2);
		sustain.draw(g2);
		release.draw(g2);
		parent.view.repaint();
	}

	public void pointSelected(int x, int y) {
		attack.pointSelected(x, y);
		decay.pointSelected(x, y);
		sustain.pointSelected(x, y);
		release.pointSelected(x, y);
		parent.view.repaint();
	}
	
}
