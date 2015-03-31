
package main.playable;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class PlayableEnvelope implements PlayableModule {

	PlayableEditor parent;
	Slider attack;
	Slider decay;
	Slider sustain;
	Slider release;
	Slider depth;
	String moduleName;
	private int screenX;
	private int screenY;
	private int maxScreenX;
	private int xPadding = 12;
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
	private int yPadding = PlayableEditor.moduleYPadding;
	
	public PlayableEnvelope(PlayableEditor parent, int screenX, int screenY, String moduleName) {
		this.parent = parent;
		this.moduleName = moduleName;
		int x = screenX;
		int y = screenY + PlayableEditor.moduleYPadding;
		this.screenX = x;
		this.screenY = screenY;
		double defaultAD = 0.01;
		double defaultR = 0.1;
		double minValADR = 0.0005;
		double maxValADR = 1.0;
		String ADRTop = new String(new Float(minValADR).toString());
		String ADRBottom = new String(new Float(minValADR).toString());
		String STop = "1.0";
		String SBottom = new String(new Float(tau).toString());
		attack = new Slider(Slider.Type.LOGARITHMIC, x, y, 400, minValADR, maxValADR, defaultAD, new String[]{"A", ADRTop, ADRBottom});
		x = attack.getMaxX();
		decay = new Slider(Slider.Type.LOGARITHMIC, x, y, 400, minValADR, maxValADR, defaultAD, new String[]{"D", ADRTop, ADRBottom});
		x = decay.getMaxX();
		sustain = new Slider(Slider.Type.LOGARITHMIC, x, y, 400, Math.exp(-1.0 * tau), 1.0, .5, new String[] {"S", STop, SBottom});
		x = sustain.getMaxX();
		release = new Slider(Slider.Type.LOGARITHMIC, x, y, 400, minValADR, maxValADR, defaultR, new String[]{"R", ADRTop, ADRBottom});
		x = release.getMaxX();
		depth = new Slider(Slider.Type.LINEAR, x, y, 400, 0.0, 1.0, 0.5, new String[]{"AMT", ADRTop, ADRBottom});
		maxScreenX = depth.getMaxX();
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
		double depthVal = depth.getCurrentValue();
		long currentTimeInSamples = absoluteTimeInSamples - startTimeInSamples;
		if(!off) {
			if(currentTimeInSamples <= attackInSamples) {
				return (1.0 - Math.exp(-1.0 * currentTimeInSamples * tau / attackInSamples)) * depthVal + (1.0 - depthVal);
			}
			if(currentTimeInSamples < attackInSamples + decayInSamples) {
				double decayVal = Math.exp(-1.0 * tau * (currentTimeInSamples - attackInSamples) / (double) decayInSamples);
				saveDecayValue = ((1.0 - sustainValue) * decayVal + sustainValue) * depthVal + (1.0 - depthVal);
				return saveDecayValue;
			}
			return saveDecayValue;
		}
		return Math.exp(-1.0 * tau * (absoluteTimeInSamples - stopTimeInSamples) / (double) releaseInSamples) * saveDecayValue * depthVal + (1.0 - depthVal);
	} 
	
	public double getFilterSample(long absoluteTimeInSamples) {
		double depthVal = depth.getCurrentValue();
		long currentTimeInSamples = absoluteTimeInSamples - startTimeInSamples;
		if(!off) {
			if(currentTimeInSamples <= attackInSamples) {
				return (1.0 - Math.exp(-1.0 * currentTimeInSamples * tau / attackInSamples)) * depthVal;
			}
			if(currentTimeInSamples < attackInSamples + decayInSamples) {
				double decayVal = Math.exp(-1.0 * tau * (currentTimeInSamples - attackInSamples) / (double) decayInSamples);
				saveDecayValue = ((1.0 - sustainValue) * decayVal + sustainValue) * depthVal;
				return saveDecayValue;
			}
			return saveDecayValue;
		}
		return Math.exp(-1.0 * tau * (absoluteTimeInSamples - stopTimeInSamples) / (double) releaseInSamples) * saveDecayValue * depthVal;
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
		g2.setFont(new Font("TRUETYPE_FONT", Font.BOLD, 12));
		Font font = g2.getFont();
		FontMetrics metrics = g2.getFontMetrics(font);
		int hgt = metrics.getHeight();
		int adv = metrics.stringWidth(moduleName);
		g2.setColor(Color.WHITE);
		int textYOffset = (yPadding - hgt) / 2 + yPadding / 2;
		int textXOffset = (maxScreenX - screenX - adv) / 2;
		g2.setColor(new Color(0.5f, 0.0f, 0.0f));
		g2.fillRect(screenX + textXOffset - 4, screenY, maxScreenX - screenX - textXOffset * 2 + 8, yPadding - 4);
		g2.setColor(Color.WHITE);
		g2.drawString(moduleName, screenX + textXOffset, screenY + textYOffset);
		g2.setColor(Color.BLUE);
		g2.drawRect(screenX, screenY, maxScreenX - screenX, attack.getMaxY());
		attack.draw(g2);
		decay.draw(g2);
		sustain.draw(g2);
		release.draw(g2);
		depth.draw(g2);
		parent.view.repaint();
	}

	public void pointSelected(int x, int y) {
		attack.pointSelected(x, y);
		decay.pointSelected(x, y);
		sustain.pointSelected(x, y);
		release.pointSelected(x, y);
		depth.pointSelected(x, y);
		parent.view.repaint();
	}
	
}
