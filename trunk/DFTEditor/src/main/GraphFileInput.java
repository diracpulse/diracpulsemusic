import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;


public class GraphFileInput {
	
	private GraphEditor parent;
	
	public GraphFileInput(GraphEditor parent) {
		this.parent = parent;
	}
	
	// This function reads from a binary file
	public static void ReadBinaryFileData(String fileName) {
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
				byte channel = in.readByte();
				int time = in.readInt();
				short note = in.readShort();
				float amp = in.readFloat();
				long id = in.readLong();
				FDData data = new FDData(FDData.byteToChannel(channel), time, note, amp, id);
				//System.out.println(data);
				GraphEditor.addData(data);
				//System.out.println(data);
			}
		} catch (Exception e) {
			if(e instanceof EOFException) {
				System.out.println("Finished reading from: " + fileName);
			} else {
				System.out.println("FileInput error: " + e.getMessage());
			}
		}
	}
	
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
				System.out.println(line);
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
	
}
