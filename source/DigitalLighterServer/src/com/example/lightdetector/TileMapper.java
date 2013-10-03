package com.example.lightdetector;

import java.util.ArrayList;

import android.graphics.Point;

public class TileMapper {
	
	private int tilesCountX;
	private int tilesCountY;
	
	
	public TileMapper(Point tilesCount) {
		setTileSize(tilesCount.x, tilesCount.y);
	}
	
	public TileMapper(int tilesCountX, int tilesCountY) {
		setTileSize(tilesCountX, tilesCountY);
	}

	public void setTileSize(int tileCountX, int tileCountY) {
		this.tilesCountX = tileCountX;
		this.tilesCountY = tileCountY;
	}
	
	public ArrayList<Point> mapList(Point imageSize, ArrayList<Point> listOfPoints) {
		ArrayList<Point> result = new ArrayList<Point>();
		
		for (Point point: listOfPoints) {
			result.add(map(imageSize, point));
		}
		
		return result;
	}

	public Point map(Point imageSize, Point point) {
		assert point.x >= 0 && point.x < imageSize.x && point.y >= 0 && point.y < imageSize.y;
		
		Point tile = new Point();
		
		tile.x = mapToBox(tilesCountX, imageSize.x, point.x);
		tile.y = mapToBox(tilesCountY, imageSize.y, point.y);
		
		return tile;
	}
	
	private int mapToBox(int boxes, int size, int position) {
		int boxSize = size/boxes; 
		int sum = 0;
		int index = 0;
		while (sum < position) {
			sum += boxSize;
			index++;
		}
		return index - 1;
	}
}
