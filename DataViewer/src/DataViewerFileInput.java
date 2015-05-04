import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.TreeMap;


public class DataViewerFileInput {
	
	public DataViewerFileInput(DataViewer parent) {}
	
	// This function reads from a binary file
	public static void ReadSelectedFileData(String fileName) {
		DataInputStream in = null;
	    try {
	    	in = new DataInputStream(new
	                BufferedInputStream(new FileInputStream(new String(fileName))));
		} catch (FileNotFoundException nf) {
			System.out.println("FileInput: " + fileName + " not found");
			return;
		}
		try {
			// loop is terminated by EOFException
			while(true) {
				double x= in.readDouble();
				double y = in.readDouble();
				double value = in.readDouble();
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
