package com.zjwfan.simplestock.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.zjwfan.simplestock.models.Config;
import com.zjwfan.simplestock.models.Stock;
import com.zjwfan.simplestock.models.StockSQLiteOpenHelper;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;

public class StockService extends Service {
    private static HashSet<String> StockIds = new HashSet<String>();
    private static HashMap<String, Stock> StocksMap = new HashMap<String, Stock>();
    private SQLiteDatabase mStockDB;
    private Config mConfig;
    private boolean isVibrate;
    private long preNotifiTime = 0L;

    private int mNotifiCount = 0;

    static class MyHandler extends Handler {
        WeakReference<Context> contextWeakReference;

        MyHandler(Context context) {
            contextWeakReference = new WeakReference<Context>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            final Context context = contextWeakReference.get();
            final StockService stockService;
            if (context == null) return;
            if (context instanceof StockService) {
                stockService = (StockService) context;
            } else {
                Logger.e("error context is not StockService.");
                return;
            }
            switch (msg.what) {
                case 99:
                    if (Stock.isTradeTime()) {
                        Stock.refreshStocks(context, StockIds, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Stock.sinaResponseStockInfo(context, stockService.mStockDB, stockService.mConfig, response, StockIds, StocksMap);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                                Logger.e("onErrorResponse()" + error.getLocalizedMessage());
                            }
                        });
                        sendEmptyMessageDelayed(99, stockService.mConfig.getTimerCountSett());
                    } else {
                        long value = Stock.getTradeStartTime();
                        sendEmptyMessageDelayed(99, value);
                        double dValue = value/1000/60;
                    }

                    break;
                case 1:
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    private MyHandler mHandler = new MyHandler(this);

    private BroadcastReceiver mReloadDataBaseReceiver, mSeetingsChangeReceiver, mNotificationReceiver;

    public static void stopService(Context context) {
        Intent intent = new Intent(context, StockService.class);
        Logger.e("StockService: stopService");
        context.stopService(intent);
    }

    public static void startService(Context context) {
        Intent intent = new Intent(context, StockService.class);
        Logger.e("StockService: startService");
        context.startService(intent);
    }

    private void initLog() {
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .tag("ZJWFAN")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                .build();

        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));
    }

    @Override
    public void onCreate() {
        initDatabase();
        initStockIds();
        initSeeting();
        initBroadcastReceiver();
        initLog();
        mHandler.sendEmptyMessageDelayed(99, 500);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if (mReloadDataBaseReceiver != null) {
            this.unregisterReceiver(mReloadDataBaseReceiver);
        }
        if (mSeetingsChangeReceiver != null) {
            this.unregisterReceiver(mSeetingsChangeReceiver);
        }
        if (mNotificationReceiver != null) {
            this.unregisterReceiver(mNotificationReceiver);
        }
        super.onDestroy();
    }

    public StockService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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
            Logger.e("StockIds.size() != StocksMap.size()" + "at 496846466");

    }


    private void initSeeting() {
        mConfig = new Config();
        mConfig = Config.loadConfigFromSharedPre(this);
        isVibrate = mConfig.isVibrate();
        mNotifiCount = 0;
    }

    private void initBroadcastReceiver() {
        mReloadDataBaseReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                initStockIds();
            }
        };
        this.registerReceiver(mReloadDataBaseReceiver, new IntentFilter("com.zjwfan.simplestock.reload.stockids"));

        mSeetingsChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                initSeeting();
            }
        };
        this.registerReceiver(mSeetingsChangeReceiver, new IntentFilter("com.zjwfan.simplestock.reload.settings"));

        mNotificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (isVibrate) {

                    if ((Calendar.getInstance().getTimeInMillis() - preNotifiTime) > 100000) {
                        mNotifiCount = 0;
                    }

                    if ((Calendar.getInstance().getTimeInMillis() - preNotifiTime) > 1000 || mNotifiCount < 3) {
                        mNotifiCount++;
                        mConfig.setVibrate(true);
                        preNotifiTime = Calendar.getInstance().getTimeInMillis();
                    } else {
                        mConfig.setVibrate(false);
                    }
                }

            }
        };
        this.registerReceiver(mNotificationReceiver, new IntentFilter("com.zjwfan.simplestock.stock.notification"));
    }


}
