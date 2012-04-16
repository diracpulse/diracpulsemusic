import java.util.ArrayList;
import java.util.TreeMap;


public class Harmonic {

	private boolean overwrite = true; // not in use currently
	private TreeMap<Integer, FDData> timeToData = new TreeMap<Integer, FDData>();
	private double maxLogAmplitude = 0.0;
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
			return data;
		}
		// data already exists at that time
		if(overwrite) {
			timeToData.put(data.getTime(), data);
		}
		System.out.println("Harmonic.addData(): Duplicate data at time = " + data.getTime());
		return data;
	}
	
	public boolean isSynthesized() {
		if(getMaxLogAmplitude() < DFTEditor.minLogAmplitudeThreshold) return false;
		if(getLength() < DFTEditor.minLengthThreshold) return false;
		return true;
	}
	
	public ArrayList<FDData> getAllData() {
		ArrayList<FDData> returnVal = new ArrayList<FDData>(timeToData.values());
		if(returnVal.size() == 0) return returnVal;
		if(getTaperLength() == 0) return returnVal;
		returnVal.add(getEnd());
		return returnVal;
	}
	
	// Sythesis seems to work OK without interpolating note values
	public ArrayList<FDData> getAllDataInterpolated() {
		return SynthTools.interpolateAmplitude(getAllData());
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
		return (getEnd().getTime() - getStart().getTime());
	}

	public double getTaperLength() {
		FDData endData = timeToData.lastEntry().getValue();
		double endLogAmp = endData.getLogAmplitude();
		double taperLength = 0;
		if(endLogAmp > 0.0) {
			double endLogFreq = endData.getNoteComplete() / FDData.noteBase;
			double cycleLength = SynthTools.sampleRate / Math.pow(FDData.logBase, endLogFreq);
			//taperLength =  (int) Math.ceil(endLogAmp * cycleLength);
			taperLength = cycleLength * 2;
			taperLength /= SynthTools.timeToSample; 
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

	public int getStartTime() {
		return timeToData.firstKey();
	}
	
	public int getEndTime() {
		return timeToData.lastKey();
	}
	
}
