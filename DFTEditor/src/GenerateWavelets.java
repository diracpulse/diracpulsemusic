import java.util.ArrayList;


public class GenerateWavelets {

	public static class WaveletParam {
		
		private double cyclesPerWindow;
		private double freqInHz;
		
		WaveletParam(double freqInHz, double cyclesPerWindow) {
			this.freqInHz = freqInHz;
			this.cyclesPerWindow = cyclesPerWindow;
		}
		
		public double cyclesPerWindow() {
			return cyclesPerWindow;
		}
		
		public double getFreqInHz() {
			return freqInHz;
		}
		
		public String toString() {
			return new String(freqInHz + " " + cyclesPerWindow);
		}
		
	}
	
	final double gaussianConstant = 2.0;
	final double onePI = Math.PI;
	final static double twoPI = Math.PI * 2.0;
	private static final int MAXFREQUENCIES = 31 * 16;

	static double samplingRate = 44100.0;
	static double maxCyclesPerWindow = 45.22540955090449;
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
    	System.out.println("PARAMS");
    	for(WaveletParam wp: InitFrequencies(18322.012048779428, 1000.0, 20.0)) {
    		System.out.println(wp);
    	}
    }
    
    static ArrayList<WaveletParam> InitFrequencies(double upperFreq, double centerFreq, double lowerFreq) {
    	ArrayList<WaveletParam> returnVal = new ArrayList<WaveletParam>();
    	double freqInHz;
    	double cyclesPerWindow;
    	double taperValue;
    	double ratio = (maxCyclesPerWindow - 1.0) / maxCyclesPerWindow;
    	if((ratio >= 1.0) || (ratio <= 0.0)) {
    		System.out.println("Error: InitFrequencies: invalid ratio: " + "ratio");
    		return null;
    	}
    	for(freqInHz = upperFreq; freqInHz > centerFreq; freqInHz *= ratio) {
    		returnVal.add(new WaveletParam(freqInHz, maxCyclesPerWindow));
    	}
    	// start tapering window length
    	double startLogFreq = Math.log(freqInHz) / Math.log(2.0);
    	double currentLogFreq;
    	for(; freqInHz >= lowerFreq; freqInHz *= ratio) {
    		currentLogFreq =  Math.log(freqInHz) / Math.log(2.0);
    		taperValue = Math.pow(taperPerOctave, startLogFreq - currentLogFreq);
    		cyclesPerWindow = maxCyclesPerWindow / taperValue;
    		returnVal.add(new WaveletParam(freqInHz, cyclesPerWindow));
    	}
    	return returnVal;
    }
    
}
