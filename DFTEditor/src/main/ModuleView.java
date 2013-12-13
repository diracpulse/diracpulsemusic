package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.swing.JComponent;

import main.Module.Connector;
import main.Module.ConnectorType;


public class ModuleView extends JComponent {
	
	private static final long serialVersionUID = -443010057744196001L;

	private ModuleEditor parent = null;
	
	ModuleView(ModuleEditor parent) {
		this.parent= parent;
	}
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		ArrayList<Module> modules = parent.getModules();
		for(Module module: modules) {
			module.draw(g);
		}
		for(Connector output: parent.connectorIDToConnector.values()) {
			if(output.getConnectorType() == ConnectorType.INPUT) continue;
			Integer connectedTo = output.getConnection();
			if(connectedTo == null) continue;
			Connector input = parent.connectorIDToConnector.get(connectedTo);
			int x0 = (int) Math.round(input.getSelectArea().getCenterX());
			int y0 = (int) Math.round(input.getSelectArea().getCenterY());
			int x1 = (int) Math.round(output.getSelectArea().getCenterX());
			int y1 = (int) Math.round(output.getSelectArea().getCenterY());			
			int red = (int) x1 % 128;
			int green = (int) y1 % 128;
			int blue = (int) (x0 + y0) % 128;
			g2.setColor(new Color(red + 128, green + 128, blue + 128, 128));
			g2.setStroke(new BasicStroke(4));
			g2.drawLine(x0, y0, x1, y1);
		}
		if(parent.selectedOutput != null) {
			int connectorID1 = parent.selectedOutput;
			int x0 = parent.controller.getMouseX();
			int y0 = parent.controller.getMouseY();
			int x1 = (int) Math.round(parent.connectorIDToConnector.get(connectorID1).getSelectArea().getCenterX());
			int y1 = (int) Math.round(parent.connectorIDToConnector.get(connectorID1).getSelectArea().getCenterY());
			int red = (int) x1 % 128;
			int green = (int) y1 % 128;
			int blue = (int) (x0 + y0) % 128;
			g2.setColor(new Color(red + 128, green + 128, blue + 128, 128));
			g2.setStroke(new BasicStroke(4));
			g2.drawLine(x0, y0, x1, y1);
		}
	}
 
}
