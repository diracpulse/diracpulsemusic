package main;

import java.util.ArrayList;
import java.util.TreeMap;

public class FilterConstants {
	
	public static ArrayList<Filter.CriticalBand> criticalBands = null;
	public static double alpha = Filter.alpha;
	
	public static void initNoiseCriticalBandsWithSoftFilters(ArrayList<Filter.CriticalBand> noiseCriticalBands) {
		noiseCriticalBands.get(1).setLPFilter(Filter.getLPFilter(226.04889190440082 , 532, alpha)); // 0.0 226.04889190440082 331.6438667250413 4.0
		noiseCriticalBands.get(1).setHPFilter(Filter.getHPFilter(123.08328430195176 , 2048, alpha)); // -0.0 123.08328430195176 21.533203125 1.0
		noiseCriticalBands.get(2).setLPFilter(Filter.getLPFilter(331.6438667250413 , 500, alpha)); // 0.0 331.6438667250413 441.2617959739804 5.0
		noiseCriticalBands.get(2).setHPFilter(Filter.getHPFilter(226.04889190440082 , 538, alpha)); // -0.0 226.04889190440082 123.08328430195176 1.5
		noiseCriticalBands.get(3).setLPFilter(Filter.getLPFilter(441.2617959739804 , 436, alpha)); // 0.0 441.2617959739804 556.5976608219034 5.5
		noiseCriticalBands.get(3).setHPFilter(Filter.getHPFilter(331.6438667250413 , 488, alpha)); // -0.0 331.6438667250413 226.04889190440082 2.5
		noiseCriticalBands.get(4).setLPFilter(Filter.getLPFilter(556.5976608219034 , 422, alpha)); // 0.0 556.5976608219034 679.1050899851506 6.5
		noiseCriticalBands.get(4).setHPFilter(Filter.getHPFilter(441.2617959739804 , 466, alpha)); // -0.0 441.2617959739804 331.6438667250413 3.5
		noiseCriticalBands.get(5).setLPFilter(Filter.getLPFilter(679.1050899851506 , 380, alpha)); // 0.0 679.1050899851506 811.5246097057512 7.0
		noiseCriticalBands.get(5).setHPFilter(Filter.getHPFilter(556.5976608219034 , 450, alpha)); // -0.0 556.5976608219034 441.2617959739804 4.5
		noiseCriticalBands.get(6).setLPFilter(Filter.getLPFilter(811.5246097057512 , 346, alpha)); // 0.0 811.5246097057512 956.4137329472474 7.5
		noiseCriticalBands.get(6).setHPFilter(Filter.getHPFilter(679.1050899851506 , 436, alpha)); // -0.0 679.1050899851506 556.5976608219034 5.5
		noiseCriticalBands.get(7).setLPFilter(Filter.getLPFilter(956.4137329472474 , 316, alpha)); // 0.0 956.4137329472474 1117.0600558426302 8.0
		noiseCriticalBands.get(7).setHPFilter(Filter.getHPFilter(811.5246097057512 , 390, alpha)); // -0.0 811.5246097057512 679.1050899851506 6.0
		noiseCriticalBands.get(8).setLPFilter(Filter.getLPFilter(1117.0600558426302 , 290, alpha)); // 0.0 1117.0600558426302 1298.3746618508972 8.5
		noiseCriticalBands.get(8).setHPFilter(Filter.getHPFilter(956.4137329472474 , 354, alpha)); // -0.0 956.4137329472474 811.5246097057512 6.5
		noiseCriticalBands.get(9).setLPFilter(Filter.getLPFilter(1298.3746618508972 , 250, alpha)); // 0.0 1298.3746618508972 1507.0285523470109 8.5
		noiseCriticalBands.get(9).setHPFilter(Filter.getHPFilter(1117.0600558426302 , 324, alpha)); // -0.0 1117.0600558426302 956.4137329472474 7.0
		noiseCriticalBands.get(10).setLPFilter(Filter.getLPFilter(1507.0285523470109 , 214, alpha)); // 0.0 1507.0285523470109 1750.4268228124736 8.5
		noiseCriticalBands.get(10).setHPFilter(Filter.getHPFilter(1298.3746618508972 , 276, alpha)); // -0.0 1298.3746618508972 1117.0600558426302 7.0
		noiseCriticalBands.get(11).setLPFilter(Filter.getLPFilter(1750.4268228124736 , 174, alpha)); // 0.0 1750.4268228124736 2040.1945763077715 8.0
		noiseCriticalBands.get(11).setHPFilter(Filter.getHPFilter(1507.0285523470109 , 256, alpha)); // -0.0 1507.0285523470109 1298.3746618508972 7.5
		noiseCriticalBands.get(12).setLPFilter(Filter.getLPFilter(2040.1945763077715 , 148, alpha)); // 0.0 2040.1945763077715 2391.1535064646487 8.0
		noiseCriticalBands.get(12).setHPFilter(Filter.getHPFilter(1750.4268228124736 , 206, alpha)); // -0.0 1750.4268228124736 1507.0285523470109 7.0
		noiseCriticalBands.get(13).setLPFilter(Filter.getLPFilter(2391.1535064646487 , 118, alpha)); // 0.0 2391.1535064646487 2820.022650957681 7.5
		noiseCriticalBands.get(13).setHPFilter(Filter.getHPFilter(2040.1945763077715 , 176, alpha)); // -0.0 2040.1945763077715 1750.4268228124736 7.0
		noiseCriticalBands.get(14).setLPFilter(Filter.getLPFilter(2820.022650957681 , 100, alpha)); // 0.0 2820.022650957681 3346.624650116489 7.5
		noiseCriticalBands.get(14).setHPFilter(Filter.getHPFilter(2391.1535064646487 , 152, alpha)); // -0.0 2391.1535064646487 2040.1945763077715 7.0
		noiseCriticalBands.get(15).setLPFilter(Filter.getLPFilter(3346.624650116489 , 84, alpha)); // 0.0 3346.624650116489 3988.1142791838934 7.5
		noiseCriticalBands.get(15).setHPFilter(Filter.getHPFilter(2820.022650957681 , 120, alpha)); // -0.0 2820.022650957681 2391.1535064646487 6.5
		noiseCriticalBands.get(16).setLPFilter(Filter.getLPFilter(3988.1142791838934 , 66, alpha)); // 0.0 3988.1142791838934 4752.566291913504 7.0
		noiseCriticalBands.get(16).setHPFilter(Filter.getHPFilter(3346.624650116489 , 102, alpha)); // -0.0 3346.624650116489 2820.022650957681 6.5
		noiseCriticalBands.get(17).setLPFilter(Filter.getLPFilter(4752.566291913504 , 56, alpha)); // 0.0 4752.566291913504 5643.956038616697 7.0
		noiseCriticalBands.get(17).setHPFilter(Filter.getHPFilter(3988.1142791838934 , 80, alpha)); // -0.0 3988.1142791838934 3346.624650116489 6.0
		noiseCriticalBands.get(18).setLPFilter(Filter.getLPFilter(5643.956038616697 , 50, alpha)); // 0.0 5643.956038616697 6688.6115008769275 7.5
		noiseCriticalBands.get(18).setHPFilter(Filter.getHPFilter(4752.566291913504 , 66, alpha)); // -0.0 4752.566291913504 3988.1142791838934 6.0
		noiseCriticalBands.get(19).setLPFilter(Filter.getLPFilter(6688.6115008769275 , 40, alpha)); // 0.0 6688.6115008769275 7959.659690325127 7.0
		noiseCriticalBands.get(19).setHPFilter(Filter.getHPFilter(5643.956038616697 , 60, alpha)); // -0.0 5643.956038616697 4752.566291913504 6.5
		noiseCriticalBands.get(20).setLPFilter(Filter.getLPFilter(7959.659690325127 , 30, alpha)); // 0.0 7959.659690325127 9637.819290033583 6.5
		noiseCriticalBands.get(20).setHPFilter(Filter.getHPFilter(6688.6115008769275 , 48, alpha)); // -0.0 6688.6115008769275 5643.956038616697 6.0
		noiseCriticalBands.get(21).setLPFilter(Filter.getLPFilter(9637.819290033583 , 20, alpha)); // 0.0 9637.819290033583 12173.792289985191 5.5
		noiseCriticalBands.get(21).setHPFilter(Filter.getHPFilter(7959.659690325127 , 40, alpha)); // -0.0 7959.659690325127 6688.6115008769275 6.0
		noiseCriticalBands.get(22).setLPFilter(Filter.getLPFilter(12173.792289985191 , 12, alpha)); // 0.0 12173.792289985191 17061.90405382271 4.5
		noiseCriticalBands.get(22).setHPFilter(Filter.getHPFilter(9637.819290033583 , 30, alpha)); // -0.0 9637.819290033583 7959.659690325127 5.5
		noiseCriticalBands.get(23).setLPFilter(Filter.getLPFilter(17061.90405382271 , 2, alpha)); // 0.0 17061.90405382271 22050.0 1.0
		noiseCriticalBands.get(23).setHPFilter(Filter.getHPFilter(12173.792289985191 , 22, alpha)); // -0.0 12173.792289985191 9637.819290033583 4.5
		noiseCriticalBands.get(24).setHPFilter(Filter.getHPFilter(17061.90405382271 , 12, alpha)); // -0.0 17061.90405382271 12173.792289985191 3.0
		noiseCriticalBands.get(0).setLPFilter(Filter.getLPFilter(123.08328430195176 , 488, alpha)); // 0.0 123.08328430195176 226.04889190440082 2.5
	}
	
