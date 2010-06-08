import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;


public class ActionHandler extends JPanel {

	private DFTEditor parent;
	
	public ActionHandler(DFTEditor parent) {
		this.parent = parent;
	}
	
	public class OpenAction extends AbstractAction {

		public OpenAction() {
			super("Open");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			parent.openFileInDFTEditor();
		}
	}	

	public class ExitAction extends AbstractAction {

		public ExitAction() {
			super("Exit");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.exit(0);
		}
	}

	public class ImportAction extends AbstractAction {

		public ImportAction() {
			super("Import");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Import");
		}
	}
	
	public class PlayHarmonicsAction extends AbstractAction {

		public PlayHarmonicsAction() {
			super("Play Harmonics");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Play Harmonics");
		}
	}
	
	public class PlayWAVAction extends AbstractAction {

		public PlayWAVAction() {
			super("Play WAV");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Play WAV");
		}
	}
	
	public JMenuBar createMenuBar() {
        //Create the menu bar.
        JMenuBar menuBar = new JMenuBar();
        //Create the File menu
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        fileMenu.add(new OpenAction());
        fileMenu.add(new ImportAction());
        fileMenu.add(new ExitAction());
        //Create the Play menu
        JMenu playMenu = new JMenu("Play");
        menuBar.add(playMenu);        
        playMenu.add(new JMenuItem(new PlayHarmonicsAction()));
        playMenu.add(new JMenuItem(new PlayWAVAction()));
        return menuBar;
	}

}
