
public class Chords {
	
	public static int[] getChord(int index) {
		switch(index) {
			case 0:
				return new int[] {6, 7, 8};
			case 1:
				return new int[] {7, 6, 8};
			case 2:
				return new int[] {6, 7, 10};
			case 3:
				return new int[] {7, 6, 10};
			case 4:
				return new int[] {8, 10, 6};
			case 5:
				return new int[] {8, 10, 7};
			case 6:
				return new int[] {10, 8, 6};
			case 7:
				return new int[] {10, 8, 7};
			case 8:
				return new int[] {13, 10};
			case 9:
				return new int[] {10, 13};
		}
		return null;
	}
	
	
}