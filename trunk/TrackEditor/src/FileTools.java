import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class FileTools {

	public static String PromptForFileOpen(Component c, String extension) {
	   	JFileChooser fc = new JFileChooser();
	   	//FileTools.GeneralFilter filter = new FileTools.GeneralFilter(extension);
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setCurrentDirectory(new File("."));
		//fc.setFileFilter(filter);
        int returnVal = fc.showOpenDialog(c);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
        	File file = fc.getSelectedFile();
            return fc.getCurrentDirectory() + "/" + file.getName();
    	} else {
	    	//System.exit(0);
    	}
    	return "";
	}
	
	public static File[] PromptForDirectoryOpen(Component c) {
	   	JFileChooser fc = new JFileChooser();
	   	//FileTools.GeneralFilter filter = new FileTools.GeneralFilter(extension);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setCurrentDirectory(new File("."));
		//fc.setFileFilter(filter);
        int returnVal = fc.showOpenDialog(c);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File[] files = fc.getSelectedFile().listFiles();
            return files;
    	} else {
	    	//System.exit(0);
    	}
    	return null;
	}
	
	public static String PromptForFileSave(Component c) {
	   	JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int returnVal = fc.showSaveDialog(c);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
        	File file = fc.getSelectedFile();
            return file.getName();
    	} else {
	    	//System.exit(0);
    	}
    	return "";
	}
	
	public static class GeneralFilter extends FileFilter {
		
		String extension;
		
		public GeneralFilter(String extension) {
			this.extension = extension;
		};
		
		public boolean accept(File f) {
			if(f.getName().contains(extension)) return true;
			if(f.isDirectory()) return true;
			return false;
		}
		
		public String getDescription() {
			return new String(extension + " files");
		}
	}

}
