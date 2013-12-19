/**
  * Digital Ligter
  * Customer Driven Project - NTNU
  * 20th November  2013
  *
  * @author Jan Bednarik
  * @author Tomas Dohnalek
  * @author Milos Jovac
  * @author Agnethe Soraa
  */

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
