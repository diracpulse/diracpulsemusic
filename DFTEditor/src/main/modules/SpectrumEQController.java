package main.modules;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class SpectrumEQController implements MouseListener, MouseMotionListener, ActionListener {

	SpectrumEQEditor parent;
	Integer eqBandIndex = null;

	public SpectrumEQController(SpectrumEQEditor parent) {
		this.parent = parent;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if("Reset".equals(e.getActionCommand())) {
			parent.parent.initEQBands();
			parent.initFFTData();
		}
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		if(eqBandIndex == null) return;
		int x = arg0.getX();
		int y = arg0.getY();
		parent.parent.eqBands.get(eqBandIndex).setGain(Math.pow(2.0, parent.yToGain(y)));
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
		eqBandIndex = null;
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		int x = arg0.getX();
		int y = arg0.getY();
		for(Rectangle rect: parent.getControlAreas()) {
			if(rect.contains(x, y)) {
				eqBandIndex = parent.xToEQBandIndex(x);
				System.out.println("Selected");
				return;
			}
		}
		eqBandIndex = null;
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		if(eqBandIndex == null) return;
		int x = arg0.getX();
		int y = arg0.getY();
		parent.parent.eqBands.get(eqBandIndex).setGain(Math.pow(2.0, parent.yToGain(y)));
		parent.initFFTData();
		eqBandIndex = null;
	}

}
