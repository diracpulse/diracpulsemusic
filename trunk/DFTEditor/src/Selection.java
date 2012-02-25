import java.util.ArrayList;


public class Selection {
	
	public enum Type {
		FLAT, FOLLOW_FREQUENCY;
	}
	
	private ArrayList<FDData> data;
	
	Type type = Type.FLAT;
	
	public Selection(Type type) {
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
				return false;
			case FOLLOW_FREQUENCY:
				System.out.println("FOLLOW_FREQUENCY not implemented currently");
				return false;
		}
		System.out.println("Selection.selectionComplete() SelectionType unknown");
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
		System.out.println("Selection.getSelectedData() SelectionType unknown");
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
			}
			try {
				returnVal.add(new FDData(time, noteAtMaximum, maxAmplitude));
			} catch (Exception e) {
				System.out.println("Selection.getFLATData: error adding FDData");
			}
		}
		return returnVal;
	}
	
}
