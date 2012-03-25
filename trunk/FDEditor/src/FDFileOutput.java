import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;


public class FDFileOutput {
	
	public static void OutputHarmonicsToFile(String fileName) {
	    try {
	    	DataOutputStream selectedOut = new DataOutputStream(new
		            BufferedOutputStream(new FileOutputStream(new String(fileName + ".harmonics"))));
            for(long harmonicID: FDEditor.selectedHarmonicIDs) {
            	Harmonic harmonic = FDEditor.harmonicIDToHarmonic.get(harmonicID);
            	for(FDData data: harmonic.getAllData()) {
            		selectedOut.writeInt(data.getTime());
            		selectedOut.writeInt(data.getNote());
            		selectedOut.writeFloat((float) data.getLogAmplitude());
            		selectedOut.writeLong(data.getHarmonicID());
            	}
            }
            selectedOut.close();
		} catch (Exception e) {
			System.out.println("Exception in FileOutput.OutputSelectedToFile");
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("Finished output of of selected to: " + fileName + ".harmonics");
	}

}
