package com.example.timesyns;

import android.os.AsyncTask;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.app.ProgressDialog;
import android.util.Log;
import android.widget.Toast;

public class SNTPClient extends AsyncTask<String, Void, Integer> {
	private static final String DEFAULT_NTP_SERVER = "0.no.pool.ntp.org";
	private static final int SNTP_PORT = 123;

	private double ntpTime = 0;
	Toast mToast;
	String sharedKey;

	public SNTPClient(Toast t, String key) {
		mToast = t;
		sharedKey = key;
	}

	@Override
	protected Integer doInBackground(String... params) {
		try {
			ntpTime = retrieveSNTPTime(params);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
	}

	@Override
	protected void onPostExecute(Integer result) {

		double utc = ntpTime - (2208988800.0);

		// milliseconds
		long ms = (long) (utc * 1000.0);

		// date/time
		String date = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date(ms));

		// fraction
		double fraction = ntpTime - ((long) ntpTime);
		String fractionSting = new DecimalFormat(".000000").format(fraction);

		mToast.setText("System Time:\n" + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.S").format(new Date()));
		mToast.show();
		mToast.setText("NTP Time:\n" + date + fractionSting);
		mToast.show();

		// Log response
		Log.d("System Time: ", new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.S").format(new Date()));
		Log.d("NTP Time: ", date + fractionSting);

		super.onPostExecute(result);
	}

	private double retrieveSNTPTime(String... params) throws SocketException, UnknownHostException,
			IOException {
		String serverName = params[0];
		DatagramSocket socket = new DatagramSocket();
		InetAddress serverAddress = InetAddress.getByName(serverName);
		byte[] buffer = new NtpMessage().toByteArray();
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, SNTP_PORT);

		NtpMessage
				.encodeTimestamp(packet.getData(), 40, (System.currentTimeMillis() / 1000.0) + 2208988800.0);

		socket.send(packet);

		packet = new DatagramPacket(buffer, buffer.length);

		socket.receive(packet);

		// Process response
		NtpMessage message = new NtpMessage(packet.getData());

		// Display response
		Log.d("NTP server: ", serverName);
		Log.d("NTP message: ", message.toString());

		socket.close();

		return message.transmitTimestamp;
	}
}