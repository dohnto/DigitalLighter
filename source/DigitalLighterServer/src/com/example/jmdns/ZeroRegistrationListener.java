/**
 * CopyRight (C), 2013
 */

package com.example.jmdns;

public interface ZeroRegistrationListener {
	public void onServiceRegistered(ZeroServiceInfo zsi);

	public void onServiceRegisteredFailed(ZeroServiceInfo zsi);

	public void onServiceUnregistered(ZeroServiceInfo zsi);

	public void onServiceUnregisteredFailed(ZeroServiceInfo zsi);
}
