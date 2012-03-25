import java.sql.Time;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TreeMap;

import javax.swing.JOptionPane;

class SynthTools {
	
	static double sampleRate = 44100.0;
	static double twoPI = 2.0 * Math.PI;
	static double timeToSample = sampleRate * (FDData.timeStepInMillis * 1.0 / 1000.0);
	static double[] PCMData = null;
	static FDEditor parent;
	static int deltaHarmonic = 1;

	static void createPCMData(FDEditor parent, int startTime, int endTime, boolean selectedOnly) {
		int startSampleOffset = (int) Math.round(startTime * timeToSample);
		int endSampleOffset = (int) Math.round(endTime * timeToSample);
		int dataLength = endSampleOffset - startSampleOffset;
		for(Harmonic harmonic: FDEditor.harmonicIDToHarmonic.values()) {
			if(harmonic.getStartSampleOffset() >= endSampleOffset) continue;
			if(harmonic.getEndSampleOffset() <= startSampleOffset) continue;
			if(harmonic.getStartSampleOffset() < startSampleOffset) {
				startSampleOffset = harmonic.getStartSampleOffset();
			}
			if(harmonic.getEndSampleOffset() > endSampleOffset) {
				endSampleOffset = harmonic.getEndSampleOffset();
			}
			if(harmonic.getStartSampleOffset() < endSampleOffset) continue;
		}
		PCMData = new double[dataLength + 1];
		System.out.println("End Sample Offset " + endSampleOffset);
		for(int i= 0; i <= dataLength; i++) PCMData[i] = 0.0;
		for(Harmonic harmonic: FDEditor.harmonicIDToHarmonic.values()) {
			if(selectedOnly) {
				if(!FDEditor.selectedHarmonicIDs.contains(harmonic.getHarmonicID())) continue;
			}
			Double[] HarmonicPCMData = harmonic.getPCMData();
			int startSample = harmonic.getStartSampleOffset();
			int endSample = startSample + HarmonicPCMData.length - 1;
			//System.out.println("SynthTools.playFileData startSample = " + startSample);
			//System.out.println("SynthTools.playFileData endSample = " + endSample);
			//System.out.println("SynthTools.playFileData HarmonicPCMData.length = " + HarmonicPCMData.length);
			for(int currentSample = startSample; currentSample < endSample - 1; currentSample++) {
				if(currentSample > dataLength) break;
				PCMData[currentSample] += HarmonicPCMData[currentSample - startSample];
				if(currentSample % 441 == 0) {
					//System.out.println(currentSample + ":" + PCMData[currentSample]);
				}
			}
		}
		for(int i= 0; i <= endSampleOffset; i += 100) {
			//System.out.println(i + "!" + PCMData[i]);
		}
		JOptionPane.showMessageDialog(parent, "Ready To Play");
	}
	
	public static void playWindow() {
		AudioPlayer ap = new AudioPlayer(parent, PCMData, 1.0);
		ap.start();
	}
	
	static void createHarmonics(TreeMap<Integer, TreeMap<Integer, FDData>> saveInput) {
		// create a copy of input because algorithm removes keys after added to harmonics
		TreeMap<Integer, TreeMap<Integer, FDData>> input = copyTreeMap(saveInput);
		FDEditor.harmonicIDToHarmonic = new TreeMap<Long, Harmonic>(); 
		while(!input.isEmpty()) {
			int inputTime = input.firstKey();
			// loop through all frequencies at current time 
			while(!input.get(inputTime).isEmpty()) {
				long id = FDEditor.getRandomID();
				Harmonic currentHarmonic = new Harmonic(id);
				// ARBITRARY: start at lowest note
				int currentNote = input.get(inputTime).firstKey();
				int harmonicTime = inputTime;
				while(input.containsKey(harmonicTime)) {
					//System.out.println("SynthTools.createHarmonics harmonicTime" + harmonicTime);
					//sleep(10);
					// check for data at [currentNote,time+1]
					if(input.get(harmonicTime).containsKey(currentNote)) {
						FDData data = input.get(harmonicTime).get(currentNote);
						data.setHarmonicID(id);
						currentHarmonic.addData(data);
						input.get(harmonicTime).remove(currentNote);
						FDEditor.addData(data);
						harmonicTime++;
						continue;
					}					
					// check for a branch (go to highest value)
					int upperNote = currentNote + 1;
					int lowerNote = currentNote - 1;
					if(input.get(harmonicTime).containsKey(upperNote)) {
						if(input.get(harmonicTime).containsKey(lowerNote)) {
							double upperAmplitude = input.get(harmonicTime).get(upperNote).getLogAmplitude();
							double lowerAmplitude = input.get(harmonicTime).get(lowerNote).getLogAmplitude();
							if(upperAmplitude > lowerAmplitude) {
								FDData data = input.get(harmonicTime).get(upperNote);
								data.setHarmonicID(id);
								currentHarmonic.addData(data);
								input.get(harmonicTime).remove(upperNote);
								FDEditor.addData(data);
								harmonicTime++;
								continue;
							} else {
								FDData data = input.get(harmonicTime).get(lowerNote);
								data.setHarmonicID(id);
								currentHarmonic.addData(data);
								input.get(harmonicTime).remove(lowerNote);
								FDEditor.addData(data);
								harmonicTime++;
								continue;								
							}
						}
					}
					// check for data at [currentNote+1,time+1]
					currentNote++;
					if(input.get(harmonicTime).containsKey(currentNote)) {
						FDData data = input.get(harmonicTime).get(currentNote);
						data.setHarmonicID(id);
						currentHarmonic.addData(data);
						input.get(harmonicTime).remove(currentNote);
						FDEditor.addData(data);
						harmonicTime++;
						continue;
					}
					// check for data at [currentNote-1,time-1]
					currentNote -= 2;
					if(input.get(harmonicTime).containsKey(currentNote)) {
						FDData data = input.get(harmonicTime).get(currentNote);
						data.setHarmonicID(id);
						currentHarmonic.addData(data);
						input.get(harmonicTime).remove(currentNote);
						FDEditor.addData(data);
						harmonicTime++;
						continue;
					}
					if(deltaHarmonic == 1) break;
					currentNote += 3;
					if(input.get(harmonicTime).containsKey(currentNote)) {
						FDData data = input.get(harmonicTime).get(currentNote);
						data.setHarmonicID(id);
						currentHarmonic.addData(data);
						input.get(harmonicTime).remove(currentNote);
						FDEditor.addData(data);
						harmonicTime++;
						continue;
					}
					currentNote -= 4;
					if(input.get(harmonicTime).containsKey(currentNote)) {
						FDData data = input.get(harmonicTime).get(currentNote);
						data.setHarmonicID(id);
						currentHarmonic.addData(data);
						input.get(harmonicTime).remove(currentNote);
						FDEditor.addData(data);
						harmonicTime++;
						continue;
					}				
					break;
				}
				FDEditor.harmonicIDToHarmonic.put(id, currentHarmonic);
			} // while(!input.get(inputTime).isEmpty())
			input.remove(inputTime);
			inputTime++;
			//System.out.println("SynthTools.createHarmonics input size " + input.size());
			//sleep(10);
		} // while(!input.isEmpty())
		//printHarmonics(harmonics);
		return;
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
