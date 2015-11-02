package com.smartglass.device;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Currency;

import com.ingenic.glass.api.sync.SyncChannel;
import com.ingenic.glass.api.sync.SyncChannel.Packet;
import com.smartglass.smartglassesledtest.LedOperation;
import com.ingenic.glass.voicerecognizer.api.VoiceRecognizer;

import android.hardware.usb.UsbManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RecoverySystem;
import android.preference.PreferenceManager;
import android.util.Log;

public class GlassReceiver extends BroadcastReceiver {

	private final static String TAG = "GlassNotifyBroadcastReceiver";
	private SyncChannel mChannel;
	private Context mContext;
	private LedOperation mLedOperation;
	private boolean mPowerConnected = false;
	private boolean mVideoRecording = false;
        private boolean mBatteryFull = false;
	private VoiceRecognizer mVoiceRecognizer = null;
	
	private long videoRecorderStartTime = 0;
	private int currentPowerPercentage = 0;

	private final static int MSG_TYPE_LOW_POWER = 2;
	private static final int MSG_CLOSE_RED_LIGHT = 1;
	
	private final static String LOW_STORAGE_ACTION = "ACTION_LOW_STORAGE";
	
	public GlassReceiver(Context context, SyncChannel channle) {
		mChannel = channle;
		mContext = context;
		mLedOperation = new LedOperation();
		mVoiceRecognizer = new VoiceRecognizer(VoiceRecognizer.REC_TYPE_COMMAND, null);
		mVoiceRecognizer.setAppName("GlassReceiver");
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		if(GlassApplication.DEBUG) Log.d(TAG, "----action:"+intent.getAction());
		if(Intent.ACTION_BATTERY_LOW.equals(action)) {
			Log.w(TAG, "Low Power");
			mLedOperation.SetRedBlinkRate(0.3f);
			mLedOperation.TurnRedLightBlinkOn();
			sendLowPowerMsg();
			mVoiceRecognizer.playTTS(mContext.getString(R.string.tts_low_battery));
	        }else if (intent.getAction().equals(UsbManager.ACTION_USB_STATE)) {
	                boolean connected = intent.getBooleanExtra(UsbManager.USB_CONNECTED, false);    
			Log.d(TAG, "connected = "+connected);
			mPowerConnected = connected;
	        }else if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
			int status = intent.getIntExtra("status",BatteryManager.BATTERY_STATUS_UNKNOWN);
			int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
			
			int level = intent.getIntExtra("level", 0);
			int scale = intent.getIntExtra("scale", 100);
			currentPowerPercentage = level*100/scale;
			
			if (status == BatteryManager.BATTERY_STATUS_FULL && mPowerConnected) {
			    mLedOperation.TurnRedLightOff();
			    if(mVideoRecording){
				Log.d(TAG,"set mBatteryFull true");
				mBatteryFull = true;
				return;
			    }
			    mLedOperation.TurnGreenLightOn();
			}else if(status == BatteryManager.BATTERY_STATUS_CHARGING){
				Log.d(TAG,"set mBatteryFull false");
			    mBatteryFull = false;
			    mLedOperation.TurnRedLightOn();
			}
		}
		else if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			mLedOperation.TurnGreenLightOff();
			mLedOperation.TurnRedLightOn();
			mHandler.sendEmptyMessageDelayed(MSG_CLOSE_RED_LIGHT, 5000);
			checkUpdateState();
			
		}else if(intent.getAction().equals("cn.ingenic.glass.ACTION_MEDIA_VIDEO_START")) {
			mVideoRecording = true;
			mLedOperation.SetGreenBlinkRate(1);
			mLedOperation.TurnGreenLightBlinkOn();
			videoRecorderStartTime = System.currentTimeMillis();

	        }else if(intent.getAction().equals("cn.ingenic.glass.ACTION_MEDIA_VIDEO_FINISH")) {
			mVideoRecording = false;
			mLedOperation.TurnGreenLightBlinkOff();
			
			Log.d(TAG,"mBatteryFull is "+mBatteryFull);
			if(mBatteryFull)
			    mLedOperation.TurnGreenLightOn();

			videoRecorderStartTime = 0;

	        }else if(intent.getAction().endsWith(Intent.ACTION_POWER_CONNECTED)) {
			mLedOperation.TurnRedLightBlinkOff();
			mLedOperation.TurnRedLightOn();
			mPowerConnected = true;

		}else if(intent.getAction().endsWith(Intent.ACTION_POWER_DISCONNECTED)) {
			mLedOperation.TurnRedLightOff();

			if(!mVideoRecording)
				mLedOperation.TurnGreenLightOff();
			
			mPowerConnected = false;
		}
		else if("INSTALL_UPDATE_PACKAGE".equals(action)) {
			String path = intent.getStringExtra("PackageFileName");
			final File file = new File(path);
			if(currentPowerPercentage > 30 || mPowerConnected) {
				try {
					if (file.exists()) {
						
						if(mChannel.isConnected()) {
							Packet pk = mChannel.createPacket();
							pk.putInt("type", GlassesService.REPORT_UPDATE_STATE);
							pk.putInt("state", GlassesService.UPDATE_START);
							mChannel.sendPacket(pk);
					}
						
					SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(mContext);
					Editor editor = preference.edit();
					editor.putInt("last_update_state", GlassesService.UPDATE_START);
					editor.commit();
					
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							try {
								RecoverySystem.installPackage(mContext, file);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}).start();
					
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				reportUpdateState(GlassesService.UPDATE_POWER_SHORTAGE);
			}
		}
		
	}
	
	public long getVideoRecorderStartTime() {
		return videoRecorderStartTime;
	}
	private void sendLowPowerMsg() {
		Packet pk = mChannel.createPacket();
		pk.putInt("type", MSG_TYPE_LOW_POWER);
		pk.putBoolean("low", true);
		mChannel.sendPacket(pk);
	}
	
	public int getCurrentPowerPercentage()	 {
		return currentPowerPercentage;
	}

	private Handler mHandler = new Handler() {
        @Override
	    public void handleMessage(Message msg) {
	    if(GlassApplication.DEBUG) Log.d(TAG,"handleMessage in msg.what="+msg.what);
            switch (msg.what) {
	    case MSG_CLOSE_RED_LIGHT: {
		if(mPowerConnected == false)
		    mLedOperation.TurnRedLightOff();
		break;
	    }
	    default:
		break;
            }
        }
	       };
	       
	private void checkUpdateState() {
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		int state = preferences.getInt("last_update_state", GlassesService.UPDATE_DEFAULT_STATE);
		String lastVersion = preferences.getString("last_version_name", "");
		
		try {
			if(state == GlassesService.UPDATE_START) {
				Object object = new Object();
				Method method = Class.forName("android.os.SystemProperties").getMethod("get", String.class);
				String currentVersion = (String)method.invoke(object, new Object[]{"ro.fota.version"});
				
				if(lastVersion.equals(currentVersion)) {
					reportUpdateState(GlassesService.UPDATE_SUCCESS);
				}
				else {
					reportUpdateState(GlassesService.UPDATE_FAILE);
				}
				
				File file = new File(Environment.getExternalStorageDirectory().toString() + "/update.zip");
				if(file.exists())
					file.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	 }
	
	private void reportUpdateState(int state) {
		
		SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(mContext);
		int newState = state;
		
		if(mChannel.isConnected()) {
				Packet pk = mChannel.createPacket();
				pk.putInt("type", GlassesService.REPORT_UPDATE_STATE);
				pk.putInt("state", state);
				mChannel.sendPacket(pk);
				
				newState = GlassesService.UPDATE_DEFAULT_STATE;
		}
		
		Editor editor = preference.edit();
		editor.putInt("last_update_state", newState);
		editor.commit();
}
}
