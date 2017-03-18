package com.udacity.stockhawk.sync;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.IntDef;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

public final class QuoteSyncJob {

    static final int ONE_OFF_ID = 2;
    public static final String ACTION_DATA_UPDATED = "com.udacity.stockhawk.ACTION_DATA_UPDATED";
    private static final int PERIOD = 300000;
    private static final int INITIAL_BACKOFF = 10000;
    private static final int PERIODIC_ID = 1;




    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ STOCK_STATUS_OK,
//            STOCK_STATUS_SERVER_DOWN,
//            STOCK_STATUS_SERVER_INVALID,
            STOCK_STATUS_UNKNOWN,
            // when user enters invalid location
            STOCK_STATUS_INVALID
    })
    public @interface StockStatus{}
    public static final int STOCK_STATUS_OK = 2;
    //    public static final int LOCATION_STATUS_SERVER_DOWN = 3;
//    public static final int LOCATION_STATUS_SERVER_INVALID = 4;
    public static final int STOCK_STATUS_UNKNOWN = 0;
    public static final int STOCK_STATUS_INVALID=1;



    static void getQuotes(Context context) {

        Timber.d("Running sync job");

        Calendar fromYear = Calendar.getInstance();
        Calendar fromWeek = Calendar.getInstance();
        Calendar fromMonth = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        fromYear.add(Calendar.YEAR, -1);
        fromMonth.add(Calendar.MONTH,-1);
//        fromWeek.add(Calendar.WEEK_OF_MONTH,-1);

        try {

            Set<String> stockPref = PrefUtils.getStocks(context).keySet();
            Set<String> stockCopy = new HashSet<>();
            stockCopy.addAll(stockPref);
            String[] stockArray = stockPref.toArray(new String[stockPref.size()]);

            Timber.d(stockCopy.toString());

            if (stockArray.length == 0) {
                return;
            }

            Map<String, Stock> quotes = YahooFinance.get(stockArray);
            Iterator<String> iterator = stockCopy.iterator();

            Timber.d(quotes.toString());

            ArrayList<ContentValues> quoteCVs = new ArrayList<>();

            while (iterator.hasNext()) {
                String symbol = iterator.next();

                try{
                Stock stock = quotes.get(symbol);
                StockQuote quote = stock.getQuote();
                    float price = quote.getPrice().floatValue();
                    float change = quote.getChange().floatValue();
                    float percentChange = quote.getChangeInPercent().floatValue();
                    if (PrefUtils.getStockStatus(symbol, context) == QuoteSyncJob.STOCK_STATUS_UNKNOWN)
                        PrefUtils.setStockToOK(symbol, context);
                    // WARNING! Don't request historical data for a stock that doesn't exist!
                    // The request will hang forever X_x
                    List<HistoricalQuote> yearHistory = stock.getHistory(fromYear, to, Interval.WEEKLY);
//                    List<HistoricalQuote> weekHistory = stock.getHistory(fromWeek, to, Interval.DAILY);
                    List<HistoricalQuote> monthHistory = stock.getHistory(fromMonth, to, Interval.DAILY);

                    StringBuilder yearHistoryBuilder = new StringBuilder();

                    for (HistoricalQuote it : yearHistory) {
                        yearHistoryBuilder.append(it.getDate().getTimeInMillis());
                        yearHistoryBuilder.append(", ");
                        yearHistoryBuilder.append(it.getClose());
                        yearHistoryBuilder.append("\n");
                    }
//                    StringBuilder weekHistoryBuilder = new StringBuilder();
//
//                    for (HistoricalQuote it : weekHistory) {
//                        weekHistoryBuilder.append(it.getDate().getTimeInMillis());
//                        weekHistoryBuilder.append(", ");
//                        weekHistoryBuilder.append(it.getClose());
//                        weekHistoryBuilder.append("\n");
//                    }
                    StringBuilder monthHistoryBuilder = new StringBuilder();

                    for (HistoricalQuote it : monthHistory) {
                        monthHistoryBuilder.append(it.getDate().getTimeInMillis());
                        monthHistoryBuilder.append(", ");
                        monthHistoryBuilder.append(it.getClose());
                        monthHistoryBuilder.append("\n");
                    }

                    ContentValues quoteCV = new ContentValues();
                    quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                    quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
                    quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
                    quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);


                    quoteCV.put(Contract.Quote.COLUMN_YEAR_HISTORY, yearHistoryBuilder.toString());
//                    quoteCV.put(Contract.Quote.COLUMN_WEEK_HISTORY,  weekHistoryBuilder.toString());
                    quoteCV.put(Contract.Quote.COLUMN_MONTH_HISTORY, monthHistoryBuilder.toString());

                    quoteCVs.add(quoteCV);


                }catch(NullPointerException e){
                    PrefUtils.setStockToInvalid(symbol,context);
                    Timber.d( context.getResources().getString(R.string.error_stock_not_found,symbol));
//                    Toast.makeText(context, context.getResources().getString(R.string.error_stock_not_found,symbol),Toast.LENGTH_SHORT).show();
//                    PrefUtils.removeStock(context,symbol);
                }

            }

            context.getContentResolver()
                    .bulkInsert(
                            Contract.Quote.uri,
                            quoteCVs.toArray(new ContentValues[quoteCVs.size()]));
            // update database
            Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
            context.sendBroadcast(dataUpdatedIntent);

        } catch (IOException exception) {
            Timber.e(exception, "Error fetching stock quotes");
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void schedulePeriodic(Context context) {
        Timber.d("Scheduling a periodic task");


//
//        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
//                .setPeriodic(PERIOD)
//                .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);
//
//
//        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
//
//        scheduler.schedule(builder.build());
    }


    synchronized public static void initialize(final Context context) {

        schedulePeriodic(context);
        syncImmediately(context);

    }

//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    synchronized public static void syncImmediately(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            Intent nowIntent = new Intent(context, QuoteIntentService.class);
            context.startService(nowIntent);
        } else {

//            JobInfo.Builder builder = new JobInfo.Builder(ONE_OFF_ID, new ComponentName(context, QuoteJobService.class));
//
//
//            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
//                    .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);
//
//
//            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
//
//            scheduler.schedule(builder.build());


        }
    }


}
