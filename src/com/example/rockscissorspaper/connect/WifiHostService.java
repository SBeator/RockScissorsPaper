package com.example.rockscissorspaper.connect;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.widget.Toast;

import com.example.rockscissorspaper.Global;
import com.example.rockscissorspaper.R;

public class WifiHostService extends WiFiService {
	
	private ServerSocket serverSocket;
	
	public WifiHostService() {
		super();
	}

	@Override
	public Socket connectSpecific() {
		Socket socket = null;
		try {
			System.out.println("WifiHostService.connectSpecific() start, port:" + Global.getInstance().WIFI_PORT);
			serverSocket = new ServerSocket(Global.getInstance().WIFI_PORT);
			socket = serverSocket.accept();
			
			System.out.println("WifiHostService.connectSpecific() accepted, address:" + socket.getRemoteSocketAddress().toString());
			
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
