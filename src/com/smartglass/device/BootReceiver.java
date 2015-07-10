package com.smartglass.device;

import com.smartglass.smartglassesledtest.LedOperation;
import android.os.Handler;
import android.os.Message;
import android.os.BatteryManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

	private final static String TAG = "BootReceiver";
	private static final int MSG_CLOSE_RED_LIGHT = 1;
	private LedOperation mLedOperation = new LedOperation();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if(GlassApplication.DEBUG) Log.d(TAG, "----action:"+intent.getAction());
		if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			mLedOperation.TurnGreenLightOff();
			mLedOperation.TurnRedLightOn();
			mHandler.sendEmptyMessageDelayed(MSG_CLOSE_RED_LIGHT, 5000);
			
		}else if(intent.getAction().equals("cn.ingenic.glass.ACTION_MEDIA_VIDEO_START")) {
			mLedOperation.SetGreenBlinkRate(1);
			mLedOperation.TurnGreenLightBlinkOn();

	        }else if(intent.getAction().equals("cn.ingenic.glass.ACTION_MEDIA_VIDEO_FINISH")) {
			mLedOperation.TurnGreenLightBlinkOff();

	        }else if(intent.getAction().equals(Intent.ACTION_BATTERY_LOW)) {
			mLedOperation.SetRedBlinkRate(2);
			mLedOperation.TurnRedLightBlinkOn();

		}else if(intent.getAction().endsWith(Intent.ACTION_POWER_CONNECTED)) {
			mLedOperation.TurnRedLightBlinkOff();
			mLedOperation.TurnRedLightOn();

		}else if(intent.getAction().endsWith(Intent.ACTION_POWER_DISCONNECTED)) {
			mLedOperation.TurnGreenLightOff();

		}else if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
			int status = intent.getIntExtra("status",BatteryManager.BATTERY_STATUS_UNKNOWN);
			if (status == BatteryManager.BATTERY_STATUS_FULL) {
				mLedOperation.TurnRedLightOff();
				mLedOperation.TurnGreenLightOn();
			}
		}
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
