package com.morgan.declan.samplelogin;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.List;

/**
 * Array Adapter to display each post in an array list.*/
public class ItemArrayAdapter extends RecyclerView.Adapter<ItemArrayAdapter.ViewHolder> {

    private Context context;
    ArrayList<Post> targetsArrayList;
     List<Upload> uploads;
    public Integer activityToPopulate;
    private Boolean dashboardPopuate;
    private DatabaseReference mDatabase;
    String userPhotoUri;
    ViewHolder vH;
    private View.OnClickListener btnListener;
    private RecyclerViewClickListener itemListener;
    private static Location location = new Location("location");

    /**
     * Create a new array adapter and store parameters as local variables.
     * @param context activity context
     * @param mTargetData array list with all posts to be displayed.
     * @param populateActivity integer representing the activity to populate.
     * @param uploads array list with all images linked to posts to be displayed.*/
    public ItemArrayAdapter(Context context, ArrayList<Post> mTargetData, List<Upload> uploads, Integer populateActivity) {
        setLocation();
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

    /**
     * Create a new array adapter and store parameters as local variables.
     * This constructor is primarily used for managing posts via a click listener.
     * @param context activity context
     * @param mTargetData array list with all posts to be displayed.
     * @param populateActivity integer representing the activity to populate.
     * @param uploads array list with all images linked to posts to be displayed.
     * @param listener click listener for each card view in recycler view.*/
    public ItemArrayAdapter(Context context, ArrayList<Post> mTargetData, List<Upload> uploads, Integer populateActivity,
                            RecyclerViewClickListener listener) {
        this.context = context;
        this.targetsArrayList = mTargetData;
        this.uploads = uploads;
        this.activityToPopulate = populateActivity;
        this.itemListener = listener;
    }


    /**
     * Generates a view holder to store data in a card.
     * Inflates the layout and calls a constructor depending on the inflated activity layout.
     * @return ViewHolder instance to store data in view.*/
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v= LayoutInflater.from(viewGroup.getContext()).inflate(activityToPopulate,viewGroup,false);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        userPhotoUri = null;
        //Use the alternative constructor with click listener for managing items activity.
        if(activityToPopulate == R.layout.manage_list_item){
            return new ViewHolder(v, activityToPopulate, itemListener);
        }
        //Use default view holder constructor.
        return new ViewHolder(v, activityToPopulate);
    }

