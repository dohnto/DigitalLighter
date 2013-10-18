package com.example.digitallighterserver;

import java.io.IOException;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import android.content.Context;

public class ImageMapper {

	int frameCounter; // current processed frame
	static Context context;
	String [] framesNames;
	String path;

	public ImageMapper(String path) throws IOException {
		this.path = path;
		context = DLSApplication.getContext();
		framesNames = context.getAssets().list(path);
		reset();
	}
	
	public void reset() {
		frameCounter = 0;
	}

	public Mat getNextFrame() {
		return Highgui.imread(path + framesNames[frameCounter++]);
	}

	public boolean isFinished() {
		return frameCounter >= framesNames.length;
	}

}
