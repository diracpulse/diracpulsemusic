

public class FDData {
	
	public static final int timeStepInMillis = 5; // time in secs = this.time * timeStepInMillis / 1000
	public static final int noteBase = 31; // freq in Hz = 2.0^((this.note + this.noteFraction) / noteBase)
	public static final double logBase = 2.0; // amplitude = logBase ^ logAmplitude
	
	//Data Bounds
	public static final int minTime = 0;
	public static final int maxTime = 10 * 60 * (1000 / timeStepInMillis);
	public static final double minLogAmplitude = 0.0;
	public static final double maxLogAmplitude = 24.0;
	public static final double minFrequencyInHz = 20.0;
	public static final double maxFrequencyInHz = 20000.0;
	
	public enum DataType {
		FUNDAMENTAL, HARMONIC, FORMANT, PERCUSSIVE, GRAIN
	}
		
	private DataType type = DataType.FUNDAMENTAL;
	private int time = minTime;
	private int note = 31 * 4; // out of bounds stored at 16Hz
	private double noteFraction = 0.0; // frequency = 2^(note/31) + 2^(noteFraction/31);
	private double logAmplitude = minLogAmplitude;
	private long HarmonicID = 1L;
	
	public FDData(int time, double noteComplete, double logAmplitude) throws Exception {
		int note = (int) Math.round(noteComplete);
		double noteFraction = noteComplete - note;
		//System.out.println("FDData: t:" + time + " n:" + note + " nf:" + noteFraction + " la:" + logAmplitude);
		if(!withinBounds(time, note, noteFraction, logAmplitude)) throw new Exception();
		this.time = time;
		this.note = note;
		this.noteFraction = noteFraction;
		this.logAmplitude = logAmplitude;
	}
	
	// This contructor is used to input file data
	public FDData(int time, int note, float logAmplitude) throws Exception {
		//System.out.println("FDData: t:" + time + " n:" + note + " nf:" + noteFraction + " la:" + logAmplitude);
		if(!withinBounds(time, note, 0.0, logAmplitude)) {
			throw new Exception("FDData [" + time + "|" + note + "|" + logAmplitude + "]");
		}
		this.time = time;
		this.note = note;
		this.noteFraction = 0.0;
		this.logAmplitude = logAmplitude;
	}
	
	public FDData(int time, int note, double noteFraction, double logAmplitude) throws Exception {
		//System.out.println("FDData: t:" + time + " n:" + note + " nf:" + noteFraction + " la:" + logAmplitude);
		if(!withinBounds(time, note, noteFraction, logAmplitude)) throw new Exception();
		this.time = time;
		this.note = note;
		this.noteFraction = noteFraction;
		this.logAmplitude = logAmplitude;
	}
	
	public FDData(int time, int note, double noteFraction, double logAmplitude, DataType type) throws Exception {
		if(!withinBounds(time, note, noteFraction, logAmplitude)) throw new Exception();
		this.time = time;
		this.note = note;
		this.noteFraction = noteFraction;
		this.logAmplitude = logAmplitude;
		this.type = type;
	}
	
	public void setHarmonicID(long id) {
		this.HarmonicID = id;
	}
	
	public long getHarmonicID() {
		return this.HarmonicID;
	}	
	
	public int getTime() {
		return time;
	}
	
	public int getTimeInMillis() {
		return time * FDData.timeStepInMillis;
	}
	
	public int getNote() {
		return note;
	}
	
	public double getNoteFraction() {
		return noteFraction;
	}
	
	public double getNoteComplete() {
		return note + noteFraction;
	}
	
	public double getLogAmplitude() {
		return logAmplitude;
	}
	
	public double getAmplitude() {
		return Math.pow(logBase, logAmplitude);
	}
	
	public DataType getDataType() {
		return type;
	}
	
	public double getFrequencyInHz() {
		return getFrequencyInHz(this.note, this.noteFraction);
	}
	
	private double getFrequencyInHz(int note, double noteFraction) {
		double exponent = (note + noteFraction) / noteBase;
		double frequency = Math.pow(2.0, exponent);
		return frequency;
	}

	private boolean withinBounds(int time, int note, double noteFraction, double logAmplitude) {
		if((noteFraction < -0.5) || (noteFraction > 0.5)) return false;
		if(time < minTime) return false;
		if(time > maxTime) return false;
		if(logAmplitude < minLogAmplitude) return false;
		if(logAmplitude > maxLogAmplitude) return false;
		double frequency = getFrequencyInHz(note, noteFraction);
		if(frequency < minFrequencyInHz) return false;
		if(frequency > maxFrequencyInHz) return false;
		return true;
	}
	
	public String toString() {
		return "[" + getTime() + "|" + getNote() + "|" + getLogAmplitude() + "]";
	}
	
}
