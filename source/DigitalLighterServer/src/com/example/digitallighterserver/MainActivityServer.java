package com.example.digitallighterserver;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivityServer extends Activity {

	// LOGCAT TAG

	public static final String TAG = "DigitalLighterServer";

	// NSD

	NsdHelper mNsdHelper;
	private Connection mConnection;
	private Handler mUpdateHandler;

	// USER COUNT

	int userCount = 0;
	TextView txtUserCount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// RETRIEVE UI ELEMENTS

		txtUserCount = (TextView) findViewById(R.id.user_counter);
		txtUserCount.setText(getString(R.string.user_number) + "0");

		// HENDELR GETS MESSAGES FROM BACKGROUND THREADS AND MAKE MODIFICATIONS TO UI

		mUpdateHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {

				// BASIC PROTOCOL THIS WILL EVOLVE.

				switch (msg.getData().getInt(Protocol.MESSAGE_TYPE)) {
				case Protocol.MESSAGE_TYPE_USER_ADDED:
					txtUserCount.setText(getString(R.string.user_number) + userCount);
					userCount++;
					break;

				case Protocol.MESSAGE_TYPE_SERVER_STARTED:
					Toast.makeText(MainActivityServer.this, "Server Started", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		};

		// NSD

		mConnection = new Connection(mUpdateHandler);
		mNsdHelper = new NsdHelper(this, mUpdateHandler);
		mNsdHelper.initializeNsd();
	}

	// ========================================================================================================
	// REGISTER SERVICE TO ROUTER
	// ========================================================================================================

	public void clickAdvertise(View v) {
		// Register service
		if (mConnection.getLocalPort() > -1) {
			mNsdHelper.registerService(mConnection.getLocalPort());
			Toast.makeText(this, "Server started", Toast.LENGTH_SHORT).show();
		} else {
			Log.d(TAG, "ServerSocket isn't bound.");
			Toast.makeText(this, "Server isn't bound", Toast.LENGTH_SHORT).show();
		}
	}

	// ========================================================================================================
	// SEND COMMAND SIGNAL TO USER (5sec red color)
	// ========================================================================================================

	public void clickRed(View v) {
		sendCommandSignal("#ff0000:5000");
	}

	// ========================================================================================================
	// SEND COMMAND SIGNAL TO USER (7sec white color)
	// ========================================================================================================

	public void clickWhite(View v) {
		sendCommandSignal("#ffffff:7000");
	}

	// ========================================================================================================
	// SEND COMMAND SIGNAL TO USER (3sec blue color)
	// ========================================================================================================

	public void clickBlue(View v) {
		sendCommandSignal("#0000ff:3000");
	}

	// ========================================================================================================
	// SENDING COMMAND SIGNAL
	// ========================================================================================================

	public void sendCommandSignal(String signal) {
		mConnection.sendMessage(signal);
		Toast.makeText(this, "Sent: " + signal, Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
