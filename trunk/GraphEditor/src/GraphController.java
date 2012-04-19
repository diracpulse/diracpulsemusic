
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
	    if(e.isControlDown()) {
	    	GraphEditor.zoomInX(x);
	    	return;
	    }
	    if(e.isShiftDown()) {
	    	if(y < GraphEditor.upperOffset) return;
	    	if(GraphView.yView == GraphView.YView.AMPLITUDE) GraphEditor.zoomInAmplitude(y);
	    	if(GraphView.yView == GraphView.YView.FREQUENCY) GraphEditor.zoomInFrequency(y);
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
	    				GraphEditor.selectedHarmonicIDs.add(harmonicID);
	    				view.repaint();
	    				return;
	    			}
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

