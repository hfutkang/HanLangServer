package com.smartglass.device;

import com.ingenic.glass.api.sync.SyncChannel;
import com.ingenic.glass.api.sync.SyncChannel.Packet;
import com.smartglass.smartglassesledtest.LedOperation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class GlassNotifyBroadcastReceiver extends BroadcastReceiver {

	private final static String TAG = "GlassNotifyBroadcastReceiver";
	private SyncChannel mChannel;
	private Context mContext;
	private LedOperation mLedOperation;
	
	private final static int MSG_TYPE_LOW_POWER = 2;
	private final static int MSG_TYPE_PHONE = 1;
	private final static int MSG_TYPE_LOW_STORAGE = 3;
	private final static int FAST_BLINK_RATE = 2;
	
	private final static String B_SHORT_PRESSED_ACTION = "ACTION_KEY_B_SHORT_PRESSED";
	private final static String LOW_STORAGE_ACTION = "ACTION_LOW_STORAGE";
	
	public GlassNotifyBroadcastReceiver(Context context, SyncChannel channle) {
		mChannel = channle;
		mContext = context;
		mLedOperation = new LedOperation();
	}
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		Log.e(TAG, "" + action);
		if(Intent.ACTION_BATTERY_LOW.equals(action)) {
			Log.e(TAG, "Low Power");
			sendLowPowerMsg();
			mLedOperation.SetRedBlinkRate(FAST_BLINK_RATE);
			mLedOperation.TurnRedLightBlinkOn();
		}
		else if(LOW_STORAGE_ACTION.equals(action)) {
			sendLowStorageMsg();
		}
	}
	
	private void sendLowStorageMsg() {
		Packet pk = mChannel.createPacket();
		pk.putInt("type", MSG_TYPE_LOW_STORAGE);
		pk.putBoolean("low", true);
		mChannel.sendPacket(pk);
	}
	
	private void sendLowPowerMsg() {
		Packet pk = mChannel.createPacket();
		pk.putInt("type", MSG_TYPE_LOW_POWER);
		pk.putBoolean("low", true);
		mChannel.sendPacket(pk);
	}

}
