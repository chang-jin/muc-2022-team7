package com.snu.muc.dogeeye.ui.logs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.snu.muc.dogeeye.R;
import com.snu.muc.dogeeye.model.LogEntity;

import java.util.ArrayList;
import java.util.List;

public class DetailAdaptor extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{

    private List<LogEntity> mData = null;

    public class entityContents extends RecyclerView.ViewHolder {
        TextView pos;
        TextView time;
        TextView loc;
        TextView step;

        entityContents(View itemView) {
            super(itemView) ;
            pos = itemView.findViewById(R.id.contentPos);
            time = itemView.findViewById(R.id.contentsTime);
            loc = itemView.findViewById(R.id.contentsLoc);
            step = itemView.findViewById(R.id.contentStep);
        }
    }

    public DetailAdaptor(List<LogEntity> list)
    {
        mData = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.entity_contents,parent,false);
        RecyclerView.ViewHolder vh = new DetailAdaptor.entityContents(view);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        String timeStamp = mData.get(position).getLogTime().split(" ")[1];
        timeStamp = timeStamp.split(":")[0] + " : " + timeStamp.split(":")[1];

        ((entityContents) holder).pos.setText(getItemCount()+"/"+(position+1));
        ((entityContents) holder).time.setText(timeStamp);
        ((entityContents) holder).loc.setText(mData.get(position).getLocName());
        ((entityContents) holder).step.setText(mData.get(position).getLocalStep()+" STEPS");
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
