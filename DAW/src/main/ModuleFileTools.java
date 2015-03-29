package main;

import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class ModuleFileTools {

	public static String PromptForFileOpen(Component c) {
	   	JFileChooser fc = new JFileChooser();
	   	//ModuleFileTools.DPMSynthFilter filter = new ModuleFileTools.DPMSynthFilter();
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setCurrentDirectory(new File(".//DPMSynth//"));
		//fc.setFileFilter(filter);
        int returnVal = fc.showOpenDialog(c);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
        	File file = fc.getSelectedFile();
        	return fc.getCurrentDirectory() + "\\" + file.getName();
    	} else {
	    	return null;
    	}
	}
	
	public static String PromptForFileSave(Component c) {
	   	JFileChooser fc = new JFileChooser();
	   	//ModuleFileTools.DPMSynthFilter filter = new ModuleFileTools.DPMSynthFilter();
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setCurrentDirectory(new File(".//DPMSynth//"));
		//fc.setFileFilter(filter);
        int returnVal = fc.showSaveDialog(c);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
        	File file = fc.getSelectedFile();
            return fc.getCurrentDirectory() + "\\" + file.getName();
    	} else {
	    	return null;
    	}
	}
	
	public static String PromptForDirectoryOpen(Component c) {
	   	JFileChooser fc = new JFileChooser();
	   	//ModuleFileTools.DPMSynthFilter filter = new ModuleFileTools.DPMSynthFilter();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setCurrentDirectory(new File("."));
		//fc.setFileFilter(filter);
        int returnVal = fc.showOpenDialog(c);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
        	File file = fc.getSelectedFile();
        	return file.toString();
    	} else {
	    	return null;
    	}
	}
	
	public static String PromptForDirectorySave(Component c) {
	   	JFileChooser fc = new JFileChooser();
	   	//ModuleFileTools.DPMSynthFilter filter = new ModuleFileTools.DPMSynthFilter();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setCurrentDirectory(new File("."));
		//fc.setFileFilter(filter);
        int returnVal = fc.showSaveDialog(c);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
        	File file = fc.getSelectedFile();
            return file.toString();
    	} else {
	    	return null;
    	}
	}
	
	public static class DPMSynthFilter extends FileFilter {
		
		public DPMSynthFilter() {};
		
		public boolean accept(File f) {
			if(f.getName().contains(".dpmsynth")) return true;
			if(f.isDirectory()) return true;
			return false;
		}
		
		public String getDescription() {
			return new String(".dpmsynth files");
		}
	}
	
}
