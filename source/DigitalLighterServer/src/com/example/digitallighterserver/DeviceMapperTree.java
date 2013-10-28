package com.example.digitallighterserver;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observer;

import org.opencv.core.Mat;
import org.opencv.core.Point;

import com.example.digitallighterserver.DeviceMapper;
import com.example.lightdetector.ColorManager;

public class DeviceMapperTree extends DeviceMapper {
	protected ArrayList<String> RARE_COLORS = new ArrayList<String>();
	protected DeviceMapperState state;
	protected HashMap<String, ArrayList<Point>> falseAlarmDevices = new HashMap<String, ArrayList<Point>>();
	protected ArrayList<Socket> sockets;
	protected ArrayList<Integer> socketsPointer = new ArrayList<Integer>();
	protected TreeListDivider<Socket> divider;
	private ArrayList<ArrayList<Socket>> division;
	protected HashMap<Socket, ArrayList<Point>> possiblePositions = new HashMap<Socket, ArrayList<Point>>();

	public DeviceMapperTree(ConnectionService mConnection, int tilesX,
			int tilesY, Observer ca) {
		super(mConnection, tilesX, tilesY, ca);
		RARE_COLORS.add(ColorManager.getHexColor(ColorManager.BLUE));
		RARE_COLORS.add(ColorManager.getHexColor(ColorManager.WHITE));
	}

	public enum DeviceMapperState {
		INIT, // broadcast all devices to shine with initial color
		DETECT_FALSE_ALARM, // take a picture and find all possible devices
		DETECT_FALSE_ALARM_WAIT_FOR_UPDATE, // waiting until the image is
											// processed
		TREE_INIT, // iterate through all of devices
					// make it light
		DETECT_TREE, // take a picure
		DETECT_TREE_WAIT_FOR_UPDATE, END;
	}

	protected ArrayList<Point> fillPositions() {
		ArrayList<Point> retval = new ArrayList<Point>();
		for (int x = 0; x < tilesX; x++) {
			for (int y = 0; y < tilesY; y++) {
				retval.add(new Point(x, y));
			}
		}
		return retval;
	}

	@Override
	protected void resetState() {
		state = DeviceMapperState.INIT;
		sockets = new ArrayList<Socket>(network.getConnectedDevices());
		divider = new TreeListDivider<Socket>(sockets, RARE_COLORS.size());
		for (Socket s : possiblePositions.keySet()) {
			possiblePositions.put(s, fillPositions());
		}
	}

	@Override
	protected boolean doFSMStep(Mat image, Boolean forceNextStep) {
		Boolean retval = false;

		switch (state) {
		case INIT:
			// broadcast all devices to shine with initial color
			network.multicastCommandSignal(sockets,
					CommandCreator.addTime(SHUT_DOWN_COLOR, LIGHT_TIME));
			startT = System.currentTimeMillis();
			state = DeviceMapperState.DETECT_FALSE_ALARM;
			break;
		case DETECT_FALSE_ALARM:
			// wait few seconds for devices to process the image
			if (System.currentTimeMillis() - startT > WAIT_TIME) {
				// take a picture and find all possible devices
				state = DeviceMapperState.DETECT_FALSE_ALARM_WAIT_FOR_UPDATE;
				detectLights(image, RARE_COLORS);
			}
			break;
		case DETECT_FALSE_ALARM_WAIT_FOR_UPDATE:
			if (forceNextStep) { // collected send update, now we can process
									// blobs
				falseAlarmDevices = new HashMap<String, ArrayList<Point>>(
						lastDetectedBlobs);
				lastDetectedBlobs.clear();
				state = DeviceMapperState.TREE_INIT;
			}
			break;
		case TREE_INIT: // make all phones shine with appropriate color
			if (divider.isFinished()) { // no need for shining anymore
				state = DeviceMapperState.END;
			} else {
				division = divider.getNextDivision();
				for (int i = 0; i < RARE_COLORS.size(); i++) { // make them
																// shine
					String command = CommandCreator.addTime(RARE_COLORS.get(i),
							LIGHT_TIME);
					network.multicastCommandSignal(division.get(i), command);
				}
				state = DeviceMapperState.DETECT_TREE;
			}
			break;
		case DETECT_TREE:
			if (System.currentTimeMillis() - startT > WAIT_TIME) {
				state = DeviceMapperState.DETECT_TREE_WAIT_FOR_UPDATE;
				detectLights(image, RARE_COLORS);
			}
			break;
		case DETECT_TREE_WAIT_FOR_UPDATE:
			if (forceNextStep) {
				for (int i = 0; i < RARE_COLORS.size(); i++) { // iterate
																// through
																// all colors
					String color = RARE_COLORS.get(i);
					ArrayList<Socket> div = division.get(i);
					if (lastDetectedBlobs.keySet().contains(color)) { // check
						// get positions where color was detected
						ArrayList<Point> positions = lastDetectedBlobs
								.get(color);
						for (Point falseAlarm: falseAlarmDevices.get(color)) {
							// remove false alarm devices
							positions.remove(falseAlarm);
						} 
						for (Socket receiver : div) {
							// for all mobile phones that should light that
							// color
							// make intersection of possiblePosition
							possiblePositions.get(receiver)
									.retainAll(positions);
						}
					}
				}
				state = DeviceMapperState.TREE_INIT;
			}
			break;
		case END:
			for (int i = 0; i < sockets.size(); i++) {
				Socket socket = sockets.get(i);
				ArrayList<Point> positions = possiblePositions.get(socket);
				if (positions.size() < 1) {
					// too bad, no position for this device
				} else if (positions.size() == 1) { // perfect case
					devices.get(positions.get(0)).add(socket);
				} else { // more positions, choose one
					Point tileOfDevice = positions.get(getRandomInt(0,
							positions.size() - 1));
					devices.get(tileOfDevice).add(socket);
				}
			}
			started = false;
			retval = true;
			break;
		default:
			// this should never happen
			break;
		}

		return retval;
	}
}
