
package main.playable;

import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.BufferedWriter;

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
	
	public void loadModuleInfo(BufferedReader in);

	public void saveModuleInfo(BufferedWriter out);

	
}
