import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
		
		public double getCyclesPerWindow() {
			return cyclesPerWindow;
		}
		
		public double getFreqInHz() {
			return freqInHz;
		}
		
		public double getNote() {
			return Math.log(freqInHz) / Math.log(2.0) * GenerateWavelets.noteBase;
		}
		
		public String toString() {
			int note = (int) getNote();
			return new String(note + " " + freqInHz + " " + cyclesPerWindow);
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
			fos = new FileOutputStream(filename);
	    	fco = fos.getChannel();
	    	buffer = ByteBuffer.allocate(4);
	    	for (WaveletParam wp: params) {
	    		float freqInHz = (float) wp.getFreqInHz();
	    		float cyclesPerWindow = (float) wp.getCyclesPerWindow();
	    		// write freqInHz to file
	    		buffer.order(ByteOrder.BIG_ENDIAN);
	    		buffer.asFloatBuffer().put(freqInHz);
	    		buffer.order(ByteOrder.LITTLE_ENDIAN);	      
	    		buffer.flip();
	    		fco.write(buffer);
	    		buffer.clear();
	    		// write cyclesPerWindow to file
	    		buffer.order(ByteOrder.BIG_ENDIAN);
	    		buffer.asFloatBuffer().put(cyclesPerWindow);
	    		buffer.order(ByteOrder.LITTLE_ENDIAN);	      
	    		buffer.flip();
	    		fco.write(buffer);
	    		buffer.clear();
	    		break;
	    	}
	    	fco.close();
	    	fos.close();
		} catch (Exception ex) {
			System.out.println(ex);
			System.exit(0);
		}
	}
		
    public static ArrayList<WaveletParam> generateParams()
    {
    	double upperFreq = 20000.0;
    	double centerFreq = 1000.0;
    	double lowerFreq = 20.0;
    	int upperNote = (int) Math.round(Math.log(upperFreq) / Math.log(2.0) * noteBase);
    	int centerNote = (int) Math.round(Math.log(centerFreq) / Math.log(2.0) * noteBase);   	
    	int lowerNote = (int) Math.round(Math.log(lowerFreq) / Math.log(2.0) * noteBase);
    	System.out.println(upperNote + " " + centerNote + " " + lowerFreq);
    	return initFrequencies(upperNote, centerNote, lowerNote);
    }
    
    public static void writeParamsToFile() {
    	ArrayList<WaveletParam> params = generateParams();
    	writeParamsToFile("wavelets", params);
    }
    
    public static void printParams() {
    	ArrayList<WaveletParam> params = generateParams();
    	System.out.println("PARAMS");
    	for(WaveletParam wp: params) {
    		System.out.println(wp);
    	}
    }
    
    static ArrayList<WaveletParam> initFrequencies(int maxNote, int centerNote, int minNote) {
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
    	for(note = maxNote; note > centerNote; note--) {
    		freqInHz = Math.pow(2.0, note / noteBase);
    		returnVal.add(new WaveletParam((float) freqInHz, maxCyclesPerWindow));
    	}
    	// start tapering window length
    	double startLogFreq = Math.log(freqInHz) / Math.log(2.0);
    	double currentLogFreq;
    	for(note = centerNote; note >= minNote; note--) {
    		freqInHz = Math.pow(2.0, note / noteBase);
    		currentLogFreq =  Math.log(freqInHz) / Math.log(2.0);
    		taperValue = Math.pow(taperPerOctave, startLogFreq - currentLogFreq);
    		cyclesPerWindow = maxCyclesPerWindow / taperValue;
    		returnVal.add(new WaveletParam((float) freqInHz, cyclesPerWindow));
    	}
    	return returnVal;
    }
    
}
