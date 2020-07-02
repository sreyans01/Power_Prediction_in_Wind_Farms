package com.ibm.energyoptimizer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.ColorFilter;
import android.os.Bundle;
import android.view.View;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class FullPredictionActivity extends AppCompatActivity {
    private Context context = FullPredictionActivity.this;
    private RecyclerView predictionRecycler;
    private TimeSlotRecyclerAdapter adapter;
    private List<Double> powerOutputs;
    private List<String> timeslots;
    private String currTime;

    private BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_prediction);



        Intent i = getIntent();
        powerOutputs = (List<Double>) i.getSerializableExtra("PredictionList");
        timeslots = i.getStringArrayListExtra("TimeSlots");
        barChart = findViewById(R.id.barChart);
        makeBarChart();
        predictionRecycler = findViewById(R.id.predictionRecycler);
        setPredictionRecycler();
    }

    private void makeBarChart(){

        List<BarEntry> barEntries = new ArrayList<>();

        for(int i=0;i<72;i++){
            int powerOutput = (int) (powerOutputs.get(i)*3600);
            barEntries.add(new BarEntry(i+1,powerOutput));

        }

        BarDataSet barDataSet = new BarDataSet(barEntries,"Windfarm Power");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.9f);

        barChart.setVisibility(View.VISIBLE);
        barChart.animateY(5000);
        barChart.setData(barData);
        barChart.setFitBars(true);

        Description description = new Description();
        description.setText("Windfarm power output 72 hours prediction");
        barChart.setDescription(description);

    }

    private void setPredictionRecycler(){

        adapter = new TimeSlotRecyclerAdapter(context,timeslots,powerOutputs);
        predictionRecycler.setLayoutManager(new LinearLayoutManager(context));
        predictionRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    public List<String> generateTimeSlots(Integer n,Integer timediff){
        List<String> timeslots = new ArrayList<>();

        String currentTime = new SimpleDateFormat("dd-MMM-yyyy HH:mm aa", Locale.getDefault()).format(new Date());

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH:mm aa");
        Date d = null;
            try {
                d = df.parse(currentTime);
                Calendar cal = Calendar.getInstance();
                cal.setTime(d);

                for(int i =0;i<n;i++) {
                    String startTime = df.format(cal.getTime());
                    cal.add(Calendar.MINUTE, timediff);
                    String endTime = df.format(cal.getTime());
                    String slot = startTime +"-"+endTime;
                    timeslots.add(slot);
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }

        return timeslots;
    }
}