	public static void initNoiseCriticalBandsWithHardFilters(ArrayList<Filter.CriticalBand> noiseCriticalBands) {
		noiseCriticalBands.get(23).setHPFilter(Filter.getHPFilter(11517.109285589595 , 104, alpha)); // 0.25894715929867046 12173.792289985191 9637.819290033583 22.5
		noiseCriticalBands.get(23).setLPFilter(Filter.getLPFilter(20149.983477579415 , 20, alpha)); // 0.619089820460111 17061.90405382271 22050.0 10.0
		noiseCriticalBands.get(22).setHPFilter(Filter.getHPFilter(9181.352873966836 , 140, alpha)); // 0.2720041741834615 9637.819290033583 7959.659690325127 25.0
		noiseCriticalBands.get(22).setLPFilter(Filter.getLPFilter(13601.621547163224 , 44, alpha)); // 0.2921024162624884 12173.792289985191 17061.90405382271 16.5
		noiseCriticalBands.get(21).setHPFilter(Filter.getHPFilter(7635.41594296854 , 196, alpha)); // 0.2550994919377141 7959.659690325127 6688.6115008769275 29.5
		noiseCriticalBands.get(21).setLPFilter(Filter.getLPFilter(10329.55895177838 , 96, alpha)); // 0.27277090953176386 9637.819290033583 12173.792289985191 26.5
		noiseCriticalBands.get(20).setHPFilter(Filter.getHPFilter(6416.145020897542 , 242, alpha)); // 0.2608194661519055 6688.6115008769275 5643.956038616697 31.0
		noiseCriticalBands.get(20).setLPFilter(Filter.getLPFilter(8355.387572896308 , 156, alpha)); // 0.23581063603243152 7959.659690325127 9637.819290033583 34.0
		noiseCriticalBands.get(19).setHPFilter(Filter.getHPFilter(5414.044518894153 , 288, alpha)); // 0.2579247972874627 5643.956038616697 4752.566291913504 31.0
		noiseCriticalBands.get(19).setLPFilter(Filter.getLPFilter(7021.147082743639 , 194, alpha)); // 0.26162311124574683 6688.6115008769275 7959.659690325127 35.0
		noiseCriticalBands.get(18).setHPFilter(Filter.getHPFilter(4558.9663185473455 , 344, alpha)); // 0.25325327181084334 4752.566291913504 3988.1142791838934 31.0
		noiseCriticalBands.get(18).setLPFilter(Filter.getLPFilter(5924.554815371107 , 244, alpha)); // 0.2686041349434989 5643.956038616697 6688.6115008769275 37.0
		noiseCriticalBands.get(17).setHPFilter(Filter.getHPFilter(3825.6549317898493 , 402, alpha)); // 0.2532532718108428 3988.1142791838934 3346.624650116489 30.5
		noiseCriticalBands.get(17).setLPFilter(Filter.getLPFilter(4988.848126646225 , 286, alpha)); // 0.2650712952517235 4752.566291913504 5643.956038616697 36.5
		noiseCriticalBands.get(16).setHPFilter(Filter.getHPFilter(3210.296947706215 , 508, alpha)); // 0.25888185504051126 3346.624650116489 2820.022650957681 32.5
		noiseCriticalBands.get(16).setLPFilter(Filter.getLPFilter(4186.390095054718 , 326, alpha)); // 0.2593698656935255 3988.1142791838934 4752.566291913504 35.0
		noiseCriticalBands.get(15).setHPFilter(Filter.getHPFilter(2705.146544748218 , 618, alpha)); // 0.2678581746543193 2820.022650957681 2391.1535064646487 33.5
		noiseCriticalBands.get(15).setLPFilter(Filter.getLPFilter(3513.0077290514905 , 376, alpha)); // 0.25936986569352427 3346.624650116489 3988.1142791838934 34.0
		noiseCriticalBands.get(14).setHPFilter(Filter.getHPFilter(2309.702039930235 , 746, alpha)); // 0.23208261575793257 2391.1535064646487 2040.1945763077715 34.5
		noiseCriticalBands.get(14).setLPFilter(Filter.getLPFilter(2960.224824905229 , 494, alpha)); // 0.26623934996735 2820.022650957681 3346.624650116489 37.5
		noiseCriticalBands.get(13).setHPFilter(Filter.getHPFilter(1970.6980593311098 , 882, alpha)); // 0.23983523403955798 2040.1945763077715 1750.4268228124736 35.0
		noiseCriticalBands.get(13).setLPFilter(Filter.getLPFilter(2492.6956594045864 , 594, alpha)); // 0.23676721499741157 2391.1535064646487 2820.022650957681 38.0
		noiseCriticalBands.get(12).setHPFilter(Filter.getHPFilter(1690.800859279062 , 1024, alpha)); // 0.24497283164496542 1750.4268228124736 1507.0285523470109 35.0
		noiseCriticalBands.get(12).setLPFilter(Filter.getLPFilter(2126.832991254612 , 710, alpha)); // 0.24686197586741382 2040.1945763077715 2391.1535064646487 38.5
		noiseCriticalBands.get(11).setHPFilter(Filter.getHPFilter(1455.6936274389964 , 1206, alpha)); // 0.24602908091460024 1507.0285523470109 1298.3746618508972 35.5
		noiseCriticalBands.get(11).setLPFilter(Filter.getLPFilter(1824.760029639913 , 886, alpha)); // 0.25652684237911805 1750.4268228124736 2040.1945763077715 41.0
		noiseCriticalBands.get(10).setHPFilter(Filter.getHPFilter(1254.1472544372934 , 1382, alpha)); // 0.24392633548555476 1298.3746618508972 1117.0600558426302 35.0
		noiseCriticalBands.get(10).setLPFilter(Filter.getLPFilter(1571.025666431721 , 1058, alpha)); // 0.2629316714630931 1507.0285523470109 1750.4268228124736 42.0
		noiseCriticalBands.get(9).setHPFilter(Filter.getHPFilter(1079.0088895292133 , 1614, alpha)); // 0.23686297717624563 1117.0600558426302 956.4137329472474 35.0
		noiseCriticalBands.get(9).setLPFilter(Filter.getLPFilter(1353.51112972323 , 1244, alpha)); // 0.26424845346154585 1298.3746618508972 1507.0285523470109 42.5
		noiseCriticalBands.get(8).setHPFilter(Filter.getHPFilter(917.4533772462299 , 1766, alpha)); // 0.26889772558068187 956.4137329472474 811.5246097057512 32.5
		noiseCriticalBands.get(8).setLPFilter(Filter.getLPFilter(1164.4968610192134 , 1410, alpha)); // 0.2616270482611884 1117.0600558426302 1298.3746618508972 41.5
		noiseCriticalBands.get(7).setHPFilter(Filter.getHPFilter(778.4664400401664 , 1916, alpha)); // 0.24964725544493835 811.5246097057512 679.1050899851506 29.5
		noiseCriticalBands.get(7).setLPFilter(Filter.getLPFilter(997.0285697957498 , 1520, alpha)); // 0.25282145346676843 956.4137329472474 1117.0600558426302 38.5
		noiseCriticalBands.get(6).setHPFilter(Filter.getHPFilter(646.941313384902 , 2060, alpha)); // 0.26254551923858294 679.1050899851506 556.5976608219034 26.0
		noiseCriticalBands.get(6).setLPFilter(Filter.getLPFilter(845.9866196982005 , 1752, alpha)); // 0.23785091124479477 811.5246097057512 956.4137329472474 38.0
		noiseCriticalBands.get(5).setHPFilter(Filter.getHPFilter(526.5734731701414 , 2200, alpha)); // 0.26031961256241226 556.5976608219034 441.2617959739804 22.0
		noiseCriticalBands.get(5).setLPFilter(Filter.getLPFilter(712.8679428907571 , 1820, alpha)); // 0.25496885184937007 679.1050899851506 811.5246097057512 33.5
		noiseCriticalBands.get(4).setHPFilter(Filter.getHPFilter(414.57549567658657 , 2262, alpha)); // 0.24344831616723947 441.2617959739804 331.6438667250413 17.0
		noiseCriticalBands.get(4).setLPFilter(Filter.getLPFilter(588.3337688230166 , 1948, alpha)); // 0.2590545587143393 556.5976608219034 679.1050899851506 30.0
		noiseCriticalBands.get(3).setHPFilter(Filter.getHPFilter(303.0665956890306 , 2244, alpha)); // 0.2706309754280537 331.6438667250413 226.04889190440082 11.5
		noiseCriticalBands.get(3).setLPFilter(Filter.getLPFilter(472.9326829560167 , 2100, alpha)); // 0.2745970390372172 441.2617959739804 556.5976608219034 26.5
		noiseCriticalBands.get(2).setHPFilter(Filter.getHPFilter(200.30749000378856 , 2330, alpha)); // 0.24999999999999997 226.04889190440082 123.08328430195176 6.5
		noiseCriticalBands.get(2).setLPFilter(Filter.getLPFilter(359.04834903727607 , 2200, alpha)); // 0.2500000000000001 331.6438667250413 441.2617959739804 22.0
		noiseCriticalBands.get(1).setHPFilter(Filter.getHPFilter(97.69576400771382 , 3072, alpha)); // 0.25000000000000006 123.08328430195176 21.533203125 1.5
		noiseCriticalBands.get(1).setLPFilter(Filter.getLPFilter(252.44763560956093 , 2328, alpha)); // 0.25 226.04889190440082 331.6438667250413 17.5
		noiseCriticalBands.get(0).setLPFilter(Filter.getLPFilter(148.82468620256404 , 2342, alpha)); // 0.2500000000000001 123.08328430195176 226.04889190440082 12.0
		noiseCriticalBands.get(24).setHPFilter(Filter.getHPFilter(15839.87611286333 , 52, alpha)); // 0.2499999999999998 17061.90405382271 12173.792289985191 14.0

	}
	
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
