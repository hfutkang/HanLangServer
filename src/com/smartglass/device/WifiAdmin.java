package com.smartglass.device;

import java.net.Inet4Address;
import java.util.List;

import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.util.Log;
import android.os.Handler;
/////import com.listong.util.WifiConnectaaaa.WifiCipherType;

public class WifiAdmin {
	
	private final static String TAG = "WifiAdmin-A";

	private WifiManager mWifiManager;
	
	private Context mContext;
	private WifiInfo mWifiInfo;

	private List<ScanResult> mWifiList;

	private List<WifiConfiguration> mWifiConfigurations;
	WifiLock mWifiLock;

	public WifiAdmin(Context context) {
		mContext = context;	
		mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		mWifiInfo = mWifiManager.getConnectionInfo();
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

		mContext.registerReceiver(mReceiver, filter);
	}
	
	public enum WifiCipherType {
		WIFICIPHER_WEP, WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID
	}


	public boolean closeWifi() {
	    Log.d(TAG, "closeWifi");
		if (mWifiManager.isWifiEnabled()) {
			return mWifiManager.setWifiEnabled(false);
		}
		return false;
	}

	/**
	 * Gets the Wi-Fi enabled state.
	 * 
	 * @return One of 
	 *         {@link WifiManager#WIFI_STATE_DISABLED},
	 *         {@link WifiManager#WIFI_STATE_DISABLING},
	 *         {@link WifiManager#WIFI_STATE_ENABLED},
	 *         {@link WifiManager#WIFI_STATE_ENABLING},
	 *         {@link WifiManager#WIFI_STATE_UNKNOWN}
	 * @see #isWifiEnabled()
	 */
	public int checkState() {
		return mWifiManager.getWifiState();
	}


	public void acquireWifiLock() {
		mWifiLock.acquire();
	}


	public void releaseWifiLock() {
		if (mWifiLock.isHeld()) {
			mWifiLock.acquire();
		}
	}

	public void createWifiLock() {
		mWifiLock = mWifiManager.createWifiLock("test");
	}


	public List<WifiConfiguration> getConfiguration() {
		return mWifiConfigurations;
	}


	public void connetionConfiguration(int index) {
		if (index > mWifiConfigurations.size()) {
			return;
		}
		mWifiManager.enableNetwork(mWifiConfigurations.get(index).networkId,true);
	}

	public void startScan() {

		// openWifi();

		mWifiManager.startScan();

		mWifiList = mWifiManager.getScanResults();

		mWifiConfigurations = mWifiManager.getConfiguredNetworks();
		
	}

	public List<ScanResult> getWifiList() {
		return mWifiList;
	}

