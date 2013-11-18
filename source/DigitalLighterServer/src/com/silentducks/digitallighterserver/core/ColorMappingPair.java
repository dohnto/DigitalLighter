package com.silentducks.digitallighterserver.core;

public class ColorMappingPair {
	public String command;
	public String detection;
	
	public ColorMappingPair(String color) {
		this.command = color;
		this.detection = color;
	}
	
	public ColorMappingPair(String command, String detection) {
		this.command = command;
		this.detection = detection;
	}
}
