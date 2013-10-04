package com.example.lightdetector;

import java.util.ArrayList;
import java.util.HashMap;

import org.opencv.core.Point;

public interface PointCollectorListener {

	public void onPointCollectorUpdate(HashMap<String, ArrayList<Point>> update);

}
