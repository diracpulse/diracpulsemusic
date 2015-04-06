
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
	double tau = 4.0;
	double currentValue = 0.0;
	double saveAttackValue;
	double saveDecayValue;
	private int yPadding = PlayableEditor.moduleYPadding;
	private EnvelopeType type;
	private double saveValue = 0.0;
	
	public enum EnvelopeType {
		AR,
		ASR,
		ADSR;
	}
	
	public PlayableEnvelope(PlayableEditor parent, EnvelopeType type, int screenX, int screenY, String moduleName) {
		this.parent = parent;
		this.type = type;
		this.moduleName = moduleName;
		int x = screenX;
		int y = screenY + PlayableEditor.moduleYPadding;
		this.screenX = x;
		this.screenY = screenY;
		double defaultAD = 0.1;
		double defaultR = 0.1;
		double minValADR = 0.001;
		double maxValADR = 2.0;
		switch(type) {
		case ADSR:
			attack = new Slider(Slider.Type.LOGARITHMIC, x, y, 400, minValADR, maxValADR, defaultAD, new String[]{"A", " ", " "});
			x = attack.getMaxX();
			decay = new Slider(Slider.Type.LOGARITHMIC, x, y, 400, minValADR, maxValADR, defaultAD, new String[]{"D", " ", " "});
			x = decay.getMaxX();
			sustain = new Slider(Slider.Type.LOGARITHMIC, x, y, 400, Math.exp(-1.0 * tau), 1.0, .5, new String[] {"S", " ", " "});
			x = sustain.getMaxX();
			release = new Slider(Slider.Type.LOGARITHMIC, x, y, 400, minValADR, maxValADR, defaultR, new String[]{"R", " ", " "});
			x = release.getMaxX();
			depth = new Slider(Slider.Type.LINEAR, x, y, 400, 0.0, 1.0, 0.5, new String[]{"DEPTH", " ", " "});
			maxScreenX = depth.getMaxX();
			return;
		case ASR:
			attack = new Slider(Slider.Type.LOGARITHMIC, x, y, 400, minValADR, maxValADR, defaultAD, new String[]{"A", " ", " "});
			x = attack.getMaxX();
			sustain = new Slider(Slider.Type.LOGARITHMIC, x, y, 400, Math.exp(-1.0 * tau), 1.0, .5, new String[] {"S", " ", " "});
			x = sustain.getMaxX();
			release = new Slider(Slider.Type.LOGARITHMIC, x, y, 400, minValADR, maxValADR, defaultR, new String[]{"R", " ", " "});
			x = release.getMaxX();
			maxScreenX = release.getMaxX();
			return;
		case AR:
			attack = new Slider(Slider.Type.LOGARITHMIC, x, y, 400, minValADR, maxValADR, defaultAD, new String[]{"A", " ", " "});
			x = attack.getMaxX();
			release = new Slider(Slider.Type.LOGARITHMIC, x, y, 400, minValADR, maxValADR, defaultR, new String[]{"R", " ", " "});
			x = release.getMaxX();
			depth = new Slider(Slider.Type.LINEAR, x, y, 400, 0.0, 1.0, 0.5, new String[]{"D", " ", " "});
			x = depth.getMaxX();
			maxScreenX = depth.getMaxX();
			return;
		}
	}
	
	public int getMaxScreenX() {
		return maxScreenX;
	}
	
	public synchronized void noteOn(long currentTimeInSamples) {
		off = false;
		startTimeInSamples = currentTimeInSamples;
		attackInSamples = (int) Math.round(attack.getCurrentValue() * AudioFetcher.sampleRate);
		if(type == EnvelopeType.ADSR) {
			decayInSamples = (int) Math.round(decay.getCurrentValue() * AudioFetcher.sampleRate);
			sustainValue = sustain.getCurrentValue();
		}
		if(type == EnvelopeType.ASR) {
			sustainValue = sustain.getCurrentValue();
		}
		releaseInSamples = (int) Math.round(release.getCurrentValue() * AudioFetcher.sampleRate);
	}
	
	public synchronized void noteOff(long currentTimeInSamples) {
		off = true;
		stopTimeInSamples = currentTimeInSamples;
	}
	
	public double getSample(long absoluteTimeInSamples) {
		switch(type) {
		case AR:
			return getSampleAR(absoluteTimeInSamples);
		case ASR:
			return getSampleASR(absoluteTimeInSamples);
		case ADSR:
			return getSampleADSR(absoluteTimeInSamples);
		}
		return 0.0;
	}
	
	private double getSampleAR(long absoluteTimeInSamples) {
		double depthVal = depth.getCurrentValue();
		long currentTimeInSamples = absoluteTimeInSamples - startTimeInSamples;
		if(currentTimeInSamples <= attackInSamples) {
			return (1.0 - Math.exp(-1.0 * currentTimeInSamples * tau / attackInSamples)) * depthVal;
		}
		return Math.exp(-1.0 * tau * (currentTimeInSamples - attackInSamples) / (double) releaseInSamples) * depthVal;
	} 
	
	private double getSampleASR(long absoluteTimeInSamples) {
		double sustainVal = sustain.getCurrentValue();
		long currentTimeInSamples = absoluteTimeInSamples - startTimeInSamples;
		if(!off) {
			if(currentTimeInSamples <= attackInSamples) {
				saveValue = sustainVal - Math.exp(-1.0 * currentTimeInSamples * tau / attackInSamples) * sustainVal;
				return saveValue;
			}
			return saveValue;
		}
		return Math.exp(-1.0 * tau * (absoluteTimeInSamples - stopTimeInSamples) / (double) releaseInSamples) * saveValue;
	} 
	
	private double getSampleADSR(long absoluteTimeInSamples) {
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
		if(type == EnvelopeType.ADSR) {
			depth.draw(g2);
			decay.draw(g2);
			sustain.draw(g2);
		}
		if(type == EnvelopeType.ASR) {
			sustain.draw(g2);
		}
		if(type == EnvelopeType.AR) {
			depth.draw(g2);
		}
		release.draw(g2);
		parent.view.repaint();
	}

	public void pointSelected(int x, int y) {
		attack.pointSelected(x, y);
		if(type == EnvelopeType.ADSR) {
			depth.pointSelected(x, y);
			decay.pointSelected(x, y);
			sustain.pointSelected(x, y);
		}
		if(type == EnvelopeType.ASR) {
			sustain.pointSelected(x, y);
		}
		if(type == EnvelopeType.AR) {
			depth.pointSelected(x, y);
		}
		release.pointSelected(x, y);
		parent.view.repaint();
	}
	
}
