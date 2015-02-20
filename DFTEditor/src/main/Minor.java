
package main;

import java.util.ArrayList;

public class Minor {

	public static final int[] majorScale = {0, 2, 4, 5, 7, 9, 11};
	public static final int[] minorScale = {0, 2, 3, 5, 7, 8, 10};
	public static final int[] ascendingMinorScale = {0, 2, 3, 5, 7, 9, 11};
	public static final int[] harmonicMinorScale = {0, 2, 3, 5, 7, 8, 11};
	public static final int[] majorTriad = {4, 7};
	public static final int[] minorTriad = {3, 7};
	public static final int[] diminsihedTriad = {3, 6};
	public static final int[] augmentedTriad = {4, 8};
	public static final double[] allJustIntonation = {1.0, 9.0 / 8.0, 6.0 / 5.0, 5.0 / 4.0, 4.0 / 3.0, 3.0 / 2.0, 5.0 / 3.0, 8.0 / 5.0, 9.0 / 5.0, 15.0 / 8.0};

	public enum Scale {
		minor, acendingMinor, harmonicMinor
	}
	
	public enum Triad {
		i, ii, iiDim, III, IIIAug, iv, IV, V, v, VI, viSharpDim, viiDim, VII 
	}

	public static class TriadNotes {
		
		int[] i = {0, 3, 7};
		int[] ii = {2, 2 + 3, 2 + 7};
		int[] iiDim = {2, 2 + 3, 2 + 6};
		int[] III = {3, 3 + 4, 3 + 7};
		int[] IIIAug = {3, 3 + 4, 3 + 8};
		int[] iv = {5, 5 + 3, 5 + 7};
		int[] IV = {5, 5 + 4, 5 + 7};
		int[] V = {7, 7 + 4, 7 + 7};
		int[] v = {7, 7 + 3, 7 + 7};
		int[] VI = {8, 8 + 4, 8 + 7};
		int[] viSharpDim = {9, 9 + 3, 9 + 6};
		int[] viiDim = {10, 10 + 3, 10 + 6};
		int[] VII = {10, 10 + 4, 10 + 7};

		int[] VIAsc = {9, 9 + 4, 9 + 7};
		int[] viSharpDimAsc = {10, 10 + 3, 10 + 6};
		int[] viiDimAsc = {11, 11 + 3, 11 + 6};
		int[] VIIAsc = {11, 11 + 4, 11 + 7};
		
		int[] getNotes(Scale scale, Triad triad) {
			switch(triad) {
			case i:
				return i;
			case ii:
				return ii;
			case iiDim:
				return iiDim;
			case III:
				return III;
			case IIIAug:
				return IIIAug;
			case iv:
				return iv;
			case IV:
				return IV;
			case V:
				return V;
			case v:
				return v;
			case VI:
				if(scale == Scale.acendingMinor) {
					return VIAsc;
				} else {
					return VI;
				}
			case viSharpDim:
				if(scale == Scale.acendingMinor) {
					return viSharpDimAsc;
				} else {
					return viSharpDim;
				}
			case viiDim:
				if(scale == Scale.minor) {
					return viiDim;
				} else {
					return viiDimAsc;
				}
			case VII:
				if(scale == Scale.minor) {
					return VII;
				} else {
					return VIIAsc;
				}
			}
			return null;
		}
	}
	
	public static class Progressions {	
		
		Triad[] i = {Triad.i, Triad.ii, Triad.iiDim, Triad.III, Triad.IIIAug, Triad.iv, Triad.IV, Triad.V, Triad.v, Triad.VI, Triad.viSharpDim, Triad.viiDim, Triad.VII};
		Triad[] ii = {Triad.i, Triad.III, Triad.V, Triad.v, Triad.viiDim, Triad.VII};
		Triad[] iiDim = {Triad.i, Triad.III, Triad.V, Triad.v, Triad.viiDim, Triad.VII};
		Triad[] III = {Triad.i, Triad.iv, Triad.IV, Triad.VI, Triad.viSharpDim, Triad.viiDim, Triad.VI};
		Triad[] IIIAug = {Triad.i, Triad.iv, Triad.IV, Triad.VI, Triad.viSharpDim, Triad.viiDim, Triad.VI};;
		Triad[] iv = {Triad.i, Triad.V, Triad.v, Triad.viiDim, Triad.VII};
		Triad[] IV = {Triad.i, Triad.V, Triad.v, Triad.viiDim, Triad.VII};
		Triad[] v = {Triad.i, Triad.VI, Triad.viSharpDim};
		Triad[] V = {Triad.i, Triad.VI, Triad.viSharpDim};
		Triad[] VI = {Triad.i, Triad.III, Triad.IIIAug, Triad.iv, Triad.IV, Triad.V, Triad.v, Triad.viiDim, Triad.VII};
		Triad[] viSharpDim = {Triad.i, Triad.III, Triad.IIIAug, Triad.iv, Triad.IV, Triad.V, Triad.v, Triad.viiDim, Triad.VII};
		Triad[] viiDim = {Triad.i};
		Triad[] VII = {Triad.i};
		
		ArrayList<ArrayList<Integer>> getNextSequence(int length) {
			return null;
		}
		
	}

}
