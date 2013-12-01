package main;

import java.util.ArrayList;
import java.util.TreeMap;

public class FilterConstants {
	
	public static ArrayList<Filter.CriticalBand> criticalBands = null;
	
	public static void initPassFreqToFilterLength() {
		Filter.passFreqToFilterLength = new TreeMap<Float, Integer>();
		Filter.passFreqToFilterLength.put((float)1378.125, 168);
		Filter.passFreqToFilterLength.put((float)1409.2864, 170);
		Filter.passFreqToFilterLength.put((float)1441.1523, 160);
		Filter.passFreqToFilterLength.put((float)1473.7389, 158);
		Filter.passFreqToFilterLength.put((float)1507.0623, 158);
		Filter.passFreqToFilterLength.put((float)1541.139, 150);
		Filter.passFreqToFilterLength.put((float)1575.9865, 148);
		Filter.passFreqToFilterLength.put((float)1611.6218, 144);
		Filter.passFreqToFilterLength.put((float)1648.0629, 140);
		Filter.passFreqToFilterLength.put((float)1685.3279, 138);
		Filter.passFreqToFilterLength.put((float)1723.4355, 134);
		Filter.passFreqToFilterLength.put((float)1762.4049, 138);
		Filter.passFreqToFilterLength.put((float)1802.2555, 132);
		Filter.passFreqToFilterLength.put((float)1843.0071, 126);
		Filter.passFreqToFilterLength.put((float)1884.68, 124);
		Filter.passFreqToFilterLength.put((float)1927.2954, 120);
		Filter.passFreqToFilterLength.put((float)1970.8743, 120);
		Filter.passFreqToFilterLength.put((float)2015.4386, 118);
		Filter.passFreqToFilterLength.put((float)2061.0105, 114);
		Filter.passFreqToFilterLength.put((float)2107.613, 110);
		Filter.passFreqToFilterLength.put((float)2155.269, 108);
		Filter.passFreqToFilterLength.put((float)2204.003, 106);
		Filter.passFreqToFilterLength.put((float)2253.8386, 104);
		Filter.passFreqToFilterLength.put((float)2304.801, 100);
		Filter.passFreqToFilterLength.put((float)2356.916, 98);
		Filter.passFreqToFilterLength.put((float)2410.2092, 98);
		Filter.passFreqToFilterLength.put((float)2464.7075, 88);
		Filter.passFreqToFilterLength.put((float)2520.438, 96);
		Filter.passFreqToFilterLength.put((float)2577.4287, 90);
		Filter.passFreqToFilterLength.put((float)2635.7083, 88);
		Filter.passFreqToFilterLength.put((float)2695.3052, 86);
		Filter.passFreqToFilterLength.put((float)2756.25, 84);
		Filter.passFreqToFilterLength.put((float)2818.5728, 86);
		Filter.passFreqToFilterLength.put((float)2882.3047, 82);
		Filter.passFreqToFilterLength.put((float)2947.4778, 80);
		Filter.passFreqToFilterLength.put((float)3014.1245, 86);
		Filter.passFreqToFilterLength.put((float)3082.278, 76);
		Filter.passFreqToFilterLength.put((float)3151.973, 78);
		Filter.passFreqToFilterLength.put((float)3223.2437, 74);
		Filter.passFreqToFilterLength.put((float)3296.1257, 72);
		Filter.passFreqToFilterLength.put((float)3370.6558, 74);
		Filter.passFreqToFilterLength.put((float)3446.871, 68);
		Filter.passFreqToFilterLength.put((float)3524.8098, 68);
		Filter.passFreqToFilterLength.put((float)3604.511, 66);
		Filter.passFreqToFilterLength.put((float)3686.0142, 64);
		Filter.passFreqToFilterLength.put((float)3769.36, 62);
		Filter.passFreqToFilterLength.put((float)3854.5908, 60);
		Filter.passFreqToFilterLength.put((float)3941.7485, 60);
		Filter.passFreqToFilterLength.put((float)4030.8772, 60);
		Filter.passFreqToFilterLength.put((float)4122.021, 58);
		Filter.passFreqToFilterLength.put((float)4215.226, 56);
		Filter.passFreqToFilterLength.put((float)4310.538, 54);
		Filter.passFreqToFilterLength.put((float)4408.006, 54);
		Filter.passFreqToFilterLength.put((float)4507.6772, 52);
		Filter.passFreqToFilterLength.put((float)4609.602, 52);
		Filter.passFreqToFilterLength.put((float)4713.832, 50);
		Filter.passFreqToFilterLength.put((float)4820.4185, 52);
		Filter.passFreqToFilterLength.put((float)4929.415, 50);
		Filter.passFreqToFilterLength.put((float)5040.876, 48);
		Filter.passFreqToFilterLength.put((float)5154.8574, 46);
		Filter.passFreqToFilterLength.put((float)5271.4165, 44);
		Filter.passFreqToFilterLength.put((float)5390.6104, 44);
		Filter.passFreqToFilterLength.put((float)5512.5, 42);
		Filter.passFreqToFilterLength.put((float)5637.1455, 42);
		Filter.passFreqToFilterLength.put((float)5764.6094, 42);
		Filter.passFreqToFilterLength.put((float)5894.9556, 40);
		Filter.passFreqToFilterLength.put((float)6028.249, 40);
		Filter.passFreqToFilterLength.put((float)6164.556, 38);
		Filter.passFreqToFilterLength.put((float)6303.946, 38);
		Filter.passFreqToFilterLength.put((float)6446.4873, 38);
		Filter.passFreqToFilterLength.put((float)6592.2515, 36);
		Filter.passFreqToFilterLength.put((float)6741.3115, 38);
		Filter.passFreqToFilterLength.put((float)6893.742, 34);
		Filter.passFreqToFilterLength.put((float)7049.6196, 34);
		Filter.passFreqToFilterLength.put((float)7209.022, 34);
		Filter.passFreqToFilterLength.put((float)7372.0283, 34);
		Filter.passFreqToFilterLength.put((float)7538.72, 32);
		Filter.passFreqToFilterLength.put((float)7709.1816, 30);
		Filter.passFreqToFilterLength.put((float)7883.497, 30);
		Filter.passFreqToFilterLength.put((float)8061.7544, 30);
		Filter.passFreqToFilterLength.put((float)8244.042, 30);
		Filter.passFreqToFilterLength.put((float)8430.452, 28);
		Filter.passFreqToFilterLength.put((float)8621.076, 28);
		Filter.passFreqToFilterLength.put((float)8816.012, 28);
		Filter.passFreqToFilterLength.put((float)9015.3545, 28);
		Filter.passFreqToFilterLength.put((float)9219.204, 26);
		Filter.passFreqToFilterLength.put((float)9427.664, 26);
		Filter.passFreqToFilterLength.put((float)9640.837, 26);
		Filter.passFreqToFilterLength.put((float)9858.83, 26);
		Filter.passFreqToFilterLength.put((float)10081.752, 24);
		Filter.passFreqToFilterLength.put((float)10309.715, 24);
		Filter.passFreqToFilterLength.put((float)10542.833, 22);
		Filter.passFreqToFilterLength.put((float)10781.221, 22);
		Filter.passFreqToFilterLength.put((float)11025.0, 22);
	}
	
	
	public static void initFullCriticalBands() {
		criticalBands = new ArrayList<Filter.CriticalBand>();
		criticalBands.add(new Filter.CriticalBand(FDData.minFrequencyInHz, 100));
		criticalBands.add(new Filter.CriticalBand(100, 200));
		criticalBands.add(new Filter.CriticalBand(200, 300));
		criticalBands.add(new Filter.CriticalBand(300, 400));
		criticalBands.add(new Filter.CriticalBand(400, 510));
		criticalBands.add(new Filter.CriticalBand(510, 630));
		criticalBands.add(new Filter.CriticalBand(630, 770));
		criticalBands.add(new Filter.CriticalBand(770, 920));
		criticalBands.add(new Filter.CriticalBand(920, 1080));
		criticalBands.add(new Filter.CriticalBand(1080, 1270));
		criticalBands.add(new Filter.CriticalBand(1270, 1480));
		criticalBands.add(new Filter.CriticalBand(1480, 1720));
		criticalBands.add(new Filter.CriticalBand(1720, 2000));
		criticalBands.add(new Filter.CriticalBand(2000, 2320));
		criticalBands.add(new Filter.CriticalBand(2320, 2700));
		criticalBands.add(new Filter.CriticalBand(2700, 3150));
		criticalBands.add(new Filter.CriticalBand(3150, 3700));
		criticalBands.add(new Filter.CriticalBand(3700, 4400));
		criticalBands.add(new Filter.CriticalBand(4400, 5300));
		criticalBands.add(new Filter.CriticalBand(5300, 6400));
		criticalBands.add(new Filter.CriticalBand(6400, 7700));
		criticalBands.add(new Filter.CriticalBand(7700, 9500));
		criticalBands.add(new Filter.CriticalBand(9500, 12000));
		criticalBands.add(new Filter.CriticalBand(12000, 15500));
		criticalBands.add(new Filter.CriticalBand(15500, FDData.maxFrequencyInHz));
	}
	
