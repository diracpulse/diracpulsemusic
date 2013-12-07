package main;

import java.awt.Graphics;

public interface Module {
	
	public enum InputType {
		CONTROL,
		SAMPLES,
	}
	
	abstract void mousePressed(int x, int y);
	abstract void draw(Graphics g, int startX, int startY);
	abstract int getWidth();
	abstract int getHeight();
}
