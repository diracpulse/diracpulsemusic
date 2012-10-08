import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TreeSet;


public class FileConvert {
	
	// This function reads from a (newly created) text file
	// It also creates a binary clone for future use
	public static void wavImportAll() {
		try {
		//runProcess();
			TreeSet<String> wavFileNames = new TreeSet<String>();
			TreeSet<String> mono5msFileNames = new TreeSet<String>();
			TreeSet<String> convertFileNames = new TreeSet<String>();
			File dataDir = new File(".");
			File[] dataFiles = dataDir.listFiles();
			for(File dataFile: dataFiles) {
				String fileName = dataFile.getName();
				if(fileName.endsWith(".wav")) {
					if(fileName.equals("input.wav")) continue;
					String fileNameTrimmed = fileName.substring(0, fileName.length() - 4);
					wavFileNames.add(fileNameTrimmed);
				}
				if(fileName.endsWith(".mono5ms")) {
					String fileNameTrimmed = fileName.substring(0, fileName.length() - 8);
					mono5msFileNames.add(fileNameTrimmed);
				}
			}
			for(String wavFileName: wavFileNames) {
				//System.out.println("WAV: " + wavFileName);
				if(!mono5msFileNames.contains(wavFileName)) {
					convertFileNames.add(wavFileName);
				}
			}
			for(@SuppressWarnings("unused") String mono5msFileName: mono5msFileNames) {
				//System.out.println("OUT5MS: " + mono5msFileName);
			}
			for(String convertFileName: convertFileNames) {
				ConvertWAVToMono5msExternal(convertFileName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void ConvertWAVToMono5msExternal(String fileName) {
		System.out.println("Starting conversion of \"" + fileName + ".wav\" to \"" + fileName + ".mono5ms\"");
		String linein = "";
		Float amp;
	    try {
	    	String[] del = new String[4];
	    	del[0] = "cmd";
	    	del[1] = "/C";
	    	del[2] = "del";
	    	del[3] = "input.wav";
	    	printToConsole("Starting: ", del);
	    	Process p = Runtime.getRuntime().exec(del);
	    	p.waitFor();
	    	printToConsole("Finished: ", del);
	    	String[] copy = new String[5];
	    	copy[0] = "cmd";
	    	copy[1] = "/C";
	    	copy[2] = "copy";
	    	copy[3] = "\"" + fileName + ".wav\"";
	    	copy[4] = "input.wav";
	    	printToConsole("Starting: ", copy);
	    	p = Runtime.getRuntime().exec(copy);
	    	p.waitFor();
	    	printToConsole("Finished: ", copy);
	    	DataOutputStream binaryOut = new DataOutputStream(new
		            BufferedOutputStream(new FileOutputStream(new String(fileName + ".mono5ms"))));
            p = Runtime.getRuntime().exec("a");
            BufferedReader stdInput = new BufferedReader(new 
                 InputStreamReader(p.getInputStream()));
            // BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            // Non-Amplitude data is indicated by "#", followed by value as string ON NEXT LINE
            // Comments start with "#" and must not include _*_
            // AMPLITUDE DATA PASSED MUST BE READ IN CORRECT ORDER
            while ((linein = stdInput.readLine()) != null) {
            	if(linein.contains("#")) {
            		Integer data = null;
            		if(linein.contains("_MAXNOTE_")) {
               			data = new Integer(stdInput.readLine());
            			binaryOut.writeInt(data);
            			System.out.println("_MAXNOTE_ = " + data);
            			continue;
            		}
            		if(linein.contains("_MINNOTE_")) {
            			data = new Integer(stdInput.readLine());
            			binaryOut.writeInt(data);
            			System.out.println("_MINNOTE_ = " + data);
            			continue;
            		}
            		System.out.println(linein); // print comment 
            	} else {
            		// assumes it's amplitude data (must be in proper order)
            		String[] leftRight = linein.split(" ");
            		float left = new Float(leftRight[0]);
            		float right = new Float(leftRight[1]);
            		binaryOut.writeFloat(left);
            		binaryOut.writeFloat(right);
            	}
            }
            binaryOut.close();
            p.waitFor();
		} catch (Exception e) {
			System.out.println("Exception in FileConvert.ConvertWAVToMono5ms()");
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("Finished conversion of \"" + fileName + ".wav\" to \"" + fileName + ".mono5ms\"");
	}
	
	private static void printToConsole(String first, String[] command) {
    	System.out.print(first + " ");
    	for(String s: command) System.out.print(s + " ");
    	System.out.println();
	}
	
}
