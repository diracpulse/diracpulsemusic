import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;


public class HarmonicsAction extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 672340751348574007L;
	private HarmonicsEditor parent;
	
	public HarmonicsAction(HarmonicsEditor parent) {
		this.parent = parent;
	}
	
	public class OpenAction extends AbstractAction {

		private static final long serialVersionUID = -5323292053150793042L;

		public OpenAction() {
			super("Open");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			parent.openFileInHarmonicsEditor();
		}
	}	

	public class ExitAction extends AbstractAction {

		private static final long serialVersionUID = -3384173774207253726L;

		public ExitAction() {
			super("Exit");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.exit(0);
		}
	}

	public class ImportAction extends AbstractAction {

		private static final long serialVersionUID = -4814209505628569590L;

		public ImportAction() {
			super("Import");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Import");
		}
	}
	
	public class PlayHarmonicsAction extends AbstractAction {

		private static final long serialVersionUID = 2018379987198757465L;

		public PlayHarmonicsAction() {
			super("Play Harmonics");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Play Harmonics");
		}
	}
	
	public class PlayWAVAction extends AbstractAction {

		private static final long serialVersionUID = 7354387706903212877L;

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
