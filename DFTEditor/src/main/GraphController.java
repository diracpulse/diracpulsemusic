
import java.awt.Rectangle;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class GraphController implements MouseListener, ActionListener {
	
	private GraphView view;
	private GraphEditor parent;
	private SelectionMode selectionMode = SelectionMode.ADD;

	public enum SelectionMode {
		ADD,
		REMOVE;
	}
	
	GraphController(GraphEditor parent) {
		this.parent = parent;
	}
	
	public void setView(GraphView view) {
		this.view = view;
	}	
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){
	    int x = e.getX();
	    int y = e.getY();
	    if(e.getButton() == MouseEvent.BUTTON3) {
	    	GraphEditor.leftClickMenu(parent, x, y);
	    	return;
	    }
	    if(e.isControlDown()) {
	    	// Zoom in
	    	if(GraphView.yView == GraphView.YView.AMPLITUDE) GraphEditor.zoomInAmplitude(y, 2.0);
	    	if(GraphView.yView == GraphView.YView.FREQUENCY) GraphEditor.zoomInFrequencyY(y, 2.0);
	    	if(GraphView.xView == GraphView.XView.TIME) GraphEditor.zoomInTime(x, 2.0);
	    	if(GraphView.xView == GraphView.XView.FREQUENCY) GraphEditor.zoomInFrequencyX(x, 2.0);
	    	GraphEditor.refreshView();
	    	return;
	    }
	    if(e.isAltDown()) {
	    	if(GraphView.yView == GraphView.YView.AMPLITUDE) GraphEditor.zoomOutAmplitude(y, 2.0);
	    	if(GraphView.yView == GraphView.YView.FREQUENCY) GraphEditor.zoomOutFrequencyY(y, 2.0);
	    	if(GraphView.xView == GraphView.XView.TIME) GraphEditor.zoomOutTime(x, 2.0);
	    	if(GraphView.xView == GraphView.XView.FREQUENCY) GraphEditor.zoomOutFrequencyX(x, 2.0);
	    	GraphEditor.refreshView();
	    	return;
	    }
	    if(e.isShiftDown()) {
    		int time = (int) Math.round(GraphUtils.screenXToValue(x));
    		int note = (int) Math.round(GraphUtils.screenYToValue(y));
	    	return;
	    }
	    if(GraphView.yView == GraphView.YView.AMPLITUDE && GraphView.xView == GraphView.XView.TIME) {
	    	int minRangeTime = (int) Math.floor(GraphUtils.screenXToValue(x - 3));
	    	int maxRangeTime = (int) Math.ceil(GraphUtils.screenXToValue(x + 3));
	    	int minRangeSLA = (int) Math.floor(GraphUtils.screenYToValue(y + 3) * GraphEditor.logAmplitudeScale);
	    	int maxRangeSLA = (int) Math.ceil(GraphUtils.screenYToValue(y - 3) * GraphEditor.logAmplitudeScale);	  
	    	System.out.println(GraphUtils.screenYToValue(y));
	    	HashSet<Long> harmonicIDs = new HashSet<Long>();
	    	for(int loopSLA = minRangeSLA;  loopSLA < maxRangeSLA; loopSLA++) {
	    		for(int testTime = minRangeTime; testTime < maxRangeTime; testTime++) {
	    			if(!GraphEditor.timeToScaledLogAmplitudeToHarmonicIDs.containsKey(testTime)) continue;
	    			if(!GraphEditor.timeToScaledLogAmplitudeToHarmonicIDs.get(testTime).containsKey(loopSLA)) continue;
	    			for(long harmonicID : GraphEditor.timeToScaledLogAmplitudeToHarmonicIDs.get(testTime).get(loopSLA)) {
	    				if(!GraphUtils.isHarmonicVisible(GraphEditor.harmonicIDToHarmonic.get(harmonicID))) continue;
	    				harmonicIDs.add(harmonicID);
	    			}
	    		}
	    	}
    		if(selectionMode == SelectionMode.REMOVE) {
    			DFTEditor.unselectHarmonics(harmonicIDs);
    			System.out.println("GraphController (YView: AMPLITUDE): removed harmonic = " + harmonicIDs);
    		} else {
    			DFTEditor.selectHarmonics(harmonicIDs);
    			System.out.println("GraphController (YView: AMPLITUDE): added harmonic = " + harmonicIDs);
	    	}
	    	return;
	    }
	    if(GraphView.yView == GraphView.YView.FREQUENCY && GraphView.xView == GraphView.XView.TIME) {
	    	int minRangeTime = (int) Math.floor(GraphUtils.screenXToValue(x - 3));
	    	int maxRangeTime = (int) Math.ceil(GraphUtils.screenXToValue(x + 3));
	    	int minRangeNote = (int) Math.floor(GraphUtils.screenYToValue(y + 3));
	    	int maxRangeNote = (int) Math.ceil(GraphUtils.screenYToValue(y - 3));
	    	HashSet<Long> harmonicIDs = new HashSet<Long>();
	    	for(int time = minRangeTime; time <= maxRangeTime; time++) {
	    		for(int note = minRangeNote; note <= maxRangeNote; note++) {
	    			if(!GraphEditor.timeToNoteToHarmonicIDs.containsKey(time)) continue;
	    			if(!GraphEditor.timeToNoteToHarmonicIDs.get(time).containsKey(note)) continue;		
	    			for(long harmonicID: GraphEditor.timeToNoteToHarmonicIDs.get(time).get(note)) {
	    				if(!GraphUtils.isHarmonicVisible(GraphEditor.harmonicIDToHarmonic.get(harmonicID))) continue;
	    				harmonicIDs.add(harmonicID);
	    			}
	    		}
	    	}
	    	if(selectionMode == SelectionMode.REMOVE) {
	    		DFTEditor.unselectHarmonics(harmonicIDs);
	    		System.out.println("GraphController (YView: AMPLITUDE): removed harmonic = " + harmonicIDs);
	    	} else {
	    		DFTEditor.selectHarmonics(harmonicIDs);
	    		System.out.println("GraphController (YView: AMPLITUDE): added harmonic = " + harmonicIDs);
		    }
	    	return;
	    }
	    if(GraphView.xView == GraphView.XView.FREQUENCY) {
	    	int minRangeNote = (int) Math.floor(GraphUtils.screenXToValue(x - 3));
	    	int maxRangeNote = (int) Math.ceil(GraphUtils.screenXToValue(x + 3));
	    	int minRangeSLA = (int) Math.floor(GraphUtils.screenYToValue(y + 3) * GraphEditor.logAmplitudeScale);
	    	int maxRangeSLA = (int) Math.ceil(GraphUtils.screenYToValue(y - 3) * GraphEditor.logAmplitudeScale);	
	    	System.out.println(GraphUtils.screenYToValue(y));
	    	HashSet<Long> harmonicIDs = new HashSet<Long>();
	    	for(int loopSLA = minRangeSLA;  loopSLA < maxRangeSLA; loopSLA++) {
	    		for(int testNote = minRangeNote; testNote < maxRangeNote; testNote++) {
	    			if(!GraphEditor.noteToScaledLogAmplitudeToHarmonicIDs.containsKey(testNote)) continue;
	    			if(!GraphEditor.noteToScaledLogAmplitudeToHarmonicIDs.get(testNote).containsKey(loopSLA)) continue;
	    			for(long harmonicID : GraphEditor.noteToScaledLogAmplitudeToHarmonicIDs.get(testNote).get(loopSLA)) {
	    				if(!GraphUtils.isHarmonicVisible(GraphEditor.harmonicIDToHarmonic.get(harmonicID))) continue;
	    				harmonicIDs.add(harmonicID);
	    			}
	    		}
	    	}
	    	if(selectionMode == SelectionMode.REMOVE) {
	    		DFTEditor.unselectHarmonics(harmonicIDs);
	    		System.out.println("GraphController (YView: AMPLITUDE): removed harmonic = " + harmonicIDs);
	    	} else {
	    		DFTEditor.selectHarmonics(harmonicIDs);
	    		System.out.println("GraphController (YView: AMPLITUDE): added harmonic = " + harmonicIDs);
	    	}
	    	return;
	    }
	}
	
	public void mouseClicked(MouseEvent e){

	}
	
	public static void getFileData(int mouseX, int mouseY) {

	}
	
    public void actionPerformed(ActionEvent e) {
 
    }

    private void adjustY(int deltaY) {
 
    }
    
    private void adjustX(int deltaX) {
 
    }
    
}