	public static void initHalfCriticalBands() {
		if(criticalBands != null) return;
		criticalBands = new ArrayList<Filter.CriticalBand>();
		criticalBands.add(new Filter.CriticalBand(FDData.minFrequencyInHz, 50));
		criticalBands.add(new Filter.CriticalBand(50, 100));
		criticalBands.add(new Filter.CriticalBand(100, 150));
		criticalBands.add(new Filter.CriticalBand(150, 200));
		criticalBands.add(new Filter.CriticalBand(200, 250));
		criticalBands.add(new Filter.CriticalBand(250, 300));
		criticalBands.add(new Filter.CriticalBand(300, 350));
		criticalBands.add(new Filter.CriticalBand(350, 400));
		criticalBands.add(new Filter.CriticalBand(400, 450));
		criticalBands.add(new Filter.CriticalBand(450, 510));
		criticalBands.add(new Filter.CriticalBand(510, 570));
		criticalBands.add(new Filter.CriticalBand(570, 630));
		criticalBands.add(new Filter.CriticalBand(630, 700));
		criticalBands.add(new Filter.CriticalBand(700, 770));
		criticalBands.add(new Filter.CriticalBand(770, 840));
		criticalBands.add(new Filter.CriticalBand(840, 920));
		criticalBands.add(new Filter.CriticalBand(920, 1000));
		criticalBands.add(new Filter.CriticalBand(1000, 1080));
		criticalBands.add(new Filter.CriticalBand(1080, 1170));
		criticalBands.add(new Filter.CriticalBand(1170, 1270));
		criticalBands.add(new Filter.CriticalBand(1270, 1370));
		criticalBands.add(new Filter.CriticalBand(1370, 1480));
		criticalBands.add(new Filter.CriticalBand(1480, 1600));
		criticalBands.add(new Filter.CriticalBand(1600, 1720));
		criticalBands.add(new Filter.CriticalBand(1720, 1850));
		criticalBands.add(new Filter.CriticalBand(1850, 2000));
		criticalBands.add(new Filter.CriticalBand(2000, 2150));
		criticalBands.add(new Filter.CriticalBand(2150, 2320));
		criticalBands.add(new Filter.CriticalBand(2320, 2500));
		criticalBands.add(new Filter.CriticalBand(2500, 2700));
		criticalBands.add(new Filter.CriticalBand(2700, 2900));
		criticalBands.add(new Filter.CriticalBand(2900, 3150));
		criticalBands.add(new Filter.CriticalBand(3150, 3400));
		criticalBands.add(new Filter.CriticalBand(3400, 3700));
		criticalBands.add(new Filter.CriticalBand(3700, 4000));
		criticalBands.add(new Filter.CriticalBand(4000, 4400));
		criticalBands.add(new Filter.CriticalBand(4400, 4800));
		criticalBands.add(new Filter.CriticalBand(4800, 5300));
		criticalBands.add(new Filter.CriticalBand(5300, 5800));
		criticalBands.add(new Filter.CriticalBand(5800, 6400));
		criticalBands.add(new Filter.CriticalBand(6400, 7000));
		criticalBands.add(new Filter.CriticalBand(7000, 7700));
		criticalBands.add(new Filter.CriticalBand(7700, 8500));
		criticalBands.add(new Filter.CriticalBand(8500, 9500));
		criticalBands.add(new Filter.CriticalBand(9500, 10500));
		criticalBands.add(new Filter.CriticalBand(10500, 12000));
		criticalBands.add(new Filter.CriticalBand(12000, 13500));
		criticalBands.add(new Filter.CriticalBand(13500, 15500));
		criticalBands.add(new Filter.CriticalBand(15500, FDData.maxFrequencyInHz));
	}

}
