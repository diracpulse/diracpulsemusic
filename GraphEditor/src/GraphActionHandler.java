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
        JMenu viewMenu = new JMenu("View");
        menuBar.add(viewMenu);
        viewMenu.add(new ToggleClipZeroAction());
        JMenu zoomMenu = new JMenu("Zoom");
        menuBar.add(zoomMenu);
        zoomMenu.add(new ZoomResetAction());
        JMenu colorMenu = new JMenu("Color");
        menuBar.add(colorMenu);
        colorMenu.add(new ColorViewAction());
        JMenu yViewMenu = new JMenu("YView");
        menuBar.add(yViewMenu);
        yViewMenu.add(new YViewAction());
        JMenu minLengthMenu = new JMenu("MinLength");
        menuBar.add(minLengthMenu);      
        for(int minLength = 1; minLength <= 20; minLength++) {
        	minLengthMenu.add(new SelectMinLengthAction(minLength));
        }
        return menuBar;
	}

}
