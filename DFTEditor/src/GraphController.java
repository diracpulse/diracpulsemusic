
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
    		int time = GraphUtils.screenXToTime(x);
    		int note = (int) Math.round(GraphUtils.screenYToValue(y));
	    	if(GraphView.yView == GraphView.YView.FREQUENCY) {
	    		for(Harmonic harmonic: GraphEditor.harmonicIDToControlPointHarmonic.values()) {
	    			if(harmonic.containsData(time, note)) {
	    				harmonic.removeData(time);
	    				GraphEditor.view.repaint();
	    				return;
	    			}
	    			if(harmonic.containsDataInterpolated(time, note)) {
	    				GraphEditor.activeControlPointHarmonicID = harmonic.getHarmonicID();
	    				GraphEditor.view.repaint();
	    				return;
	    			}	
	    		}
	    		long id = GraphEditor.activeControlPointHarmonicID;
	    		Harmonic harmonic = GraphEditor.harmonicIDToControlPointHarmonic.get(id);
	    		if(harmonic.containsTime(time)) {
	    			harmonic.removeData(time);
	    		}
	    		FDData newData = null;
	    		try {
	    			newData = new FDData(time, note, 1.0, id);
	    		} catch (Exception ex) {
	    			System.out.println("GraphController.mousePressed (ALT): Error creating FDData");
	    			return;
	    		}
	    		harmonic.addData(newData);
	    		GraphEditor.view.repaint();
	    		return;
	    	}
	    	return;
	    }
	    if(GraphView.yView == GraphView.YView.AMPLITUDE) {
	    	int time = GraphUtils.screenXToTime(x);
	    	System.out.println(GraphUtils.screenYToValue(y));
	    	int logAmplitudeTimes10 = (int) Math.round(GraphUtils.screenYToValue(y) * 10.0);
	    	for(int testTime = time - 2; testTime < time + 3; testTime++) {
	    		if(GraphEditor.timeToLogAmplitudeTimes10ToHarmonicID.containsKey(testTime)) {
	    			if(GraphEditor.timeToLogAmplitudeTimes10ToHarmonicID.get(testTime).containsKey(logAmplitudeTimes10)) {
	    				long harmonicID = GraphEditor.timeToLogAmplitudeTimes10ToHarmonicID.get(testTime).get(logAmplitudeTimes10);
		    			if(GraphEditor.selectedHarmonicIDs.contains(harmonicID)) {
		    				GraphEditor.selectedHarmonicIDs.remove(harmonicID);
		    			} else {
		    				GraphEditor.selectedHarmonicIDs.add(harmonicID);
		    			}
	    				view.repaint();
	    				return;
	    			}
	    		}
	    	}
	    }
	    if(GraphView.yView == GraphView.YView.FREQUENCY) {
	    	int time = GraphUtils.screenXToTime(x);
	    	System.out.println(GraphUtils.screenYToValue(y));
	    	int note = (int) Math.round(GraphUtils.screenYToValue(y));
	    	if(GraphEditor.timeToNoteToHarmonicID.containsKey(time)) {
	    		if(GraphEditor.timeToNoteToHarmonicID.get(time).containsKey(note)) {
	    			long harmonicID = GraphEditor.timeToNoteToHarmonicID.get(time).get(note);
	    			System.out.println("Mouse Selection " + harmonicID);
	    			if(GraphEditor.selectedHarmonicIDs.contains(harmonicID)) {
	    				GraphEditor.selectedHarmonicIDs.remove(harmonicID);
	    			} else {
	    				GraphEditor.selectedHarmonicIDs.add(harmonicID);
	    			}
	    			view.repaint();
	    			return;
	    		}
	    	}
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

