package main;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JOptionPane;

public class SequencerController implements MouseListener, MouseMotionListener, ActionListener {

	Sequencer parent;
	Integer startNote = null;
	Integer startDivision = null;
	Integer currentNote = null;
	Integer currentDivision = null;
	Integer currentModuleIndex = null;
	Boolean percussion = null;
	Boolean erase = null;
	public double[] savedFreqRatios = null;
	
	public SequencerController(Sequencer parent) {
		this.parent = parent;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
        if ("Play".equals(e.getActionCommand())) parent.play();
        if ("DFT".equals(e.getActionCommand())) parent.dft();
        if ("Save".equals(e.getActionCommand())) return; //parent.save();
        if ("Load".equals(e.getActionCommand())) return; //parent.open();
        if ("Get Module".equals(e.getActionCommand())) {
        	ArrayList<String> names = new ArrayList<String>();
        	int index = 0;
        	int endIndex = parent.moduleInfo.size() - Sequencer.numPercussion;
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
        if("Scale".equals(e.getActionCommand())) parent.scale();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if(startDivision == null) return;
		int division = parent.view.getTimeInDivisions(e.getX());
		if(division < 0) return;
		if(!percussion) {
			currentNote = parent.view.getNote(e.getY());
			if(currentNote < 0) currentNote = 0;
		}
		if(division < currentDivision) {
			for(int divisionIndex = division; divisionIndex <= currentDivision; divisionIndex++) {
				for(int time = divisionIndex * Sequencer.pixelsPerDivision; time < (divisionIndex + 1) * Sequencer.pixelsPerDivision; time++) {
					parent.freqRatiosAtTimeInPixels.get(currentModuleIndex)[time] = savedFreqRatios[time];
				}
			}
			currentDivision = division;
			return;
		}
		currentDivision = division;
		if(currentDivision < startDivision) return;
		int minDivisionIndex = startDivision;
		int maxDivisionIndex = currentDivision;
		double dNote = (double) startNote;
		double deltaNote = (1.0 * currentNote - startNote) / (currentDivision - startDivision);
		for(int divisionIndex = minDivisionIndex; divisionIndex <= maxDivisionIndex; divisionIndex++) {
			int note = (int) Math.floor(dNote);
			double freqRatio = Math.pow(2.0, 1.0 * note / Sequencer.noteBase);
			for(int time = divisionIndex * Sequencer.pixelsPerDivision; time < (divisionIndex + 1) * Sequencer.pixelsPerDivision; time++) {
				if(erase) {
					parent.freqRatiosAtTimeInPixels.get(currentModuleIndex)[time] = -1.0;
				} else {
					parent.freqRatiosAtTimeInPixels.get(currentModuleIndex)[time] = freqRatio;
				}
			}
			dNote += deltaNote;
		}
		parent.view.repaint();
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
		if(startDivision == null) {
			int division = parent.view.getTimeInDivisions(e.getX());
			if(division < 0) return;
			int note = parent.view.getNote(e.getY());
			int time = division * Sequencer.pixelsPerDivision;
			currentModuleIndex = parent.currentModuleIndex;
			percussion = false;
			if(note < 0) {
				if(Math.abs(note) <= Sequencer.numPercussion) {
					currentModuleIndex = parent.moduleInfo.size() - Math.abs(note);
					percussion = true;
				} else {
					return;
				}
			}
			double freqRatio = Math.pow(2.0, 1.0 * note / Sequencer.noteBase);
			if((float) parent.freqRatiosAtTimeInPixels.get(currentModuleIndex)[time] == (float) freqRatio) {
				erase = true;
			} else {
				erase = false;
			}
			double[] freqRatios = parent.freqRatiosAtTimeInPixels.get(currentModuleIndex);
			savedFreqRatios = new double[freqRatios.length];
			for(int index = 0; index < savedFreqRatios.length; index++) {
				savedFreqRatios[index] = freqRatios[index];
			}
			startDivision = division;
			startNote = note;
			currentDivision = division;
			currentNote = note;
			for(time = division * Sequencer.pixelsPerDivision; time < (division + 1) * Sequencer.pixelsPerDivision; time++) {
				parent.freqRatiosAtTimeInPixels.get(currentModuleIndex)[time] = freqRatio;
			}
			parent.view.repaint();
		} else {
			startDivision = null;
			startNote = null;
			currentDivision = null;
			currentNote = null;
			currentModuleIndex = null;
			percussion = null;
			erase = null;
			parent.view.repaint();
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}

}
