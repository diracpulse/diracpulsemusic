import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;


class Interpolate {
	
	public enum FDDataType {
		AMPLITUDE,
		NOTE;
	}
	
	public static ArrayList<FDData> dataInterpolate(ArrayList<FDData> input) {
		ArrayList<FDData> middleVal = dataInterpolate(input, FDDataType.AMPLITUDE);
		return dataInterpolate(middleVal, FDDataType.NOTE);
	}

	public static ArrayList<FDData> dataInterpolate(ArrayList<FDData> input, FDDataType type) {
		ArrayList<FDData> output = new ArrayList<FDData>();
		if(input.isEmpty()) return output;
		int lowerTime = input.get(0).getTime();
		double lowerValue = input.get(0).getLogAmplitude();
		FDData currentData = input.get(0);
		for(int index = 1; index < input.size(); index++) {
			int upperTime = input.get(index).getTime();
			double upperValue = 0.0;
			if(type == FDDataType.AMPLITUDE) upperValue = input.get(index).getLogAmplitude();
			if(type == FDDataType.NOTE) upperValue = input.get(index).getNote();
			double slope = (upperValue - lowerValue) / (upperTime - lowerTime);
			for(int timeIndex = lowerTime; timeIndex < upperTime; timeIndex++) {
				double value = lowerValue + (timeIndex - lowerTime) * slope;
				try {
					if(type == FDDataType.AMPLITUDE) {
						output.add(new FDData(timeIndex, currentData.getNote(), value, currentData.getHarmonicID()));
					}
					if(type == FDDataType.NOTE) {
						output.add(new FDData(timeIndex, value, currentData.getLogAmplitude(), currentData.getHarmonicID()));
					}
				} catch (Exception e) {
					System.out.println("LogLinear.dataInterpolate(): error creating data");
					return null;
				}
				lowerValue = upperValue;
				lowerTime = upperTime;
			}
		}
		return output;
	}
	
}
