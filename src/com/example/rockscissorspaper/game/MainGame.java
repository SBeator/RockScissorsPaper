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
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
	
	private TextView resultLabel;
	private TextView result;
	private TextView myChoise;
	private TextView hisChoise;
	private TextView totalResultLabel;
	private TextView totalResult;
	
	private int gameCount;
	private int winCount;
	private int loseCount;
	private int drawCount;
	
	private Handler resultHandler;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main_game);
        
        choiseNamesMap = new HashMap<Integer, Integer>();
        choiseNamesMap.put(0, R.string.rock);
        choiseNamesMap.put(1, R.string.scissors);
        choiseNamesMap.put(2, R.string.paper);
        
        resultsMap = new HashMap<Integer, Integer>();
        resultsMap.put(0, R.string.result_draw);    
        resultsMap.put(1, R.string.result_lose);  
        resultsMap.put(2, R.string.result_win);  
        
        this.radioList = new ArrayList<RadioButton>();
        this.radioList.add((RadioButton) this.findViewById(R.id.radioButton1));
        this.radioList.add((RadioButton) this.findViewById(R.id.radioButton2));
        this.radioList.add((RadioButton) this.findViewById(R.id.radioButton3));
        
        this.resultLabel = (TextView) this.findViewById(R.id.result_label);
        this.result = (TextView) this.findViewById(R.id.result);
        this.myChoise = (TextView) this.findViewById(R.id.your_choise);
        this.hisChoise = (TextView) this.findViewById(R.id.his_choise);
        this.totalResultLabel = (TextView) this.findViewById(R.id.total_result_label);
        this.totalResult = (TextView) this.findViewById(R.id.total_result);
        
        this.gameCount = 0;
        this.drawCount = 0;
        this.winCount = 0;
        this.loseCount = 0;
        
        this.global = Global.getInstance();
        
        this.resultHandler = new ResultHandler();
        
        this.setResultVisibility(View.INVISIBLE);
    }
	
	public void onClickOk(View view){
		
		int choiseIndex = -1;
		for(int i=0; i<this.radioList.size(); i++){
			if(this.radioList.get(i).isChecked()){
				choiseIndex = i;
				break;
			}
		}
		
		if(choiseNamesMap.containsKey(choiseIndex)){
			punch(choiseIndex);
		}else{
			Toast.makeText(this, R.string.no_choose_warning, Toast.LENGTH_LONG).show();
			return;
		}
	}

	private void punch(int choiseIndex) {
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
					ConnectPacket hisGamePacket = thisActivity.global.remoteService.receive();
					
					if(hisGamePacket != null && choiseNamesMap.containsKey(hisGamePacket.choiseIndex)){
						dialog.dismiss();
						
						Message resultMessage = resultHandler.obtainMessage();
						resultMessage.arg1 = myChoise;
						resultMessage.arg2 = hisGamePacket.choiseIndex;
						resultMessage.sendToTarget();
						
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

	private void showResult(final int myChoise, final int hisChoise) {
		
		boolean resultRight = true;
		
		int winLoseFlag = (3 + myChoise - hisChoise) % 3;
		switch(winLoseFlag){
		case 0:
			this.drawCount++;
			break;
		case 1:
			this.loseCount++;
			break;
		case 2:
			this.winCount++;
			break;
		default:
			resultRight = false;
			break;
		}
		
		if(resultRight){
			this.gameCount++;
			String label = String.format(this.getString(R.string.result_label_format), this.gameCount);
			this.resultLabel.setText(label);
			
			this.result.setText(resultsMap.get(winLoseFlag));
			
			String myChoiseResult = String.format(
					this.getString(R.string.result_my_choise), 
					this.getString(choiseNamesMap.get(myChoise)));
			this.myChoise.setText(myChoiseResult);
			
			String hisChoiseResult = String.format(
					this.getString(R.string.result_opponent_choise), 
					this.getString(choiseNamesMap.get(hisChoise)));
			this.hisChoise.setText(hisChoiseResult);
			
			String totalResult = String.format(
					this.getString(R.string.total_result_format), 
					this.winCount,
					this.drawCount,
					this.loseCount);
			this.totalResult.setText(totalResult);

			this.setResultVisibility(View.VISIBLE);
		}else{
			this.setResultVisibility(View.INVISIBLE);
			this.result.setText(R.string.punch_error);
		}
	}
	
	private void setResultVisibility(int visibility){
		this.resultLabel.setVisibility(visibility);
		this.myChoise.setVisibility(visibility);
		this.hisChoise.setVisibility(visibility);
		this.totalResultLabel.setVisibility(visibility);
	}
	
	private class ResultHandler extends Handler{
		public void handleMessage (Message msg) {  
			showResult(msg.arg1, msg.arg2) ;
        } 
	}
}
