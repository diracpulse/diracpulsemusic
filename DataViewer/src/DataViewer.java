
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.*;
import java.util.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

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
	JToolBar navigationBar = null;
	
	public void addNavigationButton(String buttonText) {
		JButton button = new JButton(buttonText);
		button.addActionListener((ActionListener) controller);
		navigationBar.add(button);
	}
	
	public JToolBar createNavigationBar() {
		addNavigationButton("Save");
    	return navigationBar;
	}
	
	public DataViewer() {
        view = new DataViewerView();
        view.setBackground(Color.black);
        controller = new DataViewerController(this);
        navigationBar = new JToolBar();
        add(createNavigationBar(), BorderLayout.PAGE_START);
        view.addMouseListener(controller);
        view.addMouseMotionListener(controller);
        add(view);
        loadFileData();
        createNavigationBar();
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
	
	public void save() {
		String filename = FileUtils.PromptForFileSave(view);
		if(filename == null) return;
		saveToFile(filename);
	}
	
	public void open() {
		String filename = FileUtils.PromptForFileOpen(view);
		if(filename == null) return;
		loadFromFile(filename);
		view.repaint();
	}

	public void saveToFile(String filename) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
			saveData(out);
			out.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "There was a problem saving the file");
			return;
		}
		JOptionPane.showMessageDialog(this, "Finished Saving File");
	}
	
	public void loadFromFile(String filename) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			in.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.toString());
			return;
		}
		JOptionPane.showMessageDialog(this, "Finished Loading File");
	}
	
	
	public void loadData(BufferedReader in) {
		try {
			
		} catch (Exception e) {
			System.out.println("DataViewer.loadData: Error reading from file");
		}
	}

	public void saveData(BufferedWriter out) {
		try {
			out.write(createMidiNoteToDP());
			out.newLine();
		} catch (Exception e) {
			System.out.println("DataViewer.loadData: Error saving to file");
		}
	}
	
	public String createMidiNoteToDP() {
		StringBuffer out = new StringBuffer();
		out.append("const int noteToDeltaPhase[] = {");
		int newLine = 0;
		for(double freq = 63.05 / 2.0; freq < 2017.6; freq *= Math.pow(2.0, 1.0 / 12.0)) {
			int noteVal = (int) Math.round(Math.log(freq) / Math.log(2.0) * 53.0) - (int) Math.round(Math.log(15.7625) / Math.log(2.0) * 53.0);
			out.append(new Integer(noteVal).toString());
			out.append(", ");
			if(newLine == 11) {
				out.append("\n");
				newLine = 0;
			}
			newLine++;
		}
		out.append("}\n");
		return out.toString();
	}
	
	public String createDeltaPhaseArray() {
		StringBuffer out = new StringBuffer();
		out.append("const unsigned long deltaPhase[] = {");
		int newLine = 0;
		for(double freq = 15.7625; freq < 8070.4; freq *= Math.pow(2.0, 1.0 / 53.0)) {
			out.append(new Long((long) Math.round(freq / 32000.0 * 4294967295.0)).toString());
			out.append(", ");
			if(newLine == 13) {
				out.append("\n");
				newLine = 0;
			}
			newLine++;
		}
		out.append("}\n");
		return out.toString();
	}

	public String createCosArray() {
		StringBuffer cos = new StringBuffer();
		cos.append("const cos[] = {");
		double deltaPhase = 1.0 / 1024.0 * Math.PI * 2.0;
		double phase = 0.0;
		for(int y = 0; y < 64; y++) {
			for(int x = 0; x < 16; x++) {
				int cosVal = (int) Math.round((-1.0 * Math.cos(phase) + 1.0) / 2.0 * 31.0);
				cos.append(new Integer(cosVal).toString());
				cos.append(", ");
				phase += deltaPhase;
			}
			cos.append("\n");
		}
		cos.append("}");
		cos.append("\n");
		return cos.toString();
	}
	
}
