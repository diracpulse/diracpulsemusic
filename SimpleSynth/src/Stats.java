import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.TreeMap;


public class Stats {
	
	public static ArrayList<String> ReadTextFileData(String fileName) {
		ArrayList<String> returnVal = new ArrayList<String>();
		BufferedReader in = null;
		String line = " "; // dummy non-null value
	    try {
	    	in = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException nf) {
			System.out.println("FileInput: " + fileName + ".[suffix] not found");
			return returnVal; // return empty string if file not found
		}
		try {
			// loop is terminated by EOFException
			while(line != null) {
				line = in.readLine();
				returnVal.add(line);
				//System.out.println(line);
			}
		} catch (Exception e) {
			if(e instanceof EOFException) {
				System.out.println("Finished reading from: " + fileName);
			} else {
				System.out.println("FileInput error: " + e.getMessage());
			}
		}
		return returnVal;
	}
	
	public static void printStatistics() {
		TreeMap<Integer, Double> chord1ToRating = new TreeMap<Integer, Double>();
		TreeMap<Integer, Double> chord2ToRating = new TreeMap<Integer, Double>();
		TreeMap<Integer, Double> deltaNoteToRating = new TreeMap<Integer, Double>();
		TreeMap<Integer, Double> baseNoteToRating = new TreeMap<Integer, Double>();
		TreeMap<Integer, Double> chord1ToNumRatings = new TreeMap<Integer, Double>();
		TreeMap<Integer, Double> chord2ToNumRatings = new TreeMap<Integer, Double>();
		TreeMap<Integer, Double> deltaNoteToNumRatings = new TreeMap<Integer, Double>();
		TreeMap<Integer, Double> baseNoteToNumRatings = new TreeMap<Integer, Double>();
		ArrayList<String> fileData = ReadTextFileData("Doublets.txt");
		for(String line: fileData) {
			if(line == null) break;
			if(line.isEmpty()) break;
			String[] data = line.split(" ");
			String dataFormat = data[0];
			int baseNote = new Integer(data[1]);
			int chord1 = new Integer(data[2]);
			int chord2 = new Integer(data[3]);
			int deltaNote = new Integer(data[4]);
			double rating = (double) new Integer(data[5]);
			if(!chord1ToRating.containsKey(chord1)) {
				chord1ToRating.put(chord1, rating);
				chord1ToNumRatings.put(chord1, 1.0);
			} else {
				double ratingSum = chord1ToRating.get(chord1);
				double numRatings = chord1ToNumRatings.get(chord1);
				chord1ToRating.put(chord1, rating + ratingSum);
				chord1ToNumRatings.put(chord1, numRatings + 1);
			}
			if(!chord2ToRating.containsKey(chord2)) {
				chord2ToRating.put(chord2, rating);
				chord2ToNumRatings.put(chord2, 1.0);
			} else {
				double ratingSum = chord2ToRating.get(chord2);
				double numRatings = chord2ToNumRatings.get(chord2);
				chord2ToRating.put(chord2, rating + ratingSum);
				chord2ToNumRatings.put(chord2, numRatings + 1);
			}
			if(!baseNoteToRating.containsKey(baseNote)) {
				baseNoteToRating.put(baseNote, rating);
				baseNoteToNumRatings.put(baseNote, 1.0);
			} else {
				double ratingSum = baseNoteToRating.get(baseNote);
				double numRatings = baseNoteToNumRatings.get(baseNote);
				baseNoteToRating.put(baseNote, rating + ratingSum);
				baseNoteToNumRatings.put(baseNote, numRatings + 1);
			}
			if(!deltaNoteToRating.containsKey(deltaNote)) {
				deltaNoteToRating.put(deltaNote, rating);
				deltaNoteToNumRatings.put(deltaNote, 1.0);
			} else {
				double ratingSum = deltaNoteToRating.get(deltaNote);
				double numRatings = deltaNoteToNumRatings.get(deltaNote);
				deltaNoteToRating.put(deltaNote, rating + ratingSum);
				deltaNoteToNumRatings.put(deltaNote, numRatings + 1);
			}		
		}
		System.out.println("Chord1: Average rating");
		for(Integer chord1: chord1ToRating.keySet()) {
			System.out.println(chord1 + ": " + chord1ToRating.get(chord1) / chord1ToNumRatings.get(chord1));	
		}
		System.out.println("Chord2: Average rating");
		for(Integer chord2: chord2ToRating.keySet()) {
			System.out.println(chord2 + ": " + chord2ToRating.get(chord2) / chord2ToNumRatings.get(chord2));	
		}
		System.out.println("baseNote: Average rating");
		for(Integer baseNote: baseNoteToRating.keySet()) {
			System.out.println(baseNote + ": " + baseNoteToRating.get(baseNote) / baseNoteToNumRatings.get(baseNote));	
		}
		System.out.println("deltaNote: Average rating");
		for(Integer deltaNote: deltaNoteToRating.keySet()) {
			System.out.println(deltaNote + ": " + deltaNoteToRating.get(deltaNote) / deltaNoteToNumRatings.get(deltaNote));	
		}
		
	}
	
}