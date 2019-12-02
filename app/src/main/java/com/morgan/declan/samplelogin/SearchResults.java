package com.morgan.declan.samplelogin;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.morgan.declan.samplelogin.ui.search.SearchFragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;

/**
 * Populates a recycler view with all posts from a specific search query.*/
public class SearchResults extends AppCompatActivity {


    private ArrayList<Post> postsFromSearchAL;
    private ArrayList<Upload> uploadsFromSearchAL;
    private RecyclerView search_rv;
    public RecyclerView.Adapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        postsFromSearchAL = new ArrayList<>();
        uploadsFromSearchAL = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        search_rv = findViewById(R.id.search_results_list);
        search_rv.setHasFixedSize(true);
        layoutManager.setReverseLayout(true);
        search_rv.setLayoutManager(layoutManager);
        mAdapter = new ItemArrayAdapter(getApplicationContext(), postsFromSearchAL, uploadsFromSearchAL, R.layout.dashboard_item);
        search_rv.setAdapter(mAdapter);
        Bundle extras = getIntent().getExtras();
        String titleText = extras.getString("titleSearch");
        String brandText = extras.getString("brandSearch");
        String colourText = extras.getString("colourText");
        postsFromSearchAL.clear();
        uploadsFromSearchAL.clear();

        //Call for a search on each field the user has entered text for.
        if(titleText != null){
            searchFirebase(titleText, "postTitle");
        }

        if(colourText != null){
            searchFirebase(colourText, "colour");
        }

        if(brandText != null){
            searchFirebase(brandText, "brand");
        }


    }


    /**
     * Searches Firebase Database for specific posts conforming to a search query.
     * @param searchType the type of search, e.g. title search, colour search or brand search
     * @param childName child node in Firebase relating to a specific search query.
     */

    public void searchFirebase(final String searchType, final String childName){
        final DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("posts");
        mRef.addValueEventListener(new ValueEventListener() {
            //Gets all posts relating to a specific query.
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    String pid = ds.getKey();
                    Query dbQuery = mRef.child(pid).orderByChild(childName).equalTo(searchType);
                    dbQuery.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot dsPosts : dataSnapshot.getChildren()){
                                //Add each post that is found from a query to an array list and
                                // retrieve images for the post.
                                Post postFromSearch = dsPosts.getValue(Post.class);
                                postsFromSearchAL.add(postFromSearch);
                                getImages(postFromSearch.getUid(), postFromSearch.getPid());
                                Log.e("DS", dsPosts.getValue().toString());
                            }
                            mAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    /**
     * Retrieves images relating to a specific post.
     * @param alUid User ID for a specific post.
     * @param alPid Post ID for a specific post.
     * */
    public void getImages(final String alUid, final String alPid){
        final DatabaseReference imageDBRef = FirebaseDatabase.getInstance().getReference().child("picture_uploads")
                .child(alUid).child(alPid);
        imageDBRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Add the image once it has been found to an array list.
                Upload userUpload = dataSnapshot.getValue(Upload.class);
                uploadsFromSearchAL.add(userUpload);
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
        mAdapter = new ItemArrayAdapter(getApplicationContext(),postsFromSearchAL, uploadsFromSearchAL, R.layout.dashboard_item);

        //adding adapter to recyclerview
        search_rv.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();
    }
}