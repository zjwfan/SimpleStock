package com.zjwfan.simplestock.notifications;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.zjwfan.simplestock.R;
import com.zjwfan.simplestock.activities.MainActivity;
import com.zjwfan.simplestock.models.Stock;
import com.zjwfan.simplestock.models.StockSQLiteOpenHelper;

/**
 * Created by zjw on 2016-03-19.
 */
public class StockItemPopupWindow extends PopupWindow {
    private MainActivity mMainActivity;
    private LayoutInflater mLayoutInflater;

    public StockItemPopupWindow(MainActivity mainActivity, View popupView, final Stock stock) {
        super(popupView, LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
        this.mMainActivity = mainActivity;
        this.mLayoutInflater = mainActivity.getLayoutInflater();
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        this.setBackgroundDrawable(new BitmapDrawable());

        TextView textView_title = (TextView) popupView.findViewById(R.id.pop_textview_title);

        final EditText editTextGoalPrice = (EditText) popupView.findViewById(R.id.pop_goal_price_edittext);
        Button buttonOkHighPrice = (Button) popupView.findViewById(R.id.pop_button_ok_high_goal_price_);
        Button buttonOkLowPrice = (Button) popupView.findViewById(R.id.pop_button_ok_low_goal_price_);

        final EditText editTextVolume = (EditText) popupView.findViewById(R.id.pop_edittext_volume);
        Button buttonOkHighVolume = (Button) popupView.findViewById(R.id.pop_button_ok_high_volume);
        Button buttonOkLowVolume = (Button) popupView.findViewById(R.id.pop_button_ok_low_volume);

        Button buttonCancel = (Button) popupView.findViewById(R.id.pop_button_cancel);
        Button buttonDelete = (Button) popupView.findViewById(R.id.pop_button_delete);


        editTextGoalPrice.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editTextVolume.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        textView_title.setText(stock.name + "(" + stock.id + ")");
        buttonDelete.setText("删除" + stock.name);
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);
                builder.setTitle("警告");
                builder.setMessage("确定要删除" + stock.name + "(" + stock.id + ")" + "？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mMainActivity.deleteStock(stock.id);
                        mMainActivity.refreshListViewDisplay();
                        dismiss();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });
                builder.create().show();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


        editTextGoalPrice.setHint(stock.now);
        buttonOkLowPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String goal_Price = editTextGoalPrice.getText().toString();
                StockSQLiteOpenHelper.updateGoalPriceStockDB(mMainActivity.getStockDB(), stock.id, Double.parseDouble(goal_Price), false);
                mMainActivity.refreshListViewDisplay();
                dismiss();
            }
        });

        buttonOkHighPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String goal_Price = editTextGoalPrice.getText().toString();
                StockSQLiteOpenHelper.updateGoalPriceStockDB(mMainActivity.getStockDB(), stock.id, Double.parseDouble(goal_Price), true);
                mMainActivity.refreshListViewDisplay();
                dismiss();
            }
        });

        editTextVolume.setHint(stock.goalVolumeHigh + "/" + stock.goalVolume + " || " + stock.buy[1] + "/" + stock.sale[1]); //成交额： 改为  旧上限/旧下限  || 当前买1/卖1

        buttonOkLowVolume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String editGoalVlume = editTextVolume.getText().toString();
                StockSQLiteOpenHelper.updateGoalVolumeStockDB(mMainActivity.getStockDB(), stock.id, Long.parseLong(editGoalVlume), false);
                dismiss();
            }
        });

        buttonOkHighVolume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String editGoalVlume = editTextVolume.getText().toString();
                StockSQLiteOpenHelper.updateGoalVolumeStockDB(mMainActivity.getStockDB(), stock.id, Long.parseLong(editGoalVlume), true);
                dismiss();
            }
        });

    }

    @Override
    public void dismiss() {
        super.dismiss();
        mMainActivity.antiBokehDisplay();
        mMainActivity.getApplicationContext().sendBroadcast(new Intent("com.zjwfan.simplestock.reload.stockids"));
        Log.e("ZJWFAN", "SEND  SEND  com.zjwfan.simplestock.reload.stockids");
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        mMainActivity.bokehDisplay();
    }

}
