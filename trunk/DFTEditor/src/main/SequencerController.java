package main;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JList;
import javax.swing.JOptionPane;

public class SequencerController implements MouseListener, MouseMotionListener, ActionListener {

	Sequencer parent;
	
	public SequencerController(Sequencer parent) {
		this.parent = parent;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
        if ("Play".equals(e.getActionCommand())) return; //parent.play();
        if ("DFT".equals(e.getActionCommand())) return; // parent.dft();
        if ("Save".equals(e.getActionCommand())) return; //parent.save();
        if ("Load".equals(e.getActionCommand())) return; //parent.open();
        if ("Get Module".equals(e.getActionCommand())) {
        	ArrayList<String> names = new ArrayList<String>();
        	int index = 0;
        	int endIndex = parent.moduleInfo.size() - parent.numPercussion;
        	for(MultiWindow.ModuleEditorInfo info: parent.moduleInfo) {
        		names.add(info.getName());
        		index++;
        		if(index == endIndex) break;
        	}
        	Integer result = JOptionPane.showOptionDialog(parent, "Module Select", "Select A Module", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, names.toArray(), names.get(0));
        	if(result == null) return;
        	parent.currentModuleIndex = result;
        	return;
        }
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int division = parent.view.getTimeInDivisions(e.getX());
		if(division < 0) return;
		int note = parent.view.getNote(e.getY());
		int moduleIndex = parent.currentModuleIndex;
		if(note < 0) {
			if(Math.abs(note) <= Sequencer.numPercussion) {
				moduleIndex = parent.moduleInfo.size() - Math.abs(note);
			} else {
				return;
			}
		}
		double freqRatio = Math.pow(2.0, 1.0 * note / Sequencer.noteBase);
		for(int time = division * Sequencer.pixelsPerDivision; time < (division + 1) * Sequencer.pixelsPerDivision; time++) {
			double currentFreqRatio = parent.freqRatiosAtTime.get(moduleIndex)[time];
			if(currentFreqRatio >= 0.0) {
				parent.freqRatiosAtTime.get(moduleIndex)[time] = -1.0;
			} else {
				parent.freqRatiosAtTime.get(moduleIndex)[time] = freqRatio;
			}
			
		}
		parent.view.repaint();
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		int division = parent.view.getTimeInDivisions(e.getX());
		if(division < 0) return;
		int note = parent.view.getNote(e.getY());
		int moduleIndex = parent.currentModuleIndex;
		if(note < 0) {
			if(Math.abs(note) <= Sequencer.numPercussion) {
				moduleIndex = parent.moduleInfo.size() - Math.abs(note);
			} else {
				return;
			}
		}
		double freqRatio = Math.pow(2.0, 1.0 * note / Sequencer.noteBase);
		for(int time = division * Sequencer.pixelsPerDivision; time < (division + 1) * Sequencer.pixelsPerDivision; time++) {
			double currentFreqRatio = parent.freqRatiosAtTime.get(moduleIndex)[time];
			if(currentFreqRatio >= 0.0) {
				parent.freqRatiosAtTime.get(moduleIndex)[time] = -1.0;
			} else {
				parent.freqRatiosAtTime.get(moduleIndex)[time] = freqRatio;
			}
			
		}
		parent.view.repaint();
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	


}
