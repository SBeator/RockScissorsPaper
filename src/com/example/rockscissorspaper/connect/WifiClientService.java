package com.example.rockscissorspaper.connect;

import java.io.IOException;
import java.net.Socket;

import com.example.rockscissorspaper.Global;

public class WifiClientService extends WiFiService {

	private String serverIP;
	
	public WifiClientService(String serverIP) {
		super();
		this.serverIP = serverIP;
	}
	
	@Override
	public Socket connectSpecific() {
		if(this.serverIP == null)
			return null;
		
		Socket socket = null;
		try {
			System.out.println("WifiClientService.connectSpecific() start, address:" + this.serverIP + ":" + Global.getInstance().WIFI_PORT);
			socket = new Socket(this.serverIP, Global.getInstance().WIFI_PORT);
			System.out.println("WifiClientService.connectSpecific() success, address:" + socket.getRemoteSocketAddress());

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return socket;
	}

	@Override
	public void cancelSpecific() {
		try {
			if(super.socket != null)
				super.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
