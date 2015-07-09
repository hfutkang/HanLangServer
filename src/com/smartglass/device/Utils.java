package com.smartglass.device;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

public class Utils {
	
	private static final String TAG = "Utils";
	private static final long TOTAL_STORAGE_DEADLINE = 100*1024*1024;
	private static final long ROUND_VIDEO_STORAGE_MAX = 1024*1024*1024;
	private static final long STORAGE_BLOCK_SIZE = 4096;
	
	public static String getLocalIpAddress() {
		String ip = null;
		try {
		    // 遍历网络接口
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); 
					en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				if(intf.getName().contains("wlan")) {
					// 遍历IP地址
					for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
					        .hasMoreElements();) {
						InetAddress inetAddress = enumIpAddr.nextElement();
						byte addrs[] = inetAddress.getAddress();
						
						Log.e(TAG, inetAddress.getHostAddress().toString());
						// 非回传地址时返回
						if (!inetAddress.isLoopbackAddress()) {
							String addr = inetAddress.getHostAddress();
							Pattern pattern = Pattern.compile("[0-9]*.[0-9]*.[0-9]*.[0-9]*");
							Matcher matcher = pattern.matcher(addr);
							if(matcher.matches())
								ip = inetAddress.getHostAddress().toString();
			            }
			        }
			    }
			}
		return ip;
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressLint("NewApi")
	public static String getAvailableInternalMemorySizeString() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSizeLong();
		long availableBlocks = stat.getAvailableBlocksLong();
		return formatSizeString(availableBlocks * blockSize);
	}
	
	@SuppressLint("NewApi")
	public static long getAvailableInternalMemorySizeLong() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSizeLong();
		long availableBlocks = stat.getAvailableBlocksLong();
		return blockSize*availableBlocks;
	}
	
	@SuppressLint("NewApi")
	public static String getTotalInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSizeLong();
		long availableBlocks = stat.getBlockCountLong();
		Log.e(TAG, "blocks:" + availableBlocks + "blockSize:" + blockSize);
		return formatSizeString(availableBlocks * blockSize);
	}
	
	public static String formatSizeString(long size) {
		
		String suffix = "B";
		Double dSize = (double) size;
		DecimalFormat df = new DecimalFormat("#.##");
		
		if (size >= 1024) {
			suffix = "kB";
			dSize /= 1024;
			if (dSize >= 1024) {
				suffix = "MB";
				dSize /= 1024;
				if (dSize >= 1024) {
					suffix = "GB";
					dSize /= 1024;
				}
			}
		}
		
		StringBuilder resultBuffer = new StringBuilder(df.format(dSize));
		resultBuffer.append(suffix);
		return resultBuffer.toString();
	}
	
	public static boolean isLowStorage() {
		
		long available = getNormalAvailableMemorySizeLong();
		if(available < TOTAL_STORAGE_DEADLINE)
			return true;
		return false;
	}
	
	public static boolean anySpaceLeft() {
		
		long available = getNormalAvailableMemorySizeLong();
		if(available < 3*1024*1024)
			return false;
		return true;
		
	}
	
	@SuppressLint("NewApi")
	public static boolean needDeleteVedio() {
		
		long roundVideosSize = getRoundVideosSize();
		if(roundVideosSize >= ROUND_VIDEO_STORAGE_MAX) {
			return true;
		}
		return false;
	}
	
	private static long getRoundVideosSize() {
		
		long totalSize = 0;
		File file = new File("/data/apache/GlassData/roundvideos");
		
		if(!file.exists())
			return 0;
		
		File[] videos = file.listFiles();
		for(File v : videos) {
			totalSize += v.length() + STORAGE_BLOCK_SIZE;
		}
		
		return totalSize;
	}
	
	public static void deletedTheEarlistVideo() {
		
		File firstFile = null;
		
		File file = new File("/data/apache/GlassData/roundvideos");
		File[] videos = file.listFiles();
		
		if(videos.length != 0) {
			firstFile = videos[0];
			for(File v : videos) {
				if(v.lastModified() < firstFile.lastModified())
					firstFile = v;
			}
		}
		
		if(firstFile != null)
			firstFile.delete();
		
		if(needDeleteVedio()) {
			deletedTheEarlistVideo();
		}
	}
	
	public static long getPhotosSize() {
		
		long totalSize = 0;
		File file = new File("/data/apache/GlassData/photos");
		
		if(!file.exists())
			return 0;
		
		for(File p : file.listFiles()) {
			totalSize += p.length() + STORAGE_BLOCK_SIZE;
		}
		return totalSize;
	}
	
	public static long getNormalAvailableMemorySizeLong() {
		
		long totalAvailableSize = getAvailableInternalMemorySizeLong();
		long roundVideosSize = getRoundVideosSize();
		return totalAvailableSize + roundVideosSize - ROUND_VIDEO_STORAGE_MAX;
		
	}
	
	public static long getVideosSize() {
		
		long totalSize = 0;
		File file = new File("/data/apache/GlassData/vedios");
		
		if(!file.exists())
			return 0;
		
		for(File p : file.listFiles()) {
			totalSize += p.length() + STORAGE_BLOCK_SIZE;
		}
		
		return totalSize;
	}
	
}
