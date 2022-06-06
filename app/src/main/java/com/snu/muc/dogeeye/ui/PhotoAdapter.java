package com.snu.muc.dogeeye.ui;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.snu.muc.dogeeye.R;
import com.snu.muc.dogeeye.model.PhotoEntity;

import java.io.File;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {

    private List<PhotoEntity> mData;
    private LayoutInflater mInflater;
    private Context mContext;

    // data is passed into the constructor
    PhotoAdapter(Context context, List<PhotoEntity> data) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.photo_item_holder, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        File imageFile = new File(mContext.getExternalMediaDirs()[0],
                mData.get(position).getFileName());
        if (imageFile.exists()) {
            // Set Image view
            holder.iv.setImageURI(Uri.fromFile(imageFile));
        } else {
            // Set placeholder
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv;

        ViewHolder(View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.photoImage);
        }
    }
}