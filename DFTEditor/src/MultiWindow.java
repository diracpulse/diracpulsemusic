import java.awt.event.WindowAdapter;

public class MultiWindow extends WindowAdapter {

	public DFTEditor dftEditorFrame;
	public GraphEditor graphEditorFrame;
	
	public MultiWindow() {
		dftEditorFrame = new DFTEditor();
		dftEditorFrame.setLocation(0, 0);
		graphEditorFrame = new GraphEditor();
		graphEditorFrame.setLocation(100, 100);
	}
}
