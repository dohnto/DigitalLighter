package com.example.digitallighterserver;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import org.opencv.core.Mat;
import org.opencv.core.Point;

public interface DeviceLocatingStrategy {
	
	public boolean nextFrame(Mat image);
	public HashMap<Point, ArrayList<Socket>> getDevices();
}
