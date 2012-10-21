import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class GraphActionHandler extends JPanel {

	private static final long serialVersionUID = 1L;
	private GraphEditor parent;
	private static ArrayList<GraphActionHandler.Refreshable> menuItems;
	
	public GraphActionHandler(GraphEditor parent) {
		this.parent = parent;
	}

	interface Refreshable {
		
		void refresh();
		
	}
	
	public static void refreshAll() {
		for(Refreshable menuItem: menuItems) {
			menuItem.refresh();
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
	
	public class PlaySelectedAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public PlaySelectedAction() {
			super("Play Sequencer");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			GraphEditor.playDataInSequencer(parent);
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

	public class SelectChannelAction extends AbstractAction implements Refreshable {

		private static final long serialVersionUID = 1L;
		private GraphEditor.Channel channel;
		
		public SelectChannelAction(GraphEditor.Channel channel) {
			super(channel.toString());
			this.channel = channel;
			if(GraphEditor.currentChannel == channel) {
				putValue(NAME, new String(channel + " " + '\u2713'));
			}
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println(channel);
			GraphEditor.currentChannel = channel;
			refreshAll();
			GraphEditor.refreshView();
		}
		
		public void refresh() {
			if(GraphEditor.currentChannel == channel) {
				putValue(NAME, new String(channel + " " + '\u2713'));
			} else {
				putValue(NAME, channel.toString());
			}
		}
	}
	
	public class SelectColorViewAction extends AbstractAction implements Refreshable {

		private static final long serialVersionUID = 1L;
		private GraphView.ColorView colorView;
		
		public SelectColorViewAction(GraphView.ColorView colorView) {
			super(colorView.toString());
			this.colorView = colorView;
			if(GraphView.colorView == colorView) {
				putValue(NAME, new String(colorView + " " + '\u2713'));
			}
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println(colorView);
			GraphView.colorView = colorView;
			refreshAll();
			GraphEditor.refreshView();
		}
		
		public void refresh() {
			if(GraphView.colorView == colorView) {
				putValue(NAME, new String(colorView + " " + '\u2713'));
			} else {
				putValue(NAME, colorView.toString());
			}
		}
	}


	public class SelectYViewAction extends AbstractAction implements Refreshable {

		private static final long serialVersionUID = 1L;
		private GraphView.YView yView;
		
		public SelectYViewAction(GraphView.YView yView) {
			super(yView.toString());
			this.yView = yView;
			if(GraphView.yView == yView) {
				putValue(NAME, new String(yView + " " + '\u2713'));
			}
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println(yView);
			GraphView.yView = yView;
			if(GraphView.xView == GraphView.XView.FREQUENCY && GraphView.yView == GraphView.YView.FREQUENCY) {
				GraphView.yView = GraphView.YView.AMPLITUDE;
			}
			refreshAll();
			GraphEditor.refreshView();
		}
		
		public void refresh() {
			if(GraphView.yView == yView) {
				putValue(NAME, new String(yView + " " + '\u2713'));
			} else {
				putValue(NAME, yView.toString());
			}
		}
	}
	
	public class SelectXViewAction extends AbstractAction implements Refreshable {

		private static final long serialVersionUID = 1L;
		private GraphView.XView xView;
		
		public SelectXViewAction(GraphView.XView xView) {
			super(xView.toString());
			this.xView = xView;
			if(GraphView.xView == xView) {
				putValue(NAME, new String(xView + " " + '\u2713'));
			}
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println(xView);
			GraphView.xView = xView;
			if(GraphView.xView == GraphView.XView.FREQUENCY && GraphView.yView == GraphView.YView.FREQUENCY) {
				GraphView.yView = GraphView.YView.AMPLITUDE;
			}
			refreshAll();
			GraphEditor.refreshView();
		}
		
		public void refresh() {
			if(GraphView.xView == xView) {
				putValue(NAME, new String(xView + " " + '\u2713'));
			} else {
				putValue(NAME, xView.toString());
			}
		}
	}


	public JMenuBar createMenuBar() {
		menuItems = new ArrayList<GraphActionHandler.Refreshable>();
        //Create the menu bar.
        JMenuBar menuBar = new JMenuBar();
        //Create the File menu
        JMenu playMenu = new JMenu("Play");
        menuBar.add(playMenu);
        playMenu.add(new PlayWindowAction());
        playMenu.add(new PlaySelectedAction());
        JMenu channelMenu = new JMenu("Channel");
        menuBar.add(channelMenu);
        for(GraphEditor.Channel channel: GraphEditor.Channel.values()) {
        	addAction(channelMenu, new SelectChannelAction(channel));
        }
        JMenu zoomMenu = new JMenu("Zoom");
        menuBar.add(zoomMenu);
        zoomMenu.add(new ZoomResetAction());
        JMenu colorViewMenu = new JMenu("ColorView");
        menuBar.add(colorViewMenu);
        for(GraphView.ColorView colorView: GraphView.ColorView.values()) {
        	addAction(colorViewMenu, new SelectColorViewAction(colorView));
        }
        JMenu xViewMenu = new JMenu("XView");
        menuBar.add(xViewMenu);
        for(GraphView.XView xView: GraphView.XView.values()) {
        	addAction(xViewMenu, new SelectXViewAction(xView));
        }
        JMenu yViewMenu = new JMenu("YView");
        menuBar.add(yViewMenu);
        for(GraphView.YView yView: GraphView.YView.values()) {
        	addAction(yViewMenu, new SelectYViewAction(yView));
        }
        JMenu playSpeedMenu = new JMenu("PlaySpeed");
        menuBar.add(playSpeedMenu);
        return menuBar;
	}

	private void addAction(JMenu menu, GraphActionHandler.Refreshable action) {
    	menuItems.add(action);
    	menu.add((Action) menuItems.get(menuItems.size() - 1));
	}
	
}
