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
	
	public class DFTAction extends AbstractAction {

		private static final long serialVersionUID = -5323292053150793042L;

		public DFTAction() {
			super("DFT");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			parent.FileDFT();
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

	public class SaveHarmonicsAction extends AbstractAction {

		private static final long serialVersionUID = -4814209505628569590L;

		public SaveHarmonicsAction() {
			super("Save Selected");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Save Selected");
			parent.saveHarmonicsToFile();
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
	
	public class SelectViewAction extends AbstractAction {

		private static final long serialVersionUID = 1L;
		private DFTView.View view;
		
		public SelectViewAction(DFTView.View view) {
			super(view.toString());
			this.view = view;
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println(view.toString());
			DFTView.setView(view);
			DFTEditor.view.repaint();
		}
	}


	public class SelectDataViewAction extends AbstractAction {

		private static final long serialVersionUID = 1L;
		private DFTView.DataView dataView;
		
		public SelectDataViewAction(DFTView.DataView dataView) {
			super(dataView.toString());
			this.dataView = dataView;
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println(dataView.toString());
			DFTView.setDataView(dataView);
			DFTEditor.view.repaint();
		}
	}


	public class SelectPlayAction extends AbstractAction {

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
	}

	public class SelectChannelAction extends AbstractAction {

		private static final long serialVersionUID = 1L;
		private DFTEditor.Channel channel;
		
		public SelectChannelAction(DFTEditor.Channel channel) {
			super(channel.toString());
			this.channel = channel;
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println(channel);
			DFTEditor.currentChannel = channel;
		}
	}

	public class SelectMixerAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;
		private DFTEditor.ChannelMixer channelMixer;
		
		public SelectMixerAction(DFTEditor.ChannelMixer channelMixer) {
			super(channelMixer.toString());
			this.channelMixer = channelMixer;
		}
		
		public void actionPerformed(ActionEvent arg0) {
			System.out.println(channelMixer);
			DFTEditor.currentChannelMixer = channelMixer;
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
			SynthTools.createHarmonics();
		    DFTEditor.view.repaint();
		    SynthTools.refresh = true;
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
			DFTEditor.minLengthThreshold = minLength;
			SynthTools.refresh = true;
		}		
		
	}
	
	public JMenuBar createMenuBar() {
        //Create the menu bar.
        JMenuBar menuBar = new JMenuBar();
        //Create the File menu
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        fileMenu.add(new DFTAction());
        fileMenu.add(new ExportAllAction());
        fileMenu.add(new ExitAction());
        // Create Channel Select
        JMenu channelMenu = new JMenu("Channel");
        for(DFTEditor.Channel channel: DFTEditor.Channel.values()) {
        	channelMenu.add(new SelectChannelAction(channel));
        }
        menuBar.add(channelMenu);
        JMenu mixerMenu = new JMenu("Mixer");
        for(DFTEditor.ChannelMixer channelMixer: DFTEditor.ChannelMixer.values()) {
        	mixerMenu.add(new SelectMixerAction(channelMixer));
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
        	viewMenu.add(new SelectViewAction(view));
        }
        JMenu dataViewMenu = new JMenu("DataView");
        menuBar.add(dataViewMenu);
        for(DFTView.DataView dataView: DFTView.DataView.values()) {
        	dataViewMenu.add(new SelectDataViewAction(dataView));
        }        
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
