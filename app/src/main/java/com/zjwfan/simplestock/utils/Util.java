package com.zjwfan.simplestock.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.zjwfan.simplestock.R;
import com.zjwfan.simplestock.models.Config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by zjw on 2016-03-01.
 */
public class Util {
    public final static String[] PRE_ADD_STOCKS = new String[] {
            "300369", "002439", "002241", "002269", "002230",
            "600060", "601166", "300188", "601211", "399300",
            "000895", "600887", "600696", "600519", "002415",
            "002594", "000858", "000938", "300379", "300017",
            "002405", "002739", "150019", "600436", "000538",
            "002594", "300433", "000651", "600570", "001979",
            "300104", "002739", "300017", "600233", "000725",
            "600276", "603288", "601318", "600362", "000060",
            //"600276", "603288", "601318", "600362", "000060"
            //！！bug：银华锐进 150019 （sz150019（
    };

    private Util() {
        throw new AssertionError();
    }

    public static int getAppVersionCode(Context context) {
        if (context != null) {
            PackageManager pm = context.getPackageManager();
            if (pm != null) {
                PackageInfo pi;
                try {
                    pi = pm.getPackageInfo(context.getPackageName(), 0);
                    if (pi != null) {
                        return pi.versionCode;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return -1;
    }


    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    public static void copyFile(FileInputStream fromFile, FileOutputStream toFile) throws IOException {
        FileChannel fromChannel = null;
        FileChannel toChannel = null;
        try {
            fromChannel = fromFile.getChannel();
            toChannel = toFile.getChannel();
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
        } finally {
            try {
                if (fromChannel != null) {
                    fromChannel.close();
                }
            } finally {
                if (toChannel != null) {
                    toChannel.close();
                }
            }
        }
    }

    public static void loge(String log) {
        Log.e("ZJWFAN", log);
    }

    public static void logd(String log) {
        Log.e("ZJWFAN", log);
    }

    public static void sendNotifation(Context context, Config config, int id, String title, String text){
        Log.w("ZJWFAN", "Notification: " + text);
        if (!config.isNotification())
            return;
        context.sendBroadcast(new Intent("com.zjwfan.simplestock.stock.notification"));
        NotificationCompat.Builder nBuilder =
                new NotificationCompat.Builder(context);
        nBuilder.setSmallIcon(R.mipmap.ic_launcher);
        nBuilder.setContentTitle(title);
        nBuilder.setContentText(text);

        if (config.isVibrate())
            nBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND);
        nBuilder.setLights(Color.RED, 1000, 1000);
        if (config.isVibrate())
            nBuilder.setVibrate(new long[]{100, 100, 100}); //2016年2月29日15:19:36

        int defaults=0;
        if (config.isVibrate()) {
            defaults |= Notification.DEFAULT_SOUND;
        }
        if (config.isVibrate()) {
            defaults |= Notification.DEFAULT_VIBRATE;
        }
        if (config.isVibrate()) {
            defaults |= Notification.DEFAULT_LIGHTS;
        }
        nBuilder.setDefaults(defaults);

        NotificationManager notifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifyMgr.notify(id, nBuilder.build());
    }

}
