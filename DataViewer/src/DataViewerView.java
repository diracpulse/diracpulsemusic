import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.TreeMap;

public class DataViewerView extends JComponent {
	
	private static final long serialVersionUID = 1795822702890025368L;
	private int minGridSpacing = 10;
	TreeMap<Integer, ArrayList<Integer>> dataValues;
	
	DataViewerView() {
		dataValues = new TreeMap<Integer, ArrayList<Integer>>();
	}
	
    protected void paintComponent(Graphics g) {
    	g.setColor(new Color(0.0f, 0.0f, 0.0f));
    	g.fillRect(0, 0, getWidth(), getHeight());
    	int dataIndex = 0;
    	for(ArrayList<Integer> data: dataValues.values()) {
    		int x = 0;
    		int y = getHeight() / 2;
    		g.setColor(getColor(dataIndex));
    		for(int point: data) {
    			g.drawRect(x, y - point, 1, 1);
    			x++;
    		}
    		dataIndex++;
    	}
    }
    
    public Color getColor(int dataIndex) {
    	if(dataIndex == 0) return Color.red;
    	if(dataIndex == 1) return Color.green;
    	if(dataIndex == 2) return Color.blue;
    	return Color.white;
    }

    public void addData(int dataIndex, int value) {
    	if(dataValues.containsKey(dataIndex)) {
    		dataValues.get(dataIndex).add(value);
    	} else {
    		dataValues.put(dataIndex, new ArrayList<Integer>());
    		dataValues.get(dataIndex).add(value);
    	}
    }
    
}
