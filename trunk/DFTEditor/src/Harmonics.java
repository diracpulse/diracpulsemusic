
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.TreeMap;

public class Harmonics {
	
	private String harmonicsFileName;
	private TreeMap<Integer, TreeMap<Integer, Harmonic>> noteToHarmonic;
	private ArrayList<Harmonic> harmonicsArray;	
	
	Harmonics(String baseFileName) {
		harmonicsFileName = baseFileName + ".harmonics";
	}
	
	public static class TAPair {
		
		private int timeInMillis; // time in milliseconds
		private float logAmplitude; // log2(amplitude)
		
		TAPair(int timeInMillis, float logAmplitude) {
			this.timeInMillis = timeInMillis;
			this.logAmplitude = logAmplitude;
		}
		
		int getTimeInMillis() {
			return timeInMillis;
		}
		
		float getLogAmplitude() {
			return logAmplitude;
		}
	}
	
	private static class Harmonic {
		
		int note; // note = log2(freqInHz) * 31
		int startTimeInMillis;
		int lengthInMillis;
		TreeMap<Integer, Float> timeInMillisToLogAmplitude = null;
		
		public Harmonic(int note, int startTimeInMillis, ArrayList<TAPair> TAInput) {
			this.note = note;
			this.startTimeInMillis = startTimeInMillis;
			this.lengthInMillis = 0;
			initHarmonic(TAInput);
		}
		
		public Harmonic(String stringInput) {
			initHarmonic(stringInput);
		}
		
		public void initHarmonic(ArrayList<TAPair> TAInput) {
			timeInMillisToLogAmplitude = new TreeMap<Integer, Float>();
			for(TAPair TA: TAInput) {
				int timeInMillis = TA.getTimeInMillis();
				float logAmplitude = TA.getLogAmplitude();
				if(timeInMillisToLogAmplitude.containsKey(timeInMillis)) {
					System.out.println("ERROR: initHarmonic(ArrayList<TAPair> TAInput)): duplicate time");
					System.exit(0);
				}
				if(timeInMillis > lengthInMillis) lengthInMillis = timeInMillis;
				timeInMillisToLogAmplitude.put(timeInMillis, logAmplitude);
			}
		}
		
		public void initHarmonic(String input) {
			ArrayList<TAPair> TAInput = new ArrayList<TAPair>();
			String[] taValues = input.split(":");
			this.note = Integer.parseInt(taValues[0]);
			this.startTimeInMillis = Integer.parseInt(taValues[1]);
			this.lengthInMillis = 0;
			for(int index = 2; index < taValues.length; index += 2) {
				int timeInMillis = Integer.parseInt(taValues[index]);
				float logAmplitude = Float.parseFloat(taValues[index + 1]);
				TAInput.add(new TAPair(timeInMillis, logAmplitude));
			}
			initHarmonic(TAInput);
		}
		
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append(note + ":" + startTimeInMillis);
			for(int timeInMillis: timeInMillisToLogAmplitude.keySet()) {
				sb.append(":" + timeInMillis + ":" + timeInMillisToLogAmplitude.get(timeInMillis));
			}
			return sb.toString();
		}
	}
	
	// returns true if freq and time are within the bounds of a saved harmonic
	public boolean inHarmonic(int freq, int time) {
		return false;
	}
	
	public void addHarmonic(Harmonic harmonic) {
		harmonicsArray.add(harmonic);
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