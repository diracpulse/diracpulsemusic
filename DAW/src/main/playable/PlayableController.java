package main.playable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import main.ScaleUtils;

public class PlayableController implements MouseListener, MouseMotionListener, ActionListener {

	PlayableEditor parent;
	
	public enum ClickInfo {
		NONE,
		ALT_DOWN,
		SHIFT_DOWN,
		CTRL_DOWN,
		DOUBLE_CLICK;
	}

	public PlayableController(PlayableEditor parent) {
		this.parent = parent;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if ("Open".equals(e.getActionCommand())) parent.open();
		if ("Save".equals(e.getActionCommand())) parent.save();
		if ("Play".equals(e.getActionCommand())) parent.play();
		if ("Pause".equals(e.getActionCommand())) parent.pause();
		if ("Mute".equals(e.getActionCommand())) parent.mute();
		if ("Stop".equals(e.getActionCommand())) parent.stop();
        if ("Random Sequence".equals(e.getActionCommand())) parent.randomSequence();
        if ("Random Patch".equals(e.getActionCommand())) parent.randomPatch();
        if ("Bass".equals(e.getActionCommand())) parent.sequencer.bass();
        if ("Treble".equals(e.getActionCommand())) parent.sequencer.treble();
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
		int x = arg0.getX();
		int y = arg0.getY();
		if(arg0.getClickCount() == 2) {
			parent.mouseClicked(x, y, ClickInfo.DOUBLE_CLICK);
		}
		if(arg0.isAltDown()) {
			parent.mouseClicked(x, y, ClickInfo.ALT_DOWN);
			return;
		}
		if(arg0.isControlDown()) {
			parent.mouseClicked(x, y, ClickInfo.CTRL_DOWN);
			return;
		}
		if(arg0.isShiftDown()) {
			parent.mouseClicked(x, y, ClickInfo.SHIFT_DOWN);
			return;
		}
		parent.mouseClicked(x, y, ClickInfo.NONE);
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
		if(arg0.isAltDown()) parent.mousePressed(x, y, ClickInfo.ALT_DOWN);
		if(arg0.isControlDown()) parent.mousePressed(x, y, ClickInfo.CTRL_DOWN);
		if(arg0.isShiftDown()) parent.mousePressed(x, y, ClickInfo.SHIFT_DOWN);
		parent.mousePressed(x, y, ClickInfo.NONE);
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		int x = arg0.getX();
		int y = arg0.getY();
		parent.mouseReleased(x, y);
	}

}
