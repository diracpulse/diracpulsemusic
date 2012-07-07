import java.awt.event.WindowAdapter;

public class MultiWindow extends WindowAdapter {

	public DataViewer dataViewerFrame;
	//public HarmonicsEditor harmonicsEditorFrame;
	
	public MultiWindow() {
		dataViewerFrame = new DataViewer();
		dataViewerFrame.setLocation(0, 0);
		//harmonicsEditorFrame = new HarmonicsEditor();
		//harmonicsEditorFrame.setLocation(100, 100);
	}
}
