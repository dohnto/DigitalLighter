/**
 * CopyRight (C), 2013
 */

package com.example.jmdns;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.digitallighterserver.Protocol;

public class JmDNSService {
	private static final String TAG = JmDNSService.class.getSimpleName();
	private static final int LOCAL_SERVER_WEIGHT = 1;
	private static final int LOCAL_SERVER_PROIORITY = 1;
	private ZeroRegistrationListener mServiceListener;
	private ZeroServiceDiscoverListener mDiscoverListener;
	private JmDNS mJmDNS;
	private ZeroServiceInfo mZsinfo;
	private String mType = "_http._tcp.local.";
	private boolean mHost;
	private boolean mWifiEnable;
	private InetAddress mAddress;

	Handler handler;

	// WIFI related
	private WifiManager mWifiManager;
	private MulticastLock mWifiMulticastLock = null;

	// ====================================================================================
	// CONSTRUCTOR
	// ====================================================================================

	public JmDNSService(Context contex, Handler mUpdateHandler) {
		handler = mUpdateHandler;
		mHost = true;
		mWifiEnable = true;
		mWifiManager = (WifiManager) contex.getSystemService(Context.WIFI_SERVICE);
	}

	// ====================================================================================
	// ADD AND REMOVE REGISTARTION LISTENER
	// ====================================================================================

	public void addRegistrationListener(ZeroRegistrationListener listener) {
		mServiceListener = listener;
	}

	public void addServiceDiscoverListener(ZeroServiceDiscoverListener listener) {
		mDiscoverListener = listener;
	}

	// ====================================================================================
	// CREATE AND REGISTER NEW SERVICE
	// ====================================================================================

	public void createService(String name, int port) {

		InetAddress[] addresses = new InetAddress[20];
		acquireMulticastLock();
		try {
			if (null != mJmDNS) {
				destroyService(null, false);
			}
			if (mHost) {

				StringBuilder IFCONFIG = new StringBuilder();
				try {
					int i = 0;
					for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
							.hasMoreElements();) {
						NetworkInterface intf = en.nextElement();
						for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
								.hasMoreElements();) {
							InetAddress inetAddress = enumIpAddr.nextElement();
							if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()
									&& inetAddress.isSiteLocalAddress()) {
								IFCONFIG.append(inetAddress.getHostAddress().toString() + "\n");
								addresses[i++] = inetAddress;
							}

						}
					}
				} catch (SocketException ex) {
					Log.e("LOG_TAG", ex.toString());
				}

				mJmDNS = JmDNS.create();

			}
			Bundle messageBundle = new Bundle();
			messageBundle.putInt(Protocol.MESSAGE_TYPE, Protocol.MESSAGE_TYPE_SERVER_STARTED);
			messageBundle.putString(Protocol.NEW_SERVICE_NAME, name);
			Message message = new Message();
			message.setData(messageBundle);
			handler.sendMessage(message);

		} catch (Exception e) {
			Log.e(TAG, "failed to create the service", e);
			if (null != mServiceListener) {
				mServiceListener.onServiceRegisteredFailed(new ZeroServiceInfo(null, null));
			}
		}
		if (null != mJmDNS) {
			try {
				ServiceInfo si = ServiceInfo.create(mType, name, port, LOCAL_SERVER_WEIGHT,
						LOCAL_SERVER_PROIORITY, true, addresses[0].toString());

				mJmDNS.registerService(si);
			} catch (Exception e) {
				Log.e(TAG, "failed to cast the service", e);
			}
		}
	}

	private void destroyService(ZeroServiceInfo zsi, boolean notify) {
		if (null == mJmDNS) {
			if (notify) {
				mServiceListener.onServiceUnregistered(zsi);
			}
			return;
		}
		try {
			releaseMulticastLock();
			mJmDNS.unregisterAllServices();
			mJmDNS.close();
			mJmDNS = null;
			if (null != mServiceListener && notify) {
				mServiceListener.onServiceUnregistered(zsi);
			}
		} catch (Exception e) {
			Log.e(TAG, "failed to close the JmDNS service");
			if (null != mServiceListener && notify) {
				mServiceListener.onServiceUnregisteredFailed(zsi);
			}
		}
	}

	private void acquireMulticastLock() {
		if (null != mWifiMulticastLock && mWifiMulticastLock.isHeld()) {
			mWifiMulticastLock.release();
		}

		mWifiMulticastLock = mWifiManager.createMulticastLock(TAG);
		mWifiMulticastLock.setReferenceCounted(true);
		mWifiMulticastLock.acquire();
	}

	private void releaseMulticastLock() {
		if (null != mWifiMulticastLock && mWifiMulticastLock.isHeld()) {
			mWifiMulticastLock.release();
			mWifiMulticastLock = null;
		}
	}
}
