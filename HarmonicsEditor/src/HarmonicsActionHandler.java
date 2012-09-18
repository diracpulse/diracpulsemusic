import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;


public class HarmonicsActionHandler extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2754195729464045141L;
	private HarmonicsEditor parent;
	
	public HarmonicsActionHandler(HarmonicsEditor parent) {
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
			parent.openFileInHarmonicsEditor(".saved");
		}
	}	

	public class SaveAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 6690671099967989664L;

		public SaveAction() {
			super("Save");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			HarmonicsFileOutput.OutputSelectedToFile();
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
			HarmonicsEditor.playSelectedDataInCurrentWindow(parent);
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
	
	public class ViewPixelsAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		public ViewPixelsAction() {
			super("Pixels");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("View Amplitudes");
			HarmonicsView.dataView = HarmonicsView.DataView.PIXELS;
			HarmonicsEditor.refreshView();
		}
	}
	
	public class ViewDigitsAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		public ViewDigitsAction() {
			super("Digits");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("View Harmonics");
			HarmonicsView.dataView = HarmonicsView.DataView.DIGITS;
			HarmonicsEditor.refreshView();
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
			HarmonicsEditor.flattenAllHarmonics();
			HarmonicsEditor.refreshView();
		}
	}
	
	public class RandomLoopAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		public RandomLoopAction() {
			super("Random Loop");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Random Loop");
			HarmonicsEditor.playRandomLoop(parent);
		}
	}
	
	public class NewRandomLoopAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		public NewRandomLoopAction() {
			super("New Random Loop");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("New Random Loop");
			HarmonicsEditor.newRandomLoop(parent);
		}
	}
	
	public class PlayScaleAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		public PlayScaleAction() {
			super("Play Scale");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("PlayScale");
			HarmonicsEditor.playScale(parent);
		}
	}
	
	public class LoadInstrumentAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		public LoadInstrumentAction() {
			super("Load Instrument");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Load Instrument");
			parent.loadInstrument();
		}
	}
	
	public class LoadKickDrumAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		public LoadKickDrumAction() {
			super("Load Kick Drum");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Load Kick Drum");
			parent.loadKickDrum();
		}
	}
	
	public class LoadHighFreqAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		public LoadHighFreqAction() {
			super("Load High Freq");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Load High Freq");
			parent.loadHighFreq();
		}
	}
	
	public class LoadBassSynthAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		public LoadBassSynthAction() {
			super("Load Bass Synth");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Load Bass Synth");
			parent.loadBassSynth();
		}
	}
	
	public class LoadSnareAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		public LoadSnareAction() {
			super("Load Snare");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Load Snare");
			parent.loadSnare();
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
        //Create the Play menu
        JMenu playMenu = new JMenu("Play");
        menuBar.add(playMenu);        
        playMenu.add(new JMenuItem(new PlayAllAction()));
        JMenu viewMenu = new JMenu("View");
        viewMenu.add(new ViewPixelsAction());
        viewMenu.add(new ViewDigitsAction());
        menuBar.add(viewMenu);
        JMenu harmonicsMenu = new JMenu("Harmonics");
        harmonicsMenu.add(new FlattenAllHarmonicsAction());
        menuBar.add(harmonicsMenu);
        JMenu loopMenu = new JMenu("Loop");
        loopMenu.add(new RandomLoopAction());
        menuBar.add(loopMenu);
        JMenu scaleMenu = new JMenu("Scale");
        scaleMenu.add(new PlayScaleAction());
        menuBar.add(scaleMenu);
        JMenu instrumentMenu = new JMenu("Instrument");
        instrumentMenu.add(new LoadInstrumentAction());
        instrumentMenu.add(new LoadKickDrumAction());
        instrumentMenu.add(new LoadHighFreqAction());
        instrumentMenu.add(new LoadBassSynthAction());
        instrumentMenu.add(new LoadSnareAction());
        menuBar.add(instrumentMenu);
        JMenu newMenu = new JMenu("New");
        newMenu.add(new NewRandomLoopAction());
        menuBar.add(newMenu);
        return menuBar;
	}

}
