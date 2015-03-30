package main.playable;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.JPanel;

public class PlayableView extends JPanel {

	private static final long serialVersionUID = -3597657941937913124L;
	private PlayableEditor parent;
	public static final int xPadding = 8;
	private int fontSize = 12;
	private int yStep = fontSize + 6;
	
	PlayableView(PlayableEditor parent) {
		this.parent = parent;
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
    	for(PlayableModule module: PlayableEditor.nameToModule.values()) {
    		module.draw(g2);
    	}
    	parent.sequencer.draw(g2);
	}

}
