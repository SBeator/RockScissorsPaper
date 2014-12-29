package com.example.rockscissorspaper;

import com.example.rockscissorspaper.connect.RemoteService;

public class Global {

	private static Global instance = null;

	public static Global getInstance() {
		if (instance == null)
			instance = new Global();

		return instance;
	}
	
	public final int WIFI_PORT = 57419;
	
	public RemoteService remoteService;
}
