
import java.lang.*;
import java.util.*;
import java.io.*;
import javax.sound.sampled.*;		

class Chords {
	
	public AudioBuffer AB;
	public AudioPlayer AP;
	public int samples = 44100 * 1;
	public int bufferLength = 1024;
	public double minFreq = 400.0;
	public double maxFreq = 20000.0;
	public double scale = 31.0;

	Chords() {
		AB = new AudioBuffer(samples);
		AP = new AudioPlayer();
	}

	public void playChords() {
		//superTrig();
		for (double freq = 20.0; freq < 20000.0; freq *= Math.pow(2.0, 1.0 / 12.0)) {
			System.out.println(freq);
			AB = new AudioBuffer(samples);
			AB.addFrequency(freq);
			try {
				while(AB.hasMore()) AP.playBuffer(AB.getBuffer(samples), 1.0);
				Thread.sleep(100);
			} catch (InterruptedException ex) {}
		}
	}
	
	// Java is extremely slow unless:
	// -pi /4 < theta > pi / 4
	public void superTrig() {
		double silly;
		double startVal = Math.PI / -4.0;
		double endVal = Math.PI / 4.0;
		double step = 2.0 * endVal / 1000000000.0;
		for(double val = startVal; val < endVal; val += step) {
			silly = Math.sin(val);
		}
	}
	
}


public class Test {

	public static void main(String[] args) {
		Chords c = new Chords();
		c.playChords();
		System.exit(0);
	}
	
}
