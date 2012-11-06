import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;


class Interpolate {
		
	public static TreeMap<Integer, FDData> dataInterpolate(FDData.Channel channel, TreeMap<Integer, FDData> input) {
		TreeMap<Integer, FDData> output = new TreeMap<Integer, FDData>();
		if(input.isEmpty()) return output;
		if(input.size() == 1) {
			output.put(input.firstKey(), input.firstEntry().getValue());
			return output;
		}
		long harmonicID = input.firstEntry().getValue().getHarmonicID();
		int lowerTime = input.firstKey();
		double lowerAmpValue = input.get(lowerTime).getLogAmplitude();
		double lowerNoteValue = input.get(lowerTime).getNote();
		for(int upperTime: input.keySet()) {
			if(upperTime == lowerTime) continue;
			double upperAmpValue = input.get(upperTime).getLogAmplitude();
			double upperNoteValue = input.get(upperTime).getNote();
			double ampSlope = (upperAmpValue - lowerAmpValue) / (upperTime - lowerTime);
			double noteSlope = (upperNoteValue - lowerNoteValue) / (upperTime - lowerTime);
			for(int timeIndex = lowerTime; timeIndex < upperTime; timeIndex++) {
				double ampValue = lowerAmpValue + (timeIndex - lowerTime) * ampSlope;
				int noteValue = (int) Math.round(lowerNoteValue + (timeIndex - lowerTime) * noteSlope);
				try {
					//System.out.println(timeIndex + " " +  noteValue + " " +  ampValue + " " +  harmonicID);
					output.put(timeIndex, new FDData(channel, timeIndex, noteValue, ampValue, harmonicID));
				} catch (Exception e) {
					System.out.println("Interpolate.dataInterpolate(): error creating data");
					return null;
				}
			}
			lowerAmpValue = upperAmpValue;
			lowerNoteValue = upperNoteValue;
			lowerTime = upperTime;
		}
		return output;
	}
}
