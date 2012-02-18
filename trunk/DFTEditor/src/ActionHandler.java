import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;


public class ActionHandler extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 672340751348574007L;
	private DFTEditor parent;
	
	public ActionHandler(DFTEditor parent) {
		this.parent = parent;
	}
	
	public class OpenAction extends AbstractAction {

		private static final long serialVersionUID = -5323292053150793042L;

		public OpenAction() {
			super("Open");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			parent.openFileInDFTEditor();
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
	
	public class ViewDigits1Action extends AbstractAction {

		private static final long serialVersionUID = 5910687309575402310L;

		public ViewDigits1Action() {
			super("Digits = 1");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("View Digits1");
			DFTView.setView(DFTView.View.Digits1);
			DFTEditor.view.repaint();
		}
	}
	
	public class ViewDigits2Action extends AbstractAction {

		public ViewDigits2Action() {
			super("Digits = 2");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("View Digits2");
			DFTView.setView(DFTView.View.Digits2);
			DFTEditor.view.repaint();
		}
	}
	
	public class ViewPixels1Action extends AbstractAction {

		private static final long serialVersionUID = -1592520256131827974L;

		public ViewPixels1Action() {
			super("Pixels = 1");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("View Pixels1");
			DFTView.setView(DFTView.View.Pixels1);
			DFTEditor.view.repaint();
		}
	}
	
	public class ViewPixels2Action extends AbstractAction {

		private static final long serialVersionUID = 270072643328180860L;

		public ViewPixels2Action() {
			super("Pixels = 2");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("View Pixels2");
			DFTView.setView(DFTView.View.Pixels2);
			DFTEditor.view.repaint();
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
        JMenu viewMenu = new JMenu("View");
        menuBar.add(viewMenu);        
        viewMenu.add(new JMenuItem(new ViewPixels1Action()));
        viewMenu.add(new JMenuItem(new ViewPixels2Action()));    
        viewMenu.add(new JMenuItem(new ViewDigits1Action()));
        viewMenu.add(new JMenuItem(new ViewDigits2Action()));
        return menuBar;
	}

}
