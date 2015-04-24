
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
		R0,
		R,
		AR,
		ASR,
		ADSR,
		INVISIBLE;
	}
	
	public PlayableEnvelope(PlayableEditor parent, EnvelopeType type, int screenX, int screenY, String moduleName) {
		this.parent = parent;
		this.type = type;
		this.moduleName = moduleName;
		int x = screenX;
		int y = screenY + PlayableEditor.moduleYPadding;
		this.screenX = x;
		this.screenY = screenY;
		double defaultAD = 0.001;
		double defaultR = 0.5;
		double maxValR = 8.0;
		double minValADR = 0.001;
		double maxValAD = 1.0;
		switch(type) {
		case ADSR:
			attack = new Slider(Slider.Type.LOGARITHMIC, x, y, minValADR, maxValAD, defaultAD, new String[]{"A", "s", " "});
			x = attack.getMaxX();
			decay = new Slider(Slider.Type.LOGARITHMIC, x, y, minValADR, maxValAD, defaultAD, new String[]{"D", "s", " "});
			x = decay.getMaxX();
			sustain = new Slider(Slider.Type.LOGARITHMIC_ZERO, x, y, 1.0 / 16.0, 1.0, Math.sqrt(2.0) / 2.0, new String[] {"S", "", " "});
			x = sustain.getMaxX();
			release = new Slider(Slider.Type.LOGARITHMIC, x, y, minValADR, maxValR, 0.1, new String[]{"R", "s", " "});
			x = release.getMaxX();
			depth = new Slider(Slider.Type.LOGARITHMIC_ZERO, x, y, 1.0 / 16.0, 1.0, Math.sqrt(2.0) / 2.0, new String[]{"AMT", "", ""});
			maxScreenX = depth.getMaxX();
			return;
		case ASR:
			attack = new Slider(Slider.Type.LOGARITHMIC, x, y, minValADR, maxValAD, defaultAD, new String[]{"A", "s", " "});
			x = attack.getMaxX();
			release = new Slider(Slider.Type.LOGARITHMIC, x, y, minValADR, maxValR, defaultR, new String[]{"R", "s", " "});
			x = release.getMaxX();
			depth = new Slider(Slider.Type.LOGARITHMIC_ZERO, x, y, 1.0 / 16.0, 1.0, Math.sqrt(2.0) / 2.0, new String[]{"AMT", "", ""});
			maxScreenX = depth.getMaxX();
			return;
		case AR:
			attack = new Slider(Slider.Type.LOGARITHMIC, x, y, minValADR, maxValAD, defaultAD, new String[]{"A", "s", " "});
			x = attack.getMaxX();
			release = new Slider(Slider.Type.LOGARITHMIC, x, y, minValADR, maxValR, defaultR, new String[]{"R", "s", " "});
			x = release.getMaxX();
			depth = new Slider(Slider.Type.LOGARITHMIC_ZERO, x, y, 1.0 / 16.0, 1.0, Math.sqrt(2.0) / 2.0, new String[]{"AMT",  "", ""});
			x = depth.getMaxX();
			maxScreenX = depth.getMaxX();
			return;
		case R:
			release = new Slider(Slider.Type.LOGARITHMIC, x, y, minValADR, maxValR, defaultR, new String[]{"R", "s", " "});
			x = release.getMaxX();
			depth = new Slider(Slider.Type.LOGARITHMIC_ZERO, x, y, 1.0 / 16.0, 1.0, Math.sqrt(2.0) / 2.0, new String[]{"AMT",  "", ""});
			x = depth.getMaxX();
			maxScreenX = depth.getMaxX();
			return;
		case R0:
			release = new Slider(Slider.Type.LOGARITHMIC, x, y, 0.05, maxValR, defaultR, new String[]{"R", "s", " "});
			maxScreenX = release.getMaxX();
			return;
		case INVISIBLE:
			return;
		}
	}
	
	public PlayableEnvelope(EnvelopeType type) {
		this.type = type;
	}
	
	public int getMaxScreenX() {
		return maxScreenX;
	}
	
	public synchronized void noteOn(long currentTimeInSamples) {
		off = false;
		startTimeInSamples = currentTimeInSamples;
		saveDecayValue = 0.0;
		if(this.type == EnvelopeType.INVISIBLE) return;
		if(type != EnvelopeType.R && type != EnvelopeType.R0) attackInSamples = (int) Math.round(attack.getCurrentValue() * AudioFetcher.sampleRate);
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
		case R0:
			return getSampleR0(absoluteTimeInSamples, absolute);
		case R:
			return getSampleR(absoluteTimeInSamples, absolute);
		case AR:
			return getSampleAR(absoluteTimeInSamples, absolute);
		case ASR:
			return getSampleASR(absoluteTimeInSamples, absolute);
		case ADSR:
			return getSampleADSR(absoluteTimeInSamples, absolute);
		}
		return 0.0;
	}

	private double getSampleR0(long absoluteTimeInSamples, boolean absolute) {
		long currentTimeInSamples = absoluteTimeInSamples - startTimeInSamples;
		if(currentTimeInSamples < 44.1) return currentTimeInSamples / 44.1;
		return Math.exp(-1.0 * tau * (currentTimeInSamples - 44.1) / (double) releaseInSamples);
	} 
	
	public double getSampleR0Accent(long absoluteTimeInSamples, double duration) {
		if(duration < 0.05) duration = 0.05;
		double accentTime = duration * AudioFetcher.sampleRate;
		long currentTimeInSamples = absoluteTimeInSamples - startTimeInSamples;
		if(currentTimeInSamples < accentTime) return 1.0 - currentTimeInSamples / accentTime;
		//if(currentTimeInSamples < accentTime * 2.0) return 2.0 - currentTimeInSamples / accentTime;
		return 0.0;
	} 

	private double getSampleR(long absoluteTimeInSamples, boolean absolute) {
		double depthVal = depth.getCurrentValue();
		long currentTimeInSamples = absoluteTimeInSamples - startTimeInSamples;
		double returnVal = Math.exp(-1.0 * tau * currentTimeInSamples / (double) releaseInSamples) * depthVal;
		if(absolute) return returnVal;
		return returnVal + (1.0 - depthVal);
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
	
	public void randomRelease() {
		release.setRandomValue();
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
		if(type != EnvelopeType.R && type != EnvelopeType.R0) {
			g2.drawRect(screenX, screenY, maxScreenX - screenX, attack.getMaxY());
		} else {
			g2.drawRect(screenX, screenY, maxScreenX - screenX, release.getMaxY());
		}
		if(type != EnvelopeType.R) {
			if(type != EnvelopeType.R0) attack.draw(g2);
		}
		if(type == EnvelopeType.ADSR) {
			sustain.draw(g2);
			decay.draw(g2);
		}
		if(type != EnvelopeType.R0) depth.draw(g2);
		release.draw(g2);
		parent.view.repaint();
	}

	public void pointSelected(int x, int y, PlayableController.ClickInfo info) {
		if(type != EnvelopeType.R) {
			if(type != EnvelopeType.R0) attack.pointSelected(x, y, info);
		}
		if(type == EnvelopeType.ADSR) {
			decay.pointSelected(x, y, info);
			sustain.pointSelected(x, y, info);
		}
		if(type != EnvelopeType.R0) depth.pointSelected(x, y, info);
		release.pointSelected(x, y, info);
		parent.view.repaint();
	}
	
	public void loadModuleInfo(BufferedReader in) {
		try {
			if(type == EnvelopeType.R0) {
				release.setCurrentValue(new Double(in.readLine()));
				return;
			}
			if(type != EnvelopeType.R) attack.setCurrentValue(new Double(in.readLine()));
			if(type == EnvelopeType.ADSR) {
				decay.setCurrentValue(new Double(in.readLine()));
				sustain.setCurrentValue(new Double(in.readLine()));
			}
			release.setCurrentValue(new Double(in.readLine()));
			depth.setCurrentValue(new Double(in.readLine()));
		} catch (Exception e) {
			System.out.println("PlayableEnvelope.loadModuleInfo: Error reading from file");
		}
	}

	@Override
	public void saveModuleInfo(BufferedWriter out) {
		try {
			if(type == EnvelopeType.R0) {
				out.write(new Double(release.getCurrentValue()).toString());
				out.newLine();
				return;
			}
			if(type != EnvelopeType.R) {
				out.write(new Double(attack.getCurrentValue()).toString());
				out.newLine();
			}
			if(type == EnvelopeType.ADSR) {
				out.write(new Double(decay.getCurrentValue()).toString());
				out.newLine();
				out.write(new Double(sustain.getCurrentValue()).toString());
				out.newLine();
			}
			out.write(new Double(release.getCurrentValue()).toString());
			out.newLine();
			out.write(new Double(depth.getCurrentValue()).toString());
			out.newLine();
		} catch (Exception e) {
			System.out.println("PlayableEnvelope.saveModuleInfo: Error reading from file");
		}
	}
		
}
