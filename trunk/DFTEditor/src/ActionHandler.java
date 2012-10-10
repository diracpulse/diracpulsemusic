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

	public class StereoAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public StereoAction() {
			super("Stereo");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			DFTEditor.currentChannel = DFTEditor.Channel.STEREO;
			DFTEditor.view.repaint();
			System.out.println("Stereo");
		}
	}
	
	public class MonoAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public MonoAction() {
			super("Mono");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			DFTEditor.currentChannel = DFTEditor.Channel.MONO;
			DFTEditor.view.repaint();
			System.out.println("Mono");
		}
	}
	
	public class LeftAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public LeftAction() {
			super("Left");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			DFTEditor.currentChannel = DFTEditor.Channel.LEFT;
			DFTEditor.view.repaint();
			System.out.println("Left");
		}
	}
	
	public class RightAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public RightAction() {
			super("Right");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			DFTEditor.currentChannel = DFTEditor.Channel.RIGHT;
			DFTEditor.view.repaint();
			System.out.println("Right");
		}
	}
	
	public class PlayLinearAction extends AbstractAction {

		private static final long serialVersionUID = 7354387706903212877L;

		public PlayLinearAction() {
			super("Play Linear");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			parent.playSelectedDataInCurrentWindowLinear();
			System.out.println("Play Linear");
		}
	}
	
	public class PlayLinearCubicSplineAction extends AbstractAction {

		private static final long serialVersionUID = 7354387706903212877L;

		public PlayLinearCubicSplineAction() {
			super("Play Linear Cubic Spline");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			parent.playSelectedDataInCurrentWindowLinearCubicSpline();
			System.out.println("Play Linear Cubic Spline");
		}
	}
	
	public class PlayLinearNoiseAction extends AbstractAction {

		private static final long serialVersionUID = 7354387706903212877L;

		public PlayLinearNoiseAction() {
			super("Play Linear Noise");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			parent.playSelectedDataInCurrentWindowLinearNoise();
			System.out.println("Play Linear Noise");
		}
	}
	
	public class ViewDigits1Action extends AbstractAction {

		private static final long serialVersionUID = 5910687309575402310L;

		public ViewDigits1Action() {
			super("Digits = 1");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("View Digits1");
			DFTView.setView(DFTView.View.Digits1);
			DFTEditor.view.repaint();
		}
	}
	
	public class ViewDigits2Action extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public ViewDigits2Action() {
			super("Digits = 2");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("View Digits2");
			DFTView.setView(DFTView.View.Digits2);
			DFTEditor.view.repaint();
		}
	}
	
	public class ViewPixels1Action extends AbstractAction {

		private static final long serialVersionUID = -1592520256131827974L;

		public ViewPixels1Action() {
			super("Pixels = 1");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("View Pixels1");
			DFTView.setView(DFTView.View.Pixels1);
			DFTEditor.view.repaint();
		}
	}
	
	public class ViewPixels2Action extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public ViewPixels2Action() {
			super("Pixels = 2");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("View Pixels2");
			DFTView.setView(DFTView.View.Pixels2);
			DFTEditor.view.repaint();
		}
	}
	
	public class ViewPixels3Action extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public ViewPixels3Action() {
			super("Pixels = 3");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("View Pixels3");
			DFTView.setView(DFTView.View.Pixels3);
			DFTEditor.view.repaint();
		}
	}
	
	public class DerivativesViewAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public DerivativesViewAction() {
			super("Derivatives");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Derivatives");
			DFTView.setDataView(DFTView.DataView.DERIVATIVES);
			DFTEditor.view.repaint();
		}
	}
	
	public class DataViewAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public DataViewAction() {
			super("Data");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Data");
			DFTView.setDataView(DFTView.DataView.DATA);
			DFTEditor.view.repaint();
		}
	}
	
	public class DataOnlyViewAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public DataOnlyViewAction() {
			super("Data Only");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Data Only");
			DFTView.setDataView(DFTView.DataView.DATA_ONLY);
			DFTEditor.view.repaint();
		}
	}
	
	public class HarmonicsViewAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public HarmonicsViewAction() {
			super("Harmonics");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Harmonics");
			DFTView.setDataView(DFTView.DataView.HARMONICS);
			DFTEditor.view.repaint();
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
		    parent.playSelectedDataInCurrentWindowLinear();
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
		    parent.playSelectedDataInCurrentWindowLinear();
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
        menuBar.add(channelMenu);
        channelMenu.add(new StereoAction());
        channelMenu.add(new MonoAction());
        channelMenu.add(new LeftAction());
        channelMenu.add(new RightAction());
        JMenu mixerMenu = new JMenu("Mixer");
        for(DFTEditor.ChannelMixer channelMixer: DFTEditor.ChannelMixer.values()) {
        	mixerMenu.add(new SelectMixerAction(channelMixer));
        }
        menuBar.add(mixerMenu);
        //Create the Play menu
        JMenu playMenu = new JMenu("Play");
        menuBar.add(playMenu);        
        playMenu.add(new JMenuItem(new PlayLinearAction()));
        playMenu.add(new JMenuItem(new PlayLinearCubicSplineAction()));
        playMenu.add(new JMenuItem(new PlayLinearNoiseAction()));
        JMenu viewMenu = new JMenu("View");
        menuBar.add(viewMenu);
        viewMenu.add(new JMenuItem(new ViewPixels1Action()));
        viewMenu.add(new JMenuItem(new ViewPixels2Action()));
        viewMenu.add(new JMenuItem(new ViewPixels3Action()));    
        viewMenu.add(new JMenuItem(new ViewDigits1Action()));
        viewMenu.add(new JMenuItem(new ViewDigits2Action())); 
        JMenu dataViewMenu = new JMenu("DataView");
        menuBar.add(dataViewMenu);
        dataViewMenu.add(new JMenuItem(new DataViewAction()));
        dataViewMenu.add(new JMenuItem(new DataOnlyViewAction()));
        dataViewMenu.add(new JMenuItem(new DerivativesViewAction()));
        dataViewMenu.add(new JMenuItem(new HarmonicsViewAction()));
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
