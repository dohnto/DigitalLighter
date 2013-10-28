package com.example.digitallighterserver;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import org.opencv.core.Mat;
import org.opencv.core.Point;

import android.util.Log;

import com.example.lightdetector.ColorManager;
import com.example.lightdetector.PointCollector;

public abstract class DeviceMapper implements Observer, DeviceLocatingStrategy {
	protected PointCollector collector; // point collector
	protected int tilesX;
	protected int tilesY;
	protected boolean started = false;

	protected static int LIGHT_TIME = 1000;
	protected static int WAIT_TIME = 1000; // waiting time between sending a
											// signal
	// and taking a picture in miliseconds
	protected String SHUT_DOWN_COLOR = ColorManager
			.getHexColor(ColorManager.BLACK);

	protected long startT;

	protected HashMap<String, ArrayList<Point>> lastDetectedBlobs = new HashMap<String, ArrayList<Point>>();
	protected HashMap<Point, ArrayList<Socket>> devices;
	protected ConnectionService network;

	// COLORS
	protected ArrayList<String> screenColors = new ArrayList<String>();

	public DeviceMapper(ConnectionService mConnection, int tilesX, int tilesY,
			Observer ca) {
		this.tilesX = tilesX;
		this.tilesY = tilesY;

		collector = new PointCollector(tilesX, tilesY);
		collector.addObserver(this);
		collector.addObserver(ca);
		network = mConnection;
	}

	public void reset() {
		resetState();
		started = true;

		devices = new HashMap<Point, ArrayList<Socket>>();

		for (int i = 0; i < tilesX; i++)
			for (int j = 0; j < tilesY; j++) {
				ArrayList<Socket> list = new ArrayList<Socket>();
				devices.put(new Point(i, j), list);
			}
		lastDetectedBlobs.clear();
	}

	protected abstract void resetState();

	/**
	 * Process next frame, returns true when precedure is finished.
	 */
	public boolean nextFrame(Mat image) {
		boolean finished = false;
		if (started) {
			finished = doFSMStep(image, false);
		} else {
			screenColors.clear();
			screenColors.add(ColorManager.getHexColor(ColorManager.BLUE));
			screenColors.add(ColorManager.getHexColor(ColorManager.RED));
			screenColors.add(ColorManager.getHexColor(ColorManager.GREEN));
			screenColors.add(ColorManager.getHexColor(ColorManager.ORANGE));
			screenColors.add(ColorManager.getHexColor(ColorManager.WHITE));
			detectLights(image, screenColors);
		}
		return finished;
	}

	@Override
	public void update(Observable obs, Object obj) {
		if (started) {
			lastDetectedBlobs = (HashMap<String, ArrayList<Point>>) obj; // possible
																			// race
																			// condition?
																			// copy
																			// obj?
			doFSMStep(null, true);
		}
	}

	/**
	 * Makes one step in FS machine, returns true if it is in final state.
	 * 
	 * @param image
	 * @param forceNextStep
	 */
	protected abstract boolean doFSMStep(Mat image, Boolean forceNextStep);

	protected void detectLights(Mat image, ArrayList<String> colors) {
		if (collector != null) {
			collector.collect(image, colors);
		}
	}

	public static int getRandomInt(int min, int max) {
		Random r = new Random();
		return min + (int) r.nextInt(max - min + 1);
	}

	@Override
	public final HashMap<Point, ArrayList<Socket>> getDevices() {
		return devices;
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}
}
