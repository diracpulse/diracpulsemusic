import java.util.ArrayList;
import java.util.TreeMap;


public class Harmonic {

	private boolean overwrite = true; // not in use currently
	private TreeMap<Integer, FDData> timeToData = new TreeMap<Integer, FDData>();
	private double maxLogAmplitude = 0.0;
	private int minNote = FDData.getMaxNote();
	private int maxNote = FDData.getMinNote();
	private byte channel = -1;

	private long harmonicID;
	
	public Harmonic(long id) {
		this.harmonicID = id;
	}
	
	public long getHarmonicID() {
		return this.harmonicID;
	}
	
	// returns FDData with harmonicID set
	public FDData addData(FDData data) {
		//System.out.println(data);
		data.setHarmonicID(this.harmonicID);
		if(this.channel == -1) this.channel = data.getChannel();
		if(data.getChannel() != channel) {
			System.out.println("Harmonic.addData(): Multiple channels in same harmonic");
			return data;
		}
		if(data.getLogAmplitude() > maxLogAmplitude) maxLogAmplitude = data.getLogAmplitude();
		if(!timeToData.containsKey(data.getTime())) {
			timeToData.put(data.getTime(), data);
			if(data.getNote() < minNote) minNote = data.getNote();
			if(data.getNote() > maxNote) maxNote = data.getNote();
			return data;
		}
		// data already exists at that time
		if(overwrite) {
			timeToData.put(data.getTime(), data);
			if(data.getNote() < minNote) minNote = data.getNote();
			if(data.getNote() > maxNote) maxNote = data.getNote();
		}
		System.out.println("Harmonic.addData(): Duplicate data at time = " + data.getTime());
		return data;
	}
	
	public boolean isSynthesized() {
		if(getMaxLogAmplitude() < DFTEditor.minLogAmplitudeThreshold) return false;
		if(getLength() < DFTEditor.minLengthThreshold) return false;
		return true;
	}
	
	public ArrayList<FDData> getAllDataRaw() {
		ArrayList<FDData> returnVal = new ArrayList<FDData>(timeToData.values());
		return returnVal;
	}
	
	public ArrayList<FDData> getAllData() {
		ArrayList<FDData> returnVal = new ArrayList<FDData>(timeToData.values());
		if(returnVal.size() == 0) return returnVal;
		if(getTaperLength() == 0) return returnVal;
		returnVal.add(getEnd());
		return returnVal;
	}
	
	public TreeMap<Integer, FDData> getAllDataInterpolated() {
		TreeMap<Integer, FDData> returnVal = new TreeMap<Integer, FDData>();
		if(timeToData.size() == 0) return returnVal;
		if(timeToData.size() == 1) {
			returnVal.put(getStartTime(), getStart());
			return returnVal;
		}
		returnVal = Interpolate.dataInterpolate(channel, timeToData);
		if(getTaperLength() == 0) return returnVal;
		returnVal.put(getEnd().getTime(), getEnd());
		return returnVal;
	}
	
	public boolean containsData(FDData data) {
		if(timeToData.containsKey(data.getTime())) return true;
		return false;
	}
	
	public byte getChannel() {
		return channel;
	}
	
	public FDData getStart() {
		return timeToData.get(timeToData.firstKey());
	}
	
	public FDData getEnd() {
		if(timeToData.isEmpty()) return null;
		FDData taperData = null;
		FDData endData = timeToData.get(timeToData.lastKey());
		int taperTime = endData.getTime() + (int) Math.ceil(getTaperLength());
		try {
			taperData = new FDData(channel, taperTime, endData.getNote(), 0.0, harmonicID);
		} catch (Exception e) {
			System.out.println("Harmonic.getEnd(): Error creating data");
		}
		return taperData;
	}
	
	public int getLength() {
		if(timeToData.isEmpty()) return 0;
		return (getEnd().getTime() - getStart().getTime());
	}

