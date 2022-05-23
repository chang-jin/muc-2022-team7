package com.snu.muc.dogeeye.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.snu.muc.dogeeye.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class EntityAdaptor extends RecyclerView.Adapter<EntityAdaptor.ViewHolder> {

    private DecimalFormat decimalFormat = new DecimalFormat("#.##m");
    private ArrayList<Project> mData = null;

    public EntityAdaptor(ArrayList<Project> list){
        mData = list;
    }

    public void setLogList(ArrayList<Project> list){
        mData = list;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView projectNum;
        TextView startTime;
        TextView endTime;
        TextView address;
        TextView totalStep;
        TextView totalDistance;
        TextView maxRange;

        ViewHolder(View itemView) {
            super(itemView) ;
            projectNum = itemView.findViewById(R.id.pid);
            startTime = itemView.findViewById(R.id.stime);
            endTime = itemView.findViewById(R.id.etime);
            address = itemView.findViewById(R.id.adr);
            totalStep = itemView.findViewById(R.id.step);
            totalDistance = itemView.findViewById(R.id.dis);
            maxRange = itemView.findViewById(R.id.range);
        }
    }

    @NonNull
    @Override
    public EntityAdaptor.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.recycler,parent,false);
        EntityAdaptor.ViewHolder vh = new EntityAdaptor.ViewHolder(view);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull EntityAdaptor.ViewHolder holder, int position) {
        Project project = mData.get(position);
        holder.projectNum.setText(String.valueOf(project.getId()));
        holder.startTime.setText(project.getStartTime());
        holder.endTime.setText(project.getEndTime());
        try{
            String[] tmp = project.getAddress().split("_");
            holder.address.setText(tmp[1]+"~\n"+tmp[2]);
        }catch (Exception e){
            holder.address.setText("Failed Log");
        }

        holder.totalStep.setText(String.valueOf((int) project.getTotalStep()));
        holder.totalDistance.setText(decimalFormat.format(project.getStart2EndDistance()));
        holder.maxRange.setText((decimalFormat.format(project.getStart2MaxDistance())));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
