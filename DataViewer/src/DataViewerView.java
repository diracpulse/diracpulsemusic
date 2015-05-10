import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class DataViewerView extends JComponent {
	
	private static final long serialVersionUID = 1795822702890025368L;
	private int minGridSpacing = 10;
	DataViewer parent;
	public volatile boolean noAdd = false;
	public HashMap<Integer, ArrayList<Integer>> dataValues;
	boolean cleared = false;
	
	DataViewerView(DataViewer parent) {
		this.parent = parent;
		synchronized(this) {
			dataValues = new HashMap<Integer, ArrayList<Integer>>();
		}
	}
	
    protected void paintComponent(Graphics g) {
    	super.paintComponent(g);
    	int y = getHeight() / 2;
    	g.setColor(new Color(0.0f, 0.0f, 0.0f));
    	g.fillRect(0, 0, getWidth(), getHeight());
    	g.setColor(Color.WHITE);
    	g.drawLine(0, y, getWidth(), y);
    	g.drawLine(0, y - 127, getWidth(), y - 127);
    	g.drawLine(0, y - 255, getWidth(), y - 255);
    	int dataIndex = 0;
    	synchronized(this) {
    	for(ArrayList<Integer> data: dataValues.values()) {
    		int x = 0;
    		g.setColor(getColor(dataIndex));
    		for(int point: data) {
    			g.drawRect(x, y - point, 0, 0);
    			x++;
    		}
    		dataIndex++;
    	}
    	}
    }
    
    public Color getColor(int dataIndex) {
    	if(dataIndex == 0) return Color.red;
    	if(dataIndex == 1) return Color.green;
    	if(dataIndex == 2) return Color.blue;
    	return Color.white;
    }
    
    public synchronized void addData(int dataIndex, int value) {
		if(dataValues.containsKey(dataIndex)) {
			dataValues.get(dataIndex).add(value);
		} else {
			dataValues.put(dataIndex, new ArrayList<Integer>());
			dataValues.get(dataIndex).add(value);
		}
		if(dataValues.get(0).size() > 1500) {
			dataValues.clear();
		}
    }
	
}
