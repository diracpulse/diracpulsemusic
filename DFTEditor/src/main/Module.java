package main;

public interface Module {
	
	public enum InputType {
		CONTROL,
		SAMPLES,
	}
	
	abstract void mousePressed(int x, int y);
	abstract void draw(int startX, int startY);
}
