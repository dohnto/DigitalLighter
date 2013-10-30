package com.example.digitallighter;

import java.io.IOException;
import java.util.ArrayList;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class DNSService {

	private static String SERVICE_TYPE = "_http._tcp.local.";
	static ServiceInfo[] services = null;
	static public View postView = null;
	static public ArrayList<String> list = new ArrayList<String>();
	static public ArrayAdapter<String> adapter;
	static Toast mToast = Toast.makeText(DLApplication.getContext(), "", Toast.LENGTH_SHORT);
	static Thread t;
	static private boolean running = true;

	static public void setPostingData(View v, ArrayAdapter<String> a) {
		postView = v;
		adapter = a;
		running = true;
	}

	/** Returns list of detected services as ServiceInfo[] */
	public static void scanServices() {
		services = null;
		t = new Thread(new Runnable() {

			@Override
			public void run() {

				JmDNS jmdns = null;

				try {
					jmdns = JmDNS.create();
					Log.d("JmDNSService", "Scanning for services");

					while (running) {
						services = jmdns.list(SERVICE_TYPE);
						if (postView != null && DNSService.services != null && services.length > 0)
							postView.post(new Runnable() {

								@Override
								public void run() {
									if (DNSService.services != null && services.length > 0)
										for (int i = 0; i < services.length; i++)
											if (!list.contains(services[i].getName())) {
												String serviceName = services[i].getName();
												adapter.add(serviceName);
												list.add(serviceName);
												mToast.setText(services[i].getName() + " service detected");
												mToast.show();
											}

								}
							});

						try {
							Thread.sleep(3000);
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
						}
				}
			}
		});
		t.start();
	}

	public static ServiceInfo getService(String serviceName) {
		ServiceInfo resultService = null;
		if (services != null && services.length > 0) {
			for (int i = 0; i < DNSService.services.length; i++) {
				if (DNSService.services[i].getName().equals(serviceName)) {
					resultService = DNSService.services[i];
					break;
				}
			}
		}
		return resultService;
	}

	public static void refreshStaticVars() {
		services = null;
		postView = null;
		list = new ArrayList<String>();
		adapter = null;
		running = false;
	}
}
