package com.example.digitallighterserver;

import java.io.IOException;
import java.util.ArrayList;

import com.example.lightdetector.ColorManager;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * This class creates a bunch of commands from given media.
 * 
 * @author Tomas
 * 
 */
public class CommandCreator {
	private ImageMapper imageMapper;
	private ArrayList<Bitmap> buffer;
	private boolean valid = true;
	private int frameRate; // images per second
	private int frameMs; // frame stays for frameMs milliseconds
	private String ERROR_COMMAND = "#FFFFFF:1000";

	/**
	 * 
	 * @param mediaPath
	 *            relative path (to assets folder) to media to be played from
	 * @param frameRate
	 */
	public CommandCreator(String mediaPath, int frameRate) {
		try {
			imageMapper = new ImageMapper(mediaPath);
			buffer = new ArrayList<Bitmap>();
			this.frameRate = frameRate;
			frameMs = (int) (1 / (double) frameRate * 1000);
		} catch (IOException e) {
			valid = false;
			e.printStackTrace();
		}
	}

	/**
	 * This command must be invoke every time there is need for getting new
	 * commands
	 * 
	 * @param advanceFrames
	 *            how many frames in advances should be processed into one
	 *            command
	 * @return milliseconds of how long this command will be played
	 */
	public int nextCommand(int advanceFrames) {
		int framesCounter = 0;
		buffer.clear();
		while (!imageMapper.isFinished() && framesCounter < advanceFrames) {
			// do until there are images left and we have load insufficient
			// number of frames
			buffer.add(imageMapper.getNextFrame());
			framesCounter++;
		}
		return buffer.size() * frameMs;
	}

	public String getCommand(int tileX, int tileY) {
		String retval = new String();
		if (buffer.size() == 0) {
			retval = ERROR_COMMAND;
		} else if (tileX >= buffer.get(0).getWidth()
				|| tileY >= buffer.get(0).getHeight()) {
			retval = ERROR_COMMAND;
		} else { // ok
			int frameMsCurrent = frameMs;
			for (int i = 0; i < buffer.size(); i++) {
				String color = ColorManager.getHexColor(buffer.get(i).getPixel(
						tileX, tileY));
				if (i + 1 < buffer.size() // there is still something to load
						&& ColorManager.getHexColor(buffer.get(i + 1).getPixel(
								tileX, tileY)).equals(color)) { // and it is the same
															// color!!!
					frameMsCurrent += frameMs;
				} else { // flush the command
					if (retval.length() != 0) { // not first bunch command so add deliminator
						retval += "|";
					}
					retval += color;
					retval = addTime(retval, frameMsCurrent);
					frameMsCurrent = frameMs; // reset
				}
			}
		}
		return retval;
	}

	public boolean isFinished() {
		return (valid) ? imageMapper.isFinished() : true;
	}

	static public String addTime(String prefix, int ms) {
		String retval = new String(prefix);
		retval += ":" + Integer.toString(ms);
		return retval;
	}
}
