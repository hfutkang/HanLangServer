package com.smartglass.device;

import java.lang.Thread.UncaughtExceptionHandler;

import com.ingenic.glass.api.sync.SyncChannel;
import com.ingenic.glass.api.sync.SyncChannel.CONNECTION_STATE;
import com.ingenic.glass.api.sync.SyncChannel.Packet;
import com.ingenic.glass.api.sync.SyncChannel.RESULT;
import com.ingenic.glass.api.sync.SyncChannel.onChannelListener;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class GlassApplication extends Application {
	
	private final static String TAG = "GlassApplication";
	public final static boolean DEBUG = true;
	
	public void onCreate() {
		
		if(DEBUG) Log.e(TAG, "onCreate");
		Intent eventIntent = new Intent(this, GlassesService.class);
		startService(eventIntent);		
		super.onCreate();
	}
}
