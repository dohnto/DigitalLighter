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
	private ArrayList<String> RARE_COLORS = new ArrayList<String>();
	private DeviceMapperState state;
	private HashMap<String, ArrayList<Point>> falseAlarmDevices = new HashMap<String, ArrayList<Point>>();
	private TreeListDivider<Socket> divider;
	private ArrayList<ArrayList<Socket>> division;
	private HashMap<Socket, ArrayList<Point>> possiblePositions = new HashMap<Socket, ArrayList<Point>>();
	private DeviceMapperSimple oneByOneDetector;
	private Observer obs;

	public DeviceMapperTree(ConnectionService mConnection, int tilesX, int tilesY, Observer ca) {
		super(mConnection, tilesX, tilesY, ca, null);
		RARE_COLORS.add(ColorManager.getHexColor(ColorManager.BLUE));
		RARE_COLORS.add(ColorManager.getHexColor(ColorManager.RED));
		RARE_COLORS.add(ColorManager.getHexColor(ColorManager.WHITE));
		obs = ca;
	}

	public enum DeviceMapperState {
		INIT, // broadcast all devices to shine with initial color
		DETECT_FALSE_ALARM, // take a picture and find all possible devices
		DETECT_FALSE_ALARM_WAIT_FOR_UPDATE, // waiting until the image is
											// processed
		TREE_INIT, // iterate through all of devices
					// make it light
		DETECT_TREE, // take a picure
		DETECT_TREE_WAIT_FOR_UPDATE, ONE_BY_ONE_INIT, ONE_BY_ONE_DETECT, END;
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
		sockets = new ArrayList<Socket>(getSockets());
		divider = new TreeListDivider<Socket>(sockets, RARE_COLORS.size());
		for (Socket s : sockets) {
			possiblePositions.put(s, fillPositions());
		}
	}

	@Override
	protected boolean doFSMStep(Mat image, Boolean forceNextStep) {
		Boolean retval = false;

		switch (state) {
		case INIT:
			// broadcast all devices to shine with initial color
			network.multicastCommandSignal(sockets, CommandCreator.addTime(SHUT_DOWN_COLOR, LIGHT_TIME));
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
				falseAlarmDevices = new HashMap<String, ArrayList<Point>>(lastDetectedBlobs);
				lastDetectedBlobs.clear();
				state = DeviceMapperState.TREE_INIT;
			}
			break;
		case TREE_INIT: // make all phones shine with appropriate color
			if (divider.isFinished()) { // no need for shining anymore
				state = DeviceMapperState.ONE_BY_ONE_INIT;
				String command = CommandCreator.addTime(SHUT_DOWN_COLOR, LIGHT_TIME);
				network.multicastCommandSignal(sockets, command);
			} else {
				division = divider.getNextDivision();
				for (int i = 0; i < RARE_COLORS.size(); i++) { // make them
																// shine
					String command = CommandCreator.addTime(RARE_COLORS.get(i), LIGHT_TIME);
					network.multicastCommandSignal(division.get(i), command);
				}
				startT = System.currentTimeMillis();
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
						ArrayList<Point> positions = lastDetectedBlobs.get(color);
						for (Point falseAlarm : falseAlarmDevices.get(color)) {
							// remove false alarm devices
							positions.remove(falseAlarm);
						}
						for (Socket receiver : div) {
							// for all mobile phones that should light that
							// color
							// make intersection of possiblePosition
							possiblePositions.get(receiver).retainAll(positions);
						}
					}
				}
				state = DeviceMapperState.TREE_INIT;
			}
			break;
		case ONE_BY_ONE_INIT:
			ArrayList<Socket> toBeDetected = new ArrayList<Socket>();
			for (int i = 0; i < sockets.size(); i++) {
				Socket socket = sockets.get(i);
				ArrayList<Point> positions = possiblePositions.get(socket);
				if (positions != null && positions.size() == 1) {
					// ideal case
					devices.get(positions.get(0)).add(socket);
				} else {
					// cannot detect this device, prepare list for one by one algorithm
					toBeDetected.add(socket);
				}
			}

			if (toBeDetected.size() > 0) {
				oneByOneDetector = new DeviceMapperSimple(network, tilesX, tilesY, obs, toBeDetected,
						collector);
				oneByOneDetector.reset();
				state = DeviceMapperState.ONE_BY_ONE_DETECT;
			} else {
				state = DeviceMapperState.END;
			}
			break;
		case ONE_BY_ONE_DETECT:
			if (oneByOneDetector.nextFrame(image)) {
				HashMap<Point, ArrayList<Socket>> map = oneByOneDetector.getDevices();
				for (Point tile : map.keySet()) {
					devices.get(tile).addAll(map.get(tile));
				}
				state = DeviceMapperState.END;
			}
			break;
		case END:
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
