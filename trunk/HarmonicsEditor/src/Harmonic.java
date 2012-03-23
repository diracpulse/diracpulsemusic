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
	
	public boolean containsData() {
		return !timeToData.isEmpty();
	}
	
	public int getStartSampleOffset() {
		if(!containsData()) return 0;
		return (int) Math.round(timeToData.firstKey() * SynthTools.timeToSample);
	}
	
	public int getEndSampleOffset() {
		return getStartSampleOffset() + getLength();
	}
	
	public int getLength() {
		if(!containsData()) return 0;
		int startSample = getStartSampleOffset();
		int endSample = (int) Math.round(timeToData.lastKey() * SynthTools.timeToSample);
		int length = endSample - startSample;
		length += getTaperLength() * SynthTools.timeToSample;
		return length;
	}
	
	public ArrayList<FDData> getAllData() {
		return new ArrayList<FDData>(timeToData.values());
	}
	
	public ArrayList<FDData> getAllDataInterpolated() {
		return LogLinear.dataInterpolate(getAllData());
	}
	
	public boolean containsData(FDData data) {
		if(timeToData.containsKey(data.getTime())) return true;
		return false;
	}
	
	public FDData getStart() {
		return timeToData.get(timeToData.firstKey());
	}
	
	public FDData getEnd() {
		return timeToData.get(timeToData.lastKey());
	}	
	
	// used to avoid null pointer for small harmonics
	public Double[] getDummyArray() {
		Double[] returnVal = new Double[3];
		returnVal[0] = 0.0;
		returnVal[1] = 0.0;
		returnVal[2] = 0.0;
		return returnVal;
	}
	
	public Double[] getPCMData() {
		return getPCMData(true);
	}
	
	// If synthesize == false, returns null if harmonic WILL BE systhesized, else returns dummyArray
	private Double[] getPCMData(boolean synthesize) {
		if(timeToData.size() < minTimeStep) {
			//System.out.println("Harmonics.getPCMData: number of data points < 4");
			return getDummyArray();
		}
		if(maxLogAmplitude < HarmonicsEditor.minLogAmplitudeThreshold) return getDummyArray();
		double minLogFreq = 16.0; // 65kHz
		ArrayList<Double> sampleTimes = new ArrayList<Double>();
		ArrayList<Double> logAmps = new ArrayList<Double>();
		ArrayList<Double> logFreqs = new ArrayList<Double>();
		ArrayList<Double> PCMData = new ArrayList<Double>();
		double startSample = -1;
		for(int time: timeToData.keySet()) {
			if(startSample == -1) startSample = time;
			sampleTimes.add(time - startSample);
			logAmps.add(timeToData.get(time).getLogAmplitude());
			double logFreq = timeToData.get(time).getNoteComplete() / FDData.noteBase;
			if(logFreq < minLogFreq) minLogFreq = logFreq;
			logFreqs.add(logFreq);
		}
		// NOTE logAmps.size() = logFreqs.size() = sampleTimes.size()
		// Parallel arrays are fully contained within function so they should be OK
		double endTime = sampleTimes.get(sampleTimes.size() - 1);
		double endLogFreq = logFreqs.get(sampleTimes.size() - 1);
		if (!minimumCyclesExceeded(minLogFreq, endTime)) return getDummyArray();
		if(synthesize == false) return null;
		// AT THIS POINT HARMONIC WILL BE SYNTHESIZED
		if(getTaperLength() > 0) {
			// Apply taper to avoid "pop" at end of harmonic
			sampleTimes.add(endTime + getTaperLength());
			logAmps.add(0.0);
			logFreqs.add(endLogFreq);
		}
		Double[] sampleTimesArray = new Double[sampleTimes.size()];
		Double[] logAmpsArray = new Double[sampleTimes.size()];
		Double[] logFreqsArray = new Double[sampleTimes.size()];
		sampleTimesArray = sampleTimes.toArray(sampleTimesArray);
		logAmpsArray = logAmps.toArray(logAmpsArray);
		logFreqsArray = logFreqs.toArray(logFreqsArray);
		LogLinear ampEnvelope = new LogLinear(sampleTimesArray, logAmpsArray);
		LogLinear freqEnvelope = new LogLinear(sampleTimesArray, logFreqsArray);
		LogLinear vibrato = null;
		if(useVibrato) vibrato = getVibrato(sampleTimesArray, logFreqsArray);
		double currentPhase = 0.0;
		for(int currentSample = 0; currentSample < ampEnvelope.getNumSamples(); currentSample++) {
			double amplitude = ampEnvelope.getSample(currentSample);
			double frequency = freqEnvelope.getSample(currentSample);
			if(useVibrato) {
				double vibratoVal = vibrato.getSample(currentSample);
				frequency *= vibratoVal;
			}
			double deltaPhase = (frequency / SynthTools.sampleRate) * SynthTools.twoPI;
			PCMData.add(amplitude * Math.sin(currentPhase));
			//System.out.println(PCMData.get(PCMData.size() - 1));
			//System.out.println(currentPhase);
			currentPhase += deltaPhase;
			if(currentPhase > Math.PI) currentPhase -= 2.0 * Math.PI;
		}
		Double[] returnVal = new Double[PCMData.size()];
		returnVal = PCMData.toArray(returnVal);
		return returnVal;
	}
	
	private static LogLinear getVibrato(Double[] sampleTimes, Double[] logFreqs) {
		double maxVibrato = 1.0 / (FDData.noteBase);
		ArrayList<Double> vibratoTimes = new ArrayList<Double>();
		ArrayList<Double> vibratoValues = new ArrayList<Double>();
		double minLogFreq = logFreqs[0];
		for(double logFreq: logFreqs) if(logFreq < minLogFreq) minLogFreq = logFreq;
		double minFreqInHz = Math.pow(2.0, minLogFreq);
		double samplesPerCycle = SynthTools.sampleRate / minFreqInHz;
		double timePerCycle = samplesPerCycle / SynthTools.timeToSample;
		double timeStep = timePerCycle * 4.0;
		// System.out.println("minFreqInHz" + minFreqInHz + " timePerCycle " + timePerCycle);
		// make sure we leave at least one cycle length at the end
		double endTime = sampleTimes[sampleTimes.length - 1] - timeStep;
		// System.out.println("EndTime" + endTime);
		for(double time = 0; time < endTime; time += timeStep) {
			double value = (Math.random() - 0.5) * 2.0 * maxVibrato;
			vibratoTimes.add(time);
			vibratoValues.add(value);
			//System.out.println(time + " " + value);
		}
		// harmonic should be at least one cycle, but just in case
		if(vibratoTimes.size() < 1) {
			//System.out.println("Error in dephase");
			vibratoTimes.add(0.0);
			vibratoValues.add(0.0);
		}
		vibratoTimes.add(sampleTimes[sampleTimes.length - 1]);
		vibratoValues.add(0.0);
		Double[] vibratoValuesArray = new Double[vibratoValues.size() - 1];
		Double[] vibratoTimesArray = new Double[vibratoTimes.size() - 1];
		vibratoValuesArray = vibratoValues.toArray(vibratoValuesArray);
		vibratoTimesArray = vibratoTimes.toArray(vibratoTimesArray);
		return new LogLinear(vibratoTimesArray, vibratoValuesArray);
	}
	
	public void flattenHarmonic() {
		TreeMap<Integer, FDData> newTimeToData = new TreeMap<Integer, FDData>();
		if(timeToData.size() < 2) return;
		int noteSum = 0;
		int maxNote = 0;
		int minNote = Integer.MAX_VALUE;
		for(FDData data: timeToData.values()) {
			int note = data.getNote();
			if(note < minNote) minNote = note;
			if(note > maxNote) maxNote = note;
			noteSum += note;
		}
		if((maxNote - minNote) > 31) return;
		int averageNote = noteSum / (timeToData.size());
		for(int time: timeToData.keySet()) {
			try {
				FDData data = timeToData.get(time);
				float logAmp = (float) data.getLogAmplitude();
				FDData newData = new FDData(time, averageNote, logAmp);
				newData.setHarmonicID(this.harmonicID);
				newTimeToData.put(time, newData);
			} catch (Exception e) {
				System.out.println("Error in Harmonic.flattenHarmonic(): " + e.getMessage());
			}
		}
		timeToData = newTimeToData;
	}
	
	public boolean isSynthesized() {
		if(getPCMData(false) == null) return true;
		return false; // if here, dummy array returned
	}

	public boolean minimumCyclesExceeded(double minLogFreq, double endTime) {
		double cycleLength = SynthTools.sampleRate / Math.pow(FDData.logBase, minLogFreq);
		double lengthInSamples = endTime * FDData.timeStepInMillis * (SynthTools.sampleRate / 1000.0);
		double lengthInCycles = lengthInSamples / cycleLength;
		if(lengthInCycles <= minCycles) return false;
		return true;
	}
	
	public double getTaperLength() {
		FDData endData = timeToData.lastEntry().getValue();
		double endLogAmp = endData.getLogAmplitude();
		double taperLength = 0;
		if(endLogAmp > 0.0) {
			double endLogFreq = endData.getNoteComplete() / FDData.noteBase;
			double cycleLength = SynthTools.sampleRate / Math.pow(FDData.logBase, endLogFreq);
			//taperLength =  (int) Math.ceil(endLogAmp * cycleLength);
			taperLength = cycleLength;
			taperLength /= SynthTools.timeToSample; 
		}
		//System.out.println("Harmonic.getTaperLength: " + taperLength);
		return taperLength;
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
	
	public int getAverageNote() {
		double noteSum = 0;
		double numNotes = 0;
		for(FDData data: timeToData.values()) {
			noteSum += data.getNote();
			numNotes += 1.0;
		}
		return (int) Math.round(noteSum / numNotes);
	}
	
}
