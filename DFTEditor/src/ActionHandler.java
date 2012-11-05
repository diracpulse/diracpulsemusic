import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
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
			DFT.printDFTParameters();
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
			DFT.printDFTParameters();
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

	public class SaveHarmonicsAction extends AbstractAction {

		private static final long serialVersionUID = -4814209505628569590L;

		public SaveHarmonicsAction() {
			super("Save Selected");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Save Selected");
			parent.saveHarmonicsToFile(new ArrayList<Harmonic>(DFTEditor.harmonicIDToHarmonic.values()));
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
	
	public class SelectCutoffAction extends AbstractAction implements Refreshable {
		
		private static final long serialVersionUID = 1L;
		private int logCutoff;
		
		public SelectCutoffAction(int logCutoff) {
			super("Log Cutoff = " + logCutoff);
			this.logCutoff = logCutoff;
			if(DFTEditor.minLogAmplitudeThreshold == logCutoff) {
				putValue(NAME, new String("Log Cutoff = " + logCutoff + " " + '\u2713'));
			}
		}
		
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Cutoff = " + logCutoff);
			DFTEditor.minLogAmplitudeThreshold = logCutoff;
			parent.cutoff();
			refreshAll();
		}	
		
		public void refresh() {
			if(DFTEditor.minLogAmplitudeThreshold == logCutoff) {
				putValue(NAME, new String("Log Cutoff = " + logCutoff + " " + '\u2713'));
			} else {
				putValue(NAME, new String("Log Cutoff = " + logCutoff));
			}
		}
		
	}
	
	public class SelectMinLengthAction extends AbstractAction implements Refreshable {
		
		private static final long serialVersionUID = 1L;
		private int minLength;
		
		public SelectMinLengthAction(int minLength) {
			super("Min Length = " + minLength);
			this.minLength = minLength;
			if(DFTEditor.minLengthThreshold == minLength) {
				putValue(NAME, new String("Min Length = " + minLength + " " + '\u2713'));
			}
		}
		
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("MinLength = " + minLength);
			DFTEditor.minLengthThreshold = minLength;
			DFTEditor.refreshView();
			refreshAll();
		}	
		
		public void refresh() {
			if(DFTEditor.minLengthThreshold == minLength) {
				putValue(NAME, new String("Min Length = " + minLength + " " + '\u2713'));
			} else {
				putValue(NAME, new String("Min Length = " + minLength));
			}
		}
		
	}
	
	public class SelectNoteBaseAction extends AbstractAction implements Refreshable {
		
		private static final long serialVersionUID = 1L;
		private int noteBase;
		
		public SelectNoteBaseAction(int noteBase) {
			super("Note Base = " + noteBase);
			this.noteBase = noteBase;
			if(FDData.noteBase == noteBase) {
				putValue(NAME, new String("Note Base = " + noteBase + " " + '\u2713'));
			}
		}
		
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Note Base = " + noteBase);
			FDData.noteBase = noteBase;
			DFT.printDFTParameters();
			refreshAll();
		}		
		
		public void refresh() {
			if(FDData.noteBase == noteBase) {
				putValue(NAME, new String("Note Base = " + noteBase + " " + '\u2713'));
			} else {
				putValue(NAME, new String("Note Base = " + noteBase + " "));
			}
		}
		
	}
	
	public class SelectTimeStepAction extends AbstractAction implements Refreshable {
		
		private static final long serialVersionUID = 1L;
		private int timeStep;
		
		public SelectTimeStepAction(int timeStep) {
			super("Time Step = " + timeStep + " ms");
			this.timeStep = timeStep;
			if(FDData.timeStepInMillis == timeStep) {
				putValue(NAME, new String("Time Step = " + timeStep + " ms \u2713"));
			}
		}
		
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Time Step = " + timeStep);
			FDData.timeStepInMillis = timeStep;
			DFT.printDFTParameters();
			refreshAll();
		}
		
		public void refresh() {
			if(FDData.timeStepInMillis == timeStep) {
				putValue(NAME, new String("Time Step = " + timeStep + " ms \u2713"));
			} else {
				putValue(NAME, new String("Time Step = " + timeStep + " ms"));
			}			
		}
		
	}
	
	public class SelectBinStepAction extends AbstractAction implements Refreshable {
		
		private static final long serialVersionUID = 1L;
		private double binStep;
		
		public SelectBinStepAction(double binStep) {
			super("Bin Step = " + binStep);
			this.binStep = binStep;
			if(DFT.maxBinStep == binStep) {
				putValue(NAME, new String("Bin Step = " + binStep + " " + '\u2713'));
			}
		}
		
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Bin Step = " + binStep);
			DFT.maxBinStep = binStep;
			DFT.printDFTParameters();
			refreshAll();
		}
		
		public void refresh() {
			if(DFT.maxBinStep == binStep) {
				putValue(NAME, new String("Bin Step = " + binStep + " " + '\u2713'));
			} else {
				putValue(NAME, new String("Bin Step = " + binStep));
			}
		}
		
	}
	
	public class SelectMidFreqAction extends AbstractAction implements Refreshable {
		
		private static final long serialVersionUID = 1L;
		private double midFreq;
		
		public SelectMidFreqAction(double midFreq) {
			super("Mid Freq = " + midFreq);
			this.midFreq = midFreq;
			if(DFT.midFreq == midFreq) {
				putValue(NAME, new String("Mid Freq = " + midFreq + " " + '\u2713'));
			}
			if(midFreq <= DFT.bassFreq) { 
				putValue(NAME, new String("Mid Freq (DISABLED) = " + midFreq));
				if(DFT.midFreq == midFreq) {
					putValue(NAME, new String("Mid Freq (DISABLED) = " + midFreq + " " + '\u2713'));
				}
			}
		}
		
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Mid Freq = " + midFreq);
			DFT.midFreq = midFreq;
			DFT.printDFTParameters();
			refreshAll();

		}
		
		public void refresh() {
			if(DFT.midFreq == midFreq) {
				putValue(NAME, new String("Mid Freq = " + midFreq + " " + '\u2713'));
			}
			if(midFreq <= DFT.bassFreq) { 
				putValue(NAME, new String("Mid Freq (DISABLED) = " + midFreq));
				if(DFT.midFreq == midFreq) {
					putValue(NAME, new String("Mid Freq (DISABLED) = " + midFreq + " " + '\u2713'));
				}
			}
		}
		
	}
	
	public class SelectBassFreqAction extends AbstractAction implements Refreshable {
		
		private static final long serialVersionUID = 1L;
		private double bassFreq;
		
		public SelectBassFreqAction(double bassFreq) {
			super("Bass Freq = " + bassFreq);
			this.bassFreq = bassFreq;
			if(DFT.bassFreq == bassFreq) {
				putValue(NAME, new String("Bass Freq = " + bassFreq + " " + '\u2713'));
			}
		}
		
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Bass Freq = " + bassFreq);
			DFT.bassFreq = bassFreq;
			DFT.printDFTParameters();
			if(DFT.bassFreq == bassFreq) {
				putValue(NAME, new String("Bass Freq = " + bassFreq + " " + '\u2713'));
			} else {
				putValue(NAME, new String("Bass Freq = " + bassFreq));
			}
		}

		public void refresh() {
			if(DFT.bassFreq == bassFreq) {
				putValue(NAME, new String("Bass Freq = " + bassFreq + " " + '\u2713'));
			} else {
				putValue(NAME, new String("Bass Freq = " + bassFreq));
			}	
		}
		
	}
	
	public class SelectBinRangeFactorAction extends AbstractAction implements Refreshable {
		
		private static final long serialVersionUID = 1L;
		private double binRangeFactor;
		
		public SelectBinRangeFactorAction(double binRangeFactor) {
			super("Bin Range Factor = " + binRangeFactor);
			this.binRangeFactor = binRangeFactor;
			if(SynthTools.binRangeFactor == binRangeFactor) {
				putValue(NAME, new String("Bin Range Factor = " + binRangeFactor + " " + '\u2713'));
			}
		}
		
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Bin Range Factor = " + binRangeFactor);
			SynthTools.binRangeFactor = binRangeFactor;
			SynthTools.createHarmonics();
			DFTEditor.refreshView();
			refreshAll();
		}
		
		public void refresh() {
			if(SynthTools.binRangeFactor == binRangeFactor) {
				putValue(NAME, new String("Bin Range Factor = " + binRangeFactor + " " + '\u2713'));
			} else {
				putValue(NAME, new String("Bin Range Factor = " + binRangeFactor));
			}
		}
	}
	
	public class ApplyMaskingFactorAction extends AbstractAction implements Refreshable {
		
		private static final long serialVersionUID = 1L;
		private double maskingFactor;
		
		public ApplyMaskingFactorAction(double maskingFactor) {
			super("Masking Factor = " + maskingFactor);
			this.maskingFactor = maskingFactor;
		}
		
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Masking Factor = " + maskingFactor);
			DFT.maskingFactor = maskingFactor;
			DFT.applyMasking();
			SynthTools.createHarmonics();
			DFTEditor.refreshView();
			FDEditor.refreshView();
			GraphEditor.refreshView();
			refreshAll();
		}
		
		public void refresh() {
			if(maskingFactor <= DFT.maskingFactor) {
				//putValue(NAME, new String("Masking Factor = " + maskingFactor));
				this.setEnabled(false);
			} else {
				//putValue(NAME, new String("Masking Factor = " + maskingFactor));
				this.setEnabled(true);
			}
		}
	}
	
	public class PrintDFTInfoAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;
		
		public PrintDFTInfoAction() {
			super("PrintDFTInfo");
		}
		
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("DFTInfo");
			DFT.printDFTParameters();
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
        fileMenu.add(new ExportAllAction());
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
        JMenu cutoffMenu = new JMenu("Cutoff");
        menuBar.add(cutoffMenu);
        for(int logCutoff = 12; logCutoff > 0; logCutoff--) {
        	addAction(cutoffMenu, new SelectCutoffAction(logCutoff));
        }
        JMenu minLengthMenu = new JMenu("MinLength");
        menuBar.add(minLengthMenu);      
        for(int minLength = 1; minLength <= 20; minLength++) {
        	addAction(minLengthMenu, new SelectMinLengthAction(minLength));
        }
        JMenu noteBaseMenu = new JMenu("NoteBase");
        menuBar.add(noteBaseMenu);
        for(int noteBase = 31; noteBase <= 31 * 8; noteBase += 31) {
        	addAction(noteBaseMenu, new SelectNoteBaseAction(noteBase));
        }
        JMenu timeStepMenu = new JMenu("TimeStep");
        menuBar.add(timeStepMenu);
        addAction(timeStepMenu, new SelectTimeStepAction(1));
        addAction(timeStepMenu, new SelectTimeStepAction(2));
        addAction(timeStepMenu, new SelectTimeStepAction(5));
        addAction(timeStepMenu, new SelectTimeStepAction(10));
        addAction(timeStepMenu, new SelectTimeStepAction(20));
        addAction(timeStepMenu, new SelectTimeStepAction(50));
        addAction(timeStepMenu, new SelectTimeStepAction(100));
        addAction(timeStepMenu, new SelectTimeStepAction(200));
        JMenu binStepMenu = new JMenu("BinStep");
        menuBar.add(binStepMenu);      
        for(double binStep = 0.5; binStep <= 5.0; binStep += 0.5) {
        	addAction(binStepMenu, new SelectBinStepAction(binStep));
        }
        JMenu midFreqMenu = new JMenu("MidFreq");
        menuBar.add(midFreqMenu);
        addAction(midFreqMenu, new SelectMidFreqAction(0.0));
        for(double midFreq = 250; midFreq <= 8000.0; midFreq *= Math.sqrt(2.0)) {
        	addAction(midFreqMenu, new SelectMidFreqAction(Math.round(midFreq)));
        }
        JMenu bassFreqMenu = new JMenu("BassFreq");
        menuBar.add(bassFreqMenu);          
        for(double bassFreq = 20; bassFreq < 2000.0; bassFreq *= Math.sqrt(2.0)) {
        	addAction(bassFreqMenu, new SelectBassFreqAction(Math.round(bassFreq)));
        }
        JMenu binRangeFactorMenu = new JMenu("BinRange");
        menuBar.add(binRangeFactorMenu);          
        for(double binRangeFactor = 1.0 / 64.0; binRangeFactor <= 1.0; binRangeFactor *= 2.0) {
        	addAction(binRangeFactorMenu, new SelectBinRangeFactorAction(binRangeFactor));
        }
        JMenu maskingFactorMenu = new JMenu("ApplyMasking");
        menuBar.add(maskingFactorMenu);          
        for(double maskingFactor = -16.0; maskingFactor <= 0.0; maskingFactor += 1.0) {
        	addAction(maskingFactorMenu, new ApplyMaskingFactorAction(maskingFactor));
        }
        JMenu dftParamsMenu = new JMenu("DFTParams");
        menuBar.add(dftParamsMenu);          
        dftParamsMenu.add(new PrintDFTInfoAction());
        return menuBar;
	}
	
	private void addAction(JMenu menu, ActionHandler.Refreshable action) {
    	menuItems.add(action);
    	menu.add((Action) menuItems.get(menuItems.size() - 1));
	}

}
