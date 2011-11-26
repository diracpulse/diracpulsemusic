
import java.awt.Rectangle;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class FDController implements MouseListener, ActionListener {
	
	private FDView view;
	private FDEditor parent;

	FDController(FDEditor parent) {
		this.parent = parent;
	}
	
	public void setView(FDView view) {
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
		if(mouseX < FDEditor.leftOffset || mouseY < FDEditor.upperOffset) return null;
		int xStep = DFTView.getXStep();
		int yStep = DFTView.getYStep();
		int time = (mouseX - FDEditor.leftOffset) / xStep + FDEditor.startTimeIndex;
		int freq = (mouseY - FDEditor.upperOffset) / yStep + FDEditor.startFreqIndex;
		int realFreq = FDEditor.maxRealFreq - freq;
		DFTModel.TFA returnVal = new DFTModel.TFA(time, realFreq, FDEditor.getAmplitude(time, freq));
		System.out.println("Selected: " + returnVal.getTimeInMillis() + " " + returnVal.getFreqInHz() + " " + returnVal.getAmplitude());
		return returnVal;
		*/
	}
	
    public void actionPerformed(ActionEvent e) {
    	if("Add Data".equals(e.getActionCommand())) {
    		handleDataCreationAction();
    		return;
    	}
        int apOldUpperY = FDEditor.startFreqIndex;
        int apOldLeftX = FDEditor.startTimeIndex;
        // Frequency values have reverse sign due to screen display
        if ("F+31".equals(e.getActionCommand())) adjustY(-31);
        if ("F-31".equals(e.getActionCommand())) adjustY(31);
        if ("F+6".equals(e.getActionCommand())) adjustY(-6);
        if ("F-6".equals(e.getActionCommand())) adjustY(6);        
        if ("+250ms".equals(e.getActionCommand())) adjustX(250 / FDEditor.timeStepInMillis);
        if ("+500ms".equals(e.getActionCommand())) adjustX(500 / FDEditor.timeStepInMillis);
        if ("+1s".equals(e.getActionCommand())) adjustX(1000 / FDEditor.timeStepInMillis);
        if ("+2s".equals(e.getActionCommand())) adjustX(2000 / FDEditor.timeStepInMillis);
        if ("+5s".equals(e.getActionCommand())) adjustX(5000 / FDEditor.timeStepInMillis);
        if ("+10s".equals(e.getActionCommand())) adjustX(10000 / FDEditor.timeStepInMillis);
        if ("+30s".equals(e.getActionCommand())) adjustX(30000 / FDEditor.timeStepInMillis);
    	if ("+1min".equals(e.getActionCommand())) adjustX(60000 / FDEditor.timeStepInMillis);
        if ("-250ms".equals(e.getActionCommand())) adjustX(-250 / FDEditor.timeStepInMillis);
        if ("-500ms".equals(e.getActionCommand())) adjustX(-500 / FDEditor.timeStepInMillis);
        if ("-1s".equals(e.getActionCommand())) adjustX(-1000 / FDEditor.timeStepInMillis);
        if ("-2s".equals(e.getActionCommand())) adjustX(-2000 / FDEditor.timeStepInMillis);
        if ("-5s".equals(e.getActionCommand())) adjustX(-5000 / FDEditor.timeStepInMillis);
        if ("-10s".equals(e.getActionCommand())) adjustX(-10000 / FDEditor.timeStepInMillis);
        if ("-30s".equals(e.getActionCommand())) adjustX(-30000 / FDEditor.timeStepInMillis);
    	if ("-1min".equals(e.getActionCommand())) adjustX(-60000 / FDEditor.timeStepInMillis);
        if((apOldUpperY != FDEditor.startFreqIndex) || (apOldLeftX != FDEditor.startTimeIndex)) {
        	FDEditor.view.repaint();
        }
    }
    
    public void handleDataCreationAction() {
    	System.out.println("Start Time: " + FDEditor.startTimeTextField.getText());
    	System.out.println("Start Note: " + FDEditor.startNoteTextField.getText());
    	System.out.println("Start Amplitude: " + FDEditor.startAmplitudeTextField.getText());
    	System.out.println("End Time: " + FDEditor.endTimeTextField.getText());
    	System.out.println("End Note: " + FDEditor.endNoteTextField.getText());
    	System.out.println("End Amplitude: " + FDEditor.endAmplitudeTextField.getText());
    }
    
    private void adjustY(int deltaY) {
    	if(deltaY > 0) {
    		int maxY = 1000000; // (FDEditor.maxRealFreq - FDEditor.minRealFreq);
    		if((FDEditor.startFreqIndex + deltaY) < maxY) {
    			FDEditor.startFreqIndex += deltaY;
    		} else {
    			FDEditor.startFreqIndex = maxY;
    		}
    	} else {
    		if((FDEditor.startFreqIndex + deltaY) > 0) {
    			FDEditor.startFreqIndex += deltaY;
    		} else {
    			FDEditor.startFreqIndex = 0;
    		}
    	}
    }
    
    private void adjustX(int deltaX) {
    	if(deltaX > 0) {
    		int maxX = 1000000; //FDEditor.maxTime;
    		if((FDEditor.startTimeIndex + deltaX) < maxX) {
    			FDEditor.startTimeIndex += deltaX;
    		} else {
    			FDEditor.startTimeIndex = maxX;
    		}
    	} else {
    		if((FDEditor.startTimeIndex + deltaX) > 0) {
    			FDEditor.startTimeIndex += deltaX;
    		} else {
    			FDEditor.startTimeIndex = 0;
    		}
    	}
    }
    
}

