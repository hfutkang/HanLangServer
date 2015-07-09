package com.smartglass.device;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.util.Log;

public class MultiMediaScanner implements MediaScannerConnectionClient {
    private static final String TAG = "MultiMediaManager";

    private MediaScannerConnection mConnection;
    private Context mContext;

    private String[] mFilepath;

    public MultiMediaScanner(Context context, String[] filepath, String filetype) {
	mContext = context;
	mFilepath = filepath;
	mConnection = new MediaScannerConnection(mContext, this);
    }
    
    public void connect() {
    	mConnection.connect();
    }

    public void onMediaScannerConnected() {
	Log.e(TAG, "onMediaScannerConnected");
//	mConnection.scanFile(mFilepath, null);
	mConnection.scanFile(mContext, mFilepath, null, this);
    }

    public void onScanCompleted(String path, Uri uri) {
	Log.e(TAG, "onScanCompleted");
	mConnection.disconnect();
    }
}