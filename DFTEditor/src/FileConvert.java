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
			File dataDir = new File("data\\");
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
				System.out.println("WAV: " + wavFileName);
			}
			for(String mono5msFileName: mono5msFileNames) {
				System.out.println("OUT5MS: " + mono5msFileName);
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

}
