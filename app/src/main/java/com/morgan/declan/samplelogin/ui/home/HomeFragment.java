package com.morgan.declan.samplelogin.ui.home;


import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Constants;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.morgan.declan.samplelogin.ItemArrayAdapter;
import com.morgan.declan.samplelogin.MainActivity;
import com.morgan.declan.samplelogin.Post;
import com.morgan.declan.samplelogin.R;
import com.morgan.declan.samplelogin.Upload;
import com.morgan.declan.samplelogin.User;
import com.morgan.declan.samplelogin.ui.notifications.NotificationsViewModel;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    //uri to store file
    private Uri filePath;
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();


    private NotificationsViewModel notificationsViewModel;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference userDBRef = FirebaseDatabase.getInstance().getReference();

    public RecyclerView recyclerView;
    public RecyclerView.Adapter mAdapter;
    public RecyclerView.LayoutManager layoutManager;

    public ArrayList<Post> postList = new ArrayList<Post>();

    public ArrayList<Post> mPostTargetData = new ArrayList<>();
    public List<Upload> uploads;
    public final ArrayList users = new ArrayList();

    private FirebaseRecyclerAdapter<Post, PostHolder> firebaseRecyclerAdapter;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseDatabase mref;
    private DatabaseReference mDatabase;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        recyclerView = root.findViewById(R.id.dashboard_list);
        recyclerView.setHasFixedSize(true);
        //layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        uploads = new ArrayList<>();
        recyclerView.setAdapter(mAdapter);
        fetchData();

        return root;
    }


    public void fetchData(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference myRef = database.getReference().child("posts");
        final ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mPostTargetData.clear();
                for (DataSnapshot single : dataSnapshot.getChildren()) {
                    for (DataSnapshot second : single.getChildren()) {

                        Log.e("Posts collector", second.getValue().toString() + "\n");
                        Post target = second.getValue(Post.class);
                        mPostTargetData.add(target);

                    }
                }
                for( int i=0; i<mPostTargetData.size()-1; i++){
                    Log.e("OUTPUT ARRAY LIST: ", mPostTargetData.get(i).getPostTitle());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("Declan.com", "fetchData onCancelled", databaseError.toException());
            }
        };
        myRef.addValueEventListener(postListener);



        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("picture_uploads");
        dRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Log.e("childrenCount", ""+dataSnapshot.getChildrenCount());
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    for(DataSnapshot secondSnapshot : postSnapshot.getChildren()){
                        Upload upload = secondSnapshot.getValue(Upload.class);
                        uploads.add(upload);
                    }

                }

                Log.e("SizeOfUploads_DC" , "" + uploads.size());
                //creating adapter
                mAdapter = new ItemArrayAdapter(getContext(),getmPostTargetData(), uploads, R.layout.dashboard_item);

                //adding adapter to recyclerview
                recyclerView.setAdapter(mAdapter);

                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    @Override
    public void onResume(){
        super.onResume();
        mAdapter = new ItemArrayAdapter(getContext(),getmPostTargetData(), uploads, R.layout.dashboard_item);

        //adding adapter to recyclerview
        recyclerView.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();
    }

    public ArrayList<Post> getmPostTargetData(){
        return mPostTargetData;
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