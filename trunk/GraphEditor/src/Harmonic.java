import java.util.ArrayList;
import java.util.TreeMap;


public class Harmonic {

	private int minTimeStep = 1; // 4 * 5.0ms = 20ms
	private double minCycles = 2;
	private boolean applyTaper = true; // not in use currently
	private boolean overwrite = true; // not in use currently
	private boolean useVibrato = false;
	private TreeMap<Integer, FDData> timeToData = new TreeMap<Integer, FDData>();
	private double maxLogAmplitude = 0.0;
	private int minNote = FDData.getMaxNote();
	private int maxNote = FDData.getMinNote();
	//
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
		//System.out.println("Harmonic.addData(): Duplicate data at time = " + data.getTime());
		System.out.print("|" + data.getTime() + ":" + data.getNote() + ":" + (float) data.getLogAmplitude() + "|");
		return data;
	}
	
	public boolean containsData() {
		return !timeToData.isEmpty();
	}
	
	public int getStartSampleOffset() {
		if(!containsData()) return 0;
		return (int) Math.round(timeToData.firstKey() * SynthTools.timeToSample);
	}
	
	public int getEndSampleOffset() {
		return getStartSampleOffset() + getLengthInSamples();
	}
	
	public int getLengthInSamples() {
		if(!containsData()) return 0;
		int startSample = getStartSampleOffset();
		int endSample = (int) Math.round(timeToData.lastKey() * SynthTools.timeToSample);
		int length = endSample - startSample;
		length += getTaperLength() * SynthTools.timeToSample;
		return length;
	}
	
	public ArrayList<FDData> getAllData() {
		ArrayList<FDData> returnVal = new ArrayList<FDData>(timeToData.values());
		if(returnVal.size() == 0) return returnVal;
		if(getTaperLength() == 0) return returnVal;
		returnVal.add(getEnd());
		return returnVal;
	}
	
	public ArrayList<FDData> getAllDataInterpolated() {
		return Interpolate.dataInterpolate(getAllData());
	}
	
	public boolean containsData(FDData data) {
		if(timeToData.containsKey(data.getTime())) return true;
		return false;
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
			taperData = new FDData(taperTime, endData.getNote(), 0.0, harmonicID);
		} catch (Exception e) {
			System.out.println("Harmonic.getEnd(): Error creating data");
		}
		return taperData;
	}
	
	public int getLength() {
		return getEnd().getTime() - getStart().getTime();
	}
	
	public double getTaperLength() {
		FDData endData = timeToData.lastEntry().getValue();
		double endLogAmp = endData.getLogAmplitude();
		double taperLength = 0;
		if(endLogAmp > 0.0) {
			double endLogFreq = endData.getNoteAsDouble() / FDData.noteBase;
			double cycleLength = SynthTools.sampleRate / Math.pow(FDData.logBase, endLogFreq);
			//taperLength =  (int) Math.ceil(endLogAmp * cycleLength);
			taperLength = cycleLength * 2;
			taperLength /= SynthTools.timeToSample; 
		}
		//System.out.println("Harmonic.getTaperLength: " + taperLength);
		return taperLength;
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
	
	public double getMaxLogAmplitude() {
		return maxLogAmplitude;
	}
	
	public int getMinNote() {
		return minNote;
	}
	
	public int getMaxNote() {
		return maxNote;
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

	public int getStartTime() {
		return timeToData.firstKey();
	}
	
	public int getEndTime() {
		return timeToData.lastKey();
	}
	
	public void setAllHarmonicIDsEqual() {
		for(FDData data: timeToData.values()) {
			data.setHarmonicID(harmonicID);
		}
	}
	
	public void adjustAmplitudes(double deltaAmplitude) {
		for(int time: timeToData.keySet()) {
			FDData data = timeToData.get(time);
			double logAmplitude = data.getLogAmplitude();
			logAmplitude += deltaAmplitude;
			if(logAmplitude < 0.0) logAmplitude = 0.0;
			if(logAmplitude > FDData.maxLogAmplitude) logAmplitude = FDData.maxLogAmplitude;
			FDData newData = null;
			try {
				newData = new FDData(data.getTime(), data.getNote(), logAmplitude, data.getHarmonicID());
			} catch (Exception e) {
				System.out.println("Harmonics.adjustAmplitudes: error creating data");
			}
			timeToData.put(time, newData);
		}
	}
	
	public void addCompressionWithLimiter(double ratio, double maxLogAmplitude) {
		for(FDData data: timeToData.values()) {
			double logAmplitude = data.getLogAmplitude();
			if(logAmplitude > maxLogAmplitude) logAmplitude = maxLogAmplitude;
			logAmplitude += (maxLogAmplitude - logAmplitude) / ratio;
			try {
				data.setLogAmplitude(logAmplitude);
			} catch (Exception e) {
				System.out.println("Harmonic.addCompressionWithLimiter: Error data.setLogAmplitude(logAmplitude)");
			}
		}
	}
	
	public ArrayList<FDData> getScaledAverageNote(int newAverageNote) {
		TreeMap<Integer, FDData> newTimeToData = new TreeMap<Integer, FDData>();
		int deltaNote = newAverageNote - getAverageNote();
		try {
			for(FDData data: timeToData.values()) {
				int note = data.getNote() + deltaNote;
				if(note >= FDData.getMaxNote()) continue;
				FDData newData = new FDData(data.getTime(), note, data.getLogAmplitude(), harmonicID);
				newTimeToData.put(data.getTime(), newData);
			}
		} catch (Exception e) {
			System.out.println("Harmonic.getScaledAverageNote: Error creating data");
		}
		return new ArrayList<FDData>(newTimeToData.values());
	}
	
	public ArrayList<FDData> getScaledHarmonic(int deltaTime, int endTime, int deltaNote, long harmonicID) {
		TreeMap<Integer, FDData> newTimeToData = new TreeMap<Integer, FDData>();
		try {
			for(FDData data: timeToData.values()) {
				int time = data.getTime() + deltaTime;
				int note = data.getNote() + deltaNote;
				if(time > endTime) break;
				if(note >= FDData.getMaxNote()) continue;
				FDData newData = new FDData(time, note, data.getLogAmplitude(), harmonicID);
				newTimeToData.put(time, newData);
			}
		} catch (Exception e) {
			System.out.println("Harmonic.getScaledHarmonic: Error creating data");
		}
		return new ArrayList<FDData>(newTimeToData.values());
	}
	
	public ArrayList<FDData> getTrimmedHarmonic(int startTime, int endTime) {
		TreeMap<Integer, FDData> trimmedData = new TreeMap<Integer, FDData>();
		ArrayList<FDData> returnData = new ArrayList<FDData>();
		try {
			for(FDData data: timeToData.values()) {
				if(data.getTime() < startTime) continue;
				if(data.getTime() > endTime) break;
				trimmedData.put(data.getTime(), data);
			}
			for(FDData data: trimmedData.values()) {
				FDData newData = new FDData(data.getTime() - startTime, data.getNote(), data.getLogAmplitude(), data.getHarmonicID());
				returnData.add(newData);
			}
		} catch (Exception e) {
			System.out.println("Harmonic.getTrimmedHarmonic: Error creating data");
		}
		return returnData;
	}
	
	public ArrayList<FDData> getPureSineHarmonic(double logAmplitude) {
		ArrayList<FDData> returnData = new ArrayList<FDData>();
		try {
			for(FDData data: timeToData.values()) {
				FDData newData = new FDData(data.getTime(), data.getNote(), logAmplitude, data.getHarmonicID());
				returnData.add(newData);
			}
		} catch (Exception e) {
			System.out.println("Harmonic.getPureSineHarmonic: Error creating data");
		}
		return returnData;
	}
	
	public void flattenRegion(int startTime, int endTime) {
		int note = (timeToData.get(startTime).getNote() + timeToData.get(endTime).getNote()) / 2;
		for(int time = startTime; time <= endTime; time++) {
			FDData data = timeToData.get(time);
			FDData newData = null;
			try {
				newData = new FDData(time, note, data.getLogAmplitude(), data.getHarmonicID());
			} catch (Exception e) {
				System.out.println("Harmonic.flattenRegion error creating data");
			}
			timeToData.put(time, newData);
		}
	}
	
}
