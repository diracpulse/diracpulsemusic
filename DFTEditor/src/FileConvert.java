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


public class FileConvert {
	
	// This function reads from a (newly created) text file
	// It also creates a binary clone for future use
	public static void wavImport() {
		try {
		runProcess();
		String currentDir = new File(".").getAbsolutePath();
		System.out.println("!" + currentDir);
		/*
		timeToFreqToAmp = new TreeMap<Integer, TreeMap<Integer, Float>>();
		TreeMap<Integer, Float> freqToAmp;
		String[] tokens;
		RandomAccessFile file = null;
		DataOutputStream binaryOut = null;
		String linein = "";
		maxAmplitude = 0.0f;
		minAmplitude = 15.0f;
		minRealFreq = freqsPerOctave * 100;
		maxRealFreq = 0;
		maxTime = 0;
		Integer time;
		Integer freq;
		Float amp;
	    try {
			file = new RandomAccessFile(new String(fileName + ".txt"), "r");
			binaryOut = new DataOutputStream(new
		            BufferedOutputStream(new FileOutputStream(new String(fileName + ".mono5ms"))));
		} catch (FileNotFoundException nf) {
			System.out.println("DFTEditor: " + fileName + ".[suffix] not found");
			System.exit(0);
		}
		try {
			linein = file.readLine();
			while(linein != null) {
				tokens = linein.split(" ");
				time = new Integer(tokens[0]);
				freq = new Integer(tokens[1]);
				amp = new Float(tokens[2]);
				binaryOut.writeInt(time);
				binaryOut.writeShort(freq);
				binaryOut.writeFloat(amp);
				if(amp.floatValue() > maxAmplitude) {
					maxAmplitude = amp.floatValue();
				}
				if(amp.floatValue() < minAmplitude) {
					minAmplitude = amp.floatValue();
				}
				if(freq.intValue() > maxRealFreq) {
					maxRealFreq = freq.intValue();
				}
				if(freq.intValue() < minRealFreq) {
					minRealFreq = freq.intValue();
				}				
				if(time.intValue() > maxTime) {
					maxTime = time.intValue();
				}				
				if (timeToFreqToAmp.containsKey(time)) {
					freqToAmp = timeToFreqToAmp.get(time);
				} else {
					timeToFreqToAmp.put(time, new TreeMap<Integer, Float>());
					freqToAmp = timeToFreqToAmp.get(time);
				}
				freqToAmp.put(freq, amp);
				linein = file.readLine();
			}
			file.close();
			binaryOut.close();
		*/
		} catch (Exception e) {
			System.out.println("FileConvert: Exception:");
			e.printStackTrace();
			System.exit(0);
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
