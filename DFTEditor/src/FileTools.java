import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;

public class FileTools {

	public static String PromptForFileOpen(Component c) {
	   	JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int returnVal = fc.showOpenDialog(c);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
        	File file = fc.getSelectedFile();
            return file.getName();
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

}
