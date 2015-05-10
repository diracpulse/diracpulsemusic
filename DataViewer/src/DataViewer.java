
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

public class DataViewer extends JFrame {

	private static final long serialVersionUID = -7799500422605536597L;
	public static MultiWindow parent;
	public static DataViewerView view;
	public static DataViewerController controller;
	JToolBar navigationBar = null;
	ArduinoComm serial;
	public boolean paused = false;
	
	public void addNavigationButton(String buttonText) {
		JButton button = new JButton(buttonText);
		button.addActionListener((ActionListener) controller);
		navigationBar.add(button);
	}
	
	public JToolBar createNavigationBar() {
		addNavigationButton("Save");
		addNavigationButton("Play/Pause");
    	return navigationBar;
	}
	
	public DataViewer() {
        view = new DataViewerView(this);
        view.setBackground(Color.black);
        controller = new DataViewerController(this);
        navigationBar = new JToolBar();
        add(createNavigationBar(), BorderLayout.PAGE_START);
        view.addMouseListener(controller);
        view.addMouseMotionListener(controller);
        view.setPreferredSize(new Dimension(1500, 800));
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setSize(1500, 840);
        add(scrollPane, BorderLayout.CENTER);
        add(scrollPane);
        serial = new ArduinoComm(this);
        sendSerialPortData(new int[]{0, 0, 0});
        view.repaint();
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
	
	public void readSerialPortData(ArrayList<Integer> data) {
		if(paused) return;
		view.addData(0, data.get(0));
		view.repaint();
	}
	
	public void sendSerialPortData(int[] data) {
		serial.sendData(data);
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
			out.write(createLFOArray());
			out.newLine();
		} catch (Exception e) {
			System.out.println("DataViewer.loadData: Error saving to file");
		}
	}
	
	public String createMidiNoteToDP() {
		StringBuffer out = new StringBuffer();
		out.append("const int noteToDeltaPhase[] PROGMEM = {");
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
	
	public String createDeltaAmplitude() {
		StringBuffer out = new StringBuffer();
		out.append("const int deltaAmplitude[16][16] = {");
		for(double deltaVal = 0; deltaVal <= 16; deltaVal++) {
			double deltaDeltaVal = deltaVal / 16.0;
			out.append("{");
			double currentVal = 0;
			for(int index = 0; index < 16; index++) {
				if(currentVal + deltaDeltaVal >= 1.0) {
					currentVal = currentVal - 1.0;
					currentVal += deltaDeltaVal;
					out.append("1, ");
					continue;
				}
				out.append("0, ");
				currentVal += deltaDeltaVal;
			}
			out.append("\n");
		}
		out.append("}\n");
		return out.toString();
	}
	
	public String createDeltaPhaseArray() {
		StringBuffer out = new StringBuffer();
		out.append("const unsigned long deltaPhase[] PROGMEM = {");
		int newLine = 0;
		double freq = 8.17579890625;
		for(int noteIndex = 0; noteIndex < 97; noteIndex++) {
			for(int noteFraction = 0; noteFraction < 64; noteFraction++) {
				int value = (int) Math.round(freq / 1000.0 * Math.pow(2.0, 16.0));
				out.append(new Integer(value % 256).toString() + ", ");
				out.append(new Integer((value >> 8) % 256).toString() + ", ");
				out.append(new Integer(value >> 16).toString() + ", ");
				if(newLine == 8) {
					out.append("\n");
					newLine = 0;
				}
				newLine++;
				freq *= Math.pow(2.0, 1.0 / (12.0 * 64.0));
			}
		}
		out.append("}\n");
		return out.toString();
	}
	
	public String createLFOArray() {
		StringBuffer out = new StringBuffer();
		out.append("extern const uint_8 lfoDelta[] PROGMEM = {");
		int newLine = 0;
		for(int lfoIndex = 0; lfoIndex < 1024; lfoIndex++) {
			double exp = lfoIndex / 78.77;
			double freq = Math.pow(2.0, exp - 3.0);
			int value = (int) Math.round(freq / 1024.0 * 65535.0);
			out.append(new Integer(value % 256).toString() + ", ");
			out.append(new Integer((value >> 8) % 256).toString() + ", ");
			if(newLine == 8) {
				out.append("\n");
				newLine = 0;
			}
			newLine++;
		}
		out.append("}\n");
		return out.toString();
	}

	

	public String createADSRArray() {
		StringBuffer out = new StringBuffer();
		out.append("const unsigned long adsrDeltaPhase[] PROGMEM = {");
		int newLine = 0;
		for(int attackIndex = 0; attackIndex < 1024; attackIndex++) {
			double exp = attackIndex / 102.4;
			double attackValInMillis = Math.pow(2.0, exp);
			int value = (int) Math.round(1.0 / attackValInMillis * 65535.0);
			out.append(new Integer(value % 256).toString() + ", ");
			out.append(new Integer((value >> 8) % 256).toString() + ", ");
			if(newLine == 8) {
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
