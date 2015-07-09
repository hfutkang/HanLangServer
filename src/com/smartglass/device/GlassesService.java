package com.smartglass.device;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.ingenic.glass.api.sync.SyncChannel;
import com.ingenic.glass.api.sync.SyncChannel.CONNECTION_STATE;
import com.ingenic.glass.api.sync.SyncChannel.Packet;
import com.ingenic.glass.api.sync.SyncChannel.RESULT;
import com.ingenic.glass.api.sync.SyncChannel.onChannelListener;
import com.smartglass.device.WifiAdmin.WifiCipherType;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class GlassesService extends Service {

	private final static String TAG = "GlassesService";
	
	public final static int CONNET_WIFI_MSG = 1;
	public final static int SET_PHOTO_PIXEL = 2;
	public final static int SET_VEDIO_DURATION = 4;
	public final static int SET_VOLUME = 8;
	public final static int SET_WIFI_AP = 11;
	public final static int SWITCH_ROUND_VIDEO = 12;
	public final static int GET_POWER_LEVEL = 13;
	public final static int GET_STORAGE_STATE = 14;
	
	private WifiAdmin mWifiAdmin;
	private SyncChannel mCmdChannel;
	private SharedPreferences mPreferences;
	private AudioManager mAudioManager;
	private ConnectivityManager mConnectivityManager;
	
	private SyncChannel mNotifyChannel;
	private GlassNotifyBroadcastReceiver mGlassNotifyBroadcastReceiver;
	
	private static final String[] lables = { "pixel", 
		"pixel", 
		"pixel", 
		"duration", 
		"sw", "sw", "sw", 
		"volume", "ssid", "pw", "NULL", "sw" };
	
	private static final String[] keys = { "NULL", "photo_pixel", 
		"vedio_pixel", 
		"duration", 
		"default_switch", 
		"anti_shake", 
		"timestamp" };
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		mWifiAdmin = new WifiAdmin(this);
		mCmdChannel = SyncChannel.create("00e04c68229b0", this, mOnCmdChannelListener);
		mNotifyChannel = SyncChannel.create("00e04c68229b1", this, mOnNotifyChannelListener);
		
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		mAudioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
		
		mConnectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_BATTERY_LOW);
		filter.addAction("ACTION_LOW_STORAGE");
		mGlassNotifyBroadcastReceiver = new GlassNotifyBroadcastReceiver(this, mNotifyChannel);
		registerReceiver(mGlassNotifyBroadcastReceiver, filter);

	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.e(TAG, "onStartCommand");
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.e(TAG, "GlassesService onDestroy");
		unregisterReceiver(mGlassNotifyBroadcastReceiver);
		super.onDestroy();
	}
	private SyncChannel.onChannelListener mOnCmdChannelListener = new onChannelListener() {
		
		@Override
		public void onStateChanged(CONNECTION_STATE arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onSendCompleted(RESULT arg0, Packet arg1) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onReceive(RESULT arg0, Packet data) {
			// TODO Auto-generated method stub			
			
			int type = data.getInt("type");
			if(GlassApplication.DEBUG) Log.d(TAG, "--- receive type:" + type);
			
			String sValue = "";
			boolean bvalue;
			Packet pk = mCmdChannel.createPacket();
			Editor editor = mPreferences.edit();
			
			switch (type) {
				case CONNET_WIFI_MSG:
					
					String ssid = data.getString("ssid");
					String pw = data.getString("pw");
					Log.e(TAG, "ssid:" + ssid + " pw:" + pw);
					if(isWifiConnected(ssid)) {
						String ip = Utils.getLocalIpAddress();
						pk.putString("ip", "" + ip);
					}
					else {
						mWifiAdmin.connect(ssid, pw, WifiCipherType.WIFICIPHER_WPA);
						IntentFilter filter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
						registerReceiver(glassStateBroadcastReceiver, filter);
						return;
					}
					break;
				case SET_WIFI_AP:
					String sid = data.getString("ssid");
					String spw = data.getString("pw");
					Log.e(TAG, sid + " " + spw);
					mWifiAdmin.connect(sid, spw, WifiCipherType.WIFICIPHER_WPA);
					
					pk.putInt("type", SET_WIFI_AP);
					pk.putString("ssid", sid);
					pk.putString("pw", spw);
					break;
				// case SET_PHOTO_PIXEL:
				// 	sValue = data.getString(lables[type-1]);
				// 	pk.putInt("type", type);
				// 	pk.putString(lables[type-1], sValue);
					
				// 	editor.putString(keys[type-1], sValue);
				// 	editor.commit();
				// 	break;
				case SET_VEDIO_DURATION:
					sValue = data.getString(lables[type-1]);
					pk.putInt("type", type);
					pk.putString(lables[type-1], sValue);
					Intent i = new Intent("com.ingenic.glass.camera.other.subsection");
					i.putExtra("value",sValue);
					i.setPackage("com.ingenic.glass.camera");
					sendBroadcast(i);
					break;
				case SWITCH_ROUND_VIDEO:
					bvalue = data.getBoolean(lables[type-1]);
					pk.putInt("type", type);
					pk.putBoolean(lables[type - 1], bvalue);
					
					Intent in = new Intent("com.ingenic.glass.camera.other.storage_mode");
					in.putExtra("value",bvalue);
					in.setPackage("com.ingenic.glass.camera");
					sendBroadcast(in);
					break;
				case SET_VOLUME:
					int volume = data.getInt(lables[type-1]);
					mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, volume, 0);
					
					pk.putInt("type", SET_VOLUME);
					pk.putInt("volume", volume);
					break;
				case GET_POWER_LEVEL:
					Log.e(TAG, "Get Power");
					IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
					registerReceiver(glassStateBroadcastReceiver, filter);
					break;
				case GET_STORAGE_STATE:
					String availableSize = Utils.getAvailableInternalMemorySizeString();
					String totalString = Utils.getTotalInternalMemorySize();
					pk.putString("available", availableSize);
					pk.putString("total", totalString);
					pk.putInt("type", GET_STORAGE_STATE);
					mCmdChannel.sendPacket(pk);
					break;
				default:
					return;
			}
			mCmdChannel.sendPacket(pk);
		}
	};
	
	private onChannelListener mOnNotifyChannelListener = new onChannelListener() {
		
		@Override
		public void onStateChanged(CONNECTION_STATE arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onSendCompleted(RESULT arg0, Packet arg1) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onReceive(RESULT arg0, Packet arg1) {
			// TODO Auto-generated method stub
			
		}
	};

	private boolean isWifiConnected(String ssid) {
		
		NetworkInfo netinfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		String extraInfo = "" + netinfo.getExtraInfo();
		if(netinfo.isConnected()&&extraInfo.contains(ssid)) 
			return true;
		return false;
	}
	private BroadcastReceiver glassStateBroadcastReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			Log.e(TAG, action);
			if(Intent.ACTION_BATTERY_CHANGED.equals(action)) {
				int level = intent.getIntExtra("level", 0);
				int scale = intent.getIntExtra("scale", 100);
				int percentage = level*100/scale;
				
				Packet pk = mCmdChannel.createPacket();
				pk.putInt("type", GET_POWER_LEVEL);
				pk.putInt("power", percentage);
				mCmdChannel.sendPacket(pk);
				unregisterReceiver(glassStateBroadcastReceiver);
			}
			else if(WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
				NetworkInfo ni = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				if(ni.isConnected()) {
					try {
						String ip = Utils.getLocalIpAddress();
						Packet pk = mCmdChannel.createPacket();
						pk.putString("ip", ip);
						mCmdChannel.sendPacket(pk);
						Log.e(TAG, Utils.getLocalIpAddress());
						unregisterReceiver(glassStateBroadcastReceiver);
					} catch (Exception e) {
						e.printStackTrace();
						WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
						mWifiManager.reconnect();
					}
				}
			}
		}
	};

}
