import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;


public class FDActionHandler extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2754195729464045141L;
	private FDEditor parent;
	
	public FDActionHandler(FDEditor parent) {
		this.parent = parent;
	}
	
	public class OpenAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 8330693329974727658L;

		public OpenAction() {
			super("Open");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			parent.openFileInFDEditor();
		}
	}	
	
	public class SaveAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public SaveAction() {
			super("Save Selected Harmonics");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			parent.saveSelectedHarmonicsToFile();
		}
	}	

	public class ExitAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 6690671099967989664L;

		public ExitAction() {
			super("Exit");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.exit(0);
		}
	}

	public class PlayAllAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -9196870261707581557L;

		public PlayAllAction() {
			super("Play All");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Play\n");
			FDEditor.playDataInCurrentWindow(parent);
		}
	}
	
	public class PlaySelectedAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -9196870261707581557L;

		public PlaySelectedAction() {
			super("Play Selected");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Play Selected\n");
			FDEditor.playSelectedDataInCurrentWindow(parent);
		}
	}	
	
	public class TestAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		public TestAction() {
			super("Test");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Test\n");
			MathTools mt = new MathTools();
			mt.runTests();
		}
	}
	
	public class ViewAmplitudesAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		public ViewAmplitudesAction() {
			super("Amplitudes");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("View Amplitudes");
			FDView.dataView = FDView.DataView.AMPLITUDES;
			FDEditor.refreshView();
		}
	}
	
	public class ViewHarmonicsAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		public ViewHarmonicsAction() {
			super("Harmonics");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("View Harmonics");
			FDView.dataView = FDView.DataView.HARMONICS;
			FDEditor.refreshView();
		}
	}
	
	public class ViewSelectedAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		public ViewSelectedAction() {
			super("Selected");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("View Selected");
			FDView.dataView = FDView.DataView.SELECTED;
			FDEditor.refreshView();
		}
	}
	
	public class ViewSelectedOnlyAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		public ViewSelectedOnlyAction() {
			super("Selected Only");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("View Selected Only");
			FDView.dataView = FDView.DataView.SELECTED_ONLY;
			FDEditor.refreshView();
		}
	}
	
	public class FlattenAllHarmonicsAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		public FlattenAllHarmonicsAction() {
			super("Flatten All");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Flatten All Harmonics");
			FDEditor.flattenHarmonics();
			FDEditor.refreshView();
		}
	}
	
	public JMenuBar createMenuBar() {
        //Create the menu bar.
        JMenuBar menuBar = new JMenuBar();
        //Create the File menu
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        fileMenu.add(new OpenAction());
        fileMenu.add(new SaveAction());
        fileMenu.add(new ExitAction());
        //Create the Play menu
        JMenu playMenu = new JMenu("Play");
        menuBar.add(playMenu);        
        playMenu.add(new JMenuItem(new PlayAllAction()));
        playMenu.add(new JMenuItem(new PlaySelectedAction()));
        JMenu viewMenu = new JMenu("View");
        viewMenu.add(new ViewAmplitudesAction());
        viewMenu.add(new ViewHarmonicsAction());
        viewMenu.add(new ViewSelectedAction());
        viewMenu.add(new ViewSelectedOnlyAction());
        menuBar.add(viewMenu);
        JMenu harmonicsMenu = new JMenu("Harmonics");
        harmonicsMenu.add(new FlattenAllHarmonicsAction());
        menuBar.add(harmonicsMenu);
        return menuBar;
	}

}
