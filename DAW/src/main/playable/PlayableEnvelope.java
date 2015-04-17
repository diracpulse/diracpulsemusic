
package main.playable;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;

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
		double maxValADR = 8.0;
		switch(type) {
		case ADSR:
			attack = new Slider(Slider.Type.LOGARITHMIC, x, y, minValADR, maxValADR, defaultAD, new String[]{"A", "s", " "});
			x = attack.getMaxX();
			decay = new Slider(Slider.Type.LOGARITHMIC, x, y, minValADR, maxValADR, defaultAD, new String[]{"D", "s", " "});
			x = decay.getMaxX();
			sustain = new Slider(Slider.Type.LOGARITHMIC_ZERO, x, y, 1.0 / 8.0, 1.0, .5, new String[] {"S", "", " "});
			x = sustain.getMaxX();
			release = new Slider(Slider.Type.LOGARITHMIC, x, y, minValADR, maxValADR, defaultR, new String[]{"R", "s", " "});
			x = release.getMaxX();
			depth = new Slider(Slider.Type.LOGARITHMIC, x, y, 1.0 / 256.0, 1.0, 0.5, new String[]{"AMT", "", ""});
			maxScreenX = depth.getMaxX();
			return;
		case ASR:
			attack = new Slider(Slider.Type.LOGARITHMIC, x, y, minValADR, maxValADR, defaultAD, new String[]{"A", "s", " "});
			x = attack.getMaxX();
			release = new Slider(Slider.Type.LOGARITHMIC, x, y, minValADR, maxValADR, defaultR, new String[]{"R", "s", " "});
			x = release.getMaxX();
			depth = new Slider(Slider.Type.LOGARITHMIC, x, y, 1.0 / 256.0, 1.0, 0.5, new String[]{"AMT", "", ""});
			maxScreenX = depth.getMaxX();
			return;
		case AR:
			attack = new Slider(Slider.Type.LOGARITHMIC, x, y, minValADR, maxValADR, defaultAD, new String[]{"A", "s", " "});
			x = attack.getMaxX();
			release = new Slider(Slider.Type.LOGARITHMIC, x, y, minValADR, maxValADR, defaultR, new String[]{"R", "s", " "});
			x = release.getMaxX();
			depth = new Slider(Slider.Type.LOGARITHMIC, x, y, 1.0 / 256.0, 1.0, 0.5, new String[]{"AMT",  "", ""});
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
		saveDecayValue = 0.0;
		attackInSamples = (int) Math.round(attack.getCurrentValue() * AudioFetcher.sampleRate);
		if(type == EnvelopeType.ADSR) {
			decayInSamples = (int) Math.round(decay.getCurrentValue() * AudioFetcher.sampleRate);
			sustainValue = sustain.getCurrentValue();
		}
		releaseInSamples = (int) Math.round(release.getCurrentValue() * AudioFetcher.sampleRate);
	}
	
	public synchronized void noteOff(long currentTimeInSamples) {
		off = true;
		stopTimeInSamples = currentTimeInSamples;
	}
	
	public double getSample(long absoluteTimeInSamples, boolean absolute) {
		switch(type) {
		case AR:
			return getSampleAR(absoluteTimeInSamples, absolute);
		case ASR:
			return getSampleASR(absoluteTimeInSamples, absolute);
		case ADSR:
			return getSampleADSR(absoluteTimeInSamples, absolute);
		}
		return 0.0;
	}
	
	private double getSampleAR(long absoluteTimeInSamples, boolean absolute) {
		double depthVal = depth.getCurrentValue();
		long currentTimeInSamples = absoluteTimeInSamples - startTimeInSamples;
		if(currentTimeInSamples <= attackInSamples) {
			saveAttackValue = 1.0 - Math.exp(-1.0 * tau * currentTimeInSamples / attackInSamples);
			if(absolute) return saveAttackValue * depthVal;
			return saveAttackValue * depthVal + (1.0 - depthVal);
		}
		double returnVal = Math.exp(-1.0 * tau * (currentTimeInSamples - attackInSamples) / releaseInSamples) * saveAttackValue * depthVal;
		if(absolute) return returnVal;
		return returnVal + (1.0 - depthVal);
	} 
	
	private double getSampleASR(long absoluteTimeInSamples, boolean absolute) {
		double depthVal = depth.getCurrentValue();
		long currentTimeInSamples = absoluteTimeInSamples - startTimeInSamples;
		if(!off) { 
			saveAttackValue = (1.0 - Math.exp(-1.0 * tau * currentTimeInSamples / attackInSamples));
			if(absolute) return saveAttackValue * depthVal;
			return saveAttackValue * depthVal + (1.0 - depthVal);
		}
		double returnVal = Math.exp(-1.0 * tau * (absoluteTimeInSamples - stopTimeInSamples) / releaseInSamples) * saveAttackValue * depthVal;
		if(absolute) return returnVal;
		return returnVal + (1.0 - depthVal);
	} 
	
	private double getSampleADSR(long absoluteTimeInSamples, boolean absolute) {
		double depthVal = depth.getCurrentValue();
		saveDecayValue = 0;
		long currentTimeInSamples = absoluteTimeInSamples - startTimeInSamples;
		if(!off) {
			if(currentTimeInSamples <= attackInSamples) {
				saveAttackValue = (1.0 - Math.exp(-1.0 * tau * currentTimeInSamples / attackInSamples));
				if(absolute) return saveAttackValue * depthVal;
				return saveAttackValue * depthVal + (1.0 - depthVal);
			}
			saveDecayValue = saveAttackValue * Math.exp(-1.0 * tau * (currentTimeInSamples - attackInSamples) / (double) decayInSamples);
			if(saveDecayValue > sustainValue) { 
				if(absolute) return saveDecayValue * depthVal;
				return saveDecayValue * depthVal + (1.0 - depthVal);
			} else {
				saveDecayValue = sustainValue;
			}
			if(absolute) return saveDecayValue * depthVal;
			return saveDecayValue * depthVal + (1.0 - depthVal);
		}
		if(saveDecayValue == 0) saveDecayValue = saveAttackValue;
		double returnVal = Math.exp(-1.0 * tau * (absoluteTimeInSamples - stopTimeInSamples) / (double) releaseInSamples) * saveDecayValue * depthVal;
		if(absolute) return returnVal;
		return returnVal + (1.0 - depthVal);
	}
	
	private double getFilterSample(long absoluteTimeInSamples) {
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
	
	private double getSampleLinear(long absoluteTimeInSamples) {
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
		g2.setFont(new Font("TRUETYPE_FONT", Font.BOLD, 10));
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
		depth.draw(g2);
		release.draw(g2);
		parent.view.repaint();
	}

	public void pointSelected(int x, int y) {
		attack.pointSelected(x, y);
		if(type == EnvelopeType.ADSR) {
			decay.pointSelected(x, y);
			sustain.pointSelected(x, y);
		}
		depth.pointSelected(x, y);
		release.pointSelected(x, y);
		parent.view.repaint();
	}
	
	public void loadModuleInfo(BufferedReader in) {
		if(type != EnvelopeType.ADSR) return;
		try {
			attack.setCurrentValue(new Double(in.readLine()));
			decay.setCurrentValue(new Double(in.readLine()));
			sustain.setCurrentValue(new Double(in.readLine()));
			release.setCurrentValue(new Double(in.readLine()));
			depth.setCurrentValue(new Double(in.readLine()));
		} catch (Exception e) {
			System.out.println("PlayableEnvelope.loadModuleInfo: Error reading from file");
		}
	}

	@Override
	public void saveModuleInfo(BufferedWriter out) {
		if(type != EnvelopeType.ADSR) return;
		try {
			out.write(new Double(attack.getCurrentValue()).toString());
			out.newLine();
			out.write(new Double(decay.getCurrentValue()).toString());
			out.newLine();
			out.write(new Double(sustain.getCurrentValue()).toString());
			out.newLine();
			out.write(new Double(release.getCurrentValue()).toString());
			out.newLine();
			out.write(new Double(depth.getCurrentValue()).toString());
			out.newLine();
		} catch (Exception e) {
			System.out.println("PlayableEnvelope.saveModuleInfo: Error reading from file");
		}
	}
		
}
