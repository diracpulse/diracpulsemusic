
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
	    if(GraphView.yView == GraphView.YView.AMPLITUDE) {
	    	boolean refreshView = false;
	    	int time = (int) Math.round(GraphUtils.screenXToValue(x));
	    	System.out.println(GraphUtils.screenYToValue(y));
	    	int logAmplitudeTimes10 = (int) Math.round(GraphUtils.screenYToValue(y) * 10.0);
	    	for(int testTime = time - 2; testTime < time + 3; testTime++) {
	    		if(GraphEditor.timeToLogAmplitudeTimes10ToHarmonicID.containsKey(testTime)) {
	    			if(GraphEditor.timeToLogAmplitudeTimes10ToHarmonicID.get(testTime).containsKey(logAmplitudeTimes10)) {
	    				long harmonicID = GraphEditor.timeToLogAmplitudeTimes10ToHarmonicID.get(testTime).get(logAmplitudeTimes10);
		    			if(GraphEditor.selectedHarmonicIDs.contains(harmonicID)) {
		    				if(selectionMode == SelectionMode.REMOVE) {
		    					GraphEditor.selectedHarmonicIDs.remove(harmonicID);
		    					System.out.println("GraphController (YView: AMPLITUDE): removed harmonic = " + harmonicID);
		    					refreshView = true;
		    				}
		    			} else {
		    				if(selectionMode == SelectionMode.ADD) {
		    					GraphEditor.selectedHarmonicIDs.add(harmonicID);
		    					System.out.println("GraphController (YView: AMPLITUDE): added harmonic = " + harmonicID);
		    					refreshView = true;
		    				}
		    			}
	    			}
	    		}
	    	}
	    	if(refreshView) GraphEditor.refreshView();
	    	return;
	    }
	    if(GraphView.yView == GraphView.YView.FREQUENCY) {
	    	boolean refreshView = false;
	    	int minRangeTime = (int) Math.floor(GraphUtils.screenXToValue(x - 3));
	    	int maxRangeTime = (int) Math.ceil(GraphUtils.screenXToValue(x + 3));
	    	int minRangeNote = (int) Math.floor(GraphUtils.screenYToValue(y + 3));
	    	int maxRangeNote = (int) Math.ceil(GraphUtils.screenYToValue(y - 3));	    	
	    	for(int time = minRangeTime; time <= maxRangeTime; time++) {
	    		for(int note = minRangeNote; note <= maxRangeNote; note++) {
	    			if(!GraphEditor.timeToNoteToHarmonicID.containsKey(time)) continue;
	    			if(!GraphEditor.timeToNoteToHarmonicID.get(time).containsKey(note)) continue;
	    			long harmonicID = GraphEditor.timeToNoteToHarmonicID.get(time).get(note);
	    			if(GraphEditor.selectedHarmonicIDs.contains(harmonicID)) {
	    				if(selectionMode == SelectionMode.REMOVE) {
	    					GraphEditor.selectedHarmonicIDs.remove(harmonicID);
	    					System.out.println("GraphController (YView: FREQUENCY): removed harmonic = " + harmonicID);
	    					refreshView = true;
	    				}
	    			} else {
	    				if(selectionMode == SelectionMode.ADD) {
	    					GraphEditor.selectedHarmonicIDs.add(harmonicID);
	    					System.out.println("GraphController (YView: FREQUENCY): added harmonic = " + harmonicID);
	    					refreshView = true;
	    				}
	    			}
	    		}
	    	}
	    	if(refreshView) GraphEditor.refreshView();
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

