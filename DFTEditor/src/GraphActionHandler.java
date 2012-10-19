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

	public class MinClipThresholdAction extends AbstractAction implements Refreshable {
		
		private static final long serialVersionUID = 1L;
		private double minClipThreshold = 0.0;

		public MinClipThresholdAction(double minCT) {
			super("Min Clip Threshold = " + minCT);
			minClipThreshold = minCT;
			if(GraphEditor.minClipThreshold == minClipThreshold) {
				putValue(NAME, new String("Min Clip Threshold = " + minClipThreshold + " \u2713"));
			}
		}

		public void actionPerformed(ActionEvent arg0) {
			GraphEditor.minClipThreshold = minClipThreshold;
			refreshAll();
		}
		
		public void refresh() {
			if(GraphEditor.minClipThreshold == minClipThreshold) {
				putValue(NAME, new String("Min Clip Threshold = " + minClipThreshold + " \u2713"));
			} else {
				putValue(NAME, new String("Min Clip Threshold = " + minClipThreshold));
			}
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
		    GraphEditor.refreshView();
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
        JMenu colorMenu = new JMenu("Color");
        menuBar.add(colorMenu);
        colorMenu.add(new ColorViewAction());
        JMenu yViewMenu = new JMenu("YView");
        menuBar.add(yViewMenu);
        yViewMenu.add(new YViewAction());
        JMenu minClipThresholdMenu = new JMenu("MinClipThreshold");
        for(double minClipThreshold = 0.0; minClipThreshold <= 12.0; minClipThreshold += 1.0) {
        	addAction(minClipThresholdMenu, new MinClipThresholdAction(minClipThreshold));
        }
        menuBar.add(minClipThresholdMenu);
        JMenu minLengthMenu = new JMenu("MinLength");
        menuBar.add(minLengthMenu);      
        for(int minLength = 1; minLength <= 20; minLength++) {
        	minLengthMenu.add(new SelectMinLengthAction(minLength));
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
