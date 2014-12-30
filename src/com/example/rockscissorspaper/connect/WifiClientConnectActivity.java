package com.example.rockscissorspaper.connect;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.rockscissorspaper.R;

public class WifiClientConnectActivity extends WifiConnectActivity{

	private EditText ipText;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_wifi_client);
        
        ipText = (EditText) findViewById(R.id.host_ip);
    }
	
	public void onJoinGame(View view){
		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		
		if(!wifiManager.isWifiEnabled()) {
			wifiManager.reconnect();
		}

		connect(new WifiClientService(ipText.getText().toString()), 10);
	}
	
	@Override
	protected String getSpecificInfoString() {
		return getString(R.string.connecting);
	}

}
