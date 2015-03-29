
package main.playable;

public interface AudioSource {
	
	public double[] getNextSamples(int numSamples);
	
}
