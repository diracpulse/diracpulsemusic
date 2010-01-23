import java.util.ArrayList;


public class GenerateWavelets {

	public static class WaveletParam {
		
		private int lengthInSamples;
		private double radianFreq;
		
		WaveletParam(double radianFreq, int lengthInSamples) {
			this.radianFreq = radianFreq;
			this.lengthInSamples = lengthInSamples;
		}
		
		public int getLength() {
			return lengthInSamples;
		}
		
		public double getFreq() {
			return radianFreq;
		}
		
		public String toString() {
			return new String(radianFreq + " " + lengthInSamples);
		}
		
	}
	
	final double gaussianConstant = 2.0;
	final double onePI = Math.PI;
	final static double twoPI = Math.PI * 2.0;
	private static final int MAXFREQUENCIES = 31 * 16;

	static double samplingRate = 44100.0;
	static double cyclesPerWindow = 45.22540955090449;
	static double taperPerOctave = Math.sqrt(2.0);
	double samplesPerStep = 220.5; // 5ms
	double notesPerOctave = 31.0;
	int maxDFTLength = 0;
	int inputFileLength = 0; 
	int maxCenterIndex = 0;
	int calcDFT = 0;
	int stepIndex = 0;
	
    public static void printParams()
    {
    	for(WaveletParam wp: InitFrequencies(18322.012048779428, 1000.0, 20.0)) {
    		System.out.println(wp);
    	}
    }
    
    static ArrayList<WaveletParam> InitFrequencies(double upperFreq, double centerFreq, double lowerFreq) {
    	ArrayList<WaveletParam> returnVal = new ArrayList<WaveletParam>();
    	int freqIndex = 0;
    	double freq;
    	double radianFreq;
    	double samplesPerCycle;
    	double windowLength;
    	double taperValue;
    	double ratio = (cyclesPerWindow - 1.0) / cyclesPerWindow;
    	if((ratio >= 1.0) || (ratio <= 0.0)) {
    		System.out.println("Error: InitFrequencies: invalid ratio: " + "ratio");
    		return null;
    	}
    	for(freq = upperFreq; freq > centerFreq; freq *= ratio) {
    		samplesPerCycle = samplingRate / freq;
    		windowLength = samplesPerCycle * cyclesPerWindow;
    		radianFreq = twoPI / samplesPerCycle;
    		returnVal.add(new WaveletParam(radianFreq, (int) Math.round(windowLength)));
    	}
    	// start tapering window length
    	double startLogFreq = Math.log(freq) / Math.log(2.0);
    	double currentLogFreq;
    	for(; freq >= lowerFreq; freq *= ratio) {
    		samplesPerCycle = samplingRate / freq;
    		currentLogFreq =  Math.log(freq) / Math.log(2.0);
    		taperValue = Math.pow(taperPerOctave, startLogFreq - currentLogFreq);
    		windowLength = samplesPerCycle * (cyclesPerWindow / taperValue);
    		radianFreq = twoPI / samplesPerCycle;
    		returnVal.add(new WaveletParam(radianFreq, (int) Math.round(windowLength)));
    	}
    	if(freqIndex >= MAXFREQUENCIES) {
    		System.out.println("Error: InitFrequencies: MAXFREQUENCIES exceeded, freq: " + freq + " index: " + freqIndex);
    		System.exit(0);
    	}
    	return returnVal;
    }
    
}
