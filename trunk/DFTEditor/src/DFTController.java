
import java.awt.Rectangle;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.JOptionPane;

public class DFTController implements MouseListener, ActionListener {
	
	private DFTView view;
	private DFTEditor parent;
	// This contains selected start points for harmonics
	// Used to create harmonics once an end point is selected
	private TreeMap<Integer, Integer> harmonicStartFreqToTime = null;
	private DFTModel.TFA harmonicStart = null;
	
	private class TFPair {
		
		private int time; // time/5ms
		private int note; // log2(freqInHz)*31
		
		TFPair(int time, int note) {
			this.time = time;
			this.note = note;
		}
		
	}
	
	DFTController(DFTEditor parent) {
		this.parent = parent;
	}
	
	public void setView(DFTView view) {
		this.view = view;
	}
	
	
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){
	    int x = e.getX();
	    int y = e.getY();
	    DFTModel.TFA selected = getFileData(x, y);
	    if(selected != null) {
	    	System.out.println(selected);
	    } else {
	    	System.out.println(x + " " + y);
	    }
	}
	
	
	public void mouseClicked(MouseEvent e){
		/*
	    int xPos = e.getX();
	    int yPos = e.getY();
	    int freqIndex;
	    int timeIndex;
	    if ((xPos > leftOffset) && (yPos > upperOffset)) { 
	    	timeIndex = (xPos - leftOffset) / xStep + leftX;
	    	freqIndex = (yPos - upperOffset) / yStep + upperY;
	    	if((freqIndex >= dataYDim) || (timeIndex >= dataXDim)) {
		    	// OUT OF BOUNDS
		    	System.out.println("OUT OF BOUNDS");
		    	return;
	    	}
	    	// DATA CELL SELECTED
	    	System.out.println("DATA: Freq = " + ((Float) freqs.get(freqIndex)) 
	    						+ " | Time: " + (timeIndex * timeStep) 
	    						+ " | Amplitude: " + data[timeIndex][freqIndex]);
	    	return;
    	}
    	if (xPos <= leftOffset) {
	    	if (yPos <= upperOffset) {
		    	// UPPER LEFT CORNER SELECTED
	    		System.out.println("UPPER LEFT CORNER");
	    		return;
    		}
    		// LEFT FREQUENCY SELECTED
    		freqIndex = (yPos - upperOffset) / yStep + upperY;
    		System.out.println("FREQUENCY: " + ((Float) freqs.get(freqIndex)));
    		return;
    	}
    	// UPPER TIME SELECTED
    	timeIndex = (xPos - leftOffset) / xStep + leftX;
	    System.out.println("TIME: " + (timeIndex * timeStep));
	    return;
	    */
	}
	
	public static DFTModel.TFA getFileData(int mouseX, int mouseY) {
		if(mouseX < DFTEditor.leftOffset || mouseY < DFTEditor.upperOffset) return null;
		int xStep = DFTView.getXStep();
		int yStep = DFTView.getYStep();
		int time = (mouseX - DFTEditor.leftOffset) / xStep + DFTEditor.leftX;
		int freq = (mouseY - DFTEditor.upperOffset) / yStep + DFTEditor.upperY;
		int realFreq = DFTEditor.maxRealFreq - freq;
		DFTModel.TFA returnVal = new DFTModel.TFA(time, realFreq, DFTEditor.getAmplitude(time, freq));
		System.out.println("Selected: " + returnVal.getTimeInMillis() + " " + returnVal.getFreqInHz() + " " + returnVal.getAmplitude());
		return returnVal;
	}
	
    public void actionPerformed(ActionEvent e) {
        int apOldUpperY = DFTEditor.upperY;
        int apOldLeftX = DFTEditor.leftX;
        // Frequency values have reverse sign due to screen display
        if ("F+31".equals(e.getActionCommand())) adjustY(-31);
        if ("F-31".equals(e.getActionCommand())) adjustY(31);
        if ("F+6".equals(e.getActionCommand())) adjustY(-6);
        if ("F-6".equals(e.getActionCommand())) adjustY(6);        
        if ("+250ms".equals(e.getActionCommand())) adjustX(250 / DFTEditor.timeStepInMillis);
        if ("+500ms".equals(e.getActionCommand())) adjustX(500 / DFTEditor.timeStepInMillis);
        if ("+1s".equals(e.getActionCommand())) adjustX(1000 / DFTEditor.timeStepInMillis);
        if ("+2s".equals(e.getActionCommand())) adjustX(2000 / DFTEditor.timeStepInMillis);
        if ("+5s".equals(e.getActionCommand())) adjustX(5000 / DFTEditor.timeStepInMillis);
        if ("+10s".equals(e.getActionCommand())) adjustX(10000 / DFTEditor.timeStepInMillis);
        if ("+30s".equals(e.getActionCommand())) adjustX(30000 / DFTEditor.timeStepInMillis);
    	if ("+1min".equals(e.getActionCommand())) adjustX(60000 / DFTEditor.timeStepInMillis);
        if ("-250ms".equals(e.getActionCommand())) adjustX(-250 / DFTEditor.timeStepInMillis);
        if ("-500ms".equals(e.getActionCommand())) adjustX(-500 / DFTEditor.timeStepInMillis);
        if ("-1s".equals(e.getActionCommand())) adjustX(-1000 / DFTEditor.timeStepInMillis);
        if ("-2s".equals(e.getActionCommand())) adjustX(-2000 / DFTEditor.timeStepInMillis);
        if ("-5s".equals(e.getActionCommand())) adjustX(-5000 / DFTEditor.timeStepInMillis);
        if ("-10s".equals(e.getActionCommand())) adjustX(-10000 / DFTEditor.timeStepInMillis);
        if ("-30s".equals(e.getActionCommand())) adjustX(-30000 / DFTEditor.timeStepInMillis);
    	if ("-1min".equals(e.getActionCommand())) adjustX(-60000 / DFTEditor.timeStepInMillis);
        if((apOldUpperY != DFTEditor.upperY) || (apOldLeftX != DFTEditor.leftX)) {
        	DFTEditor.view.repaint();
        }
    }
    
    private void adjustY(int deltaY) {
    	if(deltaY > 0) {
    		int maxY = (DFTEditor.maxRealFreq - DFTEditor.minRealFreq);
    		if((DFTEditor.upperY + deltaY) < maxY) {
    			DFTEditor.upperY += deltaY;
    		} else {
    			DFTEditor.upperY = maxY;
    		}
    	} else {
    		if((DFTEditor.upperY + deltaY) > 0) {
    			DFTEditor.upperY += deltaY;
    		} else {
    			DFTEditor.upperY = 0;
    		}
    	}
    }
    
    private void adjustX(int deltaX) {
    	if(deltaX > 0) {
    		int maxX = DFTEditor.maxTime;
    		if((DFTEditor.leftX + deltaX) < maxX) {
    			DFTEditor.leftX += deltaX;
    		} else {
    			DFTEditor.leftX = maxX;
    		}
    	} else {
    		if((DFTEditor.leftX + deltaX) > 0) {
    			DFTEditor.leftX += deltaX;
    		} else {
    			DFTEditor.leftX = 0;
    		}
    	}
    }
    
}

