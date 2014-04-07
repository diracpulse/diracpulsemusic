package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import main.Module.Connector;
import main.MultiWindow.ModuleEditorInfo;
import main.modules.MasterInput;

public class Sequencer extends JPanel {

	private static final long serialVersionUID = 3138005743637187863L;
	
	MultiWindow parent;
	SequencerView view;
	SequencerController controller;
	ArrayList<ModuleEditor> moduleEditors = null;
	public JScrollPane scrollPane;
	private JToolBar navigationBar = null;
	public static int noteBase = 53;
	public static double minFreq = 256.0;
	public static int noteHeight = 11;
	public static int numPercussion = 3;
	public static double bpm = 120;
	public static int maxBeats = 20;
	public static int pixelsPerBeat = 128;
	public static int totalPixels = maxBeats * pixelsPerBeat;
	public static int divisionsPerBeat = 8;
	public static int pixelsPerDivision = pixelsPerBeat / divisionsPerBeat;
	public static double secondsPerBeat = 60.0 / bpm;
	public static double maxReverbInSeconds = 2.0;
	public static double maxDelayInSeconds = 0.1;
	//public static double maxTimeInSeconds = maxBeats * secondsPerBeat + maxReverbInSeconds + maxDelayInSeconds;
	public static int leftDigits = 9;
	public static int scrollableWidth = totalPixels + SequencerUtils.digitWidth * leftDigits;
	public static double secondsPerPixel = secondsPerBeat / pixelsPerBeat;
	public static int scrollableHeight = (noteBase + 3) * noteHeight;
	public ArrayList<double[]> freqRatiosAtTimeInPixels;
	public ArrayList<MultiWindow.ModuleEditorInfo> moduleInfo;
	public int currentModuleIndex = 0;
	double[] leftSamples = null;
	double[] rightSamples = null;
	
	public void addNavigationButton(String buttonText) {
		JButton button = new JButton(buttonText);
		button.addActionListener((ActionListener) controller);
		navigationBar.add(button);
	}
	
	public JToolBar createNavigationBar() {
		navigationBar = new JToolBar("Navigation Bar");
        // Create Navigation Buttons
        addNavigationButton("Play");
        addNavigationButton("DFT");
        addNavigationButton("Open Sequencer");
        addNavigationButton("Open Project");
        addNavigationButton("Save");
        addNavigationButton("Get Module");
        addNavigationButton("Scale");
    	return navigationBar;
	}
	
    public Sequencer(MultiWindow parent) {
		super(new BorderLayout());
		this.parent = parent;
        view = new SequencerView(this);
        view.setBackground(Color.black);
        controller = new SequencerController(this);
        add(createNavigationBar(), BorderLayout.PAGE_START);
        view.addMouseListener(controller);
        view.addMouseMotionListener(controller);
        view.setPreferredSize(new Dimension(scrollableWidth, scrollableHeight));
        scrollPane = new JScrollPane(view);
        scrollPane.setSize(800, 600);
        add(scrollPane, BorderLayout.CENTER);
        freqRatiosAtTimeInPixels = new ArrayList<double[]>();
        moduleInfo = new ArrayList<MultiWindow.ModuleEditorInfo>();
        for(MultiWindow.ModuleEditorInfo info: parent.moduleEditorInfo) {
        	freqRatiosAtTimeInPixels.add(new double[maxBeats * pixelsPerBeat]);
        	double[] freqRatioAtTime = freqRatiosAtTimeInPixels.get(freqRatiosAtTimeInPixels.size() - 1);
        	for(int time = 0; time < freqRatioAtTime.length; time++) {
        		freqRatioAtTime[time] = -1.0;
        	}
        	moduleInfo.add(info);
        }
    }
    
    public int getNumActivePixels() {
    	double samplesPerPixel = (secondsPerBeat / pixelsPerBeat) * SynthTools.sampleRate;
    	int numSamples = (int) Math.round(samplesPerPixel * pixelsPerBeat * maxBeats) - 1;
    	int maxPixel = 0;
    	for(double[] pixels: freqRatiosAtTimeInPixels) {
    		int index = pixels.length - 1;
    		while(index > -1) {
    			if(pixels[index] > -1) break;
    			index--;
    		}
    		if(index > maxPixel) maxPixel = index;
    	}
    	return maxPixel;
    }
    
