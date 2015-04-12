
package main.playable;

import java.awt.Graphics;

public interface PlayableModule {
	
	public static enum Type {
		LFO,
		CONTROL,
		FILTER,
		ENVELOPE,
		CONTROLBANK;
	}
	
	public int getMaxScreenX();
	
	public void draw(Graphics g);
	
	public void pointSelected(int x, int y);
	
}
