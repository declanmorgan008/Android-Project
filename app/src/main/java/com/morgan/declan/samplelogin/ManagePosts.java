package com.morgan.declan.samplelogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * Shows a recyclerView of all the current users posts.
 * Users can delete an item by clicking an item card, they are prompted to confirm deletion via an AlertDialog.
 * Upon deletion, posts are removed from Firebase database and storage.*/
public class ManagePosts extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    ArrayList<Post> mPostTargetData = new ArrayList<>();
    private List<Upload> uploads;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private Button deleteButton;
    private RecyclerViewClickListener deleteListener;
    private TextView deleteTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_posts);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        recyclerView = findViewById(R.id.item_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        deleteTV = findViewById(R.id.delete_posts_tv);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //On click of item in recyclerView, show alertDialog confirming deletion of post.
        deleteListener = new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, final Post postToDelete) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ManagePosts.this);
                builder.setTitle("Delete Item");
                builder.setMessage("Are you sure you wish to delete this item?");
                        builder.setCancelable(false);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (dialog != null) {
                            //Remove the item from Firebase and ArrayList
                            //Update TextView in activity.
                            Log.e("Post Delete", postToDelete.getPid());
                            deleteItem(postToDelete);
                            mPostTargetData.remove(postToDelete);
                            Snackbar.make(getWindow().getDecorView().getRootView(), "Post deleted successfully.", Snackbar.LENGTH_LONG).show();
                            dialog.dismiss();
                            updateTextView();
                        }
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i){
                        return;
                    }
                });
                builder.show();
            }
        };


        //creating adapter
        mAdapter = new ItemArrayAdapter(getApplicationContext(),mPostTargetData, uploads, R.layout.manage_list_item, deleteListener);

        uploads = new ArrayList<>();
        recyclerView.setAdapter(mAdapter);
        fetchData();
    }

    /**
     * Sets the option menu for the up button in activity
     * @param item item selected from options menu.*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        super.onOptionsItemSelected(item);
        //Finish the activity and return to previous fragment that called the activity to populate.
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }

        return true;
    }

    /**
     * Get user posts and add to ItemArrayAdapter.*/
    public void fetchData(){
        //Get user ID.
        String uid = mAuth.getUid();
        //Get firebase database reference.
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference().child("posts").child(uid);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //clear all previous user posts from arraylist.
                mPostTargetData.clear();
                for (DataSnapshot single : dataSnapshot.getChildren()) {
                    //add each post retrieved from firebase to user posts array list.
                    Post target =  single.getValue(Post.class);
                    mPostTargetData.add(target);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("Declan.com", "fetchData onCancelled", databaseError.toException());
            }
        };
        myRef.addValueEventListener(postListener);

        //Get all images relating to each user post.
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("picture_uploads").child(uid);
        dRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Log.e("childrenCount", ""+dataSnapshot.getChildrenCount());
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    //Add each image to an array list.
                    Upload upload = postSnapshot.getValue(Upload.class);
                    uploads.add(upload);
                }


                //creating adapter
                mAdapter = new ItemArrayAdapter(getApplicationContext(),mPostTargetData, uploads, R.layout.manage_list_item, deleteListener);

                //adding adapter to recyclerview
                recyclerView.setAdapter(mAdapter);

                updateTextView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Updates TextView in activity to inform user of no items to delete.*/
    public void updateTextView(){
        if(mPostTargetData.size() == 0){
            deleteTV.setText("No items to delete.");
        }
    }

    /**
     * Deletes a user post from Firebase.
     * @param post item to be deleted from Firebase and user accounts.*/
    public void deleteItem(final Post post){
        Log.e("Delete Item", post.toString());
        final Integer listSize = mPostTargetData.size();
        //Get user ID.
        String uid = mAuth.getUid();
        //Get firebase database reference.
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference().child("posts").child(uid);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot single : dataSnapshot.getChildren()) {
                    if(single.getKey().equals(post.getPid())){
                        //Remove the post from Firebase.
                        Log.e("Delete Post Loop", single.getKey() + ", " + post.getPid());
                        myRef.child(single.getKey()).removeValue();
                    }
                }
                //Update the adapter.
                mAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        myRef.addValueEventListener(postListener);

        //Removing the image from Firebase Storage.
        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("uploads/").child(post.getPid()+".jpg");
        mStorage.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.e("File Deleted: ", "Deleted image from Firebase");
            }
        });
        //Update TextView.
        updateTextView();
    }
}
