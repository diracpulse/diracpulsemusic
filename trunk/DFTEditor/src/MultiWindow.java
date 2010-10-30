import java.awt.event.WindowAdapter;

public class MultiWindow extends WindowAdapter {

	public DFTEditor dftEditorFrame;
	public HarmonicsEditor harmonicsEditorFrame;
	
	public MultiWindow() {
		dftEditorFrame = new DFTEditor();
		dftEditorFrame.setLocation(0, 0);
		harmonicsEditorFrame = new HarmonicsEditor();
		harmonicsEditorFrame.setLocation(100, 100);
	}
}
