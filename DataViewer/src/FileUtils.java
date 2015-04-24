import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;


public class FileUtils {

	public static String PromptForFileOpen(Component c) {
	   	JFileChooser fc = new JFileChooser();
	   	//ModuleFileTools.DPMSynthFilter filter = new ModuleFileTools.DPMSynthFilter();
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setCurrentDirectory(new File(".//Patches//"));
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
		fc.setCurrentDirectory(new File(".//Patches//"));
		//fc.setFileFilter(filter);
        int returnVal = fc.showSaveDialog(c);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
        	File file = fc.getSelectedFile();
            return fc.getCurrentDirectory() + "\\" + file.getName();
    	} else {
	    	return null;
    	}
	}

}
