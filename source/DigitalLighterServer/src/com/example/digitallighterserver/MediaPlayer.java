package com.example.digitallighterserver;

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
	private ImageMapper imageMapper;

	static private int frameRate = 1; // images per second
	static private int frameMs = (int) (1 / (double) frameRate * 1000); // frame stays for XX ms

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

		try {
			imageMapper = new ImageMapper(media);
		} catch (IOException e) {
			// TODO Automaticky generovaný zachytávací blok
			e.printStackTrace();
		}
	}

	/**
	 * Gets the frames from ImageMapper and displays them on phones' screens as a video.
	 */
	public void play() {

		Thread playbackThread = new Thread(new Runnable() {

			@Override
			public void run() {

				// playback the whole video frame by frame
				while (!imageMapper.isFinished()) {
					// get current devices' locations
					HashMap<Point, ArrayList<Socket>> devices;
					devices = deviceMapper.getDevices();

					// get new frame to display
					Mat frame = new Mat();
					imageMapper.getNextFrame().copyTo(frame);

					// display each tile one by one
					for (int i = 0; i < (int) frame.size().width; i++) {
						for (int j = 0; j < (int) frame.size().height; j++) {

							// display one color on all devices from one tile
							for (Socket device : devices.get(new Point((double) i, (double) j))) {
								String hexColor = ColorManager.getHexColor(frame.get(i, j));
								network.unicastCommandSignal(device, createCommand(hexColor, frameMs));
								setChanged();
								HashMap<String, ArrayList<Point>> update = new HashMap<String, ArrayList<Point>>();
								ArrayList<Point> list = new ArrayList<Point>();
								notifyObservers(update);
							}
						}
					}
				}
			}
		});

		playbackThread.start();
	}

	static public String createCommand(String prefix, int ms) {
		String retval = new String(prefix);
		retval += ":" + Integer.toString(ms);
		return retval;
	}
}
