package com.example.digitallighter;

import java.io.IOException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class JmDNSActivity extends Activity {

	android.net.wifi.WifiManager.MulticastLock lock;
	android.os.Handler handler = new android.os.Handler();
	TextView text;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.jmdns);
		text = (TextView) findViewById(R.id.text);
		Button b = (Button) findViewById(R.id.servicesBtn);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						setUp();

					}
				}).start();

			}
		});
	}

	/** Called when the activity is first created. */

	private String type = "_http._tcp.local.";
	private JmDNS jmdns = null;
	private ServiceListener listener = null;
	private ServiceInfo serviceInfo;

	private void setUp() {

		try {
			jmdns = JmDNS.create();
			while (true) {
				final ServiceInfo[] infos = jmdns.list(type);
				Log.d("New", "List " + type);
				for (int i = 0; i < infos.length; i++) {
					final int j = i;
					text.post(new Runnable() {

						@Override
						public void run() {
							text.setText(text.getText().toString() + "\n" + infos[j]);
						}
					});
				}
				System.out.println();

				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (jmdns != null)
				try {
					jmdns.close();
				} catch (IOException exception) {
					//
				}
		}

		/*
		 * android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager)
		 * getSystemService(android.content.Context.WIFI_SERVICE); lock =
		 * wifi.createMulticastLock("mylockthereturn"); lock.setReferenceCounted(true); lock.acquire();
		 * 
		 * try { jmdns = JmDNS.create(); jmdns.addServiceListener(type, listener = new ServiceListener() {
		 * 
		 * @Override public void serviceResolved(ServiceEvent event) { Log.d("NoviService",
		 * "Service resolved: " + event.getName() + " of type " + event.getType()); }
		 * 
		 * @Override public void serviceRemoved(ServiceEvent event) { Log.d("Novi", "Service removed: " +
		 * event.getName() + " of type " + event.getType()); }
		 * 
		 * @Override public void serviceAdded(ServiceEvent event) { Log.d("Novi", "Service added: " +
		 * event.getName() + " of type " + event.getType()); ServiceInfo info =
		 * jmdns.getServiceInfo(event.getType(), event.getName()); Log.d("Novi", "Service info: " + info); }
		 * }); } catch (IOException e) { e.printStackTrace(); return; }
		 */
	}

	private void notifyUser(final String msg) {
		handler.postDelayed(new Runnable() {
			public void run() {

				TextView t = (TextView) findViewById(R.id.text);
				t.setText(msg + "\n=== " + t.getText());
			}
		}, 1);

	}

	@Override
	protected void onStart() {
		super.onStart();
		// new Thread(){public void run() {setUp();}}.start();
	}

	@Override
	protected void onStop() {
		if (jmdns != null) {
			if (listener != null) {
				jmdns.removeServiceListener(type, listener);
				listener = null;
			}
			jmdns.unregisterAllServices();
			try {
				jmdns.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			jmdns = null;
		}
		// repo.stop();
		// s.stop();
		if (lock != null)
			lock.release();
		super.onStop();
	}
}