    public void initLeftRight() {
    	double samplesPerPixel = (secondsPerBeat / pixelsPerBeat) * SynthTools.sampleRate;
    	int numPixels = getNumActivePixels();
    	int numControlSamples = (int) Math.round(numPixels * samplesPerPixel);
    	int numTailSamples = (int) Math.round((maxDelayInSeconds + maxReverbInSeconds) * SynthTools.sampleRate);
    	leftSamples = new double[numControlSamples + numTailSamples];
    	rightSamples = new double[leftSamples.length];
    	for(int moduleIndex = 0; moduleIndex < parent.moduleEditorInfo.size(); moduleIndex++) {
    		double[] controlSamples = new double[numControlSamples];
    		double[] controlPixels = freqRatiosAtTimeInPixels.get(moduleIndex);
	    	for(int pixel = 0; pixel < numPixels; pixel++) {
	    		int startSample = (int) Math.round(pixel * samplesPerPixel);
	    		int endSample = (int) Math.round((pixel + 1) * samplesPerPixel);
	    		double controlVal = controlPixels[pixel];
	    		if(controlVal > 0.0 && controlVal < 1.0) controlVal = 1.0; // percussion
	    		for(int sample = startSample; sample < endSample; sample++) {
	    			if(sample >= numControlSamples) break;
	    			controlSamples[sample] = controlVal;
	    		}
	    	}
	    	double[] leftOut = parent.moduleEditorInfo.get(moduleIndex).getModuleEditor().getSamples(controlSamples).get(0);
	    	double[] rightOut = parent.moduleEditorInfo.get(moduleIndex).getModuleEditor().getSamples(controlSamples).get(1);
	    	if(leftOut == null || rightOut == null) continue; 
	    	for(int sample = 0; sample < leftSamples.length; sample++) {
	    		if(sample < leftOut.length) leftSamples[sample] += leftOut[sample];
	    		if(sample < rightOut.length) rightSamples[sample] += rightOut[sample];
	    	}
    	}
    }
    
    public void play() {
    	initLeftRight();
		AudioPlayer.playAudio(leftSamples, rightSamples);
    }
    
	public void dft() {
		initLeftRight();
		parent.dftEditorFrame.ModuleDFT(leftSamples, rightSamples);
	}
	
