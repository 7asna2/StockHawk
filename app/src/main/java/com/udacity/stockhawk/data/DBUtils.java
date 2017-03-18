package com.udacity.stockhawk.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.Time;
import android.util.Pair;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import timber.log.Timber;

/**
 * Created by hasna2 on 26/01/2017.
 */
public class DBUtils {


//    [33, MSFT, 64, -0.06, -0.09, 1486357200000, 64.00

    public static ArrayList<String> getStock(Context context , String symbol) {
        ArrayList<String> al = new ArrayList<>();
        Uri uri=Contract.Quote.makeUriForStock(symbol);
        Cursor cur = context.getContentResolver().query(uri,null,null,Contract.Quote.QUOTE_COLUMNS,null);
        try {

            if (cur.getCount() > 0) {
                cur.moveToFirst();
                al.add(cur.getString(Contract.Quote.POSITION_ID));
                al.add(cur.getString(Contract.Quote.POSITION_SYMBOL));
                al.add(cur.getString(Contract.Quote.POSITION_PRICE));
                al.add(cur.getString(Contract.Quote.POSITION_ABSOLUTE_CHANGE));
                al.add(cur.getString(Contract.Quote.POSITION_PERCENTAGE_CHANGE));
                Timber.v("xxx :" + al.toString());

            }
        }catch (NullPointerException e){
            Timber.e(e.getMessage()+"cursor doesnt load ");
            return null;
        }
        return al;
    }



    public static ArrayList< Pair<String,Float> > getHistory (Context context , String symbol){
        ArrayList< Pair<String,Float> > historyList = new ArrayList<>();
        Uri uri=Contract.Quote.makeUriForStock(symbol);
        Cursor cur = context.getContentResolver().query(uri,null,null,Contract.Quote.QUOTE_COLUMNS,null);

        int interval = PrefUtils.getChartIntervalCol(context);
        try {

            if (cur.getCount() > 0) {
                cur.moveToFirst();
                String history_string = cur.getString(interval);
                StringTokenizer stringTokenizer = new StringTokenizer(history_string);
                while (stringTokenizer.hasMoreTokens()) {
                    String date = getFormattedMonthDay(Long.parseLong(stringTokenizer.nextToken()
                            .substring(0, 12)));
                    float value = Float.parseFloat(stringTokenizer.nextToken());
                    historyList.add( new Pair<>(date , value));
                }
            }
        }catch (NullPointerException e){
            Timber.e(e.getMessage()+"cursor doesnt load ");
            return  null;
        }
        return historyList;
    }


    public static String getFormattedMonthDay(long val){
        val*=10;
        Date date=new Date(val);
        SimpleDateFormat df2 = new SimpleDateFormat("dd/MM");
        String dateText = df2.format(date);
        return (dateText);
    }
}



