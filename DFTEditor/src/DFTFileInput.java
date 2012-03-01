import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.TreeMap;


public class DFTFileInput {
	
	public DFTFileInput(DFTEditor parent) {}
	
	// This function reads from a binary file
	public static void ReadSelectedFileData(String fileName) {
		DFTEditor.timeToFreqToSelectedData = new TreeMap<Integer, TreeMap<Integer, FDData>>();
		DataInputStream in = null;
	    try {
	    	in = new DataInputStream(new
	                BufferedInputStream(new FileInputStream(new String(fileName + ".selected"))));
		} catch (FileNotFoundException nf) {
			System.out.println("FileInput: " + fileName + ".[suffix] not found");
			return;
		}
		try {
			// loop is terminated by EOFException
			while(true) {
				int time = in.readInt();
				int note = in.readInt();
				float amp = in.readFloat();
				FDData data = new FDData(time, note, amp);
				//System.out.println(data);
				DFTEditor.addSelected(data);
			}
		} catch (Exception e) {
			if(e instanceof EOFException) {
				System.out.println("Finished reading from: " + fileName);
			} else {
				System.out.println("FileInput error: " + e.getMessage());
			}
		}
	}
	
}
