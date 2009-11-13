
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.lang.*;
import java.io.*;

public class DFTEditor extends JPanel implements MouseListener, ActionListener {
	
	private JPanel drawingPane;
	private TreeMap<Integer, TreeMap> timeToFreqToAmp;
	private int xStep = 6;
	private int yStep = 9; // one digit;
	private int topYStep = 8; // used by DrawUpperTimes
	private int leftOffset = xStep * 5; // start of first data cell
	private int upperOffset = topYStep * 4; // start of first data cell
	private int leftX = 0; // index of freq in leftmost data cell
	private int upperY = 0; // index of time in uppermost data cell
	private float timeStep = 0.01f;
	private float maxAmplitude;
	private float minAmplitude;
	private int freqsPerOctave = 31;
	private int minFreq;
	private int maxFreq;
	private int maxTime;
	private int dataXDim;
	private int dataYDim;
	
	public void ReadFileData(String inputFile) {
		timeToFreqToAmp = new TreeMap<Integer, TreeMap>();
		TreeMap<Integer, Float> freqToAmp;
		String[] tokens;
		RandomAccessFile file = null;
		String linein = "";
		maxAmplitude = 0.0f;
		minAmplitude = 15.0f;
		minFreq = freqsPerOctave * 100;
		maxFreq = 0;
		maxTime = 0;
		Integer time;
		Integer freq;
		Float amp;
	    try {
			file = new RandomAccessFile("out.txt", "r");
		} catch (FileNotFoundException nf) {
			System.out.println("DFTEditor: " + inputFile + " not found");
			System.exit(0);
		}
		try {
			linein = file.readLine();
			while(linein != null) {
				tokens = linein.split(" ");
				time = new Integer(tokens[0]);
				freq = new Integer(tokens[1]);
				amp = new Float(tokens[2]);
				if(amp.floatValue() > maxAmplitude) {
					maxAmplitude = amp.floatValue();
				}
				if(amp.floatValue() < minAmplitude) {
					minAmplitude = amp.floatValue();
				}
				if(freq.intValue() > maxFreq) {
					maxFreq = freq.intValue();
				}
				if(freq.intValue() < minFreq) {
					minFreq = freq.intValue();
				}				
				if(time.intValue() > maxTime) {
					maxTime = time.intValue();
				}				
				if (timeToFreqToAmp.containsKey(time)) {
					freqToAmp = timeToFreqToAmp.get(time);
				} else {
					timeToFreqToAmp.put(time, new TreeMap<Integer, Float>());
					freqToAmp = timeToFreqToAmp.get(time);
				}
				freqToAmp.put(freq, amp);
				linein = file.readLine();
			}
			file.close();
		} catch (IOException ie) {
			System.out.println("DFTEditor: error reading from: " + inputFile);
			System.exit(0);
		}
		dataXDim = maxTime;
		dataYDim = maxFreq - minFreq;
	}
	
	public void DrawUpperTimes(Graphics g) {
		int timeIndex;
		int iTime;
		int digitPlace;
		int digitVal;
		boolean leading0;
		Color f;
		Color b;
		Color blank = new Color(0.0f, 0.0f, 0.0f);
		int screenX;
		int screenY;
		int startX = leftX;
		int endX = leftX + ((drawingPane.getWidth() - leftOffset) / xStep);
		for(timeIndex = startX; timeIndex < endX; timeIndex++) {
			iTime = timeIndex;
			leading0 = true;
			int yOffset = 0;
			for(digitPlace = 1000; digitPlace >= 1; digitPlace /= 10) {
				digitVal = iTime / digitPlace;
				if((digitVal == 0) && leading0 && (digitPlace != 1)) {
					yOffset++;
					continue;
				}
				leading0 = false;
				iTime -= digitVal * digitPlace;
				if(digitPlace > 10) {
					b = new Color(1.0f, 1.0f, 1.0f);
					f = new Color(0.0f, 0.0f, 0.0f);
				} else {
					f = new Color(1.0f, 1.0f, 1.0f);
					b = new Color(0.0f, 0.0f, 0.0f);					
				}
				if(timeIndex >= dataXDim) {
					f = blank;
					b = blank;
				}
				screenX = leftOffset + ((timeIndex - startX) * xStep);
				screenY = yOffset * topYStep;
				g.setColor(b);
				g.fillRect(screenX, screenY, 6, 8);				
				SevenSegmentSmall(g, f, b, screenX, 
				                           screenY, 
				                           digitVal);
				yOffset++;
			}
		}
	}


