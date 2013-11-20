/**
  * Digital Ligter
  * Customer Driven Project - NTNU
  * 20th November  2013
  *
  * @author Jan Bednarik
  * @author Tomas Dohnalek
  * @author Milos Jovac
  * @author Agnethe Soraa
  */

package com.silentducks.dns;

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
