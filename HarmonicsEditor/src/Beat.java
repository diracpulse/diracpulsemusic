
public class Beat {
	
	int baseNote;
	double[] chord;
	int duration;
	
	public Beat(int baseNote, double[] chord, int duration) {
		this.baseNote = baseNote;
		this.chord = new double[chord.length];
		for(int index = 0; index < chord.length; index++) this.chord[index] = chord[index];
		this.duration = duration;
	}
	
	public double getBaseNote() {
		return baseNote;
	}
	
	public double[] getChord() {
		return chord;
	}
	
	public int getDuration() {
		return duration; 
	}
	
	public String toString() {
		StringBuffer returnVal = new StringBuffer();
		returnVal.append(baseNote);
		for(double chordVal: chord) returnVal.append(":" + chordVal);
		returnVal.append(":" + duration);
		return returnVal.toString();
	}
}
