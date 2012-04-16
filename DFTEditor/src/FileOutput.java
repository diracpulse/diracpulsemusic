import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

public class FileOutput {
	
	// This function reads from a (newly created) .mono5ms file
	public static void selectedExportAll(DFTEditor parent) {
		try {
		//runProcess();
			TreeSet<String> mono5msFileNames = new TreeSet<String>();
			TreeSet<String> selectedFileNames = new TreeSet<String>();
			TreeSet<String> exportFileNames = new TreeSet<String>();
			File dataDir = new File(".");
			File[] dataFiles = dataDir.listFiles();
			for(File dataFile: dataFiles) {
				String fileName = dataFile.getName();
				if(fileName.endsWith(".mono5ms")) {
					String fileNameTrimmed = fileName.substring(0, fileName.length() - 8);
					mono5msFileNames.add(fileNameTrimmed);
				}
				if(fileName.endsWith(".selected")) {
					String fileNameTrimmed = fileName.substring(0, fileName.length() - 7);
					selectedFileNames.add(fileNameTrimmed);
				}
			}
			for(String mono5msFileName: mono5msFileNames) {
				System.out.println("mono5ms: " + mono5msFileName);
				if(!selectedFileNames.contains(mono5msFileName)) {
					exportFileNames.add(mono5msFileName);
				}
			}
			for(String selectedFileName: selectedFileNames) {
				System.out.println("selected: " + selectedFileName);
			}
			for(String exportFileName: exportFileNames) {
				FileInput.ReadBinaryFileData(parent, exportFileName + ".mono5ms", "mono5ms");
				DFTEditor.timeToFreqToSelectedData = new TreeMap<Integer, TreeMap<Integer, FDData>>();
				DFTEditor.autoSelect();
				OutputSelectedToFile(exportFileName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void OutputSelectedToFile(String fileName) {
	    try {
	    	DataOutputStream selectedOut = new DataOutputStream(new
		            BufferedOutputStream(new FileOutputStream(new String(fileName + ".selected"))));
            for(int time: DFTEditor.timeToFreqToSelectedData.keySet()) {
            	for(int freq: DFTEditor.timeToFreqToSelectedData.get(time).keySet()) {
            		FDData data = DFTEditor.timeToFreqToSelectedData.get(time).get(freq);
            		float amp = (float) data.getLogAmplitude();
            		selectedOut.writeInt(time);
            		selectedOut.writeInt(DFTEditor.freqToNote(freq));
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
	
	public static double[] SynthFDDataExternally(ArrayList<Harmonic> harmonics) {
		int index = 0;
		ArrayList<FDData> allData = new ArrayList<FDData>();
		for(Harmonic harmonic: harmonics) {
			if(!harmonic.isSynthesized()) continue;
			for(FDData data: harmonic.getAllData()) {
				allData.add(data);
			}
		}
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

}