	public StringBuffer lookUpScan() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < mWifiList.size(); i++) {
			sb.append("Index_" + new Integer(i + 1).toString() + ":");
			sb.append((mWifiList.get(i)).toString()).append("\n");
		}
		return sb;
	}

	public String getMacAddress() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
	}

	/**
	 * Return the basic service set identifier (BSSID) of the current access
	 * point. The BSSID may be {@code null} if there is no network currently
	 * connected.
	 * 
	 * @return the BSSID, in the form of a six-byte MAC address:
	 *         {@code XX:XX:XX:XX:XX:XX}
	 */
	public String getBSSID() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
	}

	public int getIpAddress() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
	}

	/**
	 * Each configured network has a unique small integer ID, used to identify
	 * the network when performing operations on the supplicant. This method
	 * returns the ID for the currently connected network.
	 * 
	 * @return the network ID, or -1 if there is no currently connected network
	 */
	public int getNetWordId() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
	}


	public String getWifiInfo() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
	}

	public void addNetWork(WifiConfiguration configuration) {
		int wcgId = mWifiManager.addNetwork(configuration);
		mWifiManager.enableNetwork(wcgId, true);
	}

	public void disConnectionWifi(int netId) {
		mWifiManager.disableNetwork(netId);
		mWifiManager.disconnect();
	}

	public boolean openWifi() {
		boolean bRet = true;
		if (!mWifiManager.isWifiEnabled()) {
		    Log.d(TAG, "open wifi");
			bRet = mWifiManager.setWifiEnabled(true);
		}
		closeWifiAp();
		return bRet;
	}

    public void closeWifiAp() {
		if (mWifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_ENABLED
				|| mWifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_ENABLING) {
			mWifiManager.setWifiApEnabled(null, false);
		}
	}

	public boolean connect(String SSID, String Password, WifiCipherType Type) {
		Log.d(TAG, "connect in");
		if (!this.openWifi()) {
			return false;
		}
		Log.d(TAG, "wifi state:" + mWifiManager.getWifiState());
		while (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
			try {
				
				Thread.currentThread();
				Thread.sleep(100);
			} catch (InterruptedException ie) {
			}
		}

		System.out.println("WifiAdmin#connect");
		WifiConfiguration wifiConfig = createWifiInfo(SSID, Password, Type);
		//
		if (wifiConfig == null) {
			return false;
		}

		WifiConfiguration tempConfig = this.isExsits(SSID);
		
		int tempId = wifiConfig.networkId;
		if (tempConfig != null) {
			tempId = tempConfig.networkId;
			mWifiManager.removeNetwork(tempConfig.networkId);
			Log.e(TAG, "remove net" + tempConfig.networkId);
		}
		int netID = mWifiManager.addNetwork(wifiConfig);
		if(netID == -1) {
			
			try {
				Thread.sleep(500);
			} catch (Exception e) {
				e.printStackTrace();
			}
			netID = mWifiManager.addNetwork(wifiConfig);
		}
		Log.e(TAG, "netid" + netID);
		mWifiManager.disconnect();
		// netID = wifiConfig.networkId;
		boolean bRet = mWifiManager.enableNetwork(netID, true);
		return bRet;
	}

	private WifiConfiguration isExsits(String SSID) {
		List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
		if(existingConfigs == null){
			return null;
		}
		for (WifiConfiguration existingConfig : existingConfigs) {
			if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
				Log.e(TAG, "" + existingConfig.networkId);
				return existingConfig;
			}
		}
		return null;
	}

	private WifiConfiguration createWifiInfo(String SSID, String Password,
			WifiCipherType Type) {
		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		
		config.priority = 40;
		
		config.SSID = "\"" + SSID + "\"";
		if (Type == WifiCipherType.WIFICIPHER_NOPASS) {
			config.wepKeys[0] = "\"\"";
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		else if (Type == WifiCipherType.WIFICIPHER_WEP) {
			config.preSharedKey = Password;
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.SHARED);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedGroupCiphers
					.set(WifiConfiguration.GroupCipher.WEP104);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		else if (Type == WifiCipherType.WIFICIPHER_WPA) {

			
			config.preSharedKey = "\"" + Password + "\"";
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			// config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);

		} else {
			return null;
		}
		return config;
	}


	public boolean isConnect(ScanResult result) {
		if (result == null) {
			return false;
		}

		mWifiInfo = mWifiManager.getConnectionInfo();
		////String g2 = "\"" + result.SSID + "\"";
		String g2 = result.SSID;
		if (mWifiInfo.getSSID() != null && mWifiInfo.getSSID().endsWith(g2)) {
			return true;
		}
		return false;
	}
	
	public boolean isConnect(String SSID) {
		if (SSID == null) {
			return false;
		}

		mWifiInfo = mWifiManager.getConnectionInfo();
		String g2 = mWifiInfo.getSSID();//"\"" + SSID + "\"";
		if (mWifiInfo.getSSID() != null){//&& mWifiInfo.getSSID().endsWith(SSID)) {
			if(g2.contains(SSID)){				
			return true;
			}
		}
		return false;
	}


	public String ipIntToString(int ip) {
		try {
			byte[] bytes = new byte[4];
			bytes[0] = (byte) (0xff & ip);
			bytes[1] = (byte) ((0xff00 & ip) >> 8);
			bytes[2] = (byte) ((0xff0000 & ip) >> 16);
			bytes[3] = (byte) ((0xff000000 & ip) >> 24);
			return Inet4Address.getByAddress(bytes).getHostAddress();
		} catch (Exception e) {
			return "";
		}
	}

	public int getConnNetId() {
		// result.SSID;
		mWifiInfo = mWifiManager.getConnectionInfo();
		return mWifiInfo.getNetworkId();
	}


	public static String singlLevToStr(int level) {

		String resuString = "result";

		if (Math.abs(level) > 100) {
		} else if (Math.abs(level) > 80) {
			resuString = "bad";
		} else if (Math.abs(level) > 70) {
			resuString = "soso";
		} else if (Math.abs(level) > 60) {
			resuString = "good";
		} else if (Math.abs(level) > 50) {
			resuString = "very good";
		} else {
			resuString = "wonderful";
		}
		return resuString;
	}


	public boolean addNetwork(WifiConfiguration wcg) {
		if (wcg == null) {
			return false;
		}
		//receiverDhcp = new ReceiverDhcp(ctx, mWifiManager, this, wlanHandler);
		//ctx.registerReceiver(receiverDhcp, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
		int wcgID = mWifiManager.addNetwork(wcg);
		boolean b = mWifiManager.enableNetwork(wcgID, true);
		mWifiManager.saveConfiguration();
		System.out.println(b);
		return b;
	}
	
	public boolean connectSpecificAP(ScanResult scan) {
	    Log.d(TAG, "connectSpecificAP in");
		List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
		boolean networkInSupplicant = false;
		boolean connectResult = false;
		
		mWifiManager.disconnect();
		for (WifiConfiguration w : list) {
			
			// String str = convertToQuotedString(info.ssid);
			if (w.BSSID != null && w.BSSID.equals(scan.BSSID)) {
				connectResult = mWifiManager.enableNetwork(w.networkId, true);
				// mWifiManager.saveConfiguration();
				networkInSupplicant = true;
				break;
			}
		}
		if (!networkInSupplicant) {
			WifiConfiguration config = CreateWifiInfo(scan, "");
			connectResult = addNetwork(config);
		}

		return connectResult;
	}

	public WifiConfiguration CreateWifiInfo(ScanResult scan, String Password) {
		// Password="ultrapower2013";
		// deleteExsits(info.ssid);
		WifiConfiguration config = new WifiConfiguration();
		config.hiddenSSID = false;
		config.status = WifiConfiguration.Status.ENABLED;

		if (scan.capabilities.contains("WEP")) {
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedGroupCiphers
					.set(WifiConfiguration.GroupCipher.WEP104);

			config.SSID = "\"" + scan.SSID + "\"";

			config.wepTxKeyIndex = 0;
			config.wepKeys[0] = Password;
			// config.preSharedKey = "\"" + SHARED_KEY + "\"";
		} else if (scan.capabilities.contains("PSK")) {
			//
			config.SSID = "\"" + scan.SSID + "\"";
			config.preSharedKey = "\"" + Password + "\"";
		} else if (scan.capabilities.contains("EAP")) {
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.TKIP);
			config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			config.SSID = "\"" + scan.SSID + "\"";
			config.preSharedKey = "\"" + Password + "\"";
		} else {
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

			config.SSID = "\"" + scan.SSID + "\"";
			// config.BSSID = info.mac;
			config.preSharedKey = null;
			//
		}

		return config;
	}

    private final int TIMEOUT_MS = 60000*3;//3min
    private final int MSG_CONNECT_TIMEOUT = 0;

    private Handler mHandler = new Handler() {
	    public void handleMessage(android.os.Message msg) {
		if (msg.what == MSG_CONNECT_TIMEOUT) {
		    Log.e(TAG,"wifi is last a long time. so close wifi");
		    closeWifi();
		}
	    };
	};
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
	    @Override
		public void onReceive(Context context, Intent intent) {
		  // TODO Auto-generated method stub
		String action = intent.getAction();
		Log.e(TAG, action);
		if(WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
		    NetworkInfo ni = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
		      //WifiInfo wi = (WifiInfo)intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
		    Log.d(TAG, "ni.isConnected():" + ni.isConnected() + " "+ ni + " "
			  + mWifiManager.getConnectionInfo().getSSID());
		    if(!ni.isConnected()) {
			mHandler.sendEmptyMessageDelayed(MSG_CONNECT_TIMEOUT,TIMEOUT_MS);
		    }else{
			mHandler.removeMessages(MSG_CONNECT_TIMEOUT);
		    }
		}else if(WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)){
		    int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
						   WifiManager.WIFI_STATE_UNKNOWN);
		    Log.d(TAG,"-wifi state change to:"+state);
		    if(state == WifiManager.WIFI_STATE_ENABLED){
			mHandler.sendEmptyMessageDelayed(MSG_CONNECT_TIMEOUT,TIMEOUT_MS);
		    }else if(state == WifiManager.WIFI_STATE_DISABLED){
			mHandler.removeMessages(MSG_CONNECT_TIMEOUT);
		    }
		}
	    }
	};

}
