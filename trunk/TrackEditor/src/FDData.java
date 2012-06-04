

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
	private double time = minTime;
	private double note = 31 * 4; // out of bounds stored at 16Hz
	private double logAmplitude = minLogAmplitude;
	private long harmonicID = 1L;
		
	public FDData(double time, double note, double logAmplitude, long id) throws Exception {
		//System.out.println("FDData: t:" + time + " n:" + note + " nf:" + noteFraction + " la:" + logAmplitude);
		if(!withinBounds((int) Math.round(time), (int) Math.round(note), logAmplitude)) {
			throw new Exception("FDData [" + time + "|" + note + "|" + logAmplitude + "]");
		}
		this.time = time;
		this.note = note;
		this.logAmplitude = logAmplitude;
		this.harmonicID = id;
	}

	public void setHarmonicID(long id) {
		this.harmonicID = id;
	}
	
	public long getHarmonicID() {
		return this.harmonicID;
	}	
	
	public int getTime() {
		return (int) Math.round(time);
	}
	
	public float getTimeAsFloat() {
		return (float) time;
	}
	
	public int getTimeInMillis() {
		return (int) Math.round(time * FDData.timeStepInMillis);
	}
	
	public int getNote() {
		return (int) Math.round(note);
	}
	
	public double getNoteComplete() {
		return note;
	}
	
	public float getNoteAsFloat() {
		return (float) note;
	}
	
	public double getLogAmplitude() {
		return logAmplitude;
	}
	
	public void setLogAmplitude(double logAmplitudeArg) throws Exception {
		if(!withinBounds((int) Math.round(time), (int) Math.round(note), logAmplitudeArg)) throw new Exception();
		logAmplitude = logAmplitudeArg;
	}
	
	public double getAmplitude() {
		return Math.pow(logBase, logAmplitude);
	}
	
	public DataType getDataType() {
		return type;
	}
	
	public double getFrequencyInHz() {
		return getFrequencyInHz(this.note);
	}
	
	private double getFrequencyInHz(double note) {
		double exponent = note / noteBase;
		return Math.pow(2.0, exponent);
	}
	
	public static int getMaxNote() {
		return (int) Math.round(Math.log(maxFrequencyInHz)/Math.log(2.0) * (double) noteBase);
	}
	
	public static int getMinNote() {
		return (int) Math.round(Math.log(minFrequencyInHz)/Math.log(2.0) * (double) noteBase);
	}

	private boolean withinBounds(int time, double note, double logAmplitude) {
		if(time < minTime) return false;
		if(time > maxTime) return false;
		if(logAmplitude < minLogAmplitude) return false;
		if(logAmplitude > maxLogAmplitude) return false;
		double frequency = getFrequencyInHz(note);
		if(frequency < minFrequencyInHz) return false;
		if(frequency > maxFrequencyInHz) return false;
		return true;
	}
	
	public String toString() {
		return "[" + getTime() + "|" + getNote() + "|" + getLogAmplitude() + "]";
	}
	
}
