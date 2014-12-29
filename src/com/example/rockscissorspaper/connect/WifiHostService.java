package com.example.rockscissorspaper.connect;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.example.rockscissorspaper.Global;

public class WifiHostService extends WiFiService {
	
	private ServerSocket serverSocket;
	
	public WifiHostService() {
		super();
	}

	@Override
	public Socket connectSpecific() {
		Socket socket = null;
		try {
			serverSocket = new ServerSocket(Global.getInstance().WIFI_PORT);
			socket = serverSocket.accept();
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return socket;
	}

	@Override
	public void cancelSpecific() {
		try {
			if(serverSocket != null)
				serverSocket.close();
			if(super.socket != null)
				super.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
