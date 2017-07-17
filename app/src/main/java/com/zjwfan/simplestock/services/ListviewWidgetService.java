package com.zjwfan.simplestock.services;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.zjwfan.simplestock.R;

/**
 * Created by zjw on 2016-09-09.
 */
public class ListviewWidgetService extends RemoteViewsService {

    private static final boolean DB = true;
    private static final String TAG = "ZJWFAN";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        log("onGetViewFactory, intent=" + intent);
        return new MyWidgetFactory(getApplicationContext(), intent);
    }

    public static class MyWidgetFactory implements RemoteViewsService.RemoteViewsFactory {

        private Context mContext;

        private String[] mFoods = new String[] { "Apple", "Banana", "Pear", "Handset", "People", "Bar", "Wind", "Song",
                "Source code", "Screen", "Package", "Cup", "Computer" };

        // 构造
        public MyWidgetFactory(Context context, Intent intent) {
            log("MyWidgetFactory");
            mContext = context;
        }

        @Override
        public int getCount() {
            log("getCount");
            return mFoods.length;
        }

        @Override
        public long getItemId(int position) {
            log("getItemId");
            return position;
        }

        // 在调用getViewAt的过程中，显示一个LoadingView。
        // 如果return null，那么将会有一个默认的loadingView
        @Override
        public RemoteViews getLoadingView() {
            log("getLoadingView");
            return null;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            log("getViewAt, position=" + position);
            if (position < 0 || position >= getCount()) {
                return null;
            }
            RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.stock_listitem);
            views.setTextViewText(R.id.stock_goal_high, mFoods[position]);
            return views;
        }

        @Override
        public int getViewTypeCount() {
            log("getViewTypeCount");
            return 1;
        }

        @Override
        public boolean hasStableIds() {
            log("hasStableIds");
            return true;
        }

        @Override
        public void onCreate() {
            log("onCreate");
        }

        @Override
        public void onDataSetChanged() {
            log("onDataSetChanged");
        }

        @Override
        public void onDestroy() {
            log("onDestroy");
        }
    }

    private static void log(String log) {
        if (DB)
            Log.d(TAG, log);
    }

}
