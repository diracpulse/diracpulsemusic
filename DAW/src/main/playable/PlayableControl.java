
package main.playable;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class PlayableControl implements PlayableModule {

	private PlayableEditor parent;
	private Slider slider;
	private Slider fineSlider = null;
	private int maxScreenX;
	String moduleName;
	private int screenX;
	private int screenY;
	private int yPadding = PlayableEditor.moduleYPadding;
	
	public PlayableControl(PlayableEditor parent, int screenX, int screenY, String[] descriptors) {
		this.parent = parent;
		int x = screenX;
		int y = screenY + PlayableEditor.moduleYPadding;
		this.screenX = x;
		this.screenY = screenY;
		this.moduleName = descriptors[0];
		slider  = new Slider(Slider.Type.LINEAR, x, y, 400, 0, 1.0, 0.5, new String[] {" ", descriptors[1], descriptors[2]});
		maxScreenX = slider.getMaxX();
	}
	
	public PlayableControl(PlayableEditor parent, int screenX, int screenY, double minVal, double maxVal, double initialVal, String moduleName) {
		this.parent = parent;
		int x = screenX;
		int y = screenY + PlayableEditor.moduleYPadding;
		this.screenX = x;
		this.screenY = screenY;
		this.moduleName = moduleName;
		slider  = new Slider(Slider.Type.LOGARITHMIC, x, y, 400, minVal, maxVal, initialVal, new String[] {" ", new Float(maxVal).toString(), new Float(minVal).toString()});
		x = slider.getMaxX();
		fineSlider = new Slider(Slider.Type.LINEAR, x, y, 400, 1.0, 2.0, 1.0, new String[] {" ", " ", " "});
		maxScreenX = fineSlider.getMaxX();
	}
	
	public int getMaxScreenX() {
		return maxScreenX;
	}
	
	public synchronized double getSample() {
		if(fineSlider != null) {
			return slider.getCurrentValue() * fineSlider.getCurrentValue();
		}
		return slider.getCurrentValue();
	}
	
	public void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setFont(new Font("TRUETYPE_FONT", Font.BOLD, 12));
		Font font = g2.getFont();
		FontMetrics metrics = g2.getFontMetrics(font);
		int hgt = metrics.getHeight();
		int yPaddingVal = 0;
		int textYOffset = (yPadding - hgt) / 2 + yPadding / 2;
		for(String name: moduleName.split(" ")) {
			g2.setColor(Color.WHITE);
			int adv = metrics.stringWidth(name);
			int textXOffset = (maxScreenX - screenX - adv) / 2;
			g2.setColor(new Color(0.5f, 0.0f, 0.0f));
			g2.fillRect(screenX + textXOffset - 4, screenY + yPaddingVal, maxScreenX - screenX - textXOffset * 2 + 8, yPadding - 4);
			g2.setColor(Color.WHITE);
			g2.drawString(name, screenX + textXOffset, screenY + textYOffset);
			textYOffset += yPadding - 4;
			yPaddingVal += yPadding - 4;
		}
		g2.setColor(Color.BLUE);
		g2.drawRect(screenX, screenY, maxScreenX - screenX, slider.getMaxY());
		slider.draw(g2);
		if(fineSlider != null) fineSlider.draw(g2);
	}

	public void pointSelected(int x, int y) {
		slider.pointSelected(x, y);
		if(fineSlider != null) fineSlider.pointSelected(x, y);
		parent.view.repaint();
	}
	
}
