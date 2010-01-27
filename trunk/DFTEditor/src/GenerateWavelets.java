import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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
	
	static double maxCyclesPerWindow = 45.22540955090449;
	static double taperPerOctave = Math.sqrt(2.0);
	static double noteBase = 31.0;
	
	public static void writeParamsToFile(String filename, ArrayList<WaveletParam> params) {
		try {
			FileOutputStream fos = new FileOutputStream(filename);
	    	FileChannel fco = fos.getChannel();
	    	ByteBuffer buffer = ByteBuffer.allocate(8);
		} catch (Exception ex) {
			// do nothing
		}
	    /*for (WaveletParam wp: params) {
	      float freqInHz
	      buffer.order(ByteOrder.BIG_ENDIAN);
	      buffer.asFloatBuffer().put()
	      int read = fci.read(buffer);

	      if (read == -1)
	        break;
	      buffer.flip();
	      fco.write(buffer);
	      buffer.clear();
	    }*/
	}
	
    public static void printParams()
    {
    	double upperFreq = 20000.0;
    	double centerFreq = 1000.0;
    	double lowerFreq = 20.0;
    	int upperNote = (int) Math.round(Math.log(upperFreq) / Math.log(2.0) * noteBase);
    	int centerNote = (int) Math.round(Math.log(centerFreq) / Math.log(2.0) * noteBase);   	
    	int lowerNote = (int) Math.round(Math.log(lowerFreq) / Math.log(2.0) * noteBase);   	
    	System.out.println("PARAMS");
    	for(WaveletParam wp: InitFrequencies(upperNote, centerNote, lowerNote)) {
    		System.out.println(wp);
    	}
    }
    
    static ArrayList<WaveletParam> InitFrequencies(int maxNote, int centerNote, int minNote) {
    	ArrayList<WaveletParam> returnVal = new ArrayList<WaveletParam>();
    	int note = maxNote;
    	double freqInHz = 1.0;
    	double cyclesPerWindow = 1.0;
    	double taperValue = 1.0;
    	double ratio = (maxCyclesPerWindow - 1.0) / maxCyclesPerWindow;
    	if((ratio >= 1.0) || (ratio <= 0.0)) {
    		System.out.println("Error: InitFrequencies: invalid ratio: " + "ratio");
    		return null;
    	}
    	for(note = maxNote; note > centerNote; note++) {
    		freqInHz = Math.pow(note, note / noteBase);
    		returnVal.add(new WaveletParam((float) note, maxCyclesPerWindow));
    	}
    	// start tapering window length
    	double startLogFreq = Math.log(freqInHz) / Math.log(2.0);
    	double currentLogFreq;
    	for(note = centerNote; note <= minNote; note++) {
    		freqInHz = Math.pow(note, note / noteBase);
    		currentLogFreq =  Math.log(freqInHz) / Math.log(2.0);
    		taperValue = Math.pow(taperPerOctave, startLogFreq - currentLogFreq);
    		cyclesPerWindow = maxCyclesPerWindow / taperValue;
    		returnVal.add(new WaveletParam((float) note, cyclesPerWindow));
    	}
    	return returnVal;
    }
    
}
