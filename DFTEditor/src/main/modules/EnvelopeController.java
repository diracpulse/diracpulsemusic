package main.modules;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import main.modules.Envelope.EnvelopePoint;


public class EnvelopeController implements MouseListener, MouseMotionListener, ActionListener {

	EnvelopeEditor parent;
	EnvelopePoint oldPoint = null;

	public EnvelopeController(EnvelopeEditor parent) {
		this.parent = parent;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if("Reset".equals(e.getActionCommand())) parent.reset();
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		if(oldPoint == null) return;
		int x = arg0.getX();
		int y = arg0.getY();
		EnvelopePoint newPoint = new EnvelopePoint(parent.xToTime(x), parent.yToAmplitude(y), oldPoint.bits);
		parent.envelope.replaceEnvelopePoint(oldPoint, newPoint);
		oldPoint = newPoint;
		parent.view.repaint();
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
		oldPoint = null;
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		int x = arg0.getX();
		int y = arg0.getY();
		if(arg0.isControlDown()) {
			for(Rectangle rect: parent.getControlAreas()) {
				if(rect.contains(x, y)) {
					int index = parent.envelope.getIndexFromEnvelopeTime(parent.getEnvelopePoint(x, y).seconds);
					if(index == parent.envelope.sustainIndex) {
						parent.envelope.sustainIndex = Integer.MAX_VALUE;
					} else {
						parent.envelope.sustainIndex = index;
					}
					parent.view.repaint();
					return;
				}
			}
		}
		for(Rectangle rect: parent.getControlAreas()) {
			if(rect.contains(x, y)) {
				oldPoint = parent.getEnvelopePoint(x, y);
				return;
			}
		}
		oldPoint = null;
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		oldPoint = null;
		parent.envelope.parent.refreshData();
	}

}
