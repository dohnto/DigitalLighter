package com.example.digitallighter;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.graphics.Color;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener, OnItemSelectedListener {

	// LOGCAT TAG

	private static final String TAG = "Client";

	// NSD

	NsdHelper mNsdHelper;
	private Handler mUpdateHandler;
	private Connection mConnection;

	// UI

	View background;
	TextView counter;
	public Spinner spinner;
	public ArrayAdapter<String> adapter;

	// FLAG THAT PREVENT PLAYING NEW COMMAND IF FIRST IS STILL PLAYING

	boolean playingSignal = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		// RETRIEVE UI ELEMENTS

		background = findViewById(R.id.background);
		Button action = (Button) findViewById(R.id.action_button);
		action.setOnClickListener(this);
		counter = (TextView) findViewById(R.id.txt_count);
		spinner = (Spinner) findViewById(R.id.spinner);
		spinner.setOnItemSelectedListener(this);

		// SETTING ADAPTER FOR SPINNER (DROP-DOWN LIST)

		ArrayList<String> list = new ArrayList<String>();
		list.add("Pick Service");
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);

		// HENDELR GETS MESSAGES FROM BACKGROUND THREADS AND MAKE MODIFICATIONS TO UI

		mUpdateHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {

				switch (msg.getData().getInt(Protocol.MESSAGE_TYPE)) {
				case Protocol.MESSAGE_TYPE_NEW_SERVICE_FOUND:
					adapter.add(msg.getData().getString(Protocol.NEW_SERVICE_NAME));
					Toast.makeText(MainActivity.this, "New Sevice Detected", Toast.LENGTH_SHORT).show();
					break;

				case Protocol.MESSAGE_TYPE_COMMAND:
					playCommand(msg.getData().getString(Protocol.COMMAND));
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
	// CONNECT TO SELECTED SERVICE
	// ========================================================================================================

	public void clickConnect(View v) {
		NsdServiceInfo service = mNsdHelper.getChosenServiceInfo();
		if (service != null) {
			Log.d(TAG, "Connecting.");
			Toast.makeText(this, "Connecting", Toast.LENGTH_SHORT).show();
			mConnection.connectToServer(service.getHost(), service.getPort());
		} else {
			Log.d(TAG, "No service to connect to!");
		}
	}

	// ========================================================================================================
	// PLAY ONE COMMAND. COMMAND FORMAT (color(hex):duration(msec)) EXAMPLE: ("#ff00ff:5")
	// ========================================================================================================

	public void playCommand(String command) {

		String[] parts = command.split(":");
		int color = Color.parseColor(parts[0]);
		int duration = Integer.parseInt(parts[1]);

		if (!playingSignal) {
			playingSignal = true;
			background.setBackgroundColor(color);
			new CountDownTimer(duration, 1000) {

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
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.action_button:
			playCommand("#ffffff:10000");
			break;

		default:
			break;
		}
	}

	// ========================================================================================================
	// SPINNER SELECT ACTIONS
	// ========================================================================================================

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long arg3) {
		mNsdHelper.reslveOnDemand(pos);

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

	// ========================================================================================================
	// LIFE CYCLE METHODS
	// ========================================================================================================

	@Override
	protected void onResume() {
		mNsdHelper.discoverServices();
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
