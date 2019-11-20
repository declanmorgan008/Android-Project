package com.morgan.declan.samplelogin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Html;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.morgan.declan.samplelogin.ui.home.HomeFragment;

import java.util.ArrayList;
import java.util.List;



public class ItemArrayAdapter extends RecyclerView.Adapter<ItemArrayAdapter.ViewHolder> {

    private Context context;
    ArrayList<Post> targetsArrayList;
     List<Upload> uploads;
    public Integer activityToPopulate;
    private Boolean dashboardPopuate;
    private DatabaseReference mDatabase;
    String userPhotoUri;
    ViewHolder vH;

    public ItemArrayAdapter(Context context, ArrayList<Post> mTargetData, List<Upload> uploads, Integer populateActivity) {
        this.context = context;
        this.targetsArrayList = mTargetData;
        this.uploads = uploads;
        this.activityToPopulate = populateActivity;
    }

    public ArrayList<Post> getPosts(){
        return this.targetsArrayList;
    }

    public List<Upload> getUploads(){
        return this.uploads;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v= LayoutInflater.from(viewGroup.getContext()).inflate(activityToPopulate,viewGroup,false);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        userPhotoUri = null;
        return new ViewHolder(v, activityToPopulate);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Log.e("Com.Declan.ONBINDVIEW", "" + i);
        Log.e("sizeOfUpload", "" + uploads.size());

        if(uploads.size() == 0){

        }else {


            Upload upload = uploads.get(i);

            String pid_from_upload = upload.getPost_id();
            Glide.with(context).load(upload.getUrl()).into(viewHolder.imageView);

            vH = viewHolder;

            if(activityToPopulate == R.layout.dashboard_item) {
                mDatabase.child("users").child(targetsArrayList.get(i).getUid()).child("photoUri").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        userPhotoUri = dataSnapshot.getValue(String.class);
//                User myUser = dataSnapshot.getValue(User.class);
//                userPhotoUri = myUser.photoUri;
                        Glide.with(context).load(userPhotoUri).into(vH.userDP);
//                        Log.e("uri: ", userPhotoUri);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                mDatabase.child("users").child(targetsArrayList.get(i).getUid()).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String username = dataSnapshot.getValue(String.class);
                        vH.username.setText(username);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            Post post = targetsArrayList.get(i);
            Log.e("Declan.com", "Is array list empty ????:" + post.getPostTitle());
            viewHolder.setDetails(post, activityToPopulate);
        }
    }

    @Override
    public int getItemCount() {
        if(targetsArrayList == null)
            return 0;
        return targetsArrayList.size();
    }

    // Static inner class to initialize the views of rows
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView item, desc, size, brand, condition, username;
        protected ImageView imageView, userDP;
        protected Integer activity;

        public ViewHolder(final View itemView, Integer test) {
            super(itemView);

           itemView.setOnClickListener(this);

            item = itemView.findViewById(R.id.item_title);
            desc = itemView.findViewById(R.id.item_desc);
            imageView = itemView.findViewById(R.id.imageView);
            userDP = itemView.findViewById(R.id.user_dp);
            username = itemView.findViewById(R.id.usernameTV);
            this.activity = test;
            Log.e("FRAGMENT", test.toString() + ", " + R.layout.fragment_dashboard);
            if(this.activity == R.layout.dashboard_item){
                Log.e("FRAGMENT", "size found");
                size = itemView.findViewById(R.id.item_size);
                brand = itemView.findViewById(R.id.item_brand);
                condition = itemView.findViewById(R.id.item_condition);
            }
        }


        public void setDetails (Post post, Integer activityToPopulate){
            String myDesc = "<b>" + "Description: " + "</b>" + post.getDescription();


            this.item.setText(post.getPostTitle());
            this.desc.setText(Html.fromHtml(myDesc));
            if(activityToPopulate == R.layout.dashboard_item){
                String mySize = "<b>" + "Size: " + "</b>" + post.getSize();
                String myBrand = "<b>" + "brand: " + "</b>" + post.getBrand();
                String myCondition = "<b>" + "Condition: " + "</b>" + post.getCondition();
                this.size.setText(Html.fromHtml(mySize));
                this.brand.setText(Html.fromHtml(myBrand));
                this.condition.setText(Html.fromHtml(myCondition));
            }

        }

        @Override
        public void onClick(View view) {
            Log.d("OnClickRecyclerView", "onClick " + getAdapterPosition()+ "" + brand);
            Intent intent = new Intent(context, ItemView.class);
            Integer pos = getAdapterPosition();
            Log.d("OnClickRecyclerView", desc.getText().toString());
            intent.putExtra("Description", getAdapterPosition());

            intent.putExtra("postTitle", targetsArrayList.get(pos).getPostTitle());
            intent.putExtra("postDescription", targetsArrayList.get(pos).getDescription());
            intent.putExtra("postSize", targetsArrayList.get(pos).getSize());
            intent.putExtra("postBrand", targetsArrayList.get(pos).getBrand());

            intent.putExtra("imageURL", uploads.get(pos).getUrl());
            intent.putExtra("uid", targetsArrayList.get(pos).getUid());

            context.startActivity(intent);
        }

    }
}