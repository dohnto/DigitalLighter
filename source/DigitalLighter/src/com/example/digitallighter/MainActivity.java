package com.example.digitallighter;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.jmdns.ServiceInfo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
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

	// CONNECTION
	private Handler mUpdateHandler;
	private Connection mConnection;

	// UI
	View background;
	TextView counter;
	public Spinner spinner;
	public ArrayAdapter<String> adapter;
	ArrayList<String> list;

	// COMMAND
	boolean isPlaying = false;
	ArrayList<String> playingQueue = new ArrayList<String>();
	private int selectedServiceIndex = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.main_activity);

		// RETRIEVE UI ELEMENTS
		final Button action = (Button) findViewById(R.id.action_button);
		action.setOnClickListener(this);
		counter = (TextView) findViewById(R.id.txt_count);
		spinner = (Spinner) findViewById(R.id.spinner);
		spinner.setOnItemSelectedListener(this);
		background = findViewById(R.id.background);
		final Button connect = (Button) findViewById(R.id.btn_connect);
		background.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (action.isShown()) {
					action.setVisibility(View.GONE);
					spinner.setVisibility(View.GONE);
					connect.setVisibility(View.GONE);
				} else {
					action.setVisibility(View.VISIBLE);
					spinner.setVisibility(View.VISIBLE);
					connect.setVisibility(View.VISIBLE);
				}

			}
		});

		// SETTING ADAPTER FOR SPINNER (DROP-DOWN LIST)
		list = new ArrayList<String>();
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);
		spinner.setPrompt("Pick a Service");

		// HENDELR GETS MESSAGES FROM BACKGROUND THREADS AND MAKE MODIFICATIONS TO UI

		mUpdateHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.getData().getInt(Protocol.MESSAGE_TYPE)) {
				case Protocol.MESSAGE_TYPE_COMMAND:
					playCommand(msg.getData().getString(Protocol.COMMAND));
					break;

				case Protocol.MESSAGE_TYPE_SERVER_STARTED:
					Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		};

		// CONNECTION
		mConnection = new Connection(mUpdateHandler);

	}

	// ========================================================================================================
	// CONNECT TO SELECTED SERVICE
	// ========================================================================================================

	public void clickConnect(View v) {
		if (selectedServiceIndex != -1) {
			ServiceInfo serviceToConnectTo = DNSService.getService(list.get(selectedServiceIndex));
			if (serviceToConnectTo != null) {
				String address = serviceToConnectTo.getNiceTextString();
				InetAddress adr = intToInetAddress(ipStringToInt(address.substring(1)));
				mConnection.connectToServer(serviceToConnectTo.getAddress(), serviceToConnectTo.getPort());
				Toast.makeText(this, "Trying to connect", Toast.LENGTH_SHORT).show();
				return;
			}

		}
		Toast.makeText(this, "Pick a service", Toast.LENGTH_SHORT).show();

	}

	public static int ipStringToInt(String str) {
		int result = 0;
		String[] array = str.split("\\.");
		if (array.length != 4)
			return 0;
		try {
			result = Integer.parseInt(array[3]);
			result = (result << 8) + Integer.parseInt(array[2]);
			result = (result << 8) + Integer.parseInt(array[1]);
			result = (result << 8) + Integer.parseInt(array[0]);
		} catch (NumberFormatException e) {
			return 0;
		}
		return result;
	}

	public static InetAddress intToInetAddress(int hostAddress) {
		InetAddress inetAddress;
		byte[] addressBytes = { (byte) (0xff & hostAddress), (byte) (0xff & (hostAddress >> 8)),
				(byte) (0xff & (hostAddress >> 16)), (byte) (0xff & (hostAddress >> 24)) };

		try {
			inetAddress = InetAddress.getByAddress(addressBytes);
		} catch (UnknownHostException e) {
			return null;
		}
		return inetAddress;
	}

	// ========================================================================================================
	// PLAY ONE COMMAND. COMMAND FORMAT (color(hex):duration(msec)) EXAMPLE: ("#ff00ff:5")
	// ========================================================================================================

	public void playCommand(String command) {

		// GET MULTIPLE COMMANDS AND PUT THEM IN QUEUE
		if (command.contains("|")) {
			String[] commands = command.split("\\|");
			for (String s : commands) {
				playingQueue.add(s);
			}
		} else if (!command.equals("recursion")) {
			playingQueue.add(command);
		}

		// IN CASE OF BAD COMMAND JUST EXIT
		if (playingQueue.isEmpty())
			return;

		if (!isPlaying || command.equals("recursion")) {

			// GET ONE COMMAND INFO AND REMOVE IT FROM QUEUE
			String[] parts = playingQueue.get(0).split(":");
			int color = Color.parseColor(parts[0]);
			int duration = Integer.parseInt(parts[1]);
			playingQueue.remove(0);

			// SET FLAG SO OTHER COMMANDS HAVE TO WAIT
			isPlaying = true;

			background.setBackgroundColor(color);
			new CountDownTimer(duration, 500) {

				// SHOW TIME TILL END OF THE COMMAND
				public void onTick(long millisUntilFinished) {
					counter.setText("" + (int) millisUntilFinished / 1000);
				}

				// IF THERE IS MORE COMMANDS IN QUEUE PLAY THEM, IF NOT SET THE FLAG AND RETURN
				public void onFinish() {
					if (playingQueue.isEmpty()) {
						// background.setBackgroundColor(Color.WHITE); now device should stay lighting last
						// command color
						isPlaying = false;
					} else {
						playCommand("recursion");
					}
					counter.setText("");
				}
			}.start();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.action_button:
			playCommand("#ff0000:10000|#00ff00:10000|#0000ff:10000");
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
		selectedServiceIndex = pos;
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	// ========================================================================================================
	// LIFE CYCLE METHODS
	// ========================================================================================================

	@Override
	protected void onResume() {
		// START SCANING FOR SERVICES

		DNSService.setPostingData(background, adapter);
		DNSService.scanServices();
		super.onResume();
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
