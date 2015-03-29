
package main.playable;

import java.awt.Graphics;
import java.awt.Graphics2D;

public class PlayableControl implements PlayableModule {

	private PlayableEditor parent;
	private Slider slider;
	private int maxScreenX;
	
	public PlayableControl(PlayableEditor parent, int screenX, int screenY) {
		this.parent = parent;
		int x = screenX;
		int y = screenY;
		slider  = new Slider(Slider.Type.LINEAR, x, y, 400, 0, 1.0, 0.5, "F");
		maxScreenX = slider.getMaxX();
	}
	
	public int getMaxScreenX() {
		return maxScreenX;
	}
	
	public double getSample() {
		return slider.getCurrentValue();
	}
	
	public void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g; 
		slider.draw(g2);
	}

	public void pointSelected(int x, int y) {
		slider.pointSelected(x, y);
		parent.view.repaint();
	}
	
}
