package com.example.lightdetector;

import java.util.HashMap;

import org.opencv.core.Scalar;

public class ColorManager {
	public static double[] BLACK = { 0.0, 0.0, 0.0, 255.0 };
	public static double[] RED   = { 255.0, 0.0, 0.0, 255.0 };
	public static double[] GREEN = { 0.0, 255.0, 0.0, 255.0 };
	public static double[] BLUE  = { 0.0, 0.0, 255.0, 255.0 };
	public static double[] WHITE = { 255.0, 255.0, 255.0, 255.0 };


	public static Scalar getCvColor(double[] color) {
		return new Scalar(color[0], color[1], color[2], color[3]);
	}

	public static String getHexColor(double[] color) {
		String f = "#" + String.format("%02X", (int) color[0]);
		String s = "" + String.format("%02X", (int) color[1]);
		String t = "" + String.format("%02X", (int) color[2]);

		return f + s + t;
	}
	
	public static String getKey(double[] color) {
		return "" + color[0] + "|" + color[1] + "|" + color[2];
	}
	
	public static double[] getColor(String key) {
		double[] color = new double[4];
		
		if (key.contains("|")) { 
			String[] commands = key.split("\\|");
			color[0] = Double.parseDouble(commands[0]);
			color[1] = Double.parseDouble(commands[1]);
			color[2] = Double.parseDouble(commands[2]);
		} else {
			color = BLACK;
		}
		return color;
	}
}
