package main.modules;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import main.Module.ModuleType;

public class BasicWaveformController implements MouseListener, MouseMotionListener, ActionListener {

	BasicWaveformEditor parent;

	public BasicWaveformController(BasicWaveformEditor parent) {
		this.parent = parent;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		for(int index = 0; index < parent.moduleEditor.getNumberOfModuleType(ModuleType.BASICWAVEFORM); index++) {
			if (new Integer(index).toString().equals(e.getActionCommand())) parent.toggleWaveformDisplayed(index);
		}
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		int x = arg0.getX();
		int y = arg0.getY();
		parent.pointSelected(x, y);
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		int x = arg0.getX();
		int y = arg0.getY();
		parent.pointSelected(x, y);
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		parent.moduleEditor.refreshData();
	}

}
