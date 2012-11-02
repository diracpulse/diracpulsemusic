
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
	
	public enum ControllerAction {
		RANGE_SELECT;
	}
	
	public static int selectionIndex = -1;
	public static int[] selectedTimes = {-1, -1};
	public static int[] selectedFreqs = {-1, -1};
	
	public static ControllerAction currentAction = ControllerAction.RANGE_SELECT;


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
	    int time = FDUtils.pixelXToTime(x);
	    int note = FDUtils.pixelYToNote(y);
	    if(e.isControlDown()) {
	    	handleRangeSelection(x, y);
	    	return;
	    }
	    if(e.isAltDown()) {
	    	handleHarmonicsSelection(time, note);
	    	return;
	    }
	    if(!FDEditor.timeToNoteToData.containsKey(time)) return;
	    if(!FDEditor.timeToNoteToData.get(time).containsKey(note)) return;
	    ArrayList<Long> harmonicIDs = new ArrayList<Long>();
	    for(FDData data: FDEditor.timeToNoteToData.get(time).get(note)) {
	    	long harmonicID = data.getHarmonicID();
	    	if(!FDUtils.isHarmonicVisible(FDEditor.harmonicIDToHarmonic.get(harmonicID))) continue;
	    }
	    DFTEditor.selectHarmonics(harmonicIDs);
	}
	
	void handleHarmonicsSelection(int time, int note) {
		double innerFactorStep = 1.0;
		double maxInnerFactor = 8.0;
		for(double innerFactor = 1.0; innerFactor < maxInnerFactor; innerFactor += innerFactorStep) {
			double frequencyInHz = Math.pow(FDData.logBase, note / FDData.noteBase) * innerFactor;
			if(frequencyInHz > FDData.maxFrequencyInHz) return;
			int harmonicNote = (int) Math.round(Math.log(frequencyInHz) / Math.log(2.0) * FDData.noteBase);
			DFTEditor.selectedNotes.add(harmonicNote);
			System.out.println(harmonicNote + " " + frequencyInHz);
		}
		double prevMaxInnerFactor = 8.0;
		while(true) {
			innerFactorStep *= 2.0;
			maxInnerFactor *= 2.0;
			for(double innerFactor = prevMaxInnerFactor; innerFactor < maxInnerFactor; innerFactor += innerFactorStep) {
				double frequencyInHz = Math.pow(FDData.logBase, note / FDData.noteBase) * innerFactor;
				if(frequencyInHz > FDData.maxFrequencyInHz) return;
				int harmonicNote = (int) Math.round(Math.log(frequencyInHz) / Math.log(2.0) * FDData.noteBase);
				DFTEditor.selectedNotes.add(harmonicNote);
				System.out.println(harmonicNote + " " + frequencyInHz);
			}
			prevMaxInnerFactor = maxInnerFactor;
		}
	}
	
	void handleRangeSelection(int x, int y) {
	    if(selectionIndex <= 0) selectionIndex = 0;
	    if(selectionIndex > 1) selectionIndex = 0;
	    selectedTimes[selectionIndex] = FDUtils.pixelXToTime(x);
	    if(selectedTimes[selectionIndex] < FDEditor.minViewTime) selectedTimes[selectionIndex] = FDEditor.minViewTime;
	    if(selectedTimes[selectionIndex] > FDEditor.maxTime) selectedTimes[selectionIndex] = FDEditor.maxTime;
	    selectedFreqs[selectionIndex] = FDEditor.noteToFreq(FDUtils.pixelYToNote(y));
	    if(selectedFreqs[selectionIndex] < FDEditor.minViewFreq) selectedFreqs[selectionIndex] = FDEditor.minViewFreq;
	    if(selectedFreqs[selectionIndex] > FDEditor.noteToFreq(FDEditor.minNote)) selectedFreqs[selectionIndex] = FDEditor.noteToFreq(FDEditor.minNote);	    	
	    selectionIndex++;
	    if(selectionIndex == 1) {
	    	FDEditor.refreshView();
	    	GraphEditor.refreshView();
	    	return;
	    }
	    if(selectedTimes[1] > selectedTimes[0]) {
	    	GraphEditor.maxViewTime = selectedTimes[1];
	    	GraphEditor.minViewTime = selectedTimes[0];
	    } else {
	    	GraphEditor.maxViewTime = selectedTimes[0];
	    	GraphEditor.minViewTime = selectedTimes[1];
	    }
	    int[] selectedNotes = {FDEditor.freqToNote(selectedFreqs[0]), FDEditor.freqToNote(selectedFreqs[1])};
	    if(selectedNotes[1] > selectedNotes[0]) {
	    	GraphEditor.maxViewNote = selectedNotes[1];
	    	GraphEditor.minViewNote = selectedNotes[0];
	    } else {
	    	GraphEditor.maxViewNote = selectedNotes[0];
	    	GraphEditor.minViewNote = selectedNotes[1];
	    }
    	FDEditor.refreshView();
    	GraphEditor.refreshView();
	    return;
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
        int apOldUpperY = FDEditor.minViewFreq;
        int apOldLeftX = FDEditor.minViewTime;
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
        if((apOldUpperY != FDEditor.minViewFreq) || (apOldLeftX != FDEditor.minViewTime)) {
        	FDEditor.view.repaint();
        }
    }

    private void adjustY(int deltaY) {
    	if(deltaY > 0) {
    		int maxY = 1000000; // (FDEditor.maxRealFreq - FDEditor.minRealFreq);
    		if((FDEditor.minViewFreq + deltaY) < maxY) {
    			FDEditor.minViewFreq += deltaY;
    		} else {
    			FDEditor.minViewFreq = maxY;
    		}
    	} else {
    		if((FDEditor.minViewFreq + deltaY) > 0) {
    			FDEditor.minViewFreq += deltaY;
    		} else {
    			FDEditor.minViewFreq = 0;
    		}
    	}
    }
    
    private void adjustX(int deltaX) {
    	if(deltaX > 0) {
    		int maxX = 1000000; //FDEditor.maxTime;
    		if((FDEditor.minViewTime + deltaX) < maxX) {
    			FDEditor.minViewTime += deltaX;
    		} else {
    			FDEditor.minViewTime = maxX;
    		}
    	} else {
    		if((FDEditor.minViewTime + deltaX) > 0) {
    			FDEditor.minViewTime += deltaX;
    		} else {
    			FDEditor.minViewTime = 0;
    		}
    	}
    }
    
}

