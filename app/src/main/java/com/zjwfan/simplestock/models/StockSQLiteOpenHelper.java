package com.zjwfan.simplestock.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.zjwfan.simplestock.activities.MainActivity;
import com.zjwfan.simplestock.utils.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by zjw on 2016-03-01.
 */

public class StockSQLiteOpenHelper extends SQLiteOpenHelper {
    public final static String DB_FILEPATH = "/data/data/com.zjwfan.simplestock/databases/";
    public final static String DB_IMPORT_FILENAME = "stocks_backup.db";
    public final static String TABLE_NAME = "stocks";
    public final static String DB_NAME = "Stock_Info.db";
    public final static String COLUMN_ID_CODE = "_id";
    public final static String COLUMN_NAME = "name";
    public final static String COLUMN_OPEN = "open";
    public final static String COLUMN_YEARSTERDAY = "yesterday";
    public final static String COLUMN_NOW = "now";
    public final static String COLUMN_PERCENT = "percent";
    public final static String COLUMN_HIGH = "high";
    public final static String COLUMN_LOW = "low";
    public final static String COLUMN_VOLUME = "volume";
    public final static String COLUMN_VOLUME_GOAL = "volume_goal";
    public final static String COLUMN_VOLUME_GOAL_HIGH = "volume_goal_high";
    public final static String COLUMN_TURNOVER = "turnover";
    public final static String COLUMN_GOAL_HIGH = "goal_high";
    public final static String COLUMN_GOAL = "goal";
    public final static String COLUMN_HOLD_CAST = "hold_cast";
    public final static String COLUMN_HOLD_NUMBER = "hold_num";
    public final static String COLUMN_DATE = "date";
    public final static String COLUMN_TIME = "time";

    private volatile static StockSQLiteOpenHelper INSTANCE;

    public static final String CREATE_STOCKS = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
            + COLUMN_ID_CODE + " text primary key not null, "
            + COLUMN_NAME + " text, "
            + COLUMN_OPEN + " real, "
            + COLUMN_YEARSTERDAY + " real, "
            + COLUMN_NOW + " real, "
            + COLUMN_PERCENT + " real, " //这个还是得改回real，下一版本改进
            + COLUMN_HIGH + " real, "
            + COLUMN_LOW + " real, "
            + COLUMN_VOLUME + " real, "
            + COLUMN_VOLUME_GOAL + " real, "
            + COLUMN_VOLUME_GOAL_HIGH + " real, "
            + COLUMN_TURNOVER + " real, "
            + COLUMN_GOAL + " real, "
            + COLUMN_GOAL_HIGH + " real, "
            + COLUMN_HOLD_CAST + " real, "
            + COLUMN_HOLD_NUMBER + " real, "
            + COLUMN_DATE + " text, "
            + COLUMN_TIME + " text"
            + ")";


