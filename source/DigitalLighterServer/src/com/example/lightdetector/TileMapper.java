package com.example.lightdetector;

import java.util.ArrayList;

import org.opencv.core.Point;
import org.opencv.core.Size;

/**
 * This class maps point from image to given category.
 * Consider 
 * @author Tomas Dohnalek
 *
 */
public class TileMapper {
	
	private int tilesCountX;
	private int tilesCountY;

	/**
	 * Creates TileMapping class that
	 * @param tilesCountX
	 * @param tilesCountY
	 */
	public TileMapper(int tilesCountX, int tilesCountY) {
		setTileSize(tilesCountX, tilesCountY);
	}

	public void setTileSize(int tileCountX, int tileCountY) {
		this.tilesCountX = tileCountX;
		this.tilesCountY = tileCountY;
	}
	
	public ArrayList<Point> mapList(Size imageSize, ArrayList<Point> listOfPoints) {
		ArrayList<Point> result = new ArrayList<Point>();
		
		for (Point point: listOfPoints) {
			result.add(map(imageSize, point));
		}
		
		return result;
	}

	public Point map(Size imageSize, Point point) {
		Point tile = new Point();
		
		tile.x = mapToBox(tilesCountX, imageSize.width, point.x);
		tile.y = mapToBox(tilesCountY, imageSize.height, point.y);
		
		return tile;
	}
	
	private int mapToBox(int boxes, double size, double position) {
		double boxSize = size/boxes; 
		double sum = 0;
		int index = 0;
		while (sum < position) {
			sum += boxSize;
			index++;
		}
		
		if (index > 0)
			index -= 1; // correction 
		
		return index;
	}
}
