/**
 * CopyRight (C), 2013
 */

package com.example.jmdns;

public interface ZeroServiceDiscoverListener {
	public void onDiscoveryStarted(String type);

	public void onServiceFound(ZeroServiceInfo zsi);

	public void onServiceLost(ZeroServiceInfo zsi);

	public void onDiscoverStopped(String type);

	public void onStartDiscoveryFailed(String type, String error);

	public void onStopDiscoveryFailed(String type, String error);
}
