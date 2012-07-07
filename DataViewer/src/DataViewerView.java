import javax.swing.*;
import java.awt.*;

public class DataViewerView extends JComponent {
	
	private static final long serialVersionUID = 1795822702890025368L;
	private int minGridSpacing = 10;
	
	DataViewerView() {}
	
    protected void paintComponent(Graphics g) {
    	drawIntegerGrid(g);
		for(DataViewer.dataPoint2D data: DataViewer.dataPoints) {
			DataViewer.dataPoint2D normalData = DataViewer.getNormalizedData(data);
			g.setColor(getColor(normalData.getValue()));
			int screenX = (int) Math.round(normalData.getX() * getWidth());
			int screenY = (int) Math.round(normalData.getY() * getHeight());
			g.drawRect(screenX, screenY, 2, 2);
		}	
    }
		
	private Color getColor(double value) {
		float fValue = (float) value;
		if(fValue < 0.0f) fValue = 0.0f;
		if(fValue > 1.0f) fValue = 1.0f;
		float red = fValue;
		float green = 0.0f;
		float blue = 1.0f - fValue;
		if(red >= 0.5f) {
			green = (1.0f - red) * 2.0f;
		} else {
			green = red * 2.0f;
		}
		//return new Color(1.0f, 1.0f, 1.0f, 0.75f);
		return new Color(red, green, blue, 0.75f);
    }
	
	private void drawIntegerGrid(Graphics g) {
		g.setColor(new Color(0.75f, 0.75f, 0.75f));
		for(double x = Math.ceil(DataViewer.minX); x < Math.floor(DataViewer.maxX); x += 1.0) {
			DataViewer.dataPoint2D normalData = DataViewer.getNormalizedData(new DataViewer.dataPoint2D(x, 0.0, 0.0));
			int screenX = (int) Math.round(normalData.getX() * getWidth());
			g.drawLine(screenX, 0, screenX, getHeight());
			g.drawString(Math.round(x) + "", screenX, 10);
		}
		for(double y = Math.ceil(DataViewer.minY); y < Math.floor(DataViewer.maxY); y += 1.0) {
			DataViewer.dataPoint2D normalData = DataViewer.getNormalizedData(new DataViewer.dataPoint2D(0.0, y, 0.0));
			int screenY = (int) Math.round(normalData.getY() * getHeight());
			g.drawLine(0, screenY, getWidth(), screenY);
			g.drawString(Math.round(y) + "", 0, screenY);
		}
	}
	
}
