package com.example.rockscissorspaper.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.example.rockscissorspaper.Global;
import com.example.rockscissorspaper.R;
import com.example.rockscissorspaper.connect.ConnectPacket;
import com.example.rockscissorspaper.connect.RemoteService;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainGame extends Activity {

	private static HashMap<Integer, Integer> choiseNamesMap;
	private static HashMap<Integer, Integer> resultsMap;
	
	private Global global;
	
	private List<RadioButton> radioList;
	 
	
	public MainGame(){
        choiseNamesMap = new HashMap<Integer, Integer>();
        choiseNamesMap.put(0, R.string.rock);
        choiseNamesMap.put(1, R.string.scissors);
        choiseNamesMap.put(2, R.string.paper);
        
        resultsMap = new HashMap<Integer, Integer>();
        resultsMap.put(0, R.string.result_draw);    
        resultsMap.put(1, R.string.result_lose);  
        resultsMap.put(2, R.string.result_win);  
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main_game);
        
        this.radioList = new ArrayList<RadioButton>();
        this.radioList.add((RadioButton) this.findViewById(R.id.radioButton1));
        this.radioList.add((RadioButton) this.findViewById(R.id.radioButton2));
        this.radioList.add((RadioButton) this.findViewById(R.id.radioButton3));
        
        this.global = Global.getInstance();
    }
	
	public void onClickOk(View view){
		
		int choiseIndex = -1;
		for(int i=0; i<this.radioList.size(); i++){
			if(this.radioList.get(i).isChecked()){
				choiseIndex = i;
				break;
			}
		}
		
		if(choiseIndex == -1){
			Toast.makeText(this, R.string.no_choose_warning, Toast.LENGTH_LONG).show();
			return;
		}
		
		final ProgressDialog dialog = ProgressDialog.show(this, "", this.getText(R.string.result_waitting), true, true);
		final MainGame thisActivity = this;
		final int myChoise = choiseIndex;
		
		Thread thread = new Thread(new Runnable() {
			
			public void run() {
				ConnectPacket gamePacket = new ConnectPacket();
				gamePacket.choiseIndex = myChoise;
				
				RemoteService remoteService = thisActivity.global.remoteService;
				remoteService.send(gamePacket);
				
				boolean success = false;
				int trialsCounter = 0;
				int COUNTER_MAX = 20;
				while(remoteService.isConnected() && trialsCounter < COUNTER_MAX){
					ConnectPacket otherGamePacket = thisActivity.global.remoteService.receive();
					
					if(otherGamePacket != null){
						dialog.dismiss();
						
						Looper.prepare();
						Toast.makeText(getApplicationContext(), "my:" + myChoise + " other:" + otherGamePacket.choiseIndex, Toast.LENGTH_LONG).show();
						Looper.loop();
						
						success = true;
						break;
						
					}else{
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					trialsCounter++;
					System.out.println("TrialsCounter:" + trialsCounter + " isConnected:" + remoteService.isConnected());
				}
				
				if(!success){
					dialog.dismiss();
					
					Looper.prepare();
					if(!remoteService.isConnected()){
						Toast.makeText(getApplicationContext(), thisActivity.getText(R.string.connect_lose), Toast.LENGTH_LONG).show();
					}else{
						Toast.makeText(getApplicationContext(), thisActivity.getText(R.string.opponent_not_choise), Toast.LENGTH_LONG).show();
					}
					Looper.loop();
				}
			}
		});

		thread.start();
	}
}
