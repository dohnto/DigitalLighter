package com.example.digitallighterserver;

public class MediaPlayer {	
	
	private int tilesX;
	private int tilesY;
	private DeviceLocatingStrategy devices;
	private ConnectionService network;
	private ImageMapper imageMapper;
	
	/**
	 * Constructor
	 * @param tilesX number of tiles x axis
	 * @param tilesY number of tiles y axis
	 * @param dls DeviceTracker class
	 */
	public MediaPlayer(int tilesX, int tilesY, DeviceLocatingStrategy devices, 
			ConnectionService network, String media) {		
		this.tilesX = tilesX;
		this.tilesY = tilesY;
		this.devices = devices;
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
				
			}
			
		});
		
		playbackThread.start();		
	}
}
