import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

public class FileOutput {
	
	public static void OutputMaximasToFile(String fileName) {
	    try {
	    	DataOutputStream maximasOut = new DataOutputStream(new
		            BufferedOutputStream(new FileOutputStream(new String(fileName + ".maximas"))));
            for(int time: DFTEditor.timesWithMaxima()) {
            	for(int freq: DFTEditor.maximasAtTime(time)) {
            		float amp = DFTEditor.getAmplitude(time, freq);
            		maximasOut.writeInt(time);
            		maximasOut.writeInt(DFTEditor.freqToNote(freq));
            		maximasOut.writeFloat(amp);
            	}
            }
            maximasOut.close();
		} catch (Exception e) {
			System.out.println("Exception in FileConvert.OutputMaximasToFile");
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("Finished output of of maximas to: " + fileName + ".maximas");
	}

}
