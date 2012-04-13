import java.awt.event.WindowAdapter;

public class MultiWindow extends WindowAdapter {

	public GraphEditor graphEditorFrame;
	//public HarmonicsEditor harmonicsEditorFrame;
	
	public MultiWindow() {
		graphEditorFrame = new GraphEditor();
		graphEditorFrame.setLocation(0, 0);
		//harmonicsEditorFrame = new HarmonicsEditor();
		//harmonicsEditorFrame.setLocation(100, 100);
	}
}
