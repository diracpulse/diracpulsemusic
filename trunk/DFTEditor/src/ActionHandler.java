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
			parent.FileDFT(false);
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
			DFTEditor.refreshView();
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
			DFTEditor.refreshView();
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
			DFTEditor.refreshView();
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
			DFTEditor.refreshView();
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
	
	public class SelectNoteBaseAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;
		private int noteBase;
		
		public SelectNoteBaseAction(int noteBase) {
			super("Note Base = " + noteBase);
			this.noteBase = noteBase;
		}
		
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Note Base = " + noteBase);
			FDData.noteBase = noteBase;
		}		
		
	}
	
	public class SelectTimeStepAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;
		private int timeStep;
		
		public SelectTimeStepAction(int timeStep) {
			super("Time Step = " + timeStep + " ms");
			this.timeStep = timeStep;
		}
		
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Time Step = " + timeStep);
			FDData.timeStepInMillis = timeStep;
		}		
		
	}
	
	public class SelectBinStepAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;
		private double binStep;
		
		public SelectBinStepAction(double binStep) {
			super("Bin Step = " + binStep);
			this.binStep = binStep;
		}
		
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Bin Step = " + binStep);
			DFT.maxBinStep = binStep;
			DFT.InitWavelets();
		}
		
	}
	
	public class SelectCenterFreqAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;
		private double centerFreq;
		
		public SelectCenterFreqAction(double centerFreq) {
			super("Center Freq = " + centerFreq);
			this.centerFreq = centerFreq;
		}
		
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Center Freq = " + centerFreq);
			DFT.centerFreq = centerFreq;
			DFT.InitWavelets();
		}
		
	}
	
	public JMenuBar createMenuBar() {
        //Create the menu bar.
        JMenuBar menuBar = new JMenuBar();
        //Create the File menu
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        fileMenu.add(new DFTAction());
        fileMenu.add(new DFTOpenAction());
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
        JMenu noteBaseMenu = new JMenu("NoteBase");
        menuBar.add(noteBaseMenu);      
        noteBaseMenu.add(new SelectNoteBaseAction(7));
        noteBaseMenu.add(new SelectNoteBaseAction(12));
        noteBaseMenu.add(new SelectNoteBaseAction(19));
        noteBaseMenu.add(new SelectNoteBaseAction(31));
        noteBaseMenu.add(new SelectNoteBaseAction(43));
        noteBaseMenu.add(new SelectNoteBaseAction(62));
        noteBaseMenu.add(new SelectNoteBaseAction(72));
        noteBaseMenu.add(new SelectNoteBaseAction(93));
        noteBaseMenu.add(new SelectNoteBaseAction(124));
        noteBaseMenu.add(new SelectNoteBaseAction(144));
        JMenu timeStepMenu = new JMenu("TimeStep");
        menuBar.add(timeStepMenu);      
        timeStepMenu.add(new SelectTimeStepAction(1));
        timeStepMenu.add(new SelectTimeStepAction(2));
        timeStepMenu.add(new SelectTimeStepAction(5));
        timeStepMenu.add(new SelectTimeStepAction(10));
        timeStepMenu.add(new SelectTimeStepAction(20));
        timeStepMenu.add(new SelectTimeStepAction(40));
        timeStepMenu.add(new SelectTimeStepAction(80));
        timeStepMenu.add(new SelectTimeStepAction(160));
        timeStepMenu.add(new SelectTimeStepAction(320));
        timeStepMenu.add(new SelectTimeStepAction(500));
        JMenu binStepMenu = new JMenu("BinStep");
        menuBar.add(binStepMenu);      
        for(double binStep = 0.5; binStep <= 5.0; binStep += 0.5) {
        	binStepMenu.add(new SelectBinStepAction(binStep));
        }
        JMenu centerFreqMenu = new JMenu("CenterFreq");
        menuBar.add(centerFreqMenu);          
        for(double centerFreq = 20; centerFreq < 100.0; centerFreq += 20) {
        	centerFreqMenu.add(new SelectCenterFreqAction(centerFreq));
        }
        for(double centerFreq = 100; centerFreq < 250.0; centerFreq += 50) {
        	centerFreqMenu.add(new SelectCenterFreqAction(centerFreq));
        }
        for(double centerFreq = 250; centerFreq < 2000.0; centerFreq += 250) {
        	centerFreqMenu.add(new SelectCenterFreqAction(centerFreq));
        }       
        return menuBar;
	}

}
