import java.util.HashMap;
import java.util.TreeMap;


public class MathTools {
	
	// Pow2 Lookup Table Objects
	private static final int pow2DigitLookupSize = 24;
	private static final int pow2FractionLookupSize = 1000 * 1000;
	private static TreeMap<Integer, Double> pow2DigitLookup = null;
	private static TreeMap<Integer, Double> pow2FractionLookup = null;
	private static double numLoops = 1000 * 1000;
	
	public MathTools() {
		initPow2Tables();
	}
	
	public void runTests() {
		powSpeedTest();
		nativePowSpeedTest();
		sinSpeedTest();
		nativeSinSpeedTest();
	}
	
	void powSpeedTest() {
		double result = 0.0;
		double maxVal = 10.0;
		long startSynthTimeInMillis = System.currentTimeMillis();
		for(double exponent = 0.0; exponent < maxVal; exponent += maxVal/numLoops) {
			result += fastPow2(exponent);
		}
		long timeElapsed = System.currentTimeMillis() - startSynthTimeInMillis;
		System.out.println("MathTools.powSpeedTest: Time Elapsed = " + timeElapsed + " " + result);
	}
	
	void nativePowSpeedTest() {
		double result = 0.0;
		double maxVal = 10.0;
		long startSynthTimeInMillis = System.currentTimeMillis();
		for(double exponent = 0.0; exponent < maxVal; exponent += maxVal/numLoops) {
			result += nativePow2(exponent);
		}
		long timeElapsed = System.currentTimeMillis() - startSynthTimeInMillis;
		System.out.println("MathTools.nativePowSpeedTest: Time Elapsed = " + timeElapsed + " " + result);
	}
	
	void sinSpeedTest() {
		double result = 0.0;
		double numLoops = 10 * 1000 * 1000;
		double maxVal = numLoops;
		long startSynthTimeInMillis = System.currentTimeMillis();
		double angle = 0.0;
		for(int loops = 0; loops < numLoops; loops++) {
			if(angle > Math.PI) angle -= Math.PI;
			result += fastSin(angle);
			angle += maxVal/numLoops;
		}
		long timeElapsed = System.currentTimeMillis() - startSynthTimeInMillis;
		System.out.println("SynthTools.sinSpeedTest: Time Elapsed = " + timeElapsed + " " + result);
	}
	
	void nativeSinSpeedTest() {
		double result = 0.0;
		double numLoops = 10 * 1000 * 1000;
		double maxVal = numLoops;
		long startSynthTimeInMillis = System.currentTimeMillis();
		double angle = 0.0;
		for(int loops = 0; loops < numLoops; loops++) {
			if(angle > Math.PI) angle -= Math.PI;
			result += Math.sin(angle);
			angle += maxVal/numLoops;
		}
		long timeElapsed = System.currentTimeMillis() - startSynthTimeInMillis;
		System.out.println("SynthTools.nativeSinSpeedTest: Time Elapsed = " + timeElapsed + " " + result);
	}
	
	// must be between -PI and PI (inclusive)
	double fastSin(double theta) {
		if(theta > Math.PI / 4.0) return -1.0 * Math.sin(theta - Math.PI / 2.0);
		if(theta < Math.PI / -4.0) return -1.0 * Math.sin(theta + Math.PI + 2.0);
		return Math.sin(theta);
	}
	
	double nativePow2(double exponent) {
		return Math.exp(exponent * Math.log(2.0));
	}
	
	double nativeSin(double theta) {
		return Math.sin(theta);
	}
	
	//exponent must be < 100.0;
	double fastPow2(double exponent) {
		int digits = (int) Math.floor(exponent);
		int fraction = (int) Math.floor((exponent - digits) * pow2FractionLookupSize);
		//System.out.println(digits + " " + fraction);
		return pow2DigitLookup.get(digits) * pow2FractionLookup.get(fraction);
	}
	
	static void initPow2Tables() {
		pow2DigitLookup = new TreeMap<Integer, Double>();
		pow2FractionLookup = new TreeMap<Integer, Double>();
		for(int i = 0; i < pow2DigitLookupSize; i++) {
			pow2DigitLookup.put(i, Math.pow(2.0, i));
		}
		for(int i = 0; i < pow2FractionLookupSize; i++) {
			pow2FractionLookup.put(i, Math.pow(2.0, i / pow2FractionLookupSize));
		}
	}
	
}
