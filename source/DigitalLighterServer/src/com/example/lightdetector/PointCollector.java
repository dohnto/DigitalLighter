package com.example.lightdetector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

import com.example.digitallighterserver.DeviceMapper;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

public class PointCollector extends Observable {
	private static final String NEW_UPDATE = "";
	TileMapper mMapper;

	BlockingQueue<HashMap<String, ArrayList<Point>>> buffer = new LinkedBlockingQueue<HashMap<String, ArrayList<Point>>>();

	// LISTENER THAT CATCH THE UPDATES
	private Handler mUpdateHandler;

	boolean delivered = true;

	public PointCollector(int titleCountX, int titleCountY) {

		mMapper = new TileMapper(titleCountX, titleCountY);

		// HENDELR GETS MESSAGES FROM BACKGROUND THREADS AND MAKE MODIFICATIONS TO UI

		mUpdateHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.getData().getBoolean(NEW_UPDATE)) {
					if (buffer.size() > 0) {
						setChanged();
						notifyObservers(buffer.poll());

					}
				}
			}

		};
	}

	public void collect(final Mat input, final ArrayList<String> colors) {

		Mat imgTemp = new Mat();
		input.copyTo(imgTemp);
		final Mat img = imgTemp;

		// DO EVERYTHING IN BG THREAD
		Thread processThread = new Thread(new Runnable() {

			@Override
			public void run() {
				HashMap<String, ArrayList<Point>> update = new HashMap<String, ArrayList<Point>>();
				// REF TO MAPPER AND BLOB DETECTOR
				LightDetector mDetector = new LightDetector();

				// FIND ALL DEVICES ON IMG
				for (String color : colors) {
					double[] bgrArray = ColorManager.getInstance().get(color);
					Scalar scalar = new Scalar(bgrArray[0], bgrArray[1], bgrArray[2]);
					ArrayList<Point> points = mDetector.getBlobCoords(img, scalar);
					Size imgSize = new Size((double) img.width(), (double) img.height());
					ArrayList<Point> resultPoints = new ArrayList<Point>();
					for (Point p : mMapper.mapList(imgSize, points)) {
						resultPoints.add(p);
					}
					update.put(color, resultPoints);
				}

				buffer.add(update);

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

	/*
	 * public void collectOffline(final Mat img, final ArrayList<String> colors) { /* update = new
	 * HashMap<String, ArrayList<Point>>();
	 * 
	 * // FIND ALL DEVICES ON IMG for (String color : colors) { double[] bgrArray =
	 * ColorManager.getInstance().get(color); Scalar scalar = new Scalar(bgrArray[0], bgrArray[1],
	 * bgrArray[2]); ArrayList<Point> points = mDetector.getBlobCoords(img, scalar); Size imgSize = new
	 * Size((double) img.width(), (double) img.height()); ArrayList<Point> resultPoints = new
	 * ArrayList<Point>(); for (Point p : mMapper.mapList(imgSize, points)) { resultPoints.add(p); }
	 * update.put(color, resultPoints); }
	 * 
	 * listener.onPointCollectorUpdate(update);
	 * 
	 * 
	 * }
	 */

	class myAsync extends AsyncTask<String, String, HashMap<String, ArrayList<Point>>> {

		public myAsync() {

		}

		@Override
		protected HashMap<String, ArrayList<Point>> doInBackground(String... params) {
			return null;
		}

		@Override
		protected void onPostExecute(HashMap<String, ArrayList<Point>> result) {

		}

	}
}
