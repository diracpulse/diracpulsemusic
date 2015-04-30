
package main.playable;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeSet;

public class Switch implements PlayableModule {
	
	public static enum Serial {
		FM_TUNE,
		FM_FINE,
		FM_ENV,
		FM_LFO,
		FM_SH,
		AMP_ENV,
		AMP_LFO,
		AMP_SH,
		PWM_ENV,
		PWM_LFO,
		PWM_SH,
		PWM_AMT,
		SH_RATE,
		SH_NOISE,
		SH_LFO,
		LFO_FREQ,
		LFO_SHAPE,
		ENV_A,
		ENV_D,
		ENV_S,
		ENV_R;
	}

	private PlayableEditor parent;
	private int screenX;
	private int screenY;
	private Rectangle switchBounds;
	private int yPadding = PlayableEditor.moduleYPadding;
	private int switchWidth = 10;
	private int switchHeight = yPadding * 2;
	private int xPadding = 10;
	private String descriptor;
	private String upperLable;
	private String lowerLable;
	private boolean on = true;
	
	public Switch(PlayableEditor parent, int screenX, int screenY, String descriptor, String trueVal, String falseVal) {
		this.parent = parent;
		this.screenX = screenX;
		this.screenY = screenY;
		int x = screenX + xPadding;
		int y = screenY + Math.round(yPadding * 2.5f);
		int w = switchWidth;
		int l = switchHeight;
		this.switchBounds = new Rectangle(x, y, w, l);
		this.descriptor = descriptor;
		this.upperLable = trueVal;
		this.lowerLable = falseVal;
	}
	
	public Rectangle getBounds() {
		return new Rectangle(screenX, screenY, switchWidth + xPadding * 2, switchHeight + yPadding * 4);
	}
	
	public int getMaxX() {
		return screenX + switchWidth + xPadding * 2;
	}
	
	public int getMaxY() {
		return screenY + switchHeight + yPadding * 3;
	}
	
	public boolean getCurrentValue() {
		return on;
	}
	
	public void setCurrentValue(boolean on) {
		this.on = on;
	}

	public void setRandomValue() {
		this.on = Math.random() < 0.5;
	}
	
	public void pointSelected(int x, int y, PlayableController.ClickInfo info) {
		if(switchBounds.contains(x, y)) {
			if(y < switchBounds.y + switchHeight / 2) {
				on = true;
			} else {
				on = false;
			}
		}
	}
	
	public void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.BLUE);
		g2.drawRect(screenX, screenY, getMaxX() - screenX, getMaxY());
		g2.setFont(new Font("TRUETYPE_FONT", Font.BOLD, 10));
		Font font = g2.getFont();
		FontMetrics metrics = g2.getFontMetrics(font);
		int hgt = metrics.getHeight();
		int yPaddingVal = 0;
		int textYOffset = (yPadding - hgt) / 2 + yPadding / 2;
		int textXOffset = 0;
		int adv = 0;
		for(String name: descriptor.split(" ")) {
			g2.setColor(Color.WHITE);
			adv = metrics.stringWidth(name);
			textXOffset = (getMaxX() - screenX - adv) / 2;
			g2.setColor(new Color(0.5f, 0.0f, 0.0f));
			g2.fillRect(screenX + textXOffset - 4, screenY + yPaddingVal, getMaxX() - screenX - textXOffset * 2 + 8, yPadding - 4);
			g2.setColor(Color.WHITE);
			g2.drawString(name, screenX + textXOffset, screenY + textYOffset);
			textYOffset += yPadding - 4;
			yPaddingVal += yPadding - 4;
		}
		g2.setColor(Color.GRAY);
		g2.fill(switchBounds);
		g2.setColor(Color.YELLOW);
		g2.draw(switchBounds);
		g2.setColor(Color.RED);
		if(on) {
			g2.fillRect(switchBounds.x, switchBounds.y, switchWidth, switchHeight / 2);
		} else {
			g2.fillRect(switchBounds.x, switchBounds.y + switchHeight / 2, switchWidth, switchHeight / 2);
		}
		g2.setColor(Color.WHITE);
		g2.setFont(new Font("TRUETYPE_FONT", Font.PLAIN, 10));
		adv = metrics.stringWidth(upperLable);
		textXOffset = ((xPadding * 2 + switchWidth) - adv) / 2;
		g2.drawString(upperLable, screenX + textXOffset, screenY + textYOffset);
		adv = metrics.stringWidth(lowerLable);
		textYOffset += switchHeight + yPadding;
		textXOffset = ((xPadding * 2 + switchWidth) - adv) / 2;
		g2.drawString(lowerLable, screenX + textXOffset, screenY + textYOffset);
	}

	@Override
	public int getMaxScreenX() {
		return getMaxX();
	}

	@Override
	public void loadModuleInfo(BufferedReader in) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveModuleInfo(BufferedWriter out) {
		// TODO Auto-generated method stub
		
	}

}
