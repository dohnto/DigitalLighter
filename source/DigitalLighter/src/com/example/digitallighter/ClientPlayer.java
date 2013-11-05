package com.example.digitallighter;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import android.graphics.Color;
import android.os.CountDownTimer;
import android.view.View;

public class ClientPlayer {

	View background;
	public static long timeOffset = Long.MIN_VALUE;

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
		// INITIAL COMMAND WITH TIME STAMP
		if (command.contains("@")) {
			String[] parts = command.split("@");
			long startTime = Long.parseLong(parts[0], 10);
			long timeDiff = TimeUnit.MILLISECONDS.toNanos(startTime)
					- (System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeOffset));

			command = command.substring(command.indexOf("@") + 1);

			// Playback start time is in future.
			if (timeDiff > 0) {
				try {
					Thread.sleep(TimeUnit.NANOSECONDS.toMillis(timeDiff));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

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

		// GET ONE COMMAND INFO AND REMOVE IT FROM QUEUE
		String[] parts = playingQueue.poll().split(":");
		int color = Color.parseColor(parts[0]);
		int duration = Integer.parseInt(parts[1]);
		playingQueue.remove(0);

		// SET PROPER BACKGROUND DEFINED BY COMMAND
		background.setBackgroundColor(color);
		new CountDownTimer(duration, 500) {

			// SHOW TIME TILL END OF THE COMMAND
			public void onTick(long millisUntilFinished) {
			}

			// IF THERE IS MORE COMMANDS IN QUEUE PLAY THEM, IF NOT SET THE FLAG AND RETURN
			public void onFinish() {
				if (playingQueue.isEmpty()) {
					isPlaying = false;
				} else {
					play();
				}
			}
		}.start();

	}

}
