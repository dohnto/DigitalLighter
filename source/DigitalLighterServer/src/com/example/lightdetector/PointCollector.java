package com.example.lightdetector;

import java.util.ArrayList;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

public class PointCollector {

	public static final int TILE_COUNT_X = 4;
	public static final int TILE_COUNT_Y = 4;
	
	ArrayList<Mat> rgbResources;
	LightDetector mDetector;
	Scalar mBGRColor;
	TileMapper mMapper;


	public PointCollector(ArrayList<Mat> resOut) {
		rgbResources = resOut;
		mDetector = new LightDetector();
		mBGRColor = new Scalar(0.0, 0.0, 255.00, 0.0);
		mMapper = new TileMapper(TILE_COUNT_X,TILE_COUNT_Y);
	}
	

	public void collect() {
		Thread processThread = new Thread(new Runnable() {

			@Override
			public void run() {
				for (Mat mat : rgbResources) {
					ArrayList<Point> points = mDetector.getBlobCoords(mat, mBGRColor);
					Size imgSize = new Size((double)mat.height(), (double) mat.width());
					mMapper.mapList(imgSize, points);
					
					System.out.println("Device is in quadrant:");
				}

			}
		});
	}
}
