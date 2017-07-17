package com.zjwfan.simplestock.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.baoyz.widget.PullRefreshLayout;
import com.zjwfan.simplestock.R;
import com.zjwfan.simplestock.adapters.StockCursorAdapter;
import com.zjwfan.simplestock.models.Config;
import com.zjwfan.simplestock.models.Stock;
import com.zjwfan.simplestock.models.StockSQLiteOpenHelper;
import com.zjwfan.simplestock.network.MySingleton;
import com.zjwfan.simplestock.notifications.StockItemPopupWindow;
import com.zjwfan.simplestock.services.StockService;
import com.zjwfan.simplestock.utils.Util;

import java.util.HashMap;
import java.util.HashSet;


public class MainActivity extends AppCompatActivity {
    private final static String TAG = "ZJWFAN";
    private final static String ShIndex = "sh000001";
    private final static String SzIndex = "sz399001";
    private final static String ChuangIndex = "sz399006";


    private static HashSet<String> StockIds = new HashSet<String>();
    private static HashMap<String, Stock> StocksMap = new HashMap<String, Stock>();
    private final static String SHARE_PRE = "simple_stock_share_preferences_zjwfan";
    private final static String SHARE_PRE_USE_COUNT = "simple_stock_share_preferences_zjwfan_use_count";

    private Config mConfig;
    private String mFilePath;

    private ListView mListView;
    private LinearLayout mMainLayout;
    private PullRefreshLayout mPullRefreshLayout;
    private SQLiteDatabase mStockDB;
    private StockCursorAdapter mStockCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        mMainLayout = (LinearLayout) findViewById(R.id.main_layout);

        StockService.startService(this);

