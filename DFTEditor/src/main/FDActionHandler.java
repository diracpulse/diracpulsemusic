package main;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
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
	private static ArrayList<FDActionHandler.Refreshable> menuItems;
	
	public FDActionHandler(FDEditor parent) {
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
			//parent.openFileInFDEditor();
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
			ArrayList<Harmonic> harmonics = new ArrayList<Harmonic>();
			for(long harmonicID: DFTEditor.selectedHarmonicIDs) {
				harmonics.add(DFTEditor.harmonicIDToHarmonic.get(harmonicID));
			}
			parent.saveHarmonicsToFile(harmonics);
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
			parent.playAllDataInCurrentWindow();
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
			parent.playSelectedDataInCurrentWindow();
		}
	}	
	
	public class SelectDataViewAction extends AbstractAction implements Refreshable {

		private static final long serialVersionUID = 1L;
		private FDView.DataView view;
		
		public SelectDataViewAction(FDView.DataView view) {
			super(view.toString());
			this.view = view;
			if(FDView.dataView == view) {
				putValue(NAME, new String(view + " " + '\u2713'));
			}
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println(view.toString());
			FDView.dataView = view;
			FDEditor.refreshView();
			refreshAll();
		}
		
		public void refresh() {
			if(FDView.dataView == view) {
				putValue(NAME, new String(view + " " + '\u2713'));
			} else {
				putValue(NAME, view.toString());
			}
		}
	}

	public class SelectColorViewAction extends AbstractAction implements Refreshable {

		private static final long serialVersionUID = 1L;
		private FDView.ColorView view;
		
		public SelectColorViewAction(FDView.ColorView view) {
			super(view.toString());
			this.view = view;
			if(FDView.colorView == view) {
				putValue(NAME, new String(view + " " + '\u2713'));
			}
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println(view.toString());
			FDView.colorView = view;
			FDEditor.refreshView();
			refreshAll();
		}
		
		public void refresh() {
			if(FDView.colorView == view) {
				putValue(NAME, new String(view + " " + '\u2713'));
			} else {
				putValue(NAME, view.toString());
			}
		}
	}
	
	public class SelectChannelAction extends AbstractAction implements Refreshable {

		private static final long serialVersionUID = 1L;
		private FDEditor.Channel channel;
		
		public SelectChannelAction(FDEditor.Channel channel) {
			super(channel.toString());
			this.channel = channel;
			if(FDEditor.currentChannel == channel) {
				putValue(NAME, new String(channel + " " + '\u2713'));
			}
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println(channel);
			FDEditor.currentChannel = channel;
			refreshAll();
			FDEditor.refreshView();
		}
		
		public void refresh() {
			if(FDEditor.currentChannel == channel) {
				putValue(NAME, new String(channel + " " + '\u2713'));
			} else {
				putValue(NAME, channel.toString());
			}
		}
	}



	public JMenuBar createMenuBar() {
		menuItems = new ArrayList<FDActionHandler.Refreshable>();
        //Create the menu bar.
        JMenuBar menuBar = new JMenuBar();
        //Create the File menu
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        fileMenu.add(new OpenAction());
        fileMenu.add(new SaveAction());
        fileMenu.add(new ExitAction());
        JMenu channelMenu = new JMenu("Channel");
        for(FDEditor.Channel channel: FDEditor.Channel.values()) {
        	addAction(channelMenu, new SelectChannelAction(channel));
        }
        menuBar.add(channelMenu);
        //Create the Play menu
        JMenu playMenu = new JMenu("Play");
        menuBar.add(playMenu);        
        playMenu.add(new JMenuItem(new PlayAllAction()));
        playMenu.add(new JMenuItem(new PlaySelectedAction()));
        JMenu dataViewMenu = new JMenu("DataView");
        menuBar.add(dataViewMenu);
        for(FDView.DataView view: FDView.DataView.values()) {
        	addAction(dataViewMenu, new SelectDataViewAction(view));
        }
        JMenu colorViewMenu = new JMenu("ColorView");
        menuBar.add(colorViewMenu);
        for(FDView.ColorView view: FDView.ColorView.values()) {
        	addAction(colorViewMenu, new SelectColorViewAction(view));
        }
        return menuBar;
	}
	
	private void addAction(JMenu menu, FDActionHandler.Refreshable action) {
    	menuItems.add(action);
    	menu.add((Action) menuItems.get(menuItems.size() - 1));
	}


}
