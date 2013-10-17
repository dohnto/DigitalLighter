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
import com.example.lightdetector.ColorManager;
import com.example.lightdetector.PointCollector;

public class DeviceMapper implements Observer, DeviceLocatingStrategy {

	PointCollector collector; // point collector
	DeviceMapperState state; // state of FSM
	static int tilesX = 2;
	static int tilesY = 2;
	static long WAIT_TIME = 200; // waiting time between sending a signal
										// and taking a picture in miliseconds
	String RARE_COLOR = ColorManager.KEY_BLUE;
	double[] SHUT_DOWN_COLOR = ColorManager.getColor(ColorManager.KEY_BLACK);

	long startT;
	ArrayList<Point> falseAlarmDevices; // blobs that are shining with RARE
										// color when non of phone should light

	HashMap<String, ArrayList<Point>> lastDetectedBlobs = new HashMap<String, ArrayList<Point>>();
	HashMap<Point, ArrayList<Socket>> devices;
	int oneByOneCounter = 0;
	final int RECOVERY_TRIES = 3;
	int recoveryCounter = 0;

	Boolean detectionDone;

	ConnectionService network;

	// COLORS
	ArrayList<String> screenColors = new ArrayList<String>();

	public DeviceMapper(ConnectionService mConnection, Observer ca) {
		devices = new HashMap<Point, ArrayList<Socket>>();

		for (int i = 0; i < tilesX; i++)
			for (int j = 0; j < tilesY; j++) {
				ArrayList<Socket> list = new ArrayList<Socket>();
				devices.put(new Point(i, j), list);
			}

		collector = new PointCollector(tilesX, tilesY);
		collector.addObserver(this);
		collector.addObserver(ca);
		state = DeviceMapperState.INIT;
		network = mConnection;
	}

	public Boolean nextFrame(Mat image) {
		detectionDone = false;
		doFSMStep(image, false);
		return false;
	}

	@Override
	public void update(Observable obs, Object obj) {
		lastDetectedBlobs = (HashMap<String, ArrayList<Point>>) obj;
		doFSMStep(null, true);
	}

	public enum DeviceMapperState {
		INIT, // broadcast all devices to shine with initial color
		DETECT_FALSE_ALARM, // take a picture and find all possible devices
		DETECT_FALSE_ALARM_WAIT_FOR_UPDATE, // waiting until the image is
											// processed
		ONE_BY_ONE, // iterate through all of devices
					// make it light
		DETECT_ONE, // take a picure
		DETECT_ONE_WAIT_FOR_UPDATE, END;
	}

	private void doFSMStep(Mat image, Boolean forceNextStep) {

		switch (state) {
		case INIT:
			recoveryCounter = 0;
			// broadcast all devices to shine with initial color
			network.broadcastCommandSignal(ColorManager
					.getHexColor(SHUT_DOWN_COLOR) + ":1000");
			startT = System.currentTimeMillis();
			state = DeviceMapperState.DETECT_FALSE_ALARM;
			break;
		case DETECT_FALSE_ALARM:
			// wait few seconds for devices to process the image
			if (System.currentTimeMillis() - startT > WAIT_TIME) {
				// take a picture and find all possible devices
				screenColors.clear();
				screenColors.add(RARE_COLOR);
				state = DeviceMapperState.DETECT_FALSE_ALARM_WAIT_FOR_UPDATE;
				detectLights(image);
			}
			break;
		case DETECT_FALSE_ALARM_WAIT_FOR_UPDATE:
			if (forceNextStep) { // collected send update, now we can process
									// blobs
				falseAlarmDevices = lastDetectedBlobs.get(RARE_COLOR);
				lastDetectedBlobs.clear();
				state = DeviceMapperState.ONE_BY_ONE;
			}
			break;
		case ONE_BY_ONE:
			if (oneByOneCounter >= network.getConnectedDevices().size()) {
				state = DeviceMapperState.END;
			} else {
				// iterate through all of devices
				// make it light
				network.unicastCommandSignal(
						network.getConnectedDevices().get(oneByOneCounter),
						ColorManager.getHexColor(ColorManager
								.getColor(RARE_COLOR)) + ":1000");
				startT = System.currentTimeMillis();
				state = DeviceMapperState.DETECT_ONE;
			}
			break;
		case DETECT_ONE:
			if (System.currentTimeMillis() - startT > WAIT_TIME) {
				screenColors.clear();
				screenColors.add(RARE_COLOR);
				state = DeviceMapperState.DETECT_ONE_WAIT_FOR_UPDATE;
				detectLights(image);
			}
			break;
		case DETECT_ONE_WAIT_FOR_UPDATE:
			if (forceNextStep) {
				for (Point p : falseAlarmDevices) {
					lastDetectedBlobs.get(RARE_COLOR).remove(p);
				}

				// randomly choose the tile
				if (lastDetectedBlobs.get(RARE_COLOR).size() > 0) { // phone detected
					Point tileOfDevice = lastDetectedBlobs.get(RARE_COLOR).get(
							getRandomInt(0, lastDetectedBlobs.size() - 1));
					// add to hash map
					devices.get(tileOfDevice).add(
							network.getConnectedDevices().get(oneByOneCounter));
					oneByOneCounter++;
					state = DeviceMapperState.INIT;
				} else if (recoveryCounter < RECOVERY_TRIES) { // not detected but give second chance
					recoveryCounter++;
					state = DeviceMapperState.ONE_BY_ONE;					
				} else { // run out of second chances
					// TODO delete phone
					oneByOneCounter++;
					state = DeviceMapperState.INIT;
				}
			}
			break;
		case END:
			network.unicastCommandSignal(devices.get(new Point(0, 0))
						.get(0), "#ff0000:5000");
			break;
		default:
			// this should never happen
			break;
		}
	}

	private void detectLights(Mat image) {
		if (!detectionDone) {
			collector.collect(image, screenColors);
			detectionDone = true;
		}
	}

	public static int getRandomInt(int min, int max) {
		Random r = new Random();
		return min + (int) r.nextInt(max - min + 1);
	}

	@Override
	public HashMap<Point, ArrayList<Socket>> getDevices() {
		return devices;
	}
}
