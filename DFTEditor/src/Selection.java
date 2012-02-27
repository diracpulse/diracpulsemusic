import java.util.ArrayList;
import java.util.TreeMap;

public class Selection {
	
	public enum Area {
		RECTANGLE, RIGHT_TRIANGLE;
	}
	
	public enum Type {
		FLATTEN, FOLLOW_FREQUENCY, GRANULAR;
	}
	
	private ArrayList<FDData> inputData;
	private ArrayList<FDData> outputData;
	
	Area area = Area.RECTANGLE;
	Type type = Type.FLATTEN;
	// FILL GAPS IS BROKEN
	public boolean fillGaps = true;
	
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
			case RECTANGLE:
				if(inputData.size() == 2) return true;
				return false;
			case RIGHT_TRIANGLE:
				if(inputData.size() == 3) return true;
				return false;
		}
		System.out.println("Selection.selectionComplete() SelectionType unknown");
		return false;
	}
	
	public ArrayList<FDData> getSelectedData() {
		if(!selectionComplete()) return null;
		switch(area) {
			case RECTANGLE:
				getRECTANGLEData();
				return outputData;
			case RIGHT_TRIANGLE:
				System.out.println("RIGHT_TRIANGLE not implemented currently");
				return null;
		}
		System.out.println("Selection.getSelectedData() SelectionType unknown");
		return null;
	}

	private void getRECTANGLEData() {
		TreeMap<Integer, FDData> internalData = new TreeMap<Integer, FDData>();
		int startTime = inputData.get(0).getTime();
		int endTime = inputData.get(1).getTime();
		if(endTime < startTime) {
			int saveEndTime = endTime;
			endTime = startTime;
			startTime = saveEndTime;
		}
		int lowerNote = inputData.get(0).getNote();
		int upperNote = inputData.get(1).getNote();
		if(upperNote < lowerNote) {
			int saveUpperNote = upperNote;
			upperNote = lowerNote;
			lowerNote = saveUpperNote;
		}
		for(int time = startTime; time <= endTime; time++) {
			float maxAmplitude = 0.0f;
			float currentAmplitude = 0.0f;
			int noteAtMaximum = -1;
			for(int note = lowerNote; note <= upperNote; note++) {
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
	
	// DOES NOT RETURN end (this is to avoid duplicates)
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
				System.out.println("Selection.interpolateData():" + time + " " + noteVal + " " + ampVal);
				outputData.add(new FDData(time, noteVal, ampVal));
			} catch (Exception e) {
				System.out.println("Selection.interpolateData(): Error creating FDData");
			}
		}
	}
	
}
