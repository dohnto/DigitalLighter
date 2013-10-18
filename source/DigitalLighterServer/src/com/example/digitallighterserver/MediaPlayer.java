package com.example.digitallighterserver;

import com.example.lightdetector.ColorManager;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import org.opencv.core.Mat;
import org.opencv.core.Point;

public class MediaPlayer {	
	
	private int tilesX;
	private int tilesY;
	private DeviceLocatingStrategy deviceMapper;
	private ConnectionService network;
	private ImageMapper imageMapper;
	
	/**
	 * Constructor
	 * @param tilesX number of tiles x axis
	 * @param tilesY number of tiles y axis
	 * @param dls DeviceTracker class
	 */
	public MediaPlayer(int tilesX, int tilesY, DeviceLocatingStrategy deviceMapper, 
			ConnectionService network, String media) {		
		this.tilesX = tilesX;
		this.tilesY = tilesY;
		this.deviceMapper = deviceMapper;
		this.network = network;
		
		imageMapper = new ImageMapper(media);
	}
	
	/**
	 * Gets the frames from ImageMapper and displays them on phones' screens as a video. 
	 */
	public void play() {
		
		Thread playbackThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				// playback the whole video frame by frame
				while(!imageMapper.isFinished()) {					
					// get current devices' locations
					HashMap<Point, ArrayList<Socket>> devices;
					devices = deviceMapper.getDevices();
					
					// get new frame to display
					Mat frame = new Mat();										
					imageMapper.getNextFrame().copyTo(frame);				
					
					// display each tile one by one
					for(int i = 0; i < (int) frame.size().width; i++) {
						for(int j = 0; j < (int) frame.size().height; j++) {							
							
							// display one color on all devices from one tile
							for(Socket device: devices.get(new Point((double) i, (double) j))) {
								network.unicastCommandSignal(device, ColorManager.getHexColor(frame.get((int) i, (int) j)));
							}
						}						
					}
				}
			}			
		});
		
		playbackThread.start();		
	}
}
