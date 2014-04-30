package main.playable;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Button {
	
	private PlayableWaveformEditor parent;
	private Rectangle bounds;
	private Playable handler;
	
	public Button(PlayableWaveformEditor parent, Playable handler, Rectangle bounds) {
		this.parent = parent;
		this.bounds = bounds;
		this.handler = handler;
	}

	public void pointSelected(int x, int y) {
		if(bounds.contains(x, y)) {
			handler.triggered(parent.loopData, parent.loopPosition);
		}
	}
	
	public void draw(Graphics2D g2) {
		g2.setColor(Color.GREEN);
		g2.fill(bounds);
		g2.setColor(Color.WHITE);
		g2.draw(bounds);
	}
	
}
