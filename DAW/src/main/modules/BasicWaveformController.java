package main.modules;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import main.Module.ModuleType;
import main.modules.BasicWaveformEditor.ControlType;

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
		if("Reset".equals(e.getActionCommand())) parent.reset();
		if("Round".equals(e.getActionCommand())) parent.round();
		if("Random".equals(e.getActionCommand())) parent.randomize();
		if("Random .5".equals(e.getActionCommand())) parent.randomize(0.5);
		if("Random Amp".equals(e.getActionCommand())) parent.randomize(ControlType.AMPLITUDE);
		if("Random Freq".equals(e.getActionCommand())) parent.randomize(ControlType.FREQUENCY);
		if("Random Amp .5".equals(e.getActionCommand())) parent.randomize(ControlType.AMPLITUDE, 0.5);
		if("Random Freq .5".equals(e.getActionCommand())) parent.randomize(ControlType.FREQUENCY, 0.5);
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
