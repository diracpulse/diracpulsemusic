package main.modules;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.JPanel;

import main.ModuleEditor;
import main.modules.BasicWaveformEditor.ControlRect;

public class BasicWaveformView extends JPanel {

	private static final long serialVersionUID = -3597657941937913124L;
	private BasicWaveformEditor parent;
	public static final int xPadding = 8;
	private int fontSize = 12;
	private int yStep = fontSize + 6;
	
	BasicWaveformView(BasicWaveformEditor parent) {
		this.parent = parent;
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		int fontSize = 12;
		int yStep = fontSize + 6;
		g2.setColor(Color.WHITE);
		Font font = new Font(Font.SANS_SERIF, Font.BOLD, fontSize);
		g2.setFont(font);
		int padding = 4;
		int currentX = padding;
		int currentY = 0;
		int sliderX = 0;
      	for(ArrayList<ControlRect> controlArray: parent.controlRects.values()) {
	    	for(ControlRect controlRect: controlArray) {
	    		currentY = drawControls(g2, controlRect, currentY);
	    	}
      	}
	}
	
	public int drawControls(Graphics2D g2, ControlRect controlRect, int currentY) {
		g2.setColor(Color.GREEN);
		g2.drawString(controlRect.getIDString(), xPadding, currentY + fontSize);
		currentY += yStep;
		controlRect.coarse = new Rectangle(0, currentY + 2, getWidth(), yStep - 4);
		drawSliderBar(g2, currentY, controlRect.minValue, controlRect.range);
		int sliderX = controlRect.getXCoarse();
		drawSliderPosition(g2, sliderX, currentY);
		currentY += yStep;
		controlRect.fine = new Rectangle(0, currentY + 2, getWidth(), yStep - 4);
		drawSliderBar(g2, currentY, 0.0, controlRect.steps);
		sliderX = controlRect.getXFine();
		drawSliderPosition(g2, sliderX, currentY);
		currentY += yStep;
		return currentY;
	}
	
	public void drawSliderBar(Graphics2D g2, int y, double minValue, double steps) {
		g2.setColor(Color.LIGHT_GRAY);
		int value = (int) Math.round(minValue);
		int sliderWidth = getWidth() - xPadding * 2;
		g2.drawLine(xPadding, y + yStep / 2 , sliderWidth, y + yStep / 2);
		double divisionToPixels = (double) sliderWidth / (double) steps;
		for(double x = xPadding; Math.round(x) <= sliderWidth; x += divisionToPixels) {
			int intX = (int) Math.round(x);
			g2.drawLine(intX, y - 2, intX, y + yStep);
			g2.drawString(new Integer(value).toString(), intX + 2, y + fontSize);
			value++;
		}
	}
	
	public void drawSliderPosition(Graphics2D g2, int x, int y) {
		g2.setColor(Color.WHITE);
		g2.drawRect(x - 2, y - 2, 4, yStep - 2);
	}

}
