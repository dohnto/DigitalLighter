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

package com.silentducks.digitallighterserver.devicelocation;

import java.util.ArrayList;

public class TreeListDivider<T> {
	private ArrayList<ArrayList<T>> division;
	private int parts;
	private int steps = 0;

	public TreeListDivider(ArrayList<T> list, int parts) {
		this.division = new ArrayList<ArrayList<T>>();
		division.add(new ArrayList<T>(list));
		for (int i = 1; i < parts; i++)
			this.division.add(new ArrayList<T>());
		this.parts = parts;
	}

	public ArrayList<ArrayList<T>> getNextDivision() {
		// initialize new division
		ArrayList<ArrayList<T>> newDivision = new ArrayList<ArrayList<T>>();
		for (int i = 0; i < parts; i++)
			newDivision.add(new ArrayList<T>());

		for (int i = 0; i < division.size(); i++) { // iterate through all
			// divisions
			ArrayList<T> div = division.get(i);
			int divCounter = 0;
			for (int j = 0; j < div.size(); j++) {
				newDivision.get(divCounter).add(div.get(j));
				divCounter = (divCounter + 1) % parts;
			}
		}
		division = newDivision;
		steps++;
		return division;
	}

	public boolean isFinished() {
		int itemsCount = 0;
		for (int i = 0; i < parts; i++) {
			itemsCount += division.get(i).size();
		}
		int finalDivision = (int)Math.ceil(log(itemsCount, parts));
		if (finalDivision == 0)
			finalDivision = 1; // correction
		return finalDivision <= steps;
	}
	
	public static double log(double x, int base) {
		return Math.log(x)/Math.log(base);
	}
}