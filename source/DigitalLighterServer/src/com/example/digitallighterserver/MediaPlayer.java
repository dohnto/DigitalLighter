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

	static private int frameRate = 1; // images per second
	
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
	public MediaPlayer(int tilesX, int tilesY, DeviceLocatingStrategy deviceMapper,
			ConnectionService network, String media) {
		this.tilesX = tilesX;
		this.tilesY = tilesY;
		this.deviceMapper = deviceMapper;
		this.network = network;

		commandCreator = new CommandCreator(media, frameRate);
	}

	/**
	 * Gets the frames from ImageMapper and displays them on phones' screens as a video.
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
					int waitTime = commandCreator.nextCommand(5);
					waitTime = (waitTime > 100) ? waitTime - 100 : 0;

					// display each tile one by one
					for (int i = 0; i < tilesX; i++) {
						for (int j = 0; j < tilesY; j++) {
							String command = commandCreator.getCommand(i, j);
							//int color = frame.getPixel(i, j);
							//double[] colorArray = { Color.red(color), Color.green(color), Color.blue(color) };

							//String hexColor = ColorManager.getHexColor(colorArray);
							/*list = update.get(hexColor);
							if (list == null) {
								list = new ArrayList<Point>();
								update.put(hexColor, list);
							}*/
							/*boolean pointAdded = false;

							if (!pointAdded) {
								setChanged();
								list.add(currentPoint);
								pointAdded = true;
							}*/

							Point currentPoint = new Point(i, j);
							// display one color on all devices from one tile
							network.multicastCommandSignal(devices.get(currentPoint), command);
							Log.i("PES", "TILE " + i + ", " + j + ": " + command);
						}
						
					}
					notifyObservers(update);
					
					try {
						Thread.sleep(waitTime);
					} catch (InterruptedException e) {
						// TODO Automaticky generovaný zachytávací blok
						e.printStackTrace();
					}
				}
			}
		});

		playbackThread.start();
	}

	
}
