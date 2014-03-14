package main;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import main.modules.BasicWaveformEditor;
import main.modules.Envelope;
import main.modules.EnvelopeEditor;

public class MultiWindow extends WindowAdapter {

	public DFTEditor dftEditorFrame;
	//public GraphEditor graphEditorFrame;
	//public FDEditor fdEditorFrame;
	public JFrame sequencerFrame;
	public TreeMap<Integer, JFrame> frameIDToFrame = new TreeMap<Integer, JFrame>();
	public ArrayList<ModuleEditorInfo> moduleEditorInfo;
	public Sequencer sequencer = null;
	public JTabbedPane pane;
	
	public static class ModuleEditorInfo {
		
		String name;
		Color color;
		ModuleEditor moduleEditor;
		
		ModuleEditorInfo(String name, Color color, ModuleEditor moduleEditor) {
			this.name = name;
			this.color = color;
			this.moduleEditor = moduleEditor;
		}
		
		public String getName() {
			return name;
		}
		
		public Color getColor() {
			return color;
		}
		
		public ModuleEditor getModuleEditor() {
			return moduleEditor;
		}
		
	}
	
	public MultiWindow() {
		dftEditorFrame = new DFTEditor();
		dftEditorFrame.setLocation(0, 0);
		//graphEditorFrame = new GraphEditor();
		//graphEditorFrame.setLocation(100, 100);
		//fdEditorFrame = new FDEditor();
		//fdEditorFrame.setLocation(200, 200);
		sequencerFrame = new JFrame();
		sequencerFrame.setLocation(100, 100);
		pane = new JTabbedPane();
		moduleEditorInfo = new ArrayList<ModuleEditorInfo>();
		moduleEditorInfo.add(new ModuleEditorInfo("Tonal 1", new Color(255, 255, 255, 128), new ModuleEditor(this, moduleEditorInfo.size())));
		moduleEditorInfo.add(new ModuleEditorInfo("Tonal 2", new Color(255, 0, 0, 128), new ModuleEditor(this, moduleEditorInfo.size())));
		moduleEditorInfo.add(new ModuleEditorInfo("Tonal 3", new Color(0, 255, 0, 128), new ModuleEditor(this, moduleEditorInfo.size())));
		moduleEditorInfo.add(new ModuleEditorInfo("Tonal 4", new Color(0, 0, 255, 128), new ModuleEditor(this, moduleEditorInfo.size())));
		moduleEditorInfo.add(new ModuleEditorInfo("Percussion 1", Color.YELLOW, new ModuleEditor(this, moduleEditorInfo.size())));
		moduleEditorInfo.add(new ModuleEditorInfo("Percussion 2", Color.CYAN, new ModuleEditor(this, moduleEditorInfo.size())));
		moduleEditorInfo.add(new ModuleEditorInfo("Percussion 3", Color.MAGENTA, new ModuleEditor(this, moduleEditorInfo.size())));
		pane.add("Sequencer", (JComponent) new Sequencer(this));
		int index = 1;
		for(ModuleEditorInfo info: moduleEditorInfo) {
			pane.add(info.getName(), (JComponent) info.getModuleEditor());
			pane.setBackgroundAt(index, info.color);
			pane.setForeground(Color.BLACK);
			index++;
		}
		sequencerFrame.add(pane);
		sequencerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		sequencerFrame.pack();
		sequencerFrame.setVisible(true);
	}
	
	public void newProject(ArrayList<ModuleEditorInfo> moduleEditorInfoIn) {
		for(JFrame frame: frameIDToFrame.values()) {
			frame.dispose();
		}
		frameIDToFrame = new TreeMap<Integer, JFrame>();
		for(ModuleEditorInfo info: moduleEditorInfo) {
			pane.remove((JComponent) info.getModuleEditor());
		}
		moduleEditorInfo = moduleEditorInfoIn;
		int index = 1;
		for(ModuleEditorInfo info: moduleEditorInfo) {
			pane.add(info.getName(), (JComponent) info.getModuleEditor());
			pane.setBackgroundAt(index, info.color);
			pane.setForeground(Color.BLACK);
			index++;
		}
		//sequencerFrame.add(pane);
		//sequencerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//sequencerFrame.pack();
		//sequencerFrame.setVisible(true);
	}

	public int newFrame(JPanel pane, String title) {
		int frameID = 0;
		if(frameIDToFrame.size() != 0) frameID = frameIDToFrame.lastKey() + 1;
		JFrame frame = new JFrame();
		frame.add(pane);
		frame.setLocation(100, 100);
		frame.add(pane);
		frame.pack();
		frame.setVisible(true);
		frame.setTitle(title);
		frameIDToFrame.put(frameID, frame);
		return frameID;
	}
	
	public void addWindowListener(int frameID, WindowListener wl) {
		frameIDToFrame.get(frameID).addWindowListener(wl);
	}
	
	public void requestFocus(int frameID) {
		frameIDToFrame.get(frameID).requestFocus();
	}
	
	public void dispose(int frameID) {
		frameIDToFrame.get(frameID).dispose();
		frameIDToFrame.remove(frameID);
	}

}
