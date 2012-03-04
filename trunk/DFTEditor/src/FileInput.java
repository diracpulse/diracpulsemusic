import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;


public class FileInput {
	
	// This function reads from a binary file
	public static void ReadBinaryFileData(DFTEditor parent, String fileName, String type) {
		if(!type.equals("mono5ms")) {
			System.out.println("DFTEditor.ReadBinaryFileData: unsupported format");
			System.exit(0);
		}
		parent.setTitle("Loading: " + fileName);
		//timeToFreqToAmp = new TreeMap<Integer, TreeMap<Integer, Float>>();
		DFTEditor.floorAmpToCount = new TreeMap<Integer, Integer>();
		ArrayList<Float> matrixVals = new ArrayList<Float>();
		DataInputStream in = null;
		DFTEditor.maxAmplitude = 0.0f;
		DFTEditor.minAmplitude = 15.0f;
		DFTEditor.minScreenNote = DFTEditor.freqsPerOctave * 100;
		DFTEditor.maxScreenNote = 0;
		DFTEditor.maxTime = 0;
		float amp;
	    try {
	    	in = new DataInputStream(new
	                BufferedInputStream(new FileInputStream(new String(fileName))));
		} catch (FileNotFoundException nf) {
			System.out.println("DFTEditor: " + fileName + ".[suffix] not found");
			return;
		}
		try {
			DFTEditor.maxScreenNote = in.readInt();
			DFTEditor.minScreenNote = in.readInt();
			while(true) {
				amp = in.readFloat();
				matrixVals.add(amp);
				if(amp > DFTEditor.maxAmplitude) {
					DFTEditor.maxAmplitude = amp;
				}
				if(amp < DFTEditor.minAmplitude) {
					DFTEditor.minAmplitude = amp;
				}
				int floorAmp = (int) Math.floor(amp);
				int number = 0;
				if(DFTEditor.floorAmpToCount.containsKey(floorAmp)) {
					number = DFTEditor.floorAmpToCount.get(floorAmp);
				}
				number++;
				DFTEditor.floorAmpToCount.put(floorAmp, number);
				// End floopAmp count
			}
		} catch (IOException e) {
			if(e instanceof EOFException) {
				System.out.println("Finished reading from: " + fileName);
			} else {
				System.out.println("DFTEditor: error reading from: " + fileName);
			}
		}
		int matrixValsSize = matrixVals.size();
		DFTEditor.maxScreenFreq = DFTEditor.maxScreenNote - DFTEditor.minScreenNote;
		DFTEditor.maxTime = matrixValsSize / (DFTEditor.maxScreenFreq + 1);
		DFTEditor.amplitudes = new float[DFTEditor.maxTime + 1][DFTEditor.maxScreenFreq + 1];
		int index = 0;
		for(int time = 0; time < DFTEditor.maxTime; time++) {
			for(int freq = 0; freq <= DFTEditor.maxScreenFreq; freq++) {
				if(index < matrixValsSize) DFTEditor.amplitudes[time][freq] = matrixVals.get(index);
				index++;
			}
		}
		//System.out.println("maxtrixVals div size: " + matrixValsSize / msfplus1 + "index: " + index / msfplus1);
		//System.out.println("maxtrixVals mod size: " + matrixValsSize % msfplus1 + "index: " + index % msfplus1);
		//calculateAmpSum();
		//calculateMaxAmpAtFreq();
		parent.printFloorAmpCount();
		parent.calculateTimeToFreqsAtMaxima();
		parent.setTitle(fileName);
	}
	
	
}
