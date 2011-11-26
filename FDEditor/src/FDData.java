

public class FDData {
		
	public enum DataType {
		FUNDAMENTAL, HARMONIC, FORMANT
	}
		
	DataType type = DataType.FUNDAMENTAL;
	int time;
	int note;
	double noteFraction = 0.0; // frequency = 2^(note/31) + 2^(noteFraction/31);
	double logAmplitude;
	
	public FDData(int time, int note, double logAmplitude) {
		this.time = time;
		this.note = note;
		this.logAmplitude = logAmplitude;
	}
	
	public FDData(int time, int note, double logAmplitude, DataType type) {
		this.time = time;
		this.note = note;
		this.logAmplitude = logAmplitude;
		this.type = type;
	}
	
	int getTime() {
		return time;
	}
	
	int getNote() {
		return note;
	}
	
	double getNoteFraction() {
		return noteFraction;
	}
	
	void setNoteFraction(double noteFraction) {
		this.noteFraction = noteFraction; 
	}
		
}
