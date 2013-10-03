package com.example.lightdetector;

import java.util.ArrayList;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

public class PointCollector {

	public static final int NUM_OF_ROWS = 2;
	public static final int NUM_OF_COLS = 2;
	
	ArrayList<Mat> rgbResources;
	LightDetector mDetector;
	Scalar mBGRColor;
	//TileMapper mMapper;


	public PointCollector(ArrayList<Mat> resOut) {
		rgbResources = resOut;
		mDetector = new LightDetector();
		mBGRColor = new Scalar(0.0, 0.0, 255.00, 0.0);
		//mMapper = new Mapper(NUM_OF_ROWS,NUM_OF_COLS);
	}

	public void collect() {
		Thread processThread = new Thread(new Runnable() {

			@Override
			public void run() {
				for (Mat mat : rgbResources) {
					ArrayList<Point> points = mDetector.getBlobCoords(mat, mBGRColor);
					//mMapper.map()
					
					System.out.println("Device is in quadrant:");
				}

			}
		});
	}
}
