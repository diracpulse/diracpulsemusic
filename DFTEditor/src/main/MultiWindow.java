import java.awt.event.WindowAdapter;

public class MultiWindow extends WindowAdapter {

	public DFTEditor dftEditorFrame;
	public GraphEditor graphEditorFrame;
	public FDEditor fdEditorFrame;
	public ModuleEditor moduleEditorFrame;
	
	public MultiWindow() {
		dftEditorFrame = new DFTEditor();
		dftEditorFrame.setLocation(0, 0);
		//graphEditorFrame = new GraphEditor();
		//graphEditorFrame.setLocation(100, 100);
		//fdEditorFrame = new FDEditor();
		//fdEditorFrame.setLocation(200, 200);
		moduleEditorFrame = new ModuleEditor();
		moduleEditorFrame.setLocation(100, 100);
	}
}
