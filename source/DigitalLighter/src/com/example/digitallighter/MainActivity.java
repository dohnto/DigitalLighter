package com.example.digitallighter;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.app.Activity;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {

	View background;
	TextView counter;
	boolean playingSignal = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		background = findViewById(R.id.background);
		Button action = (Button) findViewById(R.id.action_button);
		action.setOnClickListener(this);
		counter = (TextView) findViewById(R.id.txt_count);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.action_button:
			if (!playingSignal) {
				playingSignal = true;
				background.setBackgroundColor(Color.WHITE);
				new CountDownTimer(10000, 1000) {

					public void onTick(long millisUntilFinished) {
						counter.setText("" + (int) millisUntilFinished / 1000);
					}

					public void onFinish() {
						background.setBackgroundColor(Color.BLACK);
						counter.setText("");
						playingSignal = false;
					}
				}.start();
			}

			break;

		default:
			break;
		}
	}

}
