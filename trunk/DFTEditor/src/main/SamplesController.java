package main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class SamplesController implements MouseListener, MouseMotionListener, ActionListener {

	SamplesEditor parent;

	public SamplesController(SamplesEditor parent) {
		this.parent = parent;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if("Zoom In".equals(e.getActionCommand())) parent.adjustSamplesPerPixel(0.5);
		if("Zoom Out".equals(e.getActionCommand())) parent.adjustSamplesPerPixel(2.0);
		if("Forward".equals(e.getActionCommand())) parent.adjustMinTime(0.5);
		if("Backward".equals(e.getActionCommand())) parent.adjustMinTime(-0.5);
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		int x = arg0.getX();
		int y = arg0.getY();
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
		System.out.println("Time: " + parent.xToTime(x) + " | Amplitude: " + parent.yToAmplitude(y));
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}

}
