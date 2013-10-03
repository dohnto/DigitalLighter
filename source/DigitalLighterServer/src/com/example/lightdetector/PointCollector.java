package com.example.lightdetector;

import java.util.ArrayList;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

import android.os.Handler;
import android.os.Message;

public class PointCollector {

	// COLORS TO SEARCH FOR
	ArrayList<Scalar> mBGRColors;

	// REF TO MAPPER AND BLOB DETECTOR
	LightDetector mDetector;
	TileMapper mMapper;

	// LISTENER THAT CATCH THE UPDATES
	PointCollectorListener listener;
	private Handler mUpdateHandler;
	PointCollectorUpdate update;

	public PointCollector(int titleCountX, int titleCountY, PointCollectorListener listener) {
		mDetector = new LightDetector();
		mBGRColors = new ArrayList<Scalar>();
		this.listener = listener;
		mBGRColors.add(new Scalar(0.0, 255.0, 0.00, 0.0)); // green
		mBGRColors.add(new Scalar(255.0, 0.0, 0.00, 0.0)); // blue
		mBGRColors.add(new Scalar(0.0, 0.0, 255.00, 0.0)); // red
		mMapper = new TileMapper(titleCountX, titleCountY);

		// HENDELR GETS MESSAGES FROM BACKGROUND THREADS AND MAKE MODIFICATIONS TO UI

		mUpdateHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {

			}

		};
	}

	public void collect(final Mat img) {

		// DO EVERYTHING IN BG THREAD
		Thread processThread = new Thread(new Runnable() {

			@Override
			public void run() {

				update = new PointCollectorUpdate();

				// FIND ALL DEVICES ON IMG
				for (Scalar color : mBGRColors) {
					ArrayList<Point> points = mDetector.getBlobCoords(img, color);
					Size imgSize = new Size((double) img.height(), (double) img.width());

					for (Point p : mMapper.mapList(imgSize, points)) {

					}
				}
			}
		});

		// START CREATED THREAD
		processThread.start();
	}
}
