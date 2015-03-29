
package main.playable;

import java.awt.Graphics;

public interface PlayableModule {
	
	public void draw(Graphics g);
	
	public void pointSelected(int x, int y);
	
}
