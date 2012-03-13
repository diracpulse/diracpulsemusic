import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;


public class HarmonicsFileInput {
	
	private HarmonicsEditor parent;
	
	public HarmonicsFileInput(HarmonicsEditor parent) {
		this.parent = parent;
	}
	
	// This function reads from a binary file
	public static void ReadBinaryFileData(String fileName) {
		HarmonicsEditor.clearCurrentData();
		DataInputStream in = null;
	    try {
	    	in = new DataInputStream(new
	                BufferedInputStream(new FileInputStream(new String(fileName))));
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
				long id = in.readLong();
				FDData data = new FDData(time, note, amp, id);
				//System.out.println(data);
				HarmonicsEditor.addData(data);
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
