
import java.io.*;
import java.util.*;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

public class ArduinoComm implements Runnable, SerialPortEventListener {
	
    static CommPortIdentifier portId;
    static Enumeration portList;

    InputStream inputStream;
    OutputStream outputStream;
    SerialPort serialPort;
    Thread readThread;
    DataViewer parent;
    boolean serialPortActive = false;

    public static void initRead() {
        portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                 if (portId.getName().equals("COM4")) {
			//                if (portId.getName().equals("/dev/term/a")) {
                    break;
                }
            }
        }
    }

    public ArduinoComm(DataViewer parent) {
    	this.parent = parent;
    	initRead();
        try {
            serialPort = (SerialPort) portId.open("ArduinoSynth", 2000);
        } catch (PortInUseException e) {
        	System.out.println(e);
        	return;
        }
        try {
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();
        } catch (IOException e) {
        	System.out.println(e);
        	return;
        }
        try {
            serialPort.addEventListener(this);
        } catch (TooManyListenersException e) {
        	System.out.println(e);
        	return;
        }
        serialPort.notifyOnDataAvailable(true);
        try {
            serialPort.setSerialPortParams(31500,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);
        } catch (UnsupportedCommOperationException e) {
        	System.out.println(e);
        	return;
        }
        readThread = new Thread(this);
        readThread.start();
        serialPortActive = true;
    }

    public void run() {
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {System.out.println(e);}
    }

    public void serialEvent(SerialPortEvent event) {
        switch(event.getEventType()) {
        case SerialPortEvent.BI:
        case SerialPortEvent.OE:
        case SerialPortEvent.FE:
        case SerialPortEvent.PE:
        case SerialPortEvent.CD:
        case SerialPortEvent.CTS:
        case SerialPortEvent.DSR:
        case SerialPortEvent.RI:
        case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
            break;
        case SerialPortEvent.DATA_AVAILABLE:
            try {
            	//int command = inputStream.read();
            	//if(command < 128) return;
            	ArrayList<Integer> data = new ArrayList<Integer>();
            	if(inputStream.available() >= 1) {
                	data.add(0);
                	data.add(inputStream.read());
                	parent.readSerialPortData(data);
                }
            } catch (IOException e) {System.out.println(e);}
            break;
        }
    }
    
    public void sendData(int[] data) {
    	if(!serialPortActive) return;
        try {
        	for(int d: data) outputStream.write(d);
        } catch (IOException e) {System.out.println(e);}	
    }
    
}
