package com.udacity.stockhawk.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.DBUtils;
import com.udacity.stockhawk.data.PrefUtils;

import java.util.ArrayList;
import java.util.Hashtable;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import yahoofinance.histquotes.Interval;

public class DetailActivity extends AppCompatActivity {

    @BindView(R.id.absolute_change)
    TextView absoluteChange;
    @BindView(R.id.percentage_change)
    TextView percentageChange;
    @BindView(R.id.price)
    TextView price;


    @BindView(R.id.stock_chart)
    LineChart stockLineChart;

    @BindView(R.id.interval)
    Spinner intervalSpinner;

    ArrayList<Pair<String,Float>> stockHistory;
    ArrayList<String> stockInfo;
    String symbol;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

//        getActionBar().setDisplayHomeAsUpEnabled(true);

        symbol = getIntent().getStringExtra(getString(R.string.intent_symbol_key));
        stockInfo =  DBUtils.getStock(this,symbol);

        String stockID = stockInfo .get(Contract.Quote.POSITION_ID);
        String stockSymbol = stockInfo.get(Contract.Quote.POSITION_SYMBOL);
        String stockPrice = stockInfo.get(Contract.Quote.POSITION_PRICE);
        String stockAbsoluteChange = stockInfo.get(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        String stockPercentageChange = stockInfo.get(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

        //setting toolbar
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle(stockSymbol);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        price.setText(getString(R.string.price,stockPrice));
        absoluteChange.setText(getString(R.string.absolute_change,"$"+stockAbsoluteChange));
        percentageChange.setText(stockPercentageChange+"%");
        if (Float.parseFloat(stockPercentageChange) > 0) {
            percentageChange.setBackgroundResource(R.drawable.percent_change_pill_green);
        } else {
            percentageChange.setBackgroundResource(R.drawable.percent_change_pill_red);
        }

//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
//                R.array.chart_intervals, android.R.layout.simple_spinner_item);
////        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//        intervalSpinner.setAdapter(adapter);
        setIntervalSpinner();
        intervalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                PrefUtils.setChartIntervalMode(getBaseContext(),i);
                setData();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        setData();
    }




    private ArrayList<Entry> setYAxisValues(){
        stockHistory= DBUtils.getHistory(this,symbol);
        ArrayList<Entry> yVals = new ArrayList<Entry>();
        for(int i=0 ; i<stockHistory.size(); i++){
            yVals.add(new Entry(i, stockHistory.get(i).second));
        }
        return yVals;
    }

    private void setData() {

        XAxis xAxis = stockLineChart.getXAxis();
        ArrayList<Entry> yVals = setYAxisValues();
        LineDataSet set1;
        // create a dataset and give it a type
        set1 = new LineDataSet(yVals, "stock history");
        set1.setFillAlpha(110);
        // set1.setFillColor(Color.RED);

        // set the line to be drawn like this "- - - - - -"
        // set1.enableDashedLine(10f, 5f, 0f);
        // set1.enableDashedHighlightLine(10f, 5f, 0f);
        set1.setColor(Color.CYAN);

        set1.setValueTextSize(2f);
        set1.setCircleColor(Color.WHITE);
        set1.setLineWidth(1f);
        set1.setCircleRadius(3f);
        set1.setDrawCircleHole(false);
        set1.setValueTextSize(15f);
        set1.setDrawFilled(true);
        set1.setValueTextColor(Color.GRAY);


        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return stockHistory.get( (int)value ).first;
            }
        });

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1);

        // create a data object with the datasets
        LineData data = new LineData(dataSets);

        stockLineChart.setData(data);
        stockLineChart.setBorderColor(Color.WHITE);
        stockLineChart.setDrawGridBackground(false);
        stockLineChart.setDrawBorders(true);
        stockLineChart.setBackgroundColor(Color.LTGRAY);
        stockLineChart.setNoDataTextColor(Color.WHITE);
        stockLineChart.invalidate(); // refresh


    }

    private void setIntervalSpinner (){
        int pos = PrefUtils.getChartIntervalCol(this);
        intervalSpinner.setSelection(pos-5);
    }


}
