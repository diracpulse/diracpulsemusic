package main;

import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.JComponent;

import main.ModuleEditor.ModuleScreenInfo;


public class ModuleView extends JComponent {
	
	private static final long serialVersionUID = -443010057744196001L;

	private ArrayList<ModuleScreenInfo> moduleScreenInfoArray = null;
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		for(ModuleScreenInfo moduleScreenInfo: moduleScreenInfoArray) {
			moduleScreenInfo.module.draw(g, moduleScreenInfo.dimensions.x, moduleScreenInfo.dimensions.y);
		}
    }
	
	public void updateModuleScreenInfo(ArrayList<ModuleScreenInfo> moduleScreenInfoArray) {
		this.moduleScreenInfoArray = moduleScreenInfoArray;
	}

	
}
