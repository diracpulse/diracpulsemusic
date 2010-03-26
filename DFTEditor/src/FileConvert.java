import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.TreeMap;
import java.util.TreeSet;


public class FileConvert {
	
	// This function reads from a (newly created) text file
	// It also creates a binary clone for future use
	public static void wavImport() {
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
			for(String mono5msFileName: mono5msFileNames) {
				//System.out.println("OUT5MS: " + mono5msFileName);
			}
			for(String convertFileName: convertFileNames) {
				ConvertWAVToMono5ms(convertFileName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	public static void runProcess() {
		try {
			String s;
            Process p = Runtime.getRuntime().exec("a");
            BufferedReader stdInput = new BufferedReader(new 
                 InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new 
                 InputStreamReader(p.getErrorStream()));
            // read the output from the command
            System.out.println("Here is the standard output of the command:\n");
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }
            // read any errors from the attempted command
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }
        }
        catch (IOException e) {
            System.out.println("FileConvert.runProcess: IOException: ");
            e.printStackTrace();
        }
    }
	
	public static void ConvertWAVToMono5ms(String fileName) {
		System.out.println("Starting conversion of \"" + fileName + ".wav\" to \"" + fileName + ".mono5ms\"");
		String[] tokens;
		String linein = "";
		Integer time;
		Integer freq;
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
            while ((linein = stdInput.readLine()) != null) {
				tokens = linein.split(" ");
				time = new Integer(tokens[0]);
				freq = new Integer(tokens[1]);
				amp = new Float(tokens[2]);
				binaryOut.writeInt(time);
				binaryOut.writeShort(freq);
				binaryOut.writeFloat(amp);
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