    public static StockSQLiteOpenHelper getInstance(Context context){
        if (INSTANCE == null) {
            synchronized (StockSQLiteOpenHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new StockSQLiteOpenHelper(context, StockSQLiteOpenHelper.DB_NAME, null, Util.getAppVersionCode(context));
                }
            }
        }
        return INSTANCE;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_STOCKS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            exportDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (oldVersion == 88 && oldVersion != newVersion) {
//            db.execSQL("ALTER TABLE " + StockSQLiteOpenHelper.TABLE_NAME + " ADD COLUMN " + StockSQLiteOpenHelper.COLUMN_VOLUME_GOAL + " real"); //88->
//            db.execSQL("ALTER TABLE " + StockSQLiteOpenHelper.TABLE_NAME + " ADD COLUMN " + StockSQLiteOpenHelper.COLUMN_VOLUME_GOAL_HIGH + " real"); //88->
        }
    }



    private StockSQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    private static ContentValues putStockIntoContentValues(Stock stock){
        ContentValues contentValues = new ContentValues();
        contentValues.put(StockSQLiteOpenHelper.COLUMN_ID_CODE, stock.id);
        contentValues.put(StockSQLiteOpenHelper.COLUMN_NAME, stock.name);

        contentValues.put(StockSQLiteOpenHelper.COLUMN_PERCENT, stock.percent);

        contentValues.put(StockSQLiteOpenHelper.COLUMN_OPEN, Double.parseDouble(stock.open));
        contentValues.put(StockSQLiteOpenHelper.COLUMN_YEARSTERDAY, Double.parseDouble(stock.yesterday));
        contentValues.put(StockSQLiteOpenHelper.COLUMN_NOW, Double.parseDouble(stock.now));
        contentValues.put(StockSQLiteOpenHelper.COLUMN_HIGH, Double.parseDouble(stock.high));
        contentValues.put(StockSQLiteOpenHelper.COLUMN_LOW, Double.parseDouble(stock.low));
        contentValues.put(StockSQLiteOpenHelper.COLUMN_VOLUME, Double.parseDouble(stock.volume));
        contentValues.put(StockSQLiteOpenHelper.COLUMN_TURNOVER, Double.parseDouble(stock.turnover));

        contentValues.put(StockSQLiteOpenHelper.COLUMN_DATE, stock.date);
        contentValues.put(StockSQLiteOpenHelper.COLUMN_TIME, stock.time);

        return contentValues;
    }

    private static ContentValues putStockTradeIntoContentValues(Stock stock){
        ContentValues contentValues = new ContentValues();
        contentValues.put(StockSQLiteOpenHelper.COLUMN_NAME, stock.name);

        contentValues.put(StockSQLiteOpenHelper.COLUMN_PERCENT, stock.percent);

        contentValues.put(StockSQLiteOpenHelper.COLUMN_OPEN, Double.parseDouble(stock.open));
        contentValues.put(StockSQLiteOpenHelper.COLUMN_YEARSTERDAY, Double.parseDouble(stock.yesterday));
        contentValues.put(StockSQLiteOpenHelper.COLUMN_NOW, Double.parseDouble(stock.now));
        contentValues.put(StockSQLiteOpenHelper.COLUMN_HIGH, Double.parseDouble(stock.high));
        contentValues.put(StockSQLiteOpenHelper.COLUMN_LOW, Double.parseDouble(stock.low));
        contentValues.put(StockSQLiteOpenHelper.COLUMN_VOLUME, Double.parseDouble(stock.volume));
        contentValues.put(StockSQLiteOpenHelper.COLUMN_TURNOVER, Double.parseDouble(stock.turnover));

        contentValues.put(StockSQLiteOpenHelper.COLUMN_DATE, stock.date);
        contentValues.put(StockSQLiteOpenHelper.COLUMN_TIME, stock.time);

        return contentValues;
    }


    public static HashMap<String, Stock> queryStockInfoFromStockDB(SQLiteDatabase stockDB) {
        HashMap<String, Stock> stockMap = new HashMap<String, Stock>();
        Cursor cursor = stockDB.query(StockSQLiteOpenHelper.TABLE_NAME, null, null, null, null, null, StockSQLiteOpenHelper.COLUMN_ID_CODE);
        if (cursor.moveToFirst()) {
            do {
                String string = cursor.getString(cursor.getColumnIndex(StockSQLiteOpenHelper.COLUMN_ID_CODE));
                Stock stock = new Stock();
                stock.now = cursor.getDouble(cursor.getColumnIndex(StockSQLiteOpenHelper.COLUMN_NOW)) + "";
                stock.percent = cursor.getDouble(cursor.getColumnIndex(StockSQLiteOpenHelper.COLUMN_PERCENT));
                stock.goalPrice = cursor.getDouble(cursor.getColumnIndex(StockSQLiteOpenHelper.COLUMN_GOAL));
                stock.goalPriceHigh = cursor.getDouble(cursor.getColumnIndex(StockSQLiteOpenHelper.COLUMN_GOAL_HIGH));
                stock.goalVolume = cursor.getLong(cursor.getColumnIndex(StockSQLiteOpenHelper.COLUMN_VOLUME_GOAL));
                stock.goalVolumeHigh = cursor.getLong(cursor.getColumnIndex(StockSQLiteOpenHelper.COLUMN_VOLUME_GOAL_HIGH));
                //...
                Log.i("ZJWFAN", "unCompleted! at StockSQLiteOpenHelper.java 's loadStockFromCursor(Cursor cursor) at 49846535435");

                stockMap.put(string, stock);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return stockMap;
    }


    public static HashSet<String> queryColumnStringInfoFromStockDB(SQLiteDatabase stockDB, String column) {
        HashSet<String> stringSet = new HashSet<String>();
        Cursor cursor = stockDB.query(StockSQLiteOpenHelper.TABLE_NAME, null, null, null, null, null, StockSQLiteOpenHelper.COLUMN_ID_CODE);
        if (cursor.moveToFirst()) {
            do {
                String string = cursor.getString(cursor.getColumnIndex(column));
                stringSet.add(string);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return stringSet;
    }

    public static HashSet<Double> queryColumnDoubleInfoFromStockDB(SQLiteDatabase stockDB, String column) {
        HashSet<Double> doubles = new HashSet<Double>();
        Cursor cursor = stockDB.query(StockSQLiteOpenHelper.TABLE_NAME, null, null, null, null, null, StockSQLiteOpenHelper.COLUMN_ID_CODE);
        if (cursor.moveToFirst()) {
            do {
                Double aDouble = cursor.getDouble(cursor.getColumnIndex(column));
                doubles.add(aDouble);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return doubles;
    }

    private static Stock loadStockFromCursor(Cursor cursor) {
        if (cursor == null) {
            Log.e("ZJWFAN", "Error! at StockSQLiteOpenHelper.java 's loadStockFromCursor(Cursor cursor) at 46874651351");
            return null;
        }
        Stock stock = new Stock();

        stock.id = cursor.getString(cursor.getColumnIndex(StockSQLiteOpenHelper.COLUMN_ID_CODE));
        stock.name = cursor.getString(cursor.getColumnIndex(StockSQLiteOpenHelper.COLUMN_NAME));
        stock.now = cursor.getDouble(cursor.getColumnIndex(StockSQLiteOpenHelper.COLUMN_NOW)) + "";
        stock.volume = cursor.getDouble(cursor.getColumnIndex(StockSQLiteOpenHelper.COLUMN_VOLUME)) + "";
        stock.goalVolume = cursor.getLong(cursor.getColumnIndex(StockSQLiteOpenHelper.COLUMN_VOLUME_GOAL));
        stock.goalVolumeHigh = cursor.getLong(cursor.getColumnIndex(StockSQLiteOpenHelper.COLUMN_VOLUME_GOAL_HIGH));
        //...
        Log.i("ZJWFAN", "unCompleted! at StockSQLiteOpenHelper.java 's loadStockFromCursor(Cursor cursor) at 49846535435");
        cursor.close();
        return stock;
    }

    public static Cursor queryStockDB(SQLiteDatabase stockDB) {
        return stockDB.query(StockSQLiteOpenHelper.TABLE_NAME, null, null, null, null, null, StockSQLiteOpenHelper.COLUMN_ID_CODE);
    }

    public static Stock queryStockIdByPosFromStockDB(SQLiteDatabase stockDB, int pos) {
        Cursor cursor = stockDB.query(StockSQLiteOpenHelper.TABLE_NAME, null, null, null, null, null, StockSQLiteOpenHelper.COLUMN_ID_CODE);
        cursor.moveToPosition(pos);
        Stock stock = loadStockFromCursor(cursor);
        return stock;
    }

    public static Double queryGoalPriceByStockIdFromStockDB(SQLiteDatabase stockDB, String stockId, boolean isHigh) {
        Cursor cursor = queryStockInfoByIdFromStockDB(stockDB, stockId);
        Double result;
        if (cursor.moveToFirst()) {
            if (isHigh) {
                result = cursor.getDouble(cursor.getColumnIndex(StockSQLiteOpenHelper.COLUMN_GOAL_HIGH));
                cursor.close();
                return result;
            } else {
                result = cursor.getDouble(cursor.getColumnIndex(StockSQLiteOpenHelper.COLUMN_GOAL));
                cursor.close();
                return result;
            }
        }
        Log.i("ZJWFAN", "The goal price unset!\nInfo in StockSqliteOpenHelper.java 's queryGoalPriceByStockIdFromStockDB(...).");
        return null;
    }

    public static Cursor queryStockInfoByIdFromStockDB(SQLiteDatabase stockDB, String stockIdString) {
        String[] StockIdArray = new String[] {stockIdString};
        return stockDB.query(StockSQLiteOpenHelper.TABLE_NAME, null, StockSQLiteOpenHelper.COLUMN_ID_CODE + " = ?", StockIdArray, null, null, null);
    }

    public static void insertStockDB(SQLiteDatabase stockDB, Stock stock) {
        ContentValues contentValues = putStockIntoContentValues(stock);

        stockDB.insert(StockSQLiteOpenHelper.TABLE_NAME, null, contentValues);
    }

    public static void insertStockIdIntoStockDB(SQLiteDatabase stockDB, String stockId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(StockSQLiteOpenHelper.COLUMN_ID_CODE, stockId);
        stockDB.insert(StockSQLiteOpenHelper.TABLE_NAME, null, contentValues);
    }

    public static void updateStockDB(SQLiteDatabase stockDB, Stock stock) {

        String[] StockIdArray = new String[] {stock.id};
        ContentValues contentValues = putStockTradeIntoContentValues(stock);
        stockDB.update(StockSQLiteOpenHelper.TABLE_NAME, contentValues, StockSQLiteOpenHelper.COLUMN_ID_CODE + " = ?", StockIdArray);
    }

    public static void updateGoalPriceStockDB(SQLiteDatabase stockDB, String stockIdString, Double goalPrice, boolean isHigh) {
        String[] StockIdArray = new String[] {stockIdString};
        ContentValues contentValues = new ContentValues();
        if (isHigh) {
            contentValues.put(StockSQLiteOpenHelper.COLUMN_GOAL_HIGH, goalPrice);
            MainActivity.updateGoalPriceIntoStockMap(stockIdString, goalPrice, true);
        } else {
            contentValues.put(StockSQLiteOpenHelper.COLUMN_GOAL, goalPrice);
            MainActivity.updateGoalPriceIntoStockMap(stockIdString, goalPrice, false);
        }

        Log.d("ZJWFAN", "stockDB.update\t" + stockIdString + "  " + goalPrice);
        stockDB.update(StockSQLiteOpenHelper.TABLE_NAME, contentValues, StockSQLiteOpenHelper.COLUMN_ID_CODE + " = ?", StockIdArray);
    }

    public static void updateGoalVolumeStockDB(SQLiteDatabase stockDB, String stockIdString, Long goalVolume, boolean isHigh) {
        String[] StockIdArray = new String[] {stockIdString};
        ContentValues contentValues = new ContentValues();
        if (isHigh) {
            contentValues.put(StockSQLiteOpenHelper.COLUMN_VOLUME_GOAL_HIGH, goalVolume);
            MainActivity.updateGoalVolumeIntoStockMap(stockIdString, goalVolume, true);
        } else {
            contentValues.put(StockSQLiteOpenHelper.COLUMN_VOLUME_GOAL, goalVolume);
            MainActivity.updateGoalVolumeIntoStockMap(stockIdString, goalVolume, false);
        }

        Log.e("ZJWFAN", "stockDB.update\t" + stockIdString + "  " + goalVolume);
        stockDB.update(StockSQLiteOpenHelper.TABLE_NAME, contentValues, StockSQLiteOpenHelper.COLUMN_ID_CODE + " = ?", StockIdArray);
    }


    public static int deleteStockDB(SQLiteDatabase stockDB, String stockIdString) {

        String[] StockIdArray = new String[] {stockIdString};
        return stockDB.delete(StockSQLiteOpenHelper.TABLE_NAME, StockSQLiteOpenHelper.COLUMN_ID_CODE + " = ?", StockIdArray);
    }


    public static void replaceStockDB(SQLiteDatabase stockDB, Stock stock) {

        ContentValues contentValues = putStockIntoContentValues(stock);

        Log.d("ZJWFAN", "replaceStockDB: " + stock.name);
        stockDB.replace(StockSQLiteOpenHelper.TABLE_NAME, null, contentValues);
    }


    public static void checkLocked(SQLiteDatabase db){
        while (db.isDbLockedByCurrentThread()){
            Log.d("ZJWFAN", "SQLiteDatabase db is locked by other or current threads! at 4984684655");
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void enableWAL(SQLiteDatabase db){
        if(Build.VERSION.SDK_INT >= 11){
            boolean isWALenable = db.isWriteAheadLoggingEnabled();
            if (!isWALenable) {
                db.enableWriteAheadLogging();
            }
        }
    }

    private static void safeCloseSQLite(SQLiteDatabase sqLiteDatabase) {
        if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
            sqLiteDatabase.close();
        }
    }

    private static void safeCloseAppsDBHelper(StockSQLiteOpenHelper appsDateBaseHelper) {
        if (appsDateBaseHelper != null) {
            appsDateBaseHelper.close();
        }
    }

    /**
     * Copies the database file at the specified location over the current
     * internal application database.
     * */
    public boolean importDatabase(String dbPath) throws IOException {
        // Close the SQLiteOpenHelper so it will commit the created empty
        // database to internal storage.
        File newDb = new File(dbPath);
        File oldDb = new File(DB_FILEPATH + DB_NAME);
        Log.d("ZJWFAN", dbPath);
        if (newDb.exists()) {
            Util.copyFile(new FileInputStream(newDb), new FileOutputStream(oldDb));
            // Access the copied database so SQLiteHelper will cache it and mark
            // it as created.
            return true;
        }
        Log.e("ZJWFAN", "Somthing wrong! at importDatabase() 9846515875");
        return false;
    }

    public boolean exportDatabase() throws IOException {
        File sd = Environment.getExternalStorageDirectory();
        String dbPath = sd + "/" + DB_IMPORT_FILENAME;
        Log.d("ZJWFAN", dbPath);
//        close(); //command at 2016年3月21日14:35:43 95496
        File newDb = new File(dbPath);
        File oldDb = new File(DB_FILEPATH + DB_NAME);
        if (oldDb.exists()) {
            Util.copyFile(new FileInputStream(oldDb), new FileOutputStream(newDb));

//            getWritableDatabase().close(); //command at 2016年3月21日14:35:43 95496
            return true;
        }
        Log.e("ZJWFAN", "Somthing wrong! at exportDatabase() 685516565");
        return false;
    }


}
