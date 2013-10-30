package com.example.digitallighterserver;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import com.example.lightdetector.ColorManager;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;

import org.opencv.core.Mat;
import org.opencv.core.Point;

public class MediaPlayer extends Observable {

	private int tilesX;
	private int tilesY;
	private DeviceLocatingStrategy deviceMapper;
	private ConnectionService network;
	private CommandCreator commandCreator;

	static private int NEXT_FRAMES = 25;
	static private int FRAME_RATE = 25; // images per second
	static private int SEND_COMMAND_BEFORE = 100; // number of millisecond when
													// should command be send

	/**
	 * Constructor
	 * 
	 * @param tilesX
	 *            number of tiles x axis
	 * @param tilesY
	 *            number of tiles y axis
	 * @param dls
	 *            DeviceTracker class
	 */
	public MediaPlayer(int tilesX, int tilesY,
			DeviceLocatingStrategy deviceMapper, ConnectionService network,
			String media) {
		this.tilesX = tilesX;
		this.tilesY = tilesY;
		this.deviceMapper = deviceMapper;
		this.network = network;

		commandCreator = new CommandCreator(media, FRAME_RATE);
	}

	/**
	 * Gets the frames from ImageMapper and displays them on phones' screens as
	 * a video.
	 */
	public void play() {

		Thread playbackThread = new Thread(new Runnable() {

			@Override
			public void run() {

				// playback the whole video frame by frame
				while (!commandCreator.isFinished()) {
					HashMap<String, ArrayList<Point>> update = new HashMap<String, ArrayList<Point>>();
					ArrayList<Point> list = new ArrayList<Point>();
					// get current devices' locations
					HashMap<Point, ArrayList<Socket>> devices;
					devices = deviceMapper.getDevices();

					// get new frame to display
					int waitTime = commandCreator.nextCommand(NEXT_FRAMES);
					waitTime = (waitTime > SEND_COMMAND_BEFORE) ? waitTime
							- SEND_COMMAND_BEFORE : 0;

					// display each tile one by one
					for (int i = 0; i < tilesX; i++) {
						for (int j = 0; j < tilesY; j++) {
							String command = commandCreator.getCommand(i, j);
							Point currentPoint = new Point(i, j);
							// display one color on all devices from one tile
							network.multicastCommandSignal(
									devices.get(currentPoint), command);
						}
					}
					notifyObservers(update);

					try {
						Thread.sleep(waitTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});

		playbackThread.start();
	}

}
