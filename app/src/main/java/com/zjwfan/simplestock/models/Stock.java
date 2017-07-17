package com.zjwfan.simplestock.models;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.zjwfan.simplestock.R;
import com.zjwfan.simplestock.network.MySingleton;
import com.zjwfan.simplestock.recivers.AlarmReceiver;
import com.zjwfan.simplestock.recivers.AmStockAlarmReciver;
import com.zjwfan.simplestock.recivers.PmStockAlarmReciver;
import com.zjwfan.simplestock.services.StockService;
import com.zjwfan.simplestock.utils.Util;

import android.app.AlertDialog;
import android.widget.Toast;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zjw on 2016-03-03.
 */

public class Stock {
    public String id, name;
    public String open, yesterday, now, high, low;
    public String volume, turnover;
    public String[] buy = new String[]{"", "", "", "", ""};
    public String[] sale = new String[]{"", "", "", "", ""};
    public String[] buyPrice = new String[]{"", "", "", "", ""};
    public String[] salePrice = new String[]{"", "", "", "", ""};
    public String date, time;
    public Double goalPrice, percent, goalPriceHigh;
    public Long goalVolume, goalVolumeHigh;

    public static Stock getStockWithZeroGoal(String id){
        Stock stock = new Stock();
        stock.id = id;
        stock.goalPrice = 0.0;
        stock.goalPriceHigh = 0.0;
        stock.goalVolume = 0l;
        stock.goalVolumeHigh = 0l;
        return stock;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Stock) {
            Stock coverStock = (Stock)o;
            return id.equals(coverStock.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id + " " + name + " " + now + " " + percent + " " + date + " " + time;
    }

    public static CountDownTimer startStockRefreshTimer(final Context context, final HashSet<String> stockIds, final Response.Listener<String> listener, final Response.ErrorListener errorListener) {
        Calendar calander = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
        long currentMillis = calander.getTimeInMillis();
        Config config = Config.loadConfigFromSharedPre(context);
        int timerCount = config.getTimerCountSett();
        Util.loge("getTradeDuraTime" + getTradeDuraTime());
        CountDownTimer timer = new CountDownTimer(getTradeDuraTime(), timerCount) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.e("ZJWFAN", "onTick()");
                refreshStocks(context, stockIds, listener, errorListener);
            }

            @Override
            public void onFinish() {
                Log.e("ZJWFAN", "onFinish()");
                StockService.stopService(context);
            }
        };
        return timer;
    }

    public static void setAlarm(Context context, int gap, long stopMilis) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long triggerAtTime = SystemClock.elapsedRealtime() + gap;

        Log.e("ZJWFAN", "stopMilis - triggerAtTime = " + (stopMilis - triggerAtTime));
        if (triggerAtTime < stopMilis) {
            Intent i = new Intent(context, AlarmReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
            manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        } else {
            StockService.stopService(context);
        }
    }

    public static Timer startStockRefreshTask(final Context context, final HashSet<String> stockIds, final Response.Listener<String> listener, final Response.ErrorListener errorListener) {
        Config config = Config.loadConfigFromSharedPre(context);
        int timerCount = config.getTimerCountSett();
        final Timer timer = new Timer("RefreshStocks");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (Stock.isTradeTime()) {
                    refreshStocks(context, stockIds, listener, errorListener);
                } else {

//                    timerCount = Config.TIMER_MAX_COUNT;
                }
            }
        }, 0, timerCount);
        return timer;
    }

    public static void refreshStocks(Context context, HashSet<String> stockIds, Response.Listener<String> listener, Response.ErrorListener errorListener) {

        StringBuilder ids = new StringBuilder();
        for (String id : stockIds) {
            ids.append(id);
            ids.append(",");
        }
        queryStockInfoFromSina(context, ids.toString(), listener, errorListener);
    }


    private static void queryStockInfoFromSina(Context context, String list, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        String url = "http://hq.sinajs.cn/list=" + list;
//        Util.log("Stock.url = " + url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, listener, errorListener);
        MySingleton.getInstance(context).addToRequestQueue(stringRequest);
    }

    public static void sinaResponseStockInfo(Context context, SQLiteDatabase stockDb, Config config, String response, HashSet<String> stockIds, HashMap<String, Stock> stocksMap) {
//        Log.w("ZJWFAN", response);
        response = response.replace("\n", "");
        String[] stocks = response.split(";");

        for (String stock : stocks) {
            String[] leftRight = stock.split("=");
            if (leftRight.length < 2)
                continue;


            String right = leftRight[1].replaceAll("\"", "");
            if (right.isEmpty()){
                handlerSinaResponseRightIsEmpty(context, stockDb, stockIds, leftRight[0].split("_")[2]);
                continue;
            }


            String left = leftRight[0];
            if (left.isEmpty())
                continue;

            Stock stockNow = new Stock();
            stockNow.id = left.split("_")[2];

            String[] values = right.split(",");

            if (values.length < 31){
                Log.e("ZJWFAN", "Error in sinaResponseStockInfo(String response) at 4587965416354");
                return;
            }

            stockNow.name = values[0];
            stockNow.open = values[1];
            stockNow.yesterday = values[2];
            stockNow.now = values[3];
            stockNow.high = values[4];
            stockNow.low = values[5];
            stockNow.volume = values[8];
            stockNow.turnover = values[9];

            for (int i = 0; i < 10; i++) {
                if (i % 2 == 0) {
                    stockNow.buy[i/2] = values[10 + i];
                    stockNow.sale[i/2] = values[20 + i];
                } else {
                    stockNow.buyPrice[i/2] = values[10 + i];
                    stockNow.salePrice[i/2] = values[20 + i];
                }
            }

            stockNow.date = values[values.length - 3];
            stockNow.time = values[values.length - 2];

            Double dNow = Double.parseDouble(stockNow.now);
            Double dYesterday = Double.parseDouble(stockNow.yesterday);
            Double dIncrease = dNow - dYesterday;
            Double dPercent = dIncrease / dYesterday * 100;
            stockNow.percent = dPercent;

            StockSQLiteOpenHelper.updateStockDB(stockDb, stockNow);
            handlerStock(context, config, stockNow, stocksMap);
        }
    }

    private static void handlerSinaResponseRightIsEmpty(Context context, SQLiteDatabase stockDb, HashSet<String> stockIds, String stockId) {
        Log.d("ZJWFAN", "handlerSinaResponseRightIsEmpty(String stockId)");
        int result = StockSQLiteOpenHelper.deleteStockDB(stockDb, stockId);
        stockIds.remove(stockId);
        if (result == 1) {
            new AlertDialog.Builder(context)
                    .setMessage(stockId + "不是一个有效的代码，已删除")
                    .setTitle("提示")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).create().show();
        }
    }


    private static void handlerStock(Context context, Config config, Stock stock, HashMap<String, Stock> stocksMap) {
        String sid = stock.id;
        sid = sid.replaceAll("sh", "");
        sid = sid.replaceAll("sz", "");

        double nowPrice = Double.parseDouble(stock.now);
        Double queryResult;

        queryResult = stocksMap.get(stock.id).goalPrice;
        if (queryResult > nowPrice && nowPrice > 0.1 && queryResult != 0.0) {

            Util.sendNotifation(context, config, Integer.parseInt(sid), stock.name, stock.now);
        }

        queryResult = stocksMap.get(stock.id).goalPriceHigh;
        if (queryResult < nowPrice && nowPrice > 0.1 && queryResult != 0.0) {

            Util.sendNotifation(context, config, Integer.parseInt(sid), stock.name, stock.now);
        }

        Long goalVolume, goalVolumeHigh;
        goalVolume = stocksMap.get(stock.id).goalVolume;
        goalVolumeHigh = stocksMap.get(stock.id).goalVolumeHigh;
        if (goalVolumeHigh == 0) {
            goalVolumeHigh = (long) config.getNoticeTradeSett();
        }

        String textHighBuy = "";
        String textLowBuy = "";
        String textHighSale = "";
        String textLowSale = "";
        String sBuy = context.getResources().getString(R.string.stock_buy);
        String sSell = context.getResources().getString(R.string.stock_sell);

        int lowBuyVolumeCount = 0;
        int lowSaleVolumeCount = 0;
        for (int i = 0; i < 5; i++) {

            if (Long.parseLong(stock.buy[i]) > goalVolumeHigh) {
                textHighBuy += sBuy + (i+1) + ":" + stock.buy[i] + ",";
            }

            if (Long.parseLong(stock.sale[i]) > goalVolumeHigh) {
                textHighSale += sSell + (i+1) + ":" + stock.sale[i] + ",";
            }


            if (Long.parseLong(stock.buy[i]) < goalVolume) {
                lowBuyVolumeCount++;
                textLowBuy += sBuy + (i+1) + ":" + stock.buy[i] + ",";
            }


            if (Long.parseLong(stock.sale[i]) < goalVolume) {
                lowSaleVolumeCount++;
                textLowSale += sSell + (i+1) + ":" + stock.sale[i] + ",";
            }
        }

        if(textHighBuy.length() > 0) {
            Util.sendNotifation(context, config, Integer.parseInt(sid), stock.name + "   " + stock.now, "↑ " + textHighBuy);
        }

        if(textHighSale.length() > 0) {
            Util.sendNotifation(context, config, Integer.parseInt(sid), stock.name +"   " + stock.now, "↑ " + textHighSale);
        }

        if(textLowBuy.length() > 0 && lowBuyVolumeCount > 3 ) {
            Log.e("ZJWFAN", "lowBuyVolumeCount = " + lowBuyVolumeCount);
            Util.sendNotifation(context, config, Integer.parseInt(sid), stock.name +"   " + stock.now, "↓ " + textLowBuy);
            Log.e("ZJWFAN", "↓ " + textLowBuy);
        }

        if(textLowSale.length() > 0 && lowSaleVolumeCount > 3) {
            Log.e("ZJWFAN", "lowBuyVolumeCount = " + lowSaleVolumeCount);
            Util.sendNotifation(context, config, Integer.parseInt(sid), stock.name +"   " + stock.now, "↓ " + textLowSale);
            Log.e("ZJWFAN", "↓ " + textLowSale);
        }


    }

    public static boolean isTradeTime() {
        Calendar calander = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
        int cDay = calander.get(Calendar.DAY_OF_WEEK);
        int cHour = calander.get(Calendar.HOUR_OF_DAY);
        if (cDay >1 && cDay < 7){
            if (cHour > 8 && cHour < 11 || cHour > 12 && cHour < 15) {
                return true;
            }

            if (cHour == 11) {
                int cMinute = calander.get(Calendar.MINUTE);
                if (cMinute < 31) {
                    return true;
                }
            }

        }
        return false;
    }



    public static long getTradeStartTime() {
        Calendar calander = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
        long currentMillis = calander.getTimeInMillis();

        int cHour = calander.get(Calendar.HOUR_OF_DAY);
        if (cHour < 11) {
            calander.set(Calendar.HOUR_OF_DAY, 9);
            calander.set(Calendar.MINUTE, 0);
            return calander.getTimeInMillis() - currentMillis;
        } else if (cHour > 14) {
//            calander.set(Calendar.DAY_OF_MONTH, calander.get(Calendar.DAY_OF_MONTH) + 1);
            calander.set(Calendar.HOUR_OF_DAY, 23);
            calander.set(Calendar.MINUTE, 59);
            return calander.getTimeInMillis() - currentMillis + 5000;
        } else {
            calander.set(Calendar.HOUR_OF_DAY, 13);
            calander.set(Calendar.MINUTE, 0);
            return calander.getTimeInMillis() - currentMillis;
        }
    }

    public static long getTradeDuraTime() {
        Calendar calander = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
        long currentMillis = calander.getTimeInMillis();

        int cHour = calander.get(Calendar.HOUR_OF_DAY);
        if (cHour < 12) {
            calander.set(Calendar.HOUR_OF_DAY, 11);
            calander.set(Calendar.MINUTE, 30);
            return calander.getTimeInMillis() - currentMillis;
        } else {
            calander.set(Calendar.HOUR_OF_DAY, 15);
            calander.set(Calendar.MINUTE, 00);
            return calander.getTimeInMillis() - currentMillis;
        }
    }

    private static final int INTERVAL = 1000 * 60 * 60 * 24;// 24h

    public static void setAlarmAm(Context context) {
        //...
        final int REQUEST_CODE = 0;

        Intent intent = new Intent(context, AmStockAlarmReciver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context,
                REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Schedule the alarm!
        AlarmManager am = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                INTERVAL, sender);
    }

    public static void setAlarmPm(Context context) {
        //...
        final int REQUEST_CODE = 1;

        Intent intent = new Intent(context, PmStockAlarmReciver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context,
                REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Schedule the alarm!
        AlarmManager am = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 13);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                INTERVAL, sender);
    }
}