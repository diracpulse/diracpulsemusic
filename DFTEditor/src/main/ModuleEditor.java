package main;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Random;

import javax.swing.JFrame;


public class ModuleEditor extends JFrame {

	private static final long serialVersionUID = -6364925274198951658L;
	public static MultiWindow parent;
	public static ModuleView view;
	public static ModuleController controller;
	
	public ModuleEditor() {
	   	//FileConvert.wavImportAll();
        view = new ModuleView();
        view.setBackground(Color.black);
        controller = new ModuleController();
        view.addMouseListener(controller);
        add(view);
        setSize(1500, 800);
        this.setTitle("ModuleEditor: [no project selected]");
	}
	
}
