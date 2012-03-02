import java.util.ArrayList;

public class Selection {
	
	public enum Area {
		LINE_INTERPOLATE, RECTANGLE, TRIANGLE;
	}

	// These are used to avoid excess passing of arguments in internal functions
	private ArrayList<FDData> inputData;
	private ArrayList<FDData> outputData;
	
	
	Area area = Area.LINE_INTERPOLATE;
	public boolean deleteSelected = false;
	
	public Selection(Area area, boolean deleteSelected) {
		this.area = area;
		this.deleteSelected = deleteSelected;
		inputData = new ArrayList<FDData>();
		outputData = new ArrayList<FDData>();
	}
	
	public void addData(FDData input) {
		inputData.add(input);
		if(selectionComplete()) {
			getSelectedData();
			if(deleteSelected) {
				for(FDData loopData: outputData) DFTEditor.removeSelected(loopData);
			} else {
				for(FDData loopData: outputData) DFTEditor.addSelected(loopData);
			}
			DFTEditor.newSelection(this.area == Area.LINE_INTERPOLATE);
		}
	}
	
	public void undo() {
		if(selectionComplete()) {
			getSelectedData();
			if(deleteSelected) {
				for(FDData loopData: outputData) DFTEditor.addSelected(loopData);
			} else {
				for(FDData loopData: outputData) DFTEditor.removeSelected(loopData);
			}
		} else {
			System.out.println("Selection.undo(): cannot undo incomplete selection");
		}
	}
	
	public ArrayList<FDData> getInputData() {
		return inputData;
	}
	
	public boolean selectionComplete() {
		switch(area) {
			case LINE_INTERPOLATE:
				if(inputData.size() == 2) return true;
				return false;
			case RECTANGLE:
				if(inputData.size() == 2) return true;
				return false;		
		}
		System.out.println("Selection.selectionComplete() SelectionType unknown");
		return false;
	}
	
	public ArrayList<FDData> getSelectedData() {
		if(!selectionComplete()) return null;
		switch(area) {
			case LINE_INTERPOLATE:
				// false means don't initialize start of next selection with end of previous
				interpolateData(inputData.get(0), inputData.get(1), false);
				// Continue line with previous end as start
				return outputData;
			case RECTANGLE:
				getRECTANGLEData();
				return outputData;
				
		}
		System.out.println("Selection.getSelectedData() SelectionType unknown");
		return null;
	}

	// Adds all Maxima and Selected data in a rectangular region
	private void getRECTANGLEData() {
		DFTGeometry.SortedPair times = 
			new  DFTGeometry.SortedPair(inputData.get(0).getTime(), inputData.get(1).getTime());
		DFTGeometry.SortedPair notes = 
			new  DFTGeometry.SortedPair(inputData.get(0).getNote(), inputData.get(1).getNote());
		for(int note = notes.lower; note <= notes.upper; note++) {
			float currentAmplitude = 0.0f;
			for(int time = times.lower; time <= times.upper; time++) {
				if(!DFTEditor.isMaxima(time, DFTEditor.noteToFreq(note))) {
					if(!DFTEditor.isSelected(time, DFTEditor.noteToFreq(note))) {
						if(!(notes.upper == notes.lower)) continue; // line selection
					}
				}
				currentAmplitude = DFTEditor.getAmplitude(time, DFTEditor.noteToFreq(note));
				try {
					outputData.add(new FDData(time, note, currentAmplitude));
				} catch (Exception e) {
					System.out.println("Selection.getRECTANGLEData: error adding FDData");
				}
			}
		}
	}	

	// Note adding end is to used to avoid duplicates in getRECTANGLEData 
	private void interpolateData(FDData start, FDData end, boolean addEnd) {
		int addEndVal = 0;
		if(addEnd) addEndVal = 1;
		if(start.getTime() == end.getTime()) {
			outputData.add(start);
			return;
		}
		if(start.getTime() > end.getTime()) {
			FDData saveStart = start;
			start = end;
			end = saveStart;
		}
		double dStartTime = start.getTime();
		double dEndTime = end.getTime();
		double dStartNote = start.getNote();
		double dEndNote = end.getNote();
		double dStartAmp = start.getLogAmplitude();
		double dEndAmp = end.getLogAmplitude();
		double ampSlope = (dEndAmp - dStartAmp) / (dEndTime - dStartTime);
		double noteSlope = (dEndNote - dStartNote) / (dEndTime - dStartTime);
		for(int time = start.getTime(); time < end.getTime() + addEndVal; time++) {
			double dTime = time;
			double dDeltaTime = dTime - dStartTime;
			double ampVal = dStartAmp + ampSlope * dDeltaTime;
			int noteVal = (int) Math.round(dStartNote + noteSlope * dDeltaTime);
			// note is passed to FDData as noteComplete
			try {
				//System.out.println("Selection.interpolateData():" + time + " " + noteVal + " " + ampVal);
				outputData.add(new FDData(time, noteVal, ampVal));
			} catch (Exception e) {
				System.out.println("Selection.interpolateData(): Error creating FDData");
			}
		}
	}
	
}
