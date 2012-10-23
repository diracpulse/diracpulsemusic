
import java.awt.event.*;

public class DFTController implements MouseListener, ActionListener {
	
	public enum ControllerAction {
		RANGE_SELECT;
	}
	
	public static int selectionIndex = -1;
	public static int[] selectedTimes = {-1, -1};
	public static int[] selectedFreqs = {-1, -1};
	
	public static ControllerAction currentAction = ControllerAction.RANGE_SELECT;

	DFTController() {}

	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){
	    int x = e.getX();
	    int y = e.getY();
	    if(selectionIndex <= 0) selectionIndex = 0;
	    if(selectionIndex > 1) selectionIndex = 0;
	    selectedTimes[selectionIndex] = DFTUtils.screenXToTime(x);
	    if(selectedTimes[selectionIndex] < DFTEditor.minViewTime) selectedTimes[selectionIndex] = DFTEditor.minViewTime;
	    if(selectedTimes[selectionIndex] > DFTEditor.maxTime) selectedTimes[selectionIndex] = DFTEditor.maxTime;
	    selectedFreqs[selectionIndex] = DFTUtils.screenYToFreq(y);
	    if(selectedFreqs[selectionIndex] < DFTEditor.minViewFreq) selectedFreqs[selectionIndex] = DFTEditor.minViewFreq;
	    if(selectedFreqs[selectionIndex] > DFTEditor.maxScreenFreq) selectedFreqs[selectionIndex] = DFTEditor.maxScreenFreq;	    	
	    selectionIndex++;
	    if(selectionIndex == 1) {
	    	DFTEditor.refreshView();
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
	    int[] selectedNotes = {DFTEditor.freqToNote(selectedFreqs[0]), DFTEditor.freqToNote(selectedFreqs[1])};
	    if(selectedNotes[1] > selectedNotes[0]) {
	    	GraphEditor.maxViewNote = selectedNotes[1];
	    	GraphEditor.minViewNote = selectedNotes[0];
	    } else {
	    	GraphEditor.maxViewNote = selectedNotes[0];
	    	GraphEditor.minViewNote = selectedNotes[1];
	    }
    	DFTEditor.refreshView();
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
	
	public static FDData getFileData(int mouseX, int mouseY) {
		FDData returnVal = null;
		//if(mouseX < DFTEditor.leftOffset || mouseY < DFTEditor.upperOffset) return null;
		if((DFTView.getXStep() >= 1.0) && (DFTView.getXStep() >= 1.0)) {
			int xStep = (int) DFTView.getXStep();
			int yStep = (int) DFTView.getYStep();
			int time = (mouseX - DFTEditor.leftOffset) / xStep + DFTEditor.minViewTime;
			int freq = (mouseY - DFTEditor.upperOffset) / yStep + DFTEditor.minViewFreq;
			//int realFreq = DFTEditor.maxRealFreq - freq;
			returnVal = DFTUtils.getValue(time, freq);
		}
		if(returnVal != null) {
			System.out.println("Selected: " + returnVal.getTimeInMillis() + " " + returnVal.getFrequencyInHz() + " " + returnVal.getLogAmplitude());
		}
		return returnVal;
	}
	
    public void actionPerformed(ActionEvent e) {
        int apOldUpperY = DFTEditor.minViewFreq;
        int apOldLeftX = DFTEditor.minViewTime;
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
        if((apOldUpperY != DFTEditor.minViewFreq) || (apOldLeftX != DFTEditor.minViewTime)) {
        	DFTEditor.refreshView();
        }
    }
    
    private void adjustY(int deltaY) {
    	if(deltaY > 0) {
    		int maxY = (DFTEditor.maxScreenFreq);
    		if((DFTEditor.minViewFreq + deltaY) < maxY) {
    			DFTEditor.minViewFreq += deltaY;
    		} else {
    			DFTEditor.minViewFreq = maxY;
    		}
    	} else {
    		if((DFTEditor.minViewFreq + deltaY) > 0) {
    			DFTEditor.minViewFreq += deltaY;
    		} else {
    			DFTEditor.minViewFreq = 0;
    		}
    	}
    }
    
    private void adjustX(int deltaX) {
    	if(deltaX > 0) {
    		int maxX = DFTEditor.maxTime;
    		if((DFTEditor.minViewTime + deltaX) < maxX) {
    			DFTEditor.minViewTime += deltaX;
    		} else {
    			DFTEditor.minViewTime = maxX;
    		}
    	} else {
    		if((DFTEditor.minViewTime + deltaX) > 0) {
    			DFTEditor.minViewTime += deltaX;
    		} else {
    			DFTEditor.minViewTime = 0;
    		}
    	}
    }

}

