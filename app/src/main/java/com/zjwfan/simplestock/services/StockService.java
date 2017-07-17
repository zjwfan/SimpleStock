package com.zjwfan.simplestock.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.zjwfan.simplestock.R;
import com.zjwfan.simplestock.models.Config;
import com.zjwfan.simplestock.models.Stock;
import com.zjwfan.simplestock.models.StockSQLiteOpenHelper;
import com.zjwfan.simplestock.network.MySingleton;
import com.zjwfan.simplestock.utils.Util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import static android.R.attr.value;

public class StockService extends Service {
    private final static String TAG = "ZJWFAN";
    private static HashSet<String> StockIds = new HashSet<String>();
    private static HashMap<String, Stock> StocksMap = new HashMap<String, Stock>();
    private SQLiteDatabase mStockDB;
    private Config mConfig;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 99:
                    if (Stock.isTradeTime()) {
                        Stock.refreshStocks(getApplicationContext(), StockIds, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Stock.sinaResponseStockInfo(StockService.this, mStockDB, mConfig, response, StockIds, StocksMap);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                                loge("onErrorResponse()" + error.getLocalizedMessage());
                            }
                        });
                        loge("mConfig.getTimerCountSett() = " + mConfig.getTimerCountSett());
                        sendEmptyMessageDelayed(99, mConfig.getTimerCountSett());
                    } else {
                        long value = Stock.getTradeStartTime();
                        sendEmptyMessageDelayed(99, value);
                        loge("value = " + value);
                        double dValue = value/1000/60;
                        loge("value/1000/60 = " + dValue);
                    }

                    break;
                case 1:
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private BroadcastReceiver mReloadDataBaseReceiver, mSeetingsChangeReceiver;

    public static void stopService(Context context) {
        Intent intent = new Intent(context, StockService.class);
        Log.e("ZJWFAN", "StockService: stopService");
        context.stopService(intent);
    }

    public static void startService(Context context) {
        Intent intent = new Intent(context, StockService.class);
        Log.e("ZJWFAN", "StockService: startService");
        context.startService(intent);
    }


    @Override
    public void onCreate() {
        loge("onCreate()");
        initDatabase();
        initStockIds();
        initSeeting();
        initBroadcastReceiver();
        mHandler.sendEmptyMessageDelayed(99, 500);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        loge("onDestroy()");
        if (mReloadDataBaseReceiver != null) {
            this.unregisterReceiver(mReloadDataBaseReceiver);
        }
        if (mSeetingsChangeReceiver != null) {
            this.unregisterReceiver(mSeetingsChangeReceiver);
        }
        super.onDestroy();
    }

    public StockService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        loge("onStartCommand()");
//        mHandler.sendEmptyMessageDelayed(99, 500);
//        Stock.setAlarm(this, mConfig.getTimerCountSett(), Stock.getTradeDuraTime());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void initDatabase() {
        StockSQLiteOpenHelper stockSQLiteOpenHelper = StockSQLiteOpenHelper.getInstance(getApplicationContext());
        mStockDB = stockSQLiteOpenHelper.getWritableDatabase();
    }

    private void initStockIds() {
        StockIds = StockSQLiteOpenHelper.queryColumnStringInfoFromStockDB(mStockDB, StockSQLiteOpenHelper.COLUMN_ID_CODE);
        StocksMap = StockSQLiteOpenHelper.queryStockInfoFromStockDB(mStockDB);

        if (StockIds.size() != StocksMap.size())
            Log.e(TAG, "StockIds.size() != StocksMap.size()" + "at 496846466");

    }

    private void loge(String message) {
        Log.e("ZJWFAN", message);
    }

    private void initSeeting() {
        mConfig = new Config();
        mConfig = Config.loadConfigFromSharedPre(this);
    }

    private void initBroadcastReceiver() {
        mReloadDataBaseReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e("ZJWFAN", "onReceive  onReceive  com.zjwfan.simplestock.reload.stockids");
                initStockIds();
            }
        };

        this.registerReceiver(mReloadDataBaseReceiver, new IntentFilter("com.zjwfan.simplestock.reload.stockids"));

        mSeetingsChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e("ZJWFAN", "onReceive  onReceive  com.zjwfan.simplestock.reload.settings");
                initSeeting();
            }
        };

        this.registerReceiver(mSeetingsChangeReceiver, new IntentFilter("com.zjwfan.simplestock.reload.settings"));
    }





}
