package com.smartglass.device;

import com.smartglass.smartglassesledtest.LedOperation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

	private final static String TAG = "BootReceiver";
	private LedOperation mLedOperation = new LedOperation();
	private static final float SLOW_BLINK_RATE = 1;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.e(TAG, intent.getAction());
		
		if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			mLedOperation.TurnGreenLightoff();
			mLedOperation.TurnRedLightOn();
			
		}else if(intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
			Log.e(TAG, "shut donw now");
			mLedOperation.SetRedBlinkRate(SLOW_BLINK_RATE);
			mLedOperation.TurnRedLightBlinkOn();
		}
		else if(intent.getAction().endsWith(Intent.ACTION_POWER_CONNECTED)) {
			mLedOperation.TurnRedLightOn();
		}
	}
}
