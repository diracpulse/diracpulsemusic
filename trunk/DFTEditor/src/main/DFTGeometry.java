package main;

public class DFTGeometry {
	
	public static class SortedPair {
		
		int lower;
		int upper;
		
		public SortedPair(int x1, int x2) {
			this.lower = x1;
			this.upper = x2;
			if(x2 < x1) {
				this.lower = x2;
				this.upper = x1;
			}
		}
		
		public int getLower() {
			return lower;
		}
		
		public int getUpper() {
			return upper; 
		}
	}
	
}