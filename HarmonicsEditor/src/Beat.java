
public class Beat {
	
	int baseNote;
	int[] chords;
	int duration;
	
	boolean modifyBaseNote = true;
	boolean modifyChords = true;
	boolean modifyDuration = true;
	
	public Beat(int baseNote, int[] chords, int duration) {
		this.baseNote = baseNote;
		this.chords= chords;
		this.duration = duration;
	}
	
	public int getBaseNote() {
		return baseNote;
	}
	
	public int[] getChords() {
		return chords;
	}
	
	public int getDuration() {
		return duration; 
	}

	public String toString() {
		StringBuffer returnVal = new StringBuffer();
		returnVal.append(baseNote);
		for(double chordVal: chords) returnVal.append(":" + chordVal);
		returnVal.append(":" + duration);
		return returnVal.toString();
	}
}
