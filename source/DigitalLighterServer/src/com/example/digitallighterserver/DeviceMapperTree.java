package com.example.digitallighterserver;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observer;

import org.opencv.core.Mat;
import org.opencv.core.Point;

import com.example.digitallighterserver.DeviceMapper;
import com.example.digitallighterserver.DeviceMapperSimple.DeviceMapperState;
import com.example.lightdetector.ColorManager;

public class DeviceMapperTree extends DeviceMapper {
	protected ArrayList<String> RARE_COLORS = new ArrayList<String>();
	protected DeviceMapperState state;
	protected HashMap<String, ArrayList<Point>> falseAlarmDevices = new HashMap<String, ArrayList<Point>>();
	protected ArrayList<Socket> sockets;
	protected ArrayList<Integer> socketsPointer = new ArrayList<Integer>();

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
		DETECT_ONE_WAIT_FOR_UPDATE, END;
	}

	@Override
	protected void resetState() {
		state = DeviceMapperState.INIT;
		sockets = new ArrayList<Socket>(network.getConnectedDevices());
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
		case TREE_INIT:
			socketsPointer = getDivision(sockets, RARE_COLORS.size());
			int counter = 0;
			for (int i = 0; i < socketsPointer.size(); i++) {
				String color = RARE_COLORS.get(i);
				String command = CommandCreator.addTime(color, LIGHT_TIME);
				for (int j = counter; j < socketsPointer.get(i); j++) {
					network.unicastCommandSignal(sockets.get(j), command);
				}
				counter = socketsPointer.get(i);
				state = DeviceMapperState.DETECT_TREE;
			}
			break;
		case DETECT_TREE:
			if (System.currentTimeMillis() - startT > WAIT_TIME) {
				state = DeviceMapperState.DETECT_ONE_WAIT_FOR_UPDATE;
				detectLights(image, RARE_COLORS);
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

	private static ArrayList<Integer> getDivision(ArrayList<Socket> sockets,
			int parts) {
		ArrayList<Integer> division = new ArrayList<Integer>();
		int df = sockets.size() / (parts);

		for (int i = 0; i < parts; i++) {
			division.add((i + 1) * df);
		}
		return division;
	}

}
