package com.zjwfan.simplestock.recivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zjwfan.simplestock.services.StockService;
import com.zjwfan.simplestock.utils.Util;

import java.util.Calendar;
import java.util.TimeZone;

public class AmStockAlarmReciver extends BroadcastReceiver {
    public AmStockAlarmReciver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Util.loge("AmStockAlarmReciver");
        Calendar calander = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
        int dayOfWeek = calander.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek >1 && dayOfWeek < 7) {
            context.startService(new Intent(context, StockService.class));
        }
    }
}
