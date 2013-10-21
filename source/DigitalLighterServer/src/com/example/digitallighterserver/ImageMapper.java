package com.example.digitallighterserver;

import java.io.IOException;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

public class ImageMapper {

	int frameCounter; // current processed frame
	static Context context;
	String[] framesNames;
	String path;

	public ImageMapper(String path) throws IOException {
		this.path = path;
		context = DLSApplication.getContext();
		framesNames = context.getAssets().list(path);
		context.getAssets().open(path).
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
		Bitmap b = BitmapFactory.decodeFile("file:///android_asset/image/Malay/bullet.jpg");
		b.getPixel(x, y)
		return Highgui.imread(path + framesNames[frameCounter++]);
	}

	/**
	 * Returns true if no more frames are available
	 */
	public boolean isFinished() {
		return frameCounter >= framesNames.length;
	}

}
