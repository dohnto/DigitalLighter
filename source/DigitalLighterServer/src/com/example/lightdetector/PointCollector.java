package com.example.lightdetector;

import java.util.ArrayList;
import java.util.HashMap;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

import com.example.digitallighterserver.Protocol;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class PointCollector {

	private static final String NEW_UPDATE = "";

	// REF TO MAPPER AND BLOB DETECTOR
	LightDetector mDetector;
	TileMapper mMapper;

	// LISTENER THAT CATCH THE UPDATES
	PointCollectorListener listener;
	private Handler mUpdateHandler;
	HashMap<String, ArrayList<Point>> update;

	boolean delivered = true;

	public PointCollector(int titleCountX, int titleCountY, final PointCollectorListener listener) {
		mDetector = new LightDetector();
		this.listener = listener;
		mMapper = new TileMapper(titleCountX, titleCountY);

		// HENDELR GETS MESSAGES FROM BACKGROUND THREADS AND MAKE MODIFICATIONS TO UI

		mUpdateHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.getData().getBoolean(NEW_UPDATE)) {
					listener.onPointCollectorUpdate(update);
					delivered = true;
				}
			}

		};
	}

	public void collect(final Mat img, final ArrayList<String> colors) {

		// DO EVERYTHING IN BG THREAD
		Thread processThread = new Thread(new Runnable() {

			@Override
			public void run() {
				update = new HashMap<String, ArrayList<Point>>();
				delivered = false;

				// FIND ALL DEVICES ON IMG
				for (String color : colors) {
					double[] bgrArray = ColorManager.getInstance().get(color);
					Scalar scalar = new Scalar(bgrArray[0], bgrArray[1], bgrArray[2]);
					ArrayList<Point> points = mDetector.getBlobCoords(img, scalar);
					Size imgSize = new Size((double) img.height(), (double) img.width());
					ArrayList<Point> resultPoints = new ArrayList<Point>();
					for (Point p : mMapper.mapList(imgSize, points)) {
						resultPoints.add(p);
					}
					update.put(color, resultPoints);
				}

				Message msg = new Message();
				Bundle bundle = new Bundle();
				bundle.putBoolean(NEW_UPDATE, true);
				msg.setData(bundle);
				mUpdateHandler.sendMessage(msg);
			}
		});

		// START CREATED THREAD
		processThread.start();
	}

	public void collectOffline(final Mat img, final ArrayList<String> colors) {

		update = new HashMap<String, ArrayList<Point>>();

		// FIND ALL DEVICES ON IMG
		for (String color : colors) {
			double[] bgrArray = ColorManager.getInstance().get(color);
			Scalar scalar = new Scalar(bgrArray[0], bgrArray[1], bgrArray[2]);
			ArrayList<Point> points = mDetector.getBlobCoords(img, scalar);
			Size imgSize = new Size((double) img.height(), (double) img.width());
			ArrayList<Point> resultPoints = new ArrayList<Point>();
			for (Point p : mMapper.mapList(imgSize, points)) {
				resultPoints.add(p);
			}
			update.put(color, resultPoints);
		}
		
		listener.onPointCollectorUpdate(update);

	}
}
