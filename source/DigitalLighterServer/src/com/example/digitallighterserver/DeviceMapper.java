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

import android.text.style.ForegroundColorSpan;

import com.example.lightdetector.ColorManager;
import com.example.lightdetector.PointCollector;

public class DeviceMapper implements Observer {

	PointCollector collector; // point collector
	DeviceMapperState state; // state of FSM
	static int tilesX = 4;
	static int tilesY = 4;
	static long WAIT_TIME = 1 * 1000; // waiting time between sending a signal
										// and taking a picture in miliseconds
	String RARE_COLOR = ColorManager.KEY_BLUE;
	String SHUT_DOWN_COLOR = ColorManager.KEY_BLACK;
	
	Date startTime;
	ArrayList<Point> falseAlarmDevices; // blobs that are shining with RARE
										// color when non of phone should light

	HashMap<String, ArrayList<Point>> lastDetectedBlobs;
	HashMap<Point, ArrayList<Socket>> devices = new HashMap<Point, ArrayList<Socket>>();
	int oneByOneCounter = 0;

	Boolean detectionDone;

	ConnectionService network;

	// COLORS
	ArrayList<String> screenColors = new ArrayList<String>();

	public DeviceMapper(ConnectionService mConnection) {
		collector = new PointCollector(tilesX, tilesY);
		collector.addObserver(this);
		state = DeviceMapperState.INIT;
		network = mConnection;
	}

	private int getNumberOfClients() {
		// TODO get from network module number of sockets
		return 0;
	}

	public void nextFrame(Mat image) {
		detectionDone = false;
		doFSMStep(image, false);
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
			// broadcast all devices to shine with initial color
			network.broadcastCommandSignal(ColorManager.getHexColor(ColorManager.getColor(SHUT_DOWN_COLOR)) + ":1100");
			startTime = new Date();
			state = DeviceMapperState.DETECT_FALSE_ALARM;
			break;
		case DETECT_FALSE_ALARM:
			// wait few seconds for devices to process the image
			if (new Date().getTime() - startTime.getTime() > WAIT_TIME) {
				// take a picture and find all possible devices
				screenColors.clear();
				screenColors.add(RARE_COLOR);
				detectLights(image);
				state = DeviceMapperState.DETECT_FALSE_ALARM_WAIT_FOR_UPDATE;
			}
			break;
		case DETECT_FALSE_ALARM_WAIT_FOR_UPDATE:
			if (forceNextStep) // collected send update, now we can process
								// blobs
				falseAlarmDevices = lastDetectedBlobs.get(RARE_COLOR);
			lastDetectedBlobs.clear();
			state = DeviceMapperState.ONE_BY_ONE;
			break;
		case ONE_BY_ONE:
			if (oneByOneCounter >= network.getConnectedDevices().size()) {
				state = DeviceMapperState.END;
			} else {
				// iterate through all of devices
				// make it light
				network.unicastCommandSignal(network.getConnectedDevices().get(oneByOneCounter), ColorManager.getHexColor(ColorManager.getColor(RARE_COLOR)) + ":1100");
				startTime = new Date();
				state = DeviceMapperState.DETECT_ONE;
			}
			break;
		case DETECT_ONE:
			if (new Date().getTime() - startTime.getTime() > WAIT_TIME) {
				screenColors.clear();
				screenColors.add(RARE_COLOR);
				detectLights(image);
				state = DeviceMapperState.DETECT_ONE_WAIT_FOR_UPDATE;
			}
			break;
		case DETECT_ONE_WAIT_FOR_UPDATE:
			if (forceNextStep) {
				for (Point p : falseAlarmDevices) {
					lastDetectedBlobs.get(RARE_COLOR).remove(p);
				}

				// randomly choose the tile
				Point tileOfDevice = lastDetectedBlobs.get(RARE_COLOR).get(
						getRandomInt(0, lastDetectedBlobs.size()));
				// add to hash map
				devices.get(tileOfDevice).add(
						network.getConnectedDevices().get(oneByOneCounter));
				oneByOneCounter++;
			}
		case END:
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
}
