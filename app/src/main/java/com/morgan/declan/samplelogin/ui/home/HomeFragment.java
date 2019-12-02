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
import com.google.firebase.database.ValueEventListener;

import com.morgan.declan.samplelogin.ItemArrayAdapter;
import com.morgan.declan.samplelogin.Post;
import com.morgan.declan.samplelogin.R;
import com.morgan.declan.samplelogin.Upload;
import com.morgan.declan.samplelogin.ui.notifications.NotificationsViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Retrieves all posts from every user from Firebase and populates recyclerView in fragment
 * to display each post.*/
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


    /**
     * Fetch all necessary post data to show in recycler view in fragment.*/
    public void fetchData(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference().child("posts");
        final ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mPostTargetData.clear();
                for (DataSnapshot single : dataSnapshot.getChildren()) {
                    for (DataSnapshot second : single.getChildren()) {
                        //Add each retrieved post from Firebase to an array list.
                        Log.e("Posts collector", second.getValue().toString() + "\n");
                        Post target = second.getValue(Post.class);
                        mPostTargetData.add(target);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("Declan.com", "fetchData onCancelled", databaseError.toException());
            }
        };
        myRef.addValueEventListener(postListener);


        //Retrieve all Images associated with posts and store in array list.
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("picture_uploads");
        dRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Log.e("childrenCount", ""+dataSnapshot.getChildrenCount());
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    for(DataSnapshot secondSnapshot : postSnapshot.getChildren()){
                        //Add each image retrieved from Firebase to uploads array list.
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

    /**
     * Re initialises the recycler view adapter when user returns to the activity.*/
    @Override
    public void onResume(){
        super.onResume();
        mAdapter = new ItemArrayAdapter(getContext(),getmPostTargetData(), uploads, R.layout.dashboard_item);

        //adding adapter to recyclerview
        recyclerView.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();
    }

    /**
     * @return array list with all posts.*/
    public ArrayList<Post> getmPostTargetData(){
        return mPostTargetData;
    }

}