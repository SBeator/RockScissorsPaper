package com.example.rockscissorspaper.connect;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rockscissorspaper.R;

public class WifiHostConnectActivity extends WifiConnectActivity {
	
	private TextView ipTextView;
	private Button startServerButton;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_wifi_host);
        
        ipTextView = (TextView) findViewById(R.id.host_ip);
        startServerButton = (Button) findViewById(R.id.create_game);

    	setIP(WiFiService.getHostAdress(this));
		// startServerButton.setEnabled(!ipTextView.getText().equals(getString(R.string.defaut_ip)));
    }
	
	public void onClickRefresh(View view){
		setIP(WiFiService.getHostAdress(this));
    }
	
	public void onClickCreateWifiGame(View view){
		start();
    }

	@Override
	public String getSpecificInfoString() {
		return getString(R.string.wifi_host_waiting);
	}
	
    private void setIP(String ip) {
    	if(ipTextView == null)
    		return;
    	
    	if(ip.equals("0.0.0.0"))
    		ipTextView.setText(this.getString(R.string.defaut_ip));
    	else {
    		startServerButton.setEnabled(true);
    		ipTextView.setText(ip);
    	}
    }

    private void start() {
		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		
		if(!wifiManager.isWifiEnabled()) {
			wifiManager.reconnect();
		}
		
		connect(new WifiHostService(), 10);
    }
}
