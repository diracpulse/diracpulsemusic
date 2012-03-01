import java.util.ArrayList;
import java.util.TreeMap;

public class Selection {
	
	public enum Area {
		LINE, RECTANGLE, TRIANGLE;
	}
	
	public enum Type {
		DEFAULT; //, HARMONIC, GRANULAR;
	}

	// These are used to avoid excess passing of arguments in internal functions
	private ArrayList<FDData> inputData;
	private ArrayList<FDData> outputData;
	
	
	Area area = Area.LINE;
	Type type = Type.DEFAULT;
	public boolean fillGaps = true;
	public boolean deleteSelected = false;
	
	public Selection(Area area, Type type) {
		this.area = area;
		this.type = type;
		inputData = new ArrayList<FDData>();
		outputData = new ArrayList<FDData>();
	}
	
	public void addData(FDData input) {
		inputData.add(input);
	}
	
	public boolean selectionComplete() {
		switch(area) {
			case LINE:
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
			case LINE:
				getLINEData();
				return outputData;
			case RECTANGLE:
				getRECTANGLEData();
				return outputData;
				
		}
		System.out.println("Selection.getSelectedData() SelectionType unknown");
		return null;
	}

	private void getLINEData() {
		TreeMap<Integer, FDData> internalData = new TreeMap<Integer, FDData>();
		DFTGeometry.SortedPair times = 
			new  DFTGeometry.SortedPair(inputData.get(0).getTime(), inputData.get(1).getTime());
		DFTGeometry.SortedPair notes = 
			new  DFTGeometry.SortedPair(inputData.get(0).getNote(), inputData.get(1).getNote());
		for(int time = times.lower; time <= times.upper; time++) {
			float maxAmplitude = 0.0f;
			float currentAmplitude = 0.0f;
			int noteAtMaximum = -1;
			for(int note = notes.lower; note <= notes.upper; note++) {
				if(DFTView.getDataView() != DFTView.DataView.MAXIMAS_ONLY) {
					currentAmplitude = DFTEditor.getAmplitude(time, DFTEditor.noteToFreq(note));
				} else {
					if(!DFTEditor.isMaxima(time, DFTEditor.noteToFreq(note))) continue;
					currentAmplitude = DFTEditor.getAmplitude(time, DFTEditor.noteToFreq(note));
				}
			// WEAKNESS: if two or more freqs have same amplitude, highest one is used
				if(currentAmplitude >= maxAmplitude) {
					maxAmplitude = currentAmplitude;
					noteAtMaximum = note;
				}
			}
			try {
				if(noteAtMaximum != -1) {
					internalData.put(time, new FDData(time, noteAtMaximum, maxAmplitude));
				}
			} catch (Exception e) {
				System.out.println("Selection.getFLATData: error adding FDData");
			}
		}
		if(fillGaps && (internalData.size() > 1)) {
			fillGaps(new ArrayList<FDData>(internalData.values()));
			return;
		} else {
			outputData =  new ArrayList<FDData>(internalData.values());
		}
	}
	
	// Adds all Maxima data in a rectangular region
	private void getRECTANGLEData() {
		DFTGeometry.SortedPair times = 
			new  DFTGeometry.SortedPair(inputData.get(0).getTime(), inputData.get(1).getTime());
		DFTGeometry.SortedPair notes = 
			new  DFTGeometry.SortedPair(inputData.get(0).getNote(), inputData.get(1).getNote());
		for(int note = notes.lower; note <= notes.upper; note++) {
			float currentAmplitude = 0.0f;
			for(int time = times.lower; time <= times.upper; time++) {
				if(!DFTEditor.isMaxima(time, DFTEditor.noteToFreq(note))) continue;
				currentAmplitude = DFTEditor.getAmplitude(time, DFTEditor.noteToFreq(note));
				try {
					outputData.add(new FDData(time, note, currentAmplitude));
				} catch (Exception e) {
					System.out.println("Selection.getRECTANGLEData: error adding FDData");
				}
			}
		}
	}
	
	private void fillGaps(ArrayList<FDData> internalData) {
		if(internalData.size() < 2) {
			System.out.println("Selection.fillGaps: internalData < 2");
		}
		FDData currentValue = internalData.get(0);
		FDData nextValue = null;
		boolean skipFirst = true;
		for(FDData loopData: internalData) {
			if(skipFirst) {
				skipFirst = false;
				continue;
			}
			nextValue = loopData;
			interpolateData(currentValue, nextValue);
			currentValue = nextValue;
		}
		outputData.add(currentValue);
	}
	
	// DOES NOT ADD end (this is to avoid duplicates)
	private void interpolateData(FDData start, FDData end) {
		if(start.getTime() == end.getTime()) {
			outputData.add(start);
			return;
		}
		double dStartTime = start.getTime();
		double dEndTime = end.getTime();
		double dStartNote = start.getNote();
		double dEndNote = end.getNote();
		double dStartAmp = start.getLogAmplitude();
		double dEndAmp = end.getLogAmplitude();
		double ampSlope = (dEndAmp - dStartAmp) / (dEndTime - dStartTime);
		double noteSlope = (dEndNote - dStartNote) / (dEndTime - dStartTime);
		for(int time = start.getTime(); time < end.getTime(); time++) {
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
