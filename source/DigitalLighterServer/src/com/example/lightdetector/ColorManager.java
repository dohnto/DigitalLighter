package com.example.lightdetector;

import java.util.HashMap;

import org.opencv.core.Scalar;

public class ColorManager {
	// keys meant to use for hasmap
	public static String KEY_RED = "red";
	public static String KEY_BLUE = "blue";
	public static String KEY_GREEN = "green";
	public static String KEY_WHITE = "white";
	
	private static HashMap<String, double[]> colors = null;
	
	private ColorManager() {
		colors = new HashMap<String, double[]>();
		colors.put(KEY_RED, new double[] {0.0, 0.0, 255.0});
		colors.put(KEY_GREEN, new double[] {0.0, 255.0, 0.0});
		colors.put(KEY_BLUE, new double[] {255.0, 0.0, 0.0});
		colors.put(KEY_WHITE, new double[] {255.0, 255.0, 255.0});
	}
	
	/**
	 * Singleton getter
	 * @return instance of hashmap
	 */
	public static HashMap<String, double[]> getInstance() {
		if (colors == null) {
			new ColorManager();
			return colors;
		}
		return colors;
	}
	
	/**
	 * Returns a color
	 * @param key name of color
	 * @return
	 */
	public static double[] getColor(String key) {
		return getInstance().get(key);
	}
	
	public static Scalar getCvColor(String key) {
		double[] color = getInstance().get(key);
		return new Scalar(color[0], color[1], color[2]);
	}
	
}
