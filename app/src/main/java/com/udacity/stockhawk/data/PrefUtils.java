package com.udacity.stockhawk.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.sync.QuoteSyncJob;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import butterknife.BindView;
import timber.log.Timber;

public final class PrefUtils {

    private PrefUtils() {

    }


    public static HashMap<String,Integer> getStocks(Context context) {
        String stocksKey = context.getString(R.string.pref_stocks_key);

        String initializedKey = context.getString(R.string.pref_stocks_initialized_key);
        String[] defaultStocksList = context.getResources().getStringArray(R.array.default_stocks);

        HashSet<String> defaultStocks = new HashSet<>(Arrays.asList(defaultStocksList));
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);


        boolean initialized = prefs.getBoolean(initializedKey, false);

        if (!initialized) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(initializedKey, true);
            //store status for every stock
            for (String s : defaultStocksList)
                editor.putInt(s, QuoteSyncJob.STOCK_STATUS_OK);

            editor.putStringSet(stocksKey, defaultStocks);
            editor.apply();

            HashMap<String, Integer> hm = new HashMap<>();
            for (String s : defaultStocks)
                hm.put(s,prefs.getInt(s,QuoteSyncJob.STOCK_STATUS_UNKNOWN));
            printPrefs(hm);
            return hm;
        }
        Set<String> temp= prefs.getStringSet(stocksKey, null);
        HashMap<String, Integer> hm = new HashMap<>();
        if(!temp.equals(null)) {
            for (String s : temp)
                hm.put(s, prefs.getInt(s, QuoteSyncJob.STOCK_STATUS_UNKNOWN));
        }
        printPrefs(hm);
        return hm;

    }

    public static void setStockToInvalid(String symbol , Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(symbol,QuoteSyncJob.STOCK_STATUS_INVALID);
        editor.commit();

    }

    public static void setStockToOK(String symbol , Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(symbol,QuoteSyncJob.STOCK_STATUS_OK);
        editor.commit();

    }

    public static int getStockStatus(String symbol,Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//        String key = context.getString(R.string.pref_stocks_key);
//        HashMap<String, Integer> stocks = getStocks(context);
//        if( stocks.containsKey(symbol))
//            return stocks.get(symbol);
//        return QuoteSyncJob.STOCK_STATUS_UNKNOWN;
        return prefs.getInt(symbol,QuoteSyncJob.STOCK_STATUS_UNKNOWN);
    }

    private static void printPrefs ( HashMap<String , Integer> hm){
//        HashMap<String , Integer> hm = getStocks(context);
        Timber.d("pref : "+hm.toString()+"\n");
    }



    public static void editStockPref(Context context, String symbol, Boolean add) {
        String key = context.getString(R.string.pref_stocks_key);
        HashMap<String, Integer> stocks = getStocks(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        if (add) {
            stocks.put(symbol,QuoteSyncJob.STOCK_STATUS_UNKNOWN);
            editor.putInt(symbol,QuoteSyncJob.STOCK_STATUS_UNKNOWN);

        } else {
            stocks.remove(symbol);
            editor.remove(symbol);
        }
        editor.putStringSet(key, stocks.keySet());
        editor.apply();
    }

    public static void addStock(Context context, String symbol) {
        editStockPref(context, symbol, true);
    }

    public static void removeStock(Context context, String symbol) {
        editStockPref(context, symbol, false);

    }


    public static int  getChartIntervalCol(Context context) {
        String key = context.getString(R.string.pref_chart_interval_key);
        String defaultValue = context.getString(R.string.pref_chart_interval_default);
        String [] array = context.getResources().getStringArray(R.array.chart_intervals);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String s =prefs.getString(key, defaultValue);
        Timber.v("interval is "+s+"now!");
        if(s.equals(array[0])) return Contract.Quote.POSITION_YEAR_HISTORY;
        if(s.equals(array[1])) return Contract.Quote.POSITION_MONTH_HISTORY;
        //        if(s.equals(array[1])) return Contract.Quote.POSITION_WEEK_HISTORY;


        return Contract.Quote.POSITION_YEAR_HISTORY;
//        return prefs.getString(key, defaultValue);

    }

    public static void setChartIntervalMode(Context context ,int interval) {
        String key = context.getString(R.string.pref_chart_interval_key);
        String [] array = context.getResources().getStringArray(R.array.chart_intervals);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key,array[interval]);
        Timber.v("interval setted to "+array[interval]);
        editor.apply();

    }

    public static String getDisplayMode(Context context) {
        String key = context.getString(R.string.pref_display_mode_key);
        String defaultValue = context.getString(R.string.pref_display_mode_default);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(key, defaultValue);
    }

    public static void toggleDisplayMode(Context context) {
        String key = context.getString(R.string.pref_display_mode_key);
        String absoluteKey = context.getString(R.string.pref_display_mode_absolute_key);
        String percentageKey = context.getString(R.string.pref_display_mode_percentage_key);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String displayMode = getDisplayMode(context);

        SharedPreferences.Editor editor = prefs.edit();

        if (displayMode.equals(absoluteKey)) {
            editor.putString(key, percentageKey);
        } else {
            editor.putString(key, absoluteKey);
        }

        editor.apply();
    }

}
