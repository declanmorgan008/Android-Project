package com.morgan.declan.samplelogin.ui.notifications;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.morgan.declan.samplelogin.ItemArrayAdapter;
import com.morgan.declan.samplelogin.MainActivity;
import com.morgan.declan.samplelogin.Post;
import com.morgan.declan.samplelogin.R;
import com.morgan.declan.samplelogin.Upload;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel notificationsViewModel;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference userDBRef = FirebaseDatabase.getInstance().getReference();

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private DatabaseReference mDatabase;

    private ArrayList<Post> postList = new ArrayList<Post>();

    ArrayList<Post> mPostTargetData = new ArrayList<>();
    private List<Upload> uploads;

    private FirebaseRecyclerAdapter<Post, PostHolder> firebaseRecyclerAdapter;
    private FusedLocationProviderClient fusedLocationClient;

    private ImageView profilePic;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                ViewModelProviders.of(this).get(NotificationsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);
        setHasOptionsMenu(true);


        TextView uNameTv = root.findViewById(R.id.username);
        uNameTv.setText(mAuth.getCurrentUser().getDisplayName());

        profilePic = root.findViewById(R.id.profile_picture);


        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        recyclerView = root.findViewById(R.id.item_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        uploads = new ArrayList<>();
        recyclerView.setAdapter(mAdapter);
        fetchData();


        recyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("INFO", "Clicked on card.");
            }
        });


        return root;
    }

    @Override
    public void onResume(){
        super.onResume();
        mAdapter = new ItemArrayAdapter(getContext(),mPostTargetData, uploads, R.layout.dashboard_item);

        //adding adapter to recyclerview
        recyclerView.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStart(){
        super.onStart();
        fetchData();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_sign_out:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                return true;
        }
        return false;
    }

    public void fetchData(){
        System.out.println("******************" + mAuth.getUid());
        Uri photo_url = mAuth.getCurrentUser().getPhotoUrl();
        Glide.with(getContext()).load(photo_url).into(profilePic);
        String uid = mAuth.getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference().child("posts").child(uid);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mPostTargetData.clear();
                for (DataSnapshot single : dataSnapshot.getChildren()) {
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


        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("picture_uploads").child(uid);
        dRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Log.e("childrenCount", ""+dataSnapshot.getChildrenCount());
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Upload upload = postSnapshot.getValue(Upload.class);
                    uploads.add(upload);
                }

                Log.e("SizeOfUploads_DC" , "" + uploads.size());
                //creating adapter
                mAdapter = new ItemArrayAdapter(getContext(),mPostTargetData, uploads, R.layout.list_item);

                //adding adapter to recyclerview
                recyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private class PostHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView, descTextView;


        PostHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.item_title);
            descTextView = itemView.findViewById(R.id.item_desc);
        }

        void setPost(Post userPost) {
            String postTitle = userPost.getPostTitle();
            titleTextView.setText(postTitle);
            String desc = userPost.getDescription();
            descTextView.setText(desc);
        }

    }




}