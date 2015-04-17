
package main.playable;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class ControlBank implements PlayableModule {

	public enum Name {

		OSC1Shape ("SHP", "SAW", "SQR"),
		OSC1PWM ("PWM", "", ""),
		SUBOSCLevel ("SUB", "", ""),
		NOISE_COLOR ("NOISE", "WHITE", "PINK"),
		NOISE_LEVEL ("LEVEL", "", ""),
		OSC2Shape ("SHP", "SAW", "SQR"),
		OSC2PWM ("PWM", "", ""),
		OSC2FMAmt ("FML", "", ""),
		OSC2PWMAmt ("PWL", "", ""),
		OSC2AMPAmt ("AML", "", ""),
		OSC2FREQ ("FREQ", "", ""),
		OSC2DETUNE ("FINE", "", ""),
		OSC2RING ("RING", "", ""),
		OSC2LEVEL ("LEVEL", "", "");
		
		private final String displayName;
		private final String upperLable;
		private final String lowerLable;
		
		Name(String displayName, String upperLable, String lowerLable) {
			this.displayName = displayName;
			this.upperLable = upperLable;
			this.lowerLable = lowerLable;
		}
		
		public String getDisplayName() {
			return displayName;
		}
		
		public String getUpperLable() {
			return upperLable;
		}
		
		public String getLowerLable() {
			return lowerLable;
		}
		
	}
	
	public class Spec {
		
		public final Name name;
		public final double maxVal;
		public final double minVal;
		public final double initialVal;
		public final Slider.Type taper;
		
		public Spec(Name name, Slider.Type taper, double minVal, double maxVal, double initialVal) {
			this.name = name;
			this.taper = taper;
			this.minVal = minVal;
			this.maxVal = maxVal;
			this.initialVal = initialVal;
		}
		
	}
	
	private TreeMap<Name, Slider> nameToSlider;
	private PlayableEditor parent;
	private int maxScreenX;
	String moduleName;
	private int screenX;
	private int screenY;
	private int maxScreenY;
	private int yPadding = PlayableEditor.moduleYPadding;
	
	public ControlBank(PlayableEditor parent, String moduleName, int screenX, int screenY) {
		this.parent = parent;
		this.screenX = screenX;
		this.maxScreenX = screenX;
		this.screenY = screenY;
		this.moduleName = moduleName;
		nameToSlider = new TreeMap<Name, Slider>();
	}
	
	public void add(Spec control) {
		int x = maxScreenX;
		int y = screenY + PlayableEditor.moduleYPadding;
		nameToSlider.put(control.name , new Slider(control.taper, x, y, control.minVal, control.maxVal, control.initialVal, 
						 new String[] {control.name.getDisplayName(), 
						 control.name.getUpperLable(), 
						 control.name.getLowerLable(), }));
		x = nameToSlider.get(control.name).getMaxX();
		maxScreenY = nameToSlider.get(control.name).getMaxY();
		maxScreenX = x;
	}

	public int getMaxScreenX() {
		return maxScreenX;
	}
	
	public synchronized double getValue(Name name) {
		return nameToSlider.get(name).getCurrentValue();
	}
	
	public void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setFont(new Font("TRUETYPE_FONT", Font.BOLD, 10));
		Font font = g2.getFont();
		FontMetrics metrics = g2.getFontMetrics(font);
		int hgt = metrics.getHeight();
		int yPaddingVal = 0;
		int textYOffset = (yPadding - hgt) / 2 + yPadding / 2;
		g2.setColor(Color.WHITE);
		int adv = metrics.stringWidth(moduleName);
		int textXOffset = (maxScreenX - screenX - adv) / 2;
		g2.setColor(new Color(0.5f, 0.0f, 0.0f));
		g2.fillRect(screenX + textXOffset - 4, screenY + yPaddingVal, maxScreenX - screenX - textXOffset * 2 + 8, yPadding - 4);
		g2.setColor(Color.WHITE);
		g2.drawString(moduleName, screenX + textXOffset, screenY + textYOffset);
		textYOffset += yPadding - 4;
		yPaddingVal += yPadding - 4;
		g2.setColor(Color.BLUE);
		g2.drawRect(screenX, screenY, maxScreenX - screenX, maxScreenY);
		for(Slider slider: nameToSlider.values()) slider.draw(g2);
	}

	public void pointSelected(int x, int y) {
		for(Slider slider: nameToSlider.values()) {
			slider.pointSelected(x, y);
		}
		parent.view.repaint();
	}
	
	public void loadModuleInfo(BufferedReader in) {
		try {
			for(Slider slider: nameToSlider.values()) {
				slider.setCurrentValue(new Double(in.readLine()));
			}
		} catch (Exception e) {
			System.out.println("ControlBank.loadModuleInfo: Error reading from file");
		}
	}

	@Override
	public void saveModuleInfo(BufferedWriter out) {
		try {
			for(Slider slider: nameToSlider.values()) {
				out.write(new Double(slider.getCurrentValue()).toString());
				out.newLine();
			}
		} catch (Exception e) {
			System.out.println("ControlBank.saveModuleInfo: Error reading from file");
		}
	}
	
}
