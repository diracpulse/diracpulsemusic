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
import java.nio.BufferUnderflowException;
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
	
	public static void OutputDoubleToFile(String fileName, double data) {
	    try {
	    	DataOutputStream out = new DataOutputStream(new
		            BufferedOutputStream(new FileOutputStream(fileName, true)));
            out.writeDouble(data);
            out.close();
		} catch (Exception e) {
			System.out.println("Exception in HarmonicsFileOutput.OutputDoubleToFile");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public static double[] SynthFDDataExternally(ArrayList<Harmonic> harmonics) {
		int index = 0;
		ArrayList<FDData> allData = new ArrayList<FDData>();
		for(Harmonic harmonic: harmonics) {
			for(FDData data: harmonic.getAllData()) {
				allData.add(data);
			}
		}
		ByteBuffer littleEndian = ByteBuffer.allocate(allData.size() * (4 + 4 + 8 + 8));
		littleEndian.order(ByteOrder.LITTLE_ENDIAN);
		for(FDData data: allData) {
			littleEndian.putFloat(data.getTime());
			littleEndian.putFloat(data.getNote());
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
			String lineIn;
			Process p = Runtime.getRuntime().exec("synth");
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((lineIn = stdInput.readLine()) != null) System.out.println(lineIn);
			p.waitFor();
		} catch (Exception e) {
			System.out.println("OutputFDDataToFile: Error running synth");
		}
		//File file = new File("synthOutput.dat");
		long length = 0;
		byte[] fileInput = null;
	    try {
	    	DataInputStream selectedIn = new DataInputStream(new
	    			BufferedInputStream(new FileInputStream("synthOutput.dat")));
	    	length = selectedIn.available();
	    	fileInput = new byte[(int) length];
	    	selectedIn.readFully(fileInput);
	    	selectedIn.close();
		} catch (Exception e) {
			System.out.println("Exception in FileOutput.OutputSelectedToFile");
		}
		int numDoubles = (int) length / 8;
		ByteBuffer littleEndian2 = ByteBuffer.allocate((int)length);
		littleEndian2.order(ByteOrder.LITTLE_ENDIAN);
		littleEndian2.put(fileInput);
		littleEndian2.position(0);
		double[] returnVal = new double[numDoubles];
		for(index = 0; index < numDoubles; index++) {
			returnVal[index] = littleEndian2.getDouble();
		}
		return returnVal;
	}
	
	public static void OutputSelectedToFile() {
		String fileName = "loop" + System.currentTimeMillis() + ".saved";
	    try {
	    	DataOutputStream selectedOut = new DataOutputStream(new
		            BufferedOutputStream(new FileOutputStream(fileName)));
            for(Harmonic harmonic: HarmonicsEditor.harmonicIDToHarmonicMono.values()) {
            	for(FDData data: harmonic.getAllData()) {
            		float amp = (float) data.getLogAmplitude();
            		selectedOut.writeByte((byte) 0);
            		selectedOut.writeInt(data.getTime());
            		selectedOut.writeInt(data.getNote());
            		selectedOut.writeFloat(amp);
            		selectedOut.writeLong(data.getHarmonicID());
            	}
            }
            for(Harmonic harmonic: HarmonicsEditor.harmonicIDToHarmonicLeft.values()) {
            	for(FDData data: harmonic.getAllData()) {
            		float amp = (float) data.getLogAmplitude();
            		selectedOut.writeByte((byte) 1);
            		selectedOut.writeInt(data.getTime());
            		selectedOut.writeInt(data.getNote());
            		selectedOut.writeFloat(amp);
            		selectedOut.writeLong(data.getHarmonicID());
            	}
            }
            for(Harmonic harmonic: HarmonicsEditor.harmonicIDToHarmonicRight.values()) {
            	for(FDData data: harmonic.getAllData()) {
            		float amp = (float) data.getLogAmplitude();
            		selectedOut.writeByte((byte) 2);
            		selectedOut.writeInt(data.getTime());
            		selectedOut.writeInt(data.getNote());
            		selectedOut.writeFloat(amp);
            		selectedOut.writeLong(data.getHarmonicID());
            	}
            }
            selectedOut.close();
		} catch (Exception e) {
			System.out.println("Exception in FileOutput.OutputSelectedToFile");
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("Finished output of of selected to: " + fileName + ".selected");
	}
	
}
