package com.smartglass.device;

import android.app.Application;
import android.content.Intent;
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
