package com.smartglass.device;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.fota.iport.DownloadUtil;
import com.fota.iport.IOnDownloadListener;
import com.fota.iport.MobAgentPolicy;
import com.fota.iport.config.DownParamInfo;
import com.fota.iport.error.DownLoadError;
import com.fota.iport.service.DLService;
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
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StatFs;
import android.os.SystemClock;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;
import android.hardware.usb.UsbManager;

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
	public final static int WIFI_CONNECTED = 15;
	public final static int GET_UP_TIME = 16;
	public final static int GET_GLASS_INFO = 17;
	public final static int TURN_WIFI_OFF = 18;
	public final static int GET_STATE = 19;
	public final static int UPDATE_CONNECT_WIFI_MSG = 20;
	public final static int REPORT_UPDATE_STATE = 21;
	public final static int FACTORY_RESET = 22;
	
	public final static int CONNECT_WIFI_TIMEOUT = 1;
	
	public final static int UPDATE_DEFAULT_STATE = -1;
	public final static int UPDATE_TRY_CONNECT_WIFI = 0;
	public final static int UPDATE_CONNECTI_WIFI_TIMEOUT = 1;
	public final static int UPDATE_START_DOWNLOAD = 2;
	public final static int UPDATE_DOWNLOAD_ERROR = 3;
	public final static int UPDATE_INVALID_PACKAGE = 4;
	public final static int UPDATE_START = 5;
	public final static int UPDATE_SUCCESS = 6;
	public final static int UPDATE_FAILE = 7;
	public final static int UPDATE_POWER_SHORTAGE = 8;
	public final static int UPDATE_STORAGE_SHORTAGE = 9;
	
	private static final String CMD_CHANNEL_NAME = "cmdchannel";
	private static final String NTF_CHANNEL_NAME = "ntfchannel";
	
	private static final String HANLANG_FOTA_TOKE = "fb5c379aeed5277fdf4b89c797af1bcd";
	
	private WifiAdmin mWifiAdmin;
	private SharedPreferences mPreferences;
	private AudioManager mAudioManager;
	private ConnectivityManager mConnectivityManager;
	
	private SyncChannel mCmdChannel;
	private SyncChannel mNotifyChannel;
	
	private GlassReceiver mGlassNotifyBroadcastReceiver;
	
	private updateInfo mUpdateInfo;
	
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
		mCmdChannel = SyncChannel.create(CMD_CHANNEL_NAME, this, mOnCmdChannelListener);
		mNotifyChannel = SyncChannel.create(NTF_CHANNEL_NAME, this, mOnNotifyChannelListener);
		
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		mAudioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
		
		mConnectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
		
		mUpdateInfo = new updateInfo();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_BATTERY_LOW);
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		
		filter.addAction("cn.ingenic.glass.ACTION_MEDIA_VIDEO_START");
		filter.addAction("cn.ingenic.glass.ACTION_MEDIA_VIDEO_FINISH");
		filter.addAction(Intent.ACTION_BOOT_COMPLETED);
		filter.addAction(UsbManager.ACTION_USB_STATE);
		filter.addAction(Intent.ACTION_POWER_CONNECTED);
		filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
		filter.addAction("INSTALL_UPDATE_PACKAGE");
		mGlassNotifyBroadcastReceiver = new GlassReceiver(this, mNotifyChannel);
		registerReceiver(mGlassNotifyBroadcastReceiver, filter);
		
		MobAgentPolicy.initConfig(getApplicationContext());

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
			
			mUpdateInfo.update = false;
			
			switch (type) {
				case CONNET_WIFI_MSG:
					
					String ssid = data.getString("ssid");
//					String pw = data.getString("pw");
					String pw = "12345678";
					Log.e(TAG, "ssid:" + ssid + " pw:" + pw);
					if(isWifiConnected(ssid)) {
						String ip = Utils.getLocalIpAddress();
						pk.putInt("type", WIFI_CONNECTED);
						pk.putString("ip", "" + ip);
					}
					else {
						mWifiAdmin.connect(ssid, pw, WifiCipherType.WIFICIPHER_NOPASS);
						IntentFilter filter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
						registerReceiver(glassStateBroadcastReceiver, filter);
						return;
					}
					break;
				case SET_WIFI_AP:
					String sid = data.getString("ssid");
//					String spw = data.getString("pw");
					String spw = "12345678";
					Log.e(TAG, sid + " " + spw);
					mWifiAdmin.connect(sid, spw, WifiCipherType.WIFICIPHER_NOPASS);
					
					pk.putInt("type", SET_WIFI_AP);
					pk.putString("ssid", sid);
					break;
				case SET_VEDIO_DURATION:
					sValue = data.getString(lables[type-1]);
					pk.putInt("type", type);
					pk.putString(lables[type-1], sValue);
					Intent i = new Intent("com.ingenic.glass.camera.other.subsection");
					i.putExtra("value",sValue);
					i.setPackage("com.ingenic.glass.camera");
					sendBroadcast(i);
					
					setVideoDuration(sValue);
					break;
				case SWITCH_ROUND_VIDEO:
					bvalue = data.getBoolean(lables[type-1]);
					pk.putInt("type", type);
					pk.putBoolean(lables[type - 1], bvalue);
					
					Intent in = new Intent("com.ingenic.glass.camera.other.storage_mode");
					in.putExtra("value",bvalue);
					in.setPackage("com.ingenic.glass.camera");
					sendBroadcast(in);
					
					setVideoRound(bvalue);
					break;
				case SET_VOLUME:
					int volume = data.getInt(lables[type-1]);
					mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, volume, 0);
					
					pk.putInt("type", SET_VOLUME);
					pk.putInt("volume", volume);
					break;
				case GET_POWER_LEVEL:
					Log.e(TAG, "Get Power");
					pk.putInt("type", GET_POWER_LEVEL);
					pk.putInt("power", mGlassNotifyBroadcastReceiver.getCurrentPowerPercentage());
					break;
				case GET_STORAGE_STATE:
					String availableSize = Utils.getAvailableInternalMemorySizeString();
					String totalString = Utils.getTotalInternalMemorySize();
					pk.putString("available", availableSize);
					pk.putString("total", totalString);
					pk.putInt("type", GET_STORAGE_STATE);
					break;
				case GET_UP_TIME:
					pk.putLong("uptime", SystemClock.elapsedRealtime());
					pk.putInt("type", GET_UP_TIME);
					break;
				case GET_GLASS_INFO:
					GlassInfo gf = new GlassInfo();
					getGlassInfo(gf);
					Log.e(TAG, "models:" + gf.models + " oem:" + gf.oem + " platform:" + gf.platform + " deviceType:" + gf.deviceType);
					pk.putString("cpu", gf.cpu);
					pk.putString("duration", gf.duration);
					pk.putBoolean("round", gf.round);
					pk.putString("version", gf.version);
					pk.putString("serial", gf.serial);
					pk.putInt("volume", gf.volume);
					pk.putString("models", gf.models);
					pk.putString("oem", gf.oem);
					pk.putString("platform", gf.platform);
					pk.putString("deviceType", gf.deviceType);
					pk.putInt("type", GET_GLASS_INFO);
					break;
				case TURN_WIFI_OFF:
					mWifiAdmin.closeWifi();
					break;
				case GET_STATE:
					pk.putInt("state", getState());
					pk.putInt("type", GET_STATE);
					break;
				case UPDATE_CONNECT_WIFI_MSG:
					if(isStorageShortage(data.getInt("size"))) {
						reportUpdateState(UPDATE_STORAGE_SHORTAGE);
						return;
					}
					mUpdateInfo.deltaUrl = data.getString("url");
					mUpdateInfo.fileSize = data.getInt("size");
					mUpdateInfo.md5sum = data.getString("md5");
					mUpdateInfo.deltaid = data.getString("deltaid");
					mUpdateInfo.versionName = data.getString("vname");
					mUpdateInfo.update = true;
					
					Log.e(TAG, "deltaUrl:" + mUpdateInfo.deltaUrl + " fileSize:" + mUpdateInfo.fileSize + " md5sum:" + mUpdateInfo.md5sum
							+ " deltaid:" + mUpdateInfo.deltaid + " versionName:" + mUpdateInfo.versionName);
					saveNewVersionName(mUpdateInfo.versionName);
					
					Log.e(TAG, "ssid:" + data.getString("ssid") + " pw:" + data.getString("pw"));
					mWifiAdmin.connect(data.getString("ssid"), data.getString("pw"), WifiCipherType.WIFICIPHER_WPA);
					IntentFilter filter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
					registerReceiver(glassStateBroadcastReceiver, filter);
					
					reportUpdateState(UPDATE_TRY_CONNECT_WIFI);
					
					mHandler.sendEmptyMessageDelayed(UPDATE_CONNECTI_WIFI_TIMEOUT, 10000);
					return;
				case FACTORY_RESET:
					sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
					break;
				default:
					return;
			}
			mCmdChannel.sendPacket(pk);
		}
	};
	
	private onChannelListener mOnNotifyChannelListener = new onChannelListener() {
		
		@Override
		public void onStateChanged(CONNECTION_STATE state) {
			// TODO Auto-generated method stub
			if(state == CONNECTION_STATE.BLUETOOTH_CONNECTED) {
				SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(GlassesService.this);
				int lastUpdateState = preference.getInt("last_update_state", UPDATE_DEFAULT_STATE);
				if(lastUpdateState != UPDATE_DEFAULT_STATE) {
					Packet pk = mNotifyChannel.createPacket();
					pk.putInt("type", REPORT_UPDATE_STATE);
					pk.putInt("state", lastUpdateState);
					mNotifyChannel.sendPacket(pk);
					
					Editor editor = preference.edit();
					editor.putInt("last_update_state", UPDATE_DEFAULT_STATE);
					editor.commit();
				}
			}
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

	private String getCpuInfo() {
		try {
			FileReader freader = new FileReader("/proc/cpuinfo");
			BufferedReader breader = new BufferedReader(freader);
			String line = null;
			while((line = breader.readLine()) != null) {
				if(line.startsWith("cpu model")) {
					String[] cpuInfo = line.split(": ");
					return cpuInfo[1];
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	private String getSerialNumber() {
		String serial = null;
		try {
			FileReader freader = new FileReader("/data/misc/bluetooth/bt_name");
			BufferedReader breader = new BufferedReader(freader);
			serial = breader.readLine();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return serial + "";
	}
	
	private int getVolume() {
		return mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
	}
	
	private void setVideoRound(boolean value) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = preferences.edit();
		editor.putBoolean("round", value);
		editor.commit();
	}
	
	private void setVideoDuration(String value) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = preferences.edit();
		editor.putString("duration", value);
		editor.commit();
	}
	
	private boolean getVideoRound() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		return preferences.getBoolean("round", false);
	}
	
	private String getVideoDuration() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		return preferences.getString("duration", "10");
	}
	
	private int getState() {
		long startTime = mGlassNotifyBroadcastReceiver.getVideoRecorderStartTime();
		
		if(startTime == 0)
			return 0;
		else
			return (int)(System.currentTimeMillis() - startTime);
	}
	
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
			if(WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
				NetworkInfo ni = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				WifiInfo wi = (WifiInfo)intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
				if(ni.isConnected()) {
					try {
						String ip = Utils.getLocalIpAddress();
						if(!mUpdateInfo.update) {
							Packet pk = mCmdChannel.createPacket();
							pk.putInt("type", WIFI_CONNECTED);
							pk.putString("ip", ip);
							mCmdChannel.sendPacket(pk);
						}
						else {
							startDownLoadUpdatePacket();
						}
						Log.e(TAG, Utils.getLocalIpAddress());
						unregisterReceiver(glassStateBroadcastReceiver);
						mHandler.removeMessages(UPDATE_CONNECTI_WIFI_TIMEOUT);
					} catch (Exception e) {
						e.printStackTrace();
						WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
						mWifiManager.reconnect();
					}
				}
			}
		}
	};
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if(msg.what == CONNECT_WIFI_TIMEOUT) {
				unregisterReceiver(glassStateBroadcastReceiver);
				
				reportUpdateState(UPDATE_CONNECTI_WIFI_TIMEOUT);
				
			}
		}
	};
	
	private void getGlassInfo(GlassInfo gf) {
		Log.e(TAG, "getGlassInfo");
		gf.volume = getVolume();
		gf.round = getVideoRound();
		gf.duration = getVideoDuration();
		gf.cpu = getCpuInfo();
		gf.deviceType = "glass";
		gf.serial = getSerialNumber();
		
		try {
			Object object = new Object();
			Method method = Class.forName("android.os.SystemProperties").getMethod("get", String.class);
			gf.version = (String)method.invoke(object, new Object[]{"ro.fota.version"});
			gf.oem = (String)method.invoke(object, new Object[]{"ro.fota.oem"});
			gf.platform = (String)method.invoke(object, new Object[]{"ro.fota.platform"});
			gf.models = (String)method.invoke(object, new Object[]{"ro.fota.device"});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private class GlassInfo {
		int volume;
		boolean round;
		String duration;
		String cpu;
		String serial;
		String version;
		String oem;
		String models;
		String token;
		String platform;
		String deviceType;
	}
	
	private class updateInfo {
		boolean update;
		int fileSize;
		String deltaUrl;
		String md5sum;
		String deltaid;
		String versionName;
		
	}
	
	private void startDownLoadUpdatePacket() {
		
		 final DownParamInfo downParamInfo = reportDown();
		
		 reportUpdateState(UPDATE_START_DOWNLOAD);
		 
		DLService.start(this, mUpdateInfo.deltaUrl, new File(Environment.getExternalStorageDirectory().toString()), "update.zip", new IOnDownloadListener() {
            @Override
            public void onDownloadProgress(String title, int totalSize, int downloadedSize) {
                Log.d(TAG, "totalSize:" + totalSize + "downloadedSize:" + downloadedSize);
            }

            @Override
            public void onDownloadFinished(String title, int state, final File file) {
                boolean vilateFile = FileUtil.VilateFile(file.getPath(), mUpdateInfo.md5sum, mUpdateInfo.fileSize);
                Log.d(TAG, "onDownloadFinished: file path " + file.getPath());
                Log.d(TAG, "onDownloadFinished: vilateFile " + vilateFile);
                if ((state == DownloadUtil.State.SUCCESS || state == DownloadUtil.State.ALREADY_DOWNLOADED) && vilateFile) {//download success
                    downParamInfo.downloadStatus = "1";
                    downParamInfo.downEnd = getCurrentTime();
                    MobAgentPolicy.reportDown(downParamInfo, null);
                    								
                    Intent intent = new Intent(new Intent(
                            "INSTALL_UPDATE_PACKAGE"));
                    intent.putExtra("PackageFileName", file.getPath());
                    sendBroadcast(intent);
                    
                } else { //download fail
                	
                    downParamInfo.downloadStatus = "2";
                    downParamInfo.downEnd = getCurrentTime();
                    MobAgentPolicy.reportDown(downParamInfo, null);
                    reportUpdateState(UPDATE_INVALID_PACKAGE);
                    if(file.exists())
                    	file.delete();
                }
            }

            @Override
            public void onDownloadError(int error) {
            	reportUpdateState(UPDATE_DOWNLOAD_ERROR);
                Log.e(TAG, DownLoadError.parse(error));
            }
        });
    }
	
	private DownParamInfo reportDown() {
        DownParamInfo downParamInfo = new DownParamInfo();
        downParamInfo.mid = getSerialNumber();
        downParamInfo.token = HANLANG_FOTA_TOKE;
        downParamInfo.deltaID = mUpdateInfo.deltaid;
        downParamInfo.downStart = getCurrentTime();
        return downParamInfo;
    }

	public static String getCurrentTime() {
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
					        "yyyy-MM-dd HH:mm:ss");
					String date = simpleDateFormat.format(System.currentTimeMillis());
					return date;
	}
    
	private void reportUpdateState(int state) {
		
		SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(GlassesService.this);
		int newState = state;
		
		if(mNotifyChannel.isConnected()) {
				Packet pk = mNotifyChannel.createPacket();
				pk.putInt("type", REPORT_UPDATE_STATE);
				pk.putInt("state", state);
				mNotifyChannel.sendPacket(pk);
				
				newState = UPDATE_DEFAULT_STATE;
		}
				
		Editor editor = preference.edit();
		editor.putInt("last_update_state", newState);
		editor.commit();
}
	
	private void saveNewVersionName(String name) {
		SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(GlassesService.this);
		Editor editor = preference.edit();
		editor.putString("last_version_name", name);
		editor.commit();
	}
	
	private boolean isStorageShortage(int fileSize) {
		
		File file = Environment.getExternalStorageDirectory();
		StatFs sf = new StatFs(file.getAbsolutePath());
		int storageLeft = sf.getAvailableBlocks()*sf.getBlockSize() - fileSize;
		if(storageLeft > 50000000)
			return true;
		return false;
	}

}
