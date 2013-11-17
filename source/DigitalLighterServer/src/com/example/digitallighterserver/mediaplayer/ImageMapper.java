package com.example.digitallighterserver.mediaplayer;

import java.io.IOException;
import java.io.InputStream;

import org.opencv.core.Mat;

import com.example.digitallighterserver.DLSApplication;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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
	 * This function resets all necessary settings. After calling this function, the media is ready to be
	 * replayed.
	 */
	public void reset() {
		frameCounter = 0;
	}

	/**
	 * Returns next frame, be sure (!!!) that isFinished() returns false before calling this function.
	 */
	public Bitmap getNextFrame() {
		AssetManager assetManager = context.getAssets();

		InputStream istr;
		Bitmap bitmap = null;
		try {
			String p = path + "/" + framesNames[frameCounter++];
			istr = assetManager.open(p);
			bitmap = BitmapFactory.decodeStream(istr);
		} catch (IOException e) {
			bitmap = null;
		}

		if (isFinished())
			reset();

		return bitmap;
	}

	/**
	 * Returns true if no more frames are available
	 */
	public boolean isFinished() {
		return frameCounter >= framesNames.length;
	}

}
