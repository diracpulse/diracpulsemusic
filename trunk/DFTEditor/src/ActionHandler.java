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
			parent.saveSelectedToFile();
		}
	}
		
	public class ExportAllAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public ExportAllAction() {
			super("Export All Selected");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Export All Selected");
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
	
	public class PlayLinearAction extends AbstractAction {

		private static final long serialVersionUID = 7354387706903212877L;

		public PlayLinearAction() {
			super("Play Linear");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			parent.playSelectedDataInCurrentWindowLinear();
			System.out.println("Play Linear");
		}
	}
	
	public class PlayLinearCubicSplineAction extends AbstractAction {

		private static final long serialVersionUID = 7354387706903212877L;

		public PlayLinearCubicSplineAction() {
			super("Play Linear Cubic Spline");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			parent.playSelectedDataInCurrentWindowLinearCubicSpline();
			System.out.println("Play Linear Cubic Spline");
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
	
	public class ViewMusicAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public ViewMusicAction() {
			super("Music");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("View Music");
			DFTView.setView(DFTView.View.Music);
			DFTEditor.view.repaint();
		}
	}
	
	public class UndoSelectionAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public UndoSelectionAction() {
			super("Undo Selection");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Undo Selection");
			DFTEditor.undoPreviousSelection();
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
	
	public class HarmonicsViewAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public HarmonicsViewAction() {
			super("Harmonics");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Harmonics");
			DFTView.setDataView(DFTView.DataView.HARMONICS);
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
			DFTEditor.setSelectionArea(Selection.Area.LINE_INTERPOLATE);
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
	
	public class AllAreaAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public AllAreaAction() {
			super("All");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("All");
			DFTEditor.setSelectionArea(Selection.Area.ALL);
		}
	}
	
	public class AddSelectionAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public AddSelectionAction() {
			super("Add Selection");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Add Selection");
			DFTEditor.deleteSelected = false;
		}
	}
	
	public class DeleteSelectionAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public DeleteSelectionAction() {
			super("Delete Selection");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Delete Selection");
			DFTEditor.deleteSelected = true;
			DFTEditor.clearCurrentSelection();
		}
	}
	
	public class SelectCutoffAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;
		private int logCutoff;
		
		public SelectCutoffAction(int logCutoff) {
			super("Cutoff = " + logCutoff);
			this.logCutoff = logCutoff;
		}
		
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Cutoff = " + logCutoff);
			DFTEditor.minLogAmplitudeThreshold = logCutoff;
		    DFTEditor.autoSelect();
		    DFTEditor.view.repaint();
		    SynthTools.refresh = true;
		    parent.playSelectedDataInCurrentWindow();
		}		
		
	}
	
	public class SelectMinLengthAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;
		private int minLength;
		
		public SelectMinLengthAction(int minLength) {
			super("Min Length = " + minLength);
			this.minLength = minLength;
		}
		
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("MinLength = " + minLength);
			DFTEditor.minHarmonicLength = minLength;
			SynthTools.refresh = true;
		    parent.playSelectedDataInCurrentWindow();
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
        fileMenu.add(new ExportAllAction());
        fileMenu.add(new ExitAction());
        //Create the Play menu
        JMenu playMenu = new JMenu("Play");
        menuBar.add(playMenu);        
        playMenu.add(new JMenuItem(new PlayHarmonicsAction()));
        playMenu.add(new JMenuItem(new PlayLinearAction()));
        playMenu.add(new JMenuItem(new PlayLinearCubicSplineAction()));
        JMenu viewMenu = new JMenu("View");
        menuBar.add(viewMenu);
        viewMenu.add(new JMenuItem(new ViewMusicAction())); 
        viewMenu.add(new JMenuItem(new ViewPixels1Action()));
        viewMenu.add(new JMenuItem(new ViewPixels2Action()));
        viewMenu.add(new JMenuItem(new ViewPixels3Action()));    
        viewMenu.add(new JMenuItem(new ViewDigits1Action()));
        viewMenu.add(new JMenuItem(new ViewDigits2Action())); 
        JMenu editMenu = new JMenu("Edit");
        editMenu.add(new JMenuItem(new UndoSelectionAction()));
        menuBar.add(editMenu);
        JMenu dataViewMenu = new JMenu("DataView");
        menuBar.add(dataViewMenu);
        dataViewMenu.add(new JMenuItem(new DataAndMaximasViewAction()));
        dataViewMenu.add(new JMenuItem(new MaximasViewAction()));
        dataViewMenu.add(new JMenuItem(new DataViewAction()));
        dataViewMenu.add(new JMenuItem(new HarmonicsViewAction()));
        JMenu selectionAreaMenu = new JMenu("SelectionArea");
        menuBar.add(selectionAreaMenu);
        selectionAreaMenu.add(new JMenuItem(new LineAreaAction()));
        selectionAreaMenu.add(new JMenuItem(new RectangleAreaAction()));
        selectionAreaMenu.add(new JMenuItem(new TriangleAreaAction()));
        selectionAreaMenu.add(new JMenuItem(new AllAreaAction()));
        JMenu addDeleteMenu = new JMenu("Add/Delete");
        menuBar.add(addDeleteMenu);
        addDeleteMenu.add(new JMenuItem(new AddSelectionAction()));
        addDeleteMenu.add(new JMenuItem(new DeleteSelectionAction()));
        JMenu cutoffMenu = new JMenu("Cutoff");
        menuBar.add(cutoffMenu);
        for(int logCutoff = 12; logCutoff > 2; logCutoff--) {
        	cutoffMenu.add(new SelectCutoffAction(logCutoff));
        }
        JMenu minLengthMenu = new JMenu("MinLength");
        menuBar.add(minLengthMenu);      
        for(int minLength = 1; minLength <= 20; minLength++) {
        	minLengthMenu.add(new SelectMinLengthAction(minLength));
        }
        return menuBar;
	}

}
