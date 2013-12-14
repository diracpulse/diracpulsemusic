package main;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class SequencerView extends JPanel {

	private Sequencer parent;
	
	SequencerView(Sequencer parent) {
		this.parent = parent;
	}
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
	}
	
}