	public void DrawLeftFreqs(Graphics g) {
		int x;
		int y;
		int freqIndex;
		int iFreq;
		int digitPlace;
		int digitVal;
		Color f;
		Color b;
		Color blank = new Color(0.0f, 0.0f, 0.0f);
		int startX = 0;
		int startY = upperY;
		int endY = startY + ((drawingPane.getHeight() - upperOffset) / yStep);
		int screenX;
		int screenY;
		for(freqIndex = startY; freqIndex < endY; freqIndex++) {
			if(freqIndex < dataYDim) {			
				iFreq = maxFreq - freqIndex;
			} else {
				iFreq = 1; // dummy value
			}
			int xOffset = 0;
			for(digitPlace = 10000; digitPlace >= 1; digitPlace /= 10) {
				digitVal = iFreq / digitPlace;
				iFreq -= digitVal * digitPlace;
				f = new Color(1.0f, 1.0f, 1.0f);
				b = new Color(0.0f, 0.0f, 0.0f);
				if(freqIndex >= dataYDim) {
					f = blank;
					b = blank;
				}
				screenX = xOffset * xStep;
				screenY = upperOffset + ((freqIndex - startY) * yStep);
				g.setColor(b);
				g.fillRect(screenX, screenY, 6, 8);
				SevenSegmentSmall(g, f, b, screenX, 
				                           screenY, 
				                           digitVal);
				xOffset++;
			}
		}
	}
			
			
		
	public void DrawFileData(Graphics g, boolean scaleLines) {
		DrawLeftFreqs(g);
		DrawUpperTimes(g);
		// clear old data
		g.setColor(new Color(0.0f, 0.0f, 0.0f));
		g.fillRect(leftOffset, upperOffset, drawingPane.getWidth(), drawingPane.getHeight());
		int startX = leftX;
		int endX = startX + ((drawingPane.getWidth() - leftOffset) / xStep);
		int startY = upperY;
		int endY = startY + ((drawingPane.getHeight() - upperOffset) / yStep);
		float red;
		float green;
		float blue;
		TreeMap<Integer, Float> currentMap;
		float currentVal;
		Float FVal;
		float ampRange = maxAmplitude - minAmplitude;
		Color b; // background
		Color f = new Color(0.0f, 0.0f, 0.0f); // forground
		Color black = new Color(0.0f, 0.0f, 0.0f);
		Color white = new Color(1.0f, 1.0f, 1.0f);
		Color gray = new Color(0.5f, 0.5f, 0.5f);
		int digitVal;
		float fDigitVal;
		int fractionVal;
		int screenX; // for whole part
		int screenY;
		int lowerScreenY; // for decimal part
		int bottomScreenY;
		// for(int x = 0; x < dataXDim; x++) {
		//	for(int y = 0; y < dataYDim; y++) {
		for(int x = startX; x < endX; x++) {
			if(x >= dataXDim) continue;
			if(!timeToFreqToAmp.containsKey(new Integer(x))) continue;
			currentMap = timeToFreqToAmp.get(new Integer(x));
			for(int y = startY; y < endY; y++) {
				if(y >= dataYDim) continue;
				if(!currentMap.containsKey(new Integer(maxFreq - y))) continue;
				currentVal = currentMap.get(new Integer(maxFreq - y)).floatValue();
				if(currentVal > 10.0f) {
					digitVal = (int) Math.floor(currentVal);
					digitVal -= 10;					
				} else {
					digitVal = (int) Math.floor(currentVal);
				}
				fractionVal = (int) Math.floor((currentVal - Math.floor(currentVal)) * 10.0f);
				currentVal -= minAmplitude;
				blue = 1.0f - currentVal / ampRange;	
				red = currentVal / ampRange;
				if(red >= 0.5f) {
					green = (1.0f - red) * 2.0f;
				} else {
					green = red * 2.0f;
				}
				// g.setColor(new Color(red, green, blue));
				// g.fillRect(x * xStep, y * yStep, xStep, yStep);
				b = new Color(red, green, blue);
				screenX = leftOffset + ((x - leftX) * xStep);
				screenY = upperOffset + ((y - upperY) * yStep);
				DrawSegmentData(g, b, screenX, screenY, digitVal, fractionVal); 
			}
		}
	}
	