        initSeeting();
        initToolbar();
        initPullRefresh();
        initListView();
        initStockIds();
        environmentCheck();

    }

    private void initSeeting() {
        mConfig = new Config();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_launcher);
        toolbar.setOnMenuItemClickListener(onMenuItemClick);
    }

    private void initPullRefresh() {
        mPullRefreshLayout = (PullRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mPullRefreshLayout.setRefreshStyle(PullRefreshLayout.STYLE_MATERIAL);
        mPullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshStocks();
            }
        });
    }

    private void environmentCheck() {
        if (!Util.isOnline(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), "网络不在状态", Toast.LENGTH_LONG).show();
        }
    }

    private void initStockIds() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARE_PRE, Context.MODE_PRIVATE);
        int result = sharedPreferences.getInt(SHARE_PRE_USE_COUNT, 0);
        if (result == 0) {
            //setAlarm();
            addStockIdWithPrefix(ShIndex);
            addStockIdWithPrefix(SzIndex);
            addStockIdWithPrefix(ChuangIndex);
            for (String id : Util.PRE_ADD_STOCKS) {
                addStockIdWithPrefix(addPrefixToStockId(id));
            }
            sharedPreferences.edit().putInt(SHARE_PRE_USE_COUNT, ++result).apply();
        }

        StockIds = StockSQLiteOpenHelper.queryColumnStringInfoFromStockDB(mStockDB, StockSQLiteOpenHelper.COLUMN_ID_CODE);
        StocksMap = StockSQLiteOpenHelper.queryStockInfoFromStockDB(mStockDB);

        if (StockIds.size() != StocksMap.size())
            Log.e(TAG, "StockIds.size() != StocksMap.size()" + "at 496846466");

    }

    /*
    private void setAlarm() {
        Stock.setAlarmAm(this);
        Stock.setAlarmPm(this);
    }
    */

    private void addStockIdWithPrefix(String stockIdWithPrefix) {
        if (stockIdWithPrefix == null) {
            Toast.makeText(getApplicationContext(), "输入的代码("+ stockIdWithPrefix +")有误，请检查", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!StockIds.contains(stockIdWithPrefix)) {
            StockIds.add(stockIdWithPrefix);
            StocksMap.put(stockIdWithPrefix, Stock.getStockWithZeroGoal(stockIdWithPrefix));
            StockSQLiteOpenHelper.insertStockIdIntoStockDB(mStockDB, stockIdWithPrefix);
        }

        Toast.makeText(getApplicationContext(), "已经添加" + stockIdWithPrefix, Toast.LENGTH_SHORT).show();
        refreshStocks();
    }

    public void addStockIdWithoutPrefix(String stockIdWithoutPrefix) {
        String stockIdWithPrefix = addPrefixToStockId(stockIdWithoutPrefix);
        addStockIdWithPrefix(stockIdWithPrefix);
    }

    private String addPrefixToStockId(String stockId) {
        String result;
        if(stockId.length() != 6)
            return null;
        if (stockId.startsWith("6")) {
            result = "sh" + stockId;
        } else if (stockId.startsWith("0") || stockId.startsWith("3")) {
            result = "sz" + stockId;
        } else if (stockId.startsWith("1")) {
            result = "sz" + stockId;
        } else if (stockId.contains("999999")){
            result = ShIndex;
        } else
            return null;

        return result;
    }

    private void initListView() {
        StockSQLiteOpenHelper stockSQLiteOpenHelper = StockSQLiteOpenHelper.getInstance(getApplicationContext());
        mStockDB = stockSQLiteOpenHelper.getWritableDatabase();

        mListView = (ListView) findViewById(R.id.fullscreen_content);
        mListView.requestFocus();
        mStockCursorAdapter = new StockCursorAdapter(MainActivity.this, StockSQLiteOpenHelper.queryStockDB(mStockDB), CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        mListView.setAdapter(mStockCursorAdapter);
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Stock stock = StockSQLiteOpenHelper.queryStockIdByPosFromStockDB(mStockDB, position);
                View popupView = getLayoutInflater().inflate(R.layout.pop_win_list_click, mListView, false);
                StockItemPopupWindow popupWindow = new StockItemPopupWindow(MainActivity.this, popupView, stock);
                popupWindow.showAtLocation(findViewById(R.id.fullscreen_content), Gravity.CENTER, 0, 0);
                return true;
            }
        });
    }

    public void refreshListViewDisplay() {
        Cursor newCursor = StockSQLiteOpenHelper.queryStockDB(mStockDB);
        mStockCursorAdapter.changeCursor(newCursor);
        mStockCursorAdapter.notifyDataSetChanged();
        mPullRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mConfig = Config.loadConfigFromSharedPre(this);
        mStockCursorAdapter.onUpdateSeeting(mConfig.getAdapterConfig());
    }


    private void refreshStocks() {
        Log.d(TAG, "refreshStocks()");
        StringBuilder ids = new StringBuilder();
        for (String id : StockIds) {
            ids.append(id);
            ids.append(",");
        }
        queryStockInfoFromSina(ids.toString());
    }

    public void queryStockInfoFromSina(String list) {
        String url = "http://hq.sinajs.cn/list=" + list;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Stock.sinaResponseStockInfo(MainActivity.this, mStockDB, mConfig, response, StockIds, StocksMap);
                        refreshListViewDisplay();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        MySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem myActionMenuItem = menu.findItem( R.id.action_search);
        final SearchView searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setQueryHint("300369");
        searchView.setIconifiedByDefault(true);
        searchView.setInputType(InputType.TYPE_CLASS_NUMBER);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                if (query.length() == 8) {
                    addStockIdWithPrefix(query);
                } else {
                    addStockIdWithoutPrefix(query);
                }

                searchView.onActionViewCollapsed();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1017 && resultCode == RESULT_OK) {
            Log.e("ZJWFAN", "onActivityResult: mFilePath = " + mFilePath);
            mFilePath = data.getStringExtra("apk_path");
            if (mFilePath != null) {
                try {
                    Log.e("ZJWFAN", "item_improt_database: mFilePath = " + mFilePath);
                    StockSQLiteOpenHelper.getInstance(getApplicationContext()).importDatabase(mFilePath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public void startChooseFileActivity() {
        Intent intent = new Intent(this, FileExplorerActivity.class);
        startActivityForResult(intent, 1017);
    }

    /*

应大家要求把我常用的指标参数写出来，给大家参考一下。 最常用指标， BOLL，参数（99），如果软件要求填宽度的话，宽度值为2。
各种软件中计算BOLL的公式稍有区别，我用的软件和公式是通达信的。 下跌之后底部整理时期， MA，四条线，参数分别是14,60,99,888。
有的软件会有两组均线供投资者切换来看，一组是MA，一组是MA2，我一般只改MA，MA2还用系统默认参数。 协助判断指标, MACD，指标参数设置,10,20,5。
成交量用，VOL-TDX而不是默认的VOL，因为前者会自动在盘中虚拟今日的总成交量。


 */


    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            String msg = "";
            switch (menuItem.getItemId()) {
                case R.id.item_settings:
                    startSettingActivity();
                    break;
                case R.id.item_refresh:
                    refreshStocks();
                    break;
                case R.id.item_improt_database:
                    Toast.makeText(getApplicationContext(), "请选择要导入的数据文件", Toast.LENGTH_SHORT).show();
                    startChooseFileActivity();
                    break;
                case R.id.item_export_database:
                    try {
                        StockSQLiteOpenHelper.getInstance(getApplicationContext()).exportDatabase();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        Toast.makeText(getApplicationContext(), "数据文件\"" + StockSQLiteOpenHelper.DB_IMPORT_FILENAME + "\"已导出至SD卡根目录", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.item_quick_delete:
                    if (mStockCursorAdapter.getISQuickDelete()) {
                        mStockCursorAdapter.setISQuickDelete(false);
                        menuItem.setTitle(R.string.action_quick_delete);
                    }else {
                        mStockCursorAdapter.setISQuickDelete(true);
                        menuItem.setTitle(R.string.action_quit_quick_delete);
                    }
                    refreshListViewDisplay();
                    break;
                case R.id.action_search:
                    Log.e(TAG, "R.id.action_search");
                    break;
                case R.id.item_start_service:
                    startService(new Intent(MainActivity.this, StockService.class));
                    break;
                case R.id.item_stop_service:
                    stopService(new Intent(MainActivity.this, StockService.class));
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    private void startSettingActivity() {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), SettingsActivity.class);
        startActivity(intent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public SQLiteDatabase getStockDB() {
        return mStockDB;
    }

    public void deleteStock(String stockId) {
        StockSQLiteOpenHelper.deleteStockDB(mStockDB, stockId);
        StockIds.remove(stockId);
    }

    public void antiBokehDisplay() {
        mMainLayout.setAlpha(1.0f);
    }

    public void bokehDisplay() {
        mMainLayout.setAlpha(0.2f);
    }

    public static void updateGoalPriceIntoStockMap(String id, double newGoal, boolean isHigh) {
        Stock stock = StocksMap.get(id);
        if (isHigh) {
            stock.goalPriceHigh = newGoal;
        } else {
            stock.goalPrice = newGoal;
        }
        StocksMap.put(id, stock);
    }

    public static void updateGoalVolumeIntoStockMap(String id, Long newVolume, boolean isHigh) {
        Stock stock = StocksMap.get(id);
        if (isHigh) {
            stock.goalVolumeHigh = newVolume;
        } else {
            stock.goalVolume = newVolume;
        }
        StocksMap.put(id, stock);
    }
}
