package com.morgan.declan.samplelogin.ui.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.morgan.declan.samplelogin.ItemArrayAdapter;

import com.google.firebase.auth.FirebaseAuth;

import com.morgan.declan.samplelogin.MainActivity;
import com.morgan.declan.samplelogin.Post;
import com.morgan.declan.samplelogin.R;
import com.morgan.declan.samplelogin.Upload;

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

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                ViewModelProviders.of(this).get(NotificationsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        TextView uNameTv = root.findViewById(R.id.username);
        uNameTv.setText(mAuth.getCurrentUser().getDisplayName());
        recyclerView = root.findViewById(R.id.item_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);



        uploads = new ArrayList<>();



        recyclerView.setAdapter(mAdapter);
        fetchData();


        Button clickButton = root.findViewById(R.id.signOutBtn);
        clickButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            }
        });

        Button retrieveUsername = root.findViewById(R.id.retrieve_username);
        retrieveUsername.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

            }
        });


        final TextView textView = root.findViewById(R.id.text_notifications);
        notificationsViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        return root;
    }


    public void fetchData(){
        String uid = mAuth.getCurrentUser().getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference().child("posts").child(uid);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mPostTargetData.clear();

//                Post target = dataSnapshot.getValue(Post.class);
//                mPostTargetData.add(target);
                for (DataSnapshot single : dataSnapshot.getChildren()) {
                    Post target =  single.getValue(Post.class);
                    mPostTargetData.add(target);
                }
                if(mPostTargetData.size() != 0){
                    mAdapter.notifyDataSetChanged();
                    Log.e("Declan.com", "Data received:" + mPostTargetData.size());
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("Declan.com", "fetchData onCancelled", databaseError.toException());
            }
        };
        myRef.addListenerForSingleValueEvent(postListener);


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
                mAdapter = new ItemArrayAdapter(getContext(),mPostTargetData, uploads);

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