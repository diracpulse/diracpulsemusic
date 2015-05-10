
import java.awt.event.*;

public class DataViewerController implements MouseListener, MouseMotionListener, ActionListener {

	DataViewer parent;
	
	DataViewerController(DataViewer parent) {
		this.parent = parent;
	}
	
	public void actionPerformed(ActionEvent e) {
		if ("Open".equals(e.getActionCommand())) parent.open();
		if ("Save".equals(e.getActionCommand())) parent.save();
		if ("Play/Pause".equals(e.getActionCommand())) parent.paused = !parent.paused;
	}

	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){}
	public void mouseClicked(MouseEvent e){
	}
	
	public void mouseMoved(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
	}
	
}
