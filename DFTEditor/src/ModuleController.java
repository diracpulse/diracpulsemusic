import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


public class ModuleController implements MouseListener {

	public void mouseClicked(MouseEvent arg0) {}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}

	public void mousePressed(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		System.out.println("ModuleController: Mouse Clicked At: " + x + " " + y);
	}

}
