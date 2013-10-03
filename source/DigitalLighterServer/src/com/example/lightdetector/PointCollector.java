package com.example.lightdetector;

import java.util.ArrayList;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

public class PointCollector {

	// COLORS TO SEARCH FOR
	ArrayList<Scalar> mBGRColors;

	// REF TO MAPPER AND BLOB DETECTOR
	LightDetector mDetector;
	TileMapper mMapper;

	public PointCollector(int titleCountX, int titleCountY) {
		mDetector = new LightDetector();
		mBGRColors = new ArrayList<Scalar>();
		mBGRColors.add(new Scalar(0.0, 255.0, 0.00, 0.0)); // green
		mBGRColors.add(new Scalar(255.0, 0.0, 0.00, 0.0)); // blue
		mBGRColors.add(new Scalar(0.0, 0.0, 255.00, 0.0)); // red
		mMapper = new TileMapper(titleCountX, titleCountY);
	}

	public void collect(final Mat img) {

		// DO EVERYTHING IN BG THREAD
		Thread processThread = new Thread(new Runnable() {

			@Override
			public void run() {

				// FIND ALL DEVICES ON IMG
				for (Scalar color : mBGRColors) {
					ArrayList<Point> points = mDetector.getBlobCoords(img, color);
					Size imgSize = new Size((double) img.height(), (double) img.width());

					// GET SCREEN POSITION FOR EVERY DEVICE

					System.out.println("\nCOLOR: " + color.toString());
					System.out.println("=====================================");
					for (Point p : mMapper.mapList(imgSize, points))
						System.out.println("Device in quadrant(" + p.x + "," + p.y + ")");
				}
			}
		});

		// START CREATED THREAD
		processThread.start();
	}
}
