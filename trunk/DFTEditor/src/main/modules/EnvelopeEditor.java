package main.modules;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

public class EnvelopeEditor extends JPanel {

	private static final long serialVersionUID = 3138005743637187863L;
	
	Envelope envelope;
	EnvelopeView view;
	EnvelopeController controller;
	
    public EnvelopeEditor(Envelope envelope) {
		super(new BorderLayout());
		this.envelope = envelope;
        view = new EnvelopeView(this);
        view.setBackground(Color.black);
        controller = new EnvelopeController(this);
        view.addMouseListener(controller);
        view.addMouseMotionListener(controller);
        view.setPreferredSize(new Dimension(800, 600));
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setSize(800, 600);
        add(scrollPane, BorderLayout.CENTER);
        envelope.getMultiWindow().newWindow(this);
    }
    
    public double getMillisPerPixel() {
    	return 1;
    }

}
