package main;

public class FDData {
	
	public static int timeStepInMillis = 5; // time in secs = this.time * timeStepInMillis / 1000
	public static int noteBase = 53; // freq in Hz = 2.0^((this.note + this.noteFraction) / noteBase)
	public static final double logBase = 2.0; // amplitude = logBase ^ logAmplitude
	public static final int bitDepth = 16;
	
	//Data Bounds
	public static final int minTime = 0;
	public static final int maxTime = 10 * 60 * (1000 / timeStepInMillis);
	public static final double minLogAmplitude = -1.0 * (bitDepth + 1);
	public static final double maxLogAmplitude = 24.0f;
	public static final double minFrequencyInHz = DFT.minFreqHz;
	public static final double maxFrequencyInHz = DFT.maxFreqHz;
	
	public enum DataType {
		FUNDAMENTAL, HARMONIC, FORMANT, PERCUSSIVE, GRAIN
	}
	
	public enum Channel {
		LEFT, RIGHT;
	}
		
	private DataType type = DataType.FUNDAMENTAL;
	private int time = minTime;
	private short note = (short) (noteBase * 4); // out of bounds stored at 16Hz
	private double noteFraction = 0.0f; // frequency = 2^(note/31) + 2^(noteFraction/31);
	private double logAmplitude = minLogAmplitude;
	private long harmonicID = 1L;
	private Channel channel = null;
	
	public FDData(Channel channel, int time, int note, double logAmplitude, long id) throws Exception {
		//System.out.println("FDData: t:" + time + " n:" + note + " nf:" + noteFraction + " la:" + logAmplitude);
		if(!withinBounds(time, note, 0.0, logAmplitude)) {
			throw new Exception("FDData [" + time + "|" + note + "|" + logAmplitude + "]");
		}
		this.channel = channel;
		this.time = time;
		this.note = (short) note;
		this.noteFraction = 0.0;
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
		return time;
	}
	
	public int getTimeInMillis() {
		return time * FDData.timeStepInMillis;
	}
	
	public int getNote() {
		return note;
	}

	public Channel getChannel() {
		return channel;
	}
	
	public byte getChannelAsByte() {
		if(this.channel == Channel.LEFT) return 0;
		if(this.channel == Channel.RIGHT) return 1;
		return -1;
	}
	
	public static Channel byteToChannel(byte channelByte) {
		if(channelByte == 0) return Channel.LEFT;
		if(channelByte == 1) return Channel.RIGHT;
		return null;
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
	
	public static int getMaxNote() {
		return (int) Math.round(Math.log(maxFrequencyInHz)/Math.log(2.0) * (double) noteBase);
	}
	
	public static int getMinNote() {
		return (int) Math.round(Math.log(minFrequencyInHz)/Math.log(2.0) * (double) noteBase);
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
		return "[" + getTime() + "|" + getNote() + "|" + getLogAmplitude() + "|" + getHarmonicID() + "]";
	}
	
}