	public double getTaperLength() {
		double timeToSample = SynthTools.sampleRate * (FDData.timeStepInMillis / 1000.0);
		FDData endData = timeToData.lastEntry().getValue();
		double endLogAmp = endData.getLogAmplitude();
		double taperLength = 0;
		if(endLogAmp > 0.0) {
			double endLogFreq = (double) endData.getNote() / FDData.noteBase;
			double cycleLength = SynthTools.sampleRate / Math.pow(FDData.logBase, endLogFreq);
			//taperLength =  (int) Math.ceil(endLogAmp * cycleLength);
			taperLength = cycleLength * 2;
			taperLength /= timeToSample; 
		}
		//System.out.println("Harmonic.getTaperLength: " + taperLength);
		return taperLength;
	}

	public double getMaxLogAmplitude() {
		return maxLogAmplitude;
	}
	
	public int getAverageNote() {
		double noteSum = 0;
		double numNotes = 0;
		for(FDData data: timeToData.values()) {
			noteSum += data.getNote();
			numNotes += 1.0;
		}
		return (int) Math.round(noteSum / numNotes);
	}
	
	public int getMinNote() {
		return minNote;
	}
	
	public int getMaxNote() {
		return maxNote;
	}
	
	public int getStartTime() {
		return timeToData.firstKey();
	}
	
	public int getEndTime() {
		return getEnd().getTime();
	}
	
	public boolean containsTime(int time) {
		if(timeToData.containsKey(time)) return true;
		return false;
	}
	
	public boolean containsData(int time, int note) {
		if(timeToData.containsKey(time)) {
			if(timeToData.get(time).getNote() == note) return true;
		}
		return false;
	}
	
	public void removeData(int time) {
		if(!timeToData.containsKey(time)) {
			System.out.println("Harmonic.removeData: Data does not exist!");
			return;
		}
		timeToData.remove(time);
	}
	
	public boolean containsDataInterpolated(int time, int note) {
		TreeMap <Integer, FDData> interpolatedData = getAllDataInterpolated();
		if(interpolatedData.containsKey(time)) {
			if(interpolatedData.get(time).getNote() == note) return true;
		}
		return false;
	}

	public ArrayList<FDData> getScaledHarmonic(int deltaTime, int endTime, int deltaNote, long harmonicID) {
		TreeMap<Integer, FDData> newTimeToData = new TreeMap<Integer, FDData>();
		try {
			for(FDData data: timeToData.values()) {
				int time = data.getTime() + deltaTime;
				int note = data.getNote() + deltaNote;
				if(time > endTime) break;
				if(note >= FDData.getMaxNote()) continue;
				FDData newData = new FDData(channel, time, note, data.getLogAmplitude(), harmonicID);
				newTimeToData.put(time, newData);
			}
		} catch (Exception e) {
			System.out.println("Harmonic.getScaledHarmonic: Error creating data");
		}
		return new ArrayList<FDData>(newTimeToData.values());
	}
	
	public ArrayList<FDData> getTrimmedHarmonic(int startTime, int endTime, double playSpeed) {
		TreeMap<Integer, FDData> trimmedData = new TreeMap<Integer, FDData>();
		ArrayList<FDData> returnData = new ArrayList<FDData>();
		try {
			for(FDData data: timeToData.values()) {
				if(data.getTime() < startTime) continue;
				if(data.getTime() > endTime) break;
				trimmedData.put(data.getTime(), data);
			}
			for(FDData data: trimmedData.values()) {
				FDData newData = new FDData(channel, (int) Math.round((data.getTime() - startTime) * playSpeed), data.getNote(), data.getLogAmplitude(), data.getHarmonicID());
				returnData.add(newData);
			}
		} catch (Exception e) {
			System.out.println("Harmonic.getTrimmedHarmonic: Error creating data");
		}
		return returnData;
	}
	
	public ArrayList<FDData> getPureSineHarmonic(double logAmplitude, double playSpeed) {
		ArrayList<FDData> returnData = new ArrayList<FDData>();
		try {
			for(FDData data: getAllDataInterpolated().values()) {
				FDData newData = new FDData(channel, (int) Math.round(data.getTime() * playSpeed), data.getNote(), logAmplitude, data.getHarmonicID());
				returnData.add(newData);
			}
		} catch (Exception e) {
			System.out.println("Harmonic.getPureSineHarmonic: Error creating data");
		}
		return returnData;
	}
	
}
