package com.udacity.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.net.Uri;
import android.os.Binder;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.util.ArrayList;

import timber.log.Timber;
import yahoofinance.Stock;

/**
 * Created by Hasnaa on 3/16/2017.
 */
public class RemoteView implements RemoteViewsService.RemoteViewsFactory {
    private ArrayList<StockItem> listItem = new ArrayList<>();
    private Context context = null;
    private int appWidgetId;
    private Cursor data ;

    public RemoteView(Context context, Intent intent) {
        this.context = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

    }



    @Override
    public int getCount() {
        return listItem.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {

////                    setRemoteContentDescription(views, description);


        final RemoteViews remoteView = new RemoteViews(
                        context.getPackageName(), R.layout.widget_list_item);
        StockItem item = listItem.get(i);
        remoteView.setTextViewText(R.id.widget_symbol, item.stockName);
        remoteView.setTextViewText(R.id.widget_price, "$"+item.stockPrice);
        remoteView.setTextViewText(R.id.widget_change,item.stockPercentageChange+"%");

        if (Float.parseFloat(item.stockPercentageChange) > 0) {
            remoteView.setInt(R.id.widget_change, "setBackgroundResource",R.drawable.percent_change_pill_green);
        } else {
            remoteView.setInt(R.id.widget_change, "setBackgroundResource",R.drawable.percent_change_pill_red);
        }

        Intent fillInIntent = new Intent();
        fillInIntent.putExtra(context.getString(R.string.intent_symbol_key),item.stockName );
        remoteView.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

        return remoteView;


    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        if (data != null) {
            data.close();
        }
        // This method is called by the app hosting the widget (e.g., the launcher)
        // However, our ContentProvider is not exported so it doesn't have access to the
        // data. Therefore we need to clear (and finally restore) the calling identity so
        // that calls use our process and permission
        final long identityToken = Binder.clearCallingIdentity();
        Uri uri= Contract.Quote.uri;
        data=context.getContentResolver().query(uri,null,null,null,null);
        listItem = new ArrayList<>();
        data.moveToFirst();
        int dataCount =data.getCount();
        for (int i=0 ; i<dataCount ; i++){
            StockItem stockItem = new StockItem();

            stockItem.stockID=data.getInt(Contract.Quote.POSITION_ID);

            stockItem.stockName=data.getString(Contract.Quote.POSITION_SYMBOL);
            stockItem.stockPrice=data.getString(Contract.Quote.POSITION_PRICE);
            stockItem.stockAbsChange=data.getString(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            stockItem.stockPercentageChange=data.getString(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

            listItem.add(stockItem);
            data.moveToNext();

        }

        if(data !=null)data.close();

        Binder.restoreCallingIdentity(identityToken);
    }

    @Override
    public void onDestroy() {
    }

    class StockItem {
        int stockID;
        String stockName ;
        String stockPrice;
        String stockAbsChange ;
        String stockPercentageChange ;
    }

}
