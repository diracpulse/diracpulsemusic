package main;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class MultiWindow extends WindowAdapter {

	public DFTEditor dftEditorFrame;
	//public GraphEditor graphEditorFrame;
	//public FDEditor fdEditorFrame;
	public JFrame sequencerFrame;
	public ArrayList<ModuleEditorInfo> moduleEditorInfo;
	public Sequencer sequencer = null;
	
	public class ModuleEditorInfo {
		
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
		JTabbedPane pane = new JTabbedPane();
		moduleEditorInfo = new ArrayList<ModuleEditorInfo>();
		moduleEditorInfo.add(new ModuleEditorInfo("Tonal 1", new Color(255, 255, 255, 128), new ModuleEditor(this)));
		moduleEditorInfo.add(new ModuleEditorInfo("Tonal 2", new Color(255, 0, 0, 128), new ModuleEditor(this)));
		moduleEditorInfo.add(new ModuleEditorInfo("Tonal 3", new Color(0, 255, 0, 128), new ModuleEditor(this)));
		moduleEditorInfo.add(new ModuleEditorInfo("Tonal 4", new Color(0, 0, 255, 128), new ModuleEditor(this)));
		moduleEditorInfo.add(new ModuleEditorInfo("Percussion 1", Color.YELLOW, new ModuleEditor(this)));
		moduleEditorInfo.add(new ModuleEditorInfo("Percussion 2", Color.CYAN, new ModuleEditor(this)));
		moduleEditorInfo.add(new ModuleEditorInfo("Percussion 3", Color.MAGENTA, new ModuleEditor(this)));
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
	
	public void newWindow(JPanel pane) {
		JFrame envelopeFrame = new JFrame();
		envelopeFrame.setLocation(100, 100);
		envelopeFrame.add(pane);
		envelopeFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		envelopeFrame.pack();
		envelopeFrame.setVisible(true);
	}
	
}
