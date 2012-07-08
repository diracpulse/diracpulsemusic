
import java.awt.BorderLayout;
import java.awt.Color;
import java.lang.*;
import java.util.*;

import javax.swing.JFrame;

public class DataViewer extends JFrame {
 
	public static class dataPoint2D {
		
		double x;
		double y;
		double value;
		
		public dataPoint2D(double x, double y, double value) {
			this.x = x;
			this.y = y;
			this.value = value;
		}
		
		public double getX() {return x;}
		public double getY() {return y;}
		public double getValue() {return value;}
		public String toString() {return x + "|" + y + "|" + value;};
		
	}
	
	private static final long serialVersionUID = -7799500422605536597L;
	public static MultiWindow parent;
	public static DataViewerView view;
	public static DataViewerController controller;
	public static ArrayList<dataPoint2D> dataPoints = new ArrayList<dataPoint2D>();
	public static double minX = Double.MAX_VALUE;
	public static double minY = Double.MAX_VALUE;
	public static double minValue = Double.MAX_VALUE;
	public static double maxX = Double.MIN_VALUE;
	public static double maxY = Double.MIN_VALUE;
	public static double maxValue = Double.MIN_VALUE;
	public static double xRange = 1.0;
	public static double yRange = 1.0;
	public static double valueRange = 1.0;
	
	public DataViewer() {
        view = new DataViewerView();
        view.setBackground(Color.black);
        controller = new DataViewerController();
        view.addMouseListener(controller);
        view.addMouseMotionListener(controller);
        add(view);
        loadFileData();
        view.repaint();
    }
	
	public static void loadFileData() {
		DataViewerFileInput.ReadSelectedFileData("randomChordsBinary");
		for(dataPoint2D data: dataPoints) {
			if(data.getX() < minX) minX = data.getX();
			if(data.getY() < minY) minY = data.getY();
			if(data.getValue() < minValue) minValue = data.getValue();
			if(data.getX() > maxX) maxX = data.getX();
			if(data.getY() > maxY) maxY = data.getY();
			if(data.getValue() > maxValue) maxValue = data.getValue();
			//System.out.println(data);
		}
		xRange = (DataViewer.maxX - DataViewer.minX);
		if(xRange == 0) xRange = 1.0;
		yRange = (DataViewer.maxY - DataViewer.minY);
		if(yRange == 0) yRange = 1.0;
		valueRange = (DataViewer.maxValue - DataViewer.minValue);
		if(valueRange == 0) valueRange = 1.0;
	}
	
	public static dataPoint2D getNormalizedData(dataPoint2D data) {
		double normalX = (data.getX() - DataViewer.minX) / xRange;
		double normalY = (data.getY() - DataViewer.minY) / yRange;
		double normalValue = (data.getValue() - DataViewer.minValue) / valueRange;
		return new dataPoint2D(normalX, normalY, normalValue);
	}
    
	private static void createAndShowGUI() {
		// Create and set up the window.
		parent = new MultiWindow();
		parent.dataViewerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		parent.dataViewerFrame.pack();
		parent.dataViewerFrame.setVisible(true);
		//parent.harmonicsEditorFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//parent.harmonicsEditorFrame.pack();
		//parent.harmonicsEditorFrame.setVisible(true);
	}

	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

}
