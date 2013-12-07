package main;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import main.ModuleEditor.ModuleScreenInfo;


public class ModuleController implements MouseListener {
	
	private ArrayList<ModuleScreenInfo> moduleScreenInfoArray = null;

	public void mouseClicked(MouseEvent arg0) {}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}

	public void mousePressed(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		System.out.println("ModuleController: Mouse Clicked At: " + x + " " + y);
		for(ModuleScreenInfo moduleScreenInfo: moduleScreenInfoArray) {
			if(moduleScreenInfo.pointIsInside(x, y)) {
				moduleScreenInfo.module.mousePressed(x, y);
				return;
			}
		}
	}
	
	public void updateModuleScreenInfo(ArrayList<ModuleScreenInfo> moduleScreenInfoArray) {
		this.moduleScreenInfoArray = moduleScreenInfoArray;
	}

}
