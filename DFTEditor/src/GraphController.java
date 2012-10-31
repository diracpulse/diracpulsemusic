
import java.awt.Rectangle;
import java.awt.event.*;
import java.util.ArrayList;
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
	    	GraphEditor.zoomInX(x);
	    	return;
	    }
	    if(e.isShiftDown()) {
	    	if(GraphView.yView == GraphView.YView.AMPLITUDE) GraphEditor.zoomInAmplitude(y);
	    	if(GraphView.yView == GraphView.YView.FREQUENCY) GraphEditor.zoomInFrequency(y);
	    	return;
	    }
	    if(e.isAltDown()) {
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
	    	for(int loopSLA = minRangeSLA;  loopSLA < maxRangeSLA; loopSLA++) {
	    		for(int testTime = minRangeTime; testTime < maxRangeTime; testTime++) {
	    			if(!GraphEditor.timeToScaledLogAmplitudeToHarmonicIDs.containsKey(testTime)) continue;
	    			if(!GraphEditor.timeToScaledLogAmplitudeToHarmonicIDs.get(testTime).containsKey(loopSLA)) continue;
	    			for(long harmonicID : GraphEditor.timeToScaledLogAmplitudeToHarmonicIDs.get(testTime).get(loopSLA)) {
	    				if(!GraphUtils.isHarmonicVisible(GraphEditor.harmonicIDToHarmonic.get(harmonicID))) continue;
	    				if(selectionMode == SelectionMode.REMOVE) {
	    					DFTEditor.unselectHarmonic(harmonicID);
	    					System.out.println("GraphController (YView: AMPLITUDE): removed harmonic = " + harmonicID);
	    				} else {
	    					DFTEditor.selectHarmonic(harmonicID);
	    					System.out.println("GraphController (YView: AMPLITUDE): added harmonic = " + harmonicID);
		    			}
	    			}
	    		}
	    	}
	    	return;
	    }
	    if(GraphView.yView == GraphView.YView.FREQUENCY && GraphView.xView == GraphView.XView.TIME) {
	    	int minRangeTime = (int) Math.floor(GraphUtils.screenXToValue(x - 3));
	    	int maxRangeTime = (int) Math.ceil(GraphUtils.screenXToValue(x + 3));
	    	int minRangeNote = (int) Math.floor(GraphUtils.screenYToValue(y + 3));
	    	int maxRangeNote = (int) Math.ceil(GraphUtils.screenYToValue(y - 3));	    	
	    	for(int time = minRangeTime; time <= maxRangeTime; time++) {
	    		for(int note = minRangeNote; note <= maxRangeNote; note++) {
	    			if(!GraphEditor.timeToNoteToHarmonicIDs.containsKey(time)) continue;
	    			if(!GraphEditor.timeToNoteToHarmonicIDs.get(time).containsKey(note)) continue;
	    			for(long harmonicID: GraphEditor.timeToNoteToHarmonicIDs.get(time).get(note)) {
	    				if(!GraphUtils.isHarmonicVisible(GraphEditor.harmonicIDToHarmonic.get(harmonicID))) continue;
	    				if(selectionMode == SelectionMode.REMOVE) {
	    					DFTEditor.unselectHarmonic(harmonicID);
	    					System.out.println("GraphController (YView: AMPLITUDE): removed harmonic = " + harmonicID);
	    				} else {
	    					DFTEditor.selectHarmonic(harmonicID);
	    					System.out.println("GraphController (YView: AMPLITUDE): added harmonic = " + harmonicID);
		    			}
	    			}
	    		}
	    	}
	    	return;
	    }
	    if(GraphView.xView == GraphView.XView.FREQUENCY) {
	    	boolean refreshView = false;
	    	int minRangeNote = (int) Math.floor(GraphUtils.screenXToValue(x - 3));
	    	int maxRangeNote = (int) Math.ceil(GraphUtils.screenXToValue(x + 3));
	    	int minRangeSLA = (int) Math.floor(GraphUtils.screenYToValue(y + 3) * GraphEditor.logAmplitudeScale);
	    	int maxRangeSLA = (int) Math.ceil(GraphUtils.screenYToValue(y - 3) * GraphEditor.logAmplitudeScale);	
	    	System.out.println(GraphUtils.screenYToValue(y));
	    	for(int loopSLA = minRangeSLA;  loopSLA < maxRangeSLA; loopSLA++) {
	    		for(int testNote = minRangeNote; testNote < maxRangeNote; testNote++) {
	    			if(!GraphEditor.noteToScaledLogAmplitudeToHarmonicIDs.containsKey(testNote)) continue;
	    			if(!GraphEditor.noteToScaledLogAmplitudeToHarmonicIDs.get(testNote).containsKey(loopSLA)) continue;
	    			for(long harmonicID : GraphEditor.noteToScaledLogAmplitudeToHarmonicIDs.get(testNote).get(loopSLA)) {
	    				if(!GraphUtils.isHarmonicVisible(GraphEditor.harmonicIDToHarmonic.get(harmonicID))) continue;
	    				if(selectionMode == SelectionMode.REMOVE) {
	    					DFTEditor.unselectHarmonic(harmonicID);
	    					System.out.println("GraphController (YView: AMPLITUDE): removed harmonic = " + harmonicID);
	    				} else {
	    					DFTEditor.selectHarmonic(harmonicID);
	    					System.out.println("GraphController (YView: AMPLITUDE): added harmonic = " + harmonicID);
		    			}
	    			}
	    		}
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

