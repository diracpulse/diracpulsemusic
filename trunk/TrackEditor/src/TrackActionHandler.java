import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

public class TrackActionHandler extends JPanel {

	private static final long serialVersionUID = 1L;
	private TrackEditor parent;
	
	public TrackActionHandler(TrackEditor parent) {
		this.parent = parent;
	}
	
	public class OpenAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		public OpenAction() {
			super("Open");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			parent.openFileInTrackEditor(".saved");
		}
	}
	
	public class OpenDirectoryAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		public OpenDirectoryAction() {
			super("Open Directory");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			parent.openDirectoryInTrackEditor();
		}
	}	
	
	public class PlayLoopWindowAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public PlayLoopWindowAction() {
			super("Play Loop Window");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			TrackEditor.playLoopDataInCurrentWindow(parent);
		}
	}
	
	public class PlayTrackWindowAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public PlayTrackWindowAction() {
			super("Play Track Window");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			TrackEditor.playTrackDataInCurrentWindow(parent);
		}
	}
	
	public class AddLoopAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public AddLoopAction() {
			super("Add Loop");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			TrackEditor.addLoop();
		}
	}
	
	public class AddBeatAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;
		private int beat;
		
		public AddBeatAction(int beat) {
			super("Add Beat " + beat);
			this.beat = beat;
		}
		
		public void actionPerformed(ActionEvent arg0) {
			TrackEditor.addBeat(beat);
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

	public JMenuBar createMenuBar() {
        //Create the menu bar.
        JMenuBar menuBar = new JMenuBar();
        //Create the File menu
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        fileMenu.add(new OpenAction());
        fileMenu.add(new OpenDirectoryAction());
        JMenu playMenu = new JMenu("Play");
        menuBar.add(playMenu);
        playMenu.add(new PlayLoopWindowAction());
        playMenu.add(new PlayTrackWindowAction());
        JMenu viewMenu = new JMenu("View");
        menuBar.add(viewMenu);
        JMenu addMenu = new JMenu("Add");
        addMenu.add(new AddLoopAction());
        for(int beat = 0; beat <= 3; beat++) {
        	addMenu.add(new AddBeatAction(beat));
        }    
        menuBar.add(addMenu);
        JMenu colorMenu = new JMenu("Color");
        menuBar.add(colorMenu);
        JMenu yViewMenu = new JMenu("YView");
        menuBar.add(yViewMenu);
        JMenu controlPointMenu = new JMenu("ControlPoint");
        menuBar.add(controlPointMenu);
        JMenu minLengthMenu = new JMenu("MinLength");
        menuBar.add(minLengthMenu);      
        JMenu playSpeedMenu = new JMenu("PlaySpeed");
        menuBar.add(playSpeedMenu);
        for(double playSpeed = 0.3; playSpeed <= 2.1; playSpeed += 0.1) {
        	playSpeedMenu.add(new SelectPlaySpeedAction(playSpeed));
        }
        return menuBar;
	}

}
