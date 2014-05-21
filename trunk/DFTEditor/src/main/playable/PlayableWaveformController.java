package main.playable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import main.ScaleUtils;

public class PlayableWaveformController implements MouseListener, MouseMotionListener, ActionListener {

	PlayableWaveformEditor parent;

	public PlayableWaveformController(PlayableWaveformEditor parent) {
		this.parent = parent;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
        if ("Record".equals(e.getActionCommand())) parent.record();
        if ("Stop".equals(e.getActionCommand())) parent.stop();
        if ("Arduino".equals(e.getActionCommand())) ScaleUtils.arduinoROMArray(8, 32);
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		int x = arg0.getX();
		int y = arg0.getY();
		parent.mouseDragged(x, y);
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
		parent.mousePressed(x, y);
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		int x = arg0.getX();
		int y = arg0.getY();
		parent.mouseReleased(x, y);
	}

}
