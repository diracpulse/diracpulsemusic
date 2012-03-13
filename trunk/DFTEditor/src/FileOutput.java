import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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

}