    /**
     * Populate the view holder for the array adapter. Populates with each post.*/
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        if(uploads.size() == 0){

        }else {
            Upload upload = uploads.get(i);
            //Load the corresponding post image to the image view.
            Glide.with(context).load(upload.getUrl()).into(viewHolder.imageView);
            vH = viewHolder;
            //If the activity we are viewing is the home fragment then use the dashboard item view.
            if(activityToPopulate == R.layout.dashboard_item) {
                //Get the image url from the user profile and display it in the view.
                mDatabase.child("users").child(targetsArrayList.get(i).getUid()).child("photoUri")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //Load user profile image into the imageview.
                        userPhotoUri = dataSnapshot.getValue(String.class);
                        Glide.with(context).load(userPhotoUri).apply(RequestOptions.circleCropTransform())
                                .into(vH.userDP);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                //Get the display name for each users post and display it in the textview for the post.
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

    /**
     * @return number of items in array list of posts.*/
    @Override
    public int getItemCount() {
        if(targetsArrayList == null)
            return 0;
        return targetsArrayList.size();
    }

    /**
     * Inner class to create a view holder to store data in recycler view.
     * class references view items in activity and populates them accordingly.*/
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView item, desc, size, brand, condition, username , location_address , distance;
        protected ImageView imageView, userDP;
        protected Button deleteButton;
        protected Integer activity;
        private RecyclerViewClickListener mListener;

        public ViewHolder(final View itemView, Integer test) {
            super(itemView);
            if(activityToPopulate == R.layout.manage_list_item){
                itemView.setOnClickListener(this);
            }else{
                itemView.setOnClickListener(this);
            }

            item = itemView.findViewById(R.id.item_title);
            desc = itemView.findViewById(R.id.item_desc);
            imageView = itemView.findViewById(R.id.imageView);
            userDP = itemView.findViewById(R.id.user_dp);
            username = itemView.findViewById(R.id.usernameTV);


            this.activity = test;
            Log.e("FRAGMENT", test.toString() + ", " + R.layout.fragment_dashboard);
            //If the activity is the home fragment then use the dashboard item view.
            if(this.activity == R.layout.dashboard_item){
                Log.e("FRAGMENT", "size found");
                size = itemView.findViewById(R.id.item_size);
                brand = itemView.findViewById(R.id.item_brand);
                condition = itemView.findViewById(R.id.item_condition);
                location_address = itemView.findViewById(R.id.item_location_address);
                distance = itemView.findViewById(R.id.distance);
            }
        }

        //Alternative constructor for use in managing posts with a click listener.
        public ViewHolder(final View itemView, Integer test, RecyclerViewClickListener listener) {
            super(itemView);
            mListener = listener;
            itemView.setOnClickListener(this);


            item = itemView.findViewById(R.id.item_title);
            desc = itemView.findViewById(R.id.item_desc);
            imageView = itemView.findViewById(R.id.imageView);
            userDP = itemView.findViewById(R.id.user_dp);
            username = itemView.findViewById(R.id.usernameTV);


            this.activity = test;
            Log.e("FRAGMENT", test.toString() + ", " + R.layout.fragment_dashboard);
            //If the activity is the home fragment then use the dashboard item view.
            if(this.activity == R.layout.dashboard_item){
                Log.e("FRAGMENT", "size found");
                size = itemView.findViewById(R.id.item_size);
                brand = itemView.findViewById(R.id.item_brand);
                condition = itemView.findViewById(R.id.item_condition);
            }
        }

        /**
         * Sets the details of each UI component to represent post details.
         *
         * @param activityToPopulate integer representing the activity to populate post information.
         * @param post the post to update the UI with.
         * */
        public void setDetails (Post post, Integer activityToPopulate){
            String myDesc = "<b>" + "Description: " + "</b>" + post.getDescription();
            this.item.setText(post.getPostTitle());
            this.desc.setText(Html.fromHtml(myDesc));
            if(activityToPopulate == R.layout.dashboard_item){
                String mySize = "<b>" + "Size: " + "</b>" + post.getSize();
                String myBrand = "<b>" + "brand: " + "</b>" + post.getBrand();
                String myCondition = "<b>" + "Condition: " + "</b>" + post.getCondition();
                String myLocation = "<b>" + "Location: " + "</b>" + post.getAddress();
                int d = getDistance(post); //D
                String myDistance = "<b>" + "Distance: " + "</b>" + d + "km";
                this.size.setText(Html.fromHtml(mySize));
                this.brand.setText(Html.fromHtml(myBrand));
                this.condition.setText(Html.fromHtml(myCondition));
                this.location_address.setText(Html.fromHtml(myLocation));
                this.distance.setText(Html.fromHtml(myDistance));
            }
        }

        /**
         * Calls an intent to view post details in another activity.
         * adds all post details to be shown in new activity to intent.*/
        @Override
        public void onClick(View view) {
            if(activityToPopulate == R.layout.manage_list_item) {
                mListener.onClick(view, targetsArrayList.get(getAdapterPosition()));
            }else{
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
    public void setLocation(){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Double lat = dataSnapshot.child("latitude").getValue(Double.class);
                Double lon = dataSnapshot.child("longitude").getValue(Double.class);
                setLocationSub(lat,lon);//C
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("NOTHING");
            }
        });

    }

    public void setLocationSub(Double lat, Double lon){
        if(lat!=null && lon!=null) {
            location.setLongitude(lon);
            location.setLatitude(lat);
        }
    }

    private Integer getDistance(Post post){

        Location loc2 = new Location("loc2");

        if(post.getLatitude()!=null && post.getLongitude()!=null) {
            loc2.setLatitude(post.getLatitude());
            loc2.setLongitude(post.getLongitude());
        }
        int d = Math.round(location.distanceTo(loc2)/1000);
        return d;

    }

    public static Location getLocation(){
        return location;
    }
}