import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;


public class FDFileInput {
	
	private FDEditor parent;
	
	public FDFileInput(FDEditor parent) {
		this.parent = parent;
	}
	
	// This function reads from a binary file
	public static void ReadBinaryFileData(String fileName) {
		FDEditor.timeToNoteToData.clear();
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
				FDData data = new FDData(time, note, amp);
				//System.out.println(data);
				FDEditor.addData(data);
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
