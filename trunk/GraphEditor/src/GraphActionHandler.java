import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

public class GraphActionHandler extends JPanel {

	private static final long serialVersionUID = 1L;
	private GraphEditor parent;
	
	public GraphActionHandler(GraphEditor parent) {
		this.parent = parent;
	}
	
	public class OpenAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		public OpenAction() {
			super("Open");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			parent.openFileInGraphEditor(".saved");
		}
	}	
	
	public class PlayWindowAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public PlayWindowAction() {
			super("Play Window");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			GraphEditor.playDataInCurrentWindow(parent);
		}
	}
	
	public class PlaySequencerAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public PlaySequencerAction() {
			super("Play Sequencer");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			GraphEditor.playDataInSequencer(parent);
		}
	}
	
	public class PlayControlPointsAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public PlayControlPointsAction() {
			super("Play Control Points");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			GraphEditor.playDataInControlPoints(parent);
		}
	}
	
	public class SelectPlaySpeedAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;
		private double playSpeed;
		
		public SelectPlaySpeedAction(double playSpeed) {
			super("Play Speed = " + playSpeed);
			this.playSpeed = playSpeed;
		}
		
		public void actionPerformed(ActionEvent arg0) {
			SynthTools.playSpeed = playSpeed;
		}
		
	}
	
	public class ToggleClipZeroAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		public ToggleClipZeroAction() {
			super("Toggle Clip Zero");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			GraphEditor.toggleClipZero();
		}
	}
	
	public class ToggleDisplaySelectedAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		public ToggleDisplaySelectedAction() {
			super("Toggle Display Selected");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			GraphEditor.toggleDisplaySelected();
		}
	}
	
	public class ToggleDisplayUnselectedAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		public ToggleDisplayUnselectedAction() {
			super("Toggle Display Unselected");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			GraphEditor.toggleDisplayUnselected();
		}
	}	
	
	public class ZoomResetAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		public ZoomResetAction() {
			super("Zoom Reset");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			GraphEditor.resetView();
		}
	}
	
	public class ColorViewAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		public ColorViewAction() {
			super("Select Color View");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			GraphEditor.promptForColorView(parent);
		}
	}
	
	public class YViewAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		public YViewAction() {
			super("Select Y View");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			GraphEditor.promptForYView(parent);
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
			GraphEditor.minHarmonicLength = minLength;
		    GraphEditor.view.repaint();
		}		
		
	}
	
	public class NewCPHarmonicAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;
		
		public NewCPHarmonicAction() {
			super("New CP Harmonic");
		}
		
		public void actionPerformed(ActionEvent arg0) {
			GraphEditor.newControlPointHarmonic();
		    GraphEditor.view.repaint();
		}		
		
	}

	public JMenuBar createMenuBar() {
        //Create the menu bar.
        JMenuBar menuBar = new JMenuBar();
        //Create the File menu
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        fileMenu.add(new OpenAction());
        JMenu playMenu = new JMenu("Play");
        menuBar.add(playMenu);
        playMenu.add(new PlayWindowAction());
        playMenu.add(new PlaySequencerAction());
        playMenu.add(new PlayControlPointsAction());
        JMenu viewMenu = new JMenu("View");
        menuBar.add(viewMenu);
        viewMenu.add(new ToggleClipZeroAction());
        viewMenu.add(new ToggleDisplaySelectedAction());
        viewMenu.add(new ToggleDisplayUnselectedAction());
        JMenu zoomMenu = new JMenu("Zoom");
        menuBar.add(zoomMenu);
        zoomMenu.add(new ZoomResetAction());
        JMenu colorMenu = new JMenu("Color");
        menuBar.add(colorMenu);
        colorMenu.add(new ColorViewAction());
        JMenu yViewMenu = new JMenu("YView");
        menuBar.add(yViewMenu);
        yViewMenu.add(new YViewAction());
        JMenu controlPointMenu = new JMenu("ControlPoint");
        menuBar.add(controlPointMenu);
        controlPointMenu.add(new NewCPHarmonicAction());
        JMenu minLengthMenu = new JMenu("MinLength");
        menuBar.add(minLengthMenu);      
        for(int minLength = 1; minLength <= 20; minLength++) {
        	minLengthMenu.add(new SelectMinLengthAction(minLength));
        }
        JMenu playSpeedMenu = new JMenu("PlaySpeed");
        menuBar.add(playSpeedMenu);
        for(double playSpeed = 0.3; playSpeed <= 2.1; playSpeed += 0.1) {
        	playSpeedMenu.add(new SelectPlaySpeedAction(playSpeed));
        }
        return menuBar;
	}

}
