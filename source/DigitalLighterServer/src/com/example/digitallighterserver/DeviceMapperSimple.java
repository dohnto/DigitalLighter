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

public class DeviceMapperSimple extends DeviceMapper {
	protected DeviceMapperState state;
	protected String RARE_COLOR = ColorManager.getHexColor(ColorManager.BLUE);

	protected ArrayList<Point> falseAlarmDevices; // blobs that are shining with
													// RARE
	// color when non of phone should light

	protected int oneByOneCounter;
	protected final int RECOVERY_TRIES = 3;
	protected int recoveryCounter = 0;

	public DeviceMapperSimple(ConnectionService mConnection, int tilesX,
			int tilesY, Observer ca) {
		super(mConnection, tilesX, tilesY, ca, null);
	}

	public DeviceMapperSimple(ConnectionService mConnection, int tilesX,
			int tilesY, Observer ca, ArrayList<Socket> sockets, PointCollector collector) {
		super(mConnection, tilesX, tilesY, ca, collector);
		sockets = new ArrayList<Socket>(sockets);
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

	/**
	 * Makes one step in FS machine, returns true if it is in final state.
	 * 
	 * @param image
	 * @param forceNextStep
	 */
	protected boolean doFSMStep(Mat image, Boolean forceNextStep) {
		Boolean retval = false;

		switch (state) {
		case INIT:
			recoveryCounter = 0;
			// broadcast all devices to shine with initial color
			network.multicastCommandSignal(getSockets(),
					CommandCreator.addTime(SHUT_DOWN_COLOR, LIGHT_TIME));
			startT = System.currentTimeMillis();
			state = DeviceMapperState.DETECT_FALSE_ALARM;
			break;
		case DETECT_FALSE_ALARM:
			// wait few seconds for devices to process the image
			if (System.currentTimeMillis() - startT > 0) {
				// take a picture and find all possible devices
				screenColors.clear();
				screenColors.add(RARE_COLOR);
				state = DeviceMapperState.DETECT_FALSE_ALARM_WAIT_FOR_UPDATE;
				detectLights(image, screenColors);
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
			if (oneByOneCounter >= getSockets().size()) {
				state = DeviceMapperState.END;
			} else {
				// iterate through all of devices
				// make it light
				network.unicastCommandSignal(getSockets().get(oneByOneCounter),
						CommandCreator.addTime(RARE_COLOR, LIGHT_TIME));
				startT = System.currentTimeMillis();
				state = DeviceMapperState.DETECT_ONE;
			}
			break;
		case DETECT_ONE:
			if (System.currentTimeMillis() - startT > WAIT_TIME) {
				screenColors.clear();
				screenColors.add(RARE_COLOR);
				state = DeviceMapperState.DETECT_ONE_WAIT_FOR_UPDATE;
				detectLights(image, screenColors);
			}
			break;
		case DETECT_ONE_WAIT_FOR_UPDATE:
			if (forceNextStep) {
				for (Point p : falseAlarmDevices) {
					lastDetectedBlobs.get(RARE_COLOR).remove(p);
				}

				// randomly choose the tile
				if (lastDetectedBlobs.get(RARE_COLOR).size() > 0) { // phone
																	// detected
					Point tileOfDevice = lastDetectedBlobs.get(RARE_COLOR).get(
							getRandomInt(0, lastDetectedBlobs.size() - 1));
					// add to hash map
					devices.get(tileOfDevice).add(
							getSockets().get(oneByOneCounter));
					oneByOneCounter++;
					state = DeviceMapperState.INIT;
				} else if (recoveryCounter < RECOVERY_TRIES) { // not detected
																// but give
																// second chance
					recoveryCounter++;
					state = DeviceMapperState.ONE_BY_ONE;
				} else { // run out of second chances
					// TODO delete phone
					network.unicastCommandSignal(
							getSockets().get(oneByOneCounter),
							CommandCreator.addTime(SHUT_DOWN_COLOR, LIGHT_TIME));
					oneByOneCounter++;
					state = DeviceMapperState.INIT;
				}
			}
			break;
		case END: // set flag
			started = false;
			retval = true;
			break;
		default:
			// this should never happen
			break;
		}

		return retval;
	}

	@Override
	protected void resetState() {
		state = DeviceMapperState.INIT;
		oneByOneCounter = 0;
	}
}
