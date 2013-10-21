package com.example.digitallighterserver;

import java.io.IOException;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import android.content.Context;

public class ImageMapper {

	int frameCounter; // current processed frame
	static Context context;
	String[] framesNames;
	String path;

	public ImageMapper(String path) throws IOException {
		this.path = path;
		context = DLSApplication.getContext();
		framesNames = context.getAssets().list(path);
		reset();
	}

	/**
	 * This function resets all necessary settings. After calling this function,
	 * the media is ready to be replayed.
	 */
	public void reset() {
		frameCounter = 0;
	}

	/**
	 * Returns next frame, be sure (!!!) that isFinished() returns false before
	 * calling this function.
	 */
	public Mat getNextFrame() {
		return Highgui.imread(path + framesNames[frameCounter++]);
	}

	/**
	 * Returns true if no more frames are available
	 */
	public boolean isFinished() {
		return frameCounter >= framesNames.length;
	}

}
