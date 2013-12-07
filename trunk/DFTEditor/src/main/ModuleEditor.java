package main;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;

import javax.swing.JFrame;

import main.TestSignals.TAPair;
import main.TestSignals.TAPair.AmplitudeFormat;
import main.TestSignals.TAPair.TimeFormat;
import main.generators.Sawtooth;


public class ModuleEditor extends JFrame {

	private static final long serialVersionUID = -6364925274198951658L;
	public static MultiWindow parent;
	public static ModuleView view;
	public static ModuleController controller;
	private TreeMap<Integer, TreeMap<Integer, Module>> xToYToModule = null;
	private HashMap<Long, Module> moduleIDToModule = null;
	private Random randomGenerator = new Random();
	
	public class ModuleScreenInfo {
		Rectangle dimensions;
		Module module;
		public ModuleScreenInfo(Rectangle dimensions, Module module) {
			this.dimensions = dimensions;
			this.module = module;
		}
		public boolean pointIsInside(int x, int y) {
			return dimensions.contains(x, y);
		}
	}
	
	public ModuleEditor() {
	   	//FileConvert.wavImportAll();
        view = new ModuleView();
        view.setBackground(Color.black);
        controller = new ModuleController();
        view.addMouseListener(controller);
        add(view);
        setSize(1500, 800);
        this.setTitle("ModuleEditor: [no project selected]");
        moduleIDToModule = new HashMap<Long, Module>();
        initModules();
        view.updateModuleScreenInfo(getModulesScreenInfo());
        controller.updateModuleScreenInfo(getModulesScreenInfo());
	}
	
	public void initModules() {
		int currentY = 0;
		int currentX = 0;
		xToYToModule = new TreeMap<Integer, TreeMap<Integer, Module>>();
		xToYToModule.put(currentX, new TreeMap<Integer, Module>());
		xToYToModule.get(currentX).put(currentY, new Sawtooth(currentX, currentY, 256.0, new TAPair(TimeFormat.SECONDS, AmplitudeFormat.ABSOLUTE, 1.0, 1.0)));
		moduleIDToModule.put(randomGenerator.nextLong(), xToYToModule.get(currentX).get(currentY));
		currentY += xToYToModule.get(0).get(0).getHeight();
		xToYToModule.get(currentX).put(currentY, new Sawtooth(currentX, currentY, 128.0, new TAPair(TimeFormat.SECONDS, AmplitudeFormat.ABSOLUTE, 2.0, 0.5)));
		moduleIDToModule.put(randomGenerator.nextLong(), xToYToModule.get(currentX).get(currentY));
		currentX = xToYToModule.get(0).get(0).getWidth();
		currentY = 0;
		xToYToModule.put(currentX, new TreeMap<Integer, Module>());
		xToYToModule.get(currentX).put(currentY, new Sawtooth(currentX, currentY, 64.0, new TAPair(TimeFormat.SECONDS, AmplitudeFormat.ABSOLUTE, 1.5, 0.75)));
		moduleIDToModule.put(randomGenerator.nextLong(), xToYToModule.get(currentX).get(currentY));
		currentY += xToYToModule.get(0).get(0).getHeight();
		xToYToModule.get(currentX).put(currentY, new Sawtooth(currentX, currentY, 512.0, new TAPair(TimeFormat.SECONDS, AmplitudeFormat.ABSOLUTE, 2.5, 0.25)));
		moduleIDToModule.put(randomGenerator.nextLong(), xToYToModule.get(currentX).get(currentY));
	}
	
	public ArrayList<ModuleScreenInfo> getModulesScreenInfo() {
		ArrayList<ModuleScreenInfo> returnVal = new ArrayList<ModuleScreenInfo>();
		for(Integer x: xToYToModule.keySet()) {
			for(Integer y: xToYToModule.get(x).keySet()) {
				Module currentModule = xToYToModule.get(x).get(y);
				returnVal.add(new ModuleScreenInfo(new Rectangle(x, y, currentModule.getWidth(), currentModule.getHeight()),xToYToModule.get(x).get(y)));
			}
		}
		return returnVal;
	}
	
}
