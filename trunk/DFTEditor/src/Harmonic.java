import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;


public class Harmonic {

	private int minTimeStep = 4; // 4 * 5.0ms = 20ms
	private double minCycles = 2;
	private boolean applyTaper = true; // not in use currently
	private boolean overwrite = true; // not in use currently
	private TreeMap<Integer, FDData> timeToData = new TreeMap<Integer, FDData>();
	
	public Harmonic() {
	}
	
	// returns true if data already exists at that time
	public boolean addData(FDData data) {
		//System.out.println(data);
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
	
	public ArrayList<FDData> getAllData(int pass) {
		if(pass == 0) {
			flattenHarmonic();
			return new ArrayList<FDData>(timeToData.values());
		}
		if(pass == 1) {
			if(isNoise() == true) return new ArrayList<FDData>();
			return new ArrayList<FDData>(timeToData.values());
		}
		System.out.println("Harmonic.getData(int pass): pass out of bounds:" + pass);
		return null;
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
		double currentPhase = 0.0;
		for(int currentSample = 0; currentSample < ampEnvelope.getNumSamples(); currentSample++) {
			double amplitude = ampEnvelope.getSample(currentSample);
			double frequency = freqEnvelope.getSample(currentSample);
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
	
	private void flattenHarmonic() {
		if(timeToData.size() < 2) return;
		double prevNote = -1;
		double deltaNoteSum = 0.0;
		double noteSum = -1.0;
		boolean firstPass = true;
		for(int time: timeToData.keySet()) {
			double note = timeToData.get(time).getNote();
			if(firstPass) {
				prevNote = note;
				noteSum = note;
				firstPass = false;
				continue; // might not need this
			} else {
				noteSum += note;
				deltaNoteSum += note - prevNote;
				prevNote = note;
			}
		}
		if(Math.abs(deltaNoteSum) < 31.0) {
			try {
				int averageNote = (int) Math.round(noteSum / timeToData.size());
				for(int time: timeToData.keySet()) {
					FDData data = timeToData.get(time);
					float logAmp = (float) data.getLogAmplitude();
					timeToData.put(time, new FDData(time, averageNote, logAmp));
				}
			} catch (Exception e) {
				System.out.println("Error in Harmonic.flattenHarmonic(): " + e.getMessage());
			}
		}
	}
	
	public boolean isNoise() {
		if(getPCMData(false) == null) return false;
		return true; // if here, dummy array returned
	}

	public boolean minimumCyclesExceeded(double minLogFreq, double endTime) {
		double cycleLength = SynthTools.sampleRate / Math.pow(FDData.logBase, minLogFreq);
		double lengthInSamples = endTime * FDData.timeStepInMillis * (SynthTools.sampleRate / 1000.0);
		double lengthInCycles = lengthInSamples / cycleLength;
		if(lengthInCycles <= minCycles) return false;
		return true;
	}
	
	public int getTaperLength() {
		FDData endData = timeToData.lastEntry().getValue();
		double endLogAmp = endData.getLogAmplitude();
		int taperLength = 0;
		if(endLogAmp > 0.0) {
			double endLogFreq = endData.getNoteComplete() / FDData.noteBase;
			double cycleLength = SynthTools.sampleRate / Math.pow(FDData.logBase, endLogFreq);
			taperLength =  (int) Math.ceil(endLogAmp * cycleLength);
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
	
}