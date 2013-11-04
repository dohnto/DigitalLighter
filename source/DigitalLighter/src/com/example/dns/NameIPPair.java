package com.example.dns;

import java.net.InetAddress;

public class NameIPPair {

	public String name;
	public InetAddress ipAddress;
	public int port;

	public NameIPPair(String name, InetAddress ip, int port) {
		this.name = name;
		this.ipAddress = ip;
		this.port = port;
	}

}
