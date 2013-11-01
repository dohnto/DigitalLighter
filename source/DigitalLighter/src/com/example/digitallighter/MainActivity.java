package com.example.digitallighter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import javax.jmdns.ServiceTypeListener;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener, OnItemSelectedListener,
		ServiceListener, ServiceTypeListener, OnItemClickListener {

	// LOGCAT TAG
	private static final String TAG = "Client";

	// UI
	View background;
	public ListView listView;
	public ArrayAdapter<String> adapter;
	ArrayList<String> list;
	Toast mToast;

	// COMMAND
	boolean isPlaying = false;
	ArrayList<String> playingQueue = new ArrayList<String>();
	private int selectedServiceIndex = -1;

	// NETWORK
	private static String SERVICE_TYPE = "_http._tcp.local.";
	JmDNS jmdns = null;
	ArrayList<ServiceInfo> services = new ArrayList<ServiceInfo>();
	private Handler mUpdateHandler;
	private Connection mConnection;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.main_activity);

		// RETRIEVE UI ELEMENTS
		mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		final Button action = (Button) findViewById(R.id.action_button);
		action.setOnClickListener(this);
		listView = (ListView) findViewById(R.id.lista_servisa);
		listView.setOnItemClickListener(this);
		background = findViewById(R.id.background);
		final Button connect = (Button) findViewById(R.id.btn_connect);
		final Button hide = (Button) findViewById(R.id.hideUI);
		/**/
		background.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (action.isShown()) {
					action.setVisibility(View.GONE);
					listView.setVisibility(View.GONE);
					connect.setVisibility(View.GONE);
				} else {
					action.setVisibility(View.VISIBLE);
					listView.setVisibility(View.VISIBLE);
					connect.setVisibility(View.VISIBLE);
					hide.setVisibility(View.VISIBLE);
				}

			}
		}); /**/

		hide.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				hide.setVisibility(View.GONE);
				listView.setVisibility(View.GONE);
				action.setVisibility(View.GONE);
				connect.setVisibility(View.GONE);
			}
		});

		// SETTING ADAPTER FOR SPINNER (DROP-DOWN LIST)
		list = new ArrayList<String>();
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		listView.setAdapter(adapter);

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

		// START SCANING FOR SERVICES
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					jmdns = JmDNS.create();
					jmdns.addServiceTypeListener(MainActivity.this);
				} catch (IOException e) {
					mToast.setText("JmDNS not initialised.");
					mToast.show();
					e.printStackTrace();
				}

			}
		}).start();

	}

	// ========================================================================================================
	// CONNECT TO SELECTED SERVICE
	// ========================================================================================================

	public void clickConnect(View v) {
		if (selectedServiceIndex != -1) {
			ServiceInfo serviceToConnectTo = services.get(selectedServiceIndex);
			if (serviceToConnectTo != null) {
				String address = serviceToConnectTo.getNiceTextString();
				InetAddress adr = intToInetAddress(ipStringToInt(address.substring(1)));
				if (serviceToConnectTo.getAddress() != null) {
					mConnection
							.connectToServer(serviceToConnectTo.getAddress(), serviceToConnectTo.getPort());
					mToast.setText("Trying to connect");
					mToast.show();
					return;
				} else {
					mToast.setText("Cannot connect, service not resolved");
					mToast.show();
					return;
				}
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
	// LIST ITEM CLICK ACTIONS
	// ========================================================================================================

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		mToast.setText(list.get(arg2) + " selected");
		mToast.show();
		selectedServiceIndex = arg2;
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	// ========================================================================================================
	// SERVICE LISTENER
	// ========================================================================================================
	@Override
	public void serviceAdded(final ServiceEvent arg0) {
		if (!list.contains(arg0.getName())) {
			Log.d("Detected service: ", arg0.getName());

			listView.post(new Runnable() {

				@Override
				public void run() {
					services.add(arg0.getInfo());
					list.add(arg0.getName());
					adapter.notifyDataSetChanged();
				}
			});

			mToast.setText(arg0.getName() + " service detected");
			mToast.show();
		}
	}

	@Override
	public void serviceRemoved(ServiceEvent arg0) {

	}

	@Override
	public void serviceResolved(ServiceEvent arg0) {
		mToast.setText(arg0.getName() + " service resolved");
		mToast.show();
	}

	@Override
	public void serviceTypeAdded(ServiceEvent arg0) {
		mToast.setText(arg0.getType() + " service type detected");
		mToast.show();
		jmdns.addServiceListener(arg0.getType(), MainActivity.this);
	}

	@Override
	public void subTypeForServiceTypeAdded(ServiceEvent arg0) {
		// TODO Auto-generated method stub

	}

}
