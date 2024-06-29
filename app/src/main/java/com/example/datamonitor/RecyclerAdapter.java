package com.example.datamonitor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private Context context;
    private ArrayList<AppModel> appModels;

//    MainActivity mainActivity=new MainActivity();
    public RecyclerAdapter(Context context, ArrayList<AppModel> appModels) {
        this.context = context;
        this.appModels = appModels;
        Collections.sort(appModels,new DataUsageComparator());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppModel app = appModels.get(position);
        holder.appName.setText(app.getAppName());
        holder.appCategory.setText(app.getCategory());
        holder.appLogo.setImageDrawable(app.getAppIcon());
        String size;
        float data=(float) app.getDataUsage();
        int i;
        for( i=0;i<4;i++){
            if(data>1000){
                data=data/1024;
            }
            else {
                break;
            }
        }
//        i--;
        if(i==0)
            size="KB";
        else if (i==1)
            size="KB";
        else if (i==2)
            size="MB";
        else if(i==3)
            size="GB";
        else
            size="TB";
        String formattedResult = String.format("%.1f", data);

        holder.dataUsage.setText(formattedResult+ " "+size);

    }

    @Override
    public int getItemCount() {
        return appModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView appLogo;
        TextView appName, appCategory, dataUsage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appLogo = itemView.findViewById(R.id.appLogo);
            appName = itemView.findViewById(R.id.appName);
            appCategory = itemView.findViewById(R.id.appCategory);
            dataUsage = itemView.findViewById(R.id.dataUsage);
        }
    }
}
