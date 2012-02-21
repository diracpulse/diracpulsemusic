import java.sql.Time;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TreeMap;

import javax.swing.JOptionPane;

class SynthTools {
	
	static double sampleRate = 44100.0;
	static double twoPI = 2.0 * Math.PI;
	static double timeToSample = sampleRate * (FDData.timeStepInMillis * 1.0 / 1000.0);
	
	static void playFileData(FDEditor parent) {
		//powSpeedTest();
		//sinSpeedTest();
		long startSynthTimeInMillis = System.currentTimeMillis();
		System.out.println("SynthTools.playFileData started");
		createHarmonics(FDEditor.timeToNoteToData);
		// Find number of samples needed
		int endSampleOffset = 0;
		System.out.println("SynthTools.playFileData harmonics created");
		for(Harmonic harmonic: FDEditor.harmonics) {
			//System.out.println("SynthTools.playFileData harmonic.endSampleOffset = " + harmonic.getEndSampleOffset());
			if(harmonic.getEndSampleOffset() > endSampleOffset) {
				endSampleOffset = harmonic.getEndSampleOffset();
				//System.out.println("SynthTools.playFileData endSampleOffset = " + endSampleOffset);
			}
		}
		System.out.println("SynthTools.playFileData endSampleOffset = " + endSampleOffset);
		// create PCM Data
		double[] PCMData = new double[endSampleOffset + 1];
		System.out.println("End Sample Offset " + endSampleOffset);
		for(int i= 0; i <= endSampleOffset; i++) PCMData[i] = 0.0;
		for(Harmonic harmonic: FDEditor.harmonics) {
			Double[] HarmonicPCMData = harmonic.getPCMData();
			int startSample = harmonic.getStartSampleOffset();
			int endSample = startSample + HarmonicPCMData.length - 1;
			//System.out.println("SynthTools.playFileData startSample = " + startSample);
			//System.out.println("SynthTools.playFileData endSample = " + endSample);
			//System.out.println("SynthTools.playFileData HarmonicPCMData.length = " + HarmonicPCMData.length);
			for(int currentSample = startSample; currentSample < endSample - 1; currentSample++) {
				PCMData[currentSample] += HarmonicPCMData[currentSample - startSample];
				if(currentSample % 441 == 0) {
					//System.out.println(currentSample + ":" + PCMData[currentSample]);
				}
			}
		}
		for(int i= 0; i <= endSampleOffset; i += 100) {
			//System.out.println(i + "!" + PCMData[i]);
		}
		long timeElapsed = System.currentTimeMillis() - startSynthTimeInMillis;
		System.out.println("Time Elapsed = " + timeElapsed);
		JOptionPane.showMessageDialog(parent, "Ready To Play");
		AudioPlayer ap = new AudioPlayer();
		ap.PlayBuffer(PCMData, 1.0);
	}

	//4419
	static void powSpeedTest() {
		double result = 0.0;
		double numLoops = 10 * 1000 * 1000;
		double maxVal = 10.0;
		long startSynthTimeInMillis = System.currentTimeMillis();
		for(double exponent = 0.0; exponent < maxVal; exponent += maxVal/numLoops) {
			//result += Math.pow(FDData.logBase, exponent);
			Math.exp(exponent);
		}
		long timeElapsed = System.currentTimeMillis() - startSynthTimeInMillis;
		System.out.println("SynthTools.powSpeedTest: Time Elapsed = " + timeElapsed + " " + result);
	}
	
	static void sinSpeedTest() {
		double result = 0.0;
		double numLoops = 10 * 1000 * 1000;
		double maxVal = numLoops;
		long startSynthTimeInMillis = System.currentTimeMillis();
		for(double angle = 0.0; angle < maxVal; angle += maxVal/numLoops) {
			result += Math.sin(angle);
		}
		long timeElapsed = System.currentTimeMillis() - startSynthTimeInMillis;
		System.out.println("SynthTools.sinSpeedTest: Time Elapsed = " + timeElapsed + " " + result);
	}
	
	static void createHarmonics(TreeMap<Integer, TreeMap<Integer, FDData>> saveInput) {
		// create a copy of input because algorithm removes keys after added to harmonics
		TreeMap<Integer, TreeMap<Integer, FDData>> input = copyTreeMap(saveInput);
		FDEditor.harmonics= new ArrayList<Harmonic>(); 
		while(!input.isEmpty()) {
			int inputTime = input.firstKey();
			// loop through all frequencies at current time 
			while(!input.get(inputTime).isEmpty()) {
				Harmonic currentHarmonic = new Harmonic();
				// ARBITRARY: start at lowest note
				int currentNote = input.get(inputTime).firstKey();
				int harmonicTime = inputTime;
				while(input.containsKey(harmonicTime)) {
					//System.out.println("SynthTools.createHarmonics harmonicTime" + harmonicTime);
					//sleep(10);
					// check for data at [currentNote,time+1]
					if(input.get(harmonicTime).containsKey(currentNote)) {
						currentHarmonic.addData(input.get(harmonicTime).get(currentNote));
						input.get(harmonicTime).remove(currentNote);
						harmonicTime++;
						continue;
					}					
					// check for data at [currentNote+1,time+1]
					currentNote++;
					if(input.get(harmonicTime).containsKey(currentNote)) {
						currentHarmonic.addData(input.get(harmonicTime).get(currentNote));
						input.get(harmonicTime).remove(currentNote);
						harmonicTime++;
						continue;
					}
					// check for data at [currentNote-1,time-1]
					currentNote -= 2;
					if(input.get(harmonicTime).containsKey(currentNote)) {
						currentHarmonic.addData(input.get(harmonicTime).get(currentNote));
						input.get(harmonicTime).remove(currentNote);
						harmonicTime++;
						continue;
					}
					break;
				}
				FDEditor.harmonics.add(currentHarmonic);
			} // while(!input.get(inputTime).isEmpty())
			input.remove(inputTime);
			inputTime++;
			//System.out.println("SynthTools.createHarmonics input size " + input.size());
			//sleep(10);
		} // while(!input.isEmpty())
		//printHarmonics(harmonics);
		return;
	}
	
	static void printHarmonics(ArrayList<Harmonic> harmonics) {
		for(Harmonic harmonic: harmonics) {
			System.out.print(harmonic);
		}
	}
	
	static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (Exception e){
			System.out.println(e);
		}

	}

	
	static TreeMap<Integer, TreeMap<Integer, FDData>> copyTreeMap(TreeMap<Integer, TreeMap<Integer, FDData>> input) {
		TreeMap<Integer, TreeMap<Integer, FDData>> output = new TreeMap<Integer, TreeMap<Integer, FDData>>();
		for(Integer time: input.keySet()) {
			output.put(time, new TreeMap<Integer, FDData>());
			for(Integer note: input.get(time).keySet()) {
				FDData data = input.get(time).get(note);
				output.get(time).put(note, data);
			}
		}
		return output;
	}

}
