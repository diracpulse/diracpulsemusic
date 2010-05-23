
import java.awt.Rectangle;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.JOptionPane;

public class DFTController implements MouseListener, ActionListener {
	
	private DFTView view;
	private DFTEditor parent;
	private Harmonics harmonics;
	private State currentState;
	// This contains selected start points for harmonics
	// Used to create harmonics once an end point is selected
	private TreeMap<Integer, Integer> harmonicStartFreqToTime = null;
	
	private enum State {
		SelectHarmonicStart,
		SelectHarmonicEnd,
		SelectHarmonic;
	}
	
	DFTController(DFTEditor parent) {
		this.parent = parent;
		currentState = State.SelectHarmonicStart;
		harmonicStartFreqToTime = new TreeMap<Integer, Integer>();
	}
	
	public void setView(DFTView view) {
		this.view = view;
	}
	
	public void setHarmonics(Harmonics harmonics) {
		this.harmonics = harmonics;
	}
	
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){
	    int x = e.getX();
	    int y = e.getY();
	    DFTModel.TFA selected = getFileData(x, y);
	    handleSelectedData(selected);
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
	
	public DFTModel.TFA getFileData(int mouseX, int mouseY) {
		int startX = DFTEditor.leftX;
		int endX = startX + ((view.getWidth() - DFTEditor.leftOffset) / DFTEditor.xStep);
		int startY = DFTEditor.upperY;
		int endY = startY + ((view.getHeight() - DFTEditor.upperOffset) / DFTEditor.yStep);
		int screenXIndex = startX;
		for(int x = startX; screenXIndex < endX; x += DFTUtils.getTimeIncrement()) {
            if(x >= DFTEditor.maxTime) break;
    		int screenYIndex = startY;
            for(int y = startY; screenYIndex < endY; y += DFTUtils.getFreqIncrement()) {
                if(y >= (DFTEditor.maxRealFreq - DFTEditor.minRealFreq)) break;
        		int screenX = DFTEditor.leftOffset + ((screenXIndex - DFTEditor.leftX) * DFTEditor.xStep);
        		int screenY = DFTEditor.upperOffset + ((screenYIndex - DFTEditor.upperY) * DFTEditor.yStep);
                DFTModel.TFA currentVal = DFTUtils.getMaxValue(x, y);
                if(currentVal.getAmplitude() > 0.0f) {
                	Rectangle currentRect = new Rectangle(screenX, screenY, DFTEditor.xStep, DFTEditor.yStep);
                	if (currentRect.contains(mouseX, mouseY)) {
                		System.out.println("SCREEN: " + screenX + " " + screenY);
                		view.repaint();
                		return currentVal;
                	}
                }
				screenYIndex++;
			}
            screenXIndex++;
		}
		return null;
	}
	
    public void actionPerformed(ActionEvent e) {
        int apOldUpperY = DFTEditor.upperY;
        int apOldLeftX = DFTEditor.leftX;
        if ("Higher".equals(e.getActionCommand())) adjustY(-DFTEditor.freqsPerOctave);
        if ("Lower".equals(e.getActionCommand())) adjustY(DFTEditor.freqsPerOctave);
        if ("+500ms".equals(e.getActionCommand())) adjustX(500 / DFTEditor.timeStepInMillis);
        if ("+1s".equals(e.getActionCommand())) adjustX(1000 / DFTEditor.timeStepInMillis);
        if ("+2s".equals(e.getActionCommand())) adjustX(2000 / DFTEditor.timeStepInMillis);
        if ("+5s".equals(e.getActionCommand())) adjustX(5000 / DFTEditor.timeStepInMillis);
        if ("+10s".equals(e.getActionCommand())) adjustX(10000 / DFTEditor.timeStepInMillis);
        if ("+30s".equals(e.getActionCommand())) adjustX(30000 / DFTEditor.timeStepInMillis);
    	if ("+1min".equals(e.getActionCommand())) adjustX(60000 / DFTEditor.timeStepInMillis);
        if ("-500ms".equals(e.getActionCommand())) adjustX(-500 / DFTEditor.timeStepInMillis);
        if ("-1s".equals(e.getActionCommand())) adjustX(-1000 / DFTEditor.timeStepInMillis);
        if ("-2s".equals(e.getActionCommand())) adjustX(-2000 / DFTEditor.timeStepInMillis);
        if ("-5s".equals(e.getActionCommand())) adjustX(-5000 / DFTEditor.timeStepInMillis);
        if ("-10s".equals(e.getActionCommand())) adjustX(-10000 / DFTEditor.timeStepInMillis);
        if ("-30s".equals(e.getActionCommand())) adjustX(-30000 / DFTEditor.timeStepInMillis);
    	if ("-1min".equals(e.getActionCommand())) adjustX(-60000 / DFTEditor.timeStepInMillis);
        if ("Play Harmonics".equals(e.getActionCommand())) {
            System.out.println("Play Harmonics");
        }
    	if ("Open".equals(e.getActionCommand())) {
            parent.openFileInDFTEditor();
        }
        if ("Exit".equals(e.getActionCommand())) {
            System.exit(0);
        }
        if ("Print Params".equals(e.getActionCommand())) {
            GenerateWavelets.printParams(); 
        }
        if ("Save Params".equals(e.getActionCommand())) {
            GenerateWavelets.writeParamsToFile(); 
        }        
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
    
    private void handleSelectedData(DFTModel.TFA selected) {
    	if(selected == null) return;
    	TreeMap<Integer, Float> freqToAmp = null;
    	ArrayList<DFTModel.TFA> TFAInput = new ArrayList<DFTModel.TFA>();
    	int freq = selected.getFreq();
    	int time = selected.getTime();
    	float amplitude = 0.0f;
    	if(harmonicStartFreqToTime.containsKey(freq)) {
    		currentState = State.SelectHarmonicEnd;
    	} else {
    		if(harmonics.inHarmonic(freq, time)) {
    			currentState = State.SelectHarmonic;
    		}
    		currentState = State.SelectHarmonicStart;
    	}
    	switch(currentState) {
    	case SelectHarmonicStart:
    		harmonicStartFreqToTime.put(freq, time);
    		break;
    	case SelectHarmonicEnd:
    		int startTime = harmonicStartFreqToTime.get(freq);
    		int endTime = time;
    		for(int timeIndex = startTime; timeIndex <= endTime; timeIndex++) {
    			if(DFTEditor.timeToFreqToAmp.containsKey(timeIndex)) {
    				freqToAmp = DFTEditor.timeToFreqToAmp.get(timeIndex);
    				if(freqToAmp.containsKey(freq)) {
    					amplitude = freqToAmp.get(freq);
    					TFAInput.add(new DFTModel.TFA(time, freq, amplitude));
    				}
    			}
    		}
    		harmonics.addHarmonic(TFAInput);
    		harmonicStartFreqToTime.remove(freq);
    		break;
    	case SelectHarmonic:
    		JOptionPane.showConfirmDialog(parent, "Delete Harmonic");
    		break;
    	}
    }

}