	public void scale() {
		Scale autoScale = new Scale(this, Scale.Type.MINOR_JUST_INTONATION_53, 8, 8);
		boolean rating = false;
		boolean loopAgain = true;
		while(loopAgain) {
			clearInstrument(currentModuleIndex);
			ArrayList<Integer> notes = autoScale.getNextSequence(rating);
			int index = 0;
			for(int note: notes) {
				double freqRatio = Math.pow(2.0, note / 53.0);
				for(int innerIndex = index; innerIndex < index + pixelsPerBeat - divisionsPerBeat; innerIndex++) {
					freqRatiosAtTimeInPixels.get(0)[innerIndex] = freqRatio;
				}
				index += pixelsPerBeat;
			}
			view.repaint();
			play();
			Object[] options = {"Yes", "No", "Quit"};
			Integer result = JOptionPane.showOptionDialog(this, "Do you like this sequence", "Rate Sequence", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			if(result == null) {
				loopAgain = false;
			} else {
				if(result == 0) rating = true;
				if(result == 1) rating = false;
				if(result == 2) {
					autoScale.getNextSequence(true);
					loopAgain = false;
				}
			}
		}
		autoScale.closeLogFile();
	}
    
	public void clearData() {
		for(int index = 0; index < freqRatiosAtTimeInPixels.size(); index++) {
			int numFreqRatios = freqRatiosAtTimeInPixels.get(index).length;
			for(int innerIndex = 0; innerIndex < numFreqRatios ; innerIndex++) {
				freqRatiosAtTimeInPixels.get(index)[innerIndex] = -1.0;
			}
		}
	}
	
	public void clearInstrument(int index) {
		int numFreqRatios = freqRatiosAtTimeInPixels.get(index).length;
		for(int innerIndex = 0; innerIndex < numFreqRatios ; innerIndex++) {
			freqRatiosAtTimeInPixels.get(index)[innerIndex] = -1.0;
		}
	}
	
	public void save() {
		String directory = ModuleFileTools.PromptForDirectorySave(view);
		if(directory == null) return;
		saveToDirectory(directory);
	}
	
	public void saveToDirectory(String directory) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(directory + "\\Sequencer"));
			out.write("NUM_MODULES " + parent.moduleEditorInfo.size());
			out.newLine();
			out.write("NUM_PERCUSSION " + numPercussion);
			out.newLine();
			TreeSet<String> names = new TreeSet<String>();
			out.write("START_MODULE_INFO");
			out.newLine();
			for(ModuleEditorInfo moduleEditorInfo: parent.moduleEditorInfo) {
				int index = 1;
				String originalName = moduleEditorInfo.getName();
				String uniqueName = originalName;
				while(names.contains(uniqueName)) {
					uniqueName = originalName + " (" + index + ")";
					index++;
				}
				names.add(uniqueName);
				out.write("NAME");
				out.newLine();
				out.write(uniqueName);
				out.newLine();
				out.write("RGBA_INT ");
				out.write(new Integer(moduleEditorInfo.getColor().getRed()).toString());
				out.write(" ");
				out.write(new Integer(moduleEditorInfo.getColor().getGreen()).toString());
				out.write(" ");
				out.write(new Integer(moduleEditorInfo.getColor().getBlue()).toString());
				out.write(" ");
				out.write(new Integer(moduleEditorInfo.getColor().getAlpha()).toString());
				out.newLine();
				moduleEditorInfo.getModuleEditor().saveToFile(directory + "\\" + uniqueName);
				out.write("END_MODULE");
				out.newLine();
			}
			out.write("END_MODULE_INFO");
			out.newLine();
			out.write("NOTE_BASE " + noteBase);
			out.newLine();
			out.write("BPM " + bpm);
			out.newLine();
			out.write("PIXELS_PER_BEAT " + pixelsPerBeat);
			out.newLine();
			int numPixels = getNumActivePixels();
			out.write("NUM_PIXELS " + numPixels);
			out.newLine();
			out.write("START_DATA");
			out.newLine();
			int currentModule = 0;
			for(double[] freqRatios: freqRatiosAtTimeInPixels) {
				for(int index = 0; index < numPixels; index++) {
					if(freqRatios[index] > 0) {
						out.write(currentModule + " " + index + " " + Math.round(Math.log(freqRatios[index]) / Math.log(2.0) * noteBase));
						out.newLine();
					}
				}
				currentModule++;
			}
			out.write("END_DATA");
			out.newLine();			
			out.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "There was a problem saving the file");
			return;
		}
		JOptionPane.showMessageDialog(this, "Finished Saving File");
		//this.setTitle(filename);
	}
	
	public void openSequencer() {
		String directory = ModuleFileTools.PromptForDirectoryOpen(view);
		if(directory == null) return;
		loadFromDirectory(directory, false);
		view.repaint();
	}
	
	public void openProject() {
		String directory = ModuleFileTools.PromptForDirectoryOpen(view);
		if(directory == null) return;
		loadFromDirectory(directory, true);
		view.repaint();
	}
	
	public void loadFromDirectory(String filename, boolean project) {
		String currentLine = null;
		try {
			int numModulesIn = -1;
			int numPercussionIn = -1;
			String[] lineParams = null;
			BufferedReader in = new BufferedReader(new FileReader(filename + "\\Sequencer"));
			currentLine = in.readLine();
			while(!currentLine.equals("START_MODULE_INFO")) {
				lineParams = currentLine.split(" ");
				if(lineParams[0].equals("NUM_MODULES")) {
					numModulesIn = new Integer(lineParams[1]);
				}
				if(lineParams[0].equals("NUM_PERCUSSION")) {
					numPercussionIn = new Integer(lineParams[1]);
				}
				currentLine = in.readLine();
			}
			if(numModulesIn == -1 || numPercussionIn == -1) {
				JOptionPane.showMessageDialog(this, "Project File Corrupted");
				return;
			}
			if((numModulesIn > parent.moduleEditorInfo.size() || numPercussionIn > numPercussion) && !project) {
				JOptionPane.showMessageDialog(this, "Current configuration is not compatible with sequence");
				return;
			}
			if(project) {
				numPercussion = numPercussionIn;
				ArrayList<ModuleEditorInfo> moduleEditorInfo = new ArrayList<ModuleEditorInfo>();
				// next line after MODULE_EDITOR_INFO
				currentLine = in.readLine();
				while(!currentLine.equals("END_MODULE_INFO")) {
					String name = null;
					Color color = null;
					while(!currentLine.equals("END_MODULE")) {
						lineParams = currentLine.split(" ");
						if(currentLine.equals("NAME")) {
							name = in.readLine();
						}
						if(lineParams[0].equals("RGBA_INT")) {
							int red = new Integer(lineParams[1]);
							int green = new Integer(lineParams[2]);
							int blue = new Integer(lineParams[3]);
							int alpha = new Integer(lineParams[4]);
							color = new Color(red, green, blue, alpha);
						}
						currentLine = in.readLine();
					}
					moduleEditorInfo.add(new MultiWindow.ModuleEditorInfo(name, color, new ModuleEditor(parent, moduleEditorInfo.size())));
					moduleEditorInfo.get(moduleEditorInfo.size() - 1).moduleEditor.loadFromFile(filename + "\\" + name);
					if(moduleEditorInfo.size() > numModulesIn) {
						JOptionPane.showMessageDialog(this, "There was a problem loading modules");
						return;
					}
					currentLine = in.readLine();
				}
				parent.newProject(moduleEditorInfo);
				numPercussion = numPercussionIn;
			} else {
				// SKIP OVER MODULE DATA
				while(!currentLine.equals("END_MODULE_INFO")) {
					currentLine = in.readLine();
				}
			}
			currentLine = in.readLine();
			while(!currentLine.equals("START_DATA")) {
				lineParams = currentLine.split(" ");
				if(lineParams[0].equals("NOTE_BASE")) {
					noteBase = new Integer(lineParams[1]);
				}
				if(lineParams[0].equals("BPM")) {
					bpm = new Double(lineParams[1]);
				}
				if(lineParams[0].equals("PIXELS_PER_BEAT")) {
					pixelsPerBeat = new Integer(lineParams[1]);
				}
				if(lineParams[0].equals("NUM_PIXELS")) {
					int numPixelsIn = new Integer(lineParams[1]);
					if(numPixelsIn > totalPixels) {
						JOptionPane.showMessageDialog(this, "Default Sequencer Length Exceeded");
						return;
					}
					freqRatiosAtTimeInPixels = new ArrayList<double[]>();
					for(int index = 0; index < parent.moduleEditorInfo.size(); index++) {
						freqRatiosAtTimeInPixels.add(new double[totalPixels]);
						for(int i2 = 0; i2 < freqRatiosAtTimeInPixels.get(index).length; i2++) {
							freqRatiosAtTimeInPixels.get(index)[i2] = -1;
						}
					}
				}
				currentLine = in.readLine();
			}
			currentLine = in.readLine();
			while(!currentLine.equals("END_DATA")) {
				lineParams = currentLine.split(" ");
				int currentModule = new Integer(lineParams[0]);
				int index = new Integer(lineParams[1]);
				double freqRatio = Math.pow(2.0, new Double(lineParams[2]) / noteBase);
				freqRatiosAtTimeInPixels.get(currentModule)[index] = freqRatio;
				currentLine = in.readLine();
			}
			in.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.toString());
			return;
		}
		//this.setTitle(filename);
		JOptionPane.showMessageDialog(this, "Finished Loading File");
	}

}
