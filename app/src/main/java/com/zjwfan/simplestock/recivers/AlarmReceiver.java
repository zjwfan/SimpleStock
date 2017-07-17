package com.zjwfan.simplestock.recivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zjwfan.simplestock.services.StockService;

public class AlarmReceiver extends BroadcastReceiver {
    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, StockService.class);
        context.startService(i);

    }
}
