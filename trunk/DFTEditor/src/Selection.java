import java.util.ArrayList;


public class Selection {
	
	public enum SelectionType {
		FLAT, FOLLOW_FREQUENCY;
	}
	
	private ArrayList<FDData> data;
	
	SelectionType type;
	
	public Selection(SelectionType type) {
		this.type = type;
		data = new ArrayList<FDData>();
	}
	
	public void addData(FDData input) {
		data.add(input);
	}
	
	public boolean selectionComplete() {
		switch(type) {
			case FLAT:
				if(data.size() == 2) return true;
			case FOLLOW_FREQUENCY:
				System.out.println("FOLLOW_FREQUENCY not implemented currently");
				return false;
		}
		System.out.println("SelectionType unknown");
		return false;
	}
	
	public ArrayList<FDData> getSelectedData() {
		switch(type) {
			case FLAT:
				if(data.size() == 2) return getFLATData();
			case FOLLOW_FREQUENCY:
				System.out.println("FOLLOW_FREQUENCY not implemented currently");
				return null;
		}
		System.out.println("SelectionType unknown");
		return null;
	}
	
	public ArrayList<Integer> getSelectedFreqs() {
		ArrayList<Integer> returnVal = new ArrayList<Integer>();
		switch(type) {
			case FLAT:
				if(data.size() == 0) return returnVal;
				returnVal.add(DFTEditor.noteToFreq(data.get(0).getNote()));
				if(data.size() == 1) return returnVal;
				returnVal.add(DFTEditor.noteToFreq(data.get(1).getNote()));
				if(data.size() == 2) return returnVal;
				System.out.println("Selection.getSelectedFreqs() data size exceeded");
				return null;
			case FOLLOW_FREQUENCY:
				System.out.println("FOLLOW_FREQUENCY not implemented currently");
			return null;
		}
	System.out.println("SelectionType unknown");
	return null;
	}
	
	public ArrayList<Integer> getSelectedTimes() {
		ArrayList<Integer> returnVal = new ArrayList<Integer>();
		switch(type) {
			case FLAT:
				if(data.size() == 0) return returnVal;
				returnVal.add(data.get(0).getTime());
				if(data.size() == 1) return returnVal;
				returnVal.add(data.get(1).getTime());
				if(data.size() == 2) return returnVal;
				System.out.println("Selection.getSelectedTimes() data size exceeded");
				return null;
			case FOLLOW_FREQUENCY:
				System.out.println("FOLLOW_FREQUENCY not implemented currently");
			return null;
		}
	System.out.println("SelectionType unknown");
	return null;
	}
	
	private ArrayList<FDData> getFLATData() {
		ArrayList<FDData> returnVal = new ArrayList<FDData>();
		int startTime = data.get(0).getTime();
		int endTime = data.get(1).getTime();
		if(endTime < startTime) {
			int saveEndTime = endTime;
			endTime = startTime;
			startTime = saveEndTime;
		}
		int lowerNote = data.get(0).getNote();
		int upperNote = data.get(1).getNote();
		if(upperNote < lowerNote) {
			int saveUpperNote = upperNote;
			upperNote = lowerNote;
			lowerNote = saveUpperNote;
		}
		for(int time = startTime; time <= endTime; time++) {
			float maxAmplitude = 0.0f;
			int noteAtMaximum = -1;
			for(int note = lowerNote; note <= upperNote; note++) {
				float currentAmplitude = DFTEditor.getAmplitude(time, DFTEditor.noteToFreq(note));
				// WEAKNESS: if two or more freqs have same amplitude, highest one is used
				if(currentAmplitude >= maxAmplitude) {
					maxAmplitude = currentAmplitude;
					noteAtMaximum = note;
				}
				try {
					returnVal.add(new FDData(time, noteAtMaximum, maxAmplitude));
				} catch (Exception e) {
					System.out.println("Selection.getFLATData: error adding FDData");
				}
			}
		}
		return returnVal;
	}
	
}
