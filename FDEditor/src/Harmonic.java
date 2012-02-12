import java.util.TreeMap;


public class Harmonic {

	private boolean applyTaper = false;
	private boolean overwrite = false;
	private TreeMap<Integer, FDData> timeToData = new TreeMap<Integer, FDData>();
	
	public Harmonic() {
		
	}
	
	// returns true if data already exists at that time
	public boolean addData(FDData data) {
		if(!timeToData.containsKey(data.getTime())) {
			timeToData.put(data.getTime(), data);
			return false;
		}
		// data already exists at that time
		if(overwrite) {
			timeToData.put(data.getTime(), data);
		}
		System.out.println("Harmonic.addData(): Duplicate data at time = " + data.getTime());
		return true;
	}
	
	public boolean containsData() {
		return timeToData.isEmpty();
	}
	
	public int getStartSampleOffset() {
		if(!containsData()) return 0;
		return (int) Math.round(timeToData.firstKey() * SynthTools.sampleRate);
	}
	
	public int getLength() {
		if(!containsData()) return 0;
		return (int) Math.round(timeToData.lastKey() * SynthTools.sampleRate);
	}

	// this is here so there's no warning
	public boolean getApplyTaper() {
		return applyTaper;	
	}

	public void setApplyTaper(boolean applyTaper) {
		this.applyTaper = applyTaper;	
	}
	
	public String toString() {
		StringBuffer out = new StringBuffer();
		if(timeToData.isEmpty()) return "\n\nEMPTY\n\n";
		out.append("\n\nSTART" + timeToData.firstKey() + "\n");
		for(FDData data: timeToData.values()) {
			out.append(" [" + data.getTime() + " " + data.getNote() + " " + data.getLogAmplitude() + "]\n");
		}
		out.append("END" + timeToData.lastKey() + "\n\n");
		return out.toString();
	}
	
}
