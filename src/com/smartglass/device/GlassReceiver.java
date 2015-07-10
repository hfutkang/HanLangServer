package com.smartglass.device;

import com.ingenic.glass.api.sync.SyncChannel;
import com.ingenic.glass.api.sync.SyncChannel.Packet;
import com.smartglass.smartglassesledtest.LedOperation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class GlassReceiver extends BroadcastReceiver {

	private final static String TAG = "GlassNotifyBroadcastReceiver";
	private SyncChannel mChannel;
	private Context mContext;
	private LedOperation mLedOperation;
	
	private final static int MSG_TYPE_LOW_POWER = 2;
	private static final int MSG_CLOSE_RED_LIGHT = 1;
	
	private final static String LOW_STORAGE_ACTION = "ACTION_LOW_STORAGE";
	
	public GlassReceiver(Context context, SyncChannel channle) {
		mChannel = channle;
		mContext = context;
		mLedOperation = new LedOperation();
	}
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		if(GlassApplication.DEBUG) Log.d(TAG, "----action:"+intent.getAction());
		if(Intent.ACTION_BATTERY_LOW.equals(action)) {
			Log.e(TAG, "Low Power");
			mLedOperation.SetRedBlinkRate(2);
			mLedOperation.TurnRedLightBlinkOn();
			sendLowPowerMsg();
		}
		else if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
			int status = intent.getIntExtra("status",BatteryManager.BATTERY_STATUS_UNKNOWN);
			if (status == BatteryManager.BATTERY_STATUS_FULL) {
				mLedOperation.TurnRedLightOff();
				mLedOperation.TurnGreenLightOn();
			}
		}
		else if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			mLedOperation.TurnGreenLightOff();
			mLedOperation.TurnRedLightOn();
			mHandler.sendEmptyMessageDelayed(MSG_CLOSE_RED_LIGHT, 5000);
			
		}else if(intent.getAction().equals("cn.ingenic.glass.ACTION_MEDIA_VIDEO_START")) {
			mLedOperation.SetGreenBlinkRate(1);
			mLedOperation.TurnGreenLightBlinkOn();

	        }else if(intent.getAction().equals("cn.ingenic.glass.ACTION_MEDIA_VIDEO_FINISH")) {
			mLedOperation.TurnGreenLightBlinkOff();

	        }else if(intent.getAction().endsWith(Intent.ACTION_POWER_CONNECTED)) {
			mLedOperation.TurnRedLightBlinkOff();
			mLedOperation.TurnRedLightOn();

		}else if(intent.getAction().endsWith(Intent.ACTION_POWER_DISCONNECTED)) {
			mLedOperation.TurnGreenLightOff();

		}
		
	}
	
	private void sendLowPowerMsg() {
		Packet pk = mChannel.createPacket();
		pk.putInt("type", MSG_TYPE_LOW_POWER);
		pk.putBoolean("low", true);
		mChannel.sendPacket(pk);
	}

	private Handler mHandler = new Handler() {
        @Override
	    public void handleMessage(Message msg) {
	    if(GlassApplication.DEBUG) Log.d(TAG,"handleMessage in msg.what="+msg.what);
            switch (msg.what) {
	    case MSG_CLOSE_RED_LIGHT: {
		mLedOperation.TurnRedLightOff();
		break;
	    }
	    default:
		break;
            }
        }
	       };
}
