package main;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class MultiWindow extends WindowAdapter {

	public DFTEditor dftEditorFrame;
	//public GraphEditor graphEditorFrame;
	//public FDEditor fdEditorFrame;
	public JFrame sequencerFrame;
	public ArrayList<ModuleEditor> moduleEditors = null;
	public Sequencer sequencer = null;
	
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
		pane.add("Sequencer", (JComponent) new Sequencer(this));
		pane.add("Tonal 1 (Red)", (JComponent) new ModuleEditor(this));
		pane.add("Tonal 2 (Orange)", (JComponent) new ModuleEditor(this));
		pane.add("Tonal 3 (Yellow)", (JComponent) new ModuleEditor(this));
		pane.add("Tonal 4 (Green)", (JComponent) new ModuleEditor(this));		
		pane.add("Percussion 1 (Cyan)", (JComponent) new ModuleEditor(this));
		pane.add("Percussion 2 (Blue)", (JComponent) new ModuleEditor(this));		
		pane.add("Percussion 3 (Purple)", (JComponent) new ModuleEditor(this));
		pane.setBackgroundAt(0, Color.RED);
		pane.setBackgroundAt(1, Color.ORANGE);
		pane.setBackgroundAt(2, Color.YELLOW);
		pane.setBackgroundAt(3, Color.GREEN);
		pane.setBackgroundAt(4, Color.CYAN);
		pane.setBackgroundAt(5, Color.BLUE);
		pane.setBackgroundAt(6, new Color(1.0f, 0.0f, 1.0f));
		pane.setForeground(Color.BLACK);
		sequencerFrame.add(pane);
		sequencerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		sequencerFrame.pack();
		sequencerFrame.setVisible(true);
	}
}