	public void DrawSegmentData(Graphics g, Color b, int screenX, int screenY, int digitVal, int fractionVal) {
		Color black = new Color(0.0f, 0.0f, 0.0f);
		// int lowerScreenY = screenY + topYStep;				
		g.setColor(b);
		g.fillRect(screenX, screenY, xStep, yStep);;
		SevenSegmentSmall(g, black, b, screenX, screenY, digitVal);
		// SevenSegmentSmall(g, black, b, screenX, lowerScreenY, fractionVal);
		// g.setColor(black);
		// int bottomScreenY = screenY + yStep - 1;
		// g.drawLine(screenX, bottomScreenY, screenX + xStep, bottomScreenY);
	}
	
		// (x, y) = (left, top)
	// Segment numbering (0 for OFF, otherwise ON):
	//  0
	// 6 1
	//  2
	// 5 3
	//  4
	// Color f = forground (segment) color
	// Color g = background (segment block) color
	// int digitVal = digit to display (0 - 9)
	public void SevenSegmentSmall(Graphics g, Color f, Color b, int x, int y, int digitVal) {
		int[] segments;
		int[][] digits = {	{1, 1, 0, 1, 1, 1, 1},
							{0, 1, 0, 1, 0, 0, 0},
							{1, 1, 1, 0, 1, 1, 0},
							{1, 1, 1, 1, 1, 0, 0},
							{0, 1, 1, 1, 0, 0, 1},
							{1, 0, 1, 1, 1, 0, 1},
							{1, 0, 1, 1, 1, 1, 1},
							{1, 1, 0, 1, 0, 0, 0},
							{1, 1, 1, 1, 1, 1, 1},
							{1, 1, 1, 1, 1, 0, 1}
						 };
		int[] errDigit = 	{1, 0, 1, 0, 1, 1, 1};
		if((digitVal >= 0) && (digitVal < 10)) {
			segments = digits[digitVal];
		} else {
			segments = errDigit;
		}
		// draw background
		// g.setColor(b);
		// g.fillRect(x, y, 6, 8);
		g.setColor(f);
		// top segment
		if(segments[0] != 0) g.drawLine((x + 2), (y + 1), (x + 4), (y + 1));
		// middle segment
		if(segments[2] != 0) g.drawLine((x + 2), (y + 4), (x + 4), (y + 4));
		// bottom segment
		if(segments[4] != 0) g.drawLine((x + 2), (y + 7), (x + 4), (y + 7));
		// upper right segment
		if(segments[1] != 0) g.drawLine((x + 5), (y + 2), (x + 5), (y + 3));
		// lower right segment
		if(segments[3] != 0) g.drawLine((x + 5), (y + 5), (x + 5), (y + 6));
		// lower left segment
		if(segments[5] != 0) g.drawLine((x + 1), (y + 5), (x + 1), (y + 6));
		// upper left segment
		if(segments[6] != 0) g.drawLine((x + 1), (y + 2), (x + 1), (y + 3));
	}
	
	public JMenuBar createMenuBar() {
	    JMenuBar menuBar;
        JMenu menu;
        JMenuItem menuItem;
        JButton button;
        
        //Create the menu bar.
        menuBar = new JMenuBar();

        //Build the first menu.
        menu = new JMenu("File");
        menuBar.add(menu);
        
        menuItem = new JMenuItem("Open");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Exit");
        menuItem.addActionListener(this);
        menu.add(menuItem);
 
        button = new JButton("F+1");
        button.addActionListener(this);
        menuBar.add(button);
        button = new JButton("F+6");
        button.addActionListener(this);
        menuBar.add(button);
        button = new JButton("F+31");
        button.addActionListener(this);
        menuBar.add(button);       
        button = new JButton("T+1");
        button.addActionListener(this);
        menuBar.add(button);
        button = new JButton("T+10");
        button.addActionListener(this);
        menuBar.add(button);
        button = new JButton("T+100");
        button.addActionListener(this);
        menuBar.add(button);        
        button = new JButton("F-1");
        button.addActionListener(this);
        menuBar.add(button);
        button = new JButton("F-6");
        button.addActionListener(this);
        menuBar.add(button);
        button = new JButton("F-31");
        button.addActionListener(this);
        menuBar.add(button);
        button = new JButton("T-1");
        button.addActionListener(this);
        menuBar.add(button);
        button = new JButton("T-10");
        button.addActionListener(this);
        menuBar.add(button);       
        button = new JButton("T-100");
        button.addActionListener(this);
        menuBar.add(button);             
        return menuBar;

    }
	
