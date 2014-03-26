package main.modules;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import main.modules.SpectrumEQEditor.SelectionMode;

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
		int x = arg0.getX();
		int y = arg0.getY();
		if(eqBandIndex == null) return;
		switch(parent.selectionMode) {
			case GAIN:
				parent.parent.eqBands.get(eqBandIndex).setGain(Math.pow(2.0, parent.yToGain(y)));
				break;
			case OVERSHOOT:
				parent.parent.eqBands.get(eqBandIndex).criticalBand.setOvershoot(Math.pow(2.0, parent.yToOvershoot(y)));
				break;
			case FILTER_EQ:
				parent.parent.eqBands.get(eqBandIndex).criticalBand.setFilterQ(Math.pow(2.0, parent.yToFilterQ(y)));
				break;
			case NONE:
				System.out.println("SpectrumEQController.mouseReleased: unexpected state");
				break;
		}
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
		if(eqBandIndex == null) return;
		parent.selectionMode = SelectionMode.NONE;
		parent.initFFTData();
		eqBandIndex = null;
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		int x = arg0.getX();
		int y = arg0.getY();
		if(arg0.isControlDown()) {
			for(Rectangle rect: parent.getFilterQControlAreas()) {
				if(rect.contains(x, y)) {
					parent.selectionMode = SelectionMode.FILTER_EQ;
					eqBandIndex = parent.xToEQBandIndex(x);
					return;
				}
			}
		}
		if(arg0.isShiftDown()) {
			for(Rectangle rect: parent.getOvershootControlAreas()) {
				if(rect.contains(x, y)) {
					parent.selectionMode = SelectionMode.OVERSHOOT;
					eqBandIndex = parent.xToEQBandIndex(x);
					return;
				}
			}
		}
		for(Rectangle rect: parent.getGainControlAreas()) {
			if(rect.contains(x, y)) {
				parent.selectionMode = SelectionMode.GAIN;
				eqBandIndex = parent.xToEQBandIndex(x);
				//System.out.println("Selected");
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
		switch(parent.selectionMode) {
			case GAIN:
				parent.parent.eqBands.get(eqBandIndex).setGain(Math.pow(2.0, parent.yToGain(y)));
				break;
			case OVERSHOOT:
				parent.parent.eqBands.get(eqBandIndex).criticalBand.setOvershoot(Math.pow(2.0, parent.yToOvershoot(y)));
				break;
			case FILTER_EQ:
				parent.parent.eqBands.get(eqBandIndex).criticalBand.setFilterQ(Math.pow(2.0, parent.yToFilterQ(y)));
				break;
			case NONE:
				System.out.println("SpectrumEQController.mouseReleased: unexpected state");
				break;
		}
		parent.selectionMode = SelectionMode.NONE;
		parent.initFFTData();
		eqBandIndex = null;
	}

}
