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

	public Point map(Point imageSize, Point point) {
		return new Point();
	}
	
	public ArrayList<Point> mapList(Point imageSize, ArrayList<Point> listOfPoints) {
		return new ArrayList<Point>();
	}
}
