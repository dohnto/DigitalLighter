/**
  * Digital Ligter
  * Customer Driven Project - NTNU
  * 20th November  2013
  *
  * @author Jan Bednarik
  * @author Tomas Dohnalek
  * @author Milos Jovac
  * @author Agnethe Soraa
  */

package com.silentducks.digitallighterserver.devicelocation;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import org.opencv.core.Mat;
import org.opencv.core.Point;


public class DeviceTracker implements DeviceLocatingStrategy {

	HashMap<Point, ArrayList<Socket>> devices;
	
	public DeviceTracker(int tilesX, int tilesY,
			HashMap<Point, ArrayList<Socket>> devices) {
		this.devices = devices;
	}

	@Override
	public boolean nextFrame(Mat image) {
		return false;
	}

	@Override
	public HashMap<Point, ArrayList<Socket>> getDevices() {
		return devices;
	}

}
