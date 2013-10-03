package com.example.lightdetector;

import java.util.ArrayList;
import java.util.HashMap;

import org.opencv.core.Point;

public class PointCollectorUpdate {

	public static final String KEY_RED = "red";
	public static final String KEY_BLUE = "blue";
	public static final String KEY_GREEN = "green";

	HashMap<String, ArrayList<Point>> map;

	public PointCollectorUpdate() {
		map = new HashMap<String, ArrayList<Point>>();
		map.put(KEY_RED, new ArrayList<Point>());
		map.put(KEY_BLUE, new ArrayList<Point>());
		map.put(KEY_GREEN, new ArrayList<Point>());
	}

	public void addRed(Point point) {
		map.get(KEY_RED).add(point);
	}

	public void addBlue(Point point) {
		map.get(KEY_BLUE).add(point);
	}

	public void addGreen(Point point) {
		map.get(KEY_GREEN).add(point);
	}
	
	public ArrayList<Point> getRedDevicePoints(){
		return map.get(KEY_RED);
	}
	
	public ArrayList<Point> getBlueDevicePoints(){
		return map.get(KEY_BLUE);
	}
	
	public ArrayList<Point> getGreenDevicePoints(){
		return map.get(KEY_GREEN);
	}
}
