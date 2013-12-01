package main;

import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class FileTools {

	public static String PromptForFileOpenWAV(Component c) {
	   	JFileChooser fc = new JFileChooser();
	   	FileTools.WAVFilter filter = new FileTools.WAVFilter();
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setCurrentDirectory(new File("."));
		fc.setFileFilter(filter);
        int returnVal = fc.showOpenDialog(c);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
        	File file = fc.getSelectedFile();
        	return fc.getCurrentDirectory() + "/" + file.getName();
    	} else {
	    	System.exit(0);
    	}
    	return "";
	}
	
	public static String PromptForFileOpen(Component c) {
	   	JFileChooser fc = new JFileChooser();
	   	FileTools.Mono5msFilter filter = new FileTools.Mono5msFilter();
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setCurrentDirectory(new File("."));
		fc.setFileFilter(filter);
        int returnVal = fc.showOpenDialog(c);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
        	File file = fc.getSelectedFile();
        	return fc.getCurrentDirectory() + "/" + file.getName();
    	} else {
	    	System.exit(0);
    	}
    	return "";
	}
	
	public static String PromptForFileSave(Component c) {
	   	JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int returnVal = fc.showSaveDialog(c);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
        	File file = fc.getSelectedFile();
            return file.getName();
    	} else {
	    	System.exit(0);
    	}
    	return "";
	}
	
	public static class Mono5msFilter extends FileFilter {
		
		public Mono5msFilter() {};
		
		public boolean accept(File f) {
			if(f.getName().contains(".mono5ms")) return true;
			if(f.isDirectory()) return true;
			return false;
		}
		
		public String getDescription() {
			return new String(".mono5ms files");
		}
	}
	
	public static class WAVFilter extends FileFilter {
		
		public WAVFilter() {};
		
		public boolean accept(File f) {
			if(f.getName().contains(".wav")) return true;
			if(f.isDirectory()) return true;
			return false;
		}
		
		public String getDescription() {
			return new String(".wav files");
		}
	}

}
