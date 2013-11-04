package com.example.digitallighterserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.lightdetector.ColorManager;

public class Configuration {
	// media to be played in assets folder
	static public String MEDIA_SOURCE = "5x4";
	// number of columns
	static public int TILES_X = 4; 
	// number of rows
	static public int TILES_Y = 5; 

	// how long should the screen light in ms
	static public int LIGHT_TIME = 100; 
	// waiting time between sending a signal and taking a picture (ms)
	static public int WAIT_TIME = 1000; 

	static public String SHUT_DOWN_COLOR = ColorManager
			.getHexColor(ColorManager.BLACK);

	// color for simple detection algorithm
	static public String RARE_COLOR_SIMPLE = ColorManager
			.getHexColor(ColorManager.BLUE);

	// recovery tries for detection
	static public int RECOVERY_TRIES_SIMPLE = 3; 

	// rare colors for tree algorithm
	static public List<String> RARE_COLORS_TREE = Arrays.asList(
			ColorManager.getHexColor(ColorManager.WHITE),
			ColorManager.getHexColor(ColorManager.BLUE));
	
	// how many frames in advance should be processed
	static public int NEXT_FRAMES = 25;
	// images per second
	static public int FRAME_RATE = 25; 
	// number of millisecond when
	static public int SEND_COMMAND_BEFORE = 100; 
	
	static public boolean USE_TREE_DETECTION = true;
}
