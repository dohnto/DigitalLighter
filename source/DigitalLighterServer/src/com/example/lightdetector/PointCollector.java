package com.example.lightdetector;

import java.util.ArrayList;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

public class PointCollector {

	// DIMENSIONS OF THE SCREEN
	public static final int TILE_COUNT_X = 4;
	public static final int TILE_COUNT_Y = 4;

	// ALL IMAGES THAT NEED TO BE MAPPED
	ArrayList<Mat> rgbResources;

	// COLOR TO SEARCH FOR
	Scalar mBGRColor;

	// REF TO MAPPER AND BLOB DETECTOR
	LightDetector mDetector;
	TileMapper mMapper;

	public PointCollector(ArrayList<Mat> resOut) {
		rgbResources = resOut;
		mDetector = new LightDetector();
		mBGRColor = new Scalar(0.0, 0.0, 255.00, 0.0);
		mMapper = new TileMapper(TILE_COUNT_X, TILE_COUNT_Y);
	}

	public void collect() {

		// DO EVERYTHING IN BG THREAD
		Thread processThread = new Thread(new Runnable() {

			@Override
			public void run() {

				// FIND ALL DEVICES FOR ON IMG
				for (Mat mat : rgbResources) {
					ArrayList<Point> points = mDetector.getBlobCoords(mat, mBGRColor);
					Size imgSize = new Size((double) mat.height(), (double) mat.width());

					// GET SCREEN POSITION FOR EVERY DEVICE
					for (Point p : mMapper.mapList(imgSize, points))
						System.out.println("Device in quadrant(" + p.x + "," + p.y + ")");
				}
			}
		});

		// STARTING CREATED THREAD
		processThread.start();
	}
}
