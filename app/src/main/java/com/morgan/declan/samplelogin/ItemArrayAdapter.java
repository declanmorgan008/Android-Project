package com.morgan.declan.samplelogin;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class ItemArrayAdapter extends RecyclerView.Adapter<ItemArrayAdapter.ViewHolder> {

    private Context context;
    ArrayList<Post> targetsArrayList;
    private List<Upload> uploads;

    public ItemArrayAdapter(Context context, ArrayList<Post> mTargetData, List<Upload> uploads) {
        this.context = context;
        this.targetsArrayList = mTargetData;
        this.uploads = uploads;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item,viewGroup,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Log.e("Com.Declan.ONBINDVIEW", "" + i);
        Log.e("sizeOfUpload", "" + uploads.size());
        Upload upload = uploads.get(i);

        String pid_from_upload = upload.getPost_id();
        Glide.with(context).load(upload.getUrl()).into(viewHolder.imageView);
        for(int j=0; i<targetsArrayList.size(); j++){
            Log.e("Populate Post", ""+targetsArrayList.get(j).getPid() + "\t" + pid_from_upload);
            if(targetsArrayList.get(j).getPid().equals(pid_from_upload)){
                Log.e("Populate Post", ""+j);
                viewHolder.item.setText(targetsArrayList.get(j).getPostTitle());
                viewHolder.desc.setText(targetsArrayList.get(j).getDescription());
                return;
            }
        }

        //viewHolder.item.setText(targetsArrayList.get(i).getPostTitle());
        //viewHolder.desc.setText(targetsArrayList.get(i).getDescription());

    }

    @Override
    public int getItemCount() {
        if(targetsArrayList == null)
            return 0;
        return targetsArrayList.size();
    }

    // Static inner class to initialize the views of rows
    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView item, desc;
        protected ImageView imageView;
        public ViewHolder(View itemView) {
            super(itemView);
            item = (TextView) itemView.findViewById(R.id.item_title);
            desc = itemView.findViewById(R.id.item_desc);
            imageView = itemView.findViewById(R.id.list_item_iv);
        }

    }


}