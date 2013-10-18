package com.example.digitallighterserver;

import org.opencv.core.Mat;

public class ImageMapper {
	public ImageMapper(String pathname) {
		
	}
	
	public Mat getNextFrame() {
		return new Mat();
	}
	
	public boolean isFinished() {
		return true;
	}
	
}
