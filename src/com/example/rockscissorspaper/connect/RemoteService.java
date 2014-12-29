package com.example.rockscissorspaper.connect;

public interface RemoteService {

	public void connect();
	
	public void stop();
	
	public boolean isConnected();
	
	public void send(ConnectPacket gamePacket);

	public ConnectPacket receive();
}
