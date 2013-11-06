package com.example.digitallighter;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import android.graphics.Color;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;

public class ClientPlayer {

	View background;
	public static long timeOffset = 0;

	// COMMAND
	boolean isPlaying = false;
	Queue<String> playingQueue;

	public ClientPlayer(View background) {
		this.background = background;
		playingQueue = new LinkedList<String>();
	}

	// ========================================================================================================
	// ADD ONE COMMAND. COMMAND FORMAT: (color(hex):duration(msec)) EXAMPLE: ("#ff00ff:5")
	// FOR MULTIPLE COMMANDS CONCATENATE WITH '|' EXAMPLE: ("#ff00ff:5|#ffabcf:7")
	// ========================================================================================================

	public void addCommand(String command) {

		// GET MULTIPLE COMMANDS AND PUT THEM IN QUEUE
		if (command.contains("|")) {
			String[] commands = command.split("\\|");
			for (String s : commands) {
				playingQueue.add(s);
			}
		} else {
			playingQueue.add(command);
		}

		if (!isPlaying) {
			isPlaying = true;
			play();
		}
	}

	public void play() {

		while (!playingQueue.isEmpty()) {

			// GET ONE COMMAND INFO AND REMOVE IT FROM QUEUE
			String[] parts = playingQueue.poll().split(":");
			long time = Long.parseLong(parts[0]);
			int color = Color.parseColor(parts[1]);
			// int duration = Integer.parseInt(parts[2]);

			while (System.currentTimeMillis() + timeOffset < time) {

			}

			// SET PROPER BACKGROUND DEFINED BY COMMAND
			background.setBackgroundColor(color);

		}

		isPlaying = false;

		/*
		 * new CountDownTimer(duration, 500) {
		 * 
		 * // SHOW TIME TILL END OF THE COMMAND public void onTick(long millisUntilFinished) { }
		 * 
		 * // IF THERE IS MORE COMMANDS IN QUEUE PLAY THEM, IF NOT SET THE FLAG AND RETURN public void
		 * onFinish() { if (playingQueue.isEmpty()) { isPlaying = false; } else { play(); } } }.start();
		 */

	}

}
