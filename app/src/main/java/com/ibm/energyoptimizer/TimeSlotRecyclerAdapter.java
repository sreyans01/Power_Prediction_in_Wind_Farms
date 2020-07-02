package com.ibm.energyoptimizer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class TimeSlotRecyclerAdapter extends RecyclerView.Adapter<TimeSlotRecyclerAdapter.TimeSlotViewHolder> {

    private Context  context;
    private List<Double> powerOutputs;
    private List<String> timeslots;

    public TimeSlotRecyclerAdapter(Context context, List<String> timeslots, List<Double> powerOuts){

        this.context = context;
        this.timeslots = timeslots;
        this.powerOutputs = powerOuts;
    }

    @NonNull
    @Override
    public TimeSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_predictiondata,parent,false);

        return new TimeSlotViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeSlotViewHolder holder, int position) {


        holder.index.setText(String.valueOf(position+1));
        holder.timeslot.setText(timeslots.get(position));
        holder.powerOutput.setText(String.valueOf((int) (powerOutputs.get(position)*3600))+"KW");

    }

    @Override
    public int getItemCount() {
        return powerOutputs.size();
    }

    public class TimeSlotViewHolder extends RecyclerView.ViewHolder {
        TextView powerOutput,index,timeslot;

        public TimeSlotViewHolder(@NonNull View itemView) {
            super(itemView);

            powerOutput = itemView.findViewById(R.id.powerOutput);
            index = itemView.findViewById(R.id.index);
            timeslot = itemView.findViewById(R.id.timeslot);
        }
    }
}
