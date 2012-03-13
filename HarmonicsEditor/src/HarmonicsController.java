
import java.awt.Rectangle;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class HarmonicsController implements MouseListener, ActionListener {
	
	private HarmonicsView view;
	private HarmonicsEditor parent;

	HarmonicsController(HarmonicsEditor parent) {
		this.parent = parent;
	}
	
	public void setView(HarmonicsView view) {
		this.view = view;
	}	
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){
	    int x = e.getX();
	    int y = e.getY();
	    /*
	    if (view.getView() == DFTView.View.Digits) {
	    	//harmonicSelection(getFileData(x,y));
	    } else {
	    	DFTModel.TFA selected = getFileData(x, y);
	    	if(selected != null) {
	    		System.out.println(selected);
	    	} else {
	    		System.out.println(x + " " + y);
	    	}
	    }
	    */
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
	
	public static void getFileData(int mouseX, int mouseY) {
		/*
		if(mouseX < HarmonicsEditor.leftOffset || mouseY < HarmonicsEditor.upperOffset) return null;
		int xStep = DFTView.getXStep();
		int yStep = DFTView.getYStep();
		int time = (mouseX - HarmonicsEditor.leftOffset) / xStep + HarmonicsEditor.startTimeIndex;
		int freq = (mouseY - HarmonicsEditor.upperOffset) / yStep + HarmonicsEditor.startFreqIndex;
		int realFreq = HarmonicsEditor.maxRealFreq - freq;
		DFTModel.TFA returnVal = new DFTModel.TFA(time, realFreq, HarmonicsEditor.getAmplitude(time, freq));
		System.out.println("Selected: " + returnVal.getTimeInMillis() + " " + returnVal.getFreqInHz() + " " + returnVal.getAmplitude());
		return returnVal;
		*/
	}
	
    public void actionPerformed(ActionEvent e) {
        int apOldUpperY = HarmonicsEditor.upperY;
        int apOldLeftX = HarmonicsEditor.leftX;
        // Frequency values have reverse sign due to screen display
        if ("F+31".equals(e.getActionCommand())) adjustY(-31);
        if ("F-31".equals(e.getActionCommand())) adjustY(31);
        if ("F+6".equals(e.getActionCommand())) adjustY(-6);
        if ("F-6".equals(e.getActionCommand())) adjustY(6);        
        if ("+250ms".equals(e.getActionCommand())) adjustX(250 / HarmonicsEditor.timeStepInMillis);
        if ("+500ms".equals(e.getActionCommand())) adjustX(500 / HarmonicsEditor.timeStepInMillis);
        if ("+1s".equals(e.getActionCommand())) adjustX(1000 / HarmonicsEditor.timeStepInMillis);
        if ("+2s".equals(e.getActionCommand())) adjustX(2000 / HarmonicsEditor.timeStepInMillis);
        if ("+5s".equals(e.getActionCommand())) adjustX(5000 / HarmonicsEditor.timeStepInMillis);
        if ("+10s".equals(e.getActionCommand())) adjustX(10000 / HarmonicsEditor.timeStepInMillis);
        if ("+30s".equals(e.getActionCommand())) adjustX(30000 / HarmonicsEditor.timeStepInMillis);
    	if ("+1min".equals(e.getActionCommand())) adjustX(60000 / HarmonicsEditor.timeStepInMillis);
        if ("-250ms".equals(e.getActionCommand())) adjustX(-250 / HarmonicsEditor.timeStepInMillis);
        if ("-500ms".equals(e.getActionCommand())) adjustX(-500 / HarmonicsEditor.timeStepInMillis);
        if ("-1s".equals(e.getActionCommand())) adjustX(-1000 / HarmonicsEditor.timeStepInMillis);
        if ("-2s".equals(e.getActionCommand())) adjustX(-2000 / HarmonicsEditor.timeStepInMillis);
        if ("-5s".equals(e.getActionCommand())) adjustX(-5000 / HarmonicsEditor.timeStepInMillis);
        if ("-10s".equals(e.getActionCommand())) adjustX(-10000 / HarmonicsEditor.timeStepInMillis);
        if ("-30s".equals(e.getActionCommand())) adjustX(-30000 / HarmonicsEditor.timeStepInMillis);
    	if ("-1min".equals(e.getActionCommand())) adjustX(-60000 / HarmonicsEditor.timeStepInMillis);
        if((apOldUpperY != HarmonicsEditor.upperY) || (apOldLeftX != HarmonicsEditor.leftX)) {
        	HarmonicsEditor.view.repaint();
        }
    }

    private void adjustY(int deltaY) {
    	if(deltaY > 0) {
    		int maxY = 1000000; // (HarmonicsEditor.maxRealFreq - HarmonicsEditor.minRealFreq);
    		if((HarmonicsEditor.upperY + deltaY) < maxY) {
    			HarmonicsEditor.upperY += deltaY;
    		} else {
    			HarmonicsEditor.upperY = maxY;
    		}
    	} else {
    		if((HarmonicsEditor.upperY + deltaY) > 0) {
    			HarmonicsEditor.upperY += deltaY;
    		} else {
    			HarmonicsEditor.upperY = 0;
    		}
    	}
    }
    
    private void adjustX(int deltaX) {
    	if(deltaX > 0) {
    		int maxX = 1000000; //HarmonicsEditor.maxTime;
    		if((HarmonicsEditor.leftX + deltaX) < maxX) {
    			HarmonicsEditor.leftX += deltaX;
    		} else {
    			HarmonicsEditor.leftX = maxX;
    		}
    	} else {
    		if((HarmonicsEditor.leftX + deltaX) > 0) {
    			HarmonicsEditor.leftX += deltaX;
    		} else {
    			HarmonicsEditor.leftX = 0;
    		}
    	}
    }
    
}

