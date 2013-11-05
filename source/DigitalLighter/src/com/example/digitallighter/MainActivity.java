package com.example.digitallighter;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import android.app.Activity;
import android.os.Bundle;
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
import android.widget.Toast;

import com.example.dns.NameIPPair;
import com.example.dns.NetThread;
import com.example.dns.Packet;
import com.example.timesyns.SNTPClient;

public class MainActivity extends Activity implements OnClickListener, OnItemSelectedListener,
		OnItemClickListener {

	// LOGCAT TAG
	private static final String TAG = "Client";

	// UI
	View background;
	public ListView listView;
	public ArrayAdapter<String> adapter;
	ArrayList<String> list;
	Toast mToast;
	Button connect;
	Button hide;
	Button action;
	Button refresh;
	private int selectedServiceIndex = -1;

	// NETWORK
	private static String SERVICE_TYPE = "_http._tcp.local.";
	private Connection mConnection;
	private NetThread netThread = null;
	ArrayList<NameIPPair> packetList;

	// PLAYER
	ClientPlayer player;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.main_activity);

		// SET BRIGHTNES TO MAX
		WindowManager.LayoutParams layout = getWindow().getAttributes();
		layout.screenBrightness = 0.2F;
		getWindow().setAttributes(layout);

		// RETRIEVE UI ELEMENTS
		mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		action = (Button) findViewById(R.id.action_button);
		action.setOnClickListener(this);
		listView = (ListView) findViewById(R.id.lista_servisa);
		listView.setOnItemClickListener(this);
		refresh = (Button) findViewById(R.id.action_refresh);
		background = findViewById(R.id.background);
		background.setOnClickListener(this);
		connect = (Button) findViewById(R.id.btn_connect);
		hide = (Button) findViewById(R.id.hideUI);

		// SETTING PLAYER
		player = new ClientPlayer(background);

		// SETTING ADAPTER FOR SPINNER (DROP-DOWN LIST)
		list = new ArrayList<String>();
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		listView.setAdapter(adapter);

		// CONNECTION
		mConnection = new Connection(ipc);
		packetList = new ArrayList<NameIPPair>();
	}

	public void hideUI(View v) {
		if (action.isShown()) {
			action.setVisibility(View.GONE);
			refresh.setVisibility(View.GONE);
			hide.setVisibility(View.GONE);
			listView.setVisibility(View.GONE);
			connect.setVisibility(View.GONE);
			findViewById(R.id.time_btn).setVisibility(View.GONE);
		} else {
			action.setVisibility(View.VISIBLE);
			refresh.setVisibility(View.VISIBLE);
			hide.setVisibility(View.VISIBLE);
			listView.setVisibility(View.VISIBLE);
			connect.setVisibility(View.VISIBLE);
			hide.setVisibility(View.VISIBLE);
			findViewById(R.id.time_btn).setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (netThread != null) {
			Log.e(TAG, "netThread should be null!");
			netThread.submitQuit();
		}
		netThread = new NetThread(this);
		netThread.start();
	}

	/**
	 * Handle submitting an mDNS query.
	 */
	public void handleQueryButton(View view) {

		try {
			netThread.submitQuery(SERVICE_TYPE);
		} catch (Exception e) {
			Log.w(TAG, e.getMessage(), e);
			return;
		}
	}

	// ========================================================================================================
	// CONNECT TO SELECTED SERVICE
	// ========================================================================================================

	public void clickConnect(View v) {

		if (selectedServiceIndex != -1) {
			NameIPPair data = packetList.get(selectedServiceIndex);
			String parts[] = data.name.split(":");
			timeSync(data.ipAddress.toString().substring(1));
			mConnection.connectToServer(data.ipAddress, Integer.parseInt(parts[1]));
			mToast.setText("Trying to connect");
		} else {
			mToast.setText("Pick a service");
		}
		mToast.show();
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.action_button:
			player.addCommand("#ff0000:10000|#00ff00:10000|#0000ff:10000");
			break;

		case R.id.background:
			hideUI(null);
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

	// inter-process communication
	public IPCHandler ipc = new IPCHandler();

	/**
	 * Allow the network thread to send us messages via this IPC mechanism.
	 * 
	 * @author simmons
	 */
	public class IPCHandler extends Handler {

		private static final int MSG_SET_STATUS = 1;
		private static final int MSG_ADD_PACKET = 2;
		private static final int MSG_ERROR = 3;

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			// don't process incoming IPC if we are paused.
			if (adapter == null) {
				Log.w(TAG, "dropping incoming message: " + msg);
				return;
			}

			switch (msg.what) {
			case Protocol.MESSAGE_TYPE_COMMAND:
				player.addCommand(msg.getData().getString(Protocol.COMMAND));
				break;

			case Protocol.MESSAGE_TYPE_SERVER_STARTED:
				mToast.setText("Connected");
				mToast.show();
				break;
			case MSG_SET_STATUS:
				// statusLine.setText((String) msg.obj);
				break;
			case MSG_ADD_PACKET:

				String s = ((Packet) msg.obj).description;
				InetAddress adr = ((Packet) msg.obj).src;
				int port = ((Packet) msg.obj).srcPort;

				String[] peaces = s.split("PTR ");

				if (peaces.length > 0) {
					for (int i = 1; i < peaces.length; i++) {
						String name = peaces[i].split("._")[0];
						if (!list.contains(name)) {
							packetList.add(new NameIPPair(name, adr, port));
							list.add(name);
						}
					}
					adapter.notifyDataSetChanged();
				}
				break;
			case MSG_ERROR:
				break;
			default:
				Log.w(TAG, "unknown activity message code: " + msg);
				break;
			}
		}

		public void setStatus(String status) {
			sendMessage(Message.obtain(ipc, MSG_SET_STATUS, status));
		}

		public void addPacket(Packet packet) {
			sendMessage(Message.obtain(ipc, MSG_ADD_PACKET, packet));
		}

		public void error(Throwable throwable) {
			sendMessage(Message.obtain(ipc, MSG_ERROR, throwable));
		}
	};

	// ========================================================================================================
	// TYME SYNC OVER NTP(Network Time Protocol). "0.no.pool.ntp.org" is Norway closest server
	// The offset is compared to System time and result is saved in sharedPref.
	// Since we use the mobile device as a router, in order for this code to work, client will first have to
	// connect to network that provide internet connection, like edurom.
	// Once time diff is saved, we can switch back to mobile device hotspot.
	// Also when we start using a real router, internet connection can be provided over it, and there will be
	// no need for 2 network sequence connactions by client.
	//
	// Saved value offset can be used for sheduling commands that include time constant.
	// ========================================================================================================

	public void timeSync(String adr) {
		new SNTPClient().execute(adr);
	}
}
