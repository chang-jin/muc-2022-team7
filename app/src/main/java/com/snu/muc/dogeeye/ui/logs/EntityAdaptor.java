package com.snu.muc.dogeeye.ui.logs;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.snu.muc.dogeeye.R;
import com.snu.muc.dogeeye.common.TextSpeechModule;
import com.snu.muc.dogeeye.model.Project;
import com.snu.muc.dogeeye.model.ProjectDB;
import com.snu.muc.dogeeye.model.ProjectDao;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class EntityAdaptor extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private DecimalFormat decimalFormat = new DecimalFormat("#.##m");
    private ArrayList<logEntity> mData = null;
    private ProjectDao pDao;
    private ProjectDB pdb;
    private Context logContext;
    private TextSpeechModule module = null;

    public class ViewHolderLog extends ViewHolder {
        TextView timeStamp;
        TextView startLoc;
        TextView endLoc;
        int pid=0;
        int initialPos=0;

        ViewHolderLog(View itemView) {
            super(itemView) ;
            timeStamp = itemView.findViewById(R.id.time_stamp);
            startLoc = itemView.findViewById(R.id.adr_start);
//            endLoc = itemView.findViewById(R.id.adr_end);
            final boolean[] selected = {false};
            final String[] prevData = {"",""};

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int pos = getAbsoluteAdapterPosition();

                    if(selected[0])
                    {
//                        Log.d("entitySelection", "delete Log " + pos + " [" + pid + "]");
                        Project project = new Project();
                        project.setId(pid);
                        pDao.delProject(project);
                        mData.remove(pos);
                        module.textToSpeech("Delete the " + initialPos + "th activity");
                        selected[0] = false;
                        notifyDataSetChanged();
                    }
                    else
                    {
//                        Log.d("entitySelection","unselected Log "+pos+" [" + pid + "]");
                        Intent intent = new Intent(logContext,detailLogs.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("curProj",pid);
                        intent.putExtra("orderedNumber",initialPos);

                        logContext.startActivity(intent);
                    }

                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(selected[0])
                    {
                        selected[0] = false;
                        startLoc.setText(prevData[0]);
                        module.textToSpeech("The " + initialPos + "th activity selection cancelled");
                        return true;
                    }
                    else
                    {
                        selected[0] = true;
                        prevData[0] = (String) startLoc.getText();
                        startLoc.setText("Touch Once Again to Delete the Log");
                        module.textToSpeech("The "+ initialPos + "th activity is selected to delete "+ "   touch once again to delete");
                        return true;
                    }

                }
            });
        }
    }

    public class ViewHolderDiv extends ViewHolder {
        TextView date;

        ViewHolderDiv(View itemView) {
            super(itemView) ;
            date = itemView.findViewById(R.id.date_divider);
        }
    }

    public EntityAdaptor(ArrayList<logEntity> list, Context context){
        module = TextSpeechModule.getInstance();
        mData = list;
        logContext = context;
        pdb = ProjectDB.getProjectDB(context);
        pDao = pdb.projectDao();
    }

    public void setLogList(ArrayList<logEntity> list){
        mData = list;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ViewHolder vh = null;

        if(viewType == 0)
        {
            View view = inflater.inflate(R.layout.recycler,parent,false);
            vh = new EntityAdaptor.ViewHolderLog(view);
        }
        else if(viewType == 1)
        {
            View view = inflater.inflate(R.layout.date_divider,parent,false);
            vh = new EntityAdaptor.ViewHolderDiv(view);
        }

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        logEntity lgEntity = mData.get(position);

        if(lgEntity.getType() == 0)
        {
            Project project = lgEntity.getProject();
            ((ViewHolderLog) holder).initialPos = lgEntity.getActivityNumber()+1;
            ((ViewHolderLog) holder).pid = project.getId();
            ((ViewHolderLog) holder).timeStamp.setText(String.valueOf(lgEntity.getStartTimeRev()));
            int steps = (int) project.getTotalStep();
            String far = project.getFarLocName();

            try{
                String[] tmp = project.getAddress().split("_");
                String descr = "";
                descr += steps;
                descr += " steps to " + far;


                ((ViewHolderLog) holder).startLoc.setText(descr);


            }catch (Exception e){
                // String descr = "";
                // descr += " steps to ";

                // ((ViewHolderLog) holder).startLoc.setText(descr);
//                ((ViewHolderLog) holder).endLoc.setText("Failed Log");
            }
        }

        else if(lgEntity.getType() == 1)
        {
            ((ViewHolderDiv) holder).date.setText(lgEntity.getDate());
        }

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public int getItemViewType(int position) {

        logEntity tmp = mData.get(position);

        return tmp.getType();
    }
}
