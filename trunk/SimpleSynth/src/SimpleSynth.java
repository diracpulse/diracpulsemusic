
import java.lang.*;
import java.util.*;

public class SimpleSynth {
	
	public static Random randomGenerator;
	public static NestedHashMap ntm;
	
	public static void genArrays() {
		ntm = new NestedHashMap();
		for(int i1 = 0; i1 < 5; i1++) {
			for(int i2 = 0; i2 < 4; i2++) {
				for(int i3 = 0; i3 < 3; i3++) {
					for(int i4 = 0; i4 < 2; i4++) {
						int[] array = new int[]{i1,i2,i3,i4};
						ntm.addArray(array);
					}
				}
			}
		}
		removeArrays();
		printArrays();
	}
	
	public static void removeArrays() {
		int numArrays = 0;
		for(int i1 = 0; i1 < 5; i1 += 2) {
			for(int i2 = 0; i2 < 4; i2 += 2) {
				for(int i3 = 0; i3 < 3; i3 += 2) {
					for(int i4 = 0; i4 < 2; i4 += 2) {
						int[] array = new int[]{i1,i2,i3,i4};
						NestedHashMap.printArray(array);
						ntm.removeArray(array);
						numArrays++;
					}
				}
			}
		}
		System.out.println("NumArrays: " + numArrays);
	}
	
	public static void printArrays() {
		int loopIndex = 0;
		for(int index = 0; index < 1200; index++) {
			ArrayList<Integer> values = ntm.getRandomArray();
			if(values.size() < 4) continue;
			System.out.print(loopIndex + ": ");
			for(int value: values) {
				System.out.print(value + " ");
			}
			System.out.println();
			loopIndex++;
		}
	}

	public static void main2(String[] args) {
		Chords c = new Chords();
		c.playChords();
		System.exit(0);
	}
	
	public static void main(String[] args) {
		randomGenerator = new Random();
		genArrays();
		System.exit(0);
	}
	
}
