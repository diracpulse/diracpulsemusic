import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;


public class HarmonicsFileOutput {
	
	public static void OutputStringToFile(String fileName, String data) {
		try {
			FileWriter fstream = new FileWriter(fileName, true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(data);
			out.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
	
	public static double[] SynthFDDataExternally(ArrayList<FDData> allData) {
		ByteBuffer littleEndian = ByteBuffer.allocate(allData.size() * (4 + 4 + 8 + 8));
		littleEndian.order(ByteOrder.LITTLE_ENDIAN);
		for(FDData data: allData) {
			littleEndian.putInt(data.getTime());
			littleEndian.putInt(data.getNote());
			littleEndian.putDouble(data.getLogAmplitude());
			littleEndian.putLong(data.getHarmonicID());
		}
		byte[] bytes = littleEndian.array();
	    try {
	    	DataOutputStream selectedOut = new DataOutputStream(new
		            BufferedOutputStream(new FileOutputStream("synthInput.dat")));
            selectedOut.write(bytes, 0, bytes.length);
            selectedOut.close();
		} catch (Exception e) {
			System.out.println("Exception in FileOutput.OutputSelectedToFile");
		}
		System.out.println("OutputFDDataToFile: Finished Writing Data");
		try {
			Process p = Runtime.getRuntime().exec("synth");
			p.waitFor();
		} catch (Exception e) {
			System.out.println("OutputFDDataToFile: Error running synth");
		}
		File file = new File("synthOutput.dat");
		long length = file.length();
		byte[] fileInput = new byte[(int) length];
		DataInputStream selectedIn = null;
	    try {
	    	selectedIn = new DataInputStream(new
	    			BufferedInputStream(new FileInputStream("synthOutput.dat")));
	    } catch (Exception e) {
	    	System.out.println("Error Opening File");
	    	return null;
	    }
	    try {
	    	selectedIn.readFully(fileInput);
            selectedIn.close();
		} catch (Exception e) {
			System.out.println("Exception in FileOutput.OutputSelectedToFile");
		}
		littleEndian = ByteBuffer.allocate((int) length);
		littleEndian.order(ByteOrder.LITTLE_ENDIAN);
		littleEndian.put(fileInput);
		int numSamples = (int) (length / 8);
		double[] returnVal = new double[numSamples];
		for(int index = 0; index < numSamples; index++) {
			returnVal[index] = littleEndian.getDouble();
		}
		return returnVal;
	}
	
}
