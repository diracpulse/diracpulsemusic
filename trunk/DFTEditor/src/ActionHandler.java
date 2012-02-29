import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;


public class ActionHandler extends JPanel {

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

	public class SaveSelectedAction extends AbstractAction {

		private static final long serialVersionUID = -4814209505628569590L;

		public SaveSelectedAction() {
			super("Save Selected");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Save Selected");
		}
	}
	
	public class ExportAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public ExportAction() {
			super("Export");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Export");
			parent.exportFileInDFTEditor();
		}
	}
	
	public class ExportAllAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public ExportAllAction() {
			super("Export All");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Export All");
			parent.exportAllFiles();
		}
	}
	
	public class PlayHarmonicsAction extends AbstractAction {

		private static final long serialVersionUID = 2018379987198757465L;

		public PlayHarmonicsAction() {
			super("Play Harmonics");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			parent.playSelectedDataInCurrentWindow();
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

		private static final long serialVersionUID = 1L;

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

		private static final long serialVersionUID = 1L;

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
	
	public class ViewPixels3Action extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public ViewPixels3Action() {
			super("Pixels = 3");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("View Pixels3");
			DFTView.setView(DFTView.View.Pixels3);
			DFTEditor.view.repaint();
		}
	}
	
	public class DataAndMaximasViewAction extends AbstractAction {

		private static final long serialVersionUID = 270072643328180860L;

		public DataAndMaximasViewAction() {
			super("Data and Maximas");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Data and Maximas");
			DFTView.setDataView(DFTView.DataView.DATA_AND_MAXIMAS);
			DFTEditor.view.repaint();
		}
	}
	
	public class MaximasViewAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public MaximasViewAction() {
			super("Maximas");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Maximas");
			DFTView.setDataView(DFTView.DataView.MAXIMAS_ONLY);
			DFTEditor.view.repaint();
		}
	}
	
	public class DataViewAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public DataViewAction() {
			super("Data");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Data");
			DFTView.setDataView(DFTView.DataView.DATA_ONLY);
			DFTEditor.view.repaint();
		}
	}
	
	public class LineAreaAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public LineAreaAction() {
			super("Line");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Line");
			DFTEditor.setSelectionArea(Selection.Area.LINE);
		}
	}
	
	public class RectangleAreaAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public RectangleAreaAction() {
			super("Rectangle");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Rectangle");
			DFTEditor.setSelectionArea(Selection.Area.RECTANGLE);
		}
	}
	
	public class TriangleAreaAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public TriangleAreaAction() {
			super("Triangle");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Triangle");
			DFTEditor.setSelectionArea(Selection.Area.TRIANGLE);
		}
	}
	
	public JMenuBar createMenuBar() {
        //Create the menu bar.
        JMenuBar menuBar = new JMenuBar();
        //Create the File menu
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        fileMenu.add(new OpenAction());
        fileMenu.add(new SaveSelectedAction());
        fileMenu.add(new ExportAction());
        fileMenu.add(new ExportAllAction());
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
        viewMenu.add(new JMenuItem(new ViewPixels3Action()));    
        viewMenu.add(new JMenuItem(new ViewDigits1Action()));
        viewMenu.add(new JMenuItem(new ViewDigits2Action())); 
        JMenu dataViewMenu = new JMenu("DataView");
        menuBar.add(dataViewMenu);
        dataViewMenu.add(new JMenuItem(new DataAndMaximasViewAction()));
        dataViewMenu.add(new JMenuItem(new MaximasViewAction()));
        dataViewMenu.add(new JMenuItem(new DataViewAction()));
        JMenu selectionAreaMenu = new JMenu("SelectionArea");
        menuBar.add(selectionAreaMenu);
        selectionAreaMenu.add(new JMenuItem(new LineAreaAction()));
        selectionAreaMenu.add(new JMenuItem(new RectangleAreaAction()));
        selectionAreaMenu.add(new JMenuItem(new TriangleAreaAction()));
        return menuBar;
	}

}
