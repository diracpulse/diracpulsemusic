import java.awt.event.WindowAdapter;

public class MultiWindow extends WindowAdapter {

	public HarmonicsEditor fdEditorFrame;
	//public FDEditor fdEditorFrame2;
	
	public MultiWindow() {
		fdEditorFrame = new HarmonicsEditor();
		fdEditorFrame.setLocation(0, 0);
		//fdEditorFrame2 = new DFTEditor();
		//fdEditorFrame2.setLocation(100, 100);
	}
}
