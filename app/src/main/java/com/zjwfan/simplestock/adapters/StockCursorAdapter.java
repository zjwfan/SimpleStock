package com.zjwfan.simplestock.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.zjwfan.simplestock.R;
import com.zjwfan.simplestock.activities.MainActivity;
import com.zjwfan.simplestock.models.Config;
import com.zjwfan.simplestock.models.StockSQLiteOpenHelper;

/**
 * Created by zjw on 2016-03-10.
 */
public class StockCursorAdapter extends CursorAdapter {
    private MainActivity mMainActivity;
    private LayoutInflater mInflater;
    private boolean mIsRedGreenDisplay;
    private boolean mIsRedGoalPrice;
    private boolean mIsGoalDisplay;
    private double mRedGoalPercent;
    private boolean mIsQuickDelete = false;

    public void onUpdateSeeting(Config.AdapterConfig config) {
        Log.d("ZJWFAN", "onUpdateSeeting");
        mIsRedGoalPrice = config.isRedGoalPrice();
        mIsRedGreenDisplay = config.isRedGreenColor();
        mIsGoalDisplay = config.isGoalPriceDispaly();
        mRedGoalPercent = config.getRedGoalPricePercent();
    }

    public void setISQuickDelete(boolean isQuickDelete){
        mIsQuickDelete = isQuickDelete;
    }

    public boolean getISQuickDelete(){
        return mIsQuickDelete;
    }

    public StockCursorAdapter(MainActivity mainActivity, Cursor c, int flags) {
        super(mainActivity, c, flags);
        this.mMainActivity = mainActivity;
        mInflater = (LayoutInflater) mMainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.stock_listitem, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView stockIdTextView, stockNameTextView, stockPercentTextView, stockNowTextView;
        TextView stockGoalTextView, stockGoalHighTextView;
        final String stockIdString, stockNameString;
        Double stockGoalDouble, stockGoalHighDouble, stockNowDouble, stockPercentDouble;

        stockIdTextView = (TextView) view.findViewById(R.id.stock_id);
        stockNameTextView = (TextView) view.findViewById(R.id.stock_name);
        stockPercentTextView = (TextView) view.findViewById(R.id.stock_percent);
        stockNowTextView = (TextView) view.findViewById(R.id.stock_now);

        stockGoalTextView = (TextView) view.findViewById(R.id.stock_goal);
        stockGoalHighTextView = (TextView) view.findViewById(R.id.stock_goal_high);

        stockIdString = cursor.getString(cursor.getColumnIndex(StockSQLiteOpenHelper.COLUMN_ID_CODE));
        stockNameString = cursor.getString(cursor.getColumnIndex(StockSQLiteOpenHelper.COLUMN_NAME));
        stockPercentDouble = cursor.getDouble(cursor.getColumnIndex(StockSQLiteOpenHelper.COLUMN_PERCENT));
        stockNowDouble = cursor.getDouble(cursor.getColumnIndex(StockSQLiteOpenHelper.COLUMN_NOW));

        stockIdTextView.setText(stockIdString);
        stockNameTextView.setText(stockNameString);
        stockPercentTextView.setText(String.format("%.2f%%", stockPercentDouble));
        stockNowTextView.setText(String.format("%.2f", stockNowDouble));

        if (mIsRedGreenDisplay) {
            if (stockPercentDouble < 0.0) { //此处可由用户设置
                stockNowTextView.setTextColor(Color.parseColor("#00950C"));
                stockPercentTextView.setTextColor(Color.parseColor("#00950C"));
            } else if (stockPercentDouble > 0.0) {
                stockNowTextView.setTextColor(Color.parseColor("#FF0300"));
                stockPercentTextView.setTextColor(Color.parseColor("#FF0300"));
            }
        } else {
            stockNowTextView.setTextColor(Color.BLACK);
            stockPercentTextView.setTextColor(Color.BLACK);
        }

        if (stockPercentDouble == -100.0) {
            stockPercentTextView.setText("——");
        }


        if (mIsGoalDisplay) {
            stockGoalDouble = cursor.getDouble(cursor.getColumnIndex(StockSQLiteOpenHelper.COLUMN_GOAL));
            stockGoalHighDouble = cursor.getDouble(cursor.getColumnIndex(StockSQLiteOpenHelper.COLUMN_GOAL_HIGH));
            stockGoalHighTextView.setText(String.format("%.2f", stockGoalHighDouble));
            stockGoalTextView.setText(String.format("%.2f", stockGoalDouble));

            if (mIsRedGoalPrice) {
                if (stockNowDouble * mRedGoalPercent * 0.01 < stockGoalDouble) {
                    stockGoalTextView.setTextColor(Color.parseColor("#FF0300"));
                }
                if (stockGoalHighDouble != 0 && stockGoalHighDouble * mRedGoalPercent * 0.01 < stockNowDouble) {
                    stockGoalHighTextView.setTextColor(Color.parseColor("#FF0300"));
                }
            }

            stockGoalTextView.setVisibility(View.VISIBLE);
            stockGoalHighTextView.setVisibility(View.VISIBLE);

        } else {
            stockGoalTextView.setVisibility(View.GONE);
            stockGoalHighTextView.setVisibility(View.GONE);
        }


        Button stockDeleteButton;
        stockDeleteButton = (Button) view.findViewById(R.id.stock_delete);
        if (mIsQuickDelete) {
            stockDeleteButton.setVisibility(View.VISIBLE);
            stockDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMainActivity.deleteStock(stockIdString);
                    mMainActivity.refreshListViewDisplay();
                }
            });

            stockGoalTextView.setVisibility(View.GONE);
            stockGoalHighTextView.setVisibility(View.GONE);
        }else {
            stockDeleteButton.setVisibility(View.GONE);
//            stockGoalTextView.setVisibility(View.VISIBLE);
//            stockGoalHighTextView.setVisibility(View.VISIBLE);
        }

    }
}
