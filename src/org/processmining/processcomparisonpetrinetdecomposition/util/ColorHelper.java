package org.processmining.processcomparisonpetrinetdecomposition.util;

import java.awt.Color;

public class ColorHelper {
	final private static double start = 0;
	final private static double end = 255;
	final private static double step = 1/((end-start)/2);
	final private static double input_start = -1;
	final private static double input_end = 1;
	
	
	/**
	 * Maps a hex string to a respective awt Color object.
	 * 
	 * @param hex Hex string of the from "#RRGGBB" (in upper or lowercase)
	 * @return Color object.
	 */
	public static Color hexStringToAWTColor(String hex) {
		int radix = 16;
		int r = Integer.parseInt(hex.substring(1, 3), radix);
		int g = Integer.parseInt(hex.substring(3, 5), radix);
		int b = Integer.parseInt(hex.substring(5, 7), radix);
		return new Color(r,g,b);
	}
	
	/**
	 * Maps an awt Color object to its respective hex string in lower case.
	 * 
	 * @param color
	 * @return lower case hex string of the from "#rrggbb".
	 */
	public static String awtColorToHexString(Color color) {
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		String r_string = Integer.toHexString(r);
		if (r_string.length() == 1) r_string = "0" + r_string;
		String g_string = Integer.toHexString(g);
		if (g_string.length() == 1) g_string = "0" + g_string;
		String b_string = Integer.toHexString(b);
		if (b_string.length() == 1) b_string = "0" + b_string;
		String hex_string = String.format("#%s%s%s", r_string, g_string, b_string);
		return hex_string;
	}
	
	/**
	 * Mimics matplotlib 'bwr' colour map, i.e. a diverging colour map with the colour blue for -1 and red for +1 
	 * in which the lightness (L*) of the colours is reduced the closer the difference value gets to 0.
	 * 
	 * @param difference: double between -1 and 1, where 0 is no difference and -1/1 the maximal difference in the respective direction
	 * @return Color object
	 */
	public static Color bwr(double difference) {
//		double[] thresholds = {0.95, 0.9, 0.85, 0.8};
//		double threshold = thresholds.length;
//		for (int i = 0; i < thresholds.length; i++) {
//			if (Math.abs(difference) >= thresholds[i]) {
//				threshold = i;
//				break;
//			}
//		}
//		double categorical_difference = Math.signum(difference) * (1-(threshold/thresholds.length));
//		double val = ((categorical_difference-input_start)/(input_end-input_start))*(end-start)+start;
		double val = ((difference-input_start)/(input_end-input_start))*(end-start)+start;
		double r = Math.min(step*val,1);
		double g = Math.min(step*val,2-step*val);
		double b = Math.min(2-val*step,1);
		
//		System.out.println(String.format("diff: %s, red: %s, green: %s ,blue: %s",difference, r,g,b));
		return new Color((float)r,(float)g,(float)b);
	}
}
