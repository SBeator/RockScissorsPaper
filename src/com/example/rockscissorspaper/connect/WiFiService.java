package com.example.rockscissorspaper.connect;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.LinkedList;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

public abstract class WiFiService implements RemoteService {
	
	protected final int helloMessage = 1000;
	
	protected Socket socket = null;
	
	private boolean isConnected = false;
	private LinkedList<ConnectPacket> toSendQueue;
	private LinkedList<ConnectPacket> receivedQueue;
	private SendingThread sendingThread;
	private ReceivingThread receivingThread;
	private ConnectingThread connectingThread;
	
	public WiFiService() {
        toSendQueue = new LinkedList<ConnectPacket>();
        receivedQueue = new LinkedList<ConnectPacket>();
        
        sendingThread = new SendingThread();
        receivingThread = new ReceivingThread();
        connectingThread = new ConnectingThread();
	}
	
	abstract public Socket connectSpecific();

	abstract public void cancelSpecific();
	
	public void connect() {
		if(!connectingThread.isAlive()) connectingThread.start();
	}
	
	public void stop() {
		while(sendingThread.isAlive() || receivingThread.isAlive() || connectingThread.isAlive()) {
			try {
				Thread.sleep(550);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if(sendingThread != null && sendingThread.isAlive()) sendingThread.cancel();
			if(receivingThread != null && receivingThread.isAlive()) receivingThread.cancel();
			if(connectingThread != null && connectingThread.isAlive()) connectingThread.cancel();
		}
	}
	
	public boolean isConnected() {
		if(socket != null)
			return socket.isConnected();
		else
			return false;
	}
	
	public void send(ConnectPacket gamePacket) {
		toSendQueue.offer(gamePacket);
	}
	
	public ConnectPacket receive() {
		if(!receivedQueue.isEmpty())
			return receivedQueue.poll();
		else
			return null;
	}

	public static String getHostAdress(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifiManager.getConnectionInfo();
		int ip = info.getIpAddress();
		@SuppressWarnings("deprecation")
		String ipText = Formatter.formatIpAddress(ip);
		
		return ipText;
	}

	class SendingThread extends Thread {
		private boolean isAlive = false;
		private OutputStream outputStream;
		private ObjectOutputStream objectOutputStream;
		private OutputStreamWriter outputStreamWriter;
		
		@Override
		public void run() {
			isAlive = true;
			try {
				while(!isConnected && isAlive)
					Thread.sleep(250);
				
				if(isAlive) {
					outputStream = socket.getOutputStream();
					objectOutputStream = new ObjectOutputStream(outputStream);
				}
				ConnectPacket packet;
			
				while(isAlive) {
					if(!toSendQueue.isEmpty()) {
						packet = toSendQueue.poll();
						objectOutputStream.writeObject(packet);
						objectOutputStream.flush();
					}
					
					Thread.sleep(500);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				isAlive = false;
			}
		}
		
		public void cancel() {
			isAlive = false;
			try {
				if(outputStream != null) outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	class ReceivingThread extends Thread {
		private boolean isAlive = false;
		private InputStream inputStream;
		private ObjectInputStream objectInputStream;
		private InputStreamReader inputStreamReader;
		
		@Override
		public void run() {
			isAlive = true;
			try {
				while(!isConnected && isAlive)
					Thread.sleep(250);

				if(isAlive) {
					inputStream = socket.getInputStream();
					objectInputStream = new ObjectInputStream(inputStream);
				}
				ConnectPacket packet = null;
				
				while(isAlive) {
					
					packet = (ConnectPacket) objectInputStream.readObject();
					
					if(packet != null)
						receivedQueue.offer(packet);
					
					Thread.sleep(500);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} finally {
				isAlive = false;
			}
			
		}
		
		public void cancel() {
			isAlive = false;
			try {
				if(inputStream != null) inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	class ConnectingThread extends Thread {
		private boolean isAlive = false;
		
		@Override
		public void run() {
			isAlive = true;
			while(!isConnected && isAlive) {
				socket = connectSpecific();	// can block
				
				if(socket != null) {
					try {
						InputStream input = socket.getInputStream();
						OutputStream output = socket.getOutputStream();
						
						output.write(helloMessage);	// send your DEVICE_TYPE
						
						int hello = input.read();
						if(hello == helloMessage) {
							isAlive = true;
						}
						
					} catch (IOException e1) {
						try {
							socket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						socket = null;
						e1.printStackTrace();
					}
				}
				
				isConnected = socket != null;
				
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			if(!sendingThread.isAlive() && isAlive) sendingThread.start();
			if(!receivingThread.isAlive() && isAlive) receivingThread.start();
		}
		
		public void cancel() {
			cancelSpecific();
			isAlive = false;
		}
	}

}
