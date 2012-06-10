
import java.awt.Rectangle;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class TrackController implements MouseListener, ActionListener {
	
	private TrackView view;
	private TrackEditor parent;

	TrackController(TrackEditor parent) {
		this.parent = parent;
	}
	
	public void setView(TrackView view) {
		this.view = view;
	}	
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){
	    int x = e.getX();
	    int y = e.getY();
	    if(x < view.leftPanelWidth) {
	    	// Select a loop from list of graphical list of loop files
	    	int loopFileIndex = y / view.leftYStep;
	    	if(loopFileIndex < TrackEditor.loopFiles.length) {
	    		TrackEditor.openFileInTrackEditor(TrackEditor.loopFiles[loopFileIndex].getAbsolutePath());
	    		view.repaint();
	    	}
	    } else {
	    	// Select a beat from loop or track
	    	
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

