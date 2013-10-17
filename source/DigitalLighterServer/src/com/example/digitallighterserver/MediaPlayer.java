package com.example.digitallighterserver;

public class MediaPlayer {	
	
	private int tilesX;
	private int tilesY;
	
	/**
	 * Constructor
	 * @param tilesX number of tiles x axis
	 * @param tilesY number of tiles y axis
	 * @param dls DeviceTracker class
	 */
	public MediaPlayer(int tilesX, int tilesY, DeviceLocatingStrategy devices, ConnectionService network) {		
		
	}
	
	/**
	 * Gets the next frame from ImageMapper and displays it on phones' screens.
	 * @return Playback finished
	 */
	public boolean playNextFrame() {
		return false;
	}
}