    public void actionPerformed(ActionEvent e) {
	    int apOldUpperY = upperY;
	    int apOldLeftX = leftX;
        if ("F+1".equals(e.getActionCommand())) {
			if((upperY + 1) < dataYDim) upperY += 1; 
        }
        if ("F+6".equals(e.getActionCommand())) {
			if((upperY + 6) < dataYDim) upperY += 6; 
        }
        if ("F+31".equals(e.getActionCommand())) {
			if((upperY + 31) < dataYDim) upperY += 31; 
        }         
        if ("T+1".equals(e.getActionCommand())) {
			if((leftX + 1) < dataXDim) leftX += 1; 
        }
        if ("T+10".equals(e.getActionCommand())) {
			if((leftX + 10) < dataXDim) leftX += 10; 
        }
        if ("T+100".equals(e.getActionCommand())) {
			if((leftX + 100) < dataXDim) leftX += 100; 
        }        
        if ("F-1".equals(e.getActionCommand())) {
			if((upperY - 1) >= 0) upperY -= 1; 
        }
        if ("F-6".equals(e.getActionCommand())) {
			if((upperY - 6) >= 0) upperY -= 6; 
        }
        if ("F-31".equals(e.getActionCommand())) {
			if((upperY - 31) >= 0) upperY -= 31; 
        }  
        if ("T-1".equals(e.getActionCommand())) {
			if((leftX - 1) >= 0) leftX -= 1; 
        }
        if ("T-10".equals(e.getActionCommand())) {
			if((leftX - 10) >= 0) leftX -= 10; 
        }
        if ("T-100".equals(e.getActionCommand())) {
			if((leftX - 100) >= 0) leftX -= 100; 
        }        
        if((apOldUpperY != upperY) || (apOldLeftX != leftX)) {
	        DrawFileData(drawingPane.getGraphics(), true);
    	}
    }
	

    public DFTEditor() {
        super(new BorderLayout());

        ReadFileData("out.txt");

        //Set up the drawing area.
        drawingPane = new DrawingPane();
        drawingPane.setBackground(Color.black);
        drawingPane.addMouseListener(this);
        drawingPane.setPreferredSize(new Dimension(928, 762));

        //Lay out this demo.
        add(drawingPane, BorderLayout.CENTER);
    }

    /** The component inside the scroll pane. */
    public class DrawingPane extends JPanel {
        protected void paintComponent(Graphics g) {
	        super.paintComponent(g);
            DrawFileData(g, true);
        }
    }

    //Handle mouse events.
	public void mouseReleased(MouseEvent e) {}
	

	
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){}

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        JFrame frame = new JFrame("ScrollDemo2");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        DFTEditor newContentPane = new DFTEditor();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setJMenuBar(newContentPane.createMenuBar()); // set up menu
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
    


	public void mouseClicked(MouseEvent e){
		/*
	    int xPos = e.getX();
	    int yPos = e.getY();
	    int freqIndex;
	    int timeIndex;
	    if ((xPos > leftOffset) && (yPos > upperOffset)) { 
	    	timeIndex = (xPos - leftOffset) / xStep + leftX;
	    	freqIndex = (yPos - upperOffset) / yStep + upperY;
	    	if((freqIndex >= dataYDim) || (timeIndex >= dataXDim)) {
		    	// OUT OF BOUNDS
		    	System.out.println("OUT OF BOUNDS");
		    	return;
	    	}
	    	// DATA CELL SELECTED
	    	System.out.println("DATA: Freq = " + ((Float) freqs.get(freqIndex)) 
	    						+ " | Time: " + (timeIndex * timeStep) 
	    						+ " | Amplitude: " + data[timeIndex][freqIndex]);
	    	return;
    	}
    	if (xPos <= leftOffset) {
	    	if (yPos <= upperOffset) {
		    	// UPPER LEFT CORNER SELECTED
	    		System.out.println("UPPER LEFT CORNER");
	    		return;
    		}
    		// LEFT FREQUENCY SELECTED
    		freqIndex = (yPos - upperOffset) / yStep + upperY;
    		System.out.println("FREQUENCY: " + ((Float) freqs.get(freqIndex)));
    		return;
    	}
    	// UPPER TIME SELECTED
    	timeIndex = (xPos - leftOffset) / xStep + leftX;
	    System.out.println("TIME: " + (timeIndex * timeStep));
	    return;
	    */
	}
	
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
