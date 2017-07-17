package com.zjwfan.simplestock.models;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by zjw on 2016-04-08.
 */
public class Config {
    private final static String SHARE_PRE_NOTIFI = "notifications_new_message";
    private final static String SHARE_PRE_VIBRATE = "notifications_new_message_vibrate";
    private final static String SHARE_PRE_SYNC_FRE = "sync_frequency";
    private final static String SHARE_PRE_TRADE_NOTI = "example_text";
    private final static String SHARE_PRE_Green_RED = "stock_switch_main_color";
    private final static String SHARE_PRE_RED_GOAL = "stock_switch_goal_color";
    private final static String SHARE_PRE_GOAL_DISPLAY = "stock_switch_goal_display";
    private final static String SHARE_PRE_RED_GOAL_PERCENT = "example_list_goal_percent_notice";
    private final static int StockLargeTrade_ = 10000000;
    public final static int TIMER_MAX_COUNT = 10000000;

    private boolean mIsNotification;
    private boolean mIsVibrate;
    private int mNoticeTradeSett;
    private int mTimerCountSett  = TIMER_MAX_COUNT;

    private AdapterConfig adapterConfig;

    public class AdapterConfig {
        private boolean mIsRedGreenColor;
        private boolean mIsRedGoalPrice;
        private boolean mIsGoalPriceDispaly;
        private double mRedGoalPricePercent;
        public AdapterConfig(boolean isRedGreenColor, boolean isRedGoalPrice, boolean isGoalDisplay, double redGoalPercent) {
            this.mIsGoalPriceDispaly = isRedGoalPrice;
            this.mIsRedGreenColor = isRedGreenColor;
            this.mIsGoalPriceDispaly = isGoalDisplay;
            mRedGoalPricePercent = redGoalPercent;
        }

        public boolean isRedGreenColor() {
            return mIsRedGreenColor;
        }

        public void setRedGreenColor(boolean redGreenColor) {
            mIsRedGreenColor = redGreenColor;
        }

        public boolean isRedGoalPrice() {
            return mIsRedGoalPrice;
        }

        public void setRedGoalPrice(boolean redGoalPrice) {
            mIsRedGoalPrice = redGoalPrice;
        }

        public boolean isGoalPriceDispaly() {
            return mIsGoalPriceDispaly;
        }

        public void setGoalPriceDispaly(boolean goalPriceDispaly) {
            mIsGoalPriceDispaly = goalPriceDispaly;
        }

        public double getRedGoalPricePercent() {
            return mRedGoalPricePercent;
        }

        public void setRedGoalPricePercent(double redGoalPricePercent) {
            mRedGoalPricePercent = redGoalPricePercent;
        }

        @Override
        public String toString() {
            return "\nAdapterConfig:" + "\t IsRedGreenColor = " + mIsRedGreenColor +   "\t RedGoalPercent = " + mRedGoalPricePercent
                    + "\t IsGoalDisplay = " + mIsGoalPriceDispaly +  "\t RedGoalPrice = " + mIsRedGoalPrice;
        }
    }

    public boolean isNotification() {
        return mIsNotification;
    }

    public void setNotification(boolean notification) {
        mIsNotification = notification;
    }

    public boolean isVibrate() {
        return mIsVibrate;
    }

    public void setVibrate(boolean vibrate) {
        mIsVibrate = vibrate;
    }

    public int getNoticeTradeSett() {
        return mNoticeTradeSett;
    }

    public void setNoticeTradeSett(int noticeTradeSett) {
        mNoticeTradeSett = noticeTradeSett;
    }

    public int getTimerCountSett() {
        return mTimerCountSett;
    }

    public void setTimerCountSett(int timerCountSett) {
        mTimerCountSett = timerCountSett;
    }

    public AdapterConfig getAdapterConfig() {
        return adapterConfig;
    }

    public void setAdapterConfig(AdapterConfig adapterConfig) {
        this.adapterConfig = adapterConfig;
    }

    @Override
    public String toString() {
        return "\nConfig:" + "\t Notification = " + mIsNotification + "\t Vibrate = " + mIsVibrate
                + "\t NoticeTradeSett = " + mNoticeTradeSett + "\t TimerCountSett = " + mTimerCountSett
                + adapterConfig.toString();
    }

    public static Config loadConfigFromSharedPre(Context context) {
        Config config = new Config();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        config.setNotification(sharedPreferences.getBoolean(SHARE_PRE_NOTIFI, true));
        config.setVibrate(sharedPreferences.getBoolean(SHARE_PRE_VIBRATE, true));
        config.setNoticeTradeSett(Integer.parseInt(sharedPreferences.getString(SHARE_PRE_TRADE_NOTI, StockLargeTrade_ + "")));
        config.setTimerCountSett(Integer.parseInt(sharedPreferences.getString(SHARE_PRE_SYNC_FRE, "1")) * 1000);

        boolean isRedGreenColor = sharedPreferences.getBoolean(SHARE_PRE_Green_RED, true);
        boolean isRedGoalPrice = sharedPreferences.getBoolean(SHARE_PRE_RED_GOAL, true);
        boolean isGoalPriceDispaly = sharedPreferences.getBoolean(SHARE_PRE_GOAL_DISPLAY, false);
        double redGoalPricePercent = Integer.parseInt(sharedPreferences.getString(SHARE_PRE_RED_GOAL_PERCENT, "98"));

        config.setAdapterConfig(config. new AdapterConfig(isRedGreenColor, isRedGoalPrice, isGoalPriceDispaly, redGoalPricePercent));
        Log.d("ZJWFAN", "loadConfigFromSharedPre():" + config.toString());
        return config;
    }
}
