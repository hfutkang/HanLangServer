package com.smartglass.device;

import android.util.Log;

import com.fota.iport.MobAgentPolicy;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;

/**
 * Created by brave on 2015/6/11.
 */
public class FileUtil {
    private static final String TAG = "FotaUpdate";
    public static String getMd5ByFile(File file) {
        String value = null;
        try {
            FileInputStream in = new FileInputStream(file);
//            MappedByteBuffer byteBuffer = in.getChannel().map(
//                    FileChannel.MapMode.READ_ONLY, 0, file.length());
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] byteBuffer =new byte[1024*1024*10];
            int len = 0;
            while((len = in.read(byteBuffer)) > 0) {
            md5.update(byteBuffer,0, len);
            }
            BigInteger bi = new BigInteger(1, md5.digest());
            value = bi.toString(16);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static String getMd5ByFile(String filePath) {
        File fd = new File(filePath);
        if (fd.exists()) {
            return getMd5ByFile(fd);
        }
        return "";
    }

    public static long getFileSize(String filename) {
        File fd = new File(filename);
        if (fd.exists()) {
            return fd.length();
        }
        return 0;
    }

    public static long getFileSize(File fd) {
        if (fd.exists())
            return fd.length();
        return 0;
    }

    public static boolean VilateFile(String filePath, String md5sum,
                                     int filelength) {
        String now_md5 = getMd5ByFile(filePath);
        int now_length = (int) getFileSize(filePath);
        Log.d(TAG, "onDownloadFinished: md5sum " + md5sum);
        Log.d(TAG, "onDownloadFinished: now md5 " + md5sum);
        Log.d(TAG, "onDownloadFinished: file size " + now_length);
        if (now_md5.equals(md5sum) && now_length == filelength) {
            return true;
        }

        return false;
    }




}
