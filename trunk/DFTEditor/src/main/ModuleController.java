package main;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

public class ModuleController implements MouseListener, MouseMotionListener, ActionListener {
	
	private int mouseX = 0;
	private int mouseY = 0;
	
	private ModuleEditor parent = null;
	
	public ModuleController(ModuleEditor parent) {
		this.parent = parent;
	}
	
	public int getMouseX() {
		return mouseX;
	}
	
	public int getMouseY() {
		return mouseY;
	}	
	
	public void mouseClicked(MouseEvent arg0) {}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}

	public void mousePressed(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		System.out.println("ModuleController: Mouse Clicked At: " + x + " " + y);
		for(Module module: parent.getModules()) {
			if(module.pointIsInside(x, y)) {
				module.mousePressed(x, y);
				return;
			}
		}
	}
	
	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
		if(parent.selectedOutput != null) {
			parent.view.repaint();
		}
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
   public void actionPerformed(ActionEvent e) {
        if ("Play Once".equals(e.getActionCommand())) parent.play();
        if ("Play Continuous".equals(e.getActionCommand())) parent.playContinuous();
        if ("Stop".equals(e.getActionCommand())) parent.playContinuous();
        if ("DFT".equals(e.getActionCommand())) parent.dft();
        if ("Save".equals(e.getActionCommand())) parent.save();
        if ("Load".equals(e.getActionCommand())) parent.open();
    }

}
