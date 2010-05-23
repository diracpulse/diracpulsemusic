
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.TreeMap;

public class Harmonics {
	
	private String harmonicsFileName;
	private TreeMap<Integer, TreeMap<Integer, Harmonic>> timeToFreqToHarmonic;
	private ArrayList<Harmonic> harmonicsArray;	
	
	Harmonics(String baseFileName) {
		harmonicsFileName = baseFileName + ".harmonics";
	}
	
	private static class FAPair {
		
		double freqInHz;
		double realAmplitude;
		
		FAPair(double freqInHz, double realAmplitude) {
			this.freqInHz = freqInHz;
			this.realAmplitude = realAmplitude;
		}
		
		double getFreqInHz() {
			return freqInHz;
		}
		
		double getRealAmplitude() {
			return realAmplitude;
		}
	}
	
	private static class Harmonic {
		
		static double constFreq = 0;
		static boolean freqIsConstant = true;
		static TreeMap<Integer, DFTModel.FAPair> variableFreqData = null;
		
		public Harmonic(ArrayList<DFTModel.TFA> TFAInput) {
			initHarmonic(TFAInput);
		}
		
		public Harmonic(String stringInput) {
			initHarmonic(stringInput);
		}
		
				
		public static void initHarmonic(ArrayList<DFTModel.TFA> TFAInput) {
			variableFreqData = new TreeMap<Integer, DFTModel.FAPair>();
			for(DFTModel.TFA tfa: TFAInput) {
				int time = tfa.getTimeInMillis();
				int freq = tfa.getFreq();
				float amplitude = tfa.getAmplitude();
				if(constFreq == 0) constFreq = freq;
				if(tfa.getFreq() != constFreq) freqIsConstant = false;
				if(variableFreqData.containsKey(tfa.getTime())) {
					System.out.println("Input Error in Harmonics.Harmonics: Duplicate Time" + time);
				} else {
					variableFreqData.put(time, new DFTModel.FAPair(freq, amplitude));
				}
			}
		}
		
		public static void initHarmonic(String input) {
			ArrayList<DFTModel.TFA> TFAInput = new ArrayList<DFTModel.TFA>();
			String[] tfaValues = input.split(":");
			for(String tfa: tfaValues) {
				String[] tfaParams = tfa.split(" ");
				int time = Integer.parseInt(tfaParams[0]) / DFTEditor.timeStepInMillis;
				int freq = Integer.parseInt(tfaParams[1]);
				float amplitude = Float.parseFloat(tfaParams[2]);
				TFAInput.add(new DFTModel.TFA(time, freq, amplitude));
			}
			initHarmonic(TFAInput);
		}
		
		public String toString() {
			StringBuffer sb = new StringBuffer();
			for(int timeInMillis: variableFreqData.keySet()) {
				DFTModel.FAPair fa = variableFreqData.get(timeInMillis);
				sb.append(timeInMillis + " " + fa.getFreq() + " " + fa.getAmplitude() + ":");
			}
			return sb.toString();
		}
		
	}
	
	// returns true if freq and time are within the bounds of a saved harmonic
	public boolean inHarmonic(int freq, int time) {
		return false;
	}
	
	public void addHarmonic(ArrayList<DFTModel.TFA> harmonicData) {
		harmonicsArray.add(new Harmonic(harmonicData));
	}
	
	public void removeHarmonic(int startTime, int startFreq) {
		
	}
	
	public void synthHarmonics(int startTime, int endTime) {
		
	}
	
	private void loadHarmonicsFromFile() {
		String linein;
		RandomAccessFile file;
	    try {
			file = new RandomAccessFile(new String(harmonicsFileName), "rw");
		} catch (FileNotFoundException nf) {
			System.out.println("Harmonics.loadHarmonicsFromFile():" + harmonicsFileName + " not found");
			return;
		}
		harmonicsArray = new ArrayList<Harmonic>();
		try {
			linein = file.readLine();
			while(linein != null) {
				harmonicsArray.add(new Harmonic(linein));
				linein = file.readLine();
			}
			file.close();
		} catch (IOException ie) {
			System.out.println("Harmonics.loadHarmonicsFromFile(): error reading file");
			System.exit(0);
		}
	}
	
	private void saveHarmonicToFile(Harmonic harmonic) {
		BufferedWriter bufferedWriter;
	    try {
	    	bufferedWriter = new BufferedWriter(new FileWriter(harmonicsFileName, true));
			bufferedWriter.write(harmonic.toString());
			bufferedWriter.newLine();
			bufferedWriter.flush();
			bufferedWriter.close();
		} catch (IOException ie) {
			System.out.println("Harmonics.saveHarmonicToFile(): error appending to file");
			System.exit(0);
		}
	}
	
}