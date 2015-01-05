package com.example.rockscissorspaper.connect;

import com.example.rockscissorspaper.Global;
import com.example.rockscissorspaper.R;
import com.example.rockscissorspaper.WelcomeActivity;
import com.example.rockscissorspaper.game.MainGame;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

public abstract class WifiConnectActivity extends Activity implements DialogInterface.OnCancelListener{

	private static WiFiService wiFiService;
	private boolean isActive = true;
	
	private Global global;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if(!wifiManager.isWifiEnabled()) {
			wifiManager.setWifiEnabled(true);
		}
		
		global = Global.getInstance();
    }
	
	protected abstract String getSpecificInfoString();
	
	protected void connect(WiFiService service, final int timeoutSecond) {
		wiFiService = service;
		
		final ProgressDialog dialog = ProgressDialog.show(this, "", getSpecificInfoString(), true, true, this);
		final Intent intent = new Intent(this, MainGame.class);
		
		final Activity thisActivity = this;
		Thread thread = new Thread(new Runnable() {
			
			public void run() {
				wiFiService.connect();
	    		
				int trialsCounter = 0;
				int COUNTER_MAX = timeoutSecond * 2;
				while(!wiFiService.isConnected() && trialsCounter < COUNTER_MAX && isActive) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					trialsCounter++;
				}
				
				dialog.dismiss();
				if(trialsCounter < COUNTER_MAX && isActive){
					global.remoteService = wiFiService;
					startActivity(intent);
				}
				else if(isActive){
					wiFiService.stop();
					Looper.prepare();
					Toast.makeText(getApplicationContext(), R.string.wifi_connect_time_out, Toast.LENGTH_SHORT).show();
					Looper.loop();
					thisActivity.finish();
				}
			}
		});
		
		thread.start();
	}
	
	@Override
    protected void onStop() {
    	super.onStop();
    	
    	this.finish();
    }
	
	public void onCancel(DialogInterface dialog) {
		if(wiFiService != null) wiFiService.stop();
		isActive = false;
		System.out.println("connection canceled");
		this.finish();
	}
}
