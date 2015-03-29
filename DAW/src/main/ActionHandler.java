package main;


import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;


public class ActionHandler extends JPanel {

	private static final long serialVersionUID = 672340751348574007L;
	private DFTEditor parent;
	private static ArrayList<ActionHandler.Refreshable> menuItems;
	
	public ActionHandler(DFTEditor parent) {
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
	
	public class DFTAction extends AbstractAction {

		private static final long serialVersionUID = -5323292053150793042L;

		public DFTAction() {
			super("DFT");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			parent.FileDFT(false);
			DFTEditor.refreshView();
		}
	}
	
	public class DFTOpenAction extends AbstractAction {

		private static final long serialVersionUID = -5323292053150793042L;

		public DFTOpenAction() {
			super("DFT Open");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			parent.FileDFT(true);
			DFTEditor.refreshView();
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

	public class SelectViewAction extends AbstractAction implements Refreshable {

		private static final long serialVersionUID = 1L;
		private DFTView.View view;
		
		public SelectViewAction(DFTView.View view) {
			super(view.toString());
			this.view = view;
			if(DFTView.getView() == view) {
				putValue(NAME, new String(view + " " + '\u2713'));
			}
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println(view.toString());
			DFTView.setView(view);
			DFTEditor.refreshView();
			refreshAll();
		}
		
		public void refresh() {
			if(DFTView.getView() == view) {
				putValue(NAME, new String(view + " " + '\u2713'));
			} else {
				putValue(NAME, view.toString());
			}
		}
	}


	public class SelectDataViewAction extends AbstractAction implements Refreshable {

		private static final long serialVersionUID = 1L;
		private DFTView.DataView dataView;
		
		public SelectDataViewAction(DFTView.DataView dataView) {
			super(dataView.toString());
			this.dataView = dataView;
			if(DFTView.getDataView() == dataView) {
				putValue(NAME, new String(dataView + " " + '\u2713'));
			}
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println(dataView.toString());
			DFTView.setDataView(dataView);
			DFTEditor.refreshView();
			refreshAll();
		}
		
		public void refresh() {
			if(DFTView.getDataView() == dataView) {
				putValue(NAME, new String(dataView + " " + '\u2713'));
			} else {
				putValue(NAME, dataView.toString());
			}
		}
	}


	public class SelectPlayAction extends AbstractAction implements Refreshable {

		private static final long serialVersionUID = 1L;
		private PlayDataInWindow.SynthType synthType;
		
		public SelectPlayAction(PlayDataInWindow.SynthType synthType) {
			super(synthType.toString());
			this.synthType = synthType;
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println(synthType.toString());
			PlayDataInWindow.synthType = synthType;
			parent.playDataInCurrentWindow();
		}
		
		public void refresh() {}
		
	}

	public class SelectChannelAction extends AbstractAction implements Refreshable {

		private static final long serialVersionUID = 1L;
		private DFTEditor.Channel channel;
		
		public SelectChannelAction(DFTEditor.Channel channel) {
			super(channel.toString());
			this.channel = channel;
			if(DFTEditor.currentChannel == channel) {
				putValue(NAME, new String(channel + " " + '\u2713'));
			}
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println(channel);
			DFTEditor.currentChannel = channel;
			refreshAll();
			DFTEditor.refreshView();
		}
		
		public void refresh() {
			if(DFTEditor.currentChannel == channel) {
				putValue(NAME, new String(channel + " " + '\u2713'));
			} else {
				putValue(NAME, channel.toString());
			}
		}
	}

	public class SelectMixerAction extends AbstractAction implements Refreshable {
		
		private static final long serialVersionUID = 1L;
		private DFTEditor.ChannelMixer channelMixer;
		
		public SelectMixerAction(DFTEditor.ChannelMixer channelMixer) {
			super(channelMixer.toString());
			this.channelMixer = channelMixer;
			if(DFTEditor.currentChannelMixer == channelMixer) {
				putValue(NAME, new String(channelMixer + " " + '\u2713'));
			}
		}
		
		public void actionPerformed(ActionEvent arg0) {
			System.out.println(channelMixer);
			DFTEditor.currentChannelMixer = channelMixer;
			refreshAll();
		}
		
		public void refresh() {
			if(DFTEditor.currentChannelMixer == channelMixer) {
				putValue(NAME, new String(channelMixer + " " + '\u2713'));
			} else {
				putValue(NAME, channelMixer.toString());
			}
		}
	}

	public JMenuBar createMenuBar() {
		menuItems = new ArrayList<ActionHandler.Refreshable>();
        //Create the menu bar.
        JMenuBar menuBar = new JMenuBar();
        //Create the File menu
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        fileMenu.add(new DFTAction());
        fileMenu.add(new DFTOpenAction());
        fileMenu.add(new ExitAction());
        // Create Channel Select
        JMenu channelMenu = new JMenu("Channel");
        for(DFTEditor.Channel channel: DFTEditor.Channel.values()) {
        	addAction(channelMenu, new SelectChannelAction(channel));
        }
        menuBar.add(channelMenu);
        JMenu mixerMenu = new JMenu("Mixer");
        for(DFTEditor.ChannelMixer channelMixer: DFTEditor.ChannelMixer.values()) {
        	addAction(mixerMenu, new SelectMixerAction(channelMixer));
        }
        menuBar.add(mixerMenu);
        //Create the Play menu
        JMenu playMenu = new JMenu("Play");
        for(PlayDataInWindow.SynthType synthType: PlayDataInWindow.SynthType.values()) {
        	playMenu.add(new SelectPlayAction(synthType));
        }
        menuBar.add(playMenu);
        JMenu viewMenu = new JMenu("View");
        menuBar.add(viewMenu);
        for(DFTView.View view: DFTView.View.values()) {
        	addAction(viewMenu, new SelectViewAction(view));
        }
        JMenu dataViewMenu = new JMenu("DataView");
        menuBar.add(dataViewMenu);
        for(DFTView.DataView dataView: DFTView.DataView.values()) {
        	addAction(dataViewMenu, new SelectDataViewAction(dataView));
        }
        return menuBar;
	}
	
	private void addAction(JMenu menu, ActionHandler.Refreshable action) {
    	menuItems.add(action);
    	menu.add((Action) menuItems.get(menuItems.size() - 1));
	}

